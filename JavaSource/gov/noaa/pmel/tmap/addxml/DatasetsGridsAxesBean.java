package gov.noaa.pmel.tmap.addxml;

import java.util.*;

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
public class DatasetsGridsAxesBean {
  private Vector datasets;
  private Vector grids;
  private Vector axes;
  private String error;
  
  public DatasetsGridsAxesBean() {
      datasets = new Vector();
      grids = new Vector();
      axes = new Vector();
      error = null;
  }
  

  public void setDatasets(Vector datasets) {
    this.datasets = datasets;
  }

  public void setGrids(Vector grids) {
    this.grids = grids;
  }

  public void setAxes(Vector axes) {
    this.axes = axes;
  }

  public void setError(String error) {
    this.error = error;
  }

  public Vector getDatasets() {
    return datasets;
  }

  public Vector getGrids() {
    return grids;
  }

  public Vector getAxes() {
    return axes;
  }

  public String getError() {
    return error;
  }
}
