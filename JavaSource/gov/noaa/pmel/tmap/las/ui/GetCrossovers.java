package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.client.laswidget.Constants;
import gov.noaa.pmel.tmap.las.client.serializable.ConstraintSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.product.server.LASAction;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Dataset;
import gov.noaa.pmel.tmap.las.util.Variable;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.Document;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;

public class GetCrossovers extends LASAction {

    DateTimeFormatter short_fmt = DateTimeFormat.forPattern("dd-MMM-yyyy").withZone(DateTimeZone.UTC);
    
    DateTimeFormatter long_fmt = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss").withZone(DateTimeZone.UTC);

    DateTimeFormatter iso_fmt = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
    
    public static final double EARTH_AUTHALIC_RADIUS_KM = 6371.007;
    public static final double CUTOFF = 80.d;
    public static final double SPEED = 30.d;
    
	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			        throws Exception {	
	    
	    

        
	    LASProxy lasProxy = new LASProxy();
	    LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
	    String tid = request.getParameter("tid");
	    String dsid = request.getParameter("dsid");
	    String xml = request.getParameter("xml");
	    
	    List<String> candiateCruises = new ArrayList<String>();
	    List<Crossover> crossingCruises = new ArrayList<Crossover>();
	 
	    if ( dsid != null ) {
	        String latid = null;
	        String lonid = null;
	        List<Category> c = lasConfig.getCategories(dsid);
	        Dataset dataset = c.get(0).getDataset();
	        Map<String, Map<String, String>> properties = dataset.getPropertiesAsMap();
	        List<Variable> variables =  lasConfig.getVariables(dsid);
	        for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
                Variable variable = (Variable) varIt.next();
                if ( variable.getName().toLowerCase().contains("latitude") ) {
                    latid = variable.getID();
                } else if ( variable.getName().toLowerCase().contains("longitude") ) {
                    lonid = variable.getID();
                }
            }
	        
	        if ( properties != null ) {
	            Map<String, String> tabledap = properties.get("tabledap_access");
	            if ( tabledap != null ) {
	                String table = tabledap.get("table_variables");
                    String document_base = tabledap.get("document_base");
                    String id = tabledap.get("id");
                    String did = tabledap.get("decimated_id");
                    
                    String traj_id_name = tabledap.get("trajectory_id");
                   
                    
	                VariableSerializable[] vars = dataset.getVariablesSerializable();
	                if ( vars.length > 0 ) {
	                    String dataurl = lasConfig.getDataAccessURL(dsid, vars[0].getID(), false);
	                   
	                    if ( dataurl != null && !dataurl.equals("") ) {
	                        if ( dataurl.contains("#") ) dataurl = dataurl.substring(0, dataurl.indexOf("#"));
	                        if (!dataurl.endsWith("/") ) dataurl = dataurl + "/";
	                        
	                        if ( did != null && !did.equals("") ) {
	                            id = did;
	                        }
	                        dataurl = dataurl + id;
	                        JsonArray timeminmax = null;
	                        
	                        if ( id != null && !id.equals("") ) {
	                            
	                            String url = dataurl + ".csv";

	                            if ( document_base != null && !document_base.endsWith("/") ) document_base = document_base + "/";
	                            if ( table != null && !table.equals("") ) {
	                                url = url + "?" + table;
	                                response.setContentType("application/xhtml+xml");
	                                Document doc = new Document();


	                                OutputStream output = response.getOutputStream();

	                                BufferedWriter bsw = new BufferedWriter(new OutputStreamWriter(output));



	                                // Call out to ERDDAP for the lat/lon/time box.
	                                try {
	                                    String laturl = dataurl + ".json?"+URLEncoder.encode(traj_id_name+",latitude,longitude,time&"+traj_id_name+"=\""+tid+"\"&distinct()&orderByMinMax(\"latitude\")","UTF-8");
	                                    InputStream stream = null;
	                                    JsonStreamParser jp = null;

	                                    stream = lasProxy.executeGetMethodAndReturnStream(laturl, response);
	                                    jp = new JsonStreamParser(new InputStreamReader(stream));

	                                    JsonObject latbounds = (JsonObject) jp.next();
	                                    JsonArray latminmax = getMinMax(latbounds);
	                                    stream.close();




	                                    String lonurl = dataurl + ".json?"+URLEncoder.encode(traj_id_name+",longitude,latitude,time&"+traj_id_name+"=\""+tid+"\"&distinct()&orderByMinMax(\"longitude\")", "UTF-8");

	                                    stream = lasProxy.executeGetMethodAndReturnStream(lonurl, response);
	                                    jp = new JsonStreamParser(new InputStreamReader(stream));
	                                    JsonObject lonbounds = (JsonObject) jp.next();
	                                    JsonArray lonminmax = getMinMax(lonbounds);
	                                    stream.close();




	                                    String timeurl = dataurl + ".json?"+URLEncoder.encode(traj_id_name+",time,latitude,longitude&"+traj_id_name+"=\""+tid+"\"&distinct()&orderByMinMax(\"time\")", "UTF-8");
	                                    stream = null;

	                                    stream = lasProxy.executeGetMethodAndReturnStream(timeurl, response);

	                                    jp = new JsonStreamParser(new InputStreamReader(stream));
	                                    JsonObject timebounds = (JsonObject) jp.next();
	                                    timeminmax = getMinMax(timebounds);
	                                    stream.close();

	                                    // Call out to ERDDAP for all the CRUISES in the same lat/lon/time box.

	                                    String crossoversurl = dataurl + ".json?"+URLEncoder.encode(traj_id_name+"&"+traj_id_name+"!=\""+tid+"\"&distinct()&time>="+timeminmax.get(0).getAsString()+"&time<="+timeminmax.get(1).getAsString()+
	                                            "&latitude>="+latminmax.get(0).getAsString()+"&latitude<="+latminmax.get(1).getAsString()+
	                                            "&longitude>="+lonminmax.get(0).getAsString()+"&longitude<="+lonminmax.get(1).getAsString(), "UTF-8");
	                                    stream = null;
	                                    stream = lasProxy.executeGetMethodAndReturnStream(crossoversurl, response);



	                                    if ( stream != null ) {
	                                        jp = new JsonStreamParser(new InputStreamReader(stream));
	                                        JsonObject crossovers = (JsonObject) jp.next(); 
	                                        JsonArray rows = (JsonArray) ((JsonObject) (crossovers.get("table"))).get("rows");


	                                        // Add the list of potential crosses to the table.
	                                        for (int i = 0; i < rows.size(); i++) {
	                                            candiateCruises.add(rows.get(i).getAsString());
	                                        }
	                                        stream.close();
	                                    } else {
	                                        bsw.write("none");
	                                        bsw.flush();
	                                        bsw.close();
	                                        return null;
	                                    }
	                                } catch (Exception e) {
	                                    bsw.write("check failed");
	                                    bsw.flush();
	                                    bsw.close();
	                                    return null;
	                                }
	                                StringBuilder crosslinks = new StringBuilder("none");
	                                if ( candiateCruises.size() > 0 ) {
	                                    // We found some.  Compute the cross overs.
	                                    JsonObject selectedCruise = null;
	                                    // get the data for the selected cruise.
	                                    String selectedCruiseURL = dataurl + ".json?"+URLEncoder.encode(traj_id_name+",time,latitude,longitude&"+traj_id_name+"=\""+tid+"\"&distinct()&orderBy(\"time\")", "UTF-8");
	                                    InputStream stream = lasProxy.executeGetMethodAndReturnStream(selectedCruiseURL, response);
	                                    if ( stream != null ) {
	                                        JsonStreamParser jp = new JsonStreamParser(new InputStreamReader(stream));
	                                        selectedCruise = (JsonObject) jp.next();
	                                        stream.close();
	                                    }
	                                   
	                                    if ( selectedCruise != null ) {
	                                        for (Iterator cIt = candiateCruises.iterator(); cIt.hasNext();) {
	                                            String cid = (String) cIt.next();
	                                            String potentialCrossURL = dataurl + ".json?"+URLEncoder.encode(traj_id_name+",time,latitude,longitude&"+traj_id_name+"=\""+cid+"\"&distinct()&orderBy(\"time\")", "UTF-8");
	                                            InputStream st = lasProxy.executeGetMethodAndReturnStream(selectedCruiseURL, response);
	                                            if ( st != null ) {
	                                                JsonStreamParser jp = new JsonStreamParser(new InputStreamReader(st));
	                                                JsonObject potentialCross = (JsonObject) jp.next();
	                                                Crossover cross = checkCrossover(selectedCruise, potentialCross);
	                                                st.close();
	                                                if ( cross != null ) {
	                                                    LASUIRequest lasRequest = new LASUIRequest();
	                                                    lasRequest.addVariable(dsid, lonid);
	                                                    lasRequest.addVariable(dsid, latid);
                                                        
	                                                    lasRequest.setOperation("SPPV");
	                                                    double xmin = cross.lonAtMin - 5.0;
	                                                    double xmax = cross.lonAtMin + 5.0;	                                                    
	                                                    
	                                                    double ymin = cross.latAtMin - 5.0;
	                                                    double ymax = cross.latAtMin + 5.0;
	                                                    
	                                                    // The time range we want to set should cover the time range of both cruises.
	                                                    
	                                                    List<String> allMinMaxDates = new ArrayList<String>();
	                                                    allMinMaxDates.add(cross.cruiseTimeMin);
	                                                    allMinMaxDates.add(cross.cruiseTimeMax);
	                                                    
	                                                    allMinMaxDates.add(timeminmax.get(0).getAsString());
	                                                    allMinMaxDates.add(timeminmax.get(0).getAsString());

	                                                    Collections.sort(allMinMaxDates);
	                                                    
	                                                    HashMap<String, HashMap<String,String[]>> region = new HashMap<String, HashMap<String,String[]>>();
	                                                    HashMap<String,String[]> intervals = new HashMap<String,String[]>();
	                                                    HashMap<String,String[]> points = new HashMap<String,String[]>();
	                                                    intervals.put("x", new String[] {String.valueOf(xmin), String.valueOf(xmax)});
	                                                    intervals.put("y", new String[] {String.valueOf(ymin), String.valueOf(ymax)});
	                                                    String tloAll = allMinMaxDates.get(0);
	                                                    String thiAll = allMinMaxDates.get(1);
	                                                    DateTime tloadt = iso_fmt.parseDateTime(tloAll);
	                                                    DateTime thiadt = iso_fmt.parseDateTime(thiAll);
	                                                    String ftmin = long_fmt.print(tloadt.getMillis());
	                                                    String ftmax = long_fmt.print(thiadt.getMillis());
	                                                    intervals.put("t", new String[] {ftmin, ftmax});
	                                                    
	                                                    region.put("points", points);
	                                                    region.put("intervals", intervals);
	                                                    lasRequest.setRegion(region);

	                                                    lasRequest.addTextConstraint(traj_id_name, "eq", tid);
	                                                    lasRequest.addTextConstraint(traj_id_name, "eq", cid);
	                                                    
	                                                    lasRequest.setProperty("ferret", "view", "xyt");

	                                                    if ( crosslinks.toString().equals("none") ) {
	                                                        crosslinks.replace(0, crosslinks.length(), "<div>");
	                                                    }
	                                                    
	                                                    crosslinks.append("<a target=\"_blank\" href=\"ProductServer.do?catid="+dsid+"&amp;xml="+lasRequest.toEncodedURLString()+"\">"+cid+"</a>\n");
	                                                }
	                                            }
	                                        }
	                                    }
	                                }
	                                crosslinks.append("</div>");
	                                bsw.write(crosslinks.toString());
	                                bsw.flush();
	                                bsw.close();


	                            }
	                        }
	                    }
	                }
	            }
	        }
	    }
	    return null;
	}
	public class Crossover {
	    String id;
	    double minDistance;
	    double latAtMin;
	    double lonAtMin;
	    String timeAtMin;
	    String cruiseTimeMin;
	    String cruiseTimeMax;
	    public Crossover(String id, double minDistance, double latAtMin, double lonAtMin, String timeAtMin, String cruiseTimeMin, String cruiseMaxDate) {
	        this.id = id;
	        this.minDistance = minDistance;
	        this.latAtMin = latAtMin;
	        this.lonAtMin = lonAtMin;
	        this.timeAtMin = timeAtMin;
	        this.cruiseTimeMin = cruiseTimeMin;
	        this.cruiseTimeMax = cruiseMaxDate;
	    }
	}
	public Crossover checkCrossover(JsonObject selectedCruise,  JsonObject potentialCross) {
	       // Max allowable difference in time in milliseconds
        long timeDelta = (long) Math.ceil(24.0 * 60.0 * 60.0 * 1000.0 * CUTOFF / SPEED);

        // Max allowable difference in latitude in degrees
        double latDelta = (CUTOFF / EARTH_AUTHALIC_RADIUS_KM) * 
                          (180.0 / Math.PI);
        
        double minDistance = Double.MAX_VALUE;
        
	    Crossover crossover = null;
	    JsonArray rows = (JsonArray) ((JsonObject) (selectedCruise.get("table"))).get("rows");
	    JsonArray crossingRows = (JsonArray) ((JsonObject) (potentialCross.get("table"))).get("rows");
	    
	    // Save the min date for the crossing cruise.
	    String cruiseMinDate = null;
	    String cruiseMaxDate = null;
	    for (int i = 0; i < rows.size(); i++) {
	        // A row is id, date, latitude, longitude
            JsonArray row = (JsonArray) rows.get(i);
            String id = row.get(0).getAsString();
            String date = row.get(1).getAsString();
            DateTime dt = iso_fmt.parseDateTime(date);
            long time = dt.getMillis();
            double lat = row.get(2).getAsDouble();
            double lon = row.get(3).getAsDouble();
            for ( int j = 0; j < crossingRows.size(); j++ ) {
                JsonArray crossingRow = (JsonArray) crossingRows.get(j);
                String crossingID = crossingRow.get(0).getAsString();
                String crossingDate = crossingRow.get(1).getAsString();
                if ( j == 0 ) {
                    cruiseMinDate = crossingDate;
                    JsonArray maxRow = (JsonArray) crossingRows.get(crossingRows.size() -1);
                    cruiseMaxDate = maxRow.get(1).getAsString();
                }
                DateTime cdt = iso_fmt.parseDateTime(crossingDate);
                long crossingTime = cdt.getMillis();
                double crossingLat = crossingRow.get(2).getAsDouble();
                double crossingLon = crossingRow.get(3).getAsDouble();
                
                if ( crossingTime > time + timeDelta  ) {
                    /* 
                     * The rest of the second cruise occurred far 
                     * later than the point of first cruise.  
                     * Go on to the next point of the first cruise.
                     */
                    break;
                }
                if ( crossingTime < time - timeDelta ) {
                    /* 
                     * This point of the second cruise occurred far 
                     * earlier than the point of the first cruise.
                     * Go on to the next point of the second cruise.
                     */
                    continue;
                }
                if ( Math.abs(lat - crossingLat) > latDelta ) {
                    /*
                     * Differences in latitudes are too large.
                     * Go on to the next point of the second cruise. 
                     */
                    continue;
                }
                double locTimeDist = distanceTo(lat, lon, time, crossingLat, crossingLon, crossingTime, SPEED, EARTH_AUTHALIC_RADIUS_KM );
                if ( minDistance > locTimeDist ) {
                    minDistance = locTimeDist;
                    if ( locTimeDist <= CUTOFF ) {
                       
                        crossover = new Crossover(crossingID, locTimeDist, crossingLat, crossingLon, crossingDate, cruiseMinDate, cruiseMaxDate);
                        
                       
                    }
                }
                
            }
            
        }
	    return crossover;
	}
	/**
     * Returns the location-time "distance" to another location-time point 
     * using the provided conversion factor for time to distance and the 
     * given radius for a spherical Earth.  Uses the haversine formula to
     * compute the great circle distance from the longitudes and latitudes.
     * 
     * @param other
     *      the other location-time point to use
     * @param speed
     *      the number of kilometers to use for 24 hours of time;
     *      can be zero to obtain a distance without a time contribution
     * @param radius
     *      the radius of a spherical Earth in kilometers 
     * @return
     *      the location-time distance between this location-time point
     *      and other in kilometers
     */
    public double distanceTo(double lat, double lon, long time, double crossingLat, double crossingLon, long crossingTime, double speed, double radius) {
        // Convert longitude and latitude degrees to radians
        double lat1 = lat * Math.PI / 180.0;
        double lat2 = crossingLat * Math.PI / 180.0;
        double lon1 = lon * Math.PI / 180.0;
        double lon2 = crossingLon * Math.PI / 180.0;
        /*
         * Use the haversine formula to compute the great circle distance, 
         * in radians, between the two (longitude, latitude) points. 
         */
        double dellat = Math.sin(0.5 * (lat2 - lat1));
        dellat *= dellat;
        double dellon = Math.sin(0.5 * (lon2 - lon1));
        dellon *= dellon * Math.cos(lat1) * Math.cos(lat2);
        double distance = 2.0 * Math.asin(Math.sqrt(dellon + dellat));
        // Convert the great circle distance from radians to kilometers
        distance *= radius;

        if ( speed != 0.0 ) {
            // Get the time difference in days (24 hours)
            double deltime = (crossingTime - time) / 
                             (24.0 * 60.0 * 60.0 * 1000.0);
            // Convert to the time difference to kilometers
            deltime *= speed;
            // Combine the time distance with the surface distance
            distance = Math.sqrt(distance * distance + deltime * deltime);
        }

        return distance;
    }

	private JsonArray getMinMax(JsonObject bounds) {
	    JsonArray rows = (JsonArray) ((JsonObject) (bounds.get("table"))).get("rows");
        JsonArray row1 = (JsonArray) rows.get(0);
        JsonArray row2 = (JsonArray) rows.get(1);
        
        String min = ((JsonElement) row1.get(1)).getAsString();
        String max = ((JsonElement) row2.get(1)).getAsString();
        JsonArray minmax = new JsonArray();
        minmax.add(new JsonPrimitive(min));
        minmax.add(new JsonPrimitive(max));
        return minmax;
	}
	public static String prepareURL(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		String dsid = request.getParameter("dsid");
		String catitem = request.getParameter("catitem");
		String opendap = request.getParameter("opendap");
		if ( catitem != null ) dsid = catitem;
		LASUIRequest ui_request = new LASUIRequest();
		ui_request.addVariable(dsid, "dummy");
		if ( opendap != null ) {
		   ui_request.setOperation("MetadataURLs");
		} else {
			ui_request.setOperation("Metadata");
		}
		return ui_request.toEncodedURLString();
	}

}
