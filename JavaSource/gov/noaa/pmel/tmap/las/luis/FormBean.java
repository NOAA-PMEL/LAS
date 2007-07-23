package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.sql.SQLException;

/**
 * Handles forms submitted to the LAS servlet. Only forms that
 * are submitted via a HTTP POST will be handled.
 * A servlet filter  automatically routes the POST request
 * to a class that implements this interface.
 * <p>The class that implements this interface has to be named 
 * <b>&lt;template>FromBean</b> where &lt;template> is the name of the
 * Velocity template containing HTML that describes the form
 * 
 * @author $Author: sirott $
 * @version $Revision: 1.4 $
 * @see FormFilter
 */

public interface FormBean {
  /**
   * Initializes the FormBean
   * @param req the servlet requrest
   */
  public void init(HttpServletRequest req)
    throws ServletException,SQLException;

  /**
   * Validate form parameters
   * @return true if form parameters valid
   * @param nextUrl nextUrl to visit after form has been processed
   */
  public boolean isValid(String nextUrl) throws ServletException, SQLException;

  /**
   * Error message to display if form is not valid.
   * @return error message
   */
  public java.lang.String getErrorMessage() throws ServletException, SQLException;

  /**
   * Handle the form. Logic for storing any state, etc. should go here
   */
  public void handle() throws ServletException, SQLException;
  /**
   * next URL to use after the form has been processed
   * @return next URL to use after the form has been processed
   */
  public String nextURL() throws ServletException, SQLException;
  /**
   * Form parameters contained in the HTTP post
   * @return form parameters contained in the HTTP post
   */
  public FormParameters getParameters();
}
