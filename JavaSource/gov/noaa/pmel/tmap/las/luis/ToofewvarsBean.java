package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.lang.Integer;
import java.lang.NumberFormatException;
import java.sql.SQLException;

public class ToofewvarsBean extends DefaultTemplateBean {

  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);
    HttpServletRequest req = tc.getServletRequest();
    Integer minvars = new Integer(1);
    String minvars_str = req.getParameter("minvars");
    if (minvars_str != null){
      try {
	minvars = Integer.valueOf(minvars_str);
      } catch (NumberFormatException e){
	Log.debug(this, "Bad integer param:" + minvars_str);
      }
    }
    tc.put("minvars", minvars);
  }
}
