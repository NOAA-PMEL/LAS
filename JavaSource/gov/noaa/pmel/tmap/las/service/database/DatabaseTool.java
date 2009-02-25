package gov.noaa.pmel.tmap.las.service.database;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.exception.LASRowLimitException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASDatabaseBackendConfig;
import gov.noaa.pmel.tmap.las.service.TemplateTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.rowset.WebRowSet;

import oracle.jdbc.rowset.OracleWebRowSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.jdom.JDOMException;

import ucar.nc2.NetcdfFile;

import com.sun.rowset.WebRowSetImpl;

public class DatabaseTool extends TemplateTool {
    
    LASDatabaseBackendConfig databaseBackendConfig;
    
    final Logger log = LogManager.getLogger(DatabaseTool.class.getName());
    public DatabaseTool() throws LASException, IOException {
       
        super("database", "DatabaseBackendConfig.xml");
        
        databaseBackendConfig = new LASDatabaseBackendConfig();

        try {
            JDOMUtils.XML2JDOM(getConfigFile(), databaseBackendConfig);
        } catch (Exception e) {
            throw new LASException("Could not parse Database config file: " + e.toString());
        }
    }
    
    public LASBackendResponse run(LASBackendRequest lasBackendRequest) {
        LASBackendResponse lasBackendResponse = new LASBackendResponse();
                
        if (lasBackendRequest.isCanceled()) {
            lasBackendResponse.setError("Request canceled", "Request canceled.");
            log.info("Request cancelled:"+lasBackendRequest.toCompactString());
            return lasBackendResponse;
        }
        String db_name;
        try {
            db_name = lasBackendRequest.getDatabaseProperty("db_name");
        } catch (Exception e) {
            lasBackendResponse.setError("Could not get database name from backend request name: ", e);
            return lasBackendResponse;
        }
        log.debug("got db name: "+db_name); //debug
        if ( db_name == null || db_name.equals("")) {
            lasBackendResponse.setError("Database backend configuration error.", "Request does not contain a valid db_name property.");
            return lasBackendResponse;
        }
        
        
        String driver;
        try {
            driver = databaseBackendConfig.getDriver(db_name);
        } catch (Exception e) {
            lasBackendResponse.setError("Could not get database driver: ", e);
            return lasBackendResponse;
        }
        log.debug("Got driver class: "+driver); //debug
        if ( driver.equals("")) {
            lasBackendResponse.setError("Database backend configuration error.", "Database configuration does not have a database driver attribute.");
            return lasBackendResponse;
        }
        
        // The newInstance() call is a work around for some
        // broken Java implementations
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
            lasBackendResponse.setError("Could load database driver class: ", e);
            return lasBackendResponse;
        }
        log.debug("Got instance of driver."); //debug
        File sqlFile;
        
        // If defined use the debug file name as the place to build the sql statements.
        String sqlFileName = lasBackendRequest.getResultAsFile("db_debug");
        if ( sqlFileName != "" ) {
            sqlFile = new File(sqlFileName);
        // If not, make one from scratch.
        } else {
            String template_dir = getResourcePath("resources/database/temp");
            
            if ( template_dir != null ) {
                sqlFile = new File(template_dir+"sql.sql");
            } else {
                lasBackendResponse.setError("Database backend service errror.", "Could not find SQL template directory.");
                return lasBackendResponse;
            }
        }
        log.debug("About to create sqlFile");
        if (lasBackendRequest.isCanceled()) {
            lasBackendResponse.setError("Request canceled.");
            log.info("Request cancelled:"+lasBackendRequest.toCompactString());
            return lasBackendResponse;
        }
        try {
        	String sql = lasBackendRequest.getProperty("operation","service_action") + ".vm";
            mergeCommandTemplate(lasBackendRequest, sqlFile, sql);
        } catch (Exception e) {
            lasBackendResponse.setError("Could not create SQL command file: ", e);
            return lasBackendResponse;
        }
        log.debug("Created sqlFile"); //debug
        if (lasBackendRequest.isCanceled()) {
            lasBackendResponse.setError("Request canceled", "Request canceled.");
            log.info("Request cancelled:"+lasBackendRequest.toCompactString());
            return lasBackendResponse;
        }
        
        log.debug("preparing to read sql file"); //debug
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        String statement = "";
        try {
        	statement = readMergedTemplate(sqlFile);
        } catch (IOException e ) {
        	lasBackendResponse.setError("Unable to read line from SQL file ", e);
        	return lasBackendResponse;
        }
        log.debug("Statement ready: "+statement);
        if (lasBackendRequest.isCanceled()) {
            lasBackendResponse.setError("Request canceled", "Request canceled.");
            log.info("Request cancelled:"+lasBackendRequest.toCompactString());
            return lasBackendResponse;
        }
        String user;
        try {
            user = databaseBackendConfig.getUser(db_name);
        } catch (Exception e) {
            lasBackendResponse.setError("Could not get database user from database config: ", e);
            return lasBackendResponse;
        }
        String password;
        try {
            password = databaseBackendConfig.getPassword(db_name);
        } catch (Exception e) {
            lasBackendResponse.setError("Could not get database password from database config: ", e);
            return lasBackendResponse;
        }
        String connectionURL="";
        try {
            connectionURL = databaseBackendConfig.getConnectionURL(db_name);
        } catch (Exception e) {
            lasBackendResponse.setError("Could not get database connection URL from database config: ", e);
            return lasBackendResponse;
        }
        if ( !connectionURL.startsWith("jdbc:") ) {
            connectionURL = "jdbc:"+connectionURL;
        }
        log.debug("Connecting to datbase: "+connectionURL);
        
        try {
            con =  DriverManager.getConnection(connectionURL, user, password);
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            String fetch_size = lasBackendRequest.getDatabaseProperty("fetch_size");
            int f = 0;
            if ( fetch_size != null && !fetch_size.equals("") ) {
            	try {
					f = Integer.valueOf(fetch_size);
				} catch (RuntimeException e) {
					f = 0;
				}
            }
 	
            stmt.setFetchSize(f);
            
            log.debug("executeQuery: "+statement);
            rset = stmt.executeQuery(statement);
            log.debug("Done with query.");
            
        } catch (Exception e) {
            lasBackendResponse.setError("Database access failed: "+statement+" ",e);
            return lasBackendResponse;
        }
        if (lasBackendRequest.isCanceled()) {
            lasBackendResponse.setError("Request canceled", "Request canceled.");
            log.info("Request cancelled:"+lasBackendRequest.toCompactString());
            return lasBackendResponse;
        }
        
        String netcdfFilename = "";
        netcdfFilename = lasBackendRequest.getResultAsFile("netcdf");
        log.debug("Got netcdf filename: "+ netcdfFilename); // debug
        if (lasBackendRequest.isCanceled()) {
            lasBackendResponse.setError("Request canceled","Request canceled.");
            log.info("Request cancelled:"+lasBackendRequest.toCompactString());
            return lasBackendResponse;
        }
        
        if ( netcdfFilename != null && !netcdfFilename.equals("")) {
            try {
               makeNetCDF(netcdfFilename, rset, lasBackendRequest);
            } catch (Exception e) {
            	if ( e instanceof LASRowLimitException ) {
            		lasBackendResponse.setError("Row limit exceeded.", e);
            	} else {
                    lasBackendResponse.setError("Unable to make intermediate netCDF file.", e);
            	}
                return lasBackendResponse;
            }
            
            if (lasBackendRequest.isCanceled()) {
                lasBackendResponse.setError("Request canceled.");
                log.info("Request cancelled:"+lasBackendRequest.toCompactString());
                return lasBackendResponse;
            }

        }
        
        String ncmlFilename = lasBackendRequest.getResultAsFile("ncml");
        log.debug("got ncml filename: "+ncmlFilename); // debug
        if (ncmlFilename != null && !ncmlFilename.equals("") ) {
            
            try {
                NetcdfFile ncfile = NetcdfFile.open(netcdfFilename);
                log.debug("opened ncml file"); // debug
                FileOutputStream os = new FileOutputStream(new File(ncmlFilename));
                log.debug("made stream"); // deubg
                ncfile.writeNcML(os, ncmlFilename);
                log.debug("wrote ncml file");
            } catch (IOException e) {
                try {
                    makeNetCDF(netcdfFilename, rset, lasBackendRequest);
                } catch (Exception eml) {
                    lasBackendResponse.setError("Unable to create netCDF file to dump to NcML: ", eml);
                    return lasBackendResponse;
                }
            }
        }
        
        String xmlFilename = lasBackendRequest.getResultAsFile("webrowset");
        log.debug("got xml filename: "+xmlFilename); // debug
        
        if ( xmlFilename != null && !xmlFilename.equals("")) {
            try {
                WebRowSet webrowset;
                if ( driver.contains("oracle") ) {
                   webrowset = new OracleWebRowSet();
                } else {
                   webrowset = new WebRowSetImpl();
                }
                File webrowsetFile = new File(xmlFilename);
                FileWriter fw = new FileWriter(webrowsetFile);
                rset.beforeFirst();
                webrowset.writeXml(rset, fw);               
            } catch (Exception e) {
                lasBackendResponse.setError("Unable to create  XML file: ", e);
                return lasBackendResponse;
            }
        }
        log.debug("finished with xml file");
        try { rset.close(); } catch(Exception e) { }
        try { stmt.close(); } catch(Exception e) { }
        try { con.close(); } catch(Exception e) { }
        
        
        if (lasBackendRequest.isCanceled()) {
            lasBackendResponse.setError("Request canceled.");
            log.info("Request cancelled:"+lasBackendRequest.toCompactString());
            return lasBackendResponse;
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
    private void makeNetCDF(String netcdfFilename, ResultSet rset, LASBackendRequest lasBackendRequest) throws Exception {
        IntermediateNetcdfFile netcdf = null;
        
        try {
            netcdf = new IntermediateNetcdfFile(netcdfFilename, false);               
        } catch (Exception e) {
            throw new Exception("Error creating empty netCDF file and checking for results: "+e.toString());
        }
        
        try {
            netcdf.create(rset, lasBackendRequest);
        } catch (Exception e) {
        	if ( e instanceof LASRowLimitException ) {
                throw e;
        	} else {
                throw new Exception("Could not create intermediate netCDF file stub with result set: "+e.toString());
        	}
        } finally {
            if ( netcdf != null ) {
                netcdf.close();
            }
        }
        
        log.debug("created netcdf file"); //debug
        
        // Fill up the variables with the data.
        
    }
}
