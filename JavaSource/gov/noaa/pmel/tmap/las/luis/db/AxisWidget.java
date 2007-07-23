// $Id: AxisWidget.java,v 1.3 2001/11/16 01:55:36 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      NOAA/PMEL/TMAP
 * @author Joe Sirott
 * @version 1.0
 */
import java.util.Vector;
import java.lang.String;

public class AxisWidget extends Widget {
  int initial_index_lo, initial_index_hi;
  String default_type;
  public AxisWidget() {
    super("AxisWidgets");
    addField("initial_index_lo");
    addField("initial_index_hi");
    addField("default_type");
  }
  public Vector getItems() throws java.sql.SQLException {
    AxisWidgetItem obj = new AxisWidgetItem();
    return obj.deserializeAggregate(oid, "axis_widget_id");
  }

  public int getInitialIndexLo() {
    return initial_index_lo;
  }
  public int getInitialIndexHi() {
    return initial_index_hi;
  }
  public String getDefaultType() {
    return default_type;
  }
}
