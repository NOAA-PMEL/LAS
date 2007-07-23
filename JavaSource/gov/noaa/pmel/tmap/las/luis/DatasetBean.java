// $Id: DatasetBean.java,v 1.19.4.1 2005/08/08 15:26:53 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.util.Properties;
import java.util.Vector;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import gov.noaa.pmel.tmap.las.luis.db.*;
import org.apache.velocity.*;
import java.sql.SQLException;


public class DatasetBean extends DefaultTemplateBean {
  Vector mDatasets;

  protected void doVariableInit(TemplateContext tc) throws ServletException, SQLException {
    String pname = "variables";
    FormParameters params = mHandler.getVariables();
    if (params == null){
      throw new ServletException("Missing variables session parameter");
    }
    String[] variables = params.get(pname);

    // Try to deserialize both Category and DerivedCategory
    ICategory cat;
    try {
      cat = new Category();
      cat.deserialize(variables[0]);
    } catch (IdNotFoundException nocate){
      cat = new DerivedCategory();
      cat.deserialize(variables[0]);
    }

    mDatasets = DatasetItem.getItems(cat.getParentid());
    Vector derivedItems =
      DatasetItem.getDerivedItems(getSession(), cat.getParentid());
    mDatasets.addAll(derivedItems);

    mHandler.setDatasets(mDatasets);
    Log.debug(this, "Restored variables page for id:" + variables[0]);
  }

  protected void doCategoryInit(TemplateContext tc) throws ServletException, SQLException {
    Vector dataset;
    String catNum = mReq.getParameter("catitem");
    mDatasets = DatasetItem.getItems(catNum);
    Log.debug(this, mDatasets.toString());
    Vector derivedItems =
      DatasetItem.getDerivedItems(getSession(), catNum);
    mDatasets.addAll(derivedItems);

    if (mDatasets.size() == 0) {
      mDatasets = mHandler.getDatasets();
    } else {
       if ( mDatasets.size() == 1 ) {
          Log.debug(this,"Dataset size == 1");
          // If there is only one element in the
          // very top list of datasets move to 
          // the next level down.
          if ( catNum == null || catNum.equals("0") ) {
             Log.debug(this,"catNum null or zero");
             mDatasets = ((DatasetItem)mDatasets.get(0)).getChildren();
          }
       }
       mHandler.setDatasets(mDatasets);
    }
	
    Log.debug(this, "Got request for catNum:" + catNum);
  }

  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);

    FormHandler fh = (FormHandler)tc.get(Constants.FORM);
    FormParameters params = mHandler.getVariables();
    if (params != null){
      fh.setFormParameters(params);
    }
    // Put up a warning about session timeouts at the beginning of the session.
    String catNum = mReq.getParameter("catitem");
    // Only show it when listing datasets...
    if ( catNum == null ) {
       HttpSession session = mReq.getSession(false);
       long created = session.getCreationTime();
       long access = session.getLastAccessedTime();
       int timeout = session.getMaxInactiveInterval();
       timeout = timeout/60;
       long interval = access - created;
       // If the last access was less that 30 seconds from the creation time
       // post a message.
       if ( interval < 3000 ) {
          Log.debug(this, "Session is new.  Set message.");
          fh.setErrorMessage("Welcome to LAS.  <br>This session will expire after "+timeout+" minutes of inactivity.");
       }
    }
				// Default is to show variables, need to hide 
    tc.put("hide_variables", new Boolean(true));
    Log.debug(this, "Initializing DatasetBean");
    if (mReq.getParameter("lastvar") != null){
      Log.debug(this, "Initializing variable");
      doVariableInit(tc);
    } else {
      Log.debug(this, "Initializing dataset");
      doCategoryInit(tc);
    }
  }

  public String getTemplateName() throws ServletException, SQLException {
    String rname = super.getTemplateName();
    if (mDatasets.size() > 0){
      DatasetItem item = (DatasetItem)mDatasets.elementAt(0);
      if (item.isVariable()){
	rname = rname.replaceAll("dataset", "variable");
      } else if (item.isCategory()){
	;			// Do nothing
      } else {
	throw new SQLException("Invalid dataset item type: " +
			    item.getType());
      }
    } else {
      return rname.replaceAll("dataset", "variable");
    }
    return rname;
  }
}




