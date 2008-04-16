package gov.noaa.pmel.tmap.las.service.fregrid;

import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.service.BackendService;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FregridBackendService extends BackendService {
    // This object could be persisted between requests if this
    // were packaged in a servlet.
    private static Logger log = LogManager.getLogger(FregridBackendService.class.getName());
    public String getProduct(String backendRequestXML, String cacheFileName) throws Exception {       
        LASBackendRequest lasBackendRequest = new LASBackendRequest();      
        JDOMUtils.XML2JDOM(backendRequestXML, lasBackendRequest);
        
        String debug = lasBackendRequest.getProperty("las", "debug");
        
        setLogLevel(debug);
        
        // Report logging level only for "debug" levels.
        log.debug("Logging set to " + log.getEffectiveLevel().toString()+ " for "+log.getName());
        
        LASBackendResponse lasBackendResponse = new LASBackendResponse();    
        if ( lasBackendRequest.isCancelRequest()) {           
            lasBackendResponse.setError("Fregrid backend request canceled.");
            log.info("Fregrid backend request canceled: "+lasBackendRequest.toCompactString());
            return lasBackendResponse.toString();
        }
        FregridTool fregridTool = new FregridTool();
        lasBackendResponse = fregridTool.run(lasBackendRequest);
        if ( lasBackendResponse.getError() != null && !lasBackendResponse.getError().equals("") ) {
          log.info("Fregrid backend request failed: "+lasBackendResponse.getError());
        } 
        return lasBackendResponse.toString();
    }
}
