package gov.noaa.pmel.tmap.las.luis.db;

import java.util.Vector;
import java.text.DecimalFormat;

/**
 *
 * @author $Author: rhs $
 * @version $Revision: 1.1 $
 */

public class PathIndex extends DbaseObject {


  String path;

  public PathIndex() {
      super("PathIndex");
      addField("path");
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

}
