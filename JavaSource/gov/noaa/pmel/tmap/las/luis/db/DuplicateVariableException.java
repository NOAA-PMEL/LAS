// $Id: DuplicateVariableException.java,v 1.1 2002/10/25 23:57:47 sirott Exp $

package gov.noaa.pmel.tmap.las.luis.db;

import java.sql.SQLException;


public class DuplicateVariableException extends SQLException {
  public DuplicateVariableException(String s){
    super(s);
  }
}
