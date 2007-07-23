// $Id: DataFormBean.java,v 1.1 2002/11/27 22:37:27 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.sql.SQLException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.lang.String;

public class DataFormBean extends AbstractFormBean {
  public boolean isValid(String nextUrl) throws SQLException {
    return true;
  }

  public void handle() throws SQLException, ServletException {
  }

  public String nextURL() throws SQLException, ServletException {
    return "data";
  }
}
