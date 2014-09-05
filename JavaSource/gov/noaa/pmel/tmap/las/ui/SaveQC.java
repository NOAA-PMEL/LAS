package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.socat.dashboard.handlers.DatabaseRequestHandler;
import gov.noaa.pmel.socat.dashboard.handlers.DsgNcFileHandler;
import gov.noaa.pmel.socat.dashboard.server.DashboardServerUtils;
import gov.noaa.pmel.socat.dashboard.shared.SocatQCEvent;
import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.jdom.LASDocument;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.product.server.LASAction;
import gov.noaa.pmel.tmap.las.service.TemplateTool;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.Element;

public class SaveQC extends LASAction {

	private static Logger log = Logger.getLogger(SaveQC.class.getName());
	private static final String DATABASE_CONFIG = "DatabaseBackendConfig.xml";
	private static final String DATABASE_NAME = "SOCATFlags";

	private String socatQCVersion;
	private DsgNcFileHandler dsgHandler;
	private DatabaseRequestHandler databaseHandler;

	/**
	 * Creates with the SOCAT UploadDashboard DsgNcFileHandler and DatabaseRequestHandler
	 * 
	 * @throws IllegalArgumentException
	 * 		if parameters are invalid
	 * @throws SQLException
	 * 		if one is thrown connecting to the database
	 * @throws LASException 
	 * 		if unable to get the database parameters
	 */
	public SaveQC() throws IllegalArgumentException, SQLException, LASException {
		super();
		log.debug("Initializing SaveQC from database configuraton");

		Element dbParams;
		try {
			LASDocument dbConfig = new LASDocument();
			TemplateTool tempTool = new TemplateTool("database", DATABASE_CONFIG);
			JDOMUtils.XML2JDOM(tempTool.getConfigFile(), dbConfig);
			dbParams = dbConfig.getElementByXPath(
					"/databases/database[@name='" + DATABASE_NAME + "']");
		} catch (Exception ex) {
			throw new LASException(
					"Could not parse " + DATABASE_CONFIG + ": " + ex.toString());
		}
		if ( dbParams == null )
			throw new LASException("No database definition found for database " + 
					DATABASE_NAME + " in " + DATABASE_CONFIG);

		String databaseDriver = dbParams.getAttributeValue("driver");
		log.debug("driver=" + databaseDriver);
		String databaseUrl = dbParams.getAttributeValue("connectionURL");
		log.debug("databaseUrl=" + databaseUrl);
		String selectUsername = dbParams.getAttributeValue("user");
		log.debug("selectUsername=" + selectUsername);
		String selectPassword = dbParams.getAttributeValue("password");
		log.debug("selectPassword=" + selectPassword);
		String updateUsername = dbParams.getAttributeValue("updateUser");
		log.debug("updateUsername=" + updateUsername);
		String updatePassword = dbParams.getAttributeValue("updatePassword");
		log.debug("updatePassword=" + updatePassword);
		// The database URLs in the LAS config files do not have the jdbc: prefix
		databaseHandler = new DatabaseRequestHandler(databaseDriver, "jdbc:" + databaseUrl, 
				selectUsername, selectPassword, updateUsername, updatePassword);
		log.debug("database request handler configuration successful");

		socatQCVersion = dbParams.getAttributeValue("socatQCVersion");
		log.debug("socatQCVersion=" + socatQCVersion);

		String dsgFileDir = dbParams.getAttributeValue("dsgFileDir");
		log.debug("dsgFileDir=" + dsgFileDir);
		String decDsgFileDir = dbParams.getAttributeValue("decDsgFileDir");
		log.debug("decDsgFileDir=" + decDsgFileDir);
		String erddapDsgFlag = dbParams.getAttributeValue("erddapDsgFlag");
		log.debug("erddapDsgFlag=" + erddapDsgFlag);
		String erddapDecDsgFlag = dbParams.getAttributeValue("erddapDecDsgFlag");
		log.debug("erddapDecDsgFlag=" + erddapDecDsgFlag);
		dsgHandler = new DsgNcFileHandler(dsgFileDir, decDsgFileDir, erddapDsgFlag, erddapDecDsgFlag);
		log.debug("DSG file handler configuration successful");
	}

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		// Get the request from the query parameter.
		String requestXML = request.getParameter("xml");

		// LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
		// Same for the ServerConfig
		// ServerConfig serverConfig = (ServerConfig)servlet.getServletContext().getAttribute(ServerConfigPlugIn.SERVER_CONFIG_KEY);
		// Get the global cache object.
		// Cache cache = (Cache) servlet.getServletContext().getAttribute(ServerConfigPlugIn.CACHE_KEY);

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

		// getProperty returns a trimmed string; an empty string if not found
		// String version = lasRequest.getProperty("socat_vars", "version");

		String expocode = lasRequest.getProperty("qc", "cruise_ID");
		String regionID = lasRequest.getProperty("qc", "region_ID");
		String flag = lasRequest.getProperty("qc", "flag");
		String comment = lasRequest.getProperty("qc", "comment");
		String reviewer = lasRequest.getProperty("qc", "reviewer");
		// String override = lasRequest.getProperty("qc", "override");

		try {
			String upperExpocode = DashboardServerUtils.checkExpocode(expocode);
			expocode = upperExpocode;
		} catch (Exception ex) {
			logerror(request, "Invalid expocode '" + expocode + "' specified", "");
			return mapping.findForward("error");
		}
		if ( regionID.isEmpty() ) {
			logerror(request, "No region ID specified", "");
			return mapping.findForward("error");
		}
		if ( flag.isEmpty() ) {
			// Empty flag is acceptable - just a QC comment
			flag = " ";
		}
		if ( comment.isEmpty() ) {
			logerror(request, "No comment provided", "");
			return mapping.findForward("error");
		}

		// Validate the reviewer assigning this QC flag
		String username;
		try {
			log.debug("Validating SaveQC reviewer");
			username = request.getUserPrincipal().getName();
		} catch ( Exception ex ) {
			logerror(request, "Unable to get the username for QC flagging", ex);
			return mapping.findForward("error");
		}
		if ( (username == null) || ! username.equalsIgnoreCase(reviewer) ) {
			logerror(request, "Invalid username " + username + " for reviewer " + reviewer, "");
			return mapping.findForward("error");
		}

		// Create the QC event
		SocatQCEvent qcEvent = new SocatQCEvent();
		qcEvent.setSocatVersion(socatQCVersion);
		qcEvent.setExpocode(expocode);
		qcEvent.setRegionID(regionID.charAt(0));
		qcEvent.setUsername(username);
		qcEvent.setComment(comment);
		qcEvent.setFlag(flag.charAt(0));
		qcEvent.setFlagDate(new Date());

		// Add the QC event to the database
		try {
			databaseHandler.addQCEvent(qcEvent);
			log.debug("QC event " + qcEvent.toString() + " added to the database");
		} catch (Exception ex) {
			logerror(request, "Unable to record the QC event " + 
					qcEvent.toString() + " in the database", ex);
			return mapping.findForward("error");
		}

		// Update the QC event flag to assign to the DSG files
		try {
			qcEvent.setFlag(databaseHandler.getQCFlag(expocode));
			log.debug("QC flag '" + qcEvent.getFlag().toString() + 
					"' returned by the database handler");
		} catch (Exception ex) {
			logerror(request, "Unable to obtain from the database " +
					"the QC flag to assign to the DSG files", ex);
			return mapping.findForward("error");
		}

		// Update the QC flag in the DSG files
		try {
			dsgHandler.updateQCFlag(qcEvent);
			log.debug("QC event " + qcEvent.toString() + " added to the DSG files");
		} catch (Exception ex) {
			logerror(request, "Unable to record the QC flag " + 
					qcEvent.getFlag().toString() + " in the DSG files", ex);
			return mapping.findForward("error");
		}

		return mapping.findForward("qc");
	}

}
