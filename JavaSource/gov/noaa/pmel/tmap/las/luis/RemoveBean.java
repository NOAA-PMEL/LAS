// $Id: RemoveBean.java,v 1.1 2004/12/17 14:54:47 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.util.Properties;
import java.util.Vector;
import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import gov.noaa.pmel.tmap.las.luis.db.*;
import org.apache.velocity.*;
import java.sql.SQLException;


public class RemoveBean extends DefaultTemplateBean {
  Vector mDatasets;

  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);

    HttpServletRequest req = tc.getServletRequest();
    HttpServletResponse resp = tc.getServletResponse();
    TemplateSession session = Utils.getSession(req);

    String catNum = mReq.getParameter("catitem");

    if (catNum == null) {
       throw new ServletException("Must specify a category from which to remove defined variables");
    }

    // This category ID should be the ID of the parent for these user variables.
    ICategory cat;
    try {
      cat = new Category();
      cat.deserialize(catNum);
    } catch (IdNotFoundException nocate){
       throw new ServletException("Can't find a category "+catNum+" from which to remove defined variables");
    }

    mDatasets = DatasetItem.getDerivedItems(session, catNum);

    // If for some strange reason we don't find any user defined variables
    // go back to the varaibles page.
    
    if ( mDatasets.size() == 0 ) {
       mDatasets = DatasetItem.getItems(catNum);
       mHandler.setDatasets(mDatasets);
       try {
          resp.sendRedirect("dataset?catitem=catNum");
       } catch (IOException e) {
          ;
       }
       return;
    }

    mHandler.setDatasets(mDatasets);
    Log.debug(this, "Getting user derived variables page for id:" + catNum);
    // Put up a warning about session timeouts at the beginning of the session.

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




