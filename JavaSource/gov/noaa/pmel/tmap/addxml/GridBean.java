package gov.noaa.pmel.tmap.addxml;

import java.util.*;

import org.jdom.Element;

/**
 * <p>Title: addXML</p>
 *
 * <p>Description: Reads local or OPeNDAP netCDF files and generates LAS XML
 * configuration information.</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: NOAA/PMEL/TMAP</p>
 *
 * @author RHS
 * @version 1.0
 */
public class GridBean extends LasBean{
  private Vector axes;

  public GridBean() {
  }

  public void setAxes(Vector axes) {
    this.axes = axes;
  }

  public Vector getAxes() {
    return axes;
  }

  public Element toXml() {
    Element grid = new Element(this.getElement());
    Iterator ait = axes.iterator();
    while (ait.hasNext()) {
      AxisBean ab = (AxisBean)ait.next();
      Element link = new Element("link");
      link.setAttribute("match","/lasdata/axes/"+ab.getElement());
      grid.addContent(link);
    }
    return grid;
  }
}
