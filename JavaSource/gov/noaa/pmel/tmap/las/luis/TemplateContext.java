package gov.noaa.pmel.tmap.las.luis;
import java.util.*;
import java.io.*;
import org.apache.velocity.*;
import org.apache.velocity.exception.*;
import org.apache.velocity.app.*;
import org.apache.log4j.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.sql.SQLException;

/**
 * Extension of VelocityContext class that is used by LAS to store
 * parameters that will be used by the LAS Velocity templates.<br>
 * There are two contexts in LAS -- a session TemplateContext, which
 * is available to every template in a given session, and a local 
 * TemplateContext which is only available to the current template. These
 * contexts are merged into one TemplateContext before a template is 
 * parsed and evaluated.
 * @author $Author: sirott $
 * @version $Version$
 */

public class TemplateContext extends VelocityContext {
  static Properties mLayoutMap = new Properties();
  static Map mCompareMap = new HashMap();

  protected TemplateContext() throws ServletException, SQLException {
    super();
    put("context", this);
    StringWriter writer = new StringWriter();
    try {
      boolean status = Velocity.mergeTemplate("setup.vm", this, writer);
      Log.debug(this, "Read template setup.vm; status = " + status);
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  public static TemplateContext getInstance() throws ServletException, SQLException {
    return new TemplateContext();
  }

  public void mergeContext(TemplateContext in){
    Object[] objs = in.getKeys();
    for(int i=0; i < objs.length; ++i){
      String s = (String)objs[i];
      this.put(s, in.get(s));
    }
  }

  public void setCompareState(String template, Integer val) {
    mCompareMap.put(template, val);
  }

  public Integer getCompareState(String template){
    Integer rval = (Integer)mCompareMap.get(template);
    if (rval ==  null){
      rval = new Integer(0);
    }
    return rval;
  }

  public void setLayoutManager(String template, String managerTemplate)
    throws ServletException, SQLException {
    Log.debug(this, "Adding layout manager " + managerTemplate +
	      " for " + template);
    mLayoutMap.put(template, managerTemplate);
  }

  public String getLayoutManager(String key) {
    return mLayoutMap.getProperty(key);
  }

  public HttpServletRequest getServletRequest() {
    return (HttpServletRequest)get(Constants.REQ);
  }

  public HttpServletResponse getServletResponse() {
    return (HttpServletResponse)get(Constants.RESP);
  }

}
