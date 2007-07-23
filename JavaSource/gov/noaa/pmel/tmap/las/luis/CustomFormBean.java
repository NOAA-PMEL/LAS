// $Id: CustomFormBean.java,v 1.1 2003/03/01 18:35:02 sirott Exp $ 
package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Enumeration;
import java.sql.SQLException;
import javax.servlet.ServletException;

public class CustomFormBean extends AbstractFormBean {

  public boolean isValid(String nextURL) throws ServletException,SQLException{
    return true;
  }

  public void handle() throws ServletException,SQLException{
    getSession().getSessionObject().setCustomFormParams(getParameters());
  }

  public String nextURL() throws ServletException, SQLException {
    return "data";
  }
}
