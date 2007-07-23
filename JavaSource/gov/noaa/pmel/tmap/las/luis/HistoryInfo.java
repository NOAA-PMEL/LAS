// $Id: HistoryInfo.java,v 1.6 2002/12/18 21:10:29 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.lang.String;
import java.lang.StringBuffer;
import java.util.*;
import java.sql.*;
import gov.noaa.pmel.tmap.las.luis.db.*;
import javax.servlet.*;
import java.util.HashSet;
import java.util.Date;
import java.lang.Comparable;
import gov.noaa.pmel.tmap.las.luis.DatasetItem;

public class HistoryInfo implements Comparable {
  String mVariable;
  Object[] mProducts;
  String mUrl;
  Date mDate = new Date();
  Vector mRange = new Vector();
  private HistoryInfo(FormParameters param, RegionConstraint rc, String url,
		      DatasetItem dataset)
    throws SQLException {
    mUrl = url;
    ICategory cat = rc.getCategory();
    Vector v = new Vector();
    int count = 0;
    for (Iterator i = dataset.getPath().iterator(); i.hasNext();
	 ++count){
      DatasetPathItem item = (DatasetPathItem)i.next();
      if (count>0){
	v.addElement(item.getPath());
      }
    }
    mVariable = Utils.join(">", v);
    mVariable += ">" + cat.getName();
    mProducts = Utils.split("/,/", param.get("output")[0]).toArray();
    IVariableInfo varInfo = cat.getVariableInfo();
    for (Iterator i = varInfo.getAxes().iterator(); i.hasNext(); ){
      IAxis ax = (IAxis)i.next();
      String type = ax.getType();
      String loName = type + "_lo";
      String hiName = type + "_hi";
      String value;
      String[] loArray = param.get(loName);
      String[] hiArray = param.get(hiName);
      if (loArray != null){
	if (rc.isRange(type)){
	  value = type + "(" + param.get(loName)[0] + "," + param.get(hiName)[0] + ")";
	} else {
	  value = type + "(" + param.get(loName)[0] + ")";
	}
	mRange.addElement(value);
      }
    }
  }


  public int compareTo(Object o){
    HistoryInfo in = (HistoryInfo)o;
    if (toString().equals(in.toString())){
      return 0;
    }
    return -mDate.compareTo(in.mDate);
  }

  public static HistoryInfo getInstance(FormParameters params, RegionConstraint rc,
					String url, DatasetItem dataset) throws SQLException {
    return new HistoryInfo(params,rc,url,dataset);
  }

  public String toString() {
    return mVariable + "/" + mProducts[1] + "/" + Utils.join("/", mRange);
  }

  public String getUrl() {
    return mUrl;
  }

}
