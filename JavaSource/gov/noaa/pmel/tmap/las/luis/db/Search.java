// $Id: Search.java,v 1.3 2002/11/30 23:42:51 sirott Exp $
package gov.noaa.pmel.tmap.las.luis.db;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      NOAA/PMEL/TMAP
 * @author Joe Sirott
 * @version 1.0
 */

import gov.noaa.pmel.tmap.las.luis.DatasetItem;
import gov.noaa.pmel.tmap.las.luis.db.*;
import java.util.Vector;
import java.sql.SQLException;
import java.net.URL;
import java.net.MalformedURLException;
import gov.noaa.pmel.tmap.las.luis.Log;
import java.util.List;
import java.util.Iterator;
import java.lang.String;

public class Search {
  String[] mTerms;
  static int MAX_RETURNED_ITEMS = 50;
  int mMaxReturned = MAX_RETURNED_ITEMS;
  public Search(String[] terms, boolean isUnlimited) {
    super();
    mTerms = terms;
    if (isUnlimited){
      mMaxReturned = 1 << 31;
    }
  }

  public Search() {
    super();
  }

  public Vector getResults(String sessionid, String type) throws SQLException {
    StringBuffer constraint = new StringBuffer(" where ");
    for (int i=0; i < mTerms.length; ++i){
      if (i > 0){
	constraint.append(" and ");
      }
      constraint.append("name like '%").append(mTerms[i])
	.append("%'");
    }
    if (type != null){
      constraint.append(" and type='").append(type).append("'");
    }
    constraint.append(" order by parentid,name limit " + mMaxReturned);
    Category cat = new Category();
    Vector cats = cat.deserializeAggregate(null,null,constraint.toString());
    Vector rval = new Vector();
    for (Iterator i = cats.iterator(); i.hasNext(); ){
      rval.add(DatasetItem.getInstance((ICategory)i.next()));
    }

    // User defined variables. Should be relatively few, so we'll do search
    // in Java rather than SQL (which would be ugly)
    if (type.equals("v")){
      DerivedCategory dc = new DerivedCategory();
      String extra = "where sessionid='" + sessionid + "' order by name limit " + mMaxReturned;
      cats = dc.deserializeAggregate(null, null, extra);
      for (Iterator i = cats.iterator(); i.hasNext(); ){
	ICategory icat = (ICategory)i.next();
	boolean useIt = true;
	// Search for terms
	for (int index=0; index < mTerms.length; ++index){
	  String name = icat.getName().toLowerCase();
	  if (name.indexOf(mTerms[index].toLowerCase()) < 0){
	    useIt = false;
	    break;
	  }
	}
	if (useIt){
	  rval.add(DatasetItem.getInstance(icat));
	}
      }
    }
    return rval;
  }

  public static void main(String[] args){
    if (args.length < 1){
      System.err.println("Usage: search [terms]");
      System.exit(1);
    }
    Search s = new Search(args, false);
    try {
      Vector results = s.getResults("12345", "c");
      for (Iterator i = results.iterator(); i.hasNext(); ){
	Search sresult = (Search)i.next();
	System.out.println(sresult.toString());
      }
    } catch (Exception e){
      e.printStackTrace();
    }
  }
}
