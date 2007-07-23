package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.String;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.sql.SQLException;
import java.io.PrintWriter;

/**
 * Implementation of servlet Filter interface that handles LAS exceptions
 * @author $Author: callahan $
 * @version $Version$
 */
public class ExceptionFilter implements Filter {
  public void init(FilterConfig config){
  }

  public void doFilter(ServletRequest req, ServletResponse resp,
		       FilterChain chain) throws
		       java.io.IOException,ServletException{
    try {
      chain.doFilter(req,resp);
    } catch (Exception e){
      PrintWriter out = new PrintWriter(resp.getOutputStream());
      out.println("<html>");
      out.println("<body>");
      out.println("<h3>LAS server error</h3>");
      out.println("<p><b>You have encountered an unexpected User Interface error!  ");
      out.println("Please <a href=\"dataset\">click here</a> to return to the Datasets page.  If this error persists please contact the LAS administrator for this site.</b></p>");
      out.println("<p><hr><p>");
      out.println("The following error occurred:<br><pre>");
      e.printStackTrace(out);
      e.printStackTrace();
      while (e instanceof ServletException){
	ServletException se = (ServletException)e;
	out.println("\nRoot cause of servlet exception was:\n");
	e = (Exception)se.getRootCause();
	e.printStackTrace(out);
	e.printStackTrace();
      }
      e.printStackTrace(out);
      e.printStackTrace();
      out.println("</pre></body>");
      out.println("</html>");
      out.flush();
    }
  }

  public void destroy() {}
}
