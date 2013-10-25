package gov.noaa.pmel.tmap.las.product.server;

import gov.noaa.pmel.tmap.las.jdom.LASConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@WebListener
public class ReaperListener implements ServletContextListener  {
    private ScheduledExecutorService scheduler;
    private ServletContext context; 
    @Override
    public void contextInitialized(ServletContextEvent event) {
        context = event.getServletContext();
        LASConfig lasConfig = (LASConfig) context.getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
        
        // Default to once a day...
        String interval_string = "24";
        String age_string = "168";
        String ivunits = "hours";
        String time = "00:01";
        TimeUnit timeunit = TimeUnit.HOURS;

        // How often to reap in either hours or days 
        interval_string = lasConfig.getGlobalPropertyValue("product_server", "clean_interval");
        // Units used for the interval and the age
        ivunits = lasConfig.getGlobalPropertyValue("product_server", "clean_units");
        // Age expressed in chosen units at which a file is removed (defaults to 7 days)
        age_string = lasConfig.getGlobalPropertyValue("product_server", "clean_age");
        // Time of day at which to start the cleaner running.  Defaults to start immediately.
        time = lasConfig.getGlobalPropertyValue("product_server", "clean_time");

        if ( interval_string.equals("") ) {
            interval_string = "24";
        }
        if ( age_string.equals("") ) {
            age_string = "168";
        }
        if ( ivunits.equals("") ) {
            ivunits = "hours";
        }
        if ( time.equals("") ) {
            time = "00:01";
        }
     
        DateTime now = new DateTime();
        long interval = 1000*60*60*24;    
        long age = interval*7; // Older than a week old
        try {
            interval = Long.valueOf(interval_string);
            age = Long.valueOf(age_string);
            if ( ivunits.toLowerCase().contains("hour") ) {
                interval = interval * 1000*60*60;
                age = age * 1000*60*60;
                // timeunit defaults to hours
            } else if (ivunits.toLowerCase().contains("day") ) {
                interval = interval * 1000*60*60*24;         
                age = age * 1000*60*60*24;
                timeunit = TimeUnit.DAYS;
            }
        } catch (Exception e) {
            interval = 1000*60*60*24;
            age = interval*7;
        }

        DateTimeFormatter ymd = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter ymdhm = DateTimeFormat.forPattern("yyyy-MM-dd HH:ss");
        String today_string = ymd.print(now);
        today_string = today_string + " " + time;
        long delay = 0;
        DateTime startToday = ymdhm.parseDateTime(today_string);
        DateTime startTomorrow = startToday.plusHours(24);
        if ( now.isAfter(startToday) ) {
            delay = startTomorrow.getMillis() - now.getMillis();
        } else {
            delay = startToday.getMillis() - now.getMillis();
        }
        if ( delay < 0 ) {
            delay = 0;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new ReaperTask(context, age), delay, interval, timeunit);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        scheduler.shutdownNow();
    }


}
