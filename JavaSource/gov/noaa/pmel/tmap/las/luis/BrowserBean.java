// $Id: BrowserBean.java,v 1.2 2005/02/02 21:39:14 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import gov.noaa.pmel.tmap.las.luis.db.*;
import gov.noaa.pmel.tmap.las.luis.*;
import org.apache.velocity.*;
import java.sql.SQLException;


public class BrowserBean extends DefaultTemplateBean {
   public void init(TemplateContext tc) throws ServletException, SQLException {
      super.init(tc);
      HttpServletRequest req = tc.getServletRequest();

      Browser browser = new Browser();
      browser.init(req);
      tc.put("browser", browser);

      BrowserCandidate browserc = new BrowserCandidate();
      tc.put("candidate", browserc);

   }
}
