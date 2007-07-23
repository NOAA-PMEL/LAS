package gov.noaa.pmel.tmap.las.luis.db;

import java.util.Vector;
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
}
