package gov.noaa.pmel.tmap.las.luis.db;

import java.sql.SQLException;
import java.util.Vector;



/**
 * @author $Author: sirott $
 * @version $Version$
 */
public class OptionsWidget extends DbaseObject  {

  String name;
  public OptionsWidget(){
    super("OptionsWidget");
    addField("name");
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public Vector getItems() throws SQLException {
    OptionsWidgetItem item = new OptionsWidgetItem();
    return item.deserializeAggregate(oid, "options_widget_id");
  }
}
