package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.SQLException;

public class SearchFormBean extends AbstractFormBean {
  
  public boolean isValid(String nextUrl) throws ServletException, SQLException{
    setErrorMessage("Search not supported");
    return false;
  }

  public void init(HttpServletRequest req)
    throws ServletException, SQLException {
    super.init(req);
  }

  public void handle() throws ServletException, SQLException {
  }

  public String nextURL() throws ServletException, SQLException {
    return "";
  }
}
