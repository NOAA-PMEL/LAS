package org.iges.anagram.service;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.iges.util.*;
import org.iges.anagram.*;

/** Provides administrative functions, protected by a simple authorization
 *  mechanism. */
public class AdminService 
    extends Service {

    public String getServiceName() {
	return "admin";
    }

    public void configure(Setting setting) {
	authCode = setting.getAttribute("auth", "");
	if (authCode.equals("")) {
	    if (verbose()) verbose("no authorization code; " + 
				   "admin service will be disabled");
	}
	timeout = setting.getNumAttribute("timeout", 60);
	if (verbose()) verbose("admin timeout set to " + timeout + " sec");
    }

    public void handle(ClientRequest clientRequest)
	throws ModuleException {
	
	HttpServletRequest request = clientRequest.getHttpRequest();
	HttpServletResponse response = clientRequest.getHttpResponse();

	

	Privilege privilege = clientRequest.getPrivilege();
	if ( ! request.getRemoteAddr().equals("127.0.0.1") && 
	     ! privilege.getAttribute("admin_allowed", "false").equals("true")) {
	    if (debug()) debug("IP address " + request.getRemoteAddr() + 
			       " is not local, and is not authorized");
	    throw new ModuleException(this, "floo authorization failed");
	}
	
	Hashtable queryParams;
	if (request.getMethod().equals("POST")) {
	    try {
		queryParams = HttpUtils.parsePostData
		    (request.getIntHeader("Content-Length"),
		     request.getInputStream());
	    } catch (IOException ioe) {
		queryParams = new Hashtable();
	    }
	} else {
	    if (request.getQueryString() != null) {
		queryParams = 
		    HttpUtils.parseQueryString(request.getQueryString());
	    } else {
		queryParams = new Hashtable();
	    }
	}
	if (authCode.equals("")) {
	    throw new ModuleException(this, "service not available");
	}
	    
	if (debug()) debug("parsed parameters for admin cmd"); 

	String[] userAuth = (String[])queryParams.get("auth");
	if (userAuth == null) {
	    if (debug()) debug("no userAuth given");
	    throw new ModuleException(this, "authorization failed");
	}

	if (!userAuth[0].equals(authCode)) {
	    if (debug()) debug("userAuth is " + userAuth[0]);
	    throw new ModuleException(this, "authorization failed");
	}

	String[] command = (String[])queryParams.get("cmd");
	if (command == null) {
	    throw new ModuleException(this, "no command given");
	} else if (command[0].equals("reload")) {
	    if (debug()) debug("attempting to get exclusive lock"); 
	    try {
		synchronized (server.getSynch()) {
		    if (debug()) debug("released non-exclusive lock"); 
		    if (!server.getSynch().tryLockExclusive(timeout * 1000)) {
			throw new ModuleException
			    (this, "timed out after " + timeout + 
			     " seconds; server is currently in use");
		    }
		    if (debug()) debug("got an exclusive lock"); 
		}
		info("reconfiguring server");
		server.reconfigure();
	    } catch (ModuleException me) {
		throw me;
	    } finally {
		synchronized (server.getSynch()) {
		    server.getSynch().releaseExclusive();
		    if (debug()) debug("released exclusive lock"); 
		}
	    }
	} else if (command[0].equals("clear")) {
	    info("removing all temporary catalog entries");
            try {
	       server.getCatalog().clearTemp();
	    } catch (AnagramException ae) {}
	} else {
	    throw new ModuleException(this, "unknown command " + command[0]);
	}
	
	printReply(clientRequest, command[0]);
	if (debug()) debug("sent admin cmd result to client"); 

    }

    protected void printReply(ClientRequest request, String command) {
	request.getHttpResponse().setHeader("Cache-Control", "no-cache");
	String baseURL = getBaseURL(request);
	PrintStream page = startHTML(request);
	if (page == null) { 
	    return;
	}
	printHeader(page, "admin", "admin", null, baseURL);
	page.print("Command:<p>\n");
	page.print("<b>");
	page.print(command);
	page.print("</b><p>\n");
	page.print("performed successfully.");
	printFooter(page, null, 0, baseURL);
	
    }


    protected String authCode;
    protected long timeout;

}
