package gov.noaa.pmel.tmap.las.filter;

import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.product.server.LASAction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class RequestInputFilter implements Filter {
    private static Logger log = LogManager.getLogger(RequestInputFilter.class.getName());
    // All LAS query parameters.
    private final static String[] p = {
    	"BBOX", 
    	"cancel", 
    	"catid", 
    	"catitem", 
    	"debug",
    	"dsid",
    	"email",
    	"format",
    	"JSESSIONID",
		"log_level",
		"opendap",
		"opid",
		"plot",
		"reinit", 
		"REQUEST", 
		"stream",
		"stream_ID", 
		"varid",
		"view",
		"xml"
    };
    private final static Set<String> LAS_PARAMETERS = new HashSet<String>(Arrays.asList(p));
	
    // Parameters that should be either "true" or "false"
    private final static String[] bp = {
    	"cancel",
    	"debug",
    	"stream"
    };
    private final static Set<String> LAS_BOOLEAN_PARAMETERS = new HashSet<String>(Arrays.asList(bp));
    
    // Every parameter that accepts an LAS ID
    private static final String[] lp = {
    	"catid", 
    	"catitem", 
    	"dsid",
    	"JSESSIONID",
		"opid",
		"plot",
		"stream_ID", 
		"varid"
    };
	
    private final static Set<String> LAS_ID_PARAMETERS = new HashSet<String>(Arrays.asList(lp));
    private final static Pattern ID_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");
    // Match 4 floating point numbers separated by a comma
    private final static Pattern BBOX_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+,[-+]?[0-9]*\\.?[0-9]+,[-+]?[0-9]*\\.?[0-9]+,[-+]?[0-9]*\\.?[0-9]+");
    
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		 if ( ! ( servletRequest instanceof HttpServletRequest ) )
		    {
		      log.error( "doFilter(): Not an HTTP request! How did this filter get here?" );
		      filterChain.doFilter( servletRequest, servletResponse );
		      return;
		    }
		    HttpServletRequest request = (HttpServletRequest) servletRequest;
		    HttpServletResponse response = (HttpServletResponse) servletResponse;
            if ( ! validateParameters(request) ) {
            	LASAction.logerror(request, "Illegal request parameter.", "Request contains a parameter that is not allowed.");
            	response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal query parameter.");
            	return;
            }
            if ( ! validBooleanValues(request) ) {
            	LASAction.logerror(request, "Illegal request parameter value.", "Request contains a parameter value that is not allowed.");
            	response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal boolean query parameter value.");
            	return;
            }
            if ( ! validIds(request) ) {
            	LASAction.logerror(request, "Illegal request parameter value.", "Request contains a parameter value that is not allowed.");
            	response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal LAS ID query parameter value.");
            	return;
            }
            if ( !validBBOX(request) ) {
            	LASAction.logerror(request, "Illegal request parameter value.", "Request contains a parameter value that is not allowed.");
            	response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal BBOX query parameter value.");
            	return;
            }
            if ( !validFormat(request) ) {
            	LASAction.logerror(request, "Illegal request parameter value.", "Request contains a parameter value that is not allowed.");
            	response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal format query parameter value.");
            	return;
            }
            if ( !validWMSRequest(request) ) {
            	LASAction.logerror(request, "Illegal request parameter value.", "Request contains a parameter value that is not allowed.");
            	response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal WMS query parameter value.");
            	return;
            }
            if ( !validView(request) ) {
            	LASAction.logerror(request, "Illegal request parameter value.", "Request contains a parameter value that is not allowed.");
            	response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal view query parameter value.");
            	return;
            }
            if ( !validReinit(request) ) {
            	LASAction.logerror(request, "Illegal request parameter value.", "Request contains a parameter value that is not allowed.");
            	response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal reinit query parameter value.");
            	return;
            }
            if ( !validLogLevel(request) ) {
            	LASAction.logerror(request, "Illegal request parameter value.", "Request contains a parameter value that is not allowed.");
            	response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal log_level query parameter value.");
            	return;
            }
            String requestXML = request.getParameter("xml");
            if ( (requestXML != null && !requestXML.equals("")) ) {
            	try {
            		String temp = URLDecoder.decode(requestXML, "UTF-8");
            		requestXML = temp;
            	} catch (UnsupportedEncodingException e) {
            		LASAction.logerror(request, "Error decoding the XML request query string.", e);
            		response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal xml query parameter value.");
            		return;
            	}

            	// Create a lasRequest object.
            	LASUIRequest lasRequest = new LASUIRequest();
            	try {
            		JDOMUtils.XML2JDOM(requestXML, lasRequest);
            		// Set the lasRequest object in the HttpServletRequest so the product server does not have to rebuild it.
            		request.setAttribute("las_request", lasRequest);
            	} catch (Exception e) {
            		LASAction.logerror(request, "Error parsing the request XML. ", e);
            		response.sendError(HttpServletResponse.SC_NOT_FOUND, "Request contains an illegal xml query parameter value.");
            		return;
            	}
            }
		    filterChain.doFilter( servletRequest, servletResponse );
		    return;
	}
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}
	private boolean validLogLevel(HttpServletRequest request) {
		String value[] = request.getParameterValues("log_level");
		if ( value == null || value.length == 0 ) {
			return true;
		}
		if ( value.length > 1 ) return false;
		
		if ( !value[0].equals("debug") && 
			 !value[0].equals("info") && 
			 !value[0].equals("warn") && 
			 !value[0].equals("error") && 
			 !value[0].equals("fatal")
			 ) return false;
		return true;
	}
	private boolean validReinit(HttpServletRequest request ) {
		String value[] = request.getParameterValues("reinit");
		if ( value == null || value.length == 0 ) {
			return true;
		}
		if ( value.length > 1 ) return false;
		if ( !value[0].equals("wait") && !value[0].equals("force") ) return false;
		return true;
	}
	private boolean validView(HttpServletRequest request) {
		String value[] = request.getParameterValues("view");
		if ( value == null || value.length == 0 ) {
			return true;
		}
		if ( value.length > 1 ) return false;
		
		if ( !value[0].equals("x") && 
			 !value[0].equals("y") && 
			 !value[0].equals("z") && 
			 !value[0].equals("t") && 
			 !value[0].equals("xy") && 
			 !value[0].equals("xz") && 
			 !value[0].equals("xt") && 
			 !value[0].equals("yx") && 
			 !value[0].equals("yz") && 
			 !value[0].equals("yt") && 
			 !value[0].equals("tx") && 
			 !value[0].equals("ty") && 
			 !value[0].equals("tz") && 
			 !value[0].equals("xyz") && 
			 !value[0].equals("xyt") && 
			 !value[0].equals("xzt") && 
			 !value[0].equals("yzt") && 
			 !value[0].equals("xyzt")
			 ) return false;
		return true;
	}
	private boolean validWMSRequest(HttpServletRequest request) {
		String value[] = request.getParameterValues("REQUEST");
		if ( value == null || value.length == 0 ) {
			return true;
		}
		if ( value.length > 1 ) return false;
		if ( !value[0].equals("GETMAP") && !value[0].equals("GETCAPABILITES") && !value[0].equals("GETFEATUREINFO") ) return false;
		return true;
	}
	private boolean validFormat(HttpServletRequest request) {
		String value[] = request.getParameterValues("format");
		if ( value == null || value.length == 0 ) {
			return true;
		}
		if ( value.length > 1 ) return false;
		if ( !value[0].equals("json") || !value[0].equals("xml") ) return false;
		return true;
	}
	private boolean validBBOX(HttpServletRequest request) {
		String value[] = request.getParameterValues("BBOX");
		if ( value == null || value.length == 0 ) {
			return true;
		}
		if ( value.length > 1 ) return false; // only allow 1
		Matcher m = BBOX_PATTERN.matcher(value[0]);
		return m.matches();
	}
	// email address    "^[A-Za-z0-9._-]+@[[A-Za-z0-9.-]+$"
	// LAS ID "^[A-Za-z0-9._-]"
    private boolean validateParameters(HttpServletRequest request) {
    	Set<String> parameters = request.getParameterMap().keySet();
    	return LAS_PARAMETERS.containsAll(parameters);
    }
    private boolean validBooleanValues(HttpServletRequest request) {
    	boolean valid = true;
    	for (Iterator keyIt = LAS_BOOLEAN_PARAMETERS.iterator(); keyIt.hasNext();) {
			String key = (String) keyIt.next();
			String[] value = request.getParameterValues(key);
			valid = valid && validateBooleanValue(value);			
		}
    	return valid;
    }
    private boolean validateBooleanValue(String[] value) {

		if ( value == null || value.length == 0 ) return true;
		
		if ( value.length > 1 ) return false;
		
		if ( !value[0].equals("true") && !value[0].equals("false") ) return false;
		
		return true;
    }

	private boolean validIds(HttpServletRequest request) {
		boolean valid = true;
    	for (Iterator keyIt = LAS_ID_PARAMETERS.iterator(); keyIt.hasNext();) {
			String key = (String) keyIt.next();
			String[] value = request.getParameterValues(key);
			valid = valid && validateIdValue(value);			
		}
    	return valid;
	}

	private boolean validateIdValue(String[] value) {
		if ( value == null || value.length == 0 ) return true;
		// More than one is allowed so don't test the length
		Matcher m = ID_PATTERN.matcher(value[0]);
		return m.matches();
	}
}
