package gov.noaa.pmel.tmap.las.luis.db;

/**
 * @author $Author: sirott $
 * @version $Version$
 */
import java.util.Vector;

abstract public class Widget extends DbaseObject {
  public Widget(java.lang.String table) {
    super(table);
  }
  abstract public Vector getItems() throws java.sql.SQLException;
}
