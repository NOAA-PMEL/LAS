// $Id: DefaultTemplateBean.java,v 1.6 2002/10/25 23:57:46 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.lang.String;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import javax.servlet.ServletException;
import java.io.IOException;

public class DefaultTemplateBean implements TemplateBean {
  TemplateContext mTc;
  TemplateSession mSession;
  HttpServletResponse mResp;
  HttpServletRequest mReq;
  protected String mConstraintError = "noconstraint";
  protected FormParamHandler.Variable mHandler;

  public TemplateSession getSession() { return mSession; }

  protected void setHandler(TemplateSession session) {
    mHandler = new FormParamHandler.SingleVariable(session);
  }

  public void init(TemplateContext tc) throws ServletException, SQLException {
    mTc = tc;
    mReq = (HttpServletRequest)tc.get(Constants.REQ);
    mResp = (HttpServletResponse)tc.get(Constants.RESP);
    mSession = Utils.getSession(mReq);
    setHandler(mSession);

    Log.debug(this, "Initializing template bean");
  }

  protected void noConstraintError(HttpServletResponse resp) throws ServletException {
    noConstraintError(resp, mConstraintError);
  }
  
  protected void setConstraintError(String where){
    mConstraintError = where;
  }

  protected void noConstraintError(HttpServletResponse resp, String next)
     throws ServletException{
    try {
      resp.sendRedirect(next);
    } catch (IOException e){
      throw new ServletException(e);
    }
  }

  public boolean useTemplate() {return true; }

  public String getTemplateName() throws ServletException, SQLException {
    return getDefaultTemplateName(mTc);
  }

  static public String getDefaultTemplateName(TemplateContext tc)
    throws ServletException, SQLException {
    HttpServletRequest req = (HttpServletRequest)tc.get(Constants.REQ);
    return Utils.getDefaultTemplateName(req);
  }
}
