package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.socat.dashboard.handlers.DatabaseRequestHandler;
import gov.noaa.pmel.socat.dashboard.handlers.DsgNcFileHandler;
import gov.noaa.pmel.socat.dashboard.nc.Constants;
import gov.noaa.pmel.socat.dashboard.shared.DataLocation;
import gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent;
import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.jdom.LASDocument;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.product.server.LASAction;
import gov.noaa.pmel.tmap.las.service.TemplateTool;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.Element;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

public class SaveEdits extends LASAction {

	private static Logger log = Logger.getLogger(SaveEdits.class.getName());
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
	public SaveEdits() throws IllegalArgumentException, SQLException, LASException {
		super();
		log.debug("Initializing SaveEdits from database configuraton");

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

		// Parser to convert Ferret date strings into Date objects
		SimpleDateFormat fullDateParser = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		fullDateParser.setTimeZone(TimeZone.getTimeZone("UTC"));

		// Get the username of the reviewer assigning these WOCE flags
		String username;
		try {
			log.debug("Assigning SaveEdits username");
			username = request.getUserPrincipal().getName();
		} catch ( Exception ex ) {
			logerror(request, "Unable to get the username for WOCE flagging", ex);
			return mapping.findForward("error");
		}

		JsonStreamParser parser = new JsonStreamParser(request.getReader());
		JsonObject message = (JsonObject) parser.next();

		// LAS temporary DSG file to update
		String tempname;
		try {
			tempname = message.get("temp_file").getAsString();
		} catch ( Exception ex ) {
			logerror(request, "Unable to get temp_file for WOCE flagging", ex);
			return mapping.findForward("error");
		}

		// WOCE flag comment
		String comment;
		try {
			comment = message.get("comment").getAsString();
		} catch ( Exception ex ) {
			logerror(request, "Unable to get the comment for WOCE flagging", ex);
			return mapping.findForward("error");
		}

		// List of data points getting the WOCE flag
		JsonArray edits;
		try {
			edits = (JsonArray) message.get("edits");
			if ( edits.size() < 1 )
				throw new IllegalArgumentException("No edits given");
		} catch ( Exception ex ) {
			logerror(request, "Unable to get the edits for WOCE flagging", ex);
			return mapping.findForward("error");
		}

		// Create the list of (incomplete) data locations for the WOCE event
		String expocode = null;
		Character woceFlag = null;
		String dataName = null;
		ArrayList<DataLocation> locations = new ArrayList<DataLocation>(edits.size());
		try {
			for ( JsonElement rowValues : edits ) {
				DataLocation datumLoc = new DataLocation();
				for ( Entry<String,JsonElement> rowEntry : ((JsonObject) rowValues).entrySet() ) {
					// Neither the name nor the value should be null.
					// Because of going through Ferret, everything will be uppercase
					// but just to be sure....
					String name = rowEntry.getKey().trim().toUpperCase();
					String value = rowEntry.getValue().getAsString().trim().toUpperCase();
					if ( name.equals("EXPOCODE") || name.equals("EXPOCODE_") ) {
						if ( expocode == null )
							expocode = value;
						else if ( ! expocode.equals(value) )
							throw new IllegalArgumentException("Mismatch of expocodes; " +
									"previous: '" + expocode + "'; current: '" + value + "'");
					}
					else if ( name.equals("DATE") ) {
						Date dataDate = fullDateParser.parse(value);
						datumLoc.setDataDate(dataDate);
					}
					else if ( name.equals("LONGITUDE") ) {
						Double longitude = Double.parseDouble(value);
						datumLoc.setLongitude(longitude);
					}
					else if ( name.equals("LATITUDE") ) {
						Double latitude = Double.parseDouble(value);
						datumLoc.setLatitude(latitude);
					}
					else if ( name.equals("WOCE_CO2_WATER") ) {
						// WOCE flag for the data variable
						if ( value.length() != 1 )
							throw new IllegalArgumentException("Invalid WOCE flag value '" + value + "'");
						Character givenFlag = value.charAt(0);
						if ( woceFlag == null )
							woceFlag = givenFlag;
						else if ( ! woceFlag.equals(givenFlag) )
							throw new IllegalArgumentException("Mismatch of WOCE flags; " +
									"previous: '" + woceFlag + "'; current: '" + givenFlag + "'");
					}
					else {
						// Assume it is the data variable name.
						// Note that WOCE from just lat/lon/date plots will not have this column
						if ( dataName == null )
							dataName = name;
						else if ( ! dataName.equals(name) )
							throw new IllegalArgumentException("Mismatch of data names; " +
									"previous: '" + dataName + "'; current: '" + name + "'");
						Double dataValue = Double.parseDouble(value);
						datumLoc.setDataValue(dataValue);
					}
				}
				locations.add(datumLoc);
			}
		} catch ( Exception ex ) {
			logerror(request, "Problems interpreting the WOCE flags", ex);
			if ( expocode != null )
				logerror(request, "expocode = " + expocode, "");
			if ( dataName != null )
				logerror(request, "dataName = " + dataName, "");
			if ( woceFlag != null )
				logerror(request, "woceFlag = " + woceFlag, "");
			return mapping.findForward("error");
		}

		if ( expocode == null ) {
			logerror(request, "No EXPOCODE given in the WOCE flags", "");
			return mapping.findForward("error");
		}
		if ( woceFlag == null ) {
			logerror(request, "No WOCE_CO2_WATER given in the WOCE flags", "");
			return mapping.findForward("error");
		}
		if ( dataName == null ) {
			dataName = Constants.geoposition_VARNAME;
		}
		else {
			String varName = Constants.VARIABLE_NAMES.get(dataName);
			if ( varName == null ) {
				logerror(request, "Unknown data variable '" + dataName + "'", "");
				return mapping.findForward("error");
			}
			dataName = varName;
		}

		// Create the (incomplete) WOCE event
		SocatWoceEvent woceEvent = new SocatWoceEvent();
		woceEvent.setSocatVersion(socatQCVersion);
		woceEvent.setUsername(username);
		woceEvent.setComment(comment);
		woceEvent.setExpocode(expocode);
		woceEvent.setDataVarName(dataName);
		woceEvent.setFlag(woceFlag);
		woceEvent.setFlagDate(new Date());
		woceEvent.setLocations(locations);

		// Update the DSG files with the WOCE flags, filling in the missing information
		try {
			dsgHandler.updateWoceFlags(woceEvent, tempname);
			log.debug("DSG files updated");
		} catch ( Exception ex ) {
			logerror(request, "Unable to update DSG files with the WOCE flags", ex);
			logerror(request, "expocode = " + expocode + 
							"; dataName = " + dataName + 
							"; woceFlag = " + woceFlag, "");
			return mapping.findForward("error");
		}

		// Save the (complete) WOCE event to the database
		try {
			databaseHandler.addWoceEvent(woceEvent);
			log.debug("WOCE event added to the database");
		} catch ( Exception ex ) {
			logerror(request, "Unable to record the WOCE event in the database", ex);
			logerror(request, "expocode = " + expocode + 
							"; dataName = " + dataName + 
							"; woceFlag = " + woceFlag, "");
			return mapping.findForward("error");
		}

		// Notify ERDDAP of the full DSG file update
		dsgHandler.flagErddap(false);
		log.debug("ERDDAP flagged the the full DSG files were updated");

		log.info("Assigned WOCE event (also updated " + tempname + "): \n" + 
				woceEvent.toString());

		request.setAttribute("expocode", expocode);
		return mapping.findForward("edits");
	}

}
