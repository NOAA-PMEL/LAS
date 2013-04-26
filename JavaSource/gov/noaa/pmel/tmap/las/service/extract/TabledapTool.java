/**
 * TabledapTool
 */
package gov.noaa.pmel.tmap.las.service.extract;

import gov.noaa.pfel.coastwatch.pointdata.Table;
import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASTabledapBackendConfig;
import gov.noaa.pmel.tmap.las.service.TemplateTool;
import gov.noaa.pmel.tmap.las.ui.LASProxy;
import gov.noaa.pmel.tmap.las.util.Constraint;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;

import com.cohort.util.Calendar2;
import com.cohort.util.MustBe;
import com.cohort.util.String2;
import com.cohort.util.Test;


/**
 * Creates an intermediate netCDF file from a Tabledap dataset
 * (http://coastwatch.pfel.noaa.gov/erddap/tabledap/index.html).
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
    
    LASProxy lasProxy = new LASProxy();

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
                log.debug("Tabledap backend request canceled: " + lasBackendRequest.toCompactString());
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

            causeOfError = "Could not get trajectory id from backend request: "; 
            String cruiseid = getTabledapProperty(lasBackendRequest, "trajectory_id");
            required(cruiseid, causeOfError);
            
            causeOfError = "Could not get tiem column name from backend request: "; 
            String time = getTabledapProperty(lasBackendRequest, "time");
            required(time, causeOfError);
            
            String latname = getTabledapProperty(lasBackendRequest, "latitude");
            String lonname = getTabledapProperty(lasBackendRequest, "longitude");
            String zname = getTabledapProperty(lasBackendRequest, "altitude");

            //get id, e.g., pmel_dapper/tao   
            causeOfError = "Could not get id."; 
            String id = getTabledapProperty(lasBackendRequest, "id");
            log.debug("Got id: " + id); 
            required(id, causeOfError);
     
            //get "debug" file name, may be null or ""
            //if defined, use the "debug" resultsAsFile as the place to save the constraint statement.
            causeOfError = "Unable to getResultAsFile(debug): ";
            String constraintFileName = lasBackendRequest.getResultAsFile("debug"); 
            
            //create the query.   First: variables
            StringBuffer query = new StringBuffer();
            // Apparently ERDDAP gets mad of you include lat, lon, z or time in the list of variables so just list the "data" variables.
            query.append(String2.replaceAll(lasBackendRequest.getVariablesAsString(), " ", ""));

            //then variable constraints  
            List constraintElements = lasBackendRequest.getRootElement().getChildren("constraint");
            Iterator cIt = constraintElements.iterator(); 
            while (cIt.hasNext()) {
                Element constraint = (Element) cIt.next();
                String tType = constraint.getAttributeValue("type");
                if ( tType.equals("variable") ) {
                    String rhsString = constraint.getChildText("rhs");
                    String lhsString = constraint.getChildText("lhs");
                    String opString = constraint.getChildText("op");  //some sort of text format
                    Constraint c = new Constraint(lhsString, opString, rhsString);                
                    query.append("&" + c.getAsString());  //op is now <, <=, ...
                } else if ( tType.equals("text") ) {
                    String rhsString = constraint.getChildText("rhs");
                    String lhsString = constraint.getChildText("lhs");
                    String opString = constraint.getChildText("op");  //some sort of text format
                    Constraint c = new Constraint(lhsString, opString, rhsString);                
                    query.append("&" + c.getAsERDDAPString());  //op is now <, <=, ...
                }
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
            String dsUrl = url + id + ".ncCF?";  //don't include ".dods"; readOpendapSequence does that
            File part1 = new File(netcdfFilename+".part1");
//            Table data = new Table();
            DateTime dt = new DateTime();
            DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
            if (xlo.length() > 0 && xhi.length() > 0 ) {
                
                // This little exercise will normalize the x values to -180, 180.
                double xhiDbl = String2.parseDouble(xhi);
                double xloDbl = String2.parseDouble(xlo);
                LatLonPoint p = new LatLonPointImpl(0, xhiDbl);
                xhiDbl = p.getLongitude();
                p = new LatLonPointImpl(0, xloDbl);
                xloDbl = p.getLongitude();
                
                // Now a wrap around from west to east should be have xhi < xlo;
                if ( xhiDbl < xloDbl ) {
                    //split lon needs 2 queries; take care of >xlo
                    //look at diagram in class javadoc above to understand this
                    try {
                        query.append("&longitude>=" + xlo);
                        String q = URLEncoder.encode(query.toString(), "UTF-8").replaceAll("\\+", "%20");
                        String firstUrl = dsUrl + q;
                        dt = new DateTime();
                        log.info("{TableDapTool starting file pull for part 1 at "+fmt.print(dt));
                        lasProxy.executeGetMethodAndSaveResult(firstUrl, part1, null);
                        dt = new DateTime();
                        log.info("{TableDapTool finished file pull for part 1 at "+fmt.print(dt));
                    } catch (Exception e) {
                        String message = e.getMessage();
                        if ( e.getMessage().contains("com.cohort") ) {
                            message = message.substring(message.indexOf("com.cohort.util.SimpleException: "), message.length());
                            message = message.substring(0, message.indexOf(")"));
                        }
                        causeOfError = "Data source error: " + message;
                        throw new Exception(message);
                    }
                    xlo = ""; //it has been taken care of

                    //was the request canceled?
                    if (isCanceled(cancel, lasBackendRequest, lasBackendResponse))
                        return lasBackendResponse;
                }
        }
//            //do the main data query
//            //xlo and/or xhi may be specified, but they aren't xhi<xlo
//            Table tTable = data;
//            if (data.nColumns() > 0) //put in temp table if already data in 'data'
//                tTable = new Table();
            File part2 = new File(netcdfFilename+".part2");
            if (xlo.length() > 0) query.append("&longitude>=" + xlo);
            if (xhi.length() > 0) query.append("&longitude<=" + xhi);
            try {
                String q = URLEncoder.encode(query.toString(), "UTF-8").replaceAll("\\+", "%20");
                String secondUrl = dsUrl + q;
                dt = new DateTime();
                log.info("{TableDapTool starting file pull for part 2 at "+fmt.print(dt));
                lasProxy.executeGetMethodAndSaveResult(secondUrl, part2, null);
                dt = new DateTime();
                log.info("{TableDapTool finished file pull for part 2 at "+fmt.print(dt));
            } catch (Exception e) {
                String message = e.getMessage();
                if ( e.getMessage().contains("com.cohort") ) {
                    message = message.substring(message.indexOf("com.cohort.util.SimpleException: "), message.length());
                    message = message.substring(0, message.indexOf(")"));
                }
                causeOfError = "Data source error: " + message;
                throw new Exception(message);
            }
            
            
            //was the request canceled?
            if (isCanceled(cancel, lasBackendRequest, lasBackendResponse))
                return lasBackendResponse;
            
            // If there is only one file, rename it and get on your with life.
            if ( part1.exists() && part1.length() > 0 ) {
                dt = new DateTime();
                log.info("{TableDapTool starting file join for part 1 and 2 "+fmt.print(dt));
//                Table table1 = new Table();
//                Table table2 = new Table();
//                table1.readNcCF(part1.getAbsolutePath(), null, null, null, null);
//                table2.readNcCF(part2.getAbsolutePath(), null, null, null, null);
//                table1.append(table2);
//                // How to derive the name "obs" from the files already written?
//                table1.saveAs4DNc(netcdfFilename, table1.findColumnNumber(lonname), table1.findColumnNumber(latname), table1.findColumnNumber(zname), table1.findColumnNumber(time));
            } else {
                part2.renameTo(new File(netcdfFilename));
            }

            // The service just wrote the file to the requested location so
            // copy the response element from the request to the response.
            causeOfError = "Failed to set response element: ";
            lasBackendResponse.addResponseFromRequest(lasBackendRequest);

        } catch (Exception e) {
            //System.out.println("TabledapTool is processing the exception.");
            log.warn(MustBe.throwableToString(e));
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
            log.debug("Request cancelled:" + lasBackendRequest.toCompactString());
            return true;
        }
        return false;
    }

    /**
     * Get the value of a particular database property.
     * @param property
     * @return the value of the property
     * @throws LASException
     */
    public String getTabledapProperty(LASBackendRequest backendRequest, String property) throws LASException {
        Element data = backendRequest.getRootElement().getChild("dataObjects").getChild("data");
        Element td_access = backendRequest.findPropertyGroup(data, "tabledap_access");
        return backendRequest.findPropertyValue(td_access, property);
    }


}
