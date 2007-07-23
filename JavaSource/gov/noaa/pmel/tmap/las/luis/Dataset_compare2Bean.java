package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.ServletException;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import gov.noaa.pmel.tmap.las.luis.DatasetItem;
import gov.noaa.pmel.tmap.las.luis.db.*;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Dataset_compare2Bean extends Dataset_compare1Bean {
  protected void setHandler(TemplateSession session) {
    mHandler = new FormParamHandler.CompareVariable(1, session);
  }

  private long filterVariables() throws ServletException, SQLException {
    SessionObject so = mSession.getSessionObject();

    // Get info on first variable in variable list
    FormParameters var1params = so.getCompareVariables(0);
    String[] variables = var1params.get("variables");
    ICategory cat;
    try {
      cat = new Category();
      cat.deserialize(variables[0]);
    } catch (IdNotFoundException idnfe){
      cat = new DerivedCategory();
      cat.deserialize(variables[0]);
    }
    // Only get Categories which contain data on regular grids.
    Vector sourceVars = DatasetItem.getItems(cat.getParentid(),null,"regular");
    if ( sourceVars.size() > 0 ) {
       IVariableInfo sourceVinfo =
         ((DatasetItem)sourceVars.elementAt(0)).getVariableInfo();

       // Store axis types for source variable
       Set sourceAxisSet = new HashSet();
       for (Iterator i = sourceVinfo.getAxes().iterator(); i.hasNext(); ){
         IAxis axis = (IAxis)i.next();
           Log.debug(this, "Adding axis for source variable " +
                   ((DatasetItem)sourceVars.elementAt(0)).getName() +
                   ":type:" + axis.getType());
         sourceAxisSet.add(axis.getType());
       }

       // Only add variable if axes contain at least one matching axus. That is,
       // the intersection of the axes should be non-null
       for (Iterator i = mDatasets.iterator(); i.hasNext(); ){
         DatasetItem di = (DatasetItem)i.next();
         di.setComparable(false);
         IVariableInfo vinfo = di.getVariableInfo();
         for (Iterator itemItr = vinfo.getAxes().iterator(); itemItr.hasNext(); ){
           IAxis axis = (IAxis)itemItr.next();
           if (sourceAxisSet.contains(axis.getType())){
             di.setComparable(true);
             break;
           }
         }
       }
    }
       mHandler.setDatasets(mDatasets);
       return (long) mDatasets.size();
  }

  protected void doVariableInit(TemplateContext tc)
    throws ServletException, SQLException {

    super.doVariableInit(tc);

    HttpServletResponse resp = tc.getServletResponse();

    long num = filterVariables();
    if ( num == 0 ) {
       Log.debug(this, "doVariableInit: num=filterVars == 0");
       try {
           resp.sendRedirect("dataset_compare2");
           return;
       }
       catch ( java.io.IOException e ) {
          throw new ServletException ("Failed to redirect to dataset_compare2.");
       }
    }
  }

  protected void doCategoryInit(TemplateContext tc) throws ServletException, SQLException {
    super.doCategoryInit(tc);

    HttpServletResponse resp = tc.getServletResponse();

    if (((DatasetItem)mDatasets.elementAt(0)).isVariable()){
      if (mDatasets.size() > 0){
	long num = filterVariables();
        if ( num == 0 ) {
           Log.debug(this, "doCategoryInit: num=filterVars == 0");
           try {
              resp.sendRedirect("dataset_compare2");
              return;
           }
           catch ( java.io.IOException e ) {
              throw new ServletException ("Failed to redirect to dataset_compare2.");
           }
        }
      }
    } else {
      Log.debug(this, "doCategoryInit: mDatasets.size()=0");
      mHandler.setDatasets(mDatasets);
    }
  }

}
