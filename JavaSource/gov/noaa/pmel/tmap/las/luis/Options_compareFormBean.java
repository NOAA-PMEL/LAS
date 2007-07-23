// $Id: Options_compareFormBean.java,v 1.2 2002/06/26 18:17:53 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;

import java.sql.SQLException;
import javax.servlet.*;

public class Options_compareFormBean extends OptionsFormBean {
  public String nextURL() throws ServletException, SQLException {
    return "data_compare";
  }
}
