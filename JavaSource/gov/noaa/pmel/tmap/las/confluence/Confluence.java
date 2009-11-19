package gov.noaa.pmel.tmap.las.confluence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONException;

import HTTPClient.URI;

import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASDocument;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.product.server.LASAction;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.product.server.ProductServerAction;
import gov.noaa.pmel.tmap.las.ui.GetDataConstraints;
import gov.noaa.pmel.tmap.las.ui.GetGrid;
import gov.noaa.pmel.tmap.las.ui.GetMetadata;
import gov.noaa.pmel.tmap.las.ui.GetOperations;
import gov.noaa.pmel.tmap.las.ui.GetOptions;
import gov.noaa.pmel.tmap.las.ui.GetRegions;
import gov.noaa.pmel.tmap.las.ui.GetVariables;
import gov.noaa.pmel.tmap.las.ui.GetViews;
import gov.noaa.pmel.tmap.las.ui.LASProxy;
import gov.noaa.pmel.tmap.las.ui.Util;
import gov.noaa.pmel.tmap.las.ui.GetCategories;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Constants;
import gov.noaa.pmel.tmap.las.util.ContainerComparator;
import gov.noaa.pmel.tmap.las.util.Tributary;

public class Confluence extends LASAction {

	private static Logger log = LogManager.getLogger(Confluence.class.getName());

	private static final LASProxy lasProxy = new LASProxy();

	private String request_format;

	public static final HashMap<String, String[]> ajaxCalls =   
		new HashMap<String, String[]>()   
		{  
			//Unnamed Block.  
			{  
				put(Constants.GET_CATEGORIES, new String[]{"catid"});  
				put("getDataConstraints", new String[]{"dsid", "varid"});  
				put("getGrid",  new String[]{"dsid", "varid"});  
				put("getMetadata", new String[]{"dsid", "catitem", "opendap"});  
				put("getOperations", new String[]{"dsid", "varid", "view"}); 
				put("getOptions", new String[]{"dsid", "opid"});
				put("getRegions", new String[]{"dsid", "varid"});
				put("getVariables", new String[]{"dsid"});
				put("getViews", new String[]{"dsid", "varid"});
			}  
		};
		
		public static final HashMap<String, String> services =   
			new HashMap<String, String>()   
			{  
				//Unnamed Block.  
				{  
					put(Constants.GET_CATEGORIES_KEY, Constants.GET_CATEGORIES);  
					put(Constants.GET_DATACONSTRAINTS_KEY, Constants.GET_DATACONSTRAINTS);  
					put(Constants.GET_GRID_KEY, Constants.GET_GRID);  
					put(Constants.GET_METADATA_KEY, Constants.GET_METADATA);  
					put(Constants.GET_OPERATIONS_KEY, Constants.GET_OPERATIONS); 
					put(Constants.GET_OPTIONS_KEY, Constants.GET_OPTIONS);
					put(Constants.GET_REGIONS_KEY, Constants.GET_REGIONS);
					put(Constants.GET_VARIABLES_KEY, Constants.GET_VARIABLES);
					put(Constants.GET_VIEWS_KEY, Constants.GET_VIEWS);
				}  
			};
		public ActionForward execute(ActionMapping mapping,
				ActionForm form,
				HttpServletRequest request,
				HttpServletResponse response){

			LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);

			ArrayList<Tributary> servers = new ArrayList<Tributary>();

			servers = lasConfig.getTributaries();

			request_format = "";
			// Process the request...
			String url = request.getRequestURL().toString();


			if ( url.contains(Constants.GET_CATEGORIES) ) {
				ArrayList<Category> categories = new  ArrayList<Category>();
				if ( request.getParameter("catid") ==  null) {
					// The only other possible parameter is the format.
					if ( request.getParameter("format") != null ) {
						// Save it.  When use it when we stream the response...
						request_format = request.getParameter("format");
					}
					try {
						// Start with the categories on this server...
						Category local_cat = new Category(lasConfig.getTitle(), lasConfig.getTopLevelCategoryID()); 
						categories.add(local_cat);

						for (Iterator servIt = servers.iterator(); servIt.hasNext();) {
							Tributary trib = (Tributary) servIt.next();
                            Category server_cat = new Category(trib.getName(), trib.getTopLevelCategoryID());
							categories.add(server_cat);
						}
						InputStream is = new ByteArrayInputStream(Util.toJSON(categories, "categories").toString().getBytes("UTF-8"));
						lasProxy.stream(is, response.getOutputStream());
					} catch (JSONException e) {
						log.error("Unable to get categories", e);
					} catch (UnsupportedEncodingException e) {
						log.error("Unable to get categories", e);
					} catch (IOException e) {
						log.error("Unable to get categories", e);
					} catch (JDOMException e) {
						log.error("Unable to get categories", e);
					}

				} else {
					String catid = request.getParameter("catid");
					String format = request.getParameter("format");
					if ( format == null ) format = "json";
					String server_key = catid.split(Constants.NAME_SPACE_SPARATOR)[0];
					try {
						if ( server_key.equals(lasConfig.getBaseServerURLKey()) ) {
							
							return mapping.findForward(Constants.GET_CATEGORIES_KEY);

						} else {
							
							Tributary trib = lasConfig.getTributary(server_key);
							String las_url = trib.getURL() + Constants.GET_CATEGORIES;					

							if ( !catid.endsWith(Constants.NAME_SPACE_SPARATOR+JDOMUtils.MD5Encode(trib.getName()))) {
								// If this is a request of the top level category for this server
								// the id must be left off the request.  In this case it's not so
								// add it on.
								las_url = las_url + "?" + request.getQueryString();
							}
							lasProxy.executeGetMethodAndStreamResult(las_url, response);
						}
					} catch (HttpException e) {
						log.error("Unable to fetch categories.", e);
					} catch (IOException e) {
						log.error("Unable to fetch categories.", e);
					} catch (JDOMException e) {
						log.error("Unable to fetch categories.", e);
					} 				
				}
			} else if (url.contains(Constants.GET_OPTIONS)) {
				try {
					// I think there is a bug in the client where it requests the options without the server key initially
					// so I'm going to throw that request away.

					String opid = request.getParameter("opid");
					if ( opid.contains(Constants.NAME_SPACE_SPARATOR) ) {
						String[] parts = opid.split(Constants.NAME_SPACE_SPARATOR);
						String server_key = parts[0];
						if ( server_key.equals(lasConfig.getBaseServerURLKey()) ) {
							return mapping.findForward(Constants.GET_OPTIONS_KEY);
						}

						opid = parts[1];

						Tributary trib = lasConfig.getTributary(server_key);
						String las_url = trib.getURL() + Constants.GET_OPTIONS + "?opid=" + opid;
						lasProxy.executeGetMethodAndStreamResult(las_url, response);
					} else {
						// Send it to the local server which will work most of the time..
						return mapping.findForward(Constants.GET_OPTIONS_KEY);
					}
				} catch (HttpException e) {
					log.error("Unable to fetch categories.", e);
				} catch (IOException e) {
					log.error("Unable to fetch categories.", e);
				} catch (JDOMException e) {
					log.error("Unable to fetch categories.", e);
				}
			} else if (url.contains(Constants.PRODUCT_SERVER)) {
				String xml = request.getParameter("xml");
				LASUIRequest ui_request = new LASUIRequest();
				try {
					JDOMUtils.XML2JDOM(xml, ui_request);
				} catch (IOException e) {
					logerror(request, "Failed to parse XML request.", e);
					return mapping.findForward("error");					
				} catch (JDOMException e) {
					logerror(request, "Failed to parse XML request.", e);
					return mapping.findForward("error");
				}
				ArrayList<String> ids = ui_request.getDatasetIDs();
				if ( ids.size() == 0 ) {
					// Forward to the local server...
					return mapping.findForward("LocalProductServer");
				} else if ( ids.size() > 1 ) {
					logerror(request, "Cannot do two variables yet.", "Comming soon.");
					return mapping.findForward("error");
				} else {
					try {
						// Get the server key and forward to that server...
						String server_key = ids.get(0).split(Constants.NAME_SPACE_SPARATOR)[0];
						if ( server_key.equals(lasConfig.getBaseServerURLKey()) ) {
							return mapping.findForward("LocalProductServer");
						}
						Tributary trib = lasConfig.getTributary(server_key);
						String las_url = trib.getURL();
						las_url = las_url + Constants.PRODUCT_SERVER + "?" + request.getQueryString();	
						lasProxy.executeGetMethodAndStreamResult(las_url, response);
					} catch (HttpException e) {
						logerror(request, "Unable to fetch categories.", e);
					} catch (IOException e) {
						logerror(request, "Unable to fetch categories.", e);
					} catch (JDOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else {
				String dsid = request.getParameter("dsid");
				String server_key = dsid.split(Constants.NAME_SPACE_SPARATOR)[0];
				try {
					boolean local = server_key.equals(lasConfig.getBaseServerURLKey());
					String las_url = null;
					if ( !local ) {
						Tributary trib = lasConfig.getTributary(server_key);
						las_url = trib.getURL();
					}
					if ( url.contains(Constants.GET_DATACONSTRAINTS) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_DATACONSTRAINTS_KEY);
						} else {
						    las_url = las_url + Constants.GET_DATACONSTRAINTS + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_GRID) ) {
						if ( local ) {	
							return mapping.findForward(Constants.GET_GRID_KEY);
						} else {
						    las_url = las_url + Constants.GET_GRID + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_METADATA) ) {
						if ( local ) {
							return new ActionForward("/LocalProductServer.do?xml="+GetMetadata.prepareURL(request, response));
						} else {		
							las_url = las_url + Constants.GET_METADATA + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_OPERATIONS) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_OPERATIONS_KEY);
						} else {
							las_url = las_url + Constants.GET_OPERATIONS + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_REGIONS) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_REGIONS_KEY);
						} else {
						    las_url = las_url + Constants.GET_REGIONS + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_VARIABLES) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_VARIABLES_KEY);
						} else {
						   las_url = las_url + Constants.GET_VARIABLES + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_VIEWS) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_VIEWS_KEY);
						} else {
						    las_url = las_url + Constants.GET_VIEWS + "?" + request.getQueryString();
						}
					}
					if ( !local ) {
					    lasProxy.executeGetMethodAndStreamResult(las_url, response);
					}					
				} catch (HttpException e) {
					logerror(request, "Unable to fetch categories.", e);
				} catch (IOException e) {
					logerror(request, "Unable to fetch categories.", e);
				} catch (JDOMException e) {
					logerror(request, "Unable to fetch categories.", e);
				}
			}
			return null;
		}
		
}