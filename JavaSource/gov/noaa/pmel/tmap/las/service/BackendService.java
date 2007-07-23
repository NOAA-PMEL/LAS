package gov.noaa.pmel.tmap.las.service;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

public class BackendService {
    // This object could be persisted between requests if this
    // were packaged in a servlet.
    public void setLogLevel(String debug) {
    
        Logger ancestor = LogManager.getLogger("gov.noaa.pmel.tmap");
        
        if ( debug != null && debug != "") {
            debug = debug.toLowerCase();
            if ( debug.equals("true") || debug.equals("debug") ) {
                ancestor.setLevel(Level.DEBUG);
            } else if ( debug.equals("info") ) {
                ancestor.setLevel(Level.INFO);
            } else if ( debug.equals("warn") ) {
                ancestor.setLevel(Level.WARN);
            } else if ( debug.equals("error") ) {
                ancestor.setLevel(Level.ERROR);
            } else if ( debug.equals("fatal") ) {
                ancestor.setLevel(Level.FATAL);
            } else {
                ancestor.setLevel(Level.INFO);
            }
        } else {
            debug = "info";
            ancestor.setLevel(Level.INFO);
        }
    }
}
