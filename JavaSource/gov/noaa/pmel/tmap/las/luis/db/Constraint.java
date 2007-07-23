// $Id: Constraint.java,v 1.4 2003/03/01 18:35:03 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;

import java.util.Vector;
import java.sql.SQLException;


/**
 * @author $Author: sirott $
 * @version $Version$
 */
public class Constraint extends DbaseObject {

  int count;
  String type,label,docurl, extra, size, style;

  boolean required, multiselect;
  public Constraint() {
    super("Constraints");
    addField("count");
    addField("type");
    addField("label");
    addField("docurl");
    addField("required");
    addField("multiselect");
    addField("extra");
    addField("size");
    addField("style");
  }

  /**
   * Gets the value of extra
   *
   * @return the value of extra
   */
  public String getExtra() {
    return this.extra;
  }

  /**
   * Sets the value of extra
   *
   * @param argExtra Value to assign to this.extra
   */
  public void setExtra(String argExtra){
    this.extra = argExtra;
  }

  public String getSize() {
    return this.size;
  }

  public String getStyle() {
    return this.style;
  }

  /**
   * Gets the value of required
   *
   * @return the value of required
   */
  public boolean isRequired() {
    return this.required;
  }

  /**
   * Sets the value of required
   *
   * @param argRequired Value to assign to this.required
   */
  public void setRequired(boolean argRequired){
    this.required = argRequired;
  }

  /**
   * Gets the value of multiselect
   *
   * @return the value of multiselect
   */
  public boolean isMultiselect() {
    return this.multiselect;
  }

  /**
   * Sets the value of multiselect
   *
   * @param argMultiselect Value to assign to this.multiselect
   */
  public void setMultiselect(boolean argMultiselect){
    this.multiselect = argMultiselect;
  }

  /**
   * Gets the value of count
   *
   * @return the value of count
   */
  public int getCount() {
    return this.count;
  }

  /**
   * Gets a list of integers representing indices of constraints
   * (for Velocity foreach loop)
   *
   * @return list of integers
   */
  public Vector getCountList() {
    Vector rval = new Vector();
    for (int i=0; i < getCount(); ++i){
      rval.addElement(new Integer(i));
    }
    return rval;
  }

  /**
   * Sets the value of count
   *
   * @param argCount Value to assign to this.count
   */
  public void setCount(int argCount){
    this.count = argCount;
  }

  /**
   * Gets the value of type
   *
   * @return the value of type
   */
  public String getType() {
    return this.type;
  }

  /**
   * Sets the value of type
   *
   * @param argType Value to assign to this.type
   */
  public void setType(String argType){
    this.type = argType;
  }

  /**
   * Gets the value of label
   *
   * @return the value of label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * Sets the value of label
   *
   * @param argLabel Value to assign to this.label
   */
  public void setLabel(String argLabel){
    this.label = argLabel;
  }

  /**
   * Gets the value of docurl
   *
   * @return the value of docurl
   */
  public String getDocurl() {
    return this.docurl;
  }

  /**
   * Sets the value of docurl
   *
   * @param argDocurl Value to assign to this.docurl
   */
  public void setDocurl(String argDocurl){
    this.docurl = argDocurl;
  }

  public Vector getWidgets() throws SQLException {
    ConstraintWidget widget = new ConstraintWidget();
    return widget.deserializeAggregate(oid, "constraint_id");
  }

  static public void main(String[] args){
    try {
      Constraint constraint = new Constraint();
      constraint.deserialize("1");
      Vector widgets = constraint.getWidgets();
    } catch (Exception e){
      e.printStackTrace();
    }
  }
}
