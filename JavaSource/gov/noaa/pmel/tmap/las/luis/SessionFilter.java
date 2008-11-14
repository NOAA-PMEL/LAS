// $Id: SessionFilter.java,v 1.33.2.1 2005/05/26 14:47:08 webuser Exp $ 
package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.String;
import java.util.Iterator;
import java.io.IOException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.sql.SQLException;
import java.util.ArrayList;
import gov.noaa.pmel.tmap.las.luis.db.*;


/**
 * Implementation of servlet Filter interface that establishes a LAS session.
 * @author $Author: webuser $
 * @version $Version$
 */

public class SessionFilter implements Filter {
  FilterConfig mConfig;

  private void initDefaultContexts(HttpServletRequest req,
           HttpServletResponse resp)
           throws ServletException, IOException{
    try {
      TemplateSession session = Utils.getSession(req);
      SessionTemplateContext sessionCtx = session.getSessionContext();
      Vector datasets = sessionCtx.getDatasets();

      // Check for dset parameter, redirect to appropriate page if
      // dset string exists in database
      //
      // This code works on XML path names...
      String dset = req.getParameter("dset");
      Log.debug(this,"Trying to find dset: "+dset);
      if (!(dset == null || dset.equals(""))){
        try {
          Category c = new Category();
          String id = "'" + dset + "'";
          c.deserialize(id,"path_name");
          if (c.getType().equals("v")) { // It's a variable
            Log.debug(this,"found a variable of name: "+dset);
            resp.sendRedirect("constrain?var=" + c.getOid());
          } else {
            Log.debug(this,"found a category of name: "+dset);
            resp.sendRedirect("dataset?catitem=" + c.getOid());
          }
            return;
        } catch (IdNotFoundException idnfe){
            Log.debug(this,"Found nothing falling through...");
          ;                     // Do nothing if not found
        }
      }

      // Check to see if a data set title and variable name are present.
      // If so try to find where the appear in the hierarchy and go from there.
      //
      String dstitle = req.getParameter("title");
      String varname = req.getParameter("varname");

      Log.debug(this,"Trying to find title: "+dstitle);

      if (!(dstitle == null || dstitle.equals("")) &&
          !(varname == null || varname.equals(""))){
         // Hack off the beginning and ending quotes if they exist.
         if ( dstitle.indexOf('"') == 0 ) {
            Log.debug(this,"Found a quote at beginning of the title.");
            dstitle = dstitle.substring(1,dstitle.length());
            Log.debug(this,"Should be stripped.  title="+dstitle);
         }
         if (dstitle.indexOf('"') == dstitle.length()-1 ) {
            Log.debug(this,"Found a quote at the end of the title.");
            dstitle = dstitle.substring(0,dstitle.length()-1);
            Log.debug(this,"Should be stripped.  title="+dstitle);
         }
         // Hack off the beginning and ending quotes if they exist.
         if ( varname.indexOf('"') == 0 ) {
            varname = varname.substring(1,varname.length());
         }
         if (varname.indexOf('"') == varname.length()-1 ) {
            varname = varname.substring(0,varname.length()-1);
         }
        try {
          Category c = new Category();
          String id = "'" + dstitle + "'";
          c.deserialize(id,"path_name"," and type='d' ");
          if ( c != null ) {
            Log.debug(this,"found a stable category of name: "+dstitle);
            DatasetItem v = c.getChildByName(varname);
            Log.debug(this, "Got variable with id="+v.getLink());
            if (v != null ) {
               IVariableInfo vi = v.getVariableInfo();
               if ( vi != null ) {
                  String vpathname = vi.getUrl1();

                  Log.debug(this, "Looking for var with path="+vpathname);
                  
                  VariableInfo vc = new VariableInfo(); 

                  // Get the first VariableInfo that has the same url1 path
                  // as the one from the query string.
                  vc.deserialize(Utils.quote(vpathname), "url1");

                  if ( vc != null ) {
                     String varid = vc.getCategoryid();
                     Log.debug(this, "Found varible with category="+varid);
                     resp.sendRedirect("constrain?var="+varid);
                     return;
                  }
                  else {
                     Log.debug(this,"VarInfo for actual category is null");
                  }
               }
               else {
                  Log.debug(this,"VariableInfo vi is null!");
               }
            }
          }
        } catch (IdNotFoundException idnfe){
            Log.debug(this,"Found nothing falling through...");
          ;                     // Do nothing if not found
        }
      }

      if (datasets == null){
        datasets = DatasetItem.getItems("0");
        sessionCtx.setDatasets(datasets);
      }
      Vector configs = sessionCtx.getConfigs();
      if (configs == null){
        configs = Config.getConfigs();
        sessionCtx.setConfigs(configs);
      }

      // If we are transitioning from single mode to compare mode,
      // we need to remove the compare datasets and variables.
      // Note: following returns the *default* template name. A TemplateBean
      // can override this. As long as the convention of using "compare" 
      // in the URL is used, the following should still work.
      String tname = Utils.getDefaultTemplateName(req);
      String lastTname = session.getSessionObject().getLastTemplateName();
      if (lastTname == null ||
          (lastTname.indexOf("compare") < 0 && tname.indexOf("compare") >= 0)){
        sessionCtx.removeCompareDatasets();
        session.getSessionObject().removeCompareVariables();
      }
      
      // Initialize comparison datasets if not already intialized
      ArrayList list = sessionCtx.getCompareDatasets();
      Vector[] compareDatasets;
      if (list == null){
        compareDatasets = new Vector[2];
      } else {
        compareDatasets = (Vector[])list.toArray(new Vector[2]);
      }
      if (compareDatasets[0] == null){
        compareDatasets[0] = (Vector)datasets.clone();
      }
      if (compareDatasets[1] == null){
        compareDatasets[1] = (Vector)compareDatasets[0].clone();
      }
      sessionCtx.setCompareDatasets(compareDatasets);
      
      // Set up LAS title
      DatasetItem firstVar
        = DatasetItem.getInstance(DatasetItem.getFirstVarId());
      String title =
        firstVar.getCategory().getVariableInfo().getUI().getTitle();
      sessionCtx.setTitle(title);

      // Initialize single variable if not already initialized
      FormParameters vars = session.getSessionObject().getVariables();
      if (vars == null){
        vars = new FormParameters();
        vars.add("variables", new String[]{DatasetItem.getFirstVarId()});
        session.getSessionObject().setVariables(vars);
      }
      // Initialize comparison variables if not already intialized
      // Only use first variable if multiple variables selected
      FormParameters compareVars1 =
        session.getSessionObject().getCompareVariables(0);
      if (compareVars1 == null){
        compareVars1 = new FormParameters();
        String[] newvars = new String[] {((String[])vars.get("variables"))[0]};
        compareVars1.add("variables", newvars);
        session.getSessionObject().setCompareVariables(0, compareVars1);
      }

      FormParameters compareVars2 =
        session.getSessionObject().getCompareVariables(1);
      if (compareVars2 == null){
        compareVars2 = compareVars1;
        session.getSessionObject().setCompareVariables(1, compareVars2);
      }

      Vector options = sessionCtx.getOptions();
      if ( options == null ) {
         options = new Vector();
      }

      sessionCtx.setOptions(options);

      MapStateBean mapState = sessionCtx.getMapState();
      if ( mapState == null ) {
         mapState = new MapStateBean();
      }

      sessionCtx.setMapState(mapState);

      // Set use_java
      Boolean use_java = sessionCtx.getUseJava();
      if (use_java == null){
        use_java = new Boolean(true);
      }
      String use_java_param = req.getParameter("use_java");
      if (use_java_param != null){
        Log.debug(this, "use_java: got param:" + use_java_param);
        use_java = new Boolean(use_java_param);
      }
      Log.debug(this, "use_java=" + use_java);
      sessionCtx.setUseJava(use_java);

      // Set force_java
      Boolean force_java = sessionCtx.getForceJava();
      if (force_java == null){
        force_java = new Boolean(false);
      }
      String force_java_param = req.getParameter("force_java");
      if (force_java_param != null){
        Log.debug(this, "use_java: got param:" + use_java_param);
        force_java = new Boolean(force_java_param);
      }
      Log.debug(this, "force_java=" + force_java);
      sessionCtx.setForceJava(force_java);

      // The applet is not being forced on and the assertion is that
      // the applet will work.  We will detect the browser (once) and if we
      // cannot find a match (either a direct match or an good guess
      // that it is IE on Windows) then we will set the state to
      // fail over to the server-side map.  BrowserChecked let us
      // know if we've already checked this browser. Bugzilla 744.
      Boolean browser_checked = sessionCtx.getBrowserChecked();
      if (browser_checked == null){
         browser_checked = new Boolean(false);
      }
      if (!force_java.booleanValue() && 
           use_java.booleanValue() && 
          !browser_checked.booleanValue()) {

         Browser browser = new Browser();
         browser.init(req);
         try {
            if ( ((browser.isIE() && browser.isWin()) ||
                  (browser.isMozilla() && browser.isWin()) ||
                   browser.isCompatible()) && !browser.isRejected() ) {
               Log.debug(this,"Decided that this browser is OK!");
               Log.debug(this,"isIE = "+browser.isIE());
               Log.debug(this,"isMozilla = "+browser.isMozilla());
               Log.debug(this,"isWin = "+browser.isWin());
               Log.debug(this,"isCompatible = "+browser.isCompatible());
               Log.debug(this,"isRejected = "+browser.isRejected());
               sessionCtx.setUseJava(new Boolean(true));
            }
            else {
               Log.debug(this,"Decided that this browser is NOT ok!");
               Log.debug(this,"isIE = "+browser.isIE());
               Log.debug(this,"isMozilla = "+browser.isMozilla());
               Log.debug(this,"isWin = "+browser.isWin());
               Log.debug(this,"isCompatible = "+browser.isCompatible());
               Log.debug(this,"isRejected = "+browser.isRejected());
               sessionCtx.setUseJava(new Boolean(false));
            }
         } catch (SQLException e) {
            Log.error(this, e.getMessage());
         }
         sessionCtx.setBrowserChecked(new Boolean(true));
      }


       // Set UI Version ui_version
       String ui_version = sessionCtx.getUiVersion();
       if (ui_version == null) {
          ui_version = "LAS v6 UI/LAS v7.0";
       }
       sessionCtx.setUiVersion(ui_version);

       // Set pure_html flag
       Boolean pure_html = sessionCtx.getPureHtml();
       if (pure_html == null) {
          pure_html = new Boolean(false);
       }
       sessionCtx.setPureHtml(pure_html);

      // Set new_output_window
      Boolean new_output_window = sessionCtx.getNewOutputWindow();
      if (new_output_window == null){
        // Default to always send data to a single new window.
        new_output_window = new Boolean(true);
      }
      String new_output_window_param = req.getParameter("new_output_window");
      if (new_output_window_param != null){
        Log.debug(this, "new_output_window: got param:"
                  + new_output_window_param);
        new_output_window = new Boolean(new_output_window_param);
      }
      Log.debug(this, "new_output_window=" + new_output_window);
      sessionCtx.setNewOutputWindow(new_output_window);

      // If it has not been set in the context, set it.
      String new_output_window_name = sessionCtx.getNewOutputWindowName();
      if (new_output_window_name == null){
         // Default to using the same window for each output.
         new_output_window_name = "data";
      }

      // If it appears as a parameter, replace it with new value
      String new_output_window_name_param = req.getParameter("new_output_window_name");
      if (new_output_window_name_param != null){
        new_output_window_name = new_output_window_name_param;
      }
      sessionCtx.setNewOutputWindowName(new_output_window_name);

      // Set popup_data
      PopupData popup_data = sessionCtx.getPopupData();
      if (popup_data == null){
        popup_data = new PopupData();
      }
      sessionCtx.setPopupData(popup_data);

      // Set browser
      Browser browser = new Browser();
      browser.init(req);
      Log.debug(this, "Browser is: " + browser.getBrowser());
      sessionCtx.setBrowser(browser);

    } catch (SQLException e){
        throw new ServletException(e);
    } 
  }

  // Exclude some URLS from session check
  boolean isExcludedUrl(HttpServletRequest req){
    String reqUri = req.getRequestURI();
    if (reqUri.endsWith("livemap_image")) {
      return true;
    }
    return false;
  }


  // Exclude some URLS from forced page expiration
  boolean isCachedUrl(HttpServletRequest req){
    String reqUri = req.getRequestURI();
    if (reqUri.endsWith("search") ||
        reqUri.endsWith("metadata") ||
        reqUri.endsWith("data_popup") ||
        reqUri.endsWith("livemap_image")){
      return true;
    }
    return false;
  }


  // Make a fake session for the thredds and index template.
  boolean needsFakeContext (HttpServletRequest req){
    String reqUri = req.getRequestURI();
    if (reqUri.endsWith("thredds") ||
        reqUri.endsWith("browser") ||
        reqUri.endsWith("index")){
      return true;
    }
    return false;
  }

  boolean checkSession(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException{

    	ResourceBundle bundle = null;
    	try {
    		bundle = ResourceBundle.getBundle("las");
    		String host = (String) bundle.getObject("las.db.host");
    		if ( host != null && host.equals("dummy_value") ) {
    			resp.sendRedirect("../");
                        return false;
    		}
    	} catch (MissingResourceException e) {
    			resp.sendRedirect("../");
                        return false;
    	}
      String req_url = req.getServletPath();
      if ( req_url.contains("servlets") ) {
    	  resp.sendRedirect("../");
          return false;
      }

    if (isExcludedUrl(req)){
      setupTemplateContext(req);
      return true;
    }

    if (needsFakeContext(req)){

       Log.debug(this, "Setting up a fake context for the thredds template.");
       HttpSession session = req.getSession(true);
       TemplateSession ts = TemplateSession.getInstance(session);
       req.setAttribute(Constants.TSESSION, ts);

       // Set up TemplateContext for the request. New instantiation for
       // each request
       TemplateContext tc = setupTemplateContext(req);

       initDefaultContexts(req,resp);
       return true;
    }

    // Make sure session (if it is exists) is valid
    Cookie[] cookies = req.getCookies();
    if (cookies !=null && !req.isRequestedSessionIdValid()){
      Log.debug(this, "Invalid session");
      HttpSession session = req.getSession(true);
      session.setAttribute("previous_expired", new Boolean(true));
      //resp.sendRedirect("../invalidsession.html");
      String query = req.getQueryString();
      if (query == null){
         resp.sendRedirect("dataset");
      } else {
         resp.sendRedirect("dataset"+"?"+query);
      }
      return false;
    }

    HttpSession session = req.getSession(true);
    if (session.isNew() || session.getAttribute("previous_expired") != null){
      session.removeAttribute("previous_expired");
      // If the session is new and the cookie check parameter is
      // present, then the browser doesn't support cookies
      if (req.getParameter("cookieCheck") != null){
        Log.debug(this, "Browser doesn't support cookies");
        resp.sendRedirect("../nocookies.html");
        return false;
      }
      // If the session is new, then we need to verify that the
      // browser supports cookies. We do this with a redirect
      // with the cookieCheck parameter set

      String query = req.getQueryString();
      if (query == null){
        Log.debug(this, "Active cookie check without query string redir to "
                  + req.getRequestURI() + "?cookieCheck=1");
        resp.sendRedirect(req.getRequestURI() + "?cookieCheck=1");
      } else {
        Log.debug(this, "Active cookie check with query string redir to "
                  + req.getRequestURI() + query + "?cookieCheck=1");
        resp.sendRedirect(req.getRequestURI() + "?" +
                          query + "&cookieCheck=1");
      }
      return false;
    }

    // Redirect without cookieCheck parameter if present
    if (req.getParameter("cookieCheck") != null){
      String uri = req.getRequestURI();
      int count = 0;
      for (Enumeration e = req.getParameterNames();
           e.hasMoreElements(); ){
        String name = (String)e.nextElement();
        if (!name.equals("cookieCheck")){
          String values[] = req.getParameterValues(name);
          if (count > 0){
            uri = uri + '&';
          } else {
            uri = uri + '?';
          }
          uri = uri + name + "=" + values[0];
          for (int i=1; i < values.length; i++){
            uri = uri + ";" + values[i];
          }
          ++count;
        }
      }
      Log.debug(this, "Redirecting to " + uri);
      resp.sendRedirect(uri);
      return false;
    }
    TemplateSession ts = TemplateSession.getInstance(session);
    req.setAttribute(Constants.TSESSION, ts);

    // Set up TemplateContext for the request. New instantiation for
    // each request
    TemplateContext tc = setupTemplateContext(req);

    initDefaultContexts(req,resp);

    return true;
  }

  private TemplateContext setupTemplateContext(HttpServletRequest req)
    throws ServletException {
    TemplateContext tc;
    try {
      tc = TemplateContext.getInstance();
    } catch (Exception e){
      throw new ServletException(e);
    }
    req.setAttribute(Constants.TCONTEXT, tc);
    return tc;
  }

  public void doFilter(ServletRequest req, ServletResponse resp,
                       FilterChain chain) throws
                       java.io.IOException,ServletException{
    Log.debug(this,"SessionFilter called " +
              req.getClass().getName());
    HttpServletResponse hresp = (HttpServletResponse)resp;
    if (!isCachedUrl((HttpServletRequest)req)){
      // Disable client caching
      hresp.setHeader("Cache-Control","no-cache"); //HTTP 1.1
      hresp.setHeader("Pragma","no-cache"); //HTTP 1.0
      hresp.setDateHeader("Expires", System.currentTimeMillis()-3600000);
    }
    if (checkSession((HttpServletRequest)req,(HttpServletResponse)resp)){
      chain.doFilter(req,resp);
    }
  }

  public void init(FilterConfig config){
    mConfig = config;
  }

  public void destroy() {
    mConfig = null;
  }

}
