package gov.noaa.pmel.tmap.las.product.server;

import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.test.LASTest;
import gov.noaa.pmel.tmap.las.test.LASTestOptions;

import java.io.UnsupportedEncodingException;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

public class TestTask extends TimerTask {
	ServletContext context;
	private static Logger log = LogManager.getLogger(TestTask.class.getName());
	public TestTask(ServletContext context) {
		this.context = context;
	}
	@Override
	public void run() {
		LASConfig lasConfig = (LASConfig) context.getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
		LASTestOptions l = new LASTestOptions();
		
		//TODO get testing parameters from the config and set up the right tests...
		
		// For now just do an F-TDS test...
		l.setTestFTDS();
		
		LASTest lasTest = new LASTest(l, lasConfig);
		lasTest.runTest(l, true);
	}
}
