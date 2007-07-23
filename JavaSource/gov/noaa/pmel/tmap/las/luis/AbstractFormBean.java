// $Id: AbstractFormBean.java,v 1.9 2003/03/01 18:35:02 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Enumeration;
import java.sql.SQLException;
import javax.servlet.ServletException;

abstract public class AbstractFormBean implements FormBean {
  String mErrorMessage = "";
  FormParameters mParams;
  HttpServletRequest mReq;
  TemplateSession mSession;
  SessionTemplateContext mSessionContext;

  public void init(HttpServletRequest req)
    throws ServletException, SQLException {
    mReq = req;
    mParams = new FormParameters(req);
    mSession = Utils.getSession(mReq);
    mSessionContext = mSession.getSessionContext();
    if (mSession == null){
      throw new ServletException("Null session");
    }
    Log.debug(this,"Got session id " + mSession.getId());
  }

  abstract public boolean isValid(String nextUrl) throws ServletException, SQLException;

  public void setErrorMessage(String mess){
    mErrorMessage = mess;
  }

  public java.lang.String getErrorMessage() throws ServletException, SQLException {
    return mErrorMessage;
  }

  abstract public void handle() throws ServletException, SQLException;

  abstract public String nextURL() throws ServletException, SQLException;

  public FormParameters getParameters() {
    return mParams;
  }

  public FormHandler getFormHandler(){
    return (FormHandler)Utils.getContext(mReq).get(Constants.FORM);
  }

  public HttpServletRequest getRequest() {
    return mReq;
  }

  public TemplateSession getSession() {
    return mSession;
  }

  public SessionTemplateContext getSessionContext() {
    return mSessionContext;
  }

}
