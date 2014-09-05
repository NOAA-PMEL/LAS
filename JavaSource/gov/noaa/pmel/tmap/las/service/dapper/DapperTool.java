/**
 * DapperTool
 */
package gov.noaa.pmel.tmap.las.service.dapper;

import com.cohort.array.DoubleArray;
import com.cohort.array.StringArray;
import com.cohort.util.Math2;
import com.cohort.util.MustBe;
import com.cohort.util.String2;

import gov.noaa.pfel.coastwatch.pointdata.Table;

import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASDapperBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
//not finished, not needed? import gov.noaa.pmel.tmap.las.jdom.LASDapperBackendConfig;   //file Bob wrote, not in standard distribution yet
import gov.noaa.pmel.tmap.las.service.TemplateTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;


import org.apache.log4j.Logger;
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
 * Creates an intermediate netCDF file from a Dapper server 
 * (http://www.epic.noaa.gov/epic/software/dapper/).  
 * The configuration
 * for the data source is mostly the same as the configuration for our database 
 * service.  
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
public class DapperTool extends TemplateTool {

    //LASDapperBackendConfig dapperBackendConfig;
    
    final Logger log = Logger.getLogger(DapperTool.class.getName());
    
    /**
     * This default constructor uses the TemplateTool base class to initialize all
     * of its temporary and template directories.  
     * It also reads the config file for this service.
     *
     * @throws IOException
     * @throws LASException
     */
    public DapperTool() throws IOException, LASException {
        
        super("dapper", "DapperBackendConfig.xml");
        //dapperBackendConfig = new LASDapperBackendConfig();
        //try {
        //    JDOMUtils.XML2JDOM(getConfigFile(), dapperBackendConfig);
        //} catch (Exception e) {
        //    throw new LASException("Could not parse Dapper config file: " + e.toString());
        //}
    }

    /**
     * This connects to the data source and creates the netCDF file for this request.
     *
     * @param dapperBackendRequest
     * @return lasBackendResponse. 
     *   If an error occurs, this calls lasBackendResponse.setError,
     *   then returns the LASBackendResponse object.
     *   This won't throw an Exception.
     */
    public LASBackendResponse run(LASDapperBackendRequest dapperBackendRequest) {

        log.debug("Entered DapperTool.run method."); // debug

        //If exception occurs in try/catch block, 'causeOfError' was the cause.
        String causeOfError = "Unexpected error: "; 
        LASBackendResponse lasBackendResponse = new LASBackendResponse();
        try {      

            //First: has the request already been canceled (method 1)?
            if (dapperBackendRequest.isCancelRequest()) {           
                lasBackendResponse.setError("Dapper backend request canceled.");
                causeOfError = "Dapper backendRequest failed to cancel request: ";
                File cancel = new File(dapperBackendRequest.getResultAsFile("cancel"));
                cancel.createNewFile();
                log.debug("Dapper backend request canceled: " + dapperBackendRequest.toCompactString());
                return lasBackendResponse;
            }

            //has the request been canceled (method 2)?    
            String cancelFileName = dapperBackendRequest.getResultAsFile("cancel");
            File cancel = null;
            if (cancelFileName != null && !cancelFileName.equals("")) {
                causeOfError = "Unable to create cancelFileName=" + cancelFileName + ": ";
                cancel = new File(cancelFileName);
            }            
            //then standard check can be done any time
            if (cancel != null && cancel.exists()) {
                lasBackendResponse.setError("Request canceled.");
                log.debug("Request canceled:" + dapperBackendRequest.toCompactString());
                return lasBackendResponse;
            }

            causeOfError = "Unable to get backend request's resultsAsFile(netcdf): ";
            String netcdfFilename = dapperBackendRequest.getResultAsFile("netcdf");
            log.debug("Got netcdf filename: " + netcdfFilename); // debug
            if (netcdfFilename == null || netcdfFilename.length() == 0)
                throw new LASException(causeOfError);

            causeOfError = "Could not get url from backend request: ";
            String url = dapperBackendRequest.getRootElement().getChild("dataObjects").getChild("data").getAttributeValue("url");
            if (url == null || url.length() == 0) 
                throw new LASException(causeOfError);
            
            //if defined, use the "debug" resultsAsFile as the place to build the constraint statement.
            causeOfError = "Unable to getResultAsFile(debug): ";
            String constraintFileName = dapperBackendRequest.getResultAsFile("debug");
            if (constraintFileName == null || constraintFileName.length() == 0) {
                //make a constraintFile in resources/database/temp.
                causeOfError = "Could not find resources/database/temp directory: ";
                String temp_dir = getRequiredResourcePath("resources/database/temp");                
                constraintFileName = temp_dir + "constraint_" + System.currentTimeMillis() + ".con";
            }

            //this sample url doesn't work in a browser, only via opendap library:
//url=http://las.pfeg.noaa.gov/dods/ndbc/all_noaa_time_series.cdp?location.LON,location.LAT,
//location.DEPTH,location.profile.TIME,location.profile.WSPD1,location.profile.BAR1
//&location.LON>=235.45001&location.LON<=235.47&location.LAT>=40.77&location.LAT<=40.789997
//&location.profile.TIME>=1072915199999&location.profile.TIME<=1072918800001
            causeOfError = "Could not create constraint expression in " + constraintFileName + ": ";
            File constraintFile = new File(constraintFileName);
            createDapperConstraint(dapperBackendRequest, constraintFile);
            
            causeOfError = "Unable to open constraintFile " + constraintFileName + ": ";
            BufferedReader constraintReader = new BufferedReader(new FileReader(constraintFile));
            
            //There may be 1 or 2 lines (if getting lon tails) in the constraint file.
            //Look at diagram in class javadoc above to understand this.
            causeOfError = "Could not read the constraint generated from the template: ";
            String constraintString = constraintReader.readLine();
            Table data = new Table();
            if (constraintString == null) throw new LASException("Constraint file is empty.");
            while (constraintString != null) {
                constraintString = String2.replaceAll(constraintString, " ", ""); //multi var request has internal spaces!
                if (constraintString.length() == 0) {
                    log.warn("!!constraint is \"\"!!");  //should this be an error?
                } else {
                    causeOfError = "Could not convert the data source to a netCDF file: ";   
                    String fullUrl = url + "?" + constraintString;
                    log.debug("Opening: " + fullUrl);
                    if (data.nRows() == 0) {
                        //put results in main results table
                        data.readOpendapSequence(fullUrl);  //may throw exception
                    } else {
                        //put results in tempTable, then append to main results table
                        Table tempTable = new Table();
                        tempTable.readOpendapSequence(fullUrl); //may throw exception
                        data.append(tempTable);
                    }
                }

                //read the next constraintString
                causeOfError = "Could not read the constraint generated from the template: ";
                constraintString = constraintReader.readLine();
            }
            
            //was the request canceled?
            if (cancel != null && cancel.exists()) {
                lasBackendResponse.setError("Request canceled.");
                log.debug("Request cancelled:" + dapperBackendRequest.toCompactString());
                return lasBackendResponse;
            }
            
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
                    String vars[] = String2.split(dapperBackendRequest.getVariablesAsString(), ',');
                    for (int vari = 0; vari < vars.length; vari++)
                        data.addColumn(vars[vari], new DoubleArray());
                }

                //add a row of missing values
                for (int col = 0; col < data.nColumns(); col++) 
                    data.getColumn(col).addString(""); //represents missing value
            } else {
                //log.debug("first up-to-100 rows found: " + data.toString("rows", 100));
            }
            causeOfError = "Could not enhance netCDF file metadata: ";
            enhance(dapperBackendRequest, data);            

            causeOfError = "Could not write netCDF file to disk: ";
            data.saveAsFlatNc(netcdfFilename, "row");

            // The service just wrote the file to the requested location so
            // copy the response element from the request to the response.
            causeOfError = "Failed to set response element: ";
            lasBackendResponse.addResponseFromRequest(dapperBackendRequest);

        } catch (Exception e) {
            log.error(MustBe.throwableToString(e)); //e.toString + "\n" + String2.toNewlineString(e.getStackTrace()));
            lasBackendResponse.setError(causeOfError, e);
        }

        return lasBackendResponse;
    }

    /**
     * This returns the part of s after the last '.'.
     * [This method could be in LASBackendRequest.]
     *
     * @param s
     * @return the part of s after the last '.'
     *    (or null if the s is null, or "" if s is "", s if there is no dot)
     */
    public static String afterDot(String s) {

        if (s == null)
            return null;

        int po = s.lastIndexOf('.');
        if (po >= 0) 
            return s.substring(po + 1);
        return s;
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


    /**
     * Enhances the Table with what metadata can be inferred from the configuration 
     * properties that show up in the netCDF file. 
     *
     * @param dapperBackendRequest
     * @param data the data table which will be enhanced with better metadata.
     * @throws Exception if serious trouble
     */
    private void enhance(LASDapperBackendRequest dapperBackendRequest, Table data) throws Exception {


        String longitude = afterDot(dapperBackendRequest.getRequiredDatabaseProperty("longitude"));      
        String latitude  = afterDot(dapperBackendRequest.getRequiredDatabaseProperty("latitude"));
        String time      = afterDot(dapperBackendRequest.getRequiredDatabaseProperty("time"));
        String depth     = afterDot(dapperBackendRequest.getRequiredDatabaseProperty("depth"));

    	//String miss_string = dapperBackendRequest.getRequiredDatabaseProperty("missing");
        String timeUnits   = dapperBackendRequest.getRequiredDatabaseProperty("time_units");
 
        // The Ferret formatted time_origin.
        DateUnit dateUnit = new DateUnit(timeUnits);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss");
        DateTime dt = new DateTime(dateUnit.getDateOrigin().getTime());
        String time_origin = fmt.withZone(DateTimeZone.UTC).print(dt);
        data.globalAttributes().set("Conventions", 
            "LAS Intermediate netCDF File, Unidata Observation Dataset v1.0");
        data.globalAttributes().set("cdm_datatype", "Point");       
       
    	for(int i = 0; i < data.nColumns(); i++) {
            //change column name to afterDot version
    		String columnName = afterDot(data.getColumnName(i));
            data.setColumnName(i, columnName);

            //add metadata to some columns  
    		if (columnName.equals(longitude)) {
    			data.columnAttributes(i).set("units", "degrees_east");
    			data.columnAttributes(i).set("long_name", "Longitude");
                data.columnAttributes(i).set("_CoordinateAxisType", "Lon");
                double stats[] = data.getColumn(i).calculateStats();
                data.globalAttributes().set("geospatial_lon_min", stats[PrimitiveArray.STATS_MIN]);
                data.globalAttributes().set("geospatial_lon_max", stats[PrimitiveArray.STATS_MAX]);
    		} else if (columnName.equals(latitude)) {
    			data.columnAttributes(i).set("units", "degrees_north");
    			data.columnAttributes(i).set("long_name", "Latitude");
                data.columnAttributes(i).set("_CoordinateAxisType", "Lat");
                double stats[] = data.getColumn(i).calculateStats();
                data.globalAttributes().set("geospatial_lat_min", stats[PrimitiveArray.STATS_MIN]);
                data.globalAttributes().set("geospatial_lat_max", stats[PrimitiveArray.STATS_MAX]);
    		} else if (columnName.equals(depth) ) {
    			data.columnAttributes(i).set("units", 
                    dapperBackendRequest.getRequiredDatabaseProperty("depth_units"));
    			data.columnAttributes(i).set("long_name", columnName);
                String positive = dapperBackendRequest.getDatabaseProperty("positive");
                if (positive != null && positive.equalsIgnoreCase("down") ) {
                    data.columnAttributes(i).set("_CoordinateAxisType", "Depth");
                    data.columnAttributes(i).set("positive", positive);
                }
    		} else if (columnName.equals(time) ) {
    			data.columnAttributes(i).set("units", timeUnits);
    			data.columnAttributes(i).set("time_origin", time_origin);
    			data.columnAttributes(i).set("long_name", "Time");
                data.columnAttributes(i).set("_CoordinateAxisType", "Time");
                double stats[] = data.getColumn(i).calculateStats();
                data.globalAttributes().set("time_coverage_start", stats[PrimitiveArray.STATS_MIN] + " " + timeUnits);
                data.globalAttributes().set("time_coverage_end", stats[PrimitiveArray.STATS_MAX] + " " + timeUnits);
    		}
    	}     
    }

    /**
     * A small helper method that processes the constraint template 
     * (with the VelocityTools in the same context).
     * 
     * @param dapperBackendRequest
     * @param constraintFile the file which will receive the results
     * @throws Exception if trouble
     */
    protected void createDapperConstraint(LASDapperBackendRequest dapperBackendRequest,
            File constraintFile) throws Exception  {
        
        PrintWriter constraintPrintWriter = new PrintWriter(new FileOutputStream(constraintFile));
                
        //set up the Velocity Context
        VelocityContext context = new VelocityContext(getToolboxContext());       
        context.put("dapperBackendRequest", dapperBackendRequest);

        //convert the serviceAction into a constraint
        //serviceAction guaranteed to be set by the Product Server
        String serviceAction = dapperBackendRequest.getProperty("operation", "service_action") + ".vm";
        log.debug("Merging constraint to: " + constraintFile + " using " + serviceAction); //debug
        ve.mergeTemplate(serviceAction, "ISO-8859-1", context, constraintPrintWriter);
        log.debug("Script template merged.");
        
        constraintPrintWriter.flush();
        constraintPrintWriter.close();
    }

}
