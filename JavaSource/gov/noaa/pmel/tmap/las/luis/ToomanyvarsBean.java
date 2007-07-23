// $Id: ToomanyvarsBean.java,v 1.1 2002/08/20 21:42:23 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.lang.Integer;
import java.lang.NumberFormatException;
import java.sql.SQLException;

public class ToomanyvarsBean extends DefaultTemplateBean {

  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);
    HttpServletRequest req = tc.getServletRequest();
    Integer maxvars = new Integer(1);
    String maxvars_str = req.getParameter("maxvars");
    if (maxvars_str != null){
      try {
	maxvars = Integer.valueOf(maxvars_str);
      } catch (NumberFormatException e){
	Log.debug(this, "Bad integer param:" + maxvars_str);
      }
    }
    tc.put("maxvars", maxvars);
  }
}
