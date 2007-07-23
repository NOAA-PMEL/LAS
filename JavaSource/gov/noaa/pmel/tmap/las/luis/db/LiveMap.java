package gov.noaa.pmel.tmap.las.luis.db;

import java.net.URL;
import java.net.MalformedURLException;
import javax.servlet.ServletException;
import java.sql.SQLException;



/**
 * @author $Author: rhs $
 * @version $Version$
 */
public class LiveMap extends DbaseObject {

  String image_id;
  Image live_image;
  String xlo;
  String xhi;
  String ylo;
  String yhi;

  public LiveMap() {
    super("LiveMap");
    addField("image_id");
    addField("xlo");
    addField("xhi");
    addField("ylo");
    addField("yhi");
  }

  protected void getImage() throws SQLException  {
    live_image = new Image();
    live_image.deserialize(image_id);
  }

  private void update() throws ServletException, SQLException {
    if (live_image == null){
      getImage();
    }
  }

  public int getImageID() {
     return Integer.valueOf(image_id).intValue();
  }

  public Image getCurrentImage() throws ServletException, SQLException {
    update();
    return live_image;
  }

  public String getImageUrl() throws ServletException, SQLException {
    update();
    
    URL url;
    try {
      url = new URL(live_image.getUrl());
    } catch (MalformedURLException e){
      return live_image.getUrl();
    }
    String proto = url.getProtocol();
    if (proto.startsWith("http") ||
	proto.startsWith("ftp")){
      return url.toString();
    }
    throw new ServletException("Bad image URL");
  }
  public void setXlo(String xlo) {
    this.xlo = xlo;
  }
  public String getXlo() {
    return xlo;
  }
  public void setXhi(String xhi) {
    this.xhi = xhi;
  }
  public String getXhi() {
    return xhi;
  }
  public void setYlo(String ylo) {
    this.ylo = ylo;
  }
  public String getYlo() {
    return ylo;
  }
  public void setYhi(String yhi) {
    this.yhi = yhi;
  }
  public String getYhi() {
    return yhi;
  }
  public java.util.Vector getWidgets() throws java.sql.SQLException {
    LiveMapRegionMenu obj = new LiveMapRegionMenu();
    return obj.deserializeAggregate(oid, "livemapid");
  }
}
