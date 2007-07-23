package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import java.sql.SQLException;

import gov.noaa.pmel.tmap.las.luis.db.Browser;
import gov.noaa.pmel.tmap.las.luis.db.BrowserCandidate;
import gov.noaa.pmel.tmap.las.luis.Log;

public class BrowserFormBean extends AbstractFormBean {
  
  protected FormParamHandler.Variable mHandler;

  public boolean isValid(String nextUrl) throws ServletException, SQLException{
    Log.debug(this, "Returning 'true' from isValid.");
    return true;
  }

  public void init(HttpServletRequest req)
    throws ServletException, SQLException {
    Log.debug(this, "Init of BrowserFormBean.");
    super.init(req);
    mHandler = new FormParamHandler.SingleVariable(Utils.getSession(mReq));
  }

  public void handle() throws ServletException, SQLException {

    Log.debug(this,"HANDLING the BrowserForm.");
    TemplateSession session = Utils.getSession(mReq);
    SessionTemplateContext sessionContext = session.getSessionContext();
    String sid = session.getId();

    FormParameters params = getParameters();

    // Get the agent string being submitted.
    String agent=null;
    if (params.get("agent") != null){
       String[] agents = (String[])mParams.get("agent");
       agent = agents[0];
    }
    // Get whether the user believes it is compatible or not.
    String applet=null;
    if (params.get("applet") != null){
       String[] applets = (String[])mParams.get("applet");
       applet = applets[0];
    }

    // Check to see if this agent is already in the list of browser candidates.

    BrowserCandidate bc = new BrowserCandidate();
    boolean notcandidate = false;
    try {
       bc.deserialize(Utils.quote(agent), "agent");
    } catch (SQLException e) {
       Log.debug(this,"Check candidates: "+e.getMessage());
       notcandidate = true;
    }

    // Check to see if this agent string has already been entered as
    // a rejected or compatible browser.
    Browser b = new Browser();
    boolean add = false;
    try {
       b.deserialize(Utils.quote(agent),"agent");
    } catch (SQLException e) {
       Log.debug(this,"Check browser: "+e.getMessage());
      add=true;
    }

    // This agent does not appear in the database, so add it.

    if (add && notcandidate) {
       Log.debug(this, "Adding agent: "+agent);
       BrowserCandidate addb = new BrowserCandidate();
       addb.init(agent);
       if ( applet.equals("true") ) {
          addb.setApplet("1");
       } else {
          addb.setApplet("0");
       }
       addb.setApplet(applet);
       int nextID = addb.getLastOid()+1;
       addb.setOid(nextID);
       addb.serialize();

       /*
        * I think this is probably a bad idea.  This should either be a utility
        * of part of genLas so that the user can dump it when they want or
        * gets dumped before genLas drops the table.
       // There was a change so dump the browser database to the browsers.xml

       Vector allBrowsers = addb.deserializeAggregate();

       Log.debug(this,"Found "+allBrowsers.size()+" browsers to dump.");
       Iterator bit = allBrowsers.iterator();

       try {
          // A hack to dump out the XML.  Better XML tools are available (like JDom),
          // but might be too big a hammer for this job.
          FileOutputStream bfile = new FileOutputStream("browsers.xml");
          PrintWriter bout = new PrintWriter(bfile);

          bout.println("<las_browsers>");

          String tapp="";
          while (bit.hasNext()) {
             Browser bwsr = (Browser)bit.next();
             if (bwsr.getApplet().equals("1")) {
                tapp = "true";
             }
             else {
                tapp = "false";
             }
             bout.println("\t<browser agent=\""+bwsr.getAgent()+"\" applet=\""+tapp+"\"/>");
          }

          bout.println("</las_browsers>");
          bout.close();
       } catch (FileNotFoundException e) {
          Log.error(this, "Browser xml not written: "+e.getMessage());
       }
       */

    }
    else {
       Log.debug(this, "Agent: "+agent+" will NOT BE ADDED.");
    }
    
  }


  public String nextURL() throws ServletException, SQLException {
     Log.debug(this,"Returning 'dataset' from nextURL.");
    return "dataset";
  }

}
