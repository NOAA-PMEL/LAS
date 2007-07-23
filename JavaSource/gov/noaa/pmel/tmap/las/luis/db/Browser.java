// $Id: Browser.java,v 1.3 2005/02/02 21:40:00 rhs Exp $ 
package gov.noaa.pmel.tmap.las.luis.db;
import gov.noaa.pmel.tmap.las.luis.Log;
import gov.noaa.pmel.tmap.las.luis.Utils;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.String;
import junit.framework.*;
import java.sql.SQLException;

import java.util.Vector;
import java.util.Iterator;

/**
 * Java Browser Sniffer
 */
public class Browser extends DbaseObject {
  String agent, applet;
  String mBrowser = "unknown";
  String mOS = "unknown";
  String mAgent = "unknown";

  public Browser() {
    super("Browser");
    addField("agent");
    addField("applet");
  }

  public Browser(String b) {
    super(b);
    addField("agent");
    addField("applet");
  }

  public void init(HttpServletRequest req) {
    this.init(req.getHeader("User-Agent"));
  }

  public void init(String agent){
    if (agent == null){
      return;
    }
    parse(agent);
  }

  void parse(String agent){
    this.agent = agent;
    mAgent = agent;
    agent = agent.toLowerCase();
    int loc = agent.indexOf("windows");
    if (loc >= 0){
       mOS = "win";
    }

    loc = agent.indexOf("mac");
    if (loc >= 0) {
       mOS = "mac";
    }
    loc = agent.indexOf("linux");
    if (loc >= 0) {
       mOS = "linux";
    }

       
    loc = agent.indexOf("opera");
    if (loc >= 0){
      mBrowser = "opera";
      return;
    }
    loc = agent.indexOf("msie");
    if (loc >= 0){
      mBrowser = "ie";
      return;
    }
    loc = agent.indexOf("mozilla");
    if (loc >= 0){
      String[] strings = agent.split("\\/");
      if (strings.length < 2){
        mBrowser = "unknown";
        return;
      }
      String[] versions = strings[1].split("\\.");
      int major = Integer.parseInt(versions[0]);
      if (major < 5){
        mBrowser = "netscape";
      } else {
        mBrowser = "mozilla";
      }
    }
  }

  public boolean isNetscape() {
    return mBrowser.equals("netscape");
  }

  public boolean isIE() {
    return mBrowser.equals("ie");
  }

  public boolean isMozilla() {
    return mBrowser.equals("mozilla");
  }

  public boolean isWin() {
     return mOS.equals("win");
  }

  public boolean isMac() {
     return mOS.equals("mac");
  }

  public boolean isLinux() {
     return mOS.equals("linux");
  }

  public boolean isRejected() throws SQLException {

     Browser b = new Browser();
     try {
        b.deserialize(Utils.quote(this.agent), "agent", " and applet='0'");
        return true;
     } catch (SQLException e) {
        // Not finding a match is an exception.
        if ( e.getMessage().indexOf("No match for id") >= 0 ) {
           return false;
        }
        else {
           throw new SQLException(e.getMessage());
        }
     }

  }

  public boolean isCompatible() throws SQLException {

     Browser b = new Browser();
     try {
        b.deserialize(Utils.quote(this.agent), "agent", " and applet='1'");
        return true;
     } catch (SQLException e) {
        // Not finding a match is an exception.
        if ( e.getMessage().indexOf("No match for id") >= 0 ) {
           return false;
        }
        else {
           throw new SQLException(e.getMessage());
        }
     }

  }

  public String getBrowser() {
    return mBrowser;
  }

  public String getAgent() {
    return this.agent;
  }

  public String getApplet() {
    return this.applet;
  }

  public void setApplet(String applet) {
     this.applet = applet;
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(TestBrowser.suite());
  }
}

class TestBrowserClass extends TestCase {
  String agent, expected;
  public TestBrowserClass(String agent, String expected) {
    super("Browser class");
    this.agent = agent;
    this.expected = expected;
  }

  protected void runTest() throws Throwable {
    Browser c = new Browser();
    c.init(agent);
    assertEquals(expected, c.getBrowser());
  }

}

class TestBrowser {
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new TestBrowserClass("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:0.9.9) Gecko/20020408",
           "mozilla"));
    suite.addTest(new TestBrowserClass("Mozilla/4.79 [en] (X11; U; Linux 2.4.18-3 i686)", "netscape"));
    suite.addTest(new TestBrowserClass("Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.0  [en]",
           "opera"));
    suite.addTest(new TestBrowserClass("Mozilla/4.79 [en] (Windows NT 5.0; U)", "netscape"));
    suite.addTest(new TestBrowserClass("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.0.0) Gecko/20020530", "mozilla"));
    suite.addTest(new TestBrowserClass("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; T312461; .NET CLR 1.0.3705)", "ie"));
    suite.addTest(new TestBrowserClass("Mozilla/5.0 (compatible; Konqueror/2.1.2; X11)", "mozilla"));
    suite.addTest(new TestBrowserClass("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:0.9.4) Gecko/20011126 Netscape6/6.2.1", "mozilla"));
    suite.addTest(new TestBrowserClass("some incomprehensible string", "unknown"));
    return suite;
  }
}

