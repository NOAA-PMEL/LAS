// $Id: DerivedCategory.java,v 1.9 2004/07/06 21:09:25 rhs Exp $ 

package gov.noaa.pmel.tmap.las.luis.db;
import java.lang.String;
import java.lang.StringBuffer;
import java.util.Vector;
import java.util.Iterator;
import java.sql.*;
import gov.noaa.pmel.tmap.las.luis.db.*;
import javax.servlet.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class DerivedCategory extends DbaseObject implements ICategory {
  String parentcat, parentid, sessionid, name, derived_axes;
  ICategory mCategory;
  int mask_type;	// 0 = none, 1 = ocean, 2 = land

  public DerivedCategory(String parentcat, String sessionid, String name,
			 String[] masks)
    throws SQLException {
    this();
    mCategory = new Category();
    try {
      mCategory.deserialize(parentcat);
    } catch (IdNotFoundException e){
      mCategory = new DerivedCategory();
      mCategory.deserialize(parentcat);
    }
    this.parentcat = parentcat;
    this.parentid = mCategory.getParentid();
    this.sessionid = sessionid;
    this.name = name;
    if (masks == null){
      mask_type = 0;
    } else {
      for (int i=0; i < masks.length; ++i){
	if (masks[i].equals("o")){
	  this.mask_type |= 1;
	} else if (masks[i].equals("l")){
	  this.mask_type |= 2;
	}
      }
    }
    int lastcat = 0;
    if (mCategory instanceof Category){
      lastcat = ((Category)mCategory).getLastOid() + 1;
    }
    int lastderived = getLastOid() + 1;
    setOid(Math.max(lastcat, lastderived));
  }

  public DerivedCategory() {
    super("DerivedCategory");
    addField("parentcat");
    addField("parentid");
    addField("sessionid");
    addField("name");
    addField("mask_type");
  }

  public boolean isOceanMask() {
    return (mask_type & 1) == 1;
  }

  public boolean isLandMask() {
    return (mask_type & 2) == 2;
  }

  public boolean isAnalysis() {return true; }

  public String getAnalysisExpression() throws SQLException {
    StringBuffer express = new StringBuffer();
    IVariableInfo vi = getVariableInfo();
    for (Iterator i = vi.getAxes().iterator(); i.hasNext(); ){
      IAxis axis = (IAxis)i.next();
      if (axis.isAnalysis()){
	express.append(axis.getAnalysisType()).append("(").append(axis.getType()).append("=")
	  .append(axis.getLo()).append(":").append(axis.getHi()).append(")");
      }
    }
    if (isLandMask()) express.append(" land mask");
    if (isOceanMask()) express.append(" ocean mask");
    return express.toString();
  }

  public IVariableInfo getVariableInfo() throws SQLException {
    IVariableInfo vi = mCategory.getVariableInfo();
    if (vi != null){
      DerivedVariableInfo dvi = new DerivedVariableInfo();
      String extra = " and sessionid = '" + this.sessionid + "'";
      dvi.deserialize(getOid(), "categoryid", extra);
      vi = dvi;
    }
    return vi;
  }

  public void postDeserialize() throws SQLException {
    try {
      mCategory = new Category();
      mCategory.deserialize(this.parentcat);
    } catch (IdNotFoundException e){
      mCategory = new DerivedCategory();
      mCategory.deserialize(this.parentcat);
    }
  }

  public void serialize() throws SQLException {
    // See if  variable has already been created
    String extra = " and name='" + name + "' and sessionid='" + sessionid
      + "'";
    Vector oldList = deserializeAggregate(mCategory.getOid(), "parentcat",
						   extra);
    if (oldList.size() > 0){
      throw new DuplicateVariableException("Derived variable " +
					     name + " already exists");
    }
    super.serialize();
    IVariableInfo vi = (IVariableInfo)mCategory.getVariableInfo();
    DerivedVariableInfo dvi = new DerivedVariableInfo(this, vi, sessionid);
    dvi.serialize();
  }

  public String getParentName() {
    return mCategory.getName();
  }

  public String getName() { return this.name; }

  public void setName(String name) {
    this.name = name;
  }

  public String getSessionId() { return this.sessionid; }

  public String getParentid() {
    return mCategory.getParentid();
  }

  public String getConfigid() {
    return mCategory.getConfigid();
  }

  public Config getConfig() throws SQLException {
    return mCategory.getConfig();
  }

  public MetaData getMetaData() throws SQLException {
    return mCategory.getMetaData();
  }

  public void setType(String type){
    mCategory.setType(type);
  }

  public String getType() {
    return mCategory.getType();
  }

  public String getGridType() {
    return mCategory.getGridType();
  }

  public void setPath(String path) {
    mCategory.setPath(path);
  }

  public String getPath() {
    return mCategory.getPath();
  }

  public String getCategoryInclude() {
     return mCategory.getCategoryInclude();
  }

  public String getCategoryIncludeHeader() {
     return mCategory.getCategoryIncludeHeader();
  }

  public static void main(String[] args){
    try {
      DerivedCategory dc = new DerivedCategory("11",
					       "12345", "test", null);
      dc.serialize();
      DerivedAxis anAxis = (DerivedAxis)dc.getVariableInfo().getAxisByType("x");
      anAxis.setAnalysis(true);
      anAxis.setLo("100");
      anAxis.setHi("200");
      anAxis.serialize();
      IVariableInfo info = dc.getVariableInfo();
      System.out.println(info);
      for (Iterator i = info.getAxes().iterator(); i.hasNext(); ){
	IAxis axis = (IAxis)i.next();
	System.out.println(axis);
      }
      System.out.println("Test getAxisByType");
      System.out.println(info.getAxisByType("z"));
    } catch (Exception e){
      e.printStackTrace();
    }
  }
}

