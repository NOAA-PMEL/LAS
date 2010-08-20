package gov.noaa.pmel.tmap.las.service.ferret;

import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.service.BackendService;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FerretBackendService extends BackendService {
    // This object could be persisted between requests if this
    // were packaged in a servlet.
    private static Logger log = LogManager.getLogger(FerretBackendService.class.getName());
    public String getProduct(String backendRequestXML, String cacheFileName) throws Exception {  
       
        LASBackendRequest lasBackendRequest = new LASBackendRequest();      
        JDOMUtils.XML2JDOM(backendRequestXML, lasBackendRequest);
        log.info("START: "+lasBackendRequest.toCompactString());
        String debug = lasBackendRequest.getProperty("las", "debug");
        
        setLogLevel(debug);
        
        // Report logging level only for "debug" levels.
        log.debug("Logging set to " + log.getEffectiveLevel().toString()+ " for "+log.getName());
        
        LASBackendResponse lasBackendResponse = new LASBackendResponse();    
        if ( lasBackendRequest.isCancelRequest()) {           
            lasBackendResponse.setError("Ferret backend request canceled.");
            log.debug("Ferret backend request canceled: "+lasBackendRequest.toCompactString());
            return lasBackendResponse.toString();
        }
        FerretTool ferretTool = new FerretTool();
        lasBackendResponse = ferretTool.run(lasBackendRequest);
        if ( lasBackendResponse.getError() != null && !lasBackendResponse.getError().equals("") ) {
          log.error("Ferret backend request failed: "+lasBackendResponse.getError());
        }
        log.info("END:   "+lasBackendRequest.toCompactString());
        return lasBackendResponse.toString();
    }
}
