package gov.noaa.pmel.tmap.las.luis.db;

/**
 * @author $Author: sirott $
 * @version $Id: View.java,v 1.6 2002/10/18 18:55:56 sirott Exp $
 */

public class View extends DbaseWidgetItem {
  String region;
  public View() {
      super("View");
      addField("region");
  }
  public String getRegion() {
    return this.region;
  }
}
