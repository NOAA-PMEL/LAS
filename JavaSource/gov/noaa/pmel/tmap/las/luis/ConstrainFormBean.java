// $Id: ConstrainFormBean.java,v 1.8 2004/11/16 17:16:24 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
import gov.noaa.pmel.tmap.las.luis.db.Category;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.SQLException;

public class ConstrainFormBean extends AbstractFormBean {

  public boolean isValid(String nextUrl) {
    return true;
  }

  public void handle() throws ServletException, SQLException {
    TemplateSession session = Utils.getSession(mReq);
    FormParameters state = session.getSessionObject().getConstrainState();
    if (state == null){
      state = getParameters();
      session.getSessionObject().setConstrainState(state);
    } else {
				// Remove old "apply" checkboxes from
                                // constraints
      for (int i=0; i < Constants.MAX_CONSTRAINTS; ++i){
	state.remove("constrain" + i +"_apply");
      }
      state.add(getParameters());
    }
    // Initialize the OptionsForm as well...
    getSession().getSessionObject().setOptions(getParameters());
  }

  public String nextURL() throws ServletException, SQLException {
    String[] action = getParameters().get("action");
    String tname = Utils.getFormURI(mReq);
    String root = "";
    if (tname.indexOf("compare") >= 0){
      root = "_compare";
    }
    if (action!=null && action[0].equals("changeView")){
      return "constrain" + root;
    } else {
      return "data" + root;
    }
  }

}
