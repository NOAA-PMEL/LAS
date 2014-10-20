package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.product.request.ProductRequest;
import gov.noaa.pmel.tmap.las.product.server.LASAction;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.proxy.LASProxy;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Constraint;
import gov.noaa.pmel.tmap.las.util.Dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;

import com.cohort.util.String2;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;

public class GetTrajectoryTable extends LASAction {

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			        throws Exception {	
	    
	    
	    DateTimeFormatter short_fmt = DateTimeFormat.forPattern("dd-MMM-yyyy").withZone(DateTimeZone.UTC);
	    
        DateTimeFormatter long_fmt = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm").withZone(DateTimeZone.UTC);

        DateTimeFormatter iso_fmt = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
        
	    LASProxy lasProxy = new LASProxy();
	    LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
	    String xml = request.getParameter("xml");
	    String dsid;
	    String xlo = null;
	    String xhi = null;
	    String ylo = null;
	    String yhi = null;
	    String zlo = null;
	    String zhi = null;
	    String tlo = null;
	    String thi = null;
	    LASUIRequest lasUIRequest = (LASUIRequest) request.getAttribute("las_request");
	    if ( lasUIRequest != null ) {

	        dsid = lasUIRequest.getDatasetIDs().get(0);
	        xlo = lasUIRequest.getXlo();
	        xhi = lasUIRequest.getXhi();
	        ylo = lasUIRequest.getYlo();
	        yhi = lasUIRequest.getYhi();
	        zlo = lasUIRequest.getZlo();
	        zhi = lasUIRequest.getZhi();
	        tlo = lasUIRequest.getTlo();
	        thi = lasUIRequest.getThi();

	    } else {
	        dsid = request.getParameter("dsid");
	    }
	    if ( dsid != null ) {
	        ProductRequest productRequest;
	        try {
	            productRequest = new ProductRequest(lasConfig, lasUIRequest, "false", "ANY");
	            Element operationElement = lasConfig.getElementByXPath(lasUIRequest.getOperationXPath());
	            productRequest.makeRequest(operationElement, lasConfig, lasUIRequest, "false", "ANY");
	        } catch ( LASException e ) {
	            logerror(request, e.getMessage(), e);
	            return mapping.findForward("error");
	        } catch ( UnsupportedEncodingException e) {
	            logerror(request, "Error creating the product request.", e);
	            return mapping.findForward("error");
	        } catch ( JDOMException e) {
	            logerror(request, "Error creating the product request.", e);
	            return mapping.findForward("error");
	        }
	        List<LASBackendRequest> reqs = productRequest.getRequestXML();
	        LASBackendRequest backRequest = reqs.get(0);
	        
	        Dataset ds = lasConfig.getDataset(dsid);
	        Map<String, String> fp = ds.getPropertiesAsMap().get("ferret");
	        String is = null;
            if ( fp != null ) {
                is = fp.get("is_socat");
            }
            boolean socat = false;
            if ( is != null && !is.equals("") ) {
                socat = true;
            }
	        
	        // lasRequest below try to be backRequest
	        
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
                    String cruise_id = tabledap.get("trajectory_id");
                    String lon_domain = tabledap.get("lon_domain");
                    StringBuilder xquery2 = new StringBuilder();
                    StringBuilder xquery1 = new StringBuilder();
                    StringBuilder dsgQuery = new StringBuilder();
                    StringBuilder csvQuery = new StringBuilder();
	                VariableSerializable[] vars = dataset.getVariablesSerializable();
	                if ( vars.length > 0 ) {
	                    String dataurl = lasConfig.getDataAccessURL(dsid, vars[0].getID(), false);
	                   
	                    if ( dataurl != null && !dataurl.equals("") ) {
	                        if ( dataurl.contains("#") ) dataurl = dataurl.substring(0, dataurl.indexOf("#"));
	                        if (!dataurl.endsWith("/") ) dataurl = dataurl + "/";
	                        
	                        // Use the full data for the downloads.
                            String fulldataurl = dataurl + id;
                            
                            boolean smallarea = false;

                            if ( lon_domain.contains("180") ) {
                                if (xlo.length() > 0 && xhi.length() > 0 ) {

                                    double xhiDbl = String2.parseDouble(xhi);
                                    double xloDbl = String2.parseDouble(xlo);
                                    // Check the span before normalizing and if it's big, just forget about the lon constraint all together.
                                    if ( Math.abs(xhiDbl - xloDbl ) < 358. ) {
                                       
                                        // This little exercise will normalize the x values to -180, 180.
                                        LatLonPoint p = new LatLonPointImpl(0, xhiDbl);
                                        xhiDbl = p.getLongitude();
                                        p = new LatLonPointImpl(0, xloDbl);
                                        xloDbl = p.getLongitude();

                                        double xspan = Math.abs(xhiDbl - xloDbl);
                                        double yloDbl = -90.d;
                                        double yhiDbl = 90.d;
                                        if (ylo.length() > 0) {
                                            yloDbl = Double.valueOf(ylo);
                                        }
                                        if (yhi.length() > 0) {
                                            yhiDbl = Double.valueOf(yhi);
                                        }
                                        double yspan = Math.abs(yhiDbl - yloDbl);

                                        double fraction = ((xspan+yspan)/(360.d + 180.d));

                                        if ( fraction < .1d ) {
                                            smallarea = true;
                                        }
                                    }
                                }
                            }
                            
	                        // If available use the decimated URL for the other requests if the area is NOT small.
	                        if ( did != null && !did.equals("") && !smallarea ) {
	                            id = did;
	                        }
	                        dataurl = dataurl + id;
	                        
	                        if ( id != null && !id.equals("") ) {
	                            
	                            String url = dataurl + ".csv";
	                            String dsgurl = fulldataurl+".ncCF";
	                            String csvurl = fulldataurl + ".csv";
	                            
	                            if ( document_base != null && !document_base.endsWith("/") ) document_base = document_base + "/";
	                            if ( table != null && !table.equals("") ) {
	                                url = url + "?" + table;
	                                response.setContentType("application/xhtml+xml");
	                                Document doc = new Document();

	                                String query = "";
	                                
	                                Map<String, String> dt = dataset.getPropertiesAsMap().get("tabledap_access");  
	                                boolean is360 = false;
	                                if ( dt != null ) {
	                                    String range = dt.get("lon_domain");
	                                    is360 = !range.contains("180");
	                                }
	                                
	                                try {
	                                    if ( xlo != null && xlo.length() > 0 && xhi != null && xhi.length() > 0 ) {
	                                        double dxlo = Double.valueOf(xlo);
	                                        double dxhi = Double.valueOf(xhi);
	                                        // Do the full globe and two query dance...
	                                        if ( is360 ) {
	                                            if ( dxlo < 0 ) {
	                                                dxlo = dxlo + 360.;
	                                            }
	                                            if ( dxhi < 0 ) {
	                                                dxhi = dxhi + 360.;
	                                            }
	                                        }
	                                        if ( Math.abs(dxhi - dxlo ) < 355. ) {
	                                            if ( !is360 ) {
	                                                LatLonPoint p = new LatLonPointImpl(0, dxhi);
	                                                dxhi = p.getLongitude();
	                                                p = new LatLonPointImpl(0, dxlo);
	                                                dxlo = p.getLongitude();
	                                            }

	                                            if ( dxhi < dxlo ) {
	                                                if ( dxhi < 0 && dxlo >= 0 ) {
	                                                    dxhi = dxhi + 360.0d;
	                                                    xquery1.append("&lon360>=" + dxlo);
	                                                    xquery1.append("&lon360<=" + dxhi);
	                                                    xquery2.append("&longitude>="+dxlo+"&longitude<"+180);
	                                                } // else request overlaps, so leave it off
	                                            } else {
	                                                xquery1.append("&longitude>="+dxlo);
	                                                xquery1.append("&longitude<="+dxhi);
	                                            }

	                                        }
	                                    } else {
	                                        // 
	                                        if ( xlo != null && xlo.length() > 0 ) xquery1.append("&longitude>="+xlo);
	                                        if ( xhi != null && xhi.length() > 0 ) xquery1.append("&longitude<="+xhi);
	                                    }

	                                } catch (Exception e2) {
	                                    // live without x constraints.
	                                }


	                                if ( ylo != null && !ylo.equals("") ) {
	                                    query = query + "&latitude>="+ylo;
	                                }
	                                if ( yhi != null && !yhi.equals("") ) {
	                                    query = query + "&latitude<="+yhi;
	                                }
	                                if ( zlo != null && !zlo.equals("") ) {
	                                    query = query + "&depth>="+zlo;
	                                }
	                                if ( zhi != null && !zhi.equals("") ) {
	                                    query = query + "&depth<="+zhi;
	                                }
	                                if ( tlo != null && !tlo.equals("") ) {
	                                    DateTime dlo;
	                                    try {
	                                        dlo = long_fmt.parseDateTime(tlo);
	                                    } catch (Exception e) {
	                                        try {
	                                            dlo = short_fmt.parseDateTime(tlo);
	                                        } catch (Exception e1) {
	                                            logerror(request, "Error parsing dates...", e);
	                                            return mapping.findForward("error");
	                                        }
	                                    }
	                                    String dtlo = iso_fmt.print(dlo.getMillis());
	                                    query = query + "&time>=\""+dtlo+"\"";
	                                }
	                                if ( thi != null && !thi.equals("") ) {
	                                    DateTime dhi;
	                                    try {
	                                        dhi = long_fmt.parseDateTime(thi);
	                                    } catch (Exception e) {
	                                        try {
	                                            dhi = short_fmt.parseDateTime(thi);
	                                        } catch (Exception e1) {
	                                            logerror(request, "Error parsing dates...", e);
	                                            return mapping.findForward("error");
	                                        }
	                                    }
	                                    String dthi = iso_fmt.print(dhi.getMillis());
	                                    query = query + "&time<=\""+dthi+"\"";
	                                }
	                                
	                                List constraints = backRequest.getConstraints();
                                    for (Iterator conIt = constraints.iterator(); conIt.hasNext();) {
                                        Constraint con = (Constraint) conIt.next();
                                        if ( con.getOp().equals("is") ) {
                                            query = query + "&" + con.getAsERDDAPString();
                                        } else {
                                            query = query + "&" + con.getAsString();
                                        }
                                        
                                    }
	                                
	                                // If there is only 1 x query, we're done...
	                                int xlimit = 1;
	                                List<String> previouslySent = new ArrayList<String>();
	                                if ( xquery2.length() > 0 ) {
	                                    // If there are two, we have to do two queries, save the results from the first to eliminate dups

	                                    xlimit = 2;
	                                }
	                                
	                                
	                                
	                                for (int l = 0; l < xlimit; l++ ) {

	                                    String fullquery = "";
	                                    if ( l == 0 ) {
	                                        fullquery = query + xquery1.toString();
	                                    } else {
	                                        fullquery = query + xquery2.toString();
	                                    }


	                                    fullquery = fullquery + "&distinct()";
	                                    fullquery = URLEncoder.encode(fullquery, "UTF-8");
	                                    url = url + fullquery;
	                                    InputStream input = lasProxy.executeGetMethodAndReturnStream(url, response);
	                                    OutputStream output = response.getOutputStream();

	                                    BufferedWriter bsw = new BufferedWriter(new OutputStreamWriter(output));

	                                    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
	                                            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
	                                            "<html xmlns=\"http://www.w3.org/1999/xhtml\">"+
	                                            "<head>"+
	                                            "  <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />"+
	                                            "  <title>Table of Cruises</title>"+
	                                            "  <script src=\"JavaScript/frameworks/jquery-1.11.1.min.js\" type=\"text/javascript\"></script>"+
	                                            "</head>"+
	                                            "<body style=\"color:black; background:white; font-family:Arial,Helvetica,sans-serif; font-size:85%; line-height:130%;\">"+
	                                            "<table border=\"2\" cellpadding=\"4\" cellspacing=\"0\">";

	                                    bsw.write(header);

	                                    if ( input != null ) {
	                                        BufferedReader bsr = new BufferedReader(new InputStreamReader(input));
	                                        // The regex comes from the geniuses at: 
	                                        // http://stackoverflow.com/questions/1757065/splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
	                                        // Holy mother...


	                                        // Process the column names.
	                                        String line = bsr.readLine();
	                                        String[] titles = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	                                        StringBuilder columnHeaders = new StringBuilder();
	                                        columnHeaders.append("<tr>\n");
	                                        
	                                        for (int i = 0; i < titles.length; i++) {
	                                            columnHeaders.append("\n<th>"+titles[i]+"</th>\n");
	                                        }
	                                        if ( socat ) {
	                                            columnHeaders.append("<th>documentation</th>\n");
	                                        }
	                                        columnHeaders.append("<th>download</th>\n");
	                                        columnHeaders.append("<th>start</th>\n");
	                                        columnHeaders.append("<th>end</th>\n");

	                                        if ( socat ) {
	                                            columnHeaders.append("<th>crossovers</th>\n");
	                                            columnHeaders.append("<th>qc flags</th>\n");
	                                        }
	                                        columnHeaders.append("<th>thumbnails</th>\n");

	                                        columnHeaders.append("</tr>\n");
	                                        bsw.write(columnHeaders.toString());

	                                        // Process the units.
	                                        line = bsr.readLine();
	                                        String[] units = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	                                        StringBuilder unitStrings = new StringBuilder();
	                                        unitStrings.append("<tr>\n");
	                                        unitStrings.append("<th></th>\n");

	                                        for (int i = 0; i < units.length; i++) {
	                                            unitStrings.append("<th>"+units[i]+"</th>\n");
	                                        }
	                                        if ( socat ) {
	                                            unitStrings.append("<th></th>\n");
	                                        }
	                                        unitStrings.append("<th></th>\n");
	                                        unitStrings.append("<th></th>\n");
	                                        if ( socat ) {
	                                            unitStrings.append("<th></th>\n");
	                                            unitStrings.append("<th></th>\n");
	                                        }
	                                        unitStrings.append("<th></th>\n");
	                                        unitStrings.append("</tr>\n");
	                                        bsw.write(unitStrings.toString());

	                                        int index = 1;
	                                        line = bsr.readLine();

	                                        while ( line != null ) {
	                                          
	                                            String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	                                            if ( !previouslySent.contains(parts[0])) {
	                                                previouslySent.add(parts[0]);
	                                                StringBuilder row = new StringBuilder();
	                                                if ( index%2 == 0 ) {
	                                                    row.append("<tr bgcolor=\"#FFFFFF\">");
	                                                } else {
	                                                    row.append("<tr bgcolor=\"#F3E7C9\">");
	                                                }
	                                                
	                                                for (int i = 0; i < parts.length; i++) {
	                                                    try {
	                                                        Double d = Double.valueOf(parts[i]);
	                                                        row.append("<td align=\"right\">"+parts[i]+"</td>\n");
	                                                    } catch (NumberFormatException e) {
	                                                        row.append("<td nowrap=\"nowrap\" colspan=\"1\">"+parts[i]+"</td>\n");
	                                                    }
	                                                }
	                                                dsgQuery.append("?&amp;"+cruise_id+"=\""+parts[0]+"\"");
	                                                csvQuery.append("?&amp;"+cruise_id+"=\""+parts[0]+"\"");
	                                                if ( socat ) {
	                                                    row.append("<td nowrap=\"nowrap\" colspan=\"1\"><a href=\""+document_base+parts[0].substring(0,4)+"\">Documentation</a>"+"</td>\n");
	                                                }
                                                    row.append("<td nowrap=\"nowrap\" colspan=\"1\"><a href='"+dsgurl+dsgQuery.toString()+"'>netcdf,</a><a href='"+csvurl+csvQuery.toString()+"'>csv</a>"+"</td>\n");
                                                    dsgQuery.setLength(0);
                                                    csvQuery.setLength(0);
                                                    InputStream stream = null;
	                                                // Call out to ERDDAP for the lat/lon/time box.
	                                                try {
	                                                   
	                                                    JsonStreamParser jp = null;

	                                                    String timeurl = dataurl + ".json?"+URLEncoder.encode(titles[0]+",time,latitude,longitude&"+titles[0]+"=\""+parts[0]+"\"&distinct()&orderByMinMax(\"time\")", "UTF-8");
	                                                    stream = null;

	                                                    stream = lasProxy.executeGetMethodAndReturnStream(timeurl, response);

	                                                    jp = new JsonStreamParser(new InputStreamReader(stream));
	                                                    JsonObject timebounds = (JsonObject) jp.next();
	                                                    JsonArray timeminmax = getMinMax(timebounds);
	                                                    stream.close();
	                                                    row.append("<td nowrap=\"nowrap\" colspan=\"1\">"+timeminmax.get(0).getAsString()+"</td>");
	                                                    row.append("<td nowrap=\"nowrap\" colspan=\"1\">"+timeminmax.get(1).getAsString()+"</td>");
	                                                    // Call out to ERDDAP for all the CRUISES in the same lat/lon/time box.
	                                                } catch ( Exception e ) {
	                                                    if (stream != null ) 
	                                                       stream.close();
	                                                    row.append("<td nowrap=\"nowrap\" colspan=\"1\">Unable to load time min.</td>");
	                                                    row.append("<td nowrap=\"nowrap\" colspan=\"1\">Unable to load time max.</td>");
	                                                }

	                                                if ( socat ) {
	                                                    row.append("\n<td id=\""+parts[0]+"\" nowrap=\"nowrap\" colspan=\"1\">");
	                                                    // Add the link to load a list of potential crosses to the table.
	                                                    row.append("<a href=\"javascript:$(\'#"+parts[0]+"\').html('&lt;div&gt;checking...&lt;/div&gt;');$(\'#"+parts[0]+"\').load(\'getCrossovers.do?dsid="+dsid+"&amp;tid="+parts[0]+"\');void(0);\">Check for crossovers</a>");
	                                                    row.append("\n</td>");

	                                                    // Add the QC link
	                                                    LASUIRequest qcRequest = new LASUIRequest();
	                                                    qcRequest.addVariable(dsid, vars[0].getID());
	                                                    qcRequest.setOperation("SOCAT_QC_table");
	                                                    qcRequest.setProperty("qc", cruise_id, parts[0]);
	                                                    qcRequest.setProperty("las", "output_type", "xml");
	                                                    row.append("\n<td id=\""+parts[0]+"\" nowrap=\"nowrap\" colspan=\"1\">");
	                                                    // Add the link to load a list of potential crosses to the table.
	                                                    String qc_url = "ProductServer.do?xml="+qcRequest.toEncodedURLString();


	                                                    row.append("<a href=\""+qc_url+"\">Edit the QC Flag</a>");
	                                                    row.append("\n</td>");
	                                                }
                                                    // ADD a THUMBNAIL table link...
                                                    LASUIRequest thumb = (LASUIRequest) lasUIRequest.clone();
                                                    thumb.removeLinks();
                                                    thumb.setOperation("PropPropThumbTable");
                                                    thumb.addTextConstraint(cruise_id, "eq", parts[0]);
                                                    String thumburl = "ProductServer.do?catid="+dsid+"&amp;xml="+thumb.toEncodedURLString();
                                                    row.append("\n<td id=\""+parts[0]+"\" nowrap=\"nowrap\" colspan=\"1\">");
                                                    row.append("<a target=\"_blank\" href=\""+thumburl+"\">Thumbnails</a>");
                                                    row.append("\n</td>");
	                                                
	                                                row.append("</tr>");
	                                                bsw.write(row.toString());
	                                            }
	                                            line = bsr.readLine();
	                                            index++;
	                                        }

	                                        bsw.write("</table>\n</body>\n</html>");

	                                        input.close();

	                                        bsw.flush();
	                                        bsw.close();
	                                        bsr.close();

	                                    }
	                                }
	                            } else {
	                                logerror(request, "Unable to fetch information from server...","query = "+URLDecoder.decode(url, "UTF-8"));
                                    return mapping.findForward("error");
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
