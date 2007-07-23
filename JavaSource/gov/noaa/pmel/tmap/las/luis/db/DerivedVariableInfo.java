// $Id: DerivedVariableInfo.java,v 1.4 2004/12/01 18:28:30 rhs Exp $
package gov.noaa.pmel.tmap.las.luis.db;

import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.HashSet;


public class DerivedVariableInfo extends DbaseObject implements IVariableInfo {
  IVariableInfo mVi;
  String categoryid, varinfo_id, sessionid;

  public DerivedVariableInfo(DerivedCategory cat, IVariableInfo vi,
			     String sessionid)
  throws SQLException {
    this();
    mVi = (IVariableInfo)vi;

    this.varinfo_id = mVi.getOid();
    this.categoryid = cat.getOid();
    this.sessionid = sessionid;
    int lastcat = mVi.getLastOid() + 1;
    int lastderived = getLastOid() + 1;
    setOid(Math.max(lastcat, lastderived));
  }

  public DerivedVariableInfo() throws SQLException {
    super("DerivedVariableInfo");
    addField("categoryid");
    addField("varinfo_id");
    addField("sessionid");
  }

  public void postDeserialize() throws SQLException {
    try {
      VariableInfo vi = new VariableInfo();
      vi.deserialize(this.varinfo_id);
      mVi = vi;
    } catch (IdNotFoundException e){
      DerivedVariableInfo vi = new DerivedVariableInfo();
      vi.deserialize(this.varinfo_id);
      mVi = vi;
    }
  }

  public boolean isComposite(){
    return mVi.isComposite();
  }

  public String getDodsUrl(){
    return mVi.getDodsUrl();
  }

  public String getUnits(){
    return mVi.getUnits();
  }

  public String getCategoryid() {
    return this.categoryid;
  }

  public Vector getAxes() throws SQLException {
    DerivedAxis axis = new DerivedAxis();
    String extra = " and sessionid='" + sessionid + "' order by is_new";
    return axis.deserializeAggregate(getOid(), "parent_derived", extra);
  }

  public IAxis getAxisByType(String type) throws SQLException{
    if (!(type.equals("x") || type.equals("y")
	  || type.equals("z") || type.equals("t"))){
      throw new SQLException("Invalid axis type: " + type);
    }
    DerivedAxis axis = new DerivedAxis();
    String extra = " and sessionid='" + sessionid + "'" +
      " and type='" + type + "'";
    Vector v = axis.deserializeAggregate(getOid(), "parent_derived", extra);
    if (v.size() == 0){
      return null;
    } else {
      return (IAxis)v.elementAt(0);
    }
  }

  public UI getUI() throws SQLException{
    return mVi.getUI();
  }

  public Institution getInstitution() throws SQLException{
    return mVi.getInstitution();
  }

  public String getUrl1(){
    return mVi.getUrl1();
  }

  public void serialize() throws SQLException {
    super.serialize();
    for (Iterator i = mVi.getAxes().iterator(); i.hasNext(); ){
      IAxis axis = (IAxis)i.next();
      DerivedAxis daxis = new DerivedAxis(this, axis, sessionid);
      daxis.setNew(false);
      daxis.serialize();
    }
  }

  public void setUrl1(String url1){
    mVi.setUrl1(url1);
  }

  public void setUrl2(String url2){
    mVi.setUrl2(url2);
  }

  public String getUrl2(){
    return mVi.getUrl2();
  }

  public void setCustomUrl(String customUrl){
    mVi.setCustomUrl(customUrl);
  }

  public String getCustomUrl(){
    return mVi.getCustomUrl();
  }

}
  
