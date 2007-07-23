package gov.noaa.pmel.tmap.las.luis;
import gov.noaa.pmel.tmap.las.luis.db.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.String;
import java.util.AbstractCollection;
import java.util.Vector;
import java.sql.SQLException;
import java.util.ArrayList;

public class SessionTemplateContext extends TemplateContext {
  public SessionTemplateContext() throws ServletException, SQLException {
    super();
  }

  public Object put(String key, Object value){
    if (value == null){
      return remove(key);
    }
    return super.put(key,value);
  }

  /**
   * Gets the value of region
   *
   * @return the value of region
   */
  public RegionConstraint getRegion() {
    return (RegionConstraint)get("region");
  }

  /**
   * Sets the value of region
   *
   * @param argRegion 
   */
  public void setRegion(RegionConstraint argRegion){
    put("region",argRegion);
  }

  /**
   * Gets the value of compare region
   *
   * @return the value of compare region
   */
  public RegionConstraint getCompareRegion(int index) {
    ArrayList alist = (ArrayList)get("compare_region");
    if (alist == null){
      return null;
    }
    return (RegionConstraint)alist.get(index);
  }

  /**
   * Sets the value of compare region
   *
   * @param argRegion 
   */
  public void setCompareRegion(RegionConstraint[] argRegion){
    ArrayList alist = new ArrayList();
    for (int i=0; i < argRegion.length; ++i){
      alist.add(argRegion[i]);
    }
    setCompareRegion(alist);
  }

  public void setCompareRegion(ArrayList alist){
    put("compare_region",alist);
  }

  public void setCompareRegion(int index, RegionConstraint rc){
    ArrayList alist = (ArrayList)get("compare_region");
    if (alist == null){
      alist = new ArrayList(2);
      alist.add(null);
      alist.add(null);
    }
    alist.set(index,rc);
    setCompareRegion(alist);
  }

  /**
   * Gets the value of category
   *
   * @return the value of category
   */
  public ICategory getCategory() {
    return (ICategory)get("category");
  }

  /**
   * Sets the value of category
   *
   * @param argCategory 
   */
  public void setCategory(ICategory argCategory){
    put("category", argCategory);
  }

  /**
   * Gets the value of compare category
   *
   * @return the value of category
   */
  public ArrayList getCompareCategory() {
    return (ArrayList)get("compare_category");
  }

  /**
   * Sets the value of compare category
   *
   * @param argCategory 
   */
  public void setCompareCategory(ICategory[] argCategory){
    ArrayList list = new ArrayList();
    for (int i=0; i < argCategory.length; ++i){
      list.add(argCategory[i]);
    }
    put("compare_category", list);
  }

  /**
   * Sets the value of categories
   *
   * @param argCategory
   */
  public void setCategories(ICategory[] cats){
    Vector v = new Vector();
    for (int i=0; i < cats.length; ++i){
      v.add(cats[i]);
    }
    put("categories", v);
  }

  /**
   * Gets the value of categories
   *
   * @param argCategory 
   */
  public Vector getCategories(){
    return (Vector)get("categories");
  }


  public Vector getConfigs() {
    return (Vector)get("configs");
  }

  public void setConfigs(Vector argConfigs) {
    put("configs", argConfigs);
  }

  /**
   * Gets the value of datasets
   *
   * @return the value of datasets
   */
  public Vector getDatasets() {
    return (Vector)get("dataset");
  }

  /**
   * Sets the value of datasets
   *
   * @param argDatasets Value to assign to this.datasets
   */
  public void setDatasets(Vector argDatasets){
    put("dataset",argDatasets);
  }


  /**
   * Gets the value of compare datasets
   *
   * @return the value of compare datasets
   */
  public ArrayList getCompareDatasets() {
    return (ArrayList)get("compare_dataset");
  }

  /**
   * Sets the value of compare datasets
   *
   * @param argDatasets 
   */
  public void setCompareDatasets(Vector[] argDatasets){
    ArrayList list = new ArrayList();
    for (int i=0; i < argDatasets.length; ++i){
      list.add(argDatasets[i]);
    }
    put("compare_dataset", list);
  }

  public void removeCompareDatasets(){
    remove("compare_dataset");
  }

  public void setCompareDatasets(int index, Vector argDatasets){
    ArrayList adsets = getCompareDatasets();
    Vector[] dsets;
    if (adsets == null){
      dsets = new Vector[2];
    } else {
      dsets = (Vector[])getCompareDatasets().toArray(new Vector[2]);
    }
    dsets[index] = argDatasets;
    setCompareDatasets(dsets);
  }

  /**
   * Gets the value of xml
   *
   * @return the value of xml
   */
  public XmlRequester getXml() {
    return (XmlRequester)get("xml");
  }

  /**
   * Sets the value of xml
   *
   * @param argXml Value to assign to this.xml
   */
  public void setXml(XmlRequester argXml){
    put("xml", argXml);
  }

  /**
   * Gets the value of historyList
   *
   * @return the value of historyList
   */
  public AbstractCollection getHistoryList() {
    return (AbstractCollection)get("history");
  }

  /**
   * Sets the value of historyList
   *
   * @param argHistoryList Value to assign to this.historyList
   */
  public void setHistoryList(AbstractCollection argHistoryList){
    put("history", argHistoryList);
  }

  /**
   * Get the value of browser_checked
   *
   * @return the value of browser_checked
   */
  public Boolean getBrowserChecked() {
     return (Boolean)get("browser_checked");
  }
  /**
   * Sets the value of browser_checked
   *
   * @param argBrowserChecked
   */
  public void setBrowserChecked(Boolean argBrowserChecked) {
     put("browser_checked", argBrowserChecked);
  }



  /**
   * Gets the value of use_java
   *
   * @return the value of use_java
   */
  public Boolean getUseJava() {
    return (Boolean)get("use_java");
  }

  /**
   * Sets the value of use_java
   *
   * @param argUseJava 
   */
  public void setUseJava(Boolean argUseJava){
    put("use_java", argUseJava);
  }

  /**
   * Gets the value of force_java
   *
   * @return the value of force_java
   */
  public Boolean getForceJava() {
    return (Boolean)get("force_java");
  }

  /**
   * Sets the value of force_java
   *
   * @param argForceJava 
   */
  public void setForceJava(Boolean argForceJava){
    put("force_java", argForceJava);
  }

  /**
   * Gets the value of pure_html.  Set to true if output should passed unaltered to browser.
   *
   * @return the value of pure_html
   */
  public Boolean getPureHtml() {
    return (Boolean)get("pure_html");
  }

  /**
   * Sets the value of pure_html.  Set to true if output should passed unaltered to browser.
   *
   * @param argPureHtml.
   */
  public void setPureHtml(Boolean argPureHtml){
    put("pure_html", argPureHtml);
  }

  /**
   * Gets the MapState object
   *
   * @return the current MapState object
   */
  public MapStateBean getMapState() {
    return (MapStateBean)get("mapstate");
  }

  /**
   * Sets the MapState object 
   *
   * @param argMapState
   */
  public void setMapState(MapStateBean argMapState){
    put("mapstate", argMapState);
  }

  /**
   * Gets the options vector
   *
   * @return the current options vector
   */
  public Vector getOptions() {
    return (Vector)get("options");
  }

  /**
   * Sets the options vector
   *
   * @param argOptions
   */
  public void setOptions(Vector argOptions){
    put("options", argOptions);
  }

  /**
   * Gets the value of ui_version
   *
   * @return the value of ui_version
   */
  public String getUiVersion() {
    return (String)get("ui_version");
  }

  /**
   * Sets the value of ui_version
   *
   * @param argUiVersion 
   */
  public void setUiVersion(String argUiVersion){
    put("ui_version", argUiVersion);
  }

  /**
   * Gets the value of new_output_window
   *
   * @return the value of new_output_window
   */
  public Boolean getNewOutputWindow() {
    return (Boolean)get("new_output_window");
  }

  /**
   * Sets the value of new_output_window
   *
   * @param argNewOutputWindow 
   */
  public void setNewOutputWindow(Boolean argNewOutputWindow){
    put("new_output_window", argNewOutputWindow);
  }

  /**
   * Gets the value of new_output_window_name
   *
   * @return the value of new_output_window_name
   */
  public String getNewOutputWindowName() {
    return (String)get("new_output_window_name");
  }

  /**
   * Sets the value of new_output_window_name
   *
   * @param argNewOutputWindowName 
   */
  public void setNewOutputWindowName(String argNewOutputWindowName){
    put("new_output_window_name", argNewOutputWindowName);
  }

  public void setBrowser(Browser browser){
    put("browser", browser);
  }

  public void setLivemap(LiveMap livemap){
    put("livemap", livemap);
  }

  public LiveMap getLivemap(){
    return (LiveMap)get("livemap");
  }

  public void setTitle(String title){
    put("title", title);
  }

  public void setPopupData(PopupData arg){
    put("popup_data", arg);
  }

  public PopupData getPopupData(){
    return (PopupData)get("popup_data");
  }

}
