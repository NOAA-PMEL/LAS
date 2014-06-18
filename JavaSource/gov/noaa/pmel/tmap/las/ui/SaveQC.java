package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.jdom.ServerConfig;
import gov.noaa.pmel.tmap.las.product.server.Cache;
import gov.noaa.pmel.tmap.las.product.server.LASAction;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.product.server.ServerConfigPlugIn;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class SaveQC extends LASAction {

	private static Logger log = LogManager.getLogger(SaveQC.class.getName());


	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
	    
	    // Get the request from the query parameter.
        String requestXML = request.getParameter("xml");
	    
        LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
        // Same for the ServerConfig
        ServerConfig serverConfig = (ServerConfig)servlet.getServletContext().getAttribute(ServerConfigPlugIn.SERVER_CONFIG_KEY);
        // Get the global cache object.
        Cache cache = (Cache) servlet.getServletContext().getAttribute(ServerConfigPlugIn.CACHE_KEY);

        // Get the lasRequest object from the request.  It was placed there by the RequestInputFilter.
        LASUIRequest lasRequest = (LASUIRequest) request.getAttribute("las_request");
        

        // If it wasn't built in the filter try to build it here
        if (lasRequest == null && (requestXML != null && !requestXML.equals("")) ) {
            try {
                String temp = URLDecoder.decode(requestXML, "UTF-8");
                requestXML = temp;
            } catch (UnsupportedEncodingException e) {
                LASAction.logerror(request, "Error decoding the XML request query string.", e);
                return mapping.findForward("error");
            }

            // Create a lasRequest object.
            lasRequest = new LASUIRequest();
            try {
                JDOMUtils.XML2JDOM(requestXML, lasRequest);
                // Set the lasRequest object in the HttpServletRequest so the product server does not have to rebuild it.
                request.setAttribute("las_request", lasRequest);
            } catch (Exception e) {
                LASAction.logerror(request, "Error parsing the request XML. ", e);
                return mapping.findForward("error");
            }
        }
        
  
        // You'l have to use the same tricks to get your DB parameters in here.
        
        
        // Pull out the QC parameters.
        
        String cruise_ID = lasRequest.getProperty("qc", "cruise_ID");
        String region_ID = lasRequest.getProperty("qc", "region_ID");
        String flag = lasRequest.getProperty("qc", "flag");
        String comment = lasRequest.getProperty("qc", "comment");
        String reviewer = lasRequest.getProperty("qc", "reviewer");
        String override = lasRequest.getProperty("qc", "override");
        
        
        String version = lasRequest.getProperty("socat_vars", "version");
       
        // Write a bogus response.
        StringBuilder found = new StringBuilder();

        found.append("cruise_ID="+cruise_ID);
        found.append("\n");
        found.append("region_ID="+region_ID);
        found.append("\n");
        found.append("flag="+flag);
        found.append("\n");
        found.append("comment="+comment);
        found.append("\n");
        found.append("reviewer="+reviewer);
        found.append("\n");
        found.append("override="+override);
        found.append("\n");
        
        response.setContentType("text/plain");
        PrintWriter os = response.getWriter();
        os.write(found.toString());
		return null;
	}

}
