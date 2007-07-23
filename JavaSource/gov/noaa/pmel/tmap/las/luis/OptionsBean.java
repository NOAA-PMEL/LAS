// $Id: OptionsBean.java,v 1.5 2002/06/26 18:17:53 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.util.Properties;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;
import gov.noaa.pmel.tmap.las.luis.db.*;

import org.apache.velocity.*;
import java.lang.Thread;
import java.net.URL;
import java.net.URLConnection;
import org.apache.oro.text.perl.Perl5Util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.StringBuffer;
import java.util.TreeSet;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.sql.SQLException;
import java.io.IOException;


public class OptionsBean extends DefaultTemplateBean {
  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);
    HttpServletRequest req = tc.getServletRequest();
    HttpServletResponse resp = tc.getServletResponse();

    TemplateSession session = Utils.getSession(req);
    FormParameters params = session.getSessionObject().getConstrainState();
    if (params == null){
	noConstraintError(resp);
	return;
    }
    SessionTemplateContext sessionContext = session.getSessionContext();
    RegionConstraint rc = mHandler.getRegion();
    Log.debug(this, "Region constraint:" + rc);
    if (rc == null){
	noConstraintError(resp);
	return;
    }
    String output = params.get("output")[0];
    Vector ops = Utils.split("/,/", output);
    String op = (String)ops.elementAt(0);
    Log.debug(this, "Getting options for operations:" + op);

    UI ui = rc.getUI();
    Vector options = ui.getOptions(op);
    tc.put("options", options);
  }

  public boolean useTemplate() {return true; }
}




