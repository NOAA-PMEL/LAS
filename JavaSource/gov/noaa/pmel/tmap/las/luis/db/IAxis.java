// $Id: IAxis.java,v 1.3 2002/12/20 23:35:41 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;

import java.util.Vector;
import java.sql.SQLException;


public interface IAxis {
  public String getLo();
  public String getHi();
  public void setLo(String lo);
  public void setHi(String hi);
  public Vector getWidgets() throws java.sql.SQLException;
  public int getSize();
  public String getName();
  public void setName(String name);
  public void setUnits(String units);
  public String getUnits();
  public void setType(String type);
  public String getType();
  public void setCategory(String category);
  public String getCategory();
  public void setAnalysis(boolean isAnalysis);
  public boolean isAnalysis();
  public String getOid() throws SQLException;
  public int getLastOid() throws SQLException;
  public String getAnalysisType();
}
