package gov.noaa.pmel.tmap.las.luis.db;
import java.sql.*;
import java.util.Vector;
import gov.noaa.pmel.tmap.las.luis.Log;


/**
 * @author $Author: sirott $
 * @version $Version$
 */


public class VariableInfo extends DbaseObject implements IVariableInfo {

  String url1;
  String url2;
  String customUrl;
  String ui_id, institution_id, units, dods_url, categoryid;
  UI mUI;
  Institution mInstitution;
  boolean composite;

  public VariableInfo() {
    super("VariableInfo");
    addField("url1");
    addField("url2");
    addField("customUrl", "custom_url");
    addField("ui_id");
    addField("units");
    addField("institution_id");
    addField("dods_url");
    addField("composite");
    addField("categoryid");
  }

  public boolean isComposite() {
    return this.composite;
  }

  public String getDodsUrl() {
    return this.dods_url;
  }

  public String getUnits() {
    return this.units;
  }

  public String getCategoryid() {
    return this.categoryid;
  }

  public Vector getAxes() throws SQLException {
    Axis tmp = new Axis();
    return tmp.deserializeManyToMany(oid, "varid", "AxisVariableJoin",
				     "axisid");
  }

  public IAxis getAxisByType(String type) throws SQLException {
    if (!(type.equals("x") || type.equals("y")
	  || type.equals("z") || type.equals("t"))){
      throw new SQLException("Invalid axis type: " + type);
    }
    Axis tmp = new Axis();
    String extra = " and Axis.type='" + type + "'";
    Vector v = tmp.deserializeManyToMany(oid, "varid", "AxisVariableJoin",
					 "axisid", extra);
    if (v.size() == 0){
      return null;
    } else {
      return (IAxis)v.elementAt(0);
    }
  }

  public UI getUI() throws SQLException {
    if (mUI == null){
      mUI = new UI();
      mUI.deserialize(ui_id);
    }
    return mUI;
  }

  public Institution getInstitution() throws SQLException {
    if (mInstitution == null){
      mInstitution = new Institution();
      mInstitution.deserialize(institution_id);
    }
    Log.debug(this, "Got institution name: " +
	      mInstitution.getName());
    return mInstitution;
  }

  public String getUrl1() {
    return url1;
  }
  public void setUrl1(String url1) {
    this.url1 = url1;
  }
  public void setUrl2(String url2) {
    this.url2 = url2;
  }
  public String getUrl2() {
    return url2;
  }
  public void setCustomUrl(String customUrl) {
    this.customUrl = customUrl;
  }
  public String getCustomUrl() {
    return customUrl;
  }
}
