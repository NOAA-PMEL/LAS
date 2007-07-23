package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.String;
import java.io.IOException;
import java.util.Enumeration;
import org.apache.oro.text.perl.Perl5Util;
import java.util.Vector;
import java.sql.SQLException;



public class FormFilter implements Filter {
  FilterConfig mConfig;

  void handleFormError(FormHandler fh,
		       FormBean bean, HttpServletRequest req,
		       HttpServletResponse resp,
		       FilterChain chain)
    throws ServletException, SQLException, IOException{
    Log.debug(this,"Got form error");
    fh.setErrorMessage(bean.getErrorMessage());
    chain.doFilter(req,resp);
  }

  void handleForm(FormHandler fh,
		  HttpServletRequest req, HttpServletResponse resp,
		  FilterChain chain) throws
		  ServletException, SQLException, IOException {
    String uri = req.getRequestURI();
    Log.debug(this, "Matched form for: " + uri);
    TemplateSession session = Utils.getSession(req);
    session.storeFormParameters(req);
    FormParameters params = session.getFormParameters(req);
    fh.setFormParameters(params);
    SessionObject sobj = session.getSessionObject();


    // All forms have a "nexturl" hidden form element. This allows
    // the LAS UI to save the current form and move to a new URL
    // if a button in the navigation menu is selected. This overrides
    // nextURL from the form bean. 

    // New method added to FormBean interface that allows bean to ignore
    // nexturl param if desired
    String[] urlParam = params.get("nexturl");
    String nextUrl = null;
    if (!(urlParam == null ||  urlParam[0].equals("") ||
	  urlParam[0].equals(" "))){
      Log.debug(this, "nexturl set to:'" + urlParam[0] + "'");
      nextUrl = urlParam[0];
    }

    // Invoke the form handling bean
    FormBean bean = null;
    try {
      // Use CustomFormBean if this is a custom URL
      if (sobj.isCustom()){
	uri = "custom";
      }
      bean = FormHandler.getBean(uri); // Get the form handling bean
      if (bean == null){
	throw new ServletException("Can't find bean for URL:" + uri);
      }

      // Invoke the bean
      bean.init(req);
      if (nextUrl == null){
	nextUrl = bean.nextURL();
      }
      if (bean.isValid(nextUrl)){
	bean.handle();
	resp.sendRedirect(nextUrl);
      } else {
	handleFormError(fh,bean,req,resp,chain);
      }
    } catch(Exception e){
      throw new ServletException(e);
    }
  }

  public void doFilter(ServletRequest req, ServletResponse resp,
		       FilterChain chain) throws
		       java.io.IOException,ServletException{

    TemplateSession session = Utils.getSession(req);
    if (session == null){	// This URL not associated with session
      chain.doFilter(req,resp);
      return;
    }

    FormHandler fh = FormHandler.getInstance((HttpServletRequest)req);
    Utils.getContext(req).put(Constants.FORM, fh);
    try {
      Log.debug(this,"doFilter called");
      if (((HttpServletRequest)req).getMethod().equals("POST")){
	handleForm(fh,(HttpServletRequest)req,
		   (HttpServletResponse)resp,chain);
      } else {
	chain.doFilter(req,resp);
      }
    } catch (Exception e){
      throw new ServletException(e);
    }
  }

  public void init(FilterConfig config){
    mConfig = config;
  }

  public void destroy() {
    mConfig = null;
  }
}
