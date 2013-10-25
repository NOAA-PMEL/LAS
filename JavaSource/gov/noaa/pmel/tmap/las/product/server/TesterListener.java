package gov.noaa.pmel.tmap.las.product.server;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.test.LASTestOptions;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class TesterListener implements ServletContextListener {
    private ScheduledExecutorService scheduler;
    private ServletContext context; 
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        context = event.getServletContext();
        LASConfig lasConfig = (LASConfig) context.getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
        LASTestOptions lto = lasConfig.getTestOptions();
        if ( lto != null ) {
           
            long d = lto.getDelay();
            if ( d < 0 ) d = 0;
            long p = lto.getPeriod();
            // Not more than twice a day.  
            if ( p < 43200000 ) p = 43200000;
            scheduler.scheduleAtFixedRate(new TestTask(context), d, p, TimeUnit.MICROSECONDS);
        }

    }

}
