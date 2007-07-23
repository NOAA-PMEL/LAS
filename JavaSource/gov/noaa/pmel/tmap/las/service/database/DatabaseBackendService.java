package gov.noaa.pmel.tmap.las.service.database;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.jdom.JDOMException;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.service.BackendService;


public class DatabaseBackendService extends BackendService {
    final Logger log = LogManager.getLogger(DatabaseBackendService.class.getName());
    public String getProduct(String backendRequestXML, String outputFileName) throws IOException, JDOMException, LASException {
        LASBackendRequest lasBackendRequest = new LASBackendRequest();      
        JDOMUtils.XML2JDOM(backendRequestXML, lasBackendRequest);
        LASBackendResponse lasBackendResponse = new LASBackendResponse();
        
        String debug = lasBackendRequest.getProperty("las", "debug");
        
        setLogLevel(debug);
        
        // Report logging level only for "debug" and "trace" levels.
        log.debug("Logging set to " + log.getEffectiveLevel().toString()+ " for "+log.getName());
        
        if ( lasBackendRequest.isCancelRequest()) {
            lasBackendResponse.setError("Database backend request canceled.");
            log.info("Database backend request canceled: "+lasBackendRequest.toCompactString());
            return lasBackendResponse.toString();
        }
        DatabaseTool databaseTool = new DatabaseTool();
        log.info("Running database tool.");
        lasBackendResponse = databaseTool.run(lasBackendRequest);
        if ( lasBackendResponse.getError() != null && !lasBackendResponse.getError().equals("") ) {
            log.info("Database backend request failed: "+lasBackendResponse.getError());
          } 
        return lasBackendResponse.toString();
    }
}
