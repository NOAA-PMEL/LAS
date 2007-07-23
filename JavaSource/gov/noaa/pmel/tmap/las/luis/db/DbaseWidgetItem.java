package gov.noaa.pmel.tmap.las.luis.db;


/**
 * @author $Author: sirott $
 * @version $Version$
 */
public class DbaseWidgetItem extends DbaseObject implements IWidgetItem {

  String label;
  String value;

  public DbaseWidgetItem(java.lang.String tableName) {
    super(tableName);
    addField("label");
    addField("value");
  }
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public String getValue() {
    return value;
  }
}
