// $Id: RemoveFormBean.java,v 1.1 2004/12/17 14:56:10 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.SQLException;

import gov.noaa.pmel.tmap.las.luis.db.Category;
import gov.noaa.pmel.tmap.las.luis.db.DerivedCategory;
import gov.noaa.pmel.tmap.las.luis.db.DerivedVariableInfo;
import gov.noaa.pmel.tmap.las.luis.Log;

public class RemoveFormBean extends AbstractFormBean {
  
  protected FormParamHandler.Variable mHandler;

  public boolean isValid(String nextUrl) throws ServletException, SQLException{
    if (mReq.getParameterValues("variables") == null &&
        (nextUrl.startsWith("constrain") || nextUrl.equals("data"))){
      setErrorMessage("You must select a variable");
      return false;
    }
    return true;
  }

  public void init(HttpServletRequest req)
    throws ServletException, SQLException {
    super.init(req);
    mHandler = new FormParamHandler.SingleVariable(Utils.getSession(mReq));
  }

  public void handle() throws ServletException, SQLException {
    TemplateSession session = Utils.getSession(mReq);
    SessionTemplateContext sessionContext = session.getSessionContext();
    String sid = session.getId();

    // Remove any derived variables that were selected on the form.
    if (mParams.get("variables") != null){
        String[] newVars = (String[])mParams.get("variables");
        // Since this Variable is going away, get the id of the first
        // variable in the list of siblings and use it for the variables parameter value.
        DerivedCategory firsttodie = new DerivedCategory();
        firsttodie.deserialize(newVars[0]);
        Category parent = new Category();
        parent.deserialize(firsttodie.getParentid());
        DatasetItem parentdi = DatasetItem.getInstance(parent);
        DatasetItem first = (DatasetItem)parentdi.getChildren().get(0);
        mParams.add("variables", new String[]{first.getLink()});
        mHandler.setVariables(mParams);
        for ( int i = 0; i < newVars.length; i++ ) {
           Log.debug(this, "newVars["+i+"]="+newVars[i]);
           DerivedCategory dv = new DerivedCategory();
           dv.remove(newVars[i],"oid","and sessionid="+Utils.quote(sid));
           DerivedVariableInfo dvi = new DerivedVariableInfo();
           dvi.remove(newVars[i],"categoryid","and sessionid="+Utils.quote(sid));
        }
      }
  }


  public String nextURL() throws ServletException, SQLException {
    return "dataset";
  }

}
