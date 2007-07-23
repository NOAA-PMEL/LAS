// $Id: BrowserCandidate.java,v 1.1 2005/02/02 21:40:51 rhs Exp $ 
package gov.noaa.pmel.tmap.las.luis.db;
import gov.noaa.pmel.tmap.las.luis.Log;
import gov.noaa.pmel.tmap.las.luis.Utils;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.String;
import junit.framework.*;
import java.sql.SQLException;

import java.util.Vector;
import java.util.Iterator;

/**
 * Java Browser Sniffer
 */
public class BrowserCandidate extends Browser {
  public BrowserCandidate() {
    super("BrowserCandidate");
  }
  public boolean isCompatible() throws SQLException {

     BrowserCandidate b = new BrowserCandidate();
     try {
        b.deserialize(Utils.quote(this.agent), "agent", " and applet='1'");
        return true;
     } catch (SQLException e) {
        // Not finding a match is an exception.
        if ( e.getMessage().indexOf("No match for id") >= 0 ) {
           return false;
        }
        else {
           throw new SQLException(e.getMessage());
        }
     }

  }
}

