package gov.noaa.pmel.tmap.las.product.server;

import gov.noaa.pmel.tmap.las.service.kml.LASPlacemarks;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class Reload extends Thread {
    private WatchService watcher;
    private ServletContext context;
    public Reload(WatchService watcher, ServletContext context) {
        this.watcher = watcher;
        this.context = context;
    }
    @Override
    public void run() {
        for (;;) {

            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }
            

            boolean restart = false;
            // The events come in bunches.  Take them all then decide what to do...
            WatchEvent.Kind<?> kind = null;
            for (WatchEvent<?> event: key.pollEvents()) {
                kind = event.kind();
                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                String filename = ev.context().toFile().getName();
                if ( !filename.startsWith(".") && !filename.endsWith("~") && (filename.endsWith("las.xml") || filename.endsWith("las_servers.xml")) ) {
                    // An event occurred on a file that does not look like an edit buffer...
                    restart = true;
                }
                
            }
            
            String lockedAttribute = (String) context.getAttribute(LASConfigPlugIn.LAS_LOCK_KEY);
            boolean locked = false;
            if (lockedAttribute != null && lockedAttribute.equals("true") ) {
                locked = true;
            }
            
            try {
                if ( restart && !locked) {
                    restart = false;
                    LASConfigPlugIn plugin = new LASConfigPlugIn();
                    context.setAttribute(LASConfigPlugIn.LAS_LOCK_KEY, "true");
                    plugin.reinit(context);
                    context.removeAttribute(LASConfigPlugIn.LAS_LOCK_KEY);
                }
            } catch (ServletException e) {
                continue;
            }


            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

}
