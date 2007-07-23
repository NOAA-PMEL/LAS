package org.iges.anagram.service;

import java.io.*;
import java.util.Date;
import javax.servlet.http.*;

import dods.dap.Server.ServerDDS;

import org.iges.anagram.*;

/** Provides the DODS Data Descriptor Structure for a data object */
public class DDSService 
    extends Service {

    public String getServiceName() {
	return "dds";
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
	response.setHeader("Content-Description", "dods_dds");
	response.setDateHeader("Last-Modified", data.getCreateTime());
	
	try {
	    server.getTool().writeDDS(data, 
			  clientRequest.getCE(),
                          clientRequest.getPrivilege(),
			  response.getOutputStream(),
                          clientRequest.useCache());
	} catch (IOException ioe) {}
    }
	
}

