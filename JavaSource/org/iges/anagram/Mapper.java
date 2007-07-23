package org.iges.anagram;

import java.util.*;
import java.net.*;

import javax.servlet.http.*;

import org.iges.anagram.service.*;
import org.iges.util.*;

/** Maps incoming servlet requests to services and privilege levels,
 *  and returns completed ClientRequest objects.
 */
public class Mapper 
    extends AbstractModule {

    public String getModuleID() {
	return "mapper";
    }

    public void configure(Setting setting) 
	throws ConfigException {
	if (services == null) {
	    createServices();
	}

	Iterator it = services.values().iterator();
	while (it.hasNext()) {
	    Service service = (Service)it.next();
	    Setting serviceSetting = null;
	    try {
		serviceSetting = 
		    setting.getUniqueSubSetting(service.getModuleID());
	    } catch (AnagramException ae) {
		throw new ConfigException(this, ae.getMessage());
	    }
	    boolean enabled = 
		serviceSetting.getAttribute("enabled", "true").equals("true");
	    service.setEnabled(enabled);
	    service.configure(serviceSetting);
	}
    }


    protected void createServices() {
	services = new HashMap();
	services.put("admin", new AdminService());
	services.put("asc", new ASCIIDataService());
	services.put("ascii", services.get("asc"));
	services.put("das", new DASService());
	services.put("dds", new DDSService());
	services.put("dir", new DirectoryService());
	services.put("dods", new BinaryDataService());
	services.put("help", new HelpService());
	services.put("info", new InfoService());
	services.put("thredds", new THREDDSCatalogService());
	services.put("upload", new UploadService());
	services.put("ver", new VersionService());
	services.put("version", services.get("ver"));
	services.put("xml", new XMLCatalogService());
	Iterator it = services.values().iterator();
	while (it.hasNext()) {
	    ((Module)it.next()).init(server, this);
	}
    }

    /** Builds a ClientRequest object from the servlet request provided.
     */
    public ClientRequest map(HttpServletRequest request,
			     HttpServletResponse response) {
	
	Privilege privilege = server.getPrivilegeMgr().getPrivilege(request);
        String url =FDSUtils.getServletPath(request);

        if(request.getQueryString()==null)
	    log.debug(this, "request URL is: " + url);
        else
	    log.debug(this, "request URL is: " + url+"?"+request.getQueryString());

        boolean useCache = !url.startsWith(NO_CACHE);
	Handle handle = server.getCatalog().getLocked(url);
	if (handle != null) {
	    if (debug()) log.debug(this, "got lock for handle: " + url);
	    return mapToHandle(request, response, handle, privilege, useCache);
	} else {
	    return mapByExtension(request, response, url, privilege, useCache);
	}
	
    }

    protected ClientRequest mapToHandle(HttpServletRequest request,
					HttpServletResponse response,
					Handle handle,
					Privilege privilege,
                                        boolean useCache) {
	Service service;
	if (handle instanceof DirHandle) {
	    if (debug()) debug(handle + " is a dir");
	    service = (Service)services.get("dir");
	} else {
	    if (debug()) debug(handle + " is a dataset");
	    service =  (Service)services.get("info");
	}
	String dataPath = handle.getCompleteName();
	return new ClientRequest(request, 
				 response, 
				 privilege,
				 service, 
				 service.getServiceName(), 
				 dataPath,
				 handle,
                                 useCache);
    }
	

    protected ClientRequest mapByExtension(HttpServletRequest request,
					   HttpServletResponse response,
					   String url,
					   Privilege privilege,
                                           boolean useCache) {
	if (debug()) debug("parsing extension");

	int lastSlash = FDSUtils.lastIndexOf('/', url);
	if (lastSlash < 0) {
	    lastSlash = 0;
	}
	String fileName = url.substring(lastSlash + 1);
	int lastDot = FDSUtils.lastIndexOf('.', fileName);

        String extension = null;
        if(lastDot>=0)
 	   extension = fileName.substring(lastDot + 1);
        else
           extension = "info";

	if (debug()) debug("extension is " + extension);

	Service service = (Service)services.get(extension);

	String dataPath = null;
        if(lastDot>=0) {
            dataPath = url.substring(0, url.length() - 
					(extension.length() + 1));
        }
        else{
            dataPath = url;
        }

	Handle handle = server.getCatalog().getLocked(dataPath);
	if (debug()) log.debug(this, "got lock for handle: " + dataPath);

	return new ClientRequest(request, 
				 response, 
				 privilege,
				 service, 
				 extension, 
				 dataPath,
				 handle, 
                                 useCache);
    }

    public static String NO_CACHE = "/_nocache_/";

    protected Map services;
}
