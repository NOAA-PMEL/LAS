// $Id: DatasetFormBean.java,v 1.12 2004/03/10 21:47:47 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.SQLException;

public class DatasetFormBean extends AbstractFormBean {
  
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

    // Eliminate previously stored constraints if new variable(s) selected
    if (mParams.get("variables") != null){
      FormParameters oldParams = mHandler.getVariables();
      if (oldParams != null){
	String[] oldVars = (String[])oldParams.get("variables");
	String[] newVars = (String[])mParams.get("variables");
	boolean nullit = false;
	if (oldVars.length != newVars.length){
	  nullit = true;
	} else if (oldVars.length == newVars.length){
	  for (int i=0; i < oldVars.length; ++i){
	    if (!oldVars[i].equals(newVars[i])){
	      nullit = true;
	      break;
	    }
	  }
	}
	if (nullit){
	  Log.debug(this, "Nulling region in sessionContext");
	  sessionContext.setRegion(null);
	}
      }
      mHandler.setVariables(mParams);
    }
  }


  public String nextURL() throws ServletException, SQLException {
    return "constrain";
  }

}
