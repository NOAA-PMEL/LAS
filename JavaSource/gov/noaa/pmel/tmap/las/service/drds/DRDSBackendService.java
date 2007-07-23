/**
 * 
 */
package gov.noaa.pmel.tmap.las.service.drds;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.service.BackendService;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.jdom.JDOMException;

/**
 * @author Roland Schweitzer
 *
 */
public class DRDSBackendService extends BackendService {
    private static Logger log = LogManager.getLogger(DRDSBackendService.class.getName());
    public String getProduct(String backendRequestXML, String cacheFileName) throws Exception, LASException, IOException, JDOMException {       
        LASBackendRequest lasBackendRequest = new LASBackendRequest();      
        JDOMUtils.XML2JDOM(backendRequestXML, lasBackendRequest);
        
        String debug = lasBackendRequest.getProperty("las", "debug");
        
        setLogLevel(debug);
        
        // Report logging level only for "debug" and "trace" levels.
        log.debug("Logging set to " + log.getEffectiveLevel().toString()+ " for "+log.getName());
        
        LASBackendResponse lasBackendResponse = new LASBackendResponse();    
        if ( lasBackendRequest.isCancelRequest()) {           
            lasBackendResponse.setError("DRDS backend request canceled.");       
            log.info("DRDS backend request canceled: "+lasBackendRequest.toCompactString());
            return lasBackendResponse.toString();
        }
        DRDSTool tool = new DRDSTool();
        lasBackendResponse = tool.run(lasBackendRequest);
        if ( lasBackendResponse.getError() != null && !lasBackendResponse.getError().equals("") ) {
          log.info("DRDS backend request failed: "+lasBackendResponse.getError());
        } 
        return lasBackendResponse.toString();
    }
}
