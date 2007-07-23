package gov.noaa.pmel.tmap.las.luis;
import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.reflect.*;
import java.net.URL;


import org.apache.velocity.*;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;
import org.apache.velocity.app.*;
import org.apache.velocity.io.*;
import org.apache.velocity.util.*;
import org.apache.velocity.runtime.Runtime;
import java.sql.SQLException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import gov.noaa.pmel.tmap.las.luis.db.*;
import java.io.IOException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * Servlet for Live Access Server user interface.
 * <p>The LAS UI is generated from 
 * <a href="http://jakarta.apache.org/velocity/">Velocity</a> templates.<p>
 * A HTTP request to the user interface is processed in the following order:
 * <ul>
 * <li>The request is filtered by a set of servlet filters. These currently
 * include the following classes:
 * <ul>
 * <li>FormFilter (handles POST requests)
 * <li>SessionFilter (establishes and manages LAS user sessions)
 * <li>ExceptionFilter (manages errors that occur in the servlet)
 * </ul>
 * <li>The request is then sent to this class. The SessionFilter guarantees that
 * a session has already been successfully established. 
 * <li>The servlet looks for a class that implements the TemplateBean
 * interface. A naming convention is used to find the class -- find any
 * text after the last path separator in the URL, capitalize the first letter,
 * and add "Bean" to the resulting string (i.e. the URL /las/dataset results
 * in a search for DatasetBean). If the class exists, invoke a set of
 * methods on the class.
 * <li>Merge the session TemplateContext with the local TemplateContext. For
 * every UI request, a local TemplateContext is created that allows a developer
 * to store parameters that will only be available for the current template.
 * There is also a session TemplateContext that allows a developer to store
 * parameters that are available to all templates in the current session. These
 * contexts are merged (with the local TemplateContext having precedence if 
 * a namespace collision occurs).
 * <li>Invoke the Velocity template. The naming convention used to find the
 * template is to find any text after the last path separator in the URL and
 * add ".vm" to this string. The URL /las/dataset would cause invocation of
 * the template "dataset.vm"
 * </ul>
 * @author $Author: rhs $
 * @version $Version$
 * @see FormFilter
 * @see SessionFilter
 * @see ExceptionFilter
 * @see TemplateBean
 */

public class TemplateServlet extends VelocityServlet
{
    
  private static SimplePool writerPool = new SimplePool(40);
  private static String encoding = null;

  public void init() throws ServletException {
    super.init();
    encoding = Runtime.getString( Runtime.OUTPUT_ENCODING, DEFAULT_OUTPUT_ENCODING);
  }

  protected Properties loadConfiguration(ServletConfig config )
    throws IOException, FileNotFoundException
  {
    Properties p = new Properties();
    String path = config.getServletContext().getRealPath("/");
    if (path == null)
      {
	Log.error(this,
		  "SampleServlet.loadConfiguration():unable to " 
		  + "get the current webapp root.");
	path = "/";
      }

    p.setProperty( Velocity.FILE_RESOURCE_LOADER_PATH,  path );
    p.setProperty( "runtime.log", path + "velocity.log" );
    return p;
  }

  Vector getBeanClassNames(String uri) throws ServletException, SQLException {
    Vector v = new Vector();
    v.addElement(Utils.getBeanRootFromURI(uri) + "Bean");
    return v;
  }

  public boolean useTemplate() {
    return true;
  }

  TemplateBean getBean(String uri) throws ServletException, SQLException {
    Vector v = getBeanClassNames(uri);
    TemplateBean tbean = (TemplateBean)Utils.getBean(v);
    if (tbean == null){
      Log.debug(this, "No bean class found for:" + uri);
    } else {
      Log.debug(Utils.class,
		"Found bean for template " + uri);
    }
    return tbean;
  }

    /**
     * Override base VelocityServlet method to allow ExceptionFilter to
     * catch Velocity template exceptions
     */
   protected  void error( HttpServletRequest request, HttpServletResponse response, Exception cause )
        throws ServletException, IOException {
     throw new ServletException(cause);
  }
      

  /**
   * Override base VelocityServlet method to allow special handling
   * of template merging
   */

  public Template handleRequest( HttpServletRequest req,
				 HttpServletResponse resp,
				 Context ctx ) throws ServletException,
				 IOException
  {
    Log.debug(this, "Got request:" + req.getRequestURI() + ": query :"
	      + req.getQueryString());
    Template outty = null;
    try {

      TemplateContext tc = Utils.getContext(req);

      ctx.put(Constants.TCONTEXT, tc);
      tc.put(Constants.REQ, req);
      tc.put(Constants.RESP, resp);
      TemplateSession session = Utils.getSession(req);
      tc.put(Constants.TSESSION, session);

      Log.debug(this, "***Servlet path:"+ req.getServletPath());
      Log.debug(this, "***Servlet context:"+ req.getContextPath());
      String tname = DefaultTemplateBean.getDefaultTemplateName(tc);

      TemplateBean tb = getBean(req.getRequestURI());
      if (tb == null){
	Log.debug(this, "No template bean -- using default");
	tb = new DefaultTemplateBean();
      }
      tb.init(tc);
      if (tb.useTemplate()) {
	      
	tname = tb.getTemplateName();

	Log.debug(this, "Loading template:" + tname);
	try {
	  outty = getTemplate(tname);
	} catch(ResourceNotFoundException e) {
	  String missingTemplateName = "missing";
	  tc.put(Constants.TEMPLATE_NAME, tname);    
	  tc.put(Constants.BASE_NAME, tname.substring(0,tname.length()-3));    
	  if (tname.equals(missingTemplateName + ".vm")){
	    resp.sendError(HttpServletResponse.SC_NOT_FOUND,
			   req.getRequestURI());
	  } else {
            String path = req.getContextPath() + req.getServletPath();
            if ( path.indexOf("servlets") > 0 ) {
               resp.sendRedirect(path + "/dataset");
            }
            else {
               resp.sendRedirect(path + "/servlets/dataset");
            }
	  }
	  return null;
	} // end of try-catch
	tc.put(Constants.TEMPLATE_NAME, tname);    
	tc.put(Constants.BASE_NAME, tname.substring(0,tname.length()-3));    
      } // end of if ()
	    
    }
    catch (Exception e){
      throw new ServletException(e);
    }

    return outty;
  }

  protected void mergeTemplate( Template template, Context context,
				HttpServletResponse response )
    throws ServletException, SQLException, IOException {
    ServletOutputStream output = response.getOutputStream();
    VelocityWriter vw = null;

    TemplateContext tc =
      (TemplateContext)context.get(Constants.TCONTEXT);
    HttpServletRequest req = (HttpServletRequest)tc.get(Constants.REQ);
    FormHandler fh = (FormHandler)tc.get(Constants.FORM);
    TemplateSession templateSession = Utils.getSession(req);
    TemplateContext stc;
    if (templateSession == null){
      stc = new TemplateContext();
    } else {
      stc = templateSession.getSessionContext();
    }
	
    // Merge session and request contexts
    TemplateContext merged_tc = (TemplateContext)stc.clone();
    merged_tc.mergeContext(tc);
        
    try {
      vw = (VelocityWriter) writerPool.get();
            
      if (vw == null) {
	vw = new VelocityWriter( new OutputStreamWriter(output, encoding), 4*1024, true);
      } else {
	vw.recycle(new OutputStreamWriter(output, encoding));
      }
           
      LayoutManager.mergeTemplate(merged_tc, template.getName(), vw);
    } finally {
      try {
	if (vw != null) {
				/*
				 *  flush and put back into the pool
				 *  don't close to allow us to play
				 *  nicely with others.
				 */

	  vw.flush();
	  writerPool.put(vw);
	}
	// Save the last template name
	String tname = (String)tc.get(Constants.TEMPLATE_NAME);
	if (templateSession != null){
	  templateSession.getSessionObject().setLastTemplateName(tname);
	}
      } catch (Exception e) {
	// do nothing
      }
    }
  }


}




