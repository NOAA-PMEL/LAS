package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.http.*;
import java.util.Hashtable;
import java.lang.String;

/**
 * Stores session scoped LAS info
 * @author $Author: sirott $
 * @version $Version$
 */

public class TemplateSession {
  HttpSession mHttpSession;
  static Hashtable mHttpSessionTable = new Hashtable();

  static public TemplateSession getInstance(HttpSession session)
      throws javax.servlet.ServletException {
    TemplateSession tsession =
      (TemplateSession)mHttpSessionTable.get(session.getId());
    if (tsession == null){
      tsession = new TemplateSession(session);
      mHttpSessionTable.put(session.getId(), tsession);
      Log.debug(TemplateSession.class,
		"Created new template session: id="
		+ tsession.getId());
    } else {
      Log.debug(TemplateSession.class,
		"Found template session: id="
		+ tsession.getId());
    }
    return tsession;
  }

  private TemplateSession(HttpSession s) throws javax.servlet.ServletException{
    mHttpSession = s;
    if (mHttpSession.getAttribute(Constants.URL_TO_FORM_HASH) == null){
      mHttpSession.setAttribute(Constants.URL_TO_FORM_HASH,
				new Hashtable());
    }
    // Set up TemplateContext for the session if it doesn't already
    // exist
    SessionTemplateContext sessionContext =
      (SessionTemplateContext)getAttribute(Constants.SCONTEXT);
    if (sessionContext == null){
      Log.debug(this,"Creating new session template context");
      try {
	sessionContext = new SessionTemplateContext();
      } catch (Exception e){
	throw new javax.servlet.ServletException(e);
      }
      setAttribute(Constants.SCONTEXT, sessionContext);
    }

    if (getValue(Constants.SESSION_OBJECT) == null){
      putValue(Constants.SESSION_OBJECT, new SessionObject());
    }
  }

  public SessionObject getSessionObject() {
    return (SessionObject)getValue(Constants.SESSION_OBJECT);
  }

  public SessionTemplateContext getSessionContext() {
    return (SessionTemplateContext)getAttribute(Constants.SCONTEXT);
  }

  public java.lang.String getId() {
    return mHttpSession.getId();
  }

  public java.lang.Object getAttribute(java.lang.String s){
    return mHttpSession.getAttribute(s);
  }

  public java.lang.Object getValue(java.lang.String s){
    return mHttpSession.getValue(s);
  }

  public java.util.Enumeration getAttributeNames(){
    return mHttpSession.getAttributeNames();
  }

  public java.lang.String[] getValueNames(){
    return mHttpSession.getValueNames();
  }

  public void setAttribute(java.lang.String s, java.lang.Object o){
    mHttpSession.setAttribute(s,o);
  }

  public void putValue(java.lang.String s, java.lang.Object o){
    mHttpSession.putValue(s,o);
  }

  public void removeAttribute(java.lang.String s){
    mHttpSession.removeAttribute(s);
  }

  public void removeValue(java.lang.String s){
    mHttpSession.removeValue(s);
  }

  public void invalidate(){
    mHttpSession.invalidate();
  }

  public void storeFormParameters(HttpServletRequest req){
    storeFormParameters(req, new FormParameters(req));
  }

  public void storeFormParameters(HttpServletRequest req, FormParameters params){
    String uri = req.getRequestURI();
    Hashtable urlToForm =
      (Hashtable)mHttpSession.getAttribute(Constants.URL_TO_FORM_HASH);
    urlToForm.put(uri, params);
    Log.debug(this, "Stored form parameters for " + uri + " session " +
	      getId() + " hash " + urlToForm);
  }

  public FormParameters getFormParameters(HttpServletRequest req){
    Hashtable urlToForm =
      (Hashtable)mHttpSession.getAttribute(Constants.URL_TO_FORM_HASH);
    String uri = req.getRequestURI();
    FormParameters params =
      (FormParameters)urlToForm.get(uri);
    Log.debug(this, "Retrieving form parameters for " + uri + " session " +
	      getId() + " hash " + urlToForm + " params " + params);
    return params;
  }

}
