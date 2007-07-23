// $Id: IndexBean.java,v 1.4.4.1 2005/05/03 15:23:25 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;
import java.util.ListIterator;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import gov.noaa.pmel.tmap.las.luis.db.*;
import gov.noaa.pmel.tmap.las.luis.*;
import org.apache.velocity.*;
import java.sql.SQLException;


public class IndexBean extends DefaultTemplateBean {
   public void init(TemplateContext tc) throws ServletException, SQLException {
      super.init(tc);
      HttpServletRequest req = tc.getServletRequest();

      int resultsperpage = 25;

      String page = req.getParameter("page");

      // Get the "permanent links" information...
      Category c = new Category();
      Vector datasets = c.deserializeAggregate("'d'", "type", " order by name");
      tc.put("datasets", datasets);

      // Get the "Category Hierarchies"...
      PathIndex ic = new PathIndex();
      int size = ic.size();
      int totalpages = (int)Math.ceil((double)size/(double)resultsperpage);

      String where = null;
      String next=null;
      String previous=null;

      int pagenum;
      // Start at the beginning
      if ( page == null ) {
         where = "where oid>=1 and oid<"+resultsperpage+" order by oid";
         previous = null;
         if ( totalpages > 1 ) {
            next = "index?page=2";
         }
         pagenum = 1;
      }
      else {
         pagenum = Integer.valueOf(page).intValue();
         // A page number before the beginning returns the beginning.
         if (pagenum <= 1 ) {
            where = "where oid>=1 and oid<"+resultsperpage+" order by oid";
            previous = null;
            if ( totalpages > 1 ) {
               next = "index?page=2";
            }
            pagenum = 1;
         }
         // A page number off the end returns the end.
         else if ( pagenum >= totalpages ) {
            int start = size-resultsperpage;
            where = "where oid>="+start+" order by oid";
            int prev = totalpages-1;
            previous = "index?page="+prev;
            next = null;
            pagenum = totalpages;
         }
         else {
            int ilo = (pagenum-1)*resultsperpage;
            int ihi = pagenum*resultsperpage;
            where = "where oid>="+ilo+" and oid<"+ihi+" order by oid";
            int prev=pagenum-1;
            int nex=pagenum+1;
            previous = "index?page="+prev;
            next = "index?page="+nex;
         }
      }

      Vector pages = new Vector();

      for ( int p=0; p < totalpages; p++) {
         String apage;
         int pp=p+1;
         if (pagenum == pp) {
            apage="";
         }
         else {
            apage = "index?page="+pp;
         }
         pages.add(apage);
      }

      tc.put("previous", previous);
      tc.put("next", next);
      tc.put("pages", pages);

      Vector paths = ic.selectColumn("path", where, true);
      Vector ids = ic.selectColumn("oid", where, true);
      ListIterator pathsIt = paths.listIterator();
      ListIterator idsIt = ids.listIterator();
      Vector hierarchy = new Vector();
      while (pathsIt.hasNext()) {
         String path = (String)pathsIt.next();
         Vector parts = Utils.split("/:/", path);
         String leafNode = (String)parts.lastElement();
         Category ac = new Category();
         ac.deserialize(leafNode);
         DatasetItem di = DatasetItem.getInstance(ac);
         CategoryHierarchy ch = di.getCategoryHierarchy();
         //String t = (String)idsIt.next();
         //Log.debug(this,"Setting index: "+t);
         ch.setIndex((String)idsIt.next());
         //ch.setIndex(t);
         hierarchy.addElement(ch);
      }

      tc.put("categories", hierarchy);
      tc.put("hide_variables", new Boolean(true));

   }
}
