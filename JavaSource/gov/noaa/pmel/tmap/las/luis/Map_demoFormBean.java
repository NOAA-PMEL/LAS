// $Id: Map_demoFormBean.java,v 1.2 2003/06/14 21:57:30 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.sql.SQLException;
import javax.servlet.ServletException;
import java.awt.geom.Rectangle2D;
import java.lang.Double;
import java.lang.String;
import gov.noaa.pmel.tmap.las.luis.map.*;

public class Map_demoFormBean extends ConstrainFormBean {

  public void handle() throws ServletException, SQLException {
    // Hack to get/replace x,y form values with those stored in 
    // map generator
    MapgenBean.MapgenBeanStuff stuff =
      (MapgenBean.MapgenBeanStuff)getSessionContext().get("mapgen");
    MapGenerator gen = stuff.gen;
    if (gen == null){
      throw new ServletException("My bad -- no map generator");
    }
    Rectangle2D rect = gen.getMarkerCoordinates();
    String xlo = Double.toString(rect.getX());
    String ylo = Double.toString(rect.getY());
    String xhi = Double.toString(rect.getX() + rect.getWidth());
    String yhi = Double.toString(rect.getY() + rect.getHeight());
    FormParameters state = getParameters();
    Log.debug(this, "xlo:" + xlo + ":xhi:" + xhi);
    state.add("x_lo", new String[] {xlo});
    state.add("y_lo", new String[] {ylo});
    state.add("x_hi", new String[] {xhi});
    state.add("y_hi", new String[] {yhi});
    super.handle();
  }

  public String nextURL() throws ServletException, SQLException {
    String[] action = getParameters().get("action");
    String tname = Utils.getFormURI(mReq);
    String root = "";
    if (tname.indexOf("compare") >= 0){
      root = "_compare";
    }
    if (action!=null && action[0].equals("changeView")){
      return "map_demo" + root;
    } else {
      return "data" + root;
    }
  }
}
