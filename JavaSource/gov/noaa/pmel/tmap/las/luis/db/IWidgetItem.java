// $Id: IWidgetItem.java,v 1.2 2002/06/26 18:17:55 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;

public interface IWidgetItem {
  public String getLabel();

  public void setLabel(String label);

  public void setValue(String value);

  public String getValue();
}
