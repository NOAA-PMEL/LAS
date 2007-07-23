// $Id: OptionsFormBean.java,v 1.5 2002/06/26 18:17:53 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import gov.noaa.pmel.tmap.las.luis.db.Category;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.SQLException;

public class OptionsFormBean extends AbstractFormBean {

  public boolean isValid(String nextUrl) {
    return true;
  }

  public void handle() throws ServletException, SQLException {
    Log.debug(this, "Setting options in session object");
    getSession().getSessionObject().setOptions(getParameters());
  }

  public String nextURL() throws ServletException, SQLException {
    return "data";
  }

}
