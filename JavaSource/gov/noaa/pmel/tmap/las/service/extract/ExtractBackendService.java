package gov.noaa.pmel.tmap.las.service.extract;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.jdom.JDOMException;

import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.service.BackendService;


public class ExtractBackendService extends BackendService {
    final Logger log = LogManager.getLogger(ExtractBackendService.class.getName());
    public String getProduct(String backendRequestXML, String outputFileName) throws Exception {
        try {
            LASBackendRequest lasBackendRequest = new LASBackendRequest();      
            JDOMUtils.XML2JDOM(backendRequestXML, lasBackendRequest);
            LASBackendResponse lasBackendResponse = new LASBackendResponse();

            String debug = lasBackendRequest.getProperty("las", "debug");

            setLogLevel(debug);

            // Report logging level only for "debug" and "trace" levels.
            log.debug("Logging set to " + log.getEffectiveLevel().toString()+ " for "+log.getName());

            if ( lasBackendRequest.isCancelRequest()) {
                lasBackendResponse.setError("error", "Database backend request canceled.");
                log.debug("Database backend request canceled: "+lasBackendRequest.toCompactString());
                return lasBackendResponse.toString();
            }
            String db_name = lasBackendRequest.getProperty("database_access", "db_name");
            if ( db_name != null && !db_name.equals("") ) {
                DatabaseTool databaseTool = new DatabaseTool();
                log.debug("Running database tool.");
                lasBackendResponse = databaseTool.run(lasBackendRequest);
                if ( lasBackendResponse.getError() != null && !lasBackendResponse.getError().equals("") ) {
                    log.error("Database backend request failed: "+lasBackendResponse.getError());
                } 
                return lasBackendResponse.toString();
            }
            String erddap_server = lasBackendRequest.getProperty("tabledap_access", "server");
            if ( erddap_server != null && !erddap_server.equals("") ) {
                TabledapTool tool = new TabledapTool();
                lasBackendResponse = tool.run(lasBackendRequest);
                return lasBackendResponse.toString();
            }
            lasBackendResponse.setError("Error", "Extraction service type not found or not supported.");
            return lasBackendResponse.toString();
        } catch (Exception e) {
            throw new LASException(e.getMessage());
        }
    }
}
