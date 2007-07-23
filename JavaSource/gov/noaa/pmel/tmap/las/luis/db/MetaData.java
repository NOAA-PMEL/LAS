// $Id: MetaData.java,v 1.3 2002/08/23 00:02:08 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;

import java.lang.String;
import java.net.URL;
import java.net.MalformedURLException;


/**
 * @author $Author: sirott $
 * @version $Version$
 */

public class MetaData extends DbaseObject {
  String docurl;
  public MetaData() {
    super("MetaData");
    addField("docurl");
  }
	   
  public String getDocUrl() {
    if (this.docurl == null || this.docurl.equals("")){
      return "";
    }
    URL url;
    try {
      url = new URL(this.docurl);
    } catch (MalformedURLException e){
      return "../" + this.docurl;
    }
    String proto = url.getProtocol();
    if (proto.startsWith("http") ||
	proto.startsWith("ftp")){
      return url.toString();
    }
    return "../" + this.docurl;
  }

}
