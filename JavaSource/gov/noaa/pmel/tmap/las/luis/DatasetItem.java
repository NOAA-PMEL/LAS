package gov.noaa.pmel.tmap.las.luis;
import java.lang.String;
import java.util.Vector;
import java.util.Iterator;
import java.util.ListIterator;
import java.sql.*;
import org.apache.oro.text.perl.Perl5Util;
import gov.noaa.pmel.tmap.las.luis.db.*;

public class DatasetItem implements IDatasetItem {
  Config mConfig;
  ICategory mCategory;
  Vector mPath;
  boolean mComparable = true;

  protected DatasetItem(Config config, ICategory c) throws SQLException {
    mConfig = config;
    mCategory = c;
    Perl5Util re = new Perl5Util();
    mPath = new Vector();
    re.split(mPath, "/:/", c.getPath());
  }

  public static Vector getItems(String id) throws SQLException {
    return getItems(id, null);
  }

  public static String getFirstVarId() throws SQLException {
    Config config = Config.getInstance("1");
    return config.getFirstVarId();
  }

  public static DatasetItem getInstance(ICategory c) throws SQLException {
    Config config = Config.getInstance(c.getConfigid());
    return new DatasetItem(config, c);
  }

  public static DatasetItem getInstance(String id) throws SQLException {
    ICategory c = null;
    Config config = null;
    for (Iterator ci = Config.getConfigs().iterator(); ci.hasNext(); ){
      config = (Config)ci.next(); 
      try {
	c = config.getCategory(id);
	return new DatasetItem(config,c);
      } catch (IdNotFoundException e){
      }
    }
    if (c == null){
      throw new IdNotFoundException();
    }
    return null;		// Never executed
  }

  public static Vector getDerivedItems(TemplateSession session, String id)
    throws SQLException {
    if (session == null){
      return new Vector();
    }
    String sessionid = session.getId();
    if (id == null){
      id = "0";
    }
    Vector v = new Vector();
    for (Iterator ci = Config.getConfigs().iterator(); ci.hasNext(); ){
      Config config = (Config)ci.next(); 
      for (Iterator i = config.getDerivedCategories(sessionid, id).iterator();
	   i.hasNext();){
	v.addElement(new DatasetItem(config,(ICategory)i.next()));
      }
    }
    return v;
  }

  public static Vector getItems(String id, String type) throws SQLException {
    if (id == null){
      id = "0";
    }
    if (type == null) {
       type="and (type='c' or type='v')";
    }
    Vector v = new Vector();
    for (Iterator ci = Config.getConfigs().iterator(); ci.hasNext(); ){
      Config config = (Config)ci.next(); 
      for (Iterator i = config.getCategories(id,type).iterator(); i.hasNext();){
	v.addElement(new DatasetItem(config,(ICategory)i.next()));
      }
    }
    return v;
  }

  public static Vector getItems(String id, String type, String grid_type) 
     throws SQLException {
    if (id == null){
      id = "0";
    }
    if (type == null) {
       type="and (type='c' or type='v')";
    }
    Vector v = new Vector();
    for (Iterator ci = Config.getConfigs().iterator(); ci.hasNext(); ){
      Config config = (Config)ci.next(); 
      for (Iterator i = config.getCategories(id,type,grid_type).iterator(); i.hasNext();){
	v.addElement(new DatasetItem(config,(ICategory)i.next()));
      }
    }
    return v;
  }

  public boolean isDerived() {
    return mCategory instanceof DerivedCategory;
  }

  public ICategory getCategory() {
    return mCategory;
  }

  public void setComparable(boolean flag){
    mComparable = flag;
  }

  public boolean isComparable() {
    return mComparable;
  }

  public MetaData getMetaData() throws SQLException {
    MetaData md = mCategory.getMetaData();
    // If it's null that's ok, send it back the template won't put up the "i".
    if ( md == null ) {
       Log.debug(this, "Returning null from null deserialize.");
       return md;
    }
    // If it's not null and this category contains other categories (not variables)
    // and there is no document URL then return null because there is nothing
    // to display in the metadata window.  The template won't put up the "i".
    if ( md != null && 
         this.getDatasetChildren().size() > 0 && 
         md.getDocUrl().equals("") &&
         this.getContributors().size() <= 0 ) {
       Log.debug(this,"Returning null.  getDatasetChildren.size()="+this.getDatasetChildren().size()+" DocUrl="+md.getDocUrl());
       return null;
    }
    // Looks good.  Send it back.
    Log.debug(this,"Looks good. Non-null md and non-null doc url.");
    return md;
  }

  public Vector getContributors() throws SQLException {
     Contributor c = new Contributor();
     Vector contributors = c.deserializeAggregate(mCategory.getOid(),"parentid");
     return contributors;
  }

  public Config getConfig() {
    return mConfig;
  }

  public IVariableInfo getVariableInfo()  throws SQLException{
    return mCategory.getVariableInfo();
  }

  public boolean isVariable() {
    return mCategory.getType().equals("v");
  }

  public boolean isStableVariable() {
    return mCategory.getType().equals("p");
  }

  public boolean isCategory() {
    return mCategory.getType().equals("c");
  }

  public boolean isStableCategory() {
    return mCategory.getType().equals("d");
  }

  public String getType() {
    return mCategory.getType();
  }

  public String getName() {
    return mCategory.getName();
  }

  static public String test() {
    return "test";
  }

  static public String getLinkByPath(String dset) throws SQLException {
    return Category.getLinkByPath(dset);
  }

  static public DatasetItem getItemByPath(String dset) throws SQLException {
    Category c = Category.getItemByPath(dset);
    return getInstance(c);
  }

  public String getLink() {
    return mCategory.getOid();
  }

  public CategoryHierarchy getCategoryHierarchy() {
     CategoryHierarchy ch = new CategoryHierarchy();
     if (this.isVariable()) {

        // Get the path and walk down the tree and get the parents of this category.
        Vector categories = new Vector();
        try {
           Vector path = this.getPath();
           ListIterator pi = path.listIterator();
           // Pop off the "0" category.
           pi.next();
           while ( pi.hasNext() ) {
              Category ac = new Category();
              ac.deserialize(((DatasetPathItem)pi.next()).getID());
              DatasetItem di = DatasetItem.getInstance(ac);
              categories.addElement(di);
           }
        } catch (SQLException e) {
           // It's ok.  Just go on.
           ;
        }
        ch.setCategories(categories); 

        // Load only this variable into the variable vector and
        // none of it's siblings.
        Vector variables = new Vector();
        variables.add(this);
        ch.setVariables(variables);
        
     } else if (this.isStableVariable()) {
        // Find the first "real" category hierarchy that contains
        // this variable and return it.

        try {
            IVariableInfo vi = this.getVariableInfo();
            if ( vi != null ) {
               String vpathname = vi.getUrl1();

               VariableInfo vc = new VariableInfo(); 

               // Get the first VariableInfo that has the same url1 path
               // as the one from the query string.
               vc.deserialize(Utils.quote(vpathname), "url1");

               if ( vc != null ) {
                  String varid = vc.getCategoryid();
                  Category theVar = new Category();
                  theVar.deserialize(vc.getCategoryid());
                  DatasetItem theVardi = DatasetItem.getInstance(theVar);
                  ch = theVardi.getCategoryHierarchy();
               }
               else {
                  Log.debug(this,"VariableInfo vi is null!");
               }
          }
        } catch (IdNotFoundException idnfe){
            Log.debug(this,"Found nothing falling through...");
          ;                     // Do nothing if not found
        } catch (SQLException e) {
           ;
        }

     } else if (this.isCategory()) {
        // Get the path and walk down the tree and get the parents of this category.
        Vector categories = new Vector();
        try {
           Vector path = this.getPath();
           ListIterator pi = path.listIterator();
           // Pop off the "0" category.
           pi.next();
           while ( pi.hasNext() ) {
              Category ac = new Category();
              ac.deserialize(((DatasetPathItem)pi.next()).getID());
              DatasetItem di = DatasetItem.getInstance(ac);
              categories.addElement(di);
           }
           categories.addElement(this);
        } catch (SQLException e) {
           // Go on.
           ;
        }
        ch.setCategories(categories); 
        
        // If this category happens to contain variables, load them in.
        try {
           Vector theseChildren = this.getChildren();
           if ( ((DatasetItem)theseChildren.get(0)).isVariable() ) { 
              ch.setVariables(theseChildren);
           }
        } catch (SQLException e) {
           ;
        }
     } else if (this.isStableCategory()) {
        // As "stable" category has no hierarchy.  Return the empty object.
        return ch;
     }
     return ch;
  }

  public Vector getPath() throws SQLException{
    Vector v = new Vector();
    v.addElement(new DatasetPathItem("Datasets","0"));
    for (int i=1; i < mPath.size(); i++){
      Category c = new Category();
      c.deserialize((String)mPath.elementAt(i));
      v.addElement(new DatasetPathItem(c.getName(), c.getOid()));
    }
    return v;
  }

  public Vector getChildren() throws SQLException {
    return getItems(mCategory.getOid(), null);
  }

  public DatasetItem getParent() throws SQLException {
    Category cat = new Category();
    try {
      cat.deserialize(mCategory.getParentid());
      return new DatasetItem(mConfig, cat);
    } catch (IdNotFoundException e){
      return null;
    }
  }

  public Vector getDatasetChildren() throws SQLException {
    return getItems(mCategory.getOid(), "c");
  }

  public String getCategoryInclude() throws SQLException {
     return mCategory.getCategoryInclude();
  }

  public String getCategoryIncludeHeader() throws SQLException {
     return mCategory.getCategoryIncludeHeader();
  }

  static public void main(String[] args) throws SQLException {
    for (Iterator i = getItems("10").iterator(); i.hasNext(); ){
      DatasetItem c = (DatasetItem)i.next();
      for (Iterator j = c.getPath().iterator(); j.hasNext();){
	DatasetPathItem path = (DatasetPathItem)j.next();
	System.out.println(path.getPath());
      }
    }
  }

}

