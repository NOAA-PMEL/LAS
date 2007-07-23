// $Id: WidgetItem.java,v 1.4 2002/08/27 21:02:47 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;

/**
 * @author $Author: sirott $
 * @version $Version$
 */

public class WidgetItem implements IWidgetItem {

  String label;
  String value;

  public WidgetItem(String label, String value) {
    setLabel(label);
    setValue(value);
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
