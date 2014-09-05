/**
 * 
 */
package gov.noaa.pmel.tmap.las.product.server;

import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.DataTable;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASAnnotations;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASMapScale;
import gov.noaa.pmel.tmap.las.jdom.LASRegionIndex;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.jdom.ServerConfig;
import gov.noaa.pmel.tmap.las.product.request.ProductRequest;
import gov.noaa.pmel.tmap.las.proxy.LASProxy;
import gov.noaa.pmel.tmap.las.service.ProductLocalService;
import gov.noaa.pmel.tmap.las.service.ProductWebService;
import gov.noaa.pmel.tmap.las.util.Institution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.WebRowSet;

import oracle.jdbc.rowset.OracleWebRowSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.JDOMException;

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dods.DODSNetcdfFile;
import ucar.nc2.dt.TypedDatasetFactory;
import ucar.nc2.dt.grid.GridDataset;

import com.sun.rowset.WebRowSetImpl;

/**
 * @author Roland Schweitzer
 *
 */
public final class ProductServerAction extends LASAction {
    private static Logger log = Logger.getLogger(ProductServerAction.class.getName());
    private static LASProxy lasProxy= new LASProxy();

    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response){

        ProgressForm progress = (ProgressForm) form;
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
        log.debug("Entering ProductServerAction");
        String lazy_start = (String) servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_LAZY_START_KEY);
        if ( lazy_start != null && lazy_start.equals("true") ) {
        	return mapping.findForward("lazy_start");
        }
        // Get the LASConfig (sub-class of JDOM Document) from the servlet context.
        
        LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
        // Same for the ServerConfig
        ServerConfig serverConfig = (ServerConfig)servlet.getServletContext().getAttribute(ServerConfigPlugIn.SERVER_CONFIG_KEY);
        // Get the global cache object.
        Cache cache = (Cache) servlet.getServletContext().getAttribute(ServerConfigPlugIn.CACHE_KEY);
        // Get the version string
        String version = (String) servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_VERSION_KEY);
        if ( version == null ) {
        	version = "7";
        }
        
        boolean ftds_up = Boolean.valueOf((String) servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_FTDS_UP_KEY));
        
        Institution institution = null;
        try {
            institution = lasConfig.getInstitution();
            request.setAttribute("institution", institution);
        } catch (JDOMException e1) {
            // We don't really care...
        }
        
        String serverURL;
        String serverBaseURL; 
        try {
            serverURL = lasConfig.getServerURL();
            serverBaseURL = lasConfig.getBaseServerURL();
        } catch (JDOMException e) {
            logerror(request, "Error getting product server URL...", e);
            return mapping.findForward("error");
        }
        
        if (serverURL.equals("")) {
            logerror(request, "No product server URL defined.", "Check las.xml operations element...");
            return mapping.findForward("error");
        }
        
        // See if the server is being reinitialized.  If so, stop.
        String lock = (String) servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_LOCK_KEY);
        if ( lock != null && lock.equals("true") ) {
        	return mapping.findForward("maintenance");
        }
        // Get the request from the query parameter.
        String requestXML = request.getParameter("xml");
        log.debug("Processing request xml="+requestXML);
        String stream_param = request.getParameter("stream");
        boolean stream = Boolean.valueOf(stream_param).booleanValue();
        String[] stream_ids = request.getParameterValues("stream_ID");
        
        String cancelParam = request.getParameter("cancel");
        boolean cancel = false;
        if ( cancelParam != null ) {
            cancel = cancelParam.equals("true");
        }
        
        String JSESSIONID = request.getParameter("JSESSIONID");
        if ( JSESSIONID == null ) {
        	// From the new UI, get the cookie.
        	Cookie cookies[] = request.getCookies();
        	if ( cookies != null ) {
        		for (int i = 0; i < cookies.length; i++) {
        			String name = cookies[i].getName();
        			String value = cookies[i].getValue();
        			if ( name.equals("JSESSIONID") ) {
        				JSESSIONID = value;
        			}
        		}
        	}
        }
        String email=null;
        if (progress != null) {
           email = progress.getEmail();
        }
        
        if ( email == null ) {
        	email = "Comma separated list of emails.";
        	if ( progress != null ) {
        		progress.setEmail(email);
        	}
        }
        

        if ( (requestXML == null || requestXML.equals("")) ) {
            try {
                request.setAttribute("title", lasConfig.getTitle());
                request.setAttribute("services", serverConfig.getServiceNamesAndURLs());
            }
            catch (Exception e) {
                logerror(request, "Error creating info page..", e);
                return mapping.findForward("error");
            }
            log.debug("Returning info page.");
            return mapping.findForward("info"); 
        }
        
       
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
        
        lasRequest.setProperty("product_server", "version", version);
        String[] catids = (String[]) request.getSession().getAttribute("catid");
        boolean check = true;
        if ( catids != null && catids.length > 0 ) check = false;
        if ( lasRequest.isAnalysisRequest() && check) {
        	// This request will require F-TDS...
        	// If it's marked as down, test it...
        	if ( !ftds_up ) {
        		if ( !testFTDS(lasConfig) ) {
        		    return mapping.findForward("ftds_down");
        		}
        	}
        }
        
        
        // Get the debug level from the query or request property.
        
        String debug = request.getParameter("debug");
       
        
        if ( debug == null ) {
            debug = lasConfig.getGlobalPropertyValue("las", "debug");
        }

        if ( debug == null || debug == "" ) {
           if (  lasRequest.getProperty("las", "debug") != null &&
                !lasRequest.getProperty("las", "debug").equals("") ) {
              debug = lasRequest.getProperty("las", "debug");
           } 
        }
 
        

        if ( debug.equalsIgnoreCase("true") ) {
        	request.setAttribute("debug", "true");
        	lasRequest.setProperty("product_server","use_cache", "false");
        	lasRequest.setProperty("las", "debug", "true");
        } else {
        	request.setAttribute("debug", "false");
        	lasRequest.setProperty("las", "debug", "false");
        }

        log.debug("Creating ProductRequest object.");
        // Create a new ProductRequest object
        ProductRequest productRequest;
        try {
            productRequest = new ProductRequest(lasConfig, lasRequest, debug, JSESSIONID);
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
        
         
        // This is a special product request to simply forward to the output template (no backend service will be invoked).
       
            try {
                if ( productRequest.getServiceName(productRequest.getOperationID(0)).equals("template") ) {
                    log.debug("Returning "+productRequest.getTemplate());
                    String mimeType = productRequest.getTemplateMimeType();
                    
                    if ( mimeType != null && !mimeType.equals("") ) {
                        request.setAttribute("las_mime_type", mimeType);
                    }
                    
                    return new ActionForward("/productserver/templates/"+productRequest.getTemplate()+".vm");
                }
            } catch (LASException e) {
                logerror(request, "Error checking for 'template' service. ", e);
                return mapping.findForward("error");
            }
  
        log.debug("Starting ProductServerRunner thread.");
        ProductServerRunner productServerRunner = (ProductServerRunner) servlet.getServletContext().getAttribute("runner_"+productRequest.getCacheKey());
        HashSet<String> sessions = (HashSet) servlet.getServletContext().getAttribute("sessions_"+productRequest.getCacheKey());
        // The only way to share jobs is if the cache is being used.
        // If it's not in the cache, then you can't guarantee it will
        // still be around for the second job.
        
        if ( productServerRunner != null ) {
        	
            synchronized(productServerRunner) {
                
                if ( email != null ) {
                    productServerRunner.setEmails(email);
                    progress.setEmail("Comma separated list of email addresses.");
                } else {
                    progress.setEmail("Comma separated list of email addresses.");
                }
                      
                
                if ( JSESSIONID != null && !JSESSIONID.equals("") ) {
                    if ( sessions != null ) {
                        sessions.add(JSESSIONID);
                    }
                }

                // This session is the only one asking for this product, and it
                // has requested it be cancelled so stop the current ActionRunner thread.
                if ( cancel && sessions.size() == 1) {
                    log.debug("Request canceled.");
                    productServerRunner.setCancel(true);
                    // Send cancel request to current service
                    // and forward user to 'cancel' page.
                    LASBackendRequest backendRequestDocument = productServerRunner.getCurrentBackendRequest();
                    int currentOp = productServerRunner.getCurrentOp();
                    backendRequestDocument.setCancel();
                    String key = productRequest.getOperationID(currentOp);
                    String backendServerURL = null;
                    String productCacheKey=null;
                    String methodName = null;
                    
                        try {
                            backendServerURL = serverConfig.getServerURL(productRequest.getServiceName(key));
                            methodName = serverConfig.getMethodName(productRequest.getServiceName(key));
                            productCacheKey = productRequest.getCacheKey(key);
                        } catch (LASException e) {
                            logerror(request, "Unable to send cancel request to backend service.", e);
                            removeOnError(productRequest);
                            return mapping.findForward("error");
                        }
                    
                    log.debug("Job canceled.  Send cancel request to backend service.");
                    ProductWebService productWebService = null;
                    ProductLocalService productLocalService = null;
                    try {
                    	if ( !backendServerURL.equals("local")) {
                    		productWebService = new ProductWebService(backendRequestDocument, 
                    				backendServerURL, 
                    				methodName, 
                    				productCacheKey);
                    	} else {
                    		productLocalService = new ProductLocalService(
                                    backendRequestDocument, backendServerURL,
                                    methodName, productCacheKey);
                            /*
                             * The gobbledygook below is the code necessary to invoke a method in a class when
                             * the name of the method is a variable (we know the class it's productLocalService).  
                             * The result if methodName="getTHREDDS" is to invoke the following:
                             * productLocalService.getTHREDDS(lasBackendRequest, lasConfig, serverConfig);
                             * 
                             * Add the time out tester here.  Could use a bit more generality.
                             */
                            Object[] oargs = null;
                            Method method = null;
                            if ( methodName.equals("getTHREDDS")) {
                            	Class[] args = new Class[3];
                            	args[0] = backendRequestDocument.getClass();
                            	args[1] = lasConfig.getClass();
                            	args[2] = serverConfig.getClass();
                            	method = productLocalService.getClass().getMethod(methodName, args);
                            	oargs = new Object[3];
                            	oargs[0] = backendRequestDocument;
                            	oargs[1] = lasConfig;
                            	oargs[2] = serverConfig;
                            	
                            } else if ( methodName.equals("fiveMinutes") ) {
                            	Class[] args = new Class[1];
                            	args[0] = backendRequestDocument.getClass();
                            	method = productLocalService.getClass().getMethod(methodName, args);
                            	oargs = new Object[1];
                            	oargs[0] = backendRequestDocument;
                            }
                            if (method != null ) {
                               method.invoke(productLocalService, oargs);
                            }   
                    	}
                    } catch (LASException e) {
                        logerror(request, "Error building Web service request for "+ methodName , e);
                        removeOnError(productRequest);
                        return mapping.findForward("error");
                    } catch (IOException e) {
                        logerror(request, "Error building Web service request for "+ methodName , e);
                        removeOnError(productRequest);
                        return mapping.findForward("error");
                    } catch (SecurityException e) {
                    	logerror(request, "Error building Local service request for "+ methodName , e);
                    	removeOnError(productRequest);
                        return mapping.findForward("error");
					} catch (NoSuchMethodException e) {
						logerror(request, "Error building Local service request for "+ methodName , e);
						removeOnError(productRequest);
                        return mapping.findForward("error");
					} catch (IllegalArgumentException e) {
						logerror(request, "Error running Local service request for "+ methodName , e);
						removeOnError(productRequest);
                        return mapping.findForward("error");
					} catch (IllegalAccessException e) {
						logerror(request, "Error running Local service request for "+ methodName , e);
						removeOnError(productRequest);
                        return mapping.findForward("error");
					} catch (InvocationTargetException e) {
						logerror(request, "Error running Local service request for "+ methodName , e);
						removeOnError(productRequest);
                        return mapping.findForward("error");
					}
                    String responseXML="";
                    LASBackendResponse lasResponse = new LASBackendResponse();
                    try {
                    	if ( !backendServerURL.equals("local") ) {
                    		productWebService.run();
                    		responseXML = productWebService.getResponseXML();
                    	} else {
                    		responseXML = productLocalService.getResponseXML();
                    	}
                    } catch (Exception e) {
                    	logerror(request, "Error was returned from the backend server.", e);
                    	removeOnError(productRequest);
                    	return mapping.findForward("error");
                    }
                    log.debug("Finished canceling request.");
                    
                    try {
                        JDOMUtils.XML2JDOM(responseXML, lasResponse);
                    } catch (Exception e) {
                        logerror(request, "Error parsing the XML returned from the backend server.", e);
                        removeOnError(productRequest);
                        return mapping.findForward("error");
                    }
                    request.setAttribute("server_url", serverURL);

                    if ( sessions != null ) {
                        request.setAttribute("JSESSIONID", JSESSIONID);
                    }
                    request.setAttribute("las_response", lasResponse);
                    servlet.getServletContext().removeAttribute("sessions_"+productRequest.getCacheKey());
                    servlet.getServletContext().removeAttribute("runner_"+productRequest.getCacheKey());
                    log.debug("Returning.  Request canceled");
                    return mapping.findForward("cancel");
                }
                
                // If there is a request runner that matches this one already running, get it...
                
                
                if (productServerRunner.isAlive() && productServerRunner.stillWorking()) {
                    log.debug("Found a running thread for this request.");
                    try {
                        
                        /*
                         * Since we found a running thread for this service, it means it's already a "batch"
                         * so don't wait around for the full timeout.  Join the thread for a couple of seconds
                         * to see if the job is finished then return the progress page.
                         */
                        
                        productServerRunner.setBatch(true);
                        
                        long timeout = productServerRunner.getProgressTimeout()*1000;
                        
                        String ui_timeout_string = lasRequest.getProperty("product_server", "ui_timeout");
                        long ui_timeout = -999;
                        if ( ui_timeout_string != null && !ui_timeout_string.equals("") ) {
                        	 ui_timeout = Long.valueOf(ui_timeout_string);
                        }
                        if ( ui_timeout > 0 ) {
                        	ui_timeout = ui_timeout*1000;
                        	timeout = Math.min(ui_timeout, timeout);
                        }
                        if ( timeout > 0 ) {
                        	
                            // Timeout set in the request.
                            long to = Math.max(timeout, 2000);
                           
                            productServerRunner.join(to);
                            
                        } else {
                            // No time out set.  Very unlikely, but possible if a new request wants the same
                            // product without an associated session.
                        	
							System.err
									.println("No time out set.  Very unlikely, but possible if a new request wants the same "
											+ "product without an associated session.\n"
											+ "Calling productServerRunner.join();");
                            productServerRunner.join();
                        }                        
                    } catch (Exception e) {
                        logerror(request, "Error joining the thread creating this product.", e);
                        removeOnError(productRequest);
                        return mapping.findForward("error");
                    }
                    
                    // We're not done yet.
                    // setup the progress page
                    
                    if ( productServerRunner.stillWorking() ) {                    
                        if ( sessions != null ) {
                             request.setAttribute("JSESSIONID", JSESSIONID);
                        }
                        request.setAttribute("server_url", serverURL);
                        request.setAttribute("status", productServerRunner.getStatus());
                        request.setAttribute("operations", productRequest.getOperationNames());
                        request.setAttribute("cache", productRequest.getUseCache());
                        request.setAttribute("emails", productServerRunner.getEmails().toString());
                        request.setAttribute("feed", productServerRunner.getCurrentBackendRequest().getResult("rss"));
                        request.setAttribute("seconds", productServerRunner.getSeconds());
                        request.setAttribute("date", productServerRunner.getDate());
                        log.debug("Returning progress page.");
                        return mapping.findForward("progress");
                    }
                }                
            }
        } else {
            log.debug("Starting request by creating the ProductServerRunner.");
            // Create a new runner and start the thread to do the job.
            productServerRunner = new ProductServerRunner(productRequest,lasConfig, serverConfig, request, mapping, cache);
            if ( JSESSIONID != null && !JSESSIONID.equals("")) {
                productServerRunner.setJESSIONID(JSESSIONID);
            }
            productServerRunner.start();
            if ( sessions == null && JSESSIONID != null && !JSESSIONID.equals("")) {
                sessions = new HashSet<String>();
                sessions.add(JSESSIONID);
                servlet.getServletContext().setAttribute("sessions_"+productRequest.getCacheKey(), sessions);
                request.setAttribute("JSESSIONID", JSESSIONID);
            }
            servlet.getServletContext().setAttribute("runner_"+productRequest.getCacheKey(), productServerRunner);
            synchronized(productServerRunner) {
                try {
                    long timeout = productServerRunner.getProgressTimeout()*1000;
                    log.debug("Starting ProductServerRunner thread with timeout="+timeout);
                    if ( timeout > 0 && JSESSIONID != null && !JSESSIONID.equals("") ) {
                        // Timeout set in the request.
                        // Wait until timeout elapses then send progress page.
                        productServerRunner.join(timeout);
                    } else {
                        // No time out set or no session id.  Wait forever.
                    	log.debug("Starting thread with no progress timeout limit.");
                        productServerRunner.join();
                    }   
                } catch (Exception e) {
                    logerror(request, "Could not join the newly created action runner thread", e);
                    removeOnError(productRequest);
                    return mapping.findForward("error");
                }
                
                if (productServerRunner.stillWorking() && productServerRunner.isAlive()) {
                    // We're not done yet.
                    // setup the progress page
                    productServerRunner.setBatch(true);
                    log.debug("Timeout reached.");
                    
                    
                    request.setAttribute("server_url", serverURL);
                    request.setAttribute("status", productServerRunner.getStatus());
                    request.setAttribute("cache", productRequest.getUseCache());
                    request.setAttribute("operations", productRequest.getOperationNames());
                    request.setAttribute("feed", productServerRunner.getCurrentBackendRequest().getResult("rss"));
                    request.setAttribute("seconds", productServerRunner.getSeconds());
                    request.setAttribute("date", productServerRunner.getDate());
                    log.debug("Returning progress page.");
                    return mapping.findForward("progress");
                    
                }
            }
        }
        
        ActionForward errorAction = productServerRunner.getErrorAction();
        
        // An error occurred.  Forward to the error action.
        if ( errorAction != null ) {
            servlet.getServletContext().removeAttribute("sessions_"+productRequest.getCacheKey());
            servlet.getServletContext().removeAttribute("runner_"+productRequest.getCacheKey());
            logerror(request);
            return errorAction;
        }
        
        log.debug("Request processed.  Preparing to return product.");
        
        LASBackendResponse compoundResponse = productServerRunner.getCompoundResponse();
        
        // It finished.  Return the product.
        // Prepare the DataTable object
        log.debug("Preparing the annotations file.");
        // Create a LASAnnotations object.
        DataTable lasDataTable = null;
        // Only handle local data table files
        String datatable_filename = compoundResponse.getResultAsFileByType("DataTable");
        if ( !datatable_filename.equals("")) {
            File file = new File(datatable_filename);
            try {
                lasDataTable = new DataTable(file);
                // Put these objects in the context so the output template can use them.
                request.setAttribute("las_datatable", lasDataTable);
            } catch (IOException e) {
                file.delete();
                removeOnError(productRequest);
                logerror(request, "Error parsing the DataTable file.", e);
            } 
        }

        // End of DataTable
        log.debug("Preparing the annotations file.");
        // Create a LASAnnotations object.
        LASAnnotations lasAnnotations = new LASAnnotations();
        if ( compoundResponse.isResultByTypeRemote("annotations")) {
            String annotations_url = compoundResponse.getResult("annotations");
            StringBuilder xml = new StringBuilder();
            if ( annotations_url != null && !annotations_url.equals("") ) {
                
                try {
                    URL annotations = new URL(annotations_url);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    annotations.openStream()));

                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                    	inputLine = inputLine.trim();
                    	xml.append(inputLine);
                    	
                    }

                    in.close();
        			JDOMUtils.XML2JDOM(xml.toString(), lasAnnotations);

                } catch (MalformedURLException e) {
                    logerror(request, "Error parsing the annotations file.", e);
                    removeOnError(productRequest);
                    return mapping.findForward("error");
                } catch (IOException e) {
                    logerror(request, "Error parsing the annotations file.", e);
                    removeOnError(productRequest);
                    return mapping.findForward("error");
                } catch (JDOMException e) {
                	logerror(request, "Error parsing the annotations file.", e);
                	removeOnError(productRequest);
                    return mapping.findForward("error");
				}
                
                // Put these objects in the context so the output template can use them.
                request.setAttribute("las_annotations", lasAnnotations);
                
            }
        } else {
        	String annotations_filename = compoundResponse.getResultAsFileByType("annotations");
        	if ( !annotations_filename.equals("")) {
                File file = new File(annotations_filename);
        		try {
        			JDOMUtils.XML2JDOM(file, lasAnnotations);
        			// Put these objects in the context so the output template can use them.
                    request.setAttribute("las_annotations", lasAnnotations);
                    request.setAttribute("annotations_URL", "getAnnotations.do?file="+annotations_filename.substring(annotations_filename.lastIndexOf("/")+1, annotations_filename.length()));
        		} catch (IOException e) {
        		    file.delete();
        		    removeOnError(productRequest);
        			logerror(request, "Error parsing the annotations file.", e);
        		} catch (JDOMException e) {
        		    file.delete();
        		    removeOnError(productRequest);
        			logerror(request, "Error parsing the annotations file.", e);
				}
        		
        	}
        }
        
        log.debug("Preparing the map scale file.");
        // Create a lasMapScale object.
        LASMapScale lasMapScale = new LASMapScale();
        if ( compoundResponse.isResultByTypeRemote("map_scale")) {
            String map_scale_url = compoundResponse.getResult("map_scale");
            if ( map_scale_url != null && !map_scale_url.equals("") ) {
                StringBuffer map_buffer = new StringBuffer();
                try {
                    URL map_scale = new URL(map_scale_url);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    map_scale.openStream()));

                    String inputLine;

                    while ((inputLine = in.readLine()) != null)
                        map_buffer.append(inputLine);

                    in.close();
                } catch (MalformedURLException e) {
                    logerror(request, "Error parsing the map scale file.", e);
                    removeOnError(productRequest);
                    return mapping.findForward("error");
                } catch (IOException e) {
                    logerror(request, "Error parsing the map scale file.", e);
                    removeOnError(productRequest);
                    return mapping.findForward("error");
                }
                try {
                    JDOMUtils.XML2JDOM(map_buffer.toString(), lasMapScale);
                } catch (Exception e) {
                    logerror(request, "Error parsing the map scale file.", e);
                    removeOnError(productRequest);
                    return mapping.findForward("error");
                }
                // Put these objects in the context so the output template can use them.
                request.setAttribute("las_map_scale", lasMapScale);
            }
        } else {
            String map_scale_filename = compoundResponse.getResultAsFileByType("map_scale");
            if ( !map_scale_filename.equals("")) {
                File map_scale_file = new File(map_scale_filename);
                try {
                    JDOMUtils.XML2JDOM(map_scale_file, lasMapScale);
                } catch (Exception e) {
                    logerror(request, "Error parsing the map scale file.", e);
                    removeOnError(productRequest);
                    map_scale_file.delete();
                    return mapping.findForward("error");
                }
                // Put these objects in the context so the output template can use them.
                request.setAttribute("las_map_scale", lasMapScale);
                compoundResponse.addMapScale(lasMapScale);
            }
        }
       
        log.debug("Preparing the range index file.");
        LASRegionIndex lasRegionIndex = new LASRegionIndex();
        if ( compoundResponse.isResultByTypeRemote("index")) {
            String index_url = compoundResponse.getResult("index");
            if (index_url != null && !index_url.equals("") ) {
                StringBuffer index_buffer = new StringBuffer();
                try {
                    URL index = new URL(index_url);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(index.openStream()));

                    String inputLine;

                    while ((inputLine = in.readLine()) != null)
                        index_buffer.append(inputLine);

                    in.close();
                } catch (MalformedURLException e) {
                    logerror(request, "Error parsing the region index file.", e);
                    removeOnError(productRequest);
                    return mapping.findForward("error");
                } catch (IOException e) {
                    logerror(request, "Error parsing the region file.", e);
                    removeOnError(productRequest);
                    return mapping.findForward("error");
                }
                try {
                    JDOMUtils.XML2JDOM(index_buffer.toString(), lasRegionIndex);
                } catch (Exception e) {
                    logerror(request, "Error parsing the map scale file.", e);
                    removeOnError(productRequest);
                    return mapping.findForward("error");
                }
            }            

        } else {
            String region_index_filename = compoundResponse.getResultAsFileByType("index");
            if ( !region_index_filename.equals("")) {
                // Create a lasRegionIndex object.
                
                File region_index_file = new File(region_index_filename);
                try {
                    JDOMUtils.XML2JDOM(region_index_file, lasRegionIndex);
                } catch (Exception e) {
                    logerror(request, "Error parsing the region index file.", e);
                    removeOnError(productRequest);
                    return mapping.findForward("error");
                }

                // Put these objects in the context so the output template can use them.
                request.setAttribute("las_region_index", lasRegionIndex);
            }
        }
        if (compoundResponse.isResultByTypeRemote("webrowset") || !compoundResponse.getResultAsFileByType("webrowset").equals("")) {
          	// Put the webrowset result into the output context if one exists.

        	String db_type;
			try {
				LASBackendRequest db_request = productRequest.getRequestByService("database");
				db_type = "mysql";
				if ( db_request != null ) {
					db_type = db_request.getDatabaseProperty("db_type");
				}
			} catch (JDOMException e) {
				logerror(request, "Unable to create webrowset.  Can't find db_type: ", e);
				removeOnError(productRequest);
        		return mapping.findForward("error");
			} catch (LASException e) {
				logerror(request, "Unable to create webrowset.  Can't find db_type: ", e);
				removeOnError(productRequest);
        		return mapping.findForward("error");
			}           
        	WebRowSet webrowset;
        	try {
        		if ( db_type.contains("oracle") ) {
        			webrowset = new OracleWebRowSet();
        			System.setProperty("javax.xml.parsers.SAXParserFactory","com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        		} else {
        			webrowset = new WebRowSetImpl();
        		}
        	} catch (Exception e) {
        		logerror(request, "Unable to create webrowset: ", e);
        		removeOnError(productRequest);
        		return mapping.findForward("error");
        	}
        	if ( compoundResponse.isResultByTypeRemote("webrowset")) {


        		String rowset_url = compoundResponse.getResult("webrowset");
        		if (rowset_url != null && !rowset_url.equals("") ) {
        			try {
        				URL index = new URL(rowset_url);
        				BufferedReader in = new BufferedReader(
        						new InputStreamReader(index.openStream()));
        				webrowset.readXml(in);
        			} catch (MalformedURLException e) {
        				logerror(request, "Error parsing the webrowset file.", e);
        				removeOnError(productRequest);
        				return mapping.findForward("error");
        			} catch (IOException e) {
        				logerror(request, "Error parsing the webrowset file.", e);
        				removeOnError(productRequest);
        				return mapping.findForward("error");
        			} catch (SQLException e) {
        				logerror(request, "Error parsing the webrowset file.", e);
        				removeOnError(productRequest);
        				return mapping.findForward("error");
        			}
        		}            

        	} else {
        		String webrowsetFileName = compoundResponse.getResultAsFileByType("webrowset");
        		if ( !webrowsetFileName.equals("")) {

        			try {
        				FileReader in = new FileReader(new File(webrowsetFileName));
        				webrowset.readXml(in);
        			} catch (FileNotFoundException e) {
        				logerror(request, "Error parsing the webrowset file.", e);
        				removeOnError(productRequest);
        				return mapping.findForward("error");
        			} catch (SQLException e) {
        				logerror(request, "Error parsing the webrowset file.", e);
        				removeOnError(productRequest);
        				return mapping.findForward("error");
        			}
        			// Put these objects in the context so the output template can use them.
        			request.setAttribute("las_webrowset", webrowset);
        		}
        	}
        }
        // Remove the runner from the servlet context.
        servlet.getServletContext().removeAttribute("sessions_"+productRequest.getCacheKey());
        servlet.getServletContext().removeAttribute("runner_"+productRequest.getCacheKey());
        
        // Log the access to the product server.
        if ( query != null ) {
			log.info("END:   "+request.getRequestURL()+"?"+query);						
		} else {
			log.info("END:   "+request.getRequestURL());
		}
        if ( stream ) {
            
        	if ( stream_ids == null ) {
        		logerror(request, "No stream_ID parameter values found.", "Set stream_ID=resultID for the result you want streamed.");
        		return mapping.findForward("error");
        	}
        	if ( stream_ids.length > 1 ) {
        		logerror(request, "Streaming for more than one result has not yet been implemented.", "Stay tuned.");
        		return mapping.findForward("error");
        	}
            log.debug("Streaming output requested for "+stream_ids[0]);
            String mimeType = compoundResponse.getStreamedMimeType(stream_ids[0]);
            if ( mimeType == null || mimeType.equals("")) {
                logerror(request, "Cannot stream result.", "Cannot find MIME type for streamed result.");
                return mapping.findForward("error");
            }
            response.setContentType(mimeType);
            log.debug("Steaming output MIME type set to: "+mimeType);
            if ( compoundResponse.isStreamable(stream_ids[0])) {
                String fileToStream = null;
                String urlToStream = null;
                boolean remote = compoundResponse.isResultRemote(stream_ids[0]);
                if ( remote ) {
                    urlToStream = compoundResponse.getResult(stream_ids[0]);
                } else {
                    fileToStream = compoundResponse.getResultAsFile(stream_ids[0]);
                }
                try {
                    
                    if (mimeType.contains("image")) {
                    	response.setHeader("Content-disposition", "inline;filename=\"las_product.png\"");
                        ServletOutputStream sos = response.getOutputStream();
                        
                        if ( remote ) {
                            if (!ImageIO.write(ImageIO.read(new URL(urlToStream)),"png",sos)) {
                                logerror(request, "Cannot stream result.", "Cannot find writer for image.");
                                return mapping.findForward("error");
                            }
                        } else {
                            // There is no writer for "gif" images.  Always write as PNG.
                            // Check that the writer is found since we got burned by this once.
                            if (!ImageIO.write(ImageIO.read(new File(fileToStream)),"png",sos)) {
                                logerror(request, "Cannot stream result.", "Cannot find writer for image.");
                                return mapping.findForward("error");
                            }
                        }
                        sos.flush();
                    } else if (mimeType.contains("text") || mimeType.contains("postscript") || mimeType.contains("kml")) {
                    	if ( mimeType.contains("postscript")) {
                    	    response.setHeader("Content-disposition", "inline;filename=\"las_product.ps\"");
                    	} else if (mimeType.contains("kml")) {
                    		response.setHeader("Content-disposition", "inline;filename=\"las_product.kml\"");
                    	} else {
                    		response.setHeader("Content-disposition", "inline;filename=\"las_product.txt\"");
                    	}
                        // Get the response writer.
                        PrintWriter writer = response.getWriter();
                        // Read the output text file.
                        BufferedReader textReader = null;
                        try {
                            FileReader f = new FileReader(fileToStream);
                            textReader = new BufferedReader(f);
                        } catch (FileNotFoundException e) {
                            logerror(request, "Cannot stream result.", "Cannot find text file to stream.");
                            return mapping.findForward("error");
                        }
                        if (textReader != null) {
                            try {
                                String line = textReader.readLine();
                                while ( line != null ) {
                                    writer.println(line);
                                    line = textReader.readLine();
                                }
                                writer.flush();
                            } catch (IOException e) {
                                logerror(request, "Cannot stream result.", "Cannot stream text output file.");
                                return mapping.findForward("error");
                            }
                        }
                        
                        //Use writer to render text
                    }
                } catch (IOException e) {
                    logerror(request,"IO Error attempting to read image file for stream.", e);
                }
            } else {
                logerror(request, "Streamed result not found.", "Cannot find a streamable result with ID = "+stream_ids[0]);
                return mapping.findForward("error");
            }            
            return null;
        } else {            
            // Send control to the template specified in the LASRequest.     	
            request.setAttribute("las_response", compoundResponse);
            String output_template = productRequest.getTemplate();
            
            if ( output_template == null || output_template.equals("") ) {
                logerror(request, "No output template defined for this operation.", "Add output template to the operation XML configuraiton file.");
                return mapping.findForward("error");
            }
            
            String mimeType = productRequest.getTemplateMimeType();
            
            if ( mimeType != null && !mimeType.equals("") ) {
                request.setAttribute("las_mime_type", mimeType);
            }
            return new ActionForward("/productserver/templates/"+productRequest.getTemplate()+".vm");
        }
        
    }
    private void removeOnError(ProductRequest productRequest) {
        servlet.getServletContext().removeAttribute("sessions_"+productRequest.getCacheKey());
        servlet.getServletContext().removeAttribute("runner_"+productRequest.getCacheKey());
    }
    private boolean testFTDS(LASConfig lasConfig) {
		int max = 10;
		List<String> test = lasConfig.getFTDSTestURLs(max);
		for (int i = 0; i < test.size(); i++ ) {
			String url = test.get(i);
			NetcdfDataset ncds = null;
			try {
				String dodsurl = DODSNetcdfFile.canonicalURL(url);
				ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(dodsurl);
				StringBuilder error = new StringBuilder();
				GridDataset gridDs = (GridDataset) TypedDatasetFactory.open(FeatureType.GRID, ncds, null, error);
				servlet.getServletContext().setAttribute(LASConfigPlugIn.LAS_FTDS_UP_KEY, "true");
				return true;
			} catch (IOException e) {
				log.error("IO error testing FTDS URL = " + e);
			} finally {
				if ( ncds != null ) {
					try {
						ncds.close();
					} catch (IOException e) {
						log.error("IO error testing FTDS URL = " + e);
					}
				}
			}
		}
		servlet.getServletContext().setAttribute(LASConfigPlugIn.LAS_FTDS_UP_KEY, "false");
		return false;
	}
}
