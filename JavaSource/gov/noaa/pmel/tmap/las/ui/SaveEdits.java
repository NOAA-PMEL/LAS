package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.socat.dashboard.nc.DsgNcFileHandler;
import gov.noaa.pmel.socat.dashboard.server.DatabaseRequestHandler;
import gov.noaa.pmel.socat.dashboard.shared.DataLocation;
import gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent;
import gov.noaa.pmel.tmap.las.product.server.LASAction;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

public class SaveEdits extends LASAction {

	private static Logger log = LogManager.getLogger(SaveEdits.class.getName());

	private double socatQCVersion;
	private DsgNcFileHandler dsgHandler;
	private DatabaseRequestHandler databaseHandler;

	/**
	 * Creates with the SOCAT UploadDashboard DsgNcFileHandler and DatabaseRequestHandler
	 * 
	 * @throws IllegalArgumentException
	 * 		if parameters are invalid
	 * @throws SQLException
	 * 		if one is thrown connecting to or querying the database
	 */
	public SaveEdits() throws IllegalArgumentException, SQLException {
		super();

		log.debug("Initializing SaveEdits with hard-coded values");
		// TODO: All of the following values need to made into parameters retrieved from configuration file(s)
		socatQCVersion = 3.0;

		String dsgFileDir = "/home/data/socat/socatV3";
		String decDsgFileDir = "/home/data/socat/socat3_decimated";
		String erddapDsgFlag = "/home/data/kobrien/OSMC/erddap/flag/socatV3_c6c1_d431_8194";
		String erddapDecDsgFlag = "/home/data/kobrien/OSMC/erddap/flag/socatV3_decimated";
		dsgHandler = new DsgNcFileHandler(dsgFileDir, decDsgFileDir, erddapDsgFlag, erddapDecDsgFlag);

		String databaseDriver = "com.mysql.jdbc.Driver";
		String databaseUrl = "jdbc:mysql://localhost:3306/SOCATFlags";
		String catalogName = "SOCATFlags";
		String selectUsername = "erddap";
		String selectPassword = "dapper";
		String updateUsername = "scientist";
		String updatePassword = "qc4socat3";
		databaseHandler = new DatabaseRequestHandler(databaseDriver, databaseUrl, catalogName, 
				selectUsername, selectPassword, updateUsername, updatePassword);
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
					else if ( name.startsWith("WOCE_") ) {
						// WOCE flag for the data variable
						String woceDataName = name.substring(5);
						if ( dataName == null )
							dataName = woceDataName;
						else if ( ! dataName.equals(woceDataName) )
							throw new IllegalArgumentException("Mismatch of data names from WOCE name; " +
									"previous: '" + dataName + "'; current: '" + woceDataName + "'");
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
						// Note that WOCE_geoposition will not have this column
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
			logerror(request, "No WOCE_... given in the WOCE flags", "");
			return mapping.findForward("error");
		}
		if ( dataName == null ) {
			// Should never happen if woceFlag is not null 
			logerror(request, "Unexpected failure to get the data name for the WOCE flags", "");
			return mapping.findForward("error");
		}

		// Create the (incomplete) WOCE event
		SocatWoceEvent woceEvent = new SocatWoceEvent();
		woceEvent.setSocatVersion(socatQCVersion);
		woceEvent.setUsername(username);
		woceEvent.setComment(comment);
		woceEvent.setExpocode(expocode);
		woceEvent.setColumnName(dataName);  // Note: all-uppercase
		woceEvent.setFlag(woceFlag);
		woceEvent.setFlagDate(new Date());
		woceEvent.setLocations(locations);

		// Update the DSG files with the WOCE flags, filling in the missing 
		// information and fixing the data variable name in the process
		try {
			dsgHandler.updateWoceFlags(woceEvent, tempname, log);
		} catch ( Exception ex ) {
			logerror(request, "Unable to update DSG files with the WOCE flags", ex);
			logerror(request, "expocode = " + expocode, "");
			logerror(request, "dataName = " + dataName, "");
			logerror(request, "woceFlag = " + woceFlag, "");
			return mapping.findForward("error");
		}

		// Save the (complete) WOCE event to the database
		try {
			databaseHandler.addWoceEvent(woceEvent);
		} catch ( Exception ex ) {
			logerror(request, "Unable to record the WOCE event in the database", ex);
			logerror(request, "expocode = " + expocode, "");
			logerror(request, "dataName = " + dataName, "");
			logerror(request, "woceFlag = " + woceFlag, "");
			return mapping.findForward("error");
		}

		// Notify ERDDAP of the full DSG file update
		dsgHandler.flagErddap(false);

		log.info("Assigned WOCE event (also updated " + tempname + "): \n" + 
				woceEvent.toString());

		return null;
	}

}
