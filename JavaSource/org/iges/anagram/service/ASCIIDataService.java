package org.iges.anagram.service;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import dods.dap.*;
import dods.dap.Server.*;

import org.iges.anagram.*;


/** Provides data subsets in ASCII comma-delimited format.
 * 
 * This service is part of the standard DODS services, 
 * however the format of its output is not specified.
 * For compatibility reasons, it is suggested that implementations of this
 * service match the output of the DODS netCDF server.
 *
 * Last modified: $Date: 2005/02/16 00:01:35 $ 
 * Revision for this file: $Revision: 1.4 $
 * Release name: $Name:  $
 * Original for this file: $Source: /home/ja9/tmap/FERRET_ROOT/fds/src/org/iges/anagram/service/ASCIIDataService.java,v $
 */
public class ASCIIDataService 
    extends Service {

    public String getServiceName() {
	return "ascii";
    }

    public void configure(Setting setting) {
    }

    /** Handles a request from the main dispatching servlet */
    public void handle(ClientRequest clientRequest)
	throws ModuleException {

	HttpServletRequest request = clientRequest.getHttpRequest();
	HttpServletResponse response = clientRequest.getHttpResponse();
/*
	if (clientRequest.getCE() == null) {
	    throw new ModuleException
		(this, "subset requests must include a constraint expression");
	}
*/
	try {
	    DataHandle data = getDataFromPath(clientRequest);

	    response.setContentType("text/plain");
	    response.setDateHeader("Last-Modified", data.getCreateTime());

	    server.getTool().writeASCIIData(data, 
					    clientRequest.getCE(), 
					    clientRequest.getPrivilege(),
                                            request,
					    response.getOutputStream(),
                                            clientRequest.useCache());
	    
	} catch (IOException ioe){
	    // Ignore if user disconnects
	}

    }

}
