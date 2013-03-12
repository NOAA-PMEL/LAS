package gov.noaa.pmel.tmap.las.product.server;

import gov.noaa.pmel.tmap.exception.LASException;

import java.io.UnsupportedEncodingException;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

public class UpdateTask extends TimerTask {
	ServletContext context;
	private static Logger log = LogManager.getLogger(UpdateTask.class.getName());
	public UpdateTask(ServletContext context) {
		this.context = context;
	}
	@Override
	public void run() {
	    LASConfigPlugIn plugin = new LASConfigPlugIn();
	    try {
	        log.info("START: Config update.");
	        plugin.update(context);
	        log.info("END: Config update.");
	    } catch (ServletException e) {
	        // Log the error and continue.
	        log.error("Could not update configuration: "+e.toString());
	    } catch (JDOMException e) {
	        // Log the error and continue.
	        log.error("Could not update configuration: "+e.toString());
	    } catch (UnsupportedEncodingException e) {
	        // Log the error and continue.
	        log.error("Could not update configuration: "+e.toString());
	    } catch (LASException e) {
	        // Log the error and continue.
	        log.error("Could not update configuration: "+e.toString());
	    }
	}

}
