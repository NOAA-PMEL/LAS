// $Id: IDbaseObject.java,v 1.1 2002/10/25 23:57:47 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;

import java.lang.String;
import java.sql.SQLException;


public interface IDbaseObject {
  public String getOid();
  public void deserialize(String id) throws SQLException;
}
