// $Id: MapgenBean.java,v 1.10 2004/11/02 19:06:49 callahan Exp $
package gov.noaa.pmel.tmap.las.luis;
import gov.noaa.pmel.tmap.las.luis.db.*;
import gov.noaa.pmel.tmap.las.luis.map.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.SQLException;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.lang.Double;
import java.lang.NumberFormatException;
import java.awt.geom.Rectangle2D;

/**
 *
 * <p>Title: LAS template bean that implements server side map functionality </p>
 * <p>Copyright: Copyright (c) 2003 Joe Sirott</p>
 * @author Joe Sirott
 * @version 1.0
 * <p/>Arguments are passed to this bean in the query portion of the URI. The
 * query consists of an
 * <b>op</b> argument that describes the server side map function followed by
 * one or more op dependent arguments.<p/>The following operations are supported:
 * <ul>
 * <li><b>pan</b> pan the map to a new location. Args: </li>
 * <ul>
 * <li><b>dir</b> pan direction = left|right|up|down
 * </ul>
 * <li><b>zoomin</b> zoom in one level.</li>
 * <li><b>zoomout</b> zoom out one level. Args:  </li>
 * <li><b>settdomain</b> set the map tool domain using world (lat/lon) coordinates. Args: </li>
 *   <ul>
 *   <li><b>xlo</b>
 *   <li><b>xhi</b>
 *   <li><b>ylo</b>
 *   <li><b>yhi</b>
 *   </ul>
 * <li><b>setidomain</b> set the map tool domain using image coordinates. Args: </li>
 *   <ul>
 *   <li><b>x</b>
 *   <li><b>y</b>
 *   </ul>
 * <li><b>firstpt</b> set the first point of a two point operation. Args: </li>
 *   <ul>
 *   <li><b>x</b>
 *   <li><b>y</b>
 *   </ul>
 * <li><b>restdomain</b> restrict the map to the given world coordinates. Args: </li>
 *   <ul>
 *   <li><b>xlo</b>
 *   <li><b>xhi</b>
 *   <li><b>ylo</b>
 *   <li><b>yhi</b>
 *   </ul>
 * <li><b>seltool</b> select the current map cursor display mode. Args: </li>
 *   <ul>
 *   <li><b>type</b> the tool type = xy|x|y|pt
 *   </ul>
 * <li><b>gettdomain</b> return (as XML) the current map tool domain.</li>
 * </ul>
 */
public class MapgenBean extends DefaultTemplateBean {

  static public class MapgenBeanStuff {
    MapGenerator gen;
    int currentMarkerMode;

    MapgenBeanStuff(MapGenerator gen){
      this.gen = gen;
      this.currentMarkerMode = MapGenerator.MODE_XY;
    }
  }

  void setFirst(MapgenBeanStuff stuff, boolean isFirst){
    if (isFirst){
      stuff.gen.setMarkerMode(MapGenerator.MODE_CROSS);
    } else {
      stuff.gen.setMarkerMode(stuff.currentMarkerMode);
    }
  }

  /**
   * Process the map server request
   * @param tc template context
   * @throws ServletException
   * @throws SQLException
   */
  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);
    HttpServletRequest req = tc.getServletRequest();
    HttpServletResponse resp = tc.getServletResponse();
    resp.setContentType("image/jpeg");

    try {

      String qstring = req.getQueryString();
      Log.debug(this, "MapgenBean init with this query string: "+qstring);

      OutputStream writer = resp.getOutputStream();
      String op = req.getParameter("op");

      SessionTemplateContext sessionContext = getSession().getSessionContext();
      MapgenBeanStuff stuff = (MapgenBeanStuff)sessionContext.get("mapgen");
      if (stuff == null){
	LiveMap livemap = sessionContext.getLivemap();
	Image image = livemap.getCurrentImage();
	byte[] image_data = image.getImage();
	stuff = new MapgenBeanStuff(new MapGenerator(image_data,320,160));
	try {
	  String sxlo = livemap.getXlo();
	  String sxhi = livemap.getXhi();
	  String sylo = livemap.getYlo();
	  String syhi = livemap.getYhi();
	  double xlo = Double.parseDouble(sxlo);
	  double xhi = Double.parseDouble(sxhi);
	  double ylo = Double.parseDouble(sylo);
	  double yhi = Double.parseDouble(syhi);
          Log.debug(this, "image coords: "+xlo+", "+xhi+", "+ylo+", "+yhi);
	  stuff.gen.setImageCoordinates(xlo,xhi,ylo,yhi);
	} catch (Exception e){
	  throw new ServletException(e.getMessage());
	}
	sessionContext.put("mapgen",stuff);
      }
      MapGenerator gen = stuff.gen;
      if (op != null){
        if (op.equals("none")) {
           // Leave the map as it is
	} else if (op.equals("pan")){
	  String dir = req.getParameter("dir");
	  if (dir == null){
	    throw new IOException("Pan missing dir parameter");
	  }
	  if (dir.equals("right")){
	    gen.setPan(MapGenerator.RIGHT);
	  } else if (dir.equals("up")){
	    gen.setPan(MapGenerator.UP);
	  } else if (dir.equals("down")){
	    gen.setPan(MapGenerator.DOWN);
	  } else if (dir.equals("left")){
	    gen.setPan(MapGenerator.LEFT);
	  } else {
	    throw new ServletException("Unknown pan direction:" + dir);
	  }
	} else if (op.equals("zoomin")){
	  gen.zoomIn();
	} else if (op.equals("zoomout")){
	  gen.zoomOut();
	} else if (op.equals("settdomain")){
	  setFirst(stuff, false);
	  String sxlo = req.getParameter("xlo");
	  String sxhi = req.getParameter("xhi");
	  String sylo = req.getParameter("ylo");
	  String syhi = req.getParameter("yhi");
	  if (sxlo == null || sxhi == null ||
	      sylo == null || syhi == null){
	    throw new ServletException("Invalid parameter to settdomain");
	  }

	  try {
	    double xlo = Double.parseDouble(sxlo);
	    double xhi = Double.parseDouble(sxhi);
	    double ylo = Double.parseDouble(sylo);
	    double yhi = Double.parseDouble(syhi);
	    gen.setMarkerCoordinates(xlo,xhi,ylo,yhi);
	  } catch (NumberFormatException e){
	    throw new ServletException(e);
	  }
	} else if (op.equals("setidomain")){
	  setFirst(stuff, false);
	  String sxlo = req.getParameter("x");
	  String sylo = req.getParameter("y");
	  if (sxlo == null || sylo == null){
	    throw new ServletException("Invalid parameter to setidomain");
	  }

	  try {
	    double x = Double.parseDouble(sxlo);
	    double y = Double.parseDouble(sylo);
	    gen.setLastMarkerCoordinateFromImage(x,y);
	  } catch (NumberFormatException e){
	    throw new ServletException(e);
	  }
	} else if (op.equals("firstpt")){
	  setFirst(stuff,true);
	  String sxlo = req.getParameter("x");
	  String sylo = req.getParameter("y");
	  if (sxlo == null || sylo == null){
	    throw new ServletException("Invalid parameter to firspt");
	  }
	  try {
	    double xlo = Double.parseDouble(sxlo);
	    double ylo = Double.parseDouble(sylo);
	    gen.setMarkerCoordinatesFromImage(xlo,xlo,ylo,ylo);
	  } catch (NumberFormatException e){
	    throw new ServletException(e);
	  }
	} else if (op.equals("restdomain")){
	  setFirst(stuff, false);
	  String sxlo = req.getParameter("xlo");
	  String sxhi = req.getParameter("xhi");
	  String sylo = req.getParameter("ylo");
	  String syhi = req.getParameter("yhi");
	  if (sxlo == null || sxhi == null ||
	      sylo == null || syhi == null){
	    throw new ServletException("Invalid parameter to restdomain");
	  }

	  try {
	    double xlo = Double.parseDouble(sxlo);
	    double xhi = Double.parseDouble(sxhi);
	    double ylo = Double.parseDouble(sylo);
	    double yhi = Double.parseDouble(syhi);
	    gen.setRestrictedCoordinates(xlo,xhi,ylo,yhi);
	  } catch (NumberFormatException e){
	    throw new ServletException(e);
	  }
	} else if (op.equals("seltool")){
	  setFirst(stuff, false);
	  String type = req.getParameter("type");
	  if (type == null){
	    throw new ServletException("Missing seltool type parameter");
	  }
	  type = type.toLowerCase();
	  if (type.equals("xy")){
	    gen.setMarkerMode(MapGenerator.MODE_XY);
	    stuff.currentMarkerMode = MapGenerator.MODE_XY;
	  } else if (type.equals("x")){
	    gen.setMarkerMode(MapGenerator.MODE_X);
	    stuff.currentMarkerMode = MapGenerator.MODE_X;
	  } else if (type.equals("y")){
	    gen.setMarkerMode(MapGenerator.MODE_Y);
	    stuff.currentMarkerMode = MapGenerator.MODE_Y;
	  } else if (type.equals("pt")){
	    gen.setMarkerMode(MapGenerator.MODE_PT);
	    stuff.currentMarkerMode = MapGenerator.MODE_PT;
	  } else {
	    throw new ServletException("Invalid seltool type parameter");
	  }
	} else if (op.equals("gettdomain")){
	  Rectangle2D r = gen.getMarkerCoordinates();
	  double xlo = r.getX(); double xhi = r.getX() + r.getWidth();
	  double ylo = r.getY(); double yhi = r.getY() + r.getHeight();
	  resp.setContentType("text/xml");
	  java.io.PrintStream stream = new java.io.PrintStream(writer);
	  stream.println("<?xml version='1.0' ?>");
	  stream.println("<domain>");
	  stream.println("<x low='" + xlo + "'" +
			 " high='" + xhi + "'/>");
	  stream.println("<y low='" + ylo + "'" +
			 " high='" + yhi + "'/>");
	  stream.println("</domain>");
	  stream.flush();
	  return;
	} else {
	  throw new IOException("Unknown op:" + op);
	}
      }

      gen.write(writer);

    } catch (IOException ioe) {
      throw new ServletException(ioe);
    }
  }

  public boolean useTemplate() {return false; }

}
