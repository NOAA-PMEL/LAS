package gov.noaa.pmel.tmap.las.luis.db;


/**
 * @author $Author: rhs $
 * @version $Version$
 */
public class Contributor extends DbaseObject {

  String parentid;
  String name;
  String url;
  String role;
  public Contributor() {
    super("Contributor");
    addField("parentid");
    addField("name");
    addField("url");
    addField("role");
  }
  public String getParentid() {
    return parentid;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String getUrl() {
    return url;
  }
  public void setRole(String role) {
    this.role = role;
  }
  public String getRole() {
    return role;
  }
}
