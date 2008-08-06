package gov.noaa.pmel.tmap.las.product.server;

import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASDocument;
import gov.noaa.pmel.tmap.las.jdom.ServerConfig;
import gov.noaa.pmel.tmap.las.exception.LASException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

public class LASConfigPlugIn implements PlugIn {
    
	/* These are Java objects that contain these configuration pieces. */
    /**  Servlet context key server config is stored under */
    public final static String LAS_CONFIG_KEY = "las_config";

    /**  Servlet context key server config is stored under */
    public final static String SERVER_CONFIG_KEY = "server_config";

    /**  Servlet context key server config is stored under */
    public final static String CACHE_KEY = "cache";
    
    /* 
     * These are the files names from which these configuration objects are derived. 
     * The file names are used by the reinit method.
     */
    public final static String LAS_CONFIG_FILENAME_KEY = "las_config_filename";
    public final static String LAS_SERVER_CONFIG_FILENAME_KEY = "server_config_filename";
    public final static String LAS_OPERATIONS_CONFIG_FILENAME_KEY = "operations_config_filename";
    public final static String LAS_UI_CONFIG_FILENAME_KEY = "ui_config_filename";
    
    private static Logger log = LogManager.getLogger(LASConfigPlugIn.class.getName());
    
    private ServletContext context;

    /** File name from the plug-in config for the productserver.xml file **/
    private String serverConfigFileName=null;

    public String getServerConfigFileName() {
        return serverConfigFileName;
    }

    public void setServerConfigFileName(String serverConfigFileName) {
        this.serverConfigFileName = serverConfigFileName;
    }
    
    /** File name from plug-in config **/
    private String configFileName=null;
    
    public String getConfigFileName() {
        return configFileName;
    }
    
    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }
    /*
     *  File name from the plug-in config
     *  for the v7 operations definitions
     */
    private String v7OperationsFileName;
    
    public String getV7OperationsFileName() {
        return v7OperationsFileName;
    }
    public void setV7OperationsFileName(String v7OperationsFileName) {
        this.v7OperationsFileName = v7OperationsFileName;
    }
    
    /*
     *  File name from the plug-in config
     *  for the v7 operations definitions
     */
    private String lasUIFileName;
    
    public String getLasUIFileName() {
        return lasUIFileName;
    }
    public void setLasUIFileName(String lasUIFileName) {
        this.lasUIFileName = lasUIFileName;
    }
    
    
    
    /**
     *  Initialize the LASConfig
     *
     *@param  servlet               The ActionServlet for this web application
     *@param  config                The ModuleConfig for our owning module
     *@exception  ServletException  if we cannot configure ourselves correctly
     * @throws IOException 
     */
    public void init(ActionServlet servlet, ModuleConfig config)
    throws ServletException {
        
        context = servlet.getServletContext();
        
        if ((configFileName == null || configFileName.length() == 0)) {
            throw new ServletException("No LAS configuration file specified.");
        } else {
        	// Store in the servlet context for use by reinit method
        	context.setAttribute(LAS_CONFIG_FILENAME_KEY, configFileName);
        }
        
        /* Set up the serverConfig for this server. */

        if ((serverConfigFileName == null || serverConfigFileName.length() == 0)) {
            throw new ServletException("No server configuration file specified.");
        } else {
        	// Store in the servlet context for use by the reinit method
        	context.setAttribute(LAS_SERVER_CONFIG_FILENAME_KEY, serverConfigFileName);
        }

        if (v7OperationsFileName == null || v7OperationsFileName.length() == 0) {
            throw new ServletException("No v7 operations file specified.");
        } else {
        	// Store in the servlet context for use by the reinit method
        	context.setAttribute(LAS_OPERATIONS_CONFIG_FILENAME_KEY, v7OperationsFileName);
        }
        
        if (lasUIFileName == null || lasUIFileName.length() == 0) {
            throw new ServletException("No ui.xml file specified.");
        } else {
        	// Store in the servlet context for use by the reinit method
        	context.setAttribute(LAS_UI_CONFIG_FILENAME_KEY, lasUIFileName);
        }
        
        go_init();
    }
    public void reinit(ServletContext reinitContext) throws ServletException {
    	context = reinitContext;
    	configFileName = (String) reinitContext.getAttribute(LAS_CONFIG_FILENAME_KEY);
    	serverConfigFileName = (String) reinitContext.getAttribute(LAS_SERVER_CONFIG_FILENAME_KEY);
    	v7OperationsFileName = (String) reinitContext.getAttribute(LAS_OPERATIONS_CONFIG_FILENAME_KEY);
    	lasUIFileName = (String) reinitContext.getAttribute(LAS_UI_CONFIG_FILENAME_KEY);
    	
    	if ((configFileName == null || configFileName.length() == 0)) {
            throw new ServletException("No LAS configuration file specified.");
        }
        
        /* Set up the serverConfig for this server. */

        if ((serverConfigFileName == null || serverConfigFileName.length() == 0)) {
            throw new ServletException("No server configuration file specified.");
        }

        if (v7OperationsFileName == null || v7OperationsFileName.length() == 0) {
            throw new ServletException("No v7 operations file specified.");
        }
        
        if (lasUIFileName == null || lasUIFileName.length() == 0) {
            throw new ServletException("No ui.xml file specified.");
        }
    	go_init();
    }
    public void go_init() {

    	File configFile = new File(configFileName);
        LASConfig lasConfig = new LASConfig();
        
        try {
            JDOMUtils.XML2JDOM(configFile, lasConfig);
        } catch (Exception e) {
            log.error("Could not parse the las config file "+configFileName);
        }

        File serverConfigFile = new File(serverConfigFileName);
        ServerConfig serverConfig = new ServerConfig();

        try {
            JDOMUtils.XML2JDOM(serverConfigFile, serverConfig);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        context.setAttribute(SERVER_CONFIG_KEY, serverConfig);

        // Create the Cache
        Cache cache = new Cache(serverConfig.getCacheSize());

        // Read the Cache if it exists
        File cacheFile = serverConfig.getCacheFile();
        if ( cacheFile != null) {
            try {
                cache.loadCacheFromStore(cacheFile);
            } catch (Exception e) {
               log.warn("Cache file not loaded: "+e.toString());
            }
        }

        // Store the cache in the context.
        context.setAttribute(CACHE_KEY, cache);
        
        Element root = lasConfig.getRootElement();
        String version = root.getAttributeValue("version");
        boolean seven = false;
        if ( version != null && version.contains("7.")) {
            seven = true;
        }
        
        File v7OperationsFile = new File(v7OperationsFileName);
        LASDocument v7operationsDoc = new LASDocument();
        try {
            JDOMUtils.XML2JDOM(v7OperationsFile, v7operationsDoc);
        } catch (Exception e) {
            log.error("Could not parse the v7 operations file "+v7OperationsFileName, e);
        }
        
        
        List v7operations = v7operationsDoc.getRootElement().getChildren("operation");
        Element operations = lasConfig.getRootElement().getChild("operations");
        for (Iterator opIt = v7operations.iterator(); opIt.hasNext();) {
            Element op = (Element) opIt.next();
            operations.addContent((Element)op.clone());
        }
        
        
        if ( lasConfig.getOutputDir() == null ) {
            lasConfig.setOutputDir(context.getRealPath("/")+"output");
        }
        
        if ( !seven ) {
            lasConfig.convertToSeven();
        }
        
        lasConfig.mergeProperites();
        
        try {
            lasConfig.addIntervalsAndPoints();        
        } catch (Exception e) {
            log.error("Could not add the intervals and points attributes to variables in this LAS configuration.", e);
        }
        
        try {
            lasConfig.addGridType();
        } catch (Exception e) {
            log.error("Could not add the grid_type to variables in this LAS configuration.", e);
        }

        String fds_base = serverConfig.getFTDSBase();
        String fds_dir = serverConfig.getFTDSDir();
        try {
            log.debug("Adding F-TDS attributes to data set.");
            log.debug("base url: "+fds_base+" local directory "+fds_dir);
            lasConfig.addFDS(fds_base, fds_dir);
        } catch (LASException e) {
            log.error("Could not add F-TDS URLs to data configuration. "+e.toString());
        } catch (JDOMException e) {
            log.error("Could not add F-TDS URLs to data configuration. "+e.toString());
        } catch (IOException e) {
            log.error("Could not add F-TDS URLs to data configuration. "+e.toString());
        }
        
        
        File lasUIFile = new File(lasUIFileName);
        LASDocument lasUIDoc = new LASDocument();
        try {
            JDOMUtils.XML2JDOM(lasUIFile, lasUIDoc);
        } catch (Exception e) {
            log.error("Could not parse the ui.xml file "+lasUIFileName, e);
        }
        
        String title = lasUIDoc.getRootElement().getAttributeValue("title");
        List uis = lasUIDoc.getRootElement().getChildren("ui");
        
        Element ui = new Element("lasui");
        if (title != null && !title.equals("")) {
            ui.setAttribute("title", title);
        }
        for (Iterator uiIt = uis.iterator(); uiIt.hasNext();) {
            Element uiE = (Element) uiIt.next();
            ui.addContent((Element)uiE.clone());
        }
        
        List options = lasUIDoc.getRootElement().getChildren("options");
        
        for (Iterator optionsIt = options.iterator(); optionsIt.hasNext();) {
            Element optionsElement = (Element) optionsIt.next();
            List optionsDefElements = optionsElement.getChildren("optiondef");
            for (Iterator optionsDefElementsIt = optionsDefElements.iterator(); optionsDefElementsIt.hasNext();) {
                Element optionsDef = (Element) optionsDefElementsIt.next();
                String od_name = optionsDef.getAttributeValue("name");
                List optionElements = optionsDef.getChildren("option");
                for (Iterator optionsElementsIt = optionElements.iterator(); optionsElementsIt.hasNext();) {
                    String id = "id_"+Double.toString(Math.random());
                    Element option = (Element) optionsElementsIt.next();
                    Element textfield = option.getChild("textfield");
                    String name = "";
                    if (textfield != null) {
                        name = textfield.getAttributeValue("name");
                    }
                    Element menu = option.getChild("menu");
                    if ( menu != null ) {
                        name = menu.getAttributeValue("name");
                    }
                    if (!od_name.equals(name)) {
                        id = od_name+"_"+name.replaceAll(" ", "_");
                    } else {
                        id = od_name;
                    }
                    option.setAttribute("ID", id);
                }
            }
            ui.addContent((Element)optionsElement.clone());
        }
        
        List defaults = lasUIDoc.getRootElement().getChildren("defaults");
        for (Iterator defit = defaults.iterator(); defit.hasNext();) {
            Element defaultE = (Element) defit.next();
            ui.addContent((Element)defaultE.clone());
        }
        
        List maps = lasUIDoc.getRootElement().getChildren("maps");
        for (Iterator mapIt = maps.iterator(); mapIt.hasNext();) {
            Element mapsE = (Element) mapIt.next();
            ui.addContent((Element)mapsE.clone());
        }
        
        List menus = lasUIDoc.getRootElement().getChildren("menus");
        for (Iterator menuIt = menus.iterator(); menuIt.hasNext();) {
            Element menusE = (Element) menuIt.next();
            ui.addContent((Element)menusE.clone());
        }
        
        lasConfig.getRootElement().addContent(ui);
        
        
        File v7 = new File(lasConfig.getOutputDir()+"/lasV7.xml");
        try {
        	lasConfig.write(v7);
        } catch (Exception e) {
            log.error("Cannot write out new Version 7.0 las.xml file.", e);
        }
       context.setAttribute(LAS_CONFIG_KEY, lasConfig);   
    }
    public void destroy() {

    	Cache cache = (Cache) context.getAttribute(ServerConfigPlugIn.CACHE_KEY);
    	ServerConfig serverConfig = (ServerConfig)context.getAttribute(ServerConfigPlugIn.SERVER_CONFIG_KEY);
    	try {
    		cache.saveCacheToStore(serverConfig.getCacheFile());
    	} catch (LASException e) {
    		log.error(e.toString());
    	}

    }
}
