package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.addxml.JDOMUtils;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.product.server.LASAction;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			        throws Exception {	
	    
	    
	    DateTimeFormatter short_fmt = DateTimeFormat.forPattern("dd-MMM-yyyy").withZone(DateTimeZone.UTC);
	    
        DateTimeFormatter long_fmt = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss").withZone(DateTimeZone.UTC);

        DateTimeFormatter iso_fmt = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
        
	    LASProxy lasProxy = new LASProxy();
	    LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
	    String tid = request.getParameter("tid");
	    String dsid = request.getParameter("dsid");
	 
	    if ( dsid != null ) {
	        List<Category> c = lasConfig.getCategories(dsid);
	        Dataset dataset = c.get(0).getDataset();
	        Map<String, Map<String, String>> properties = dataset.getPropertiesAsMap();
	        if ( properties != null ) {
	            Map<String, String> tabledap = properties.get("tabledap_access");
	            if ( tabledap != null ) {
	                String table = tabledap.get("table_variables");
                    String document_base = tabledap.get("document_base");
                    String id = tabledap.get("id");
                    String did = tabledap.get("decimated_id");
                    // DEBUG
                    did = "ddsg_files_badval_034c_f37d_432d";
                    
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
	                        
	                        
	                        if ( id != null && !id.equals("") ) {
	                            
	                            String url = dataurl + ".csv";
	                            
	                            // DEBUG
	                            document_base = "http://yahoo.com/";
	                            if ( document_base != null && !document_base.endsWith("/") ) document_base = document_base + "/";
	                            if ( table != null && !table.equals("") ) {
	                                url = url + "?" + table;
	                                response.setContentType("application/xhtml+xml");
	                                Document doc = new Document();
	                                

	                                OutputStream output = response.getOutputStream();
	                                
	                                BufferedWriter bsw = new BufferedWriter(new OutputStreamWriter(output));
	                                
	                                                               
	                               
	                                    StringBuilder row = new StringBuilder();
                                       
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
                                            JsonArray timeminmax = getMinMax(timebounds);
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
                                                    row.append(rows.get(i).getAsString());
                                                    if ( i < rows.size() - 1) row.append(", ");
                                                }
                                                
                                                stream.close();
                                            } else {
                                                row.append("none");
                                            }
                                        } catch (Exception e) {
                                            row.append("Check failed.  "+e.getMessage());
                                        }
                                    
                                        
                                       
                                        
                                        
                                        
	                                    
	                                    bsw.write(row.toString());
	                                  
	                                
	                                
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
