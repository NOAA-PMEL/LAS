package gov.noaa.pmel.tmap.las.product.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class InitThread extends Thread {
	private ServletContext context;
	private static Logger log = LogManager.getLogger(InitThread.class.getName());
	public InitThread(ServletContext context) {
		this.context = context;
	}
	public void run() {
		LASConfigPlugIn plugin = new LASConfigPlugIn();
		try {
			plugin.reinit(context);
		} catch (ServletException e) {
			log.error("Error in LAS init thread.");
		}
	}
}
