package gov.noaa.pmel.tmap.las.luis.db;

import java.sql.SQLException;
import java.util.Vector;
import gov.noaa.pmel.tmap.las.luis.*;



/**
 * @author $Author: sirott $
 * @version $Version$
 */

public class ConstraintWidget extends DbaseObject {

  String size, style;
  public ConstraintWidget() {
    super("ConstraintWidget");
    addField("size");
    addField("style");
  }

  public String getStyle() {
    return this.style;
  }

  public String getSize() {
    return this.size;
  }

  public Vector getItems() throws SQLException {
    DbaseWidgetItem item = new ConstraintWidgetItem();
    return item.deserializeAggregate(oid, "constraint_widget_id");
  }

  static public void main(String[] args){
    try {
      ConstraintWidget widget = new ConstraintWidget();
      widget.deserialize("1");
      Vector items = widget.getItems();
    } catch (Exception e){
      e.printStackTrace();
    }
  }
}
