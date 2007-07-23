package org.iges.anagram.service;

import java.io.*;
import java.util.*;
import java.text.*;
import javax.servlet.http.*;

import org.iges.util.*;
import org.iges.anagram.*;

/** Sends a complete listing of the server's contents in THREDDS XML format.
 */
public class THREDDSCatalogService
    extends Service {

    public String getServiceName() {
	return "thredds";
    }

    public void configure(Setting setting) {
        counter = 0;
    }

    public void handle(ClientRequest clientRequest)
	throws ModuleException {
	
	HttpServletRequest request = clientRequest.getHttpRequest();
	HttpServletResponse response = clientRequest.getHttpResponse();


	Hashtable queryParams = getQueryParams(clientRequest);
	String[] clientVersion = (String[])queryParams.get("version");

	THREDDSPrinter catalog;

	if (clientVersion == null || clientVersion[0].equals("0.6")) {
	    catalog = v06Printer; 
	} else if (clientVersion[0].equals("1.0")){
  	    throw new ModuleException(this, "THREDDS v1.0 support not yet enabled");
	    // THREDDS 1.0 support is partially implemented but 
	    // not ready for deployment yet. 

    	    //	    catalog = v10Printer;
	} else {
		throw new ModuleException
		    (this, "unsupported THREDDS version: " + clientVersion[0] + 
		     " (must be 0.6 or 1.0)");
	}	    
	  
	response.setContentType("text/xml");
	response.setDateHeader("Last-Modified", server.getLastConfigTime());

	PrintStream page;
        File cache = null;
	try {
	    page = 
		new PrintStream
		(response.getOutputStream());

	    catalog.printHeader(page, getSiteName(clientRequest)+getBaseURL(clientRequest));

	    cache = catalog.load(clientRequest.getPrivilege(),
                                 clientRequest.useCache());

 	    InputStream in = 
		    new BufferedInputStream
		    (new FileInputStream
		     (cache));

	    Spooler.spool(in, page);
	    in.close();
            if(!clientRequest.useCache())
               cache.delete();
	} catch (IOException ioe) {
            if(cache!=null)
               cache.delete();
	    throw new ModuleException
	            (this, "spooling THREDDS catalog failed");
        }
	    	
    }

    protected long counter;

    protected abstract class THREDDSPrinter {
	
	protected File load(Privilege privilege,
                            boolean useCache) 
	    throws ModuleException {

            File cache = null;
	    try {
                 String cacheName;
                 synchronized(THREDDSCatalogService.this){
                     cacheName = "catalog-" + getVersion() 
                                 + System.currentTimeMillis() 
                                 + (THREDDSCatalogService.this.counter++)
                                 + ".xml"; 
                 }

                 cache = server.getStore().get(THREDDSCatalogService.this, 
                                               cacheName);

                 if (debug()) debug("generating new THREDDS catalog (version "
                                     + getVersion() + ")");
		 PrintStream print;
		 print = new PrintStream(new FileOutputStream(cache));
		 printCatalog(print, privilege, useCache);
		 print.close();

	    } catch (IOException ioe) {
                cache.delete();
	        throw new ModuleException
		        (THREDDSCatalogService.this, 
		         "saving THREDDS catalog version " + 
		         getVersion() + " failed");
	    }

            return cache;
	}

	protected void printCatalog(PrintStream page, 
                                    Privilege privilege,
                                    boolean useCache) 
	    throws ModuleException {

	    // Retrieve dataset list
	    DirHandle root = (DirHandle)server.getCatalog().getLocked("/");
	    
	    try {
		printDir(page, root, "        ", privilege, useCache);
		
	    } catch (ModuleException me) {
		throw me;
	    } finally {
		root.getSynch().release();
	    }

	    printFooter(page);
	    page.flush();
	}


	protected abstract String getVersion();
	protected abstract void printHeader(PrintStream page, String baseURL);
	protected abstract void printFooter(PrintStream page);

	protected abstract void printDataset(PrintStream page, 
					     DataHandle dataset,
                                             String indent,
                                             Privilege privilege,
                                             boolean useCache);
	protected void printDir(PrintStream page, 
				DirHandle dir, 
                                String indent,
                                Privilege privilege,
                                boolean useCache) 
	    throws ModuleException{
	
	    Collection datasets = dir.getEntries(false).values();
	
	    Iterator it = datasets.iterator();
	    while (it.hasNext()) {
		Handle handle = (Handle)it.next();
		handle.getSynch().lock();
		if (handle instanceof DirHandle) {
		    DirHandle subdir = (DirHandle)handle;
                    if(privilege.everAllowsPath(subdir.getCompleteName())) {
		        page.print(indent + "<dataset name=\"");
		        page.print(subdir.getName() + "\" >\n");
		        printDir(page, subdir, indent + "    ", privilege, useCache);
		        page.print(indent + "</dataset>\n");
                    }
		} else {
                    DataHandle dataHandle = (DataHandle)handle;
                    if(privilege.allowsPath(dataHandle.getCompleteName())) {
  		        printDataset(page, dataHandle, indent, privilege, useCache);
                    }
		}
		handle.getSynch().release();
	    }
	}	

    }

    protected class THREDDSv06Printer
	extends THREDDSPrinter {

	protected String getVersion() {
	    return "0.6";
	}

	protected void printHeader(PrintStream page, String baseURL) {
    
	    page.print
		("<?xml version=\"1.0\"?>\n");
	    page.print
		("<!DOCTYPE catalog SYSTEM \"http://www.unidata.ucar.edu/projects/THREDDS/xml/InvCatalog.0.6.dtd\">\n");
	    page.print
		 ("<catalog" +
		  " xmlns=\"http://www.unidata.ucar.edu/thredds\"\n" +
		  "         xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
		  "         version=\"0.6\" name=\"" + server.getServerName() + 
		  "\" >\n");
	    page.print
		("    <dataset name=\"" + server.getServerName() + "\" >\n" +
		 "        <service name=\"" + server.getModuleName() + 
		"\" serviceType=\"DODS\" base=\"" + baseURL + "\" />\n");
	}

	protected void printDataset(PrintStream page, 
                                    DataHandle dataset, 
                                    String indent,
                                    Privilege privilege,
                                    boolean useCache) {
	    page.print(indent + "<dataset name=\"");
	    page.print(dataset.getDescription() + "\"\n");
	    page.print(indent + "         urlPath=\"");
	    page.print(dataset.getCompleteName() + "\"\n");
	    page.print(indent + "         serviceName=\"" + 
		       server.getModuleName() + "\" />\n");
	}

	protected void printFooter(PrintStream page) {
	    page.print("    </dataset>\n" +
		       "</catalog>\n");
	}

    }

    protected class THREDDSv10Printer
	extends THREDDSPrinter {

	protected String getVersion() {
	    return "1.0";
	}

	protected void printHeader(PrintStream page, String baseURL) {
	
	    page.print
		("<?xml version=\"1.0\"?>\n");
	    page.print
		 ("<catalog" + 
		  " xmlns=\"http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0\"" +
		 " xmlns:xlink=\"http://www.w3.org/1999/xlink\"" +
		 " version=\"1.0\" name=\"" + server.getServerName() + 
		  "\" >\n");
	    page.print
		("    <service name=\"" + server.getModuleName() + 
		 "\" serviceType=\"DODS\" base=\"" + baseURL + "/\" />\n");

	}

	protected void printDataset(PrintStream page, 
                                    DataHandle dataset,
                                    String indent, 
                                    Privilege privilege,
                                    boolean useCache) {

	    try {
		server.getTool().writeTHREDDSTag(dataset, privilege, page, useCache);
	    } catch (ModuleException me) {
		error("no catalog info for " + dataset + ": " + 
		      me.getMessage());
		page.print(indent + "<dataset name=\"");
		page.print(dataset.getDescription() + "\"\n");
		page.print(indent + "         urlPath=\"");
		page.print(dataset.getCompleteName() + "\"\n");
		page.print(indent + "         serviceName=\"" + 
			   server.getModuleName() + "\" />\n");
		
	    }
	}

	protected void printFooter(PrintStream page) {
	    page.print("</catalog>\n");
	}

    }

    protected THREDDSPrinter v06Printer = new THREDDSv06Printer();
    protected THREDDSPrinter v10Printer = new THREDDSv10Printer();

       
}
