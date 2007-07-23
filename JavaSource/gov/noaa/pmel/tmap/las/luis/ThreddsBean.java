package gov.noaa.pmel.tmap.las.luis;
import java.util.Properties;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import gov.noaa.pmel.tmap.las.luis.db.*;
import org.apache.velocity.*;
import java.sql.SQLException;


public class ThreddsBean extends DefaultTemplateBean {

  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);

    HttpServletRequest req = tc.getServletRequest();
    HttpServletResponse resp = tc.getServletResponse();

    String rand = Double.toString(Math.random());

    String theXML = "<?xml version=\"1.0\"?><lasRequest href=\"file:las.xml\" useCache=\"false\"><link match=\"/lasdata/operations/operation[@ID='THREDDS']\"/><args></args></lasRequest>";

    TemplateSession session = Utils.getSession(req);
    SessionTemplateContext sessionContext = session.getSessionContext();
    Vector configs = sessionContext.getConfigs();
    Config config = (Config)configs.get(0);
    String base = config.getServerurl();
    String target = base + "?xml=" + java.net.URLEncoder.encode(theXML);
    tc.put("dataurl", target);
    Log.debug(this, "Fetching url: " + target);
    try {
    URL url = new URL(target);
    URLConnection conn = url.openConnection();
    conn.connect();
    InputStream istream = conn.getInputStream();
    //resp.setContentType("text/xml");
    char[] buf = new char[4096];
    BufferedReader is =
       new BufferedReader(new InputStreamReader(istream));
    StringBuffer sbuf = new StringBuffer();
    int length = is.read(buf,0,4096);
    while (length>=0) {
       sbuf.append(buf,0,length);
       length = is.read(buf,0,4096);
    }
    String result = sbuf.toString();
    tc.put("result", result);
    } catch (Exception e){
      throw new ServletException(e);
    }
    Log.debug(this, "Done with THREDDS bean.  Happy?");
  }


}




