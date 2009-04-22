/**
 * 
 */
package gov.noaa.pmel.tmap.las.product.server;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASMapScale;
import gov.noaa.pmel.tmap.las.jdom.LASRegionIndex;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.jdom.ServerConfig;
import gov.noaa.pmel.tmap.las.product.request.ProductRequest;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.WebRowSet;

import oracle.jdbc.rowset.OracleWebRowSet;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.JDOMException;

import com.sun.rowset.WebRowSetImpl;

/**
 * @author Roland Schweitzer
 *
 */
public final class ProductServerAction extends LASAction {
    private static Logger log = LogManager.getLogger(ProductServerAction.class.getName());
    
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response){

        ProgressForm progress = (ProgressForm) form;
        
        log.debug("Entering ProductServerAction");

        // Get the LASConfig (sub-class of JDOM Document) from the servlet context.
        
        LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
        // Same for the ServerConfig
        ServerConfig serverConfig = (ServerConfig)servlet.getServletContext().getAttribute(ServerConfigPlugIn.SERVER_CONFIG_KEY);
        // Get the global cache object.
        Cache cache = (Cache) servlet.getServletContext().getAttribute(ServerConfigPlugIn.CACHE_KEY);
        
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
        String lock = (String) servlet.getServletContext().getAttribute("lock");
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
            cancel = cancelParam.equals("Cancel");
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
        
        try {
            String temp = URLDecoder.decode(requestXML, "UTF-8");
            requestXML = temp;
        } catch (UnsupportedEncodingException e) {
            logerror(request, "Error decoding the XML request query string.", e);
            return mapping.findForward("error");
        }
        
        // Create a lasRequest object.
        LASUIRequest lasRequest = new LASUIRequest();
        try {
            JDOMUtils.XML2JDOM(requestXML, lasRequest);
            // Always set the request XML JDOM object in the request servlet context
            // so it can be used in any output template including the error template.
            request.setAttribute("las_request", lasRequest);
            request.setAttribute("server_base_url", serverBaseURL);

        } catch (Exception e) {
            logerror(request, "Error parsing the request XML. ", e);
            return mapping.findForward("error");
        }

        // Get the debug level from the query or request property.
        
        String debug = request.getParameter("debug");
        Logger ancestor = Logger.getLogger("gov.noaa.pmel.tmap");
        
        if ( debug == null ) {
            debug = lasConfig.getGlobalPropertyValue("las", "debug");
        }

        if ( debug == null || debug == "" ) {
           if (  lasRequest.getProperty("las", "debug") != null &&
                !lasRequest.getProperty("las", "debug").equals("") ) {
              debug = lasRequest.getProperty("las", "debug");
           } 
        }
 
        String log_level = request.getParameter("log_level");

        if ( log_level != null ) {
        
            if ( log_level.equalsIgnoreCase("debug") ) {
                ancestor.setLevel(Level.DEBUG);
            } else if ( log_level.equalsIgnoreCase("info") ) {
                ancestor.setLevel(Level.INFO);
            } else if ( log_level.equalsIgnoreCase("warn") ) {
                ancestor.setLevel(Level.WARN);
            } else if ( log_level.equalsIgnoreCase("error") ) {
                ancestor.setLevel(Level.ERROR);
            } else if ( log_level.equalsIgnoreCase("fatal") ) {
                ancestor.setLevel(Level.FATAL);
            } else {
                ancestor.setLevel(Level.INFO);
            }

        }

        if ( debug.equalsIgnoreCase("true") ) debug = "debug";
        request.setAttribute("debug", debug);
        
        // Report logging level only for "debug" and "trace" levels.
        log.debug("Logging set to " + log.getEffectiveLevel().toString() + " for "+log.getName());
        //debug
        
        log.debug("Creating ProductRequest object.");
        // Create a new ProductRequest object
        ProductRequest productRequest;
        try {
            productRequest = new ProductRequest(lasConfig, lasRequest, debug, JSESSIONID);
        } catch ( LASException e ) {
            logerror(request, "Error creating the product request.", e);
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
                            return mapping.findForward("error");
                        }
                    
                    log.debug("Job canceled.  Send cancel request to backend service.");
                    ProductWebService productWebService;
                    try {
                        productWebService = new ProductWebService(backendRequestDocument, 
                                backendServerURL, 
                                methodName, 
                                productCacheKey);
                    } catch (LASException e) {
                        logerror(request, "Error building Web service request for "+ methodName , e);
                        return mapping.findForward("error");
                    } catch (IOException e) {
                        logerror(request, "Error building Web service request for "+ methodName , e);
                        return mapping.findForward("error");
                    }
                    String responseXML="";
                    LASBackendResponse lasResponse = new LASBackendResponse();
                    try {
                        productWebService.run();
                        responseXML = productWebService.getResponseXML();
                    } catch (Exception e) {
                        logerror(request, "Error was returned from the backend server.", e);
                        return mapping.findForward("error");
                    }
                    log.debug("Finished canceling request.");
                    
                    try {
                        JDOMUtils.XML2JDOM(responseXML, lasResponse);
                    } catch (Exception e) {
                        logerror(request, "Error parsing the XML returned from the backend server.", e);
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
                        
                        if ( timeout > 0 ) {
                            // Timeout set in the request.
                            long to = Math.min(timeout, 20000);
                            productServerRunner.join(to);
                        } else {
                            // No time out set.  Very unlikely, but possible if a new request wants the same
                            // product without an assoicated session.
                            productServerRunner.join();
                        }                        
                    } catch (Exception e) {
                        logerror(request, "Error joining the thread creating this product.", e);
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
                        productServerRunner.join();
                    }   
                } catch (Exception e) {
                    logerror(request, "Could not join the newly created action runner thread", e);
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
                    return mapping.findForward("error");
                } catch (IOException e) {
                    logerror(request, "Error parsing the map scale file.", e);
                    return mapping.findForward("error");
                }
                try {
                    JDOMUtils.XML2JDOM(map_buffer.toString(), lasMapScale);
                } catch (Exception e) {
                    logerror(request, "Error parsing the map scale file.", e);
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
                    return mapping.findForward("error");
                }
                // Put these objects in the context so the output template can use them.
                request.setAttribute("las_map_scale", lasMapScale);
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
                    return mapping.findForward("error");
                } catch (IOException e) {
                    logerror(request, "Error parsing the region file.", e);
                    return mapping.findForward("error");
                }
                try {
                    JDOMUtils.XML2JDOM(index_buffer.toString(), lasRegionIndex);
                } catch (Exception e) {
                    logerror(request, "Error parsing the map scale file.", e);
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
        		return mapping.findForward("error");
			} catch (LASException e) {
				logerror(request, "Unable to create webrowset.  Can't find db_type: ", e);
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
        				return mapping.findForward("error");
        			} catch (IOException e) {
        				logerror(request, "Error parsing the webrowset file.", e);
        				return mapping.findForward("error");
        			} catch (SQLException e) {
        				logerror(request, "Error parsing the webrowset file.", e);
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
        				return mapping.findForward("error");
        			} catch (SQLException e) {
        				logerror(request, "Error parsing the webrowset file.", e);
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
        log.info("Processed request: xml=\""+lasRequest.toCompactString()+"\"");
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
}
