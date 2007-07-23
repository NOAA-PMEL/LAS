// $Id: Utils.java,v 1.10 2002/06/26 18:17:54 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.lang.String;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.oro.text.perl.Perl5Util;
import java.sql.SQLException;

public class Utils {
  static String formMarker = "form/";
  /**
   * Get the relative URI of a request (relative to the servlet context)
   * @param req servlet request object
   * @return String relative URI
   */
  public static String getRelativeURI(HttpServletRequest req) {
    String uri = req.getRequestURI();
    String cpath = req.getContextPath();
    return getRelativeURI(uri, cpath);
  }
    
  /**
   * Get the name of a TemplateBean from a URI
   * @param req servlet request object
   * @return name of TemplateBean
   */

  public static String getDefaultTemplateName(HttpServletRequest req){
    String tname = "";
    String uri = req.getRequestURI();
    int lastslash = uri.lastIndexOf("/");
    if (lastslash != -1){
      tname = uri.substring(lastslash+1) + ".vm";
    }
    return tname;
  }

  public static String getRelativeURI(String uri, String cpath) {
    String ruri = uri;
    if (cpath.startsWith(cpath)){
      int length = cpath.length();
      ruri = uri.substring(length);
    }
    return ruri;
  }

  /**
   * Get the root of a bean name from a template URL string
   * @param uri to a template template name
   * @return root name of bean
   */
  public static String getBeanRootFromURI(String uri) throws ServletException, SQLException {
    Vector v = new Vector();
    Perl5Util p5 = new Perl5Util();
    p5.split(v,"/\\//",uri);
    String name = (String)v.elementAt(v.size()-1);
    if (name.length() < 1) {
      return "";
    } // end of if ()
    
    String end = name.substring(1);
    String beg = name.substring(0,1);
    return "gov.noaa.pmel.tmap.las.luis." + beg.toUpperCase() + end;
  }

  /**
   * Instantiate a bean from a list of bean names
   * The first bean class that successfully loads is used
   * @param v vector of strings containing bean names
   * @return instantiated bean
   */
  public static Object getBean(Vector v) throws ServletException, SQLException {
    Class c = null;
    String className = null;
    for(Iterator i = v.iterator(); i.hasNext();){	
      try {
	className = (String)i.next();
	Log.debug(Utils.class,"Attempting to load class "
		  + className);
	c = Class.forName(className);
      } catch (Exception e) {
      }
    }
    if (c == null){
      return null;
    }
    try {
      return c.newInstance();
    } catch (Exception e){
      throw new ServletException(e);
    }
  }

  static public String getFormURI(HttpServletRequest req){
    return req.getRequestURI();
  }

  static public String formToRequestURI(String furi) throws ServletException{
    int index = furi.lastIndexOf(formMarker);
    if (index < 0){
      throw new ServletException("Bad form URI;" + furi);
    }
    return furi.substring(index+formMarker.length());
  }

  static public TemplateContext getContext(ServletRequest req){
    return (TemplateContext)req.getAttribute(Constants.TCONTEXT);
  }

  static public TemplateSession getSession(ServletRequest req){
    return (TemplateSession)req.getAttribute(Constants.TSESSION);
  }

  static public String join(String glue, Vector list){
    Iterator i = list.iterator();
    if (!i.hasNext()){
      return "";
    }
    StringBuffer rval = new StringBuffer((String)i.next());
    for (; i.hasNext(); ){
      rval.append(glue).append((String)i.next());
    }
    return rval.toString();
  }

  static public Vector split(String pattern, String in){
    Perl5Util re = new Perl5Util();
    Vector v = new Vector();
    re.split(v, pattern, in);
    return v;
  }

  static public String escapeXML(String in){
    Perl5Util re = new Perl5Util();
    return re.substitute("s#<#&lt;#g", in);
  }

  static public String substitute(String pattern, String in){
    Perl5Util re = new Perl5Util();
    return re.substitute(pattern, in);
  }

  static public String quote(String in){
    return "\"" + in + "\"";
  }

  static public void sendNotFound(HttpServletResponse resp){
    try {
      resp.sendError(404);
    } catch ( java.io.IOException e) {
    } // end of try-catch
    
  }
}
