package gov.noaa.pmel.tmap.las.luis;
import java.sql.*;
import javax.servlet.*;
import org.apache.log4j.Category;
import org.apache.log4j.BasicConfigurator;
import java.util.*;
import java.lang.String;

// Very dumb, but quick and dirty, connection manager
/*
 * Yeah, it's dump alright.  The code doesn't catch SQLExceptions in other
 * classes and retry.  Connections get lost after timeouts between
 * sessions.  To fix this we either have to go and catch every SQLException
 * rebuild the connection and retry (best solution, but too much effort),
 * test every connection here before it's used (why have a pool then),
 * or stop pooling and build them fresh every time (easy, fast enough until
 * we stop using this type of code for the UI).
 */
public class ConnectionManager {
  static Stack mConnections = new Stack();
  static String mJdbcUrl;
  static {
    try {
      Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    } catch (Exception e){
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
    ResourceBundle bundle = null;
    try {
      bundle = ResourceBundle.getBundle("las");
      mJdbcUrl = "jdbc:mysql://" + bundle.getObject("las.db.host") + "/"
	+ bundle.getObject("las.db.dbase")
	+ "?user=" + bundle.getObject("las.db.user")
	+ "&password=" + bundle.getObject("las.db.password")
	+ "&autoReconnect=true";
    } catch (MissingResourceException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    } // end of try-catch
	
  }
  static public synchronized Connection getConnection() throws SQLException {
    Connection c=DriverManager.getConnection(mJdbcUrl);
    return c;
  }

  static public synchronized void freeConnection(Connection c)
    throws SQLException {
    c.close();
  }
}
