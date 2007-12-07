/**
 * TabledapTool
 */
package gov.noaa.pmel.tmap.las.service.tabledap;

import com.cohort.array.DoubleArray;
import com.cohort.array.StringArray;
import com.cohort.util.Calendar2;
import com.cohort.util.Math2;
import com.cohort.util.MustBe;
import com.cohort.util.String2;
import com.cohort.util.Test;

import gov.noaa.pfel.coastwatch.pointdata.Table;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASTabledapBackendConfig;  
import gov.noaa.pmel.tmap.las.service.TemplateTool;
import gov.noaa.pmel.tmap.las.util.Constraint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.jdom.Element;

import com.cohort.array.PrimitiveArray;

/**
 * Creates an intermediate netCDF file from a Tabledap dataset
 * (http://coastwatch.pfel.noaa.gov/coastwatch/erddap/tabledap/index.html).
 *     
 * <p>The associated template will create two constraint expressions if needed to 
 * collect data from the two "edges" of the domain.
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
public class TabledapTool extends TemplateTool {

    LASTabledapBackendConfig tabledapBackendConfig;  //force its compilation
    
    final Logger log = LogManager.getLogger(TabledapTool.class.getName());
    
    /**
     * This default constructor uses the TemplateTool base class to initialize all
     * of its temporary and template directories.  
     * It also reads the config file for this service.
     *
     * @throws IOException
     * @throws LASException
     */
    public TabledapTool() throws IOException, LASException {
        
        super("tabledap", "TabledapBackendConfig.xml");
        //tabledapBackendConfig = new LASTabledapBackendConfig();
        //try {
        //    JDOMUtils.XML2JDOM(getConfigFile(), tabledapBackendConfig);
        //} catch (Exception e) {
        //    throw new LASException("Could not parse Tabledap config file: " + e.toString());
        //}
    }

    /**
     * This connects to the data source and creates the netCDF file for this request.
     *
     * @param lasBackendRequest  
     * @return lasBackendResponse. 
     *   If an error occurs, this calls lasBackendResponse.setError,
     *   then returns the LASBackendResponse object.
     *   This won't throw an Exception.
     */
    public LASBackendResponse run(LASBackendRequest lasBackendRequest) {

        log.debug("Entered TabledapTool.run method."); // debug

        //If exception occurs in try/catch block, 'causeOfError' was the cause.
        String causeOfError = "Unexpected error: "; 
        LASBackendResponse lasBackendResponse = new LASBackendResponse();
        try {      

            //First: has the request already been canceled (method 1)?
            if (lasBackendRequest.isCancelRequest()) {           
                lasBackendResponse.setError("Tabledap backend request canceled.");
                causeOfError = "Tabledap lasBackendRequest failed to cancel request: ";
                File cancel = new File(lasBackendRequest.getResult("cancel"));
                cancel.createNewFile();
                log.info("Tabledap backend request canceled: " + lasBackendRequest.toCompactString());
                return lasBackendResponse;
            }

            //has the request been canceled (method 2)?    
            String cancelFileName = lasBackendRequest.getResult("cancel");
            File cancel = null;
            if (cancelFileName != null && cancelFileName.length() > 0) {
                causeOfError = "Unable to create cancelFileName=" + cancelFileName + ": ";
                cancel = new File(cancelFileName);
            }            
            //then standard check can be done any time
            if (isCanceled(cancel, lasBackendRequest, lasBackendResponse))
                return lasBackendResponse;

            //get the name for the .nc results file
            causeOfError = "Unable to get backend request's resultsAsFile(netcdf): ";
            String netcdfFilename = lasBackendRequest.getResultAsFile("netcdf");
            log.debug("Got netcdf filename: " + netcdfFilename);
            required(netcdfFilename, causeOfError);

            //get url (almost always ending in "/tabledap/")
            causeOfError = "Could not get url from backend request: "; 
            String url = lasBackendRequest.getRootElement().getChild(
                "dataObjects").getChild("data").getAttributeValue("url");
            required(url, causeOfError);

            //get db_name (what tabledap calls websiteID), e.g., pmel_dapper
            causeOfError = "Could not get db_name."; 
            String websiteID = lasBackendRequest.getDatabaseProperty("db_name");
            log.debug("Got db_name/websiteID: " + websiteID); 
            required(websiteID, causeOfError);

            //get db_table (what tabledap calls datasetID), e.g., tao
            causeOfError = "Could not get db_table.";   
            String datasetID = lasBackendRequest.getDatabaseProperty("db_table");
            log.debug("Got db_table/datasetID: " + datasetID);
            required(websiteID, causeOfError);
       
            //get "debug" file name, may be null or ""
            //if defined, use the "debug" resultsAsFile as the place to save the constraint statement.
            causeOfError = "Unable to getResultAsFile(debug): ";
            String constraintFileName = lasBackendRequest.getResultAsFile("debug"); 
            
            //create the query.   First: variables
            StringBuffer query = new StringBuffer();
            //get axis var names   ("" if not available from dataset)
            if (lasBackendRequest.getDatabaseProperty("longitude").length() > 0) query.append("longitude,");
            if (lasBackendRequest.getDatabaseProperty("latitude").length() > 0)  query.append("latitude,");
            if (lasBackendRequest.getDatabaseProperty("depth").length() > 0)     query.append("altitude,");
            if (lasBackendRequest.getDatabaseProperty("time").length() > 0)      query.append("time,");
            //response variables   (no trailing comma)
            query.append(String2.replaceAll(lasBackendRequest.getVariablesAsString(), " ", ""));

            //then variable constraints  
            List constraintElements = lasBackendRequest.getRootElement().getChildren("constraint");
            Iterator cIt = constraintElements.iterator(); 
            while (cIt.hasNext()) {
                Element constraint = (Element) cIt.next();
                String tType = constraint.getAttributeValue("type");
                if (tType == null || !tType.equals("variable")) //get the "variable" constraints
                    continue;
                String rhsString = constraint.getChildText("rhs");
                String lhsString = constraint.getChildText("lhs");
                String opString = constraint.getChildText("op");  //some sort of text format
                Constraint c = new Constraint(lhsString, opString, rhsString);                
                query.append("&" + c.getAsString());  //op is now <, <=, ...
            }
    
            //get region constraints 
            causeOfError = "Unable to get required database properties.";
            String xlo = lasBackendRequest.getXlo();  //don't constrain to +-180?  getDatabaseXlo?
            String xhi = lasBackendRequest.getXhi();  
            String ylo = lasBackendRequest.getYlo();
            String yhi = lasBackendRequest.getYhi();
            String zlo = lasBackendRequest.getZlo();
            String zhi = lasBackendRequest.getZhi();
            String s = lasBackendRequest.getTlo();  //in Ferret format
            String tlo = s.length() == 0 ? "" : 
                Calendar2.formatAsISODateTimeT(Calendar2.parseDDMonYYYYZulu(s)); //throws exception if trouble
            s = lasBackendRequest.getThi();  //in Ferret format
            String thi = s.length() == 0 ? "" : 
                Calendar2.formatAsISODateTimeT(Calendar2.parseDDMonYYYYZulu(s)); //throws exception if trouble

            //add region constraints other than lon
            if (ylo.length() > 0) query.append("&latitude>=" + ylo);
            if (yhi.length() > 0) query.append("&latitude<=" + yhi);
            if (zlo.length() > 0) query.append("&altitude>=" + zlo);
            if (zhi.length() > 0) query.append("&altitude<=" + zhi);
            if (tlo.length() > 0) query.append("&time>=" + tlo);
            if (thi.length() > 0) query.append("&time<=" + thi);

            //store constraint in debug file
            causeOfError = "Could not create constraint expression in " + constraintFileName + ": ";
            String querySummary = "query=" + query.toString() + " xlo=" + xlo + " xhi=" + xhi;
            log.debug(querySummary);
            if (constraintFileName != null && constraintFileName.length() > 0)
                Test.ensureEqual(String2.writeToFile(constraintFileName, querySummary), "", causeOfError);

            //get the data   
            causeOfError = "Could not convert the data source to a netCDF file: ";   
            String dsUrl = url + websiteID + "/" + datasetID + "?";  //don't include ".dods"; readOpendapSequence does that
            Table data = new Table();
            if (xlo.length() > 0 && xhi.length() > 0 && 
                String2.parseDouble(xhi) < String2.parseDouble(xlo)) {
                //split lon needs 2 queries; take care of >xlo
                //look at diagram in class javadoc above to understand this
                try {
                    data.readOpendapSequence(dsUrl + query.toString() + "&longitude>=" + xlo);  //errors are common
                } catch (Exception e) {
                    causeOfError = "Data source error: " + e.toString();
                    throw e;
                }
                xlo = ""; //it has been taken care of

                //was the request canceled?
                if (isCanceled(cancel, lasBackendRequest, lasBackendResponse))
                    return lasBackendResponse;
            }
            //do the main data query
            //xlo and/or xhi may be specified, but they aren't xhi<xlo
            Table tTable = data;
            if (data.nColumns() > 0) //put in temp table if already data in 'data'
                tTable = new Table();
            if (xlo.length() > 0) query.append("&longitude>=" + xlo);
            if (xhi.length() > 0) query.append("&longitude<=" + xhi);
            try {
                data.readOpendapSequence(dsUrl + query.toString());  //errors are common
            } catch (Exception e) {
                causeOfError = "Data source error: " + e.toString();
                throw e;
            }
            if (tTable != data) {
                data.append(tTable); //they should have the same columns
                tTable = null;
            }

            //was the request canceled?
            if (isCanceled(cancel, lasBackendRequest, lasBackendResponse))
                return lasBackendResponse;
            
            //make table with 1 mv row if no results were returned
            log.debug("found nRows=" + data.nRows());
            if (data.nRows() == 0) { //possibly no columns, too

                //if no columns, create them
                causeOfError = "Could not create empty table: ";
                if (data.nColumns() == 0) { 
                    ArrayList varNames = lasBackendRequest.getVariables();
                    for (int col = 0; col < varNames.size(); col++)
                        data.addColumn(varNames.get(col).toString(), new DoubleArray()); //lame to assume all are doubles
                }

                //add a row of missing values
                for (int col = 0; col < data.nColumns(); col++) 
                    data.getColumn(col).addString(""); //represents missing value
            } 

            //(don't) convert from Tabledap-style file to LAS Intermediate File-style
            int lonCol  = data.findColumnNumber("longitude"); //names are standardized in tabledap
            int latCol  = data.findColumnNumber("latitude");
            int altCol  = data.findColumnNumber("altitude");
            int timeCol = data.findColumnNumber("time");
            //if (lonCol >= 0) data.setColumnName(lonCol, "LON");
            //if (latCol >= 0) data.setColumnName(latCol, "LAT");
            //if (altCol >= 0) {
            //    data.setColumnName(altCol, "DEPTH");
            //    data.getColumn(altCol).scaleAddOffset(-1, 0); //altitude -> depth (metadata changed below)
            //}
            //if (timeCol >= 0) data.setColumnName(timeCol, "TIME");
            //data.globalAttributes().set("Conventions", "LAS Intermediate netCDF File, Unidata Observation Dataset v1.0");

            //set actuaRangeAndBoundingBox
            data.setActualRangeAndBoundingBox(lonCol, latCol, altCol, timeCol, "Time");
log.debug("first up-to-100 rows found: " + data.toString("rows", 100));

            causeOfError = "Could not write netCDF file to disk: ";
            data.saveAsFlatNc(netcdfFilename, "row");

            // The service just wrote the file to the requested location so
            // copy the response element from the request to the response.
            causeOfError = "Failed to set response element: ";
            lasBackendResponse.addResponseFromRequest(lasBackendRequest);

        } catch (Exception e) {
            log.info(MustBe.throwableToString(e));
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
    	return required(getResourcePath(propertyName), 
            "resource path \"" + propertyName + "\"");
    }

    /**
     * This throws an LASException("Required value wasn't specified: " + id)
     * if s is null or "".
     * [This method could be in LASBackendRequest.]
     *
     * @param s a string which may be null or ""
     * @param id e.g., "database_access property 'url'"
     * @return s (for convenience)
     * @throws LASException("Required value wasn't specified: " + id)
     * if s is null or "".
     */
    public static String required(String s, String id) throws LASException {
        if (s == null || s.equals("")) 
            throw new LASException ("Required value wasn't specified: " + id + ".");
        return s;
    }

    /**
     * This checks if the request was canceled and caller should return.
     *
     * @return true if canceled and caller should return the lasBackendResponse.
     */
    protected boolean isCanceled(File cancel, LASBackendRequest lasBackendRequest,
        LASBackendResponse lasBackendResponse) {

        //was the request canceled?
        if (cancel != null && cancel.exists()) {
            lasBackendResponse.setError("Request canceled.");
            log.info("Request cancelled:" + lasBackendRequest.toCompactString());
            return true;
        }
        return false;
    }

}
