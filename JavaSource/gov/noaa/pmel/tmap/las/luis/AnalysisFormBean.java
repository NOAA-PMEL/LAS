// $Id: AnalysisFormBean.java,v 1.9 2002/12/20 23:35:41 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import gov.noaa.pmel.tmap.las.luis.db.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

public class AnalysisFormBean extends AbstractFormBean {
  String mVarName;
  FormParamHandler.Variable mHandler;
  DerivedCategory mVar;
  List mSelectedAxes = new Vector();

  public void init(HttpServletRequest req) throws ServletException,SQLException{
    super.init(req);
    mHandler = new FormParamHandler.SingleVariable(getSession());
  }

  public boolean isValid(String nextUrl)
    throws ServletException, SQLException {
    // TODO -- validate all form parameters

    FormParameters params = getParameters();
    // Make sure there's at least one variable name
    String[] names = params.get("result");
    if (names == null || names[0] == null || names[0].equals("")){
      setErrorMessage("You must specify name for new variable");
      return false;
    }
    mVarName = names[0];
      
    // Get selected axes
    // TODO -- make sure this works with one point axes
    String[] axisSelect = params.get("axis_select");
    String[] axisTypes = {"x", "y", "z", "t"};
    for (int i=0; i < axisTypes.length; ++i){
      String axisName = axisTypes[i];
      if (axisSelect != null){
	for (int axindex=0; axindex < axisSelect.length; ++axindex){
	  if (axisSelect[axindex] != null && 
	      axisSelect[axindex].equals(axisName)){
	    String loName = axisName + "_lo";
	    String hiName = axisName + "_hi";
	    String[] loArray = params.get(loName);
	    String[] hiArray = params.get(hiName);
	    String lo = null, hi = null;
	    if (loArray != null){
	      lo = loArray[0];
	    }
	    if (hiArray != null){
	      hi = hiArray[0];
	    }
	    if (hi == null){
	      hi = lo;
	    }
	    if (lo != null){
	      mSelectedAxes.add(new AxisInfo(axisTypes[i], lo, hi));
	    }
	  }
	}
      }
    }
    if (mSelectedAxes.size() == 0){
      setErrorMessage("You must select at least one axis");
      return false;
    }
    FormParameters sessionParams = mHandler.getVariables();
    String[] variables = sessionParams.get("variables");
    if (variables == null || variables.length == 0){
      throw new IdNotFoundException();
    }

    // Error if all axes are selected
    ICategory mainCat = getSessionContext().getRegion().getCategory();
    Set allAxesSet = new HashSet();
    Set usedAxesSet = new HashSet();
    IVariableInfo mainVarInfo = mainCat.getVariableInfo();
    for (Iterator i = mainVarInfo.getAxes().iterator(); i.hasNext(); ){
      allAxesSet.add(((IAxis)i.next()).getType());
    }
    if (axisSelect != null){
      for (int i=0; i < axisSelect.length; ++i){
	if (axisSelect[i] != null){
	  usedAxesSet.add(axisSelect[i]);
	}
      }
    }
    if (usedAxesSet.containsAll(allAxesSet)){
      setErrorMessage("You cannot select all of the axes");
      return false;
    }

    String masks[] = params.get("mask");
    if (masks != null){
      for (int i=0; i < masks.length; ++i){
	if (!(masks[i].equals("n") || masks[i].equals("o") ||
	      masks[i].equals("l"))){
	  setErrorMessage("Invalid mask name:" + masks[i]);
	  return false;
	}
      }
    }
    

    String analysis_type = params.get("analysis_type")[0];
    // Create new derived category
    String sessionid = getSession().getId();
    try {
      mVar = new DerivedCategory(variables[0],
				 sessionid, mVarName, masks);
      mVar.serialize();

      IVariableInfo vi = mVar.getVariableInfo();

      for (Iterator i = mSelectedAxes.iterator(); i.hasNext(); ){
	AxisInfo axinfo = (AxisInfo)i.next();
	DerivedAxis axis = (DerivedAxis)vi.getAxisByType(axinfo.name);
	if (axis != null){
	  axis.setAnalysisType(analysis_type);
	  axis.setAnalysis(true);
	  axis.setLo(axinfo.lo);
	  axis.setHi(axinfo.hi);
	  axis.setNew(true);
	  axis.serialize();
	}
      }
    } catch (DuplicateVariableException e){
      setErrorMessage("An analysis variable named: '" + mVarName +
		      "' already exists");
      return false;
    }

    // Set currently selected variable to newly created variable
    sessionParams.add("variables", new String[]{mVar.getOid()});

    return true;
  }

  public void handle() throws ServletException, SQLException{
  }

  public String nextURL() throws ServletException, SQLException{
    return "constrain";
  }

  public static class AxisInfo {
    String name,lo,hi;
    public AxisInfo(String name, String lo, String hi){
      this.name = name;
      this.hi = hi;
      this.lo = lo;
    }
  }

}
