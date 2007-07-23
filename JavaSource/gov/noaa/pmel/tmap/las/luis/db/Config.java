package gov.noaa.pmel.tmap.las.luis.db;

import java.util.*;
import gov.noaa.pmel.tmap.las.luis.*;
import java.lang.Class;
import java.lang.reflect.*;
import java.sql.SQLException;


/**
 * @author $Author: rhs $
 * @version $Version$
 */

public class Config extends DbaseObject {
  Vector mCategories;
  String packageid;
  String href;
  String serverurl;
  String contact;

  public Config() {
    super("Config");
    addField("packageid");
    addField("href");
    addField("serverurl");
    addField("contact");
  }

  // TODO -- Only get DerivedCategory from correct config id
  public Vector getDerivedCategories(String sessionid, String parentid)
    throws SQLException {
    DerivedCategory c = new DerivedCategory();
    String extra = new String(" and sessionid='" + sessionid + "'");
    return c.deserializeAggregate(parentid, "parentid", extra);
  }

  public Vector getCategories(String parentid) throws SQLException {
    return getCategories(parentid, null);
  }

  public Vector getCategories(String parentid, String type) throws SQLException {
     // should be a call to getCategories(parentid, type, null);
    Category c = new Category();
    String extra = "";
    if (type != null && type.indexOf("or")<0){
      extra = "and type='" + type + "'";
    }
    else if (type !=null && type.indexOf("or")>=0){
       extra=type;
    }
    extra += " and parentid=" + parentid + " order by name";
    mCategories = c.deserializeAggregate(oid,"configid", extra);
    return mCategories;
  }

  public Vector getCategories(String parentid, String type, String grid_type) 
     throws SQLException {
    String extra = "";
    if (type != null && type.indexOf("or")<0){
      extra += "and type='" + type + "'";
    }
    else if (type !=null && type.indexOf("or")>=0){
       extra+=type;
    }
    if (grid_type != null){
      extra += "and grid_type='" + grid_type + "'";
    }
    extra += " and parentid=" + parentid + " order by name";
    Category c = new Category();
    mCategories = c.deserializeAggregate(oid,"configid", extra);
    return mCategories;
  }

  public Category getCategory(String id)
    throws SQLException {
    Category c = new Category();
    String extra = " and configid=" + getOid();
    c.deserialize(id, "oid", extra);
    return c;
  }

  static public Config getInstance(String id) throws SQLException {
    Config c = new Config();
    c.deserialize(id);
    return c;
  }

  static public Vector getConfigs() throws SQLException {
    Config c = new Config();
    return c.deserializeAggregate();
  }

  static public void main(String[] args) {
    try {
      Config c = Config.getInstance("1");
      System.out.println(c);
      for (Iterator i = c.getCategories("0").iterator(); i.hasNext();){
        Category cat = (Category)i.next();
	System.out.println(cat);
      }

      // Test VariableInfo
      for (Iterator i = c.getCategories("1").iterator(); i.hasNext();){
        Category cat = (Category)i.next();
	System.out.println(cat);
        IVariableInfo vi = cat.getVariableInfo();
	System.out.println(vi);

	// Test Axes
	for (Iterator axes = vi.getAxes().iterator(); axes.hasNext(); ){
	  IAxis axis = (IAxis)axes.next();
	  System.out.println(axis);
	  for (Iterator widgets = axis.getWidgets().iterator(); widgets.hasNext(); ){
	    AxisWidget mi = (AxisWidget)widgets.next();
	    System.out.println(mi);
	    for (Iterator items = mi.getItems().iterator(); items.hasNext(); ){
	      System.out.println(items.next());
	    }
	  }
	}

	// Test UI
	UI ui = vi.getUI();
	System.out.println(ui);
	System.out.println(ui.getLiveMap());
	for (Iterator lmitem = ui.getLiveMap().getWidgets().iterator();
	     lmitem.hasNext(); ){
	  System.out.println(lmitem.next());
	}
	for (Iterator ops = ui.getOps().iterator(); ops.hasNext(); ){
	  System.out.println((Op)ops.next());
	}
	for (Iterator views = ui.getViews().iterator(); views.hasNext(); ){
	  System.out.println((View)views.next());
	}
      }


    } catch (Exception e){
      e.printStackTrace();
    }
  }

  public String getContact() {
     return contact;
  }
  public void setContact(String contact) {
     this.contact = contact;
  }
  public String getPackageid() {
    return packageid;
  }
  public void setPackageid(String packageid) {
    this.packageid = packageid;
  }
  public void setHref(String href) {
    this.href = href;
  }
  public String getHref() {
    return href;
  }
  public void setServerurl(String serverurl) {
    this.serverurl = serverurl;
  }
  public String getServerurl() {
    return serverurl;
  }
  public void addCategory(Category c){
    mCategories.addElement(c);
  }
  public String getFirstVarId() throws SQLException {
    Category c = new Category();
    Vector v = c.deserializeAggregate(oid,"configid",
				 "and type='v' order by name limit 1");
    return ((Category)v.elementAt(0)).getOid();
  }
}
