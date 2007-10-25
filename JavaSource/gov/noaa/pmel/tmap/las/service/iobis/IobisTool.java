/**
 * IobisTool
 */
package gov.noaa.pmel.tmap.las.service.iobis;

import com.cohort.array.DoubleArray;
import com.cohort.array.StringArray;
import com.cohort.util.Calendar2;
import com.cohort.util.Math2;
import com.cohort.util.MustBe;
import com.cohort.util.String2;
import com.cohort.util.Test;

import gov.noaa.pfel.coastwatch.pointdata.Table;
import gov.noaa.pfel.coastwatch.pointdata.DigirHelper;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASDapperBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
//not finished, not needed? import gov.noaa.pmel.tmap.las.jdom.LASIobisBackendConfig;  
import gov.noaa.pmel.tmap.las.service.TemplateTool;
import gov.noaa.pmel.tmap.las.util.Constraint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.velocity.VelocityContext;
import org.jdom.JDOMException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.units.DateUnit;
import ucar.nc2.units.SimpleUnit;

import com.cohort.array.PrimitiveArray;

/**
 * Creates an intermediate netCDF file from a Darwin2 or OBIS data source.  
 *     
 * <p>The associated template will create two constraint expressions if needed to 
 * collect data from the two "edges"
 * of the domain.
 * 
 *<pre>  
 *
 *             ------------------------------
 *             |                            |
 * xhi < xlo   |----x                 x-----|
 *             |                            |
 * xlo < xhi   |    x-----------------x     |
 *             |                            |
 *             ------------------------------
 *             
 *</pre>
 *  
 * @author Roland Schweitzer
 * @author Bob Simons (bob.simons@noaa.gov)
 *
 */
public class IobisTool extends TemplateTool {

    //LASIobisBackendConfig iobisBackendConfig;
    
    final Logger log = LogManager.getLogger(IobisTool.class.getName());
    
    /**
     * This default constructor uses the TemplateTool base class to initialize all
     * of its temporary and template directories.  
     * It also reads the config file for this service.
     *
     * @throws IOException
     * @throws LASException
     */
    public IobisTool() throws IOException, LASException {
        
        super("iobis", "IobisBackendConfig.xml");
        //iobisBackendConfig = new LASIobisBackendConfig();
        //try {
        //    JDOMUtils.XML2JDOM(getConfigFile(), iobisBackendConfig);
        //} catch (Exception e) {
        //    throw new LASException("Could not parse Iobis config file: " + e.toString());
        //}
    }

    /**
     * This connects to the data source and creates the netCDF file for this request.
     *
     * @param backendRequest  I could make a special IobisBackendRequest,
     *    but the dapperBackendRequest has all the methods I need.
     * @return lasBackendResponse. 
     *   If an error occurs, this calls lasBackendResponse.setError,
     *   then returns the LASBackendResponse object.
     *   This won't throw an Exception.
     */
    public LASBackendResponse run(LASDapperBackendRequest backendRequest) {

        log.debug("Entered IobisTool.run method."); // debug

        //change ip (just used for identification of source of requests) 
        DigirHelper.SOURCE_IP = "161.55.64.5"; //ferret.pmel.noaa.gov

        //If exception occurs in try/catch block, 'causeOfError' was the cause.
        String causeOfError = "Unexpected error: "; 
        LASBackendResponse lasBackendResponse = new LASBackendResponse();
        try {      

            //First: has the request already been canceled (method 1)?
            if (backendRequest.isCancelRequest()) {           
                lasBackendResponse.setError("Iobis backend request canceled.");
                causeOfError = "Iobis backendRequest failed to cancel request: ";
                File cancel = new File(backendRequest.getResult("cancel"));
                cancel.createNewFile();
                log.info("Iobis backend request canceled: " + backendRequest.toCompactString());
                return lasBackendResponse;
            }

            //has the request been canceled (method 2)?    
            String cancelFileName = backendRequest.getResult("cancel");
            File cancel = null;
            if (cancelFileName != null && !cancelFileName.equals("")) {
                causeOfError = "Unable to create cancelFileName=" + cancelFileName + ": ";
                cancel = new File(cancelFileName);
            }            
            //then standard check can be done any time
            if (cancel != null && cancel.exists()) {
                lasBackendResponse.setError("Request canceled.");
                log.info("Request canceled:" + backendRequest.toCompactString());
                return lasBackendResponse;
            }

            causeOfError = "Unable to get backend request's resultsAsFile(netcdf): ";
            String netcdfFilename = backendRequest.getResultAsFile("netcdf");
            log.debug("Got netcdf filename: " + netcdfFilename); // debug
            if (netcdfFilename == null || netcdfFilename.length() == 0)
                throw new LASException(causeOfError);

            causeOfError = "Could not get url from backend request: ";
            String url = backendRequest.getRootElement().getChild(
                "dataObjects").getChild("data").getAttributeValue("url");
            if (url == null || url.length() == 0) 
                throw new LASException(causeOfError);
            
            //if defined, use the "debug" resultsAsFile as the place to build the constraint statement.
            causeOfError = "Unable to getResultAsFile(debug): ";
            String constraintFileName = backendRequest.getResultAsFile("debug");
            if (constraintFileName == null || constraintFileName.length() == 0) {
                //make a constraintFile in resources/database/temp.
                causeOfError = "Could not find resources/database/temp directory: ";
                String temp_dir = getRequiredResourcePath("resources/database/temp");                
                constraintFileName = temp_dir + "constraint_" + System.currentTimeMillis() + ".con";
            }
            

            //collect the request parameters
            causeOfError = "Invalid constraint (must be genus= and/or species=).";
            ArrayList constraints = backendRequest.getConstraintsByType("variable");
            String genus = "";
            String species = "";
            for (int c = 0; c < constraints.size(); c++) {
                Constraint constraint = (Constraint)constraints.get(c);
                String cons = constraint.getAsString();
                if (cons.startsWith("genus="))
                    genus = constraint.getRhs();
                else if (cons.startsWith("species="))
                    species = constraint.getRhs();
                else 
                    throw new LASException("Invalid constraint: " + cons);
            }
    
            //add region constraints 
            //getDatabaseXlo/hi are modified to be -180 - 180, may be ""
            causeOfError = "Unable to get required database properties.";
            String resultsVariables[] = String2.split(backendRequest.getVariablesAsString(), ',');
            String xlo = backendRequest.getDatabaseXlo();  
            String xhi = backendRequest.getDatabaseXhi();  
            String ylo = backendRequest.getYlo();
            String yhi = backendRequest.getYhi();
            String zlo = backendRequest.getZlo();
            String zhi = backendRequest.getZhi();
            String s = backendRequest.getTlo();  //in Ferret format
            String tlo = s.length() == 0 ? "" : 
                Calendar2.formatAsISODateTimeSpace(Calendar2.parseDDMonYYYYZulu(s)); //throws exception if trouble
            s = backendRequest.getThi();  //in Ferret format
            String thi = s.length() == 0 ? "" : 
                Calendar2.formatAsISODateTimeSpace(Calendar2.parseDDMonYYYYZulu(s)); //throws exception if trouble

            //this sample url doesn't work in a browser, only via opendap library:
            causeOfError = "Could not create constraint expression in " + constraintFileName + ": ";
            String constraintSummary = 
                "url=" + url +
                "\ngenus=" + genus +
                "\nspecies=" + species +
                "\nxLo=" + xlo +
                "\nxHi=" + xhi +
                "\nyLo=" + ylo +
                "\nyHi=" + yhi +
                "\nzLo=" + zlo +
                "\nzHi=" + zhi +
                "\ntLo=" + tlo +
                "\ntHi=" + thi +
                "\n";
            log.debug(constraintSummary);
            Test.ensureEqual(String2.writeToFile(constraintFileName, constraintSummary), "", causeOfError);

            //get the data   
            causeOfError = "Could not convert the data source to a netCDF file: ";   
            Table data = new Table();
            if (xlo.length() > 0 && xhi.length() > 0 && 
                String2.parseDouble(xhi) < String2.parseDouble(xlo)) {
                //split lon needs 2 queries; take care of >xlo
                //look at diagram in class javadoc above to understand this
                data.readIobis(url, 
                    genus, species, xlo, "", ylo, yhi, zlo, zhi, tlo, thi,
                    resultsVariables);                
                xlo = ""; //it has been taken care of

                //was the request canceled?
                if (cancel != null && cancel.exists()) {
                    lasBackendResponse.setError("Request canceled.");
                    log.info("Request cancelled:" + backendRequest.toCompactString());
                    return lasBackendResponse;
                }            
            }
            //do the main data query
            //xlo and/or xhi may be specified, but they aren't xhi<xlo
            Table tTable = data;
            if (data.nColumns() > 0) //put in temp table if already data in 'data'
                tTable = new Table();
            data.readIobis(url,  
                genus, species, xlo, xhi, ylo, yhi, zlo, zhi, tlo, thi,
                resultsVariables);                
            if (tTable != data) {
                data.append(tTable); //they should have the same columns
                data.setObisAttributes(0,1,2,3, url, 
                    new String[]{"AdvancedQuery"},
                    "OBIS_" + Math2.reduceHashCode(constraintSummary.hashCode()));
                tTable = null;
            }

            //was the request canceled?
            if (cancel != null && cancel.exists()) {
                lasBackendResponse.setError("Request canceled.");
                log.info("Request cancelled:" + backendRequest.toCompactString());
                return lasBackendResponse;
            }
            
            //the table already has metadata, no need to enhance
            //causeOfError = "Could not enhance netCDF file metadata: ";
            //enhance(backendRequest, data);
            
            //make table with 1 mv row if no results were returned
            log.debug("found nRows=" + data.nRows());
            if (data.nRows() == 0) {

                //if no columns, add a time column
                causeOfError = "Could not create empty netCDF file: ";
                if (data.nColumns() == 0) { //Dapper likes these, all uppercase names
                    data.addColumn("LON",   new DoubleArray());
                    data.addColumn("LAT",   new DoubleArray());
                    data.addColumn("DEPTH", new DoubleArray());
                    data.addColumn("TIME",  new DoubleArray());
                    data.addColumn("ID",    new StringArray());
                    for (int col = 0; col < resultsVariables.length; col++)
                        data.addColumn(resultsVariables[col], new DoubleArray());
                }

                //add a row of missing values
                for (int col = 0; col < data.nColumns(); col++) 
                    data.getColumn(col).addString(""); //represents missing value

                data.setObisAttributes(0,1,2,3, url, 
                    new String[]{"AdvancedQuery"},
                    "OBIS_" + Math2.reduceHashCode(constraintSummary.hashCode()));

            } else {
                //log.debug("first up-to-100 rows found: " + data.toString("rows", 100));
            }
            causeOfError = "Could not write netCDF file to disk: ";
            data.saveAsFlatNc(netcdfFilename, "row");

            // The service just wrote the file to the requested location so
            // copy the response element from the request to the response.
            causeOfError = "Failed to set response element: ";
            lasBackendResponse.addResponseFromRequest(backendRequest);

        } catch (Exception e) {
            log.info(MustBe.throwableToString(e)); //e.toString + "\n" + String2.toNewlineString(e.getStackTrace()));
            lasBackendResponse.setError(causeOfError, e);
        }

        return lasBackendResponse;
    }

    /**
     * This returns a required resourcePath.
     * [This method could be part of TemplateTool.]
     *
     * @param propertyName
     * @return the reqested resourcePath.
     * @throws LASException if the property isn't found or is "".
     */
    public String getRequiredResourcePath(String propertyName) throws LASException {
    	return LASDapperBackendRequest.required(getResourcePath(propertyName), 
            "resource path \"" + propertyName + "\"");
    }



}
