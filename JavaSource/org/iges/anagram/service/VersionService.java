package org.iges.anagram.service;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import org.iges.anagram.*;

/** Provides HTML listings of the server's contents for a given directory
 *  path. 
 */
public class VersionService
    extends Service {

    public String getServiceName() {
	return "ver";
    }

    public void configure(Setting setting) {
    }

    public void handle(ClientRequest clientRequest)
	throws ModuleException {
	
	long updateTime = server.getLastConfigTime();

	clientRequest.getHttpResponse().setContentType("text/plain");
	clientRequest.getHttpResponse().setHeader("CacheControl", "no-cache");
	clientRequest.getHttpResponse().setHeader("XDODS-Server",  
			                                "3.1");
	clientRequest.getHttpResponse().setDateHeader("Last-Modified", 
						      updateTime);
	
	PrintStream page = startHTML(clientRequest);
	if (page == null) {
	    return;
	}
        page.println("Core version:DODS Java/1.1.5");
        page.println("Server version:Ferret Data Server/1.0");
	
	page.flush();
	page.close();
    }

}
