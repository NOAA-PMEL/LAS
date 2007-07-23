package gov.noaa.pmel.tmap.las.luis.db;

import java.lang.String;
import java.sql.SQLException;


/**
 * $Id: ICategory.java,v 1.5 2004/07/06 16:45:22 rhs Exp $
 * @author $Author: rhs $
 * @version $Version$
 */

public interface ICategory extends IDbaseObject{
  public String getParentid();
  public String getConfigid();
  public Config getConfig() throws SQLException;
  public MetaData getMetaData() throws SQLException;
  public IVariableInfo getVariableInfo() throws SQLException;
  public String getCategoryInclude();
  public String getCategoryIncludeHeader();
  public String getName();
  public void setName(String name);
  public void setType(String type);
  public String getType();
  public void setPath(String path);
  public String getPath();
  public boolean isAnalysis();
  public String getGridType();
}
