// $Id: IdNotFoundException.java,v 1.1 2001/11/18 01:06:32 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;

import java.sql.SQLException;


public class IdNotFoundException extends SQLException {
  public IdNotFoundException(String mess) {
    super(mess);
  }
  public IdNotFoundException() {
    super();
  }
}
