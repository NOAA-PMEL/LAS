package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.ui.json.JSONUtil;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Dataset;
import gov.noaa.pmel.tmap.las.util.Variable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class GetVariables extends Action {

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
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
		String format = request.getParameter("format");

		if ( format == null ) {
			format = "json";
		}

		log.debug("Processing request for variables for dsid="+dsID);

		Dataset dataset = null;
		try {
			dataset = lasConfig.getFullDatasetNoGrids(dsID);
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (LASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		StringBuffer xml = new StringBuffer();
		Element category = new Element("category");
		category.addContent(dataset.getElement());
		ArrayList<Category> categories = new ArrayList<Category>();
		Category cat = new Category(category);
		categories.add(cat);
	

		try {
			PrintWriter respout = response.getWriter();
			if (format.equals("xml")) {
				response.setContentType("application/xml");
				respout.print(xml.toString());
			} else {
				response.setContentType("application/json");
				JSONObject json_response = Util.toJSON(categories, "categories");
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