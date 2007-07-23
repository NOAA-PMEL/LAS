package gov.noaa.pmel.tmap.las.luis;
import org.apache.log4j.*;
public class Log {
    static Logger log = Logger.getLogger(Log.class);
    static void setClass(Object o){
	log = Logger.getLogger(o.getClass());
    }

    static public void error(Object o, String s){
	setClass(o);
	log.error(s);
    }

    static public void warn(Object o, String s){
	setClass(o);
	log.warn(s);
    }

    static public void info(Object o, String s){
	setClass(o);
	log.info(s);
    }

    static public void debug(Object o, String s){
	setClass(o);
	log.debug(s);
    }
}
