// $Id: Livemap_imageBean.java,v 1.2 2002/10/13 00:05:23 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import gov.noaa.pmel.tmap.las.luis.db.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.SQLException;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.OutputStream;

public class Livemap_imageBean extends DefaultTemplateBean {

  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);
    HttpServletRequest req = tc.getServletRequest();
    HttpServletResponse resp = tc.getServletResponse();

    try {
      String query = req.getQueryString();
      if (query == null){
	Log.debug(this, "No query string");
	resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	return;
      }

      Log.debug(this, "Query string:" + query);
      byte[] image_data = null;
      try {
	Image image = new Image();
	image.deserialize(query);
	image_data = image.getImage();
      } catch (SQLException e){
	e.printStackTrace();
      }
      if (image_data == null){
	Log.debug(this, "Image string null");
	resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	return;
      }
      OutputStream writer = resp.getOutputStream();
      writer.write(image_data);
      writer.flush();
    } catch (IOException ioe) {
      throw new ServletException(ioe);
    }
  }

  public boolean useTemplate() {return false; }

}
