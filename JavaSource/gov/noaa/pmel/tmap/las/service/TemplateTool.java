package gov.noaa.pmel.tmap.las.service;

import gov.noaa.pmel.tmap.las.exception.LASException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.XMLToolboxManager;

public class TemplateTool extends Tool {
    
    public VelocityEngine ve = new VelocityEngine();
    Map toolboxContext;
    private File configFile;
    final Logger log = LogManager.getLogger(TemplateTool.class.getName());
    
    public TemplateTool() {    
    }
    
    public TemplateTool(String serviceName, String configFileName) throws LASException, IOException {
        Properties p = new Properties();
        InputStream is;
        String resourcePath = "resources/"+serviceName;
        is = this.getClass().getClassLoader().getResourceAsStream(resourcePath+"/velocity.properties");
        String template = getResourcePath(resourcePath+"/templates");
        if (is == null) {
            if ( template != null ) {
                // Can't find properties file.  Set where we look for templates.
                log.info("Setting template path to default: "+template);
                p.setProperty("file.resource.loader.path", template);
            } else {
                throw new LASException("Cannot find "+serviceName+" templates directory.");
            }
        } else {
           p.load(is);
        }  
        if ( p.getProperty("file.resource.loader.path") == null ) {
            if ( template != null ) {
                // Can't find properties file.  Set where we look for templates.
                log.info("Template path not found in properties file.  Setting to default: "+template);
                p.setProperty("file.resource.loader.path", template);
            } else {
                throw new LASException("Cannot find database templates directory.");
            }
        }
           
        try {
            log.info("Setting loader path to: "+p.getProperty("file.resource.loader.path"));
            ve.init(p);
        } catch (Exception e) {
            throw new LASException("Cannot initialize the velocity engine.");
        }
        
        String configPath = getResourcePath(resourcePath+"/"+configFileName);
        if ( configPath != null ) {
            configFile = new File(configPath);                               
        } else {
            throw new LASException("Config file "+ configPath +" not found.");
        }
        
        XMLToolboxManager toolboxManager = new XMLToolboxManager();
        try {
            InputStream tbis = this.getClass().getClassLoader().getResourceAsStream("resources/services/toolbox.xml");
            if ( tbis == null ) {
                String toolboxPath = getResourcePath("resources/services/toolbox.xml");
                tbis = new FileInputStream(toolboxPath);
                throw new LASException("The Velocity toolbox configuration toolbox.xml not found. ");
            }
            toolboxManager.load(tbis);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new LASException("The Velocity toolbox configuration toolbox.xml not found. "+e.toString());
        }
        toolboxContext = toolboxManager.getToolbox(null);
    }

    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    public Map getToolboxContext() {
        return toolboxContext;
    }

    public void setToolboxContext(Map toolboxContext) {
        this.toolboxContext = toolboxContext;
    }
}
