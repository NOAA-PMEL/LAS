package gov.noaa.pmel.tmap.las.luis.db;

/**
 * @author $Author: rhs $
 * @version $Version$
 */

import java.util.Vector;
import java.sql.SQLException;
import java.net.URL;
import java.net.MalformedURLException;
import gov.noaa.pmel.tmap.las.luis.Log;
import gov.noaa.pmel.tmap.las.luis.DatasetItem;
import java.util.Iterator;

public class Category extends DbaseObject implements ICategory {

  String name;
  String path_name;
  String type;
  String grid_type;
  String path;
  String parentid, configid;
  String category_include, variable_include, constrain_include;
  String category_include_header, variable_include_header, constrain_include_header;


  public String getCategoryInclude() {
    return this.category_include;
  }

  public String getVariableInclude() {
    return this.variable_include;
  }

  public String getConstrainInclude() {
    return this.constrain_include;
  }

  public String getCategoryIncludeHeader() {
    return this.category_include_header;
  }

  public String getVariableIncludeHeader() {
    return this.variable_include_header;
  }

  public String getConstrainIncludeHeader() {
    return this.constrain_include_header;
  }

  public String getParentid() {
    return this.parentid;
  }

  public String getConfigid() {
    return this.configid;
  }

  public Category() {
    super("Category");
    addField("name");
    addField("type");
    addField("path");
    addField("grid_type");
    addField("parentid");
    addField("configid");
    addField("path_name");
    addField("category_include");
    addField("variable_include");
    addField("constrain_include");
    addField("category_include_header");
    addField("variable_include_header");
    addField("constrain_include_header");
  }

   public Category getParent() throws SQLException {
     Category parent =  new Category();
     if ( this.parentid.equals("0") ) {
        // Return the empty Category for the fictious parent with id=0.
        return parent;
     }
     try {
        // Parent should be "real" so fill it up and return it.
        parent.deserialize(this.parentid);
        return parent;
     }
     catch (IdNotFoundException e) {
        Log.debug(Category.class,
           "getParent: couldn't find parent for id" + this.parentid);
        return null;
     }
   }

   public DatasetItem getChildByName(String name) throws SQLException {
     Vector v = new Vector();
     for (Iterator ci = Config.getConfigs().iterator(); ci.hasNext(); ){
       Config config = (Config)ci.next();
       for (Iterator i = config.getCategories(this.getOid()).iterator(); i.hasNext();){
         // If this child can be a DatasetItem store it as such.  If
       // it blows up, store it as a Category.
          try {
             DatasetItem di = DatasetItem.getInstance((ICategory)i.next());
             if ( di.getName().equals(name) ) {
                return di;
             }
          }
          catch (SQLException e) {
            return null;
          }
       }
    }
    return null;
  }

   public Vector getChildren() throws SQLException {
     Vector v = new Vector();
     for (Iterator ci = Config.getConfigs().iterator(); ci.hasNext(); ){
       Config config = (Config)ci.next();
       for (Iterator i = config.getCategories(this.getOid()).iterator(); i.hasNext();){
         // If this child can be a DatasetItem store it as such.  If
       // it blows up, store it as a Category.
         try {
          DatasetItem di = DatasetItem.getInstance((ICategory)i.next());
          v.addElement(di);
       }
       catch (SQLException e) {
            v.addElement((ICategory)i.next());
       }
       }
     }
     return v;
   }

  static public Category getItemByPath(String dset) throws SQLException {
    try {
      Category c = new Category();
      String id = "'" + dset + "'";
      c.deserialize(id,"path_name");
      return c;
    } catch (IdNotFoundException idnfe){
      Log.debug(Category.class, "getItemByPath: couldn't find dset:" + dset);
      return null;
    }
  }

  static public String getLinkByPath(String dset) throws SQLException {
    try {
      Category c = getItemByPath(dset);
      return c.getOid();
    } catch (IdNotFoundException idnfe){
      Log.debug(Category.class, "getLinkByPath: couldn't find dset:" + dset);
      return "";
    }
  }

  public String getPathName() throws SQLException {
    return this.path_name;
  }

  public Config getConfig() throws SQLException {
    Config c = new Config();
    c.deserialize(configid);
    return c;
  }

  public MetaData getMetaData() throws SQLException {
    MetaData m = new MetaData();
    try {
      m.deserialize(oid, "parentid");
    } catch (IdNotFoundException e){
      return null;
    }
    return m;
  }

  public IVariableInfo getVariableInfo() throws SQLException {
    VariableInfo vi = null;
    if (type.equals("v") || type.equals("p")){
      VariableInfo tmp = new VariableInfo();
      tmp.deserialize(oid, "categoryid");
      vi = tmp;
    }
    return vi;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getType() {
    return type;
  }
  public void setGridType(String grid_type) {
    this.grid_type = grid_type;
  }
  public String getGridType() {
    return grid_type;
  }
  public void setPath(String path) {
    this.path = path;
  }
  public String getPath() {
    return path;
  }
  public boolean isAnalysis() {return false; }

  static public void main(String[] args){
    try {
      Category category = new Category();
      category.deserialize("12");
      System.out.println(category.toString());
      category.setOid("12345678");
      category.serialize();
    } catch (Exception e){
      e.printStackTrace();
    }
  }
}
