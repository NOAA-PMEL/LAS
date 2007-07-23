package gov.noaa.pmel.tmap.las.luis.db;


/**
 * @author $Author: sirott $
 * @version $Version$
 */
public class Institution extends DbaseObject {

  String name;
  String url;
  public Institution() {
    super("Institution");
    addField("name");
    addField("url");
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
}
