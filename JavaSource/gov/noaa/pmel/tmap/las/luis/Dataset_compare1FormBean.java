package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.http.*;
import javax.servlet.*;
import java.sql.SQLException;

public class Dataset_compare1FormBean extends DatasetFormBean {
  public void init(HttpServletRequest req)
    throws ServletException, SQLException {
    super.init(req);
    mHandler = new FormParamHandler.CompareVariable(0, Utils.getSession(mReq));
  }
}
