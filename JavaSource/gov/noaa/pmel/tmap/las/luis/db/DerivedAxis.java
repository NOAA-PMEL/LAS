// $Id: DerivedAxis.java,v 1.3 2002/12/20 23:35:41 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;

import java.util.Vector;
import java.sql.SQLException;
import java.text.DecimalFormat;


public class DerivedAxis extends DbaseObject implements IAxis {
  IAxis mSourceAxis;
  String parent_axis, parent_derived, sessionid, lo,hi, type;
  boolean is_analysis;
  boolean is_new = true;
  String analysis_type = "";

  public DerivedAxis(IVariableInfo vi, IAxis axis, String sessionid)
  throws SQLException {
    this();
    mSourceAxis = axis;

    this.parent_axis = mSourceAxis.getOid();
    this.parent_derived = ((DerivedVariableInfo)vi).getOid();
    this.sessionid = sessionid;
    this.lo = mSourceAxis.getLo();
    this.hi = mSourceAxis.getHi();
    this.type = mSourceAxis.getType();
    this.is_analysis = axis.isAnalysis();
    this.analysis_type = axis.getAnalysisType();
    int lastsource = mSourceAxis.getLastOid() + 1;
    int lastderived = getLastOid() + 1;
    setOid(Math.max(lastsource, lastderived));
  }

  public DerivedAxis() throws SQLException {
    super("DerivedAxis");
    addField("parent_axis");
    addField("parent_derived");
    addField("sessionid");
    addField("analysis_type");
    addField("lo");
    addField("hi");
    addField("type");
    addField("is_analysis");
    addField("is_new");
  }

  public void setNew(boolean val) {
    is_new = val;
  }

  public boolean isNew() {
    return is_new;
  }

  public void postDeserialize() throws SQLException {
    try {
      Axis axis = new Axis();
      axis.deserialize(this.parent_axis);
      mSourceAxis = axis;
    } catch (IdNotFoundException e){
      DerivedAxis axis = new DerivedAxis();
      axis.deserialize(this.parent_axis);
      mSourceAxis = axis;
    }
  }

  public void setLo(String lo) {
    this.lo = lo;
  }

  public void setHi(String hi){
    this.hi = hi;
  }

  public void setAnalysis(boolean is_analysis){
    this.is_analysis = is_analysis;
  }

  public boolean isAnalysis(){
    return this.is_analysis;
  }

  public void setAnalysisType(String type){
    this.analysis_type = type;
    if (this.analysis_type == null){
      this.analysis_type = "";
    }
  }

  public String getAnalysisType() {
    return this.analysis_type;
  }

  public String getLo() {
    return this.lo;
  }

  public String getHi() {
    return this.hi;
  }

  public Vector getWidgets() throws java.sql.SQLException {
    return mSourceAxis.getWidgets();
  }

  public int getSize() {
    return mSourceAxis.getSize();
  }

  public String getName() {
    return mSourceAxis.getName();
  }

  public void setName(String name) {
    mSourceAxis.setName(name);
  }

  public void setUnits(String units) {
    mSourceAxis.setUnits(units);
  }

  public String getUnits() {
    return mSourceAxis.getUnits();
  }

  public void setType(String type) {
    mSourceAxis.setType(type);
  }

  public String getType() {
    return mSourceAxis.getType();
  }

  public void setCategory(String category) {
    mSourceAxis.setCategory(category);
  }

  public String getCategory() {
    return mSourceAxis.getCategory();
  }

  public String getDMYT() throws java.sql.SQLException {
	  int size = getWidgets().size();
	  if ( getCategory().equals("rtime")) {
		  if ( size == 4 ) {
			  return "DMYT";
		  } else if ( size == 3 ) {
			  Vector widgets = getWidgets();
			  AxisWidget one = (AxisWidget) widgets.get(0);
			  int widget_size = one.getItems().size();
			  if ( widget_size == 1 ) {
				  return "MY";
			  } else {
				  return "DMY";
			  }
		  } else if ( size == 2 ) {
			  return "MY";
		  }
	  } else if (getCategory().equals("ctime") ) {
		 if ( size == 3 ) {
			  return "MDT";
		  } else if ( size == 2 ) {
			  return "MD";
		  }
	  }
	  return "";
  }
  public String getDeltaMinutes() throws SQLException {
	  int size = getWidgets().size();
	  if ( size == 4 ) {
		  DecimalFormat format = new DecimalFormat("##");
		  Vector widgets = getWidgets();
		  AxisWidget one = (AxisWidget) widgets.get(3);
		  Vector items = one.getItems();
		  double sum = 0.0;
		  for (int i=0; i<items.size()-1; i++) {
			  AxisWidgetItem itemI = (AxisWidgetItem)items.get(i);
			  AxisWidgetItem itemIP1 = (AxisWidgetItem)items.get(i+1);
			double delta = 
				Double.valueOf(itemIP1.getValue()).doubleValue() - 
			    Double.valueOf(itemI.getValue()).doubleValue();
			sum = sum + delta;
		  }
		  double ave = (sum/Double.valueOf(items.size()-1).doubleValue())*60.;
		  return format.format(ave);
	  } else {
		  return "0";
	  }
  }
}
