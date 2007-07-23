// $Id: RegionConstraint.java,v 1.15 2005/03/21 23:47:40 callahan Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.lang.String;
import java.lang.StringBuffer;
import java.util.Vector;
import java.util.Iterator;
import java.sql.*;
import gov.noaa.pmel.tmap.las.luis.db.*;
import javax.servlet.*;
import java.util.HashSet;

public class RegionConstraint {
  ICategory mCategory;
  ICategory[] mCategories;
  UI mUI;
  LiveMap mLiveMap;
  View mView;
  HashSet mAxesSet = new HashSet();
  HashSet mViewSet = new HashSet();
  Vector mAxes;
  Vector mViewItems;

  protected RegionConstraint(ICategory[] cats, String view) throws SQLException,
  ServletException{
    Log.debug(this, "new RegionConstraintfor: " + cats.length +
	" variables");
    mCategory = cats[0];
    mCategories = cats;
    IVariableInfo vi = mCategory.getVariableInfo();
    if (vi == null){
      throw new ServletException("Not a variable");
    }
    mAxes = vi.getAxes();
    for (Iterator i = mAxes.iterator(); i.hasNext(); ){
      IAxis a = (IAxis)i.next();
      if (a.getSize() > 1 && !a.isAnalysis()){
	mAxesSet.add(a.getType());
      }
    }
    mUI = vi.getUI();
    setView(view);
    mLiveMap = mUI.getLiveMap();
    initViewItems();
  }

  static RegionConstraint getInstance(String[] ids)
    throws SQLException,ServletException {
    return getInstance(ids, null);
  }

  static RegionConstraint getInstance(String[] ids, String view)
    throws SQLException,ServletException {
    ICategory[] theCategories = new ICategory[ids.length];
    for (int i=0; i < ids.length; ++i){
      ICategory c;
      try {
	c = new Category();
	c.deserialize(ids[i]);
      } catch (IdNotFoundException idnfe){
	try {
	  c = new DerivedCategory();
	  c.deserialize(ids[i]);
	} catch (IdNotFoundException no_derived_e){
	  throw idnfe;
	}
      }
      theCategories[i] = c;
     }
     return new RegionConstraint(theCategories, view);
  }


  public UI getUI() { return mUI; }

  public boolean hasAxis(String axis){
    return mAxesSet.contains(axis);
  }

  public boolean isRange(String axis){
    return mViewSet.contains(axis);
  }

  public LiveMap getLiveMap() {
    return mLiveMap;
  }

  public Vector getRegionItems() throws SQLException {
    Vector rval = new Vector();
    for (Iterator lmitem = mLiveMap.getWidgets().iterator();
	 lmitem.hasNext(); ){
      rval.addElement(lmitem.next());
    }
    return rval;
  }

  public IAxis getAxis(String type) throws SQLException {
    for (Iterator i = mAxes.iterator(); i.hasNext(); ){
      IAxis axis = (IAxis)i.next();
      if (axis.getType().equals(type)){
	return axis;
      }
    }
    Log.debug(this, "No axis found of type: " + type);
    return null;
  }

  public Vector getAxisWidgets(String type) throws SQLException {
    IAxis axis = getAxis(type);
    if (axis != null){
      return axis.getWidgets();
    }
    Log.debug(this, "No widgets found for axis of type: " + type);
    return null;
  }

  public String getAxisLo(String type) throws SQLException {
    Vector axisWidgets = getAxisWidgets(type);
    if (axisWidgets == null){
      return null;
    }
    Vector items = ((AxisWidget)axisWidgets.elementAt(0)).getItems();
    if (items == null){
      return null;
    }
    return ((IWidgetItem)items.elementAt(0)).getLabel();
  }

  public String getAxisHi(String type) throws SQLException {
    Vector axisWidgets = getAxisWidgets(type);
    if (axisWidgets == null){
      return null;
    }
    Vector items = ((AxisWidget)axisWidgets.elementAt(0)).getItems();
    if (items == null){
      return null;
    }
    return ((IWidgetItem)items.elementAt(items.size()-1)).getLabel();
  }

  public Vector getProducts() throws SQLException {
     //return mUI.getOps(mView.getValue());
     // Instead of just returning the list of available output products (operations)
     // we're going to filter them based on the number of variables selected.
     //
     // and we're going to filter out the gen_script operation if any of the
     // variables are user defined variables.
     //
     // When the new XML is in place, we'll want to have the min and max variable
     // arguements in the database table with the output product widgets and do
     // the filtering using a "where" clause in the select instead of after the fact.

     boolean userdefined = false;
     for ( int vit = 0; vit < mCategories.length; vit++ ) {
        ICategory icat = mCategories[vit];
        DatasetItem item = DatasetItem.getInstance(icat);
        if (item.isDerived()) {
           userdefined = true;
        }
     }

     Vector ops = mUI.getOps(mView.getValue());
     Vector fops = new Vector();
     String op_method;
     for (int iop = 0; iop < ops.size(); iop++ ) {
        Op op1 = (Op)ops.elementAt(iop);
        String output = op1.getValue();
        Vector op = Utils.split("/,/", output);
        int min;
        int max;
        op_method = (String)op.elementAt(0);
        if ( op.size() == 4 ) {
           max = Integer.valueOf((String)op.elementAt(2)).intValue();
           min = Integer.valueOf((String)op.elementAt(3)).intValue();
           if ( mCategories.length >= min && mCategories.length <= max ) {
              fops.add(op1);
           }
        }
        else {
           if (!op_method.equals("gen_script")) {
              fops.add(op1);
           } else {
              if (!userdefined) {
                 fops.add(op1);
              }
           }
        }

     }
     return fops;
  }

  public Vector getComparisonProducts() throws SQLException {
    return mUI.getOps(mView.getValue(), "comparison");
  }

  public Vector getViewItems() {
    return mViewItems;
  }

  private void initViewItems() throws SQLException {
    Vector rval = new Vector();
    for (Iterator view = mUI.getViews().iterator(); view.hasNext(); ){
      View theView = (View)view.next();
      String value = theView.getValue();
      boolean useIt = true;
      for (int i=0; i < value.length(); i++){
	char[] c = {value.charAt(i)};
	String avalue = new String(c);
	if (!mAxesSet.contains(avalue)){
	  useIt = false;
	  break;
	}
      }
      if (useIt){
	if (mView == null){
	  setView(theView);
	}
	rval.addElement(theView);
      }
    }
    mViewItems = rval;
  }

  private void setView(View theView) throws SQLException {
    mViewSet = new HashSet();
    mView = theView;
    String region = theView.getRegion();
    Log.debug(this, "The region string is: "+region);
    for (int i=0; i < region.length(); i++){
      char[] c = {region.charAt(i)};
      String aregion = new String(c);
      Log.debug(this, "Adding region: "+aregion+" to the viewSet");
      mViewSet.add(aregion);
    }
  }

  private void setView(String view) throws SQLException {
    if (view != null){
      // First make sure that this view is part of the set of axes
      // available for this variable
      for (int i=0; i < view.length(); i++){
	char[] c = {view.charAt(i)};
	String aview = new String(c);
        Log.debug(this, "setView: got aview with string value: "+aview);
	if (!mAxesSet.contains(aview)){
	  mView = null;
	  return;
	}
      }

      // Get view by name
      Log.debug(this, "setView: setting view to : "+view);
      View aView = mUI.getViewByName(view);

      // Not all variables have all views.  
      if ( aView == null ) {
         Log.debug(this,"Desired view not available.  Returning null view");
         mView = null;
         return;
      }
      // Now safe to set view
      setView(aView);
    }
  }

  /**
   * Returns the "main" category
   */
  public ICategory getCategory() throws SQLException {
    return mCategory;
  }

  /**
   * Returns all the requested categories
   * The first category is the "main" category
   */
  public ICategory[] getCategories() throws SQLException {
    return mCategories;
  }

  public void setCategories(Category[] categories) {
    mCategories = categories;
  }

  public String getView() {
    return mView.getValue();
  }

  public String getViewRegion() {
    return mView.getRegion();
  }

  static public void main(String[] args) throws SQLException, ServletException {
    RegionConstraint c = RegionConstraint.getInstance(new String[] {"2"});
    for (Iterator i = c.getViewItems().iterator(); i.hasNext(); ){
      System.out.println(i.next());
    }
  }

}

