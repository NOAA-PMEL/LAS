package org.iges.anagram.filter;

import java.util.*;

import org.iges.anagram.*;
import org.iges.anagram.service.*;

import org.iges.util.*;

    
/** Performs analysis for requests that contain analysis expressions.
 */
public class AnalysisFilter 
    extends Filter {

    public static String ANALYSIS_PREFIX = "/_expr_";

    public AnalysisFilter() {
	generating = new LinkedList();
	applicable = Arrays.asList(new String[] {
	    "dds", "das", "dods", "info", "asc", "ascii"
	});
    }

    public String getFilterName() {
	return "analysis";
    }

    protected void doFilter(ClientRequest clientRequest) 
	throws ModuleException {

	if (applicable.contains(clientRequest.getServiceName()) &&
	    clientRequest.getHandle() == null &&
	    clientRequest.getDataPath().indexOf(ANALYSIS_PREFIX)>=0) {

	    doAnalysis(clientRequest);
	} else {
	    if (debug()) debug("no analysis to do");
	}

	next.handle(clientRequest);
	
    }

    protected void doAnalysis(ClientRequest clientRequest) 
	throws ModuleException {

	String name = clientRequest.getDataPath();

	synchronized (generating) {
	    while (generating.contains(name)) {
		if (debug()) log.debug(this, clientRequest + 
				       "waiting for analysis to complete");
		try {
		    generating.wait(0);
		} catch (InterruptedException ie) {}
		
	    }

	    if (server.getCatalog().contains(name)) {
		if (debug()) log.debug(this, clientRequest + 
				       "analysis result already in cache");
		return;
	    } 
	    
	    log.info(this, "evaluating analysis expression: " + name);
	    generating.add(name);
	}
	
	try {
	    TempDataHandle result = 
		server.getTool().doAnalysis(name,
                                            clientRequest.getHttpRequest(),
					    clientRequest.getPrivilege());
	    
	    server.getCatalog().addTemp(result);
	    clientRequest.setHandle(server.getCatalog().getLocked(clientRequest.getDataPath()));
	    
	    if (debug()) log.debug(this, clientRequest + 
				   "finished analysis");

	} catch (ModuleException me) {
	    throw me;
	} finally {
	    synchronized (generating) {
		generating.remove(name);
		generating.notifyAll();
	    }
	}
    }


    protected List applicable;

    protected List generating;

}
