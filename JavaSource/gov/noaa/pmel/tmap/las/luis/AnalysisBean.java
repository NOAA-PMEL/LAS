// $Id: AnalysisBean.java,v 1.4 2004/04/23 17:49:42 callahan Exp $
package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import javax.servlet.ServletException;
import gov.noaa.pmel.tmap.las.luis.db.*;
import java.io.IOException;
import java.util.Iterator;

public class AnalysisBean extends ConstrainBean {

  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);
    HttpServletRequest req = tc.getServletRequest();
    HttpServletResponse resp = tc.getServletResponse();
    TemplateSession session = Utils.getSession(req);
    RegionConstraint rc = session.getSessionContext().getRegion();
    if (rc == null){
      noConstraintError(resp);
      return;
    }

    // Make sure there is at least two non-analysis axis
    ICategory theCat = rc.getCategory();
    int axisCount = 0;
    for (Iterator i = theCat.getVariableInfo().getAxes().iterator();
	 i.hasNext(); ){
      if (!((IAxis)i.next()).isAnalysis()){
	++axisCount;
      }
    }
    if (axisCount <= 1){
      try {
	resp.sendRedirect("define_error");
      } catch (IOException e){
	throw new ServletException(e);
      }
    }
  }
}

