// $Id: DataBean.java,v 1.25.4.4 2005/12/06 23:25:48 rhs Exp $
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
import java.io.PrintWriter;

public class DataBean extends DefaultTemplateBean {

    private void handleCustom(HttpServletResponse resp, SessionObject sobj,
            RegionConstraint rc) throws ServletException, SQLException {

        try {
            ICategory[] cats = rc.getCategories();
            ICategory theCat = cats[0];
            String customUrl = theCat.getVariableInfo().getCustomUrl();
            if (customUrl == null || customUrl.equals("")) {
                return;
            }
            // Only allow one variable for custom var interface
            if (cats.length > 1) {
                String result = "toomanyvars?maxvars=1";
                resp.sendRedirect(result);
                return;
            }

            String lastTemplateName = sobj.getLastTemplateName();
            Log.debug(this, "DataBean: last template was:" + lastTemplateName);
            if (lastTemplateName.startsWith(customUrl)) {
                getSession().getSessionObject().setCustom(false);
                return;
            }

            getSession().getSessionObject().setCustom(true);
            resp.sendRedirect(customUrl);
        } catch (Exception e) {
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new ServletException(e);
        }
    }

    public void init(TemplateContext tc) throws ServletException, SQLException {
        super.init(tc);

        try {
            HttpServletRequest req = tc.getServletRequest();
            HttpServletResponse resp = tc.getServletResponse();
            TemplateSession session = Utils.getSession(req);
            SessionObject sobj = session.getSessionObject();
            Log.debug(this, "Last template:" + sobj.getLastTemplateName());
            FormParameters params = sobj.getConstrainState();
            if (params == null) {
                noConstraintError(resp);
                return;
            }

            XmlRequester xml = null;

            // Hard-wire this to true since the options show up on
            // the contraints page.
            boolean useOptions = true;
            sobj.setUseOptions(new Boolean(true));

            SessionTemplateContext sessionContext = session.getSessionContext();
            boolean doCompare = getTemplateName().startsWith("data_compare");
            Log.debug(this, "doCompare:" + doCompare);
            boolean isNewOutputWindow = sessionContext.getNewOutputWindow()
                    .booleanValue();

            RegionConstraint rc;
            if (doCompare) {
                String[] variables = sobj.getCompareVariables(0).get(
                        "variables");
                rc = sessionContext.getCompareRegion(0);
                // If variable has changed, RegionConstraint no longer valid
                if (!(rc == null || rc.getCategory().getOid().equals(
                        variables[0]))) {
                    Log.debug(this,
                            "Variable changed; removing RegionConstraint");
                    sessionContext.remove("compare_region");
                    sessionContext.remove("compare_category");
                    rc = null;
                }
                Log.debug(this, "Region constraint:" + rc);

                if (rc == null) {
                    noConstraintError(resp);
                    return;
                }

                variables = sobj.getCompareVariables(1).get("variables");
                RegionConstraint crc = sessionContext.getCompareRegion(1);
                if (!(crc == null || crc.getCategory().getOid().equals(
                        variables[0]))) {
                    Log.debug(this,
                            "Variable changed; removing RegionConstraint");
                    sessionContext.remove("compare_region");
                    sessionContext.remove("compare_category");
                    crc = null;
                }
                if (crc == null) {
                    noConstraintError(resp);
                    return;
                }

                xml = XmlRequester.getInstance(params, rc);
                xml.setCompareConstraints(sessionContext.getCompareRegion(1));
            } else {
                String[] variables = sobj.getVariables().get("variables");
                rc = sessionContext.getRegion();
                // If variable has changed, RegionConstraint no longer valid
                if (!(rc == null || rc.getCategory().getOid().equals(
                        variables[0]))) {
                    Log.debug(this,
                            "Variable changed; removing RegionConstraint");
                    sessionContext.remove("region");
                    sessionContext.remove("category");
                    rc = null;
                }
                Log.debug(this, "Region constraint:" + rc);

                if (rc == null) {
                    noConstraintError(resp);
                    return;
                }

                // Handle any custom pages
                handleCustom(resp, sobj, rc);

                // Make sure number of requested variables is less than max
                // number
                // of allowed variables
                int maxvars = 1;
                String output = params.get("output")[0];
                Vector op = Utils.split("/,/", output);
                // This is a kludge to handle property/property plots.
                // This should be done better when new XML definitions are
                // finished.

                String method = (String) op.elementAt(0);
                if (method.equals("insitu_property") && variables.length < 2) {
                    resp.sendRedirect("toofewvars?minvars=2");
                    return;
                }
                if (method.equals("insitu_property") && variables.length > 2) {
                    resp.sendRedirect("toomanyvars?maxvars=2");
                    return;
                }

                // End of kludge
                if (op.size() > 2) {
                    String val = (String) op.elementAt(2);
                    try {
                        maxvars = Integer.parseInt(val);
                    } catch (NumberFormatException e) {
                        Log.debug(this, "Bad integer:" + val);
                    }
                }
                if (variables.length > maxvars) {
                    String result = "toomanyvars?maxvars=" + maxvars;
                    resp.sendRedirect(result);
                    return;
                }

                xml = XmlRequester.getInstance(params, rc);
            }

            if (useOptions) {
                xml.setOptions(sobj.getOptions());
            }
            xml.setCustom(sobj.getCustomFormParameters());
            sessionContext.setXml(xml);
            String requestedXML = xml.toString();

            Config config = rc.getCategory().getConfig();
            Vector datasets = sessionContext.getDatasets();
            DatasetItem dataset = (DatasetItem) datasets.elementAt(0);

             HistoryList historyList = (HistoryList) sessionContext
                        .getHistoryList();
             if (historyList == null) {
                 historyList = new HistoryList();
                 sessionContext.setHistoryList(historyList);
             }

             HistoryInfo history = HistoryInfo.getInstance(params, rc,
                     requestedXML, dataset);
             historyList.add(history);
             String JSESSIONID = session.getId();
            String requesturl = config.getServerurl()+"?xml="+ java.net.URLEncoder.encode(requestedXML)+"&JSESSIONID="+JSESSIONID;
            resp.sendRedirect(requesturl);
            return;

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public boolean useTemplate() {
        return true;
    }
}
