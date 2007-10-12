package gov.noaa.pmel.tmap.las.luis.db;

import java.util.Iterator;
import java.util.Vector;
import java.sql.SQLException;
import java.text.DecimalFormat;

/**
 *
 * @author $Author: rhs $
 * @version $Revision: 1.10 $
 */

public class Axis extends DbaseObject implements IAxis {


  String name;
  String units;
  String type;
  String category;
  String lo,hi;
  int size;

  public Axis() {
      super("Axis");
      addField("name");
      addField("units");
      addField("type");
      addField("category");
      addField("size");
      addField("lo");
      addField("hi");
  }

  public void setAnalysis(boolean isAnalysis){
    throw new RuntimeException("Not implemented");
  }

  public boolean isAnalysis(){
    return false;
  }

  public String getLo() {return this.lo; }

  public String getHi() {return this.hi; }

  public String getLoFormatted(String format) {
     double dlo = Double.valueOf(this.lo).doubleValue();
     DecimalFormat javaformat;
     try {
        javaformat = new DecimalFormat(format);
     } catch (IllegalArgumentException e ) {
        return this.lo;
     } catch (NullPointerException e) {
        return this.lo;
     }
     return javaformat.format(dlo);
  }
  public String getHiFormatted(String format) {
     double dhi = Double.valueOf(this.hi).doubleValue();
     DecimalFormat javaformat;
     try {
        javaformat = new DecimalFormat(format);
     } catch (IllegalArgumentException e ) {
        return this.hi;
     } catch (NullPointerException e) {
        return this.hi;
     }
     return javaformat.format(dhi);
   }

  public void setLo(String lo) {
    this.lo = lo;
  }

  public void setHi(String hi){
    this.hi = hi;
  }

  public Vector getWidgets() throws java.sql.SQLException {
    AxisWidget obj = new AxisWidget();
    return obj.deserializeAggregate(oid, "axis_id");
  }

  public int getSize() {
    return size;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setUnits(String units) {
    this.units = units;
  }
  public String getUnits() {
    return units;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getType() {
    return type;
  }
  public void setCategory(String category) {
    this.category = category;
  }
  public String getCategory() {
    return category;
  }

  public String getAnalysisType() {
    return "";
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
			  return "MDH";
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
