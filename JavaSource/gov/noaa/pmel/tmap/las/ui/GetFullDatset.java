package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.ui.json.JSONUtil;
import gov.noaa.pmel.tmap.las.util.Dataset;
import gov.noaa.pmel.tmap.las.util.Grid;
import gov.noaa.pmel.tmap.las.util.Variable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
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

public class GetFullDatset extends ConfigService {

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	private static Logger log = LogManager.getLogger(GetFullDatset.class.getName());
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {

		LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
		String dsID = request.getParameter("dsid");
		String format = request.getParameter("format");

		if ( format == null ) {
			format = "json";
		}

		log.debug("Processing request for variables for dsid="+dsID);

		Dataset dataset = null;
		try {
			dataset = lasConfig.getFullDataset(dsID);
			PrintWriter respout = response.getWriter();
			if (format.equals("xml")) {
				response.setContentType("application/xml");
				respout.print(dataset.toXML());
			} else {
				response.setContentType("application/json");
				JSONObject json_response = XML.toJSONObject(dataset.toXML());
				log.debug(json_response.toString(3));
				json_response.write(respout);
			}
		} catch (JDOMException e) {
			sendError(response, "<dataset>", format, "Could not parse XML");
		} catch (LASException e) {
			sendError(response, "<dataset>", format, "Could not parse XML");
		} catch (IOException e) {
			sendError(response, "<dataset>", format, "Could not parse XML");
		} catch (JSONException e) {
			sendError(response, "<dataset>", format, "Could not parse XML");
		}
    
		return null;
	}
}