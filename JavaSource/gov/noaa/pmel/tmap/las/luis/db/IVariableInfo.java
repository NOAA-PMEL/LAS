// $Id: IVariableInfo.java,v 1.3 2004/12/01 18:22:00 rhs Exp $
package gov.noaa.pmel.tmap.las.luis.db;

import java.sql.SQLException;
import java.lang.String;
import java.util.Vector;


public interface IVariableInfo {
  public boolean isComposite();
  public String getDodsUrl();
  public String getUnits();
  public String getCategoryid();
  public Vector getAxes() throws SQLException;
  public IAxis getAxisByType(String type) throws SQLException;
  public UI getUI() throws SQLException;
  public Institution getInstitution() throws SQLException;
  public String getUrl1();
  public void setUrl1(String url1);
  public void setUrl2(String url2);
  public String getUrl2();
  public void setCustomUrl(String customUrl);
  public String getCustomUrl();
  public String getOid() throws SQLException;
  public int getLastOid() throws SQLException;
}
