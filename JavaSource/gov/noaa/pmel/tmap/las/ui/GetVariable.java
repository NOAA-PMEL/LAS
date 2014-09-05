package gov.noaa.pmel.tmap.las.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.util.Dataset;
import gov.noaa.pmel.tmap.las.util.Variable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class GetVariable extends ConfigService {

	private static Logger log = Logger.getLogger(GetVariables.class.getName());
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {

		String query = request.getQueryString();
		if ( query != null ) {
			try{
				query = URLDecoder.decode(query, "UTF-8");
				log.info("START: "+request.getRequestURL()+"?"+query);
			} catch (UnsupportedEncodingException e) {
				// Don't care we missed a log message.
			}			
		} else {
			log.info("START: "+request.getRequestURL());
		}
		LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
		String dsID = request.getParameter("dsid");
		String varID = request.getParameter("varid");
		String format = request.getParameter("format");

		if ( format == null ) {
			format = "json";
		}

		log.debug("Processing request for variable for dsid="+dsID+" varid= "+varID);
		
		Variable variable = null;
		try {
			Dataset dataset = lasConfig.getDataset(dsID);			
			variable = dataset.getVariable(varID);
		} catch (JDOMException e) {
			
		} catch (LASException e) {
		
		}
		
		try {
			PrintWriter respout = response.getWriter();
			if (format.equals("xml")) {
				response.setContentType("application/xml");
				respout.print(variable.toXML().toString());
			} else {
				response.setContentType("application/json");
				JSONObject json_response = XML.toJSONObject(variable.toXML().toString());
				log.debug(json_response.toString(3));
				json_response.write(respout);
			}
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
		if ( query != null ) {
			log.info("END:   "+request.getRequestURL()+"?"+query);						
		} else {
			log.info("END:   "+request.getRequestURL());
		}
		return null;
	}
}
