/**
 * ObisTool
 */
package gov.noaa.pmel.tmap.las.service.obis;

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
//not finished, not needed? import gov.noaa.pmel.tmap.las.jdom.LASObisBackendConfig; 
import gov.noaa.pmel.tmap.las.service.TemplateTool;

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
public class ObisTool extends TemplateTool {

    //LASObisBackendConfig obisBackendConfig;
    
    final Logger log = LogManager.getLogger(ObisTool.class.getName());
    
    /**
     * This default constructor uses the TemplateTool base class to initialize all
     * of its temporary and template directories.  
     * It also reads the config file for this service.
     *
     * @throws IOException
     * @throws LASException
     */
    public ObisTool() throws IOException, LASException {
        
        super("obis", "ObisBackendConfig.xml");
        //obisBackendConfig = new LASObisBackendConfig();
        //try {
        //    JDOMUtils.XML2JDOM(getConfigFile(), obisBackendConfig);
        //} catch (Exception e) {
        //    throw new LASException("Could not parse Obis config file: " + e.toString());
        //}
    }

    /**
     * This connects to the data source and creates the netCDF file for this request.
     *
     * @param backendRequest  I could make a special ObisBackendRequest,
     *    but the dapperBackendRequest has all the methods I need.
     * @return lasBackendResponse. 
     *   If an error occurs, this calls lasBackendResponse.setError,
     *   then returns the LASBackendResponse object.
     *   This won't throw an Exception.
     */
    public LASBackendResponse run(LASDapperBackendRequest backendRequest) {

        log.debug("Entered ObisTool.run method."); // debug

        //change ip (just used for identification of source of requests) 
        DigirHelper.SOURCE_IP = "161.55.64.5"; //ferret.pmel.noaa.gov

        //If exception occurs in try/catch block, 'causeOfError' was the cause.
        String causeOfError = "Unexpected error: "; 
        LASBackendResponse lasBackendResponse = new LASBackendResponse();
        try {      

            //First: has the request already been canceled (method 1)?
            if (backendRequest.isCancelRequest()) {           
                lasBackendResponse.setError("Obis backend request canceled.");
                causeOfError = "Obis backendRequest failed to cancel request: ";
                File cancel = new File(backendRequest.getResult("cancel"));
                cancel.createNewFile();
                log.info("Obis backend request canceled: " + backendRequest.toCompactString());
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

            //construct the resultsVariables and filter
            causeOfError = "Invalid 'variables' constraint.";
            String tFilter = backendRequest.getOpendapConstraint(); //"" if none
            log.debug("tFilter=" + tFilter);
            StringArray resultsVariables = new StringArray();
            StringArray filterVariables  = new StringArray();
            StringArray filterCops       = new StringArray();
            StringArray filterValues     = new StringArray();
            DigirHelper.parseQuery(
                String2.replaceAll(backendRequest.getVariablesAsString(), " ", "") + tFilter,                    
                resultsVariables, filterVariables, filterCops, filterValues);
            
            //get the resource codes
            causeOfError = "Unable to get required database properties.";
            String resources[] = 
                String2.split(backendRequest.getRequiredDatabaseProperty("db_table"), ',');
            if (resources.length == 0)
                throw new LASException(causeOfError + 
                    " 'db_table' must have 1 or more recource codes (comma-separated).");

            //add region constraints (except lon)
            //getDatabaseXlo/hi are modified to be -180 - 180, may be NaN
            double xLo = String2.parseDouble(backendRequest.getDatabaseXlo());  
            double xHi = String2.parseDouble(backendRequest.getDatabaseXhi());  
            String s = backendRequest.getYlo();
            if (s.length() > 0) {
                filterVariables.add(DigirHelper.DARWIN_PREFIX + ":Latitude");
                filterCops.add("greaterThanOrEquals");
                filterValues.add(s);
            }
            s = backendRequest.getYhi();
            if (s.length() > 0) {
                filterVariables.add(DigirHelper.DARWIN_PREFIX + ":Latitude");
                filterCops.add("lessThanOrEquals");
                filterValues.add(s);
            }
            s = backendRequest.getZlo();
            if (s.length() > 0) {
                filterVariables.add(DigirHelper.DARWIN_PREFIX + ":MinimumDepth");
                filterCops.add("greaterThanOrEquals");
                filterValues.add(s);
            }
            s = backendRequest.getZhi();
            if (s.length() > 0) {
                filterVariables.add(DigirHelper.DARWIN_PREFIX + ":MinimumDepth");
                filterCops.add("lessThanOrEquals");
                filterValues.add(s);
            }
            //apply final time constraint below (because time is a constructed column)
            s = backendRequest.getTlo();  //in Ferret format
            double tLoSec = s.length() == 0 ? Double.NaN : 
                Calendar2.gcToEpochSeconds(Calendar2.parseDDMonYYYYZulu(s)); //throws exception if trouble
            s = backendRequest.getThi();  //in Ferret format
            double tHiSec = s.length() == 0 ? Double.NaN : 
                Calendar2.gcToEpochSeconds(Calendar2.parseDDMonYYYYZulu(s)); //throws exception if trouble
            //apply crude time constraint here
            if (!Double.isNaN(tLoSec)) {
                filterVariables.add(DigirHelper.DARWIN_PREFIX + ":YearCollected");
                filterCops.add("greaterThanOrEquals");
                filterValues.add(Calendar2.epochSecondsToIsoStringT(tLoSec).substring(0, 4));
            }
            if (!Double.isNaN(tHiSec)) {
                filterVariables.add(DigirHelper.DARWIN_PREFIX + ":YearCollected");
                filterCops.add("lessThanOrEquals");
                filterValues.add(Calendar2.epochSecondsToIsoStringT(tHiSec).substring(0, 4));
            }

            //this sample url doesn't work in a browser, only via opendap library:
            causeOfError = "Could not create constraint expression in " + constraintFileName + ": ";
            String constraintSummary = 
                "url=" + url +
                "\nresources=" + String2.toCSVString(resources) +
                "\nresultsVariables=" + resultsVariables +
                "\nxLo=" + xLo +
                "\nxHi=" + xHi +
                "\nfilterVariables=" + filterVariables + 
                "\nfilterCops="      + filterCops + 
                "\nfilterValues="    + filterValues + 
                "\ntLo=" + backendRequest.getTlo() +
                "\ntHi=" + backendRequest.getThi() +
                "\n";
            Test.ensureEqual(String2.writeToFile(constraintFileName, constraintSummary), "", causeOfError);

            //get the data   
            causeOfError = "Could not convert the data source to a netCDF file: ";   
            Table data = new Table();
            if (!Double.isNaN(xLo) && !Double.isNaN(xHi) && xHi < xLo) {
                //split lon needs 2 queries; take care of >xLo
                //look at diagram in class javadoc above to understand this
                filterVariables.add(DigirHelper.DARWIN_PREFIX + ":Longitude");
                filterCops.add("greaterThanOrEquals");
                filterValues.add("" + xLo);
                DigirHelper.searchObis(resources, url,  
                    filterVariables.toArray(), filterCops.toArray(), filterValues.toArray(),
                    data, true, resultsVariables.toArray());                
                filterVariables.remove(filterVariables.size() - 1);
                filterCops.remove(filterVariables.size() - 1);
                filterValues.remove(filterVariables.size() - 1);
                xLo = Double.NaN; //it has been taken care of

                //was the request canceled?
                if (cancel != null && cancel.exists()) {
                    lasBackendResponse.setError("Request canceled.");
                    log.info("Request cancelled:" + backendRequest.toCompactString());
                    return lasBackendResponse;
                }            
            }
            //do the main data query
            //xlo and/or xhi may be specified, but they aren't xhi<xlo
            if (!Double.isNaN(xLo)) {
                filterVariables.add(DigirHelper.DARWIN_PREFIX + ":Longitude");
                filterCops.add("greaterThanOrEquals");
                filterValues.add("" + xLo);
            }
            if (!Double.isNaN(xHi)) {
                filterVariables.add(DigirHelper.DARWIN_PREFIX + ":Longitude");
                filterCops.add("lessThanOrEquals");
                filterValues.add("" + xHi);
            }
            DigirHelper.searchObis(resources, url,  
                filterVariables.toArray(), filterCops.toArray(), filterValues.toArray(),
                data, true, resultsVariables.toArray());                

            //apply time constraint afterwards (because time is a constructed column)
            //If only one was specified, set the other to an extreme, but finite, value.
            if (!Double.isNaN(tLoSec) && Double.isNaN(tHiSec)) tHiSec = Double.MAX_VALUE;
            if (Double.isNaN(tLoSec) && !Double.isNaN(tHiSec)) tLoSec = -Double.MAX_VALUE;
            if (!Double.isNaN(tLoSec))
                data.subset(new int[]{3}, new double[]{tLoSec}, new double[]{tHiSec});

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
                    for (int col = 0; col < resultsVariables.size(); col++)
                        data.addColumn(resultsVariables.get(col), new DoubleArray());
                }

                //add a row of missing values
                for (int col = 0; col < data.nColumns(); col++) 
                    data.getColumn(col).addString(""); //represents missing value

                data.setObisAttributes(0,1,2,3, url, resources,
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
