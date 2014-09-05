package gov.noaa.pmel.tmap.las.service.climate.analysis;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;

import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.service.BackendService;
import gov.noaa.pmel.tmap.las.service.database.DatabaseBackendService;
import gov.noaa.pmel.tmap.las.service.ferret.FerretTool;

public class ClimateAnalysisBackendService extends BackendService {
    final Logger log = Logger.getLogger(ClimateAnalysisBackendService.class.getName());
    public String getProduct(String backendRequestXML, String outputFileName) throws Exception {
    	 LASBackendRequest lasBackendRequest = new LASBackendRequest();      
         JDOMUtils.XML2JDOM(backendRequestXML, lasBackendRequest);
         LASBackendResponse lasBackendResponse = new LASBackendResponse();
   
         if ( lasBackendRequest.isCancelRequest()) {
             lasBackendResponse.setError("Climate Analysis backend request canceled.");
             log.debug("Climate Analysis backend request canceled: "+lasBackendRequest.toCompactString());
             return lasBackendResponse.toString();
         }
         
         ClimateAnalysisTool climateAnalysisTool = new ClimateAnalysisTool();
         lasBackendResponse = climateAnalysisTool.run(lasBackendRequest);
         if ( lasBackendResponse.getError() != null && !lasBackendResponse.getError().equals("") ) {
           log.error("Climate Analysis backend request failed: "+lasBackendResponse.getError());
         }
         log.debug("Ending request: "+lasBackendRequest.toCompactString());
         log.info("END:   "+lasBackendRequest.getServiceAction());
         return lasBackendResponse.toString();
    }
}
