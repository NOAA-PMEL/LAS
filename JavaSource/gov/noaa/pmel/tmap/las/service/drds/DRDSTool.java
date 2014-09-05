/**
 * 
 */
package gov.noaa.pmel.tmap.las.service.drds;

import gov.noaa.pfel.coastwatch.pointdata.Table;
import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASDRDSBackendConfig;
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

import com.cohort.array.DoubleArray;
import com.cohort.array.PrimitiveArray;
import com.cohort.array.StringArray;

/**
 * Creates an intermediate netCDF file from a DODS Relational Database Server (DRDS).  The configuration
 * for the data source is mostly the same as the configuration for our database service.  For example,
 * this configuration will:
 * 
 *     
 * The associated template will create two constraint expressions if needed to collect data from the two "edges"
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
 *
 */
public class DRDSTool extends TemplateTool {

     LASDRDSBackendConfig drdsBackendConfig;
    
    final Logger log = Logger.getLogger(DRDSTool.class.getName());
    
    /**
     * The default constructor which uses the TemplateTool base class to initialize all
     * of its temporary and template directories.  It also reads the config file for this
     * service.
     * @throws IOException
     * @throws LASException
     */
    public DRDSTool() throws IOException, LASException {
        
        super("drds", "DRDSBackendConfig.xml");
        drdsBackendConfig = new LASDRDSBackendConfig();

        try {
            JDOMUtils.XML2JDOM(getConfigFile(), drdsBackendConfig);
        } catch (Exception e) {
            throw new LASException("Could not parse DRDS config file: " + e.toString());
        }
    }
    /**
     * Runs the DRDS tool to connect to the data source and create the netCDF file for this request.
     * @param lasBackendRequest
     * @return LASBackendResponse
     */
    public LASBackendResponse run(LASBackendRequest lasBackendRequest) {
        log.debug("Entered DRDS Backend Service run method."); // debug
        LASBackendResponse lasBackendResponse = new LASBackendResponse();
        
        String netcdfFilename = "";
        netcdfFilename = lasBackendRequest.getResultAsFile("netcdf");
        log.debug("Got netcdf filename: "+ netcdfFilename); // debug
        if ( lasBackendRequest.isCanceled() ) {
            lasBackendResponse.setError("Request canceled.");
            log.debug("Request cancelled:"+lasBackendRequest.toCompactString());
            return lasBackendResponse;
        }
        
        String db_server;
        try {
            db_server = lasBackendRequest.getDatabaseProperty("db_server");
        } catch (Exception e) {
            lasBackendResponse.setError("Could not get DRDS server name from backend request: ", e);
            return lasBackendResponse;
        }
        
        String db_name;
        try {
            db_name = lasBackendRequest.getDatabaseProperty("db_name");
        } catch (Exception e) {
            lasBackendResponse.setError("Could not get DRDS db_name from backend request: ", e);
            return lasBackendResponse;
        }
        
        String db_table;
        try {
            db_table = lasBackendRequest.getDatabaseProperty("db_table");
        } catch (Exception e) {
            lasBackendResponse.setError("Could not get DRDS db_table name from backend request: ", e);
            return lasBackendResponse;
        }
        
        String url="";
        try {
            url = drdsBackendConfig.getURL(db_server);
        } catch (Exception e) {
            lasBackendResponse.setError("Could not get connection URL from DRDS config: ", e);
            return lasBackendResponse;
        } 
        
        if ( url == null || url.equals("") ) {
            lasBackendResponse.setError("Could not get connection URL from DRDS config.");
            return lasBackendResponse;
        }
        
        if ( !url.endsWith("/") ) {
            url = url + "/";
        }
        
        // If defined use the debug file name as the place to build the constraint statement.
        String conFileName = lasBackendRequest.getResultAsFile("db_debug");
        File conFile;
        if ( conFileName != null && conFileName.length() > 0 ) {
            conFile = new File(conFileName);
        // If not, make one from scratch.
        } else {
            String temp_dir = getResourcePath("resources/database/temp");
            
            if ( temp_dir != null ) {
                conFile = new File(temp_dir+"constraint"+"_" + System.currentTimeMillis() + ".con");
            } else {
                lasBackendResponse.setError("Could not find SQL template directory.");
                return lasBackendResponse;
            }
        }
        
        try {
            createDRDSConstraint(lasBackendRequest, conFile);
        } catch (Exception e) {
            lasBackendResponse.setError("Could not create DSRD constraint expression: ", e);
            return lasBackendResponse;
        }
        
        BufferedReader conReader = null;
        try {
            FileReader f = new FileReader(conFile);
            conReader = new BufferedReader(f);
        } catch (FileNotFoundException e) {
            lasBackendResponse.setError("SQL template not found: " + e.toString());
            return lasBackendResponse;
        }
        
        
        ArrayList<String> constraint = new ArrayList<String>();
        
        try {
            String line = conReader.readLine();
            while (line != null) {
                if ( line.startsWith("?") ) {
                    line = line.substring(1,line.length());
                }
               constraint.add(line);
               line = conReader.readLine();
            }
        } catch (IOException e) {
            lasBackendResponse.setError("Could not read the constraint generated from the template: " + e.toString());
            return lasBackendResponse;
        }
        
        
        
        ArrayList<String> opendapURL = new ArrayList<String>();
        
        /*
         * I don't like this.  Seems like there should be a better way, but URLEncoder
         * encodes a blank to "+" instead of "%20".  Encoding the http:// etc. is too much since 
         * sane code doen't know how to deal with the encoded "://".
         * 
         * The URI.getASCIIString() has the right encoding, but
         * you can't construct a URI from a string with blanks in it.  Geez.
         */ 
        
        
        try {
            log.debug("Encoding: "+url+db_name+"?"+constraint);
            for (Iterator conIt = constraint.iterator(); conIt.hasNext();) {
                String con = (String) conIt.next();
                String urlToEncode = url+URLEncoder.encode(db_name, "UTF-8")+"?"+URLEncoder.encode(con,"UTF-8");
                urlToEncode = urlToEncode.replaceAll("\\+","%20");
                opendapURL.add(urlToEncode);
            }
        } catch (UnsupportedEncodingException e) {
            lasBackendResponse.setError("Could not encode the data source URL: " + e.toString());
            return lasBackendResponse;
        }
        
        
        
        
        Table data = new Table();
        if ( netcdfFilename != null && !netcdfFilename.equals("")) {
            try {
                int it = 0;
            	for (Iterator urlIt = opendapURL.iterator(); urlIt.hasNext();) {
            		String drds_url = (String) urlIt.next();
            		log.debug("Opening: "+drds_url);
                    if ( it == 0 || data.nRows() == 0 ) {
                        try {
                           data.readOpendapSequence(drds_url);
                        } catch (Exception e) {
                            if ( !e.getMessage().contains("No Matching Result") ) {
                                throw e;
                            }
                        }
                    } else {
                        Table table = new Table();
                        table.readOpendapSequence(drds_url);
                        data.append(table);
                    }    
                    it++;
            	}
                
                //Table.convert(opendapURL.get(0), Table.READ_OPENDAP_SEQUENCE, netcdfFilename, Table.SAVE_AS_FLAT_NC, "row", false);
            } catch (Exception e) {
                lasBackendResponse.setError("Could not convert the data source to a netCDF file", e);
                return lasBackendResponse;
            }

            if (lasBackendRequest.isCanceled()) {
                lasBackendResponse.setError("Request canceled.");
                log.debug("Request cancelled:"+lasBackendRequest.toCompactString());
                return lasBackendResponse;
            }
            
            try {
                enhance(lasBackendRequest, data);
            } catch (IOException e) {
                lasBackendResponse.setError("Could not enhance netCDF file metadata.", e);
                return lasBackendResponse;
            } catch (LASException e) {
                lasBackendResponse.setError("Could not enhance netCDF file metadata.", e);
                return lasBackendResponse;
            }
            
            // Make our own empty netCDF file if no results were returned
            if ( data.nRows() == 0 ) {
                NetcdfFileWriteable netcdfFile;
				try {
					netcdfFile = NetcdfFileWriteable.createNew(netcdfFilename, false);
				} catch (IOException e) {
					lasBackendResponse.setError("Could not convert the data source to a netCDF file", e);
	                return lasBackendResponse;
				}
                ArrayList<Dimension> dimList = new ArrayList<Dimension>();
                Dimension index = netcdfFile.addDimension("index", 1);
                dimList.add(index);
                String time_name;
                try {
                    time_name = lasBackendRequest.getDatabaseProperty("time");
                } catch (LASException e) {
                    lasBackendResponse.setError("Could not create a netCDF file because no time database_property was found.", e);
                    return lasBackendResponse;
                }
                netcdfFile.addVariable(time_name, DataType.DOUBLE, dimList);
                ArrayDouble.D1 fake_data = new ArrayDouble.D1(1);
                Double d = new Double("-9999.");
                fake_data.set(0, d);
                try {
                    netcdfFile.create();
                    netcdfFile.write(time_name, fake_data);
                } catch (IOException e) {
                    lasBackendResponse.setError("Could not create empty netCDF file.", e);
                    return lasBackendResponse;
                } catch (InvalidRangeException e) {
                    lasBackendResponse.setError("Could not create empty netCDF file.", e);
                    return lasBackendResponse;
                } finally {
                    if ( netcdfFile != null ) {
                        try {
                            netcdfFile.close();
                        } catch (IOException e) {
                            
                        }
                    }
                }
            } else {
                try {
                    data.saveAsFlatNc(netcdfFilename, "row");
                } catch (Exception e) {
                    lasBackendResponse.setError("Could not write netCDF file to disk.", e);
                    return lasBackendResponse;
                }
            }

        }
        // The service just wrote the file to the requested location so
        // copy the response element from the request to the response.
        // 
        try {
            lasBackendResponse.addResponseFromRequest(lasBackendRequest);
        } catch (JDOMException e) {
            lasBackendResponse.setError("Failed to set response element: ", e);
            return lasBackendResponse;
        }
        return lasBackendResponse;
    }
    
    /**
     * Enhances the Table with what metadata can be inferred from the configuration properties it shows
     * up in the netCDF file. 
     * @param lasBackendRequest
     * @param netcdfFilename
     * @throws IOException 
     * @throws LASException 
     */
    private void enhance(LASBackendRequest lasBackendRequest, Table data) throws IOException, LASException {
    	String miss_string = lasBackendRequest.getDatabaseProperty("missing");
        if ( miss_string == null || miss_string.equals("")) {
            throw new LASException ("Cannot find the missing value database_access property.");
        }
        String longitude = lasBackendRequest.getDatabaseProperty("longitude");
        if ( longitude != null && longitude.contains(".") ) {
            longitude = longitude.substring(longitude.indexOf(".")+1, longitude.length());
        }
        
        String latitude = lasBackendRequest.getDatabaseProperty("latitude");
        if (latitude != null && latitude.contains(".") ) {
            latitude = latitude.substring(latitude.indexOf(".")+1, latitude.length());
        }
        String time = lasBackendRequest.getDatabaseProperty("time");
        if ( time != null && time.contains(".") ) {
            time = time.substring(time.indexOf(".")+1, time.length());
        }
        String time_units = lasBackendRequest.getDatabaseProperty("time_units");
        if ( time_units == null || time_units.equals("") ) {
            throw new LASException("time_units database property not found.");
        }
        String time_type = lasBackendRequest.getDatabaseProperty("time_type");
        if ( time_type == null || time_type.equals("") ) {
            throw new LASException("time_type database property not found.");
        }
        String time_format = "";
        if ( time_type.equalsIgnoreCase("string") ) {
           // If time is stored as a string you need to know the format.
           time_format = lasBackendRequest.getDatabaseProperty("time_format");
           if ( time_format == null || time_format.equals("") ) {
               throw new LASException("time_format database property not found.");
           }
        }
        String depth = lasBackendRequest.getDatabaseProperty("depth");
        if ( depth != null && depth.contains(".") ) {
            depth = depth.substring(depth.indexOf(".")+1, depth.length());
        }
        double missing=Double.valueOf(miss_string).doubleValue();
        // The Ferret formatted time_origin.
        DateUnit dateUnit;
		try {
			dateUnit = new DateUnit(time_units);
		} catch (Exception e) {
			throw new LASException(e.toString());
		}
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss");
        DateTime dt = new DateTime(dateUnit.getDateOrigin().getTime());
        String time_origin = fmt.withZone(DateTimeZone.UTC).print(dt);
        data.globalAttributes().set("Conventions", "LAS Intermediate netCDF File, Unidata Observation Dataset v1.0");
        data.globalAttributes().set("cdm_datatype", "Point");
        
       
    	for(int i=0; i<data.nColumns(); i++) {
    		String col_name = data.getColumnName(i);
    		data.columnAttributes(i).set("missing", missing);
    		if ( col_name.equals(longitude)) {
    			data.columnAttributes(i).set("units", "degrees_east");
    			data.columnAttributes(i).set("long_name", "Longitude");
                data.columnAttributes(i).set("_CoordinateAxisType", "Lon");
                double stats[] = data.getColumn(i).calculateStats();
                data.globalAttributes().set("geospatial_lon_min", stats[PrimitiveArray.STATS_MIN]);
                data.globalAttributes().set("geospatial_lon_max", stats[PrimitiveArray.STATS_MAX]);
    		} else if ( col_name.equals(latitude)) {
    			data.columnAttributes(i).set("units", "degrees_north");
    			data.columnAttributes(i).set("long_name", "Latitude");
                data.columnAttributes(i).set("_CoordinateAxisType", "Lat");
                double stats[] = data.getColumn(i).calculateStats();
                data.globalAttributes().set("geospatial_lat_min", stats[PrimitiveArray.STATS_MIN]);
                data.globalAttributes().set("geospatial_lat_max", stats[PrimitiveArray.STATS_MAX]);
    		} else if ( col_name.equals(depth) ) {
    			data.columnAttributes(i).set("units", lasBackendRequest.getDatabaseProperty("depth_units"));
    			data.columnAttributes(i).set("long_name", col_name);
                String positive = lasBackendRequest.getDatabaseProperty("positive");
                if (positive != null && positive.equalsIgnoreCase("down") ) {
                    data.columnAttributes(i).set("_CoordinateAxisType", "Depth");
                    data.columnAttributes(i).set("positive", positive);
                }
    		} else if ( col_name.equals(time) ) {
                // First if nessecary encode times into a column of UDUNITS time (instead of ISO ASCII Strings)
                if (time_type.equalsIgnoreCase("string") ) {
                    DateTimeFormatter convert_fmt = DateTimeFormat.forPattern(time_format).withZone(DateTimeZone.UTC);
                    DateUnit convert_unit;
					try {
						convert_unit = new DateUnit(time_units);
					} catch (Exception e) {
						throw new LASException(e.toString());
					}
                    StringArray sa = (StringArray)data.getColumn(i);
                    DoubleArray da = new DoubleArray(sa.size(), false);
                    for (int row = 0; row < sa.size(); row++) {
                        String time_string = sa.get(row);
                        Double timed;
                        if (time_string == null || time_string.equals("") ) {
                            timed = new Double(missing);
                        } else {
                            DateTime datetime = convert_fmt.parseDateTime(time_string).withZone(DateTimeZone.UTC);
                            double t = convert_unit.makeValue(datetime.toDate());
                            timed = new Double(t);
                        }
                        da.add(timed);
                    }
                    data.setColumn(i, da); 
                } 
                
                // Add the enhanced metadata.
    			data.columnAttributes(i).set("units", time_units);
    			data.columnAttributes(i).set("time_origin", time_origin);
    			data.columnAttributes(i).set("long_name", "Time");
                data.columnAttributes(i).set("_CoordinateAxisType", "Time");
                double stats[] = data.getColumn(i).calculateStats();
                data.globalAttributes().set("time_coverage_start", stats[PrimitiveArray.STATS_MIN]+" "+time_units);
                data.globalAttributes().set("time_coverage_end", stats[PrimitiveArray.STATS_MAX]+" "+time_units);
    		}
    	}
        

        
    }
    /**
     * A small helper method that processes the constraint template (with the VelocityTools in the same context).
     * @param lasBackendRequest
     * @param conFile
     * @throws Exception
     */
    protected void createDRDSConstraint(LASBackendRequest lasBackendRequest, File conFile) throws Exception  {
        
        PrintWriter sqlPrintWriter = null;
        sqlPrintWriter = new PrintWriter(new FileOutputStream(conFile));
                
        // Set up the Velocity Context
        VelocityContext context = new VelocityContext(getToolboxContext());
        
        context.put("las_backendrequest", lasBackendRequest);
        
        // Guaranteed to be set by the Product Server
        String sql = lasBackendRequest.getProperty("operation","service_action") + ".vm";
        
        log.debug("Merging sql to: " + conFile + " using " + sql); //debug
        ve.mergeTemplate(sql, "ISO-8859-1", context,
                sqlPrintWriter);
        log.debug("Script template merged.");
        
        sqlPrintWriter.flush();
        sqlPrintWriter.close();
    }
}
