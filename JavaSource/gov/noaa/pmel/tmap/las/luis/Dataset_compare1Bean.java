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

public class Dataset_compare1Bean extends DatasetBean {
  protected void setHandler(TemplateSession session) {
    mHandler = new FormParamHandler.CompareVariable(0, session);
  }
  protected void doVariableInit(TemplateContext tc) throws ServletException, SQLException {
    HttpServletResponse resp = tc.getServletResponse();
    String pname = "variables";
    FormParameters params = mHandler.getVariables();
    if (params == null){
      throw new ServletException("Missing variables session parameter");
    }
    String[] variables = params.get(pname);

    // Try to deserialize both Category and DerivedCategory
    ICategory cat;

    Log.debug(this," Using variables[0] = "+variables[0]+" to deserialize from DB");

    try {
      cat = new Category();
      cat.deserialize(variables[0]);
    } catch (IdNotFoundException nocate){
      cat = new DerivedCategory();
      cat.deserialize(variables[0]);
    }

    // First check to see if there are any regular data sets at all.
    int regularCount = DatasetItem.getItems(null, null, "regular").size();
    if (regularCount == 0) {
       mDatasets = DatasetItem.getItems(null, null);
       try {
           resp.sendRedirect("noregulardata");
           return;
       }
       catch ( java.io.IOException e ) {
          throw new ServletException ("Failed to redirect to noregulardata.");
       }
    }


    // Get count of all Categories for this id.
    int allDatasetCount =  DatasetItem.getItems(cat.getParentid()).size();

    // Get only Categories which contain regular grids.
    mDatasets = DatasetItem.getItems(cat.getParentid(), null, "regular");

    if ( mDatasets.size() > 0 && allDatasetCount != mDatasets.size() ) {
       FormHandler fh = (FormHandler)tc.get(Constants.FORM);
       fh.setErrorMessage("Only data sets defined on a regular grid are available in comparison mode.");
    }

    Vector derivedItems =
      DatasetItem.getDerivedItems(getSession(), cat.getParentid());
    mDatasets.addAll(derivedItems);

    // It's possible that mDatasets is empty because the previously selected
    // Category does not contain any regular grids.  If so, redirect to the
    // dataset page.
    
    if ( mDatasets.size() == 0 ) {
      try {
          resp.sendRedirect("dataset_compare1");
	  return;
      }
      catch ( java.io.IOException e ) {
         throw new ServletException ("Failed to redirect to dataset selection page.");
      }
    }

    mHandler.setDatasets(mDatasets);
    Log.debug(this, "Restored variables page for id:" + variables[0]);
  }

  protected void doCategoryInit(TemplateContext tc) throws ServletException, SQLException {
    HttpServletResponse resp = tc.getServletResponse();
    Vector dataset;
    String catNum = mReq.getParameter("catitem");

    // First check to see if there are any regular data sets at all.
    int regularCount = DatasetItem.getItems(null, null, "regular").size();
    if (regularCount == 0) {
       try {
           mDatasets = DatasetItem.getItems(null, null);
           resp.sendRedirect("noregulardata");
           return;
       }
       catch ( java.io.IOException e ) {
          throw new ServletException ("Failed to redirect to noregulardata.");
       }
    }

    // Count all Categories for this id.
    int allDatasetsCount = DatasetItem.getItems(catNum).size();

    // Get only Categories which contain regular grids.
    mDatasets = DatasetItem.getItems(catNum, null,"regular");

    if ( mDatasets.size() > 0 && allDatasetsCount != mDatasets.size() ) {
       FormHandler fh = (FormHandler)tc.get(Constants.FORM);
       fh.setErrorMessage("Only data sets defined on a regular grid are available in comparison mode.");
    }

    Vector derivedItems =
      DatasetItem.getDerivedItems(getSession(), catNum);
    mDatasets.addAll(derivedItems);
    if ( mDatasets.size() == 1 ) {
       Log.debug(this,"Dataset size == 1");
       // If there is only one element in the
       // very top list of datasets move to
       // the next level down.
       if ( catNum == null || catNum.equals("0") ) {
          Log.debug(this,"catNum null or zero");
          // We have to get only regular data sets. 
          DatasetItem top = (DatasetItem)mDatasets.get(0);
          mDatasets = DatasetItem.getItems(top.getLink(), null, "regular");
       }
    }
    if (mDatasets.size() == 0) {
      mDatasets = mHandler.getDatasets();
    } else {
      mHandler.setDatasets(mDatasets);
    }
	
    Log.debug(this, "Got request for catNum: " + catNum);
  }
}
