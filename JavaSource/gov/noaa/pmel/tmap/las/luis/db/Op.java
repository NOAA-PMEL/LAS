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

public class Op extends DbaseWidgetItem {

  String mode;
  String view;
  public Op() {
      super("Op");
      addField("mode");
      addField("view");
  }
  public String getMode() {
    return mode;
  }
  public void setMode(String mode) {
    this.mode = mode;
  }
  public void setView(String view) {
    this.view = view;
  }
  public String getView() {
    return view;
  }
}
