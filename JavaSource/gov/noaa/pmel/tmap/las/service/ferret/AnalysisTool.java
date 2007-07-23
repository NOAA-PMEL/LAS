/**
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */
package gov.noaa.pmel.tmap.las.service.ferret;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASFerretBackendConfig;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.jdom.JDOMException;

/**
 * This class takes the lasBackendRequest and creates a data object which exposes the virtual variables created by the analysis request
 * via the F-TDS OPeNDAP server
 * @author Roland Schweitzer
 *
 */
public class AnalysisTool extends FerretTool {
    final Logger log = LogManager.getLogger(AnalysisTool.class.getName());
    LASFerretBackendConfig lasFerretBackendConfig;
    
    /**
     * A constructor which can be by the service to get ahold of the config file for this service.
     * @throws LASException
     * @throws IOException
     */
    public AnalysisTool() throws LASException, IOException {
        
        super("ferret", "FerretBackendConfig.xml");
        
        
        lasFerretBackendConfig = new LASFerretBackendConfig();

        try {
            JDOMUtils.XML2JDOM(getConfigFile(), lasFerretBackendConfig);
        } catch (Exception e) {
            throw new LASException("Could not parse Ferret config file: " + e.toString());
        }
    }
    public LASBackendResponse run(LASBackendRequest lasBackendRequest) throws Exception, LASException, IOException, JDOMException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        LASBackendResponse lasBackendResponse = new LASBackendResponse();
        log.debug("Running the FerretTool.");
        
        String journalName = null;
        synchronized(this) {
            journalName = "ferret_operation"
                + "_" + System.currentTimeMillis();
        }
        String tempDir   = lasFerretBackendConfig.getTempDir();
        if ( tempDir == "" ) {
            tempDir = getResourcePath("resources/ferret/temp");
        }
        
        if ( lasBackendRequest.isCanceled() ) {
            lasBackendResponse.setError("Job canceled");
            return lasBackendResponse;
        }
        
        String path = lasBackendRequest.getResultAsFile("f-tds");
        File jnlFile = new File(path);
        
        createPlotJournal(lasBackendRequest, jnlFile, "launch.vm" );     
        
        return lasBackendResponse;
    }
}