package org.iges.anagram.service;

import java.io.*;
import java.util.Date;
import javax.servlet.http.*;
import org.iges.anagram.*;

/** Provides the DODS Data Attribute Structure for a data object */
public class DASService 
    extends Service {

    public String getServiceName() {
	return "das";
    }

    public void configure(Setting setting) {
    }

    public void handle(ClientRequest clientRequest)
	throws ModuleException {

	HttpServletRequest request = clientRequest.getHttpRequest();
	HttpServletResponse response = clientRequest.getHttpResponse();
	
	DataHandle data = getDataFromPath(clientRequest);
	
	response.setContentType("text/plain");
	response.setHeader("XDODS-Server",  
			   "3.1");
	response.setHeader("Content-Description", "dods_das");
	response.setDateHeader("Last-Modified", data.getCreateTime());
	
	try {
	    server.getTool().writeDAS(data,
                                      clientRequest.getPrivilege(),
				      response.getOutputStream(),
                                      clientRequest.useCache());
	} catch (IOException ioe) {}
    }
	
}

