// $Id: UI.java,v 1.10 2002/11/30 23:42:51 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;


/**
 * @author $Author: sirott $
 * @version $Version$
 */
import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;
import java.lang.String;
public class UI extends DbaseObject {

  LiveMap mLiveMap;
  Vector mOps;
  Vector mViews;
  Vector mOptions;
  Vector mConstraints;
  Vector mAnalysis;
  Hashtable mViewByName;
  boolean isConstrained;
  String z_text, title;

  public UI() {
    super("UI");
    addField("isConstrained");
    addField("z_text");
    addField("title");
  }

  public String getTitle() {
    return title.replaceAll(" ", "&nbsp;");
  }

  public String get_z_text() {
    
    return z_text.replaceAll(" ", "&nbsp;");
  }

  public boolean isConstrained() {
    return isConstrained;
  }

  public Vector getAnalysisItems() throws java.sql.SQLException {
    if (mAnalysis == null){
      Analysis a = new Analysis();
      mAnalysis = a.deserializeAggregate(oid, "ui_id");
    }
    return mAnalysis;
  }

  public LiveMap getLiveMap() throws java.sql.SQLException{
    if (mLiveMap == null){
      LiveMap tmp = new LiveMap();
      java.util.Vector v = tmp.deserializeAggregate(oid, "ui_id");
      mLiveMap = (LiveMap)v.elementAt(0);
    }
    return mLiveMap;
  }

  public Vector getOps() throws java.sql.SQLException{
    return getOps(null, null);
  }

  public Vector getOps(String view) throws java.sql.SQLException {
    return getOps(view, null);
  }

  public Vector getConstraints() throws java.sql.SQLException {
    if (mConstraints == null){
      Constraint tmp = new Constraint();
      mConstraints = tmp.deserializeAggregate(oid, "ui_id");
    }
    return mConstraints;
  }

  public Vector getOps(String view, String mode) throws java.sql.SQLException{
    if (mode == null){
      mode = "normal";
    }
    String addedConstraint = " and mode='" + mode + "'";
    if (view != null){
      addedConstraint += " and (view='" + view + "'" +
	                        " or view='*')";
    }
    if (mOps == null){
      Op tmp = new Op();
      mOps = tmp.deserializeAggregate(oid, "ui_id", addedConstraint);
    }
    return mOps;
  }

  public Vector getOptions(String op) throws java.sql.SQLException{
    String addedConstraint = "";
    if (op != null){
      addedConstraint = " and op='" + op + "'";
    }
    if (mOptions == null){
      Options tmp = new Options();
      mOptions = tmp.deserializeAggregate(oid, "ui_id", addedConstraint);
    }
    return mOptions;
  }

  public Vector getOptions() throws java.sql.SQLException{
    return getOptions(null);
  }

  public View getViewByName(String name) throws java.sql.SQLException {
    if (mViewByName == null){
      getViews();
      mViewByName = new Hashtable();
      for (Iterator i = getViews().iterator(); i.hasNext(); ){
	View v = (View)i.next();
	mViewByName.put(v.getValue(), v);
      }
    }
    return (View)mViewByName.get(name);
  }

  public Vector getViews() throws java.sql.SQLException{
    if (mViews == null){
      View tmp = new View();
      mViews = tmp.deserializeAggregate(oid, "ui_id");
    }
    return mViews;
  }

  static public void main(String[] args) throws java.sql.SQLException {
    UI ui = new UI();
    ui.deserialize("1");
    for (java.util.Iterator i = ui.getOptions().iterator(); i.hasNext(); ){
      System.out.println(i.next());
    }
  }
    
}

