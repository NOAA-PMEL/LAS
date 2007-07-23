package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.SQLException;

public class Dataset_compare2FormBean extends DatasetFormBean {
  public void init(HttpServletRequest req)
    throws ServletException, SQLException {
    super.init(req);
    mHandler = new FormParamHandler.CompareVariable(1, Utils.getSession(mReq));
  }

}
