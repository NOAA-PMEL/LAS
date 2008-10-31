package gov.noaa.pmel.tmap.las.product.server;

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
			log.info("Update started.");
			plugin.update(context);
			log.info("Update finished.");
		} catch (ServletException e) {
			// Log the error and continue.
			log.error("Could not update configuration: "+e.toString());
		} catch (JDOMException e) {
			// Log the error and continue.
			log.error("Could not update configuration: "+e.toString());
		}
	}

}
