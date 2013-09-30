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
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import ucar.ma2.DataType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.DataIterator;
import ucar.nc2.dt.TrajectoryObsDataset;
import ucar.nc2.dt.TrajectoryObsDatatype;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeatureCollectionIterator;
import ucar.nc2.ft.TrajectoryFeature;
import ucar.nc2.ft.TrajectoryFeatureCollection;
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
            if ( cruiseid != null ) {
                if ( cruiseid.equals("") ) {
                    cruiseid = getTabledapProperty(lasBackendRequest, "profile_id");
                }
            } else {
                cruiseid = getTabledapProperty(lasBackendRequest, "profile_id");
            }
            required(cruiseid, causeOfError);

            causeOfError = "Could not get tiem column name from backend request: "; 
            String time = getTabledapProperty(lasBackendRequest, "time");
            required(time, causeOfError);

            String latname = getTabledapProperty(lasBackendRequest, "latitude");
            String lonname = getTabledapProperty(lasBackendRequest, "longitude");
            String zname = getTabledapProperty(lasBackendRequest, "altitude");
            String orderby = getTabledapProperty(lasBackendRequest, "orderby");
            String dummy = getTabledapProperty(lasBackendRequest, "dummy");


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
            ArrayList<String> vars = lasBackendRequest.getVariables();
            // If lat, lon and z are included as data variables, knock them out of this list.
            vars.remove(latname);
            vars.remove(lonname);
            vars.remove(zname);
            vars.remove(time);
            String variables = "";
            for (Iterator varIt = vars.iterator(); varIt.hasNext();) {
                String variable = (String) varIt.next();
                variables = variables+variable;
                if (varIt.hasNext()) {
                    variables = variables + ",";
                }
            }
            // Apparently ERDDAP gets mad if you list the trajectory_id in the request...
            variables = variables.replace(cruiseid+",", "");
            variables = variables.replace(cruiseid, "");
            if ( variables.endsWith(",") ) {
                variables = variables.substring(0, variables.length()-1);
            }

            if ( !variables.equals("") ) {
                query.append(String2.replaceAll(variables, " ", ""));
            } else {
                query.append(dummy);
            }



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
            
            if ( orderby != null ) {
                if ( !orderby.equals("") ) {
                    query.append("&orderBy(\""+orderby+"\")");
                } else {
                    query.append("&orderBy(\""+cruiseid+","+time+"\")");
                }
            } else {
                query.append("&orderBy(\""+cruiseid+","+time+"\")");
            }
            
            

            //store constraint in debug file
            causeOfError = "Could not create constraint expression in " + constraintFileName + ": ";
            String querySummary = "query=" + query.toString() + " xlo=" + xlo + " xhi=" + xhi;
            log.debug(querySummary);
            
            if (constraintFileName != null && constraintFileName.length() > 0)
                Test.ensureEqual(String2.writeToFile(constraintFileName, querySummary), "", causeOfError);

            //get the data   
            causeOfError = "Could not convert the data source to a netCDF file: ";   
            String dsUrl = url + id + ".ncCF?";  //don't include ".dods"; readOpendapSequence does that

            //            Table data = new Table();
            DateTime dt = new DateTime();
            DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
            if (xlo.length() > 0 && xhi.length() > 0 ) {

                // This little exercise will normalize the x values to -180, 180.
                double xhiDbl = String2.parseDouble(xhi);
                double xloDbl = String2.parseDouble(xlo);
                // Check the span before normalizing and if it's big, just forget about the lon constraint all together.
                if ( Math.abs(xhiDbl - xloDbl ) < 358. ) {
                    LatLonPoint p = new LatLonPointImpl(0, xhiDbl);
                    xhiDbl = p.getLongitude();
                    p = new LatLonPointImpl(0, xloDbl);
                    xloDbl = p.getLongitude();

                    // Now a wrap around from west to east should be have xhi < xlo;
                    if ( xhiDbl < xloDbl ) {
                        if ( xhiDbl < 0 && xloDbl >=0 ) {
                            // This should be true, otherwise how would to get into this situation unless you wrapped around the entire world and overlapped...
                            xhiDbl = xhiDbl + 360.0d;
                            query.append("&lon360>=" + xloDbl);
                            query.append("&lon360<=" + xhiDbl);
                        } // the else block is that you overlapped so leave off the longitude constraint all teogether

                    } else {
                        // This else block is the case where it a query that does not cross the date line
                        query.append("&longitude>=" + xlo);
                        query.append("&longitude<=" + xhi);
                    }
                }// Span the whole globe so leave off the lon query all together.
            } else {
                //  If they are not both defined, add the one that is...  There will be no difficulties with dateline crossings...
                if (xlo.length() > 0) query.append("&longitude>=" + xlo);
                if (xhi.length() > 0) query.append("&longitude<=" + xhi);
            }
            //            //do the main data query
            //            //xlo and/or xhi may be specified, but they aren't xhi<xlo
            //            Table tTable = data;
            //            if (data.nColumns() > 0) //put in temp table if already data in 'data'
            //                tTable = new Table();
            File temp_file = new File(netcdfFilename+".temp");
            
            try {
                String q = URLEncoder.encode(query.toString(), "UTF-8").replaceAll("\\+", "%20");
                String secondUrl = dsUrl + q;
                dt = new DateTime();
                log.info("{TableDapTool starting file pull for part 2 at "+fmt.print(dt));
                lasProxy.executeGetMethodAndSaveResult(secondUrl, temp_file, null);
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

            temp_file.renameTo(new File(netcdfFilename));


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

    private void merge(File part1, File part2) throws IOException {
        FeatureDataset dataset1;
        FeatureDataset dataset2;
        Formatter errlog = new Formatter();
        dataset1 = FeatureDatasetFactoryManager.open(null, part1.getAbsolutePath(), null, errlog);
        if (dataset1 == null) {
          log.error("Could not open: "+ part1.getAbsolutePath()+" because "+errlog);
          return;
        }
        dataset2 = FeatureDatasetFactoryManager.open(null, part2.getAbsolutePath(), null, errlog);
        if (dataset2 == null) {
          log.error("Could not open: "+ part2.getAbsolutePath()+" because "+errlog);
          return;
        }
        FeatureDatasetPoint traj1 = null;
        FeatureType type1 = dataset1.getFeatureType();
        if ( type1 == FeatureType.TRAJECTORY ) {
            traj1 = (FeatureDatasetPoint) dataset1;
        }
        FeatureDatasetPoint traj2 = null ;
        FeatureType type2 = dataset2.getFeatureType();
        if ( type2 == FeatureType.TRAJECTORY ) {
            traj2 = (FeatureDatasetPoint) dataset2;
        }
        
        List<FeatureCollection> fc1 = traj1.getPointFeatureCollectionList();
        for (Iterator iterator = fc1.iterator(); iterator.hasNext();) {
            FeatureCollection fc = (FeatureCollection) iterator.next();
            if ( fc.getCollectionFeatureType() == FeatureType.TRAJECTORY ) {
                TrajectoryFeatureCollection tfc1 = (TrajectoryFeatureCollection) fc;
                for (PointFeatureCollectionIterator tfcIt = tfc1.getPointFeatureCollectionIterator(8196); tfcIt.hasNext();) {
                    TrajectoryFeature tf = (TrajectoryFeature) tfcIt.next();
                    String name = tf.getName();
                    System.out.println("\t"+name);
                }
            }
        }
        
        
        if ( dataset1 != null ) {
            dataset1.close();
        }
        if ( dataset2 != null ) {
            dataset2.close();
        }
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
