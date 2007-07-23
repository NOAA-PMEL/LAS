// $Id: SearchBean.java,v 1.5.4.1 2005/05/03 15:27:32 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
import gov.noaa.pmel.tmap.las.luis.db.Search;
import java.util.Properties;
import java.util.Vector;
import java.util.ListIterator;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import gov.noaa.pmel.tmap.las.luis.db.*;
import org.apache.velocity.*;
import java.sql.SQLException;


public class SearchBean extends DefaultTemplateBean {
  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);
    Vector results = new Vector();
    tc.put("var_search", results);
    tc.put("cat_search", results);
    tc.put("terms", "");
    HttpServletRequest req = tc.getServletRequest();
    String term = req.getParameter("terms");
    if (term == null){
      return;
    }

    boolean isUnlimited = req.getParameter("unlimited") != null;
    tc.put("unlimited", new Boolean(isUnlimited));
    // Get rid of any leading/trailing whitespace
    term = term.replaceAll("^\\s+","");
    term = term.replaceAll("\\s+$","");
    if (term.equals("")){
      return;
    }
    tc.put("terms", term);

    // Only use valid SQL
    term = term.replaceAll("(['\"\\_\\%])", "\\\\$1");
    Log.debug(this, "Search term: " + term);
    String[] terms = term.split("\\s+");
    if (terms == null){
      return;
    }

    Search s = new Search(terms, isUnlimited);
    String sessionid = getSession().getId();

    // The Category results appear to contain duplicates, but they are really
    // different hierarchies.  Load up a CategoryHierarchy object so the template
    // can show the entire hierarchy so the user can tell the difference.
    Vector hierarchies = new Vector();
    results = s.getResults(sessionid, "c");
    ListIterator ri = results.listIterator();
    while ( ri.hasNext() ) {
       DatasetItem result = (DatasetItem)ri.next();
       CategoryHierarchy ch = result.getCategoryHierarchy();
       hierarchies.addElement(ch);
     }

     tc.put("cat_search", hierarchies);


    // Get only the "stable" variables so there will be no duplicates.
    // Use the contraint?varname=xxx&title=yyy form of the link in the template
    // so the user is dropped into the first category hierarchy that contains
    // this variable.
    //
    // Expand this hierachry, so this can appear on the page with the link.
    
    results = s.getResults(sessionid, "p");
    ri = results.listIterator();
    Vector var_hierarchy = new Vector();
    while (ri.hasNext()) {
       DatasetItem di = (DatasetItem)ri.next();
       CategoryHierarchy ch = di.getCategoryHierarchy();
       var_hierarchy.addElement(ch);
    }
    tc.put("var_search", var_hierarchy);
    tc.put("hide_variables", new Boolean(true));
  }
}
