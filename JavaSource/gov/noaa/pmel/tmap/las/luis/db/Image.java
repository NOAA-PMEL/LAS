// $Id: Image.java,v 1.4 2002/10/13 00:05:24 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;

import javax.servlet.ServletException;
import java.sql.SQLException;


/**
 * @author $Author: sirott $
 * @version $Version$
 */

public class Image extends DbaseObject {
  byte[] image;

  public Image() {
    super("Images");
    addField("image");
  }

  public String getUrl() throws ServletException {
    return "../servlets/livemap_image?" + this.getOid();
  }

  public byte[] getImage() throws SQLException {
    return image;
  }
}
