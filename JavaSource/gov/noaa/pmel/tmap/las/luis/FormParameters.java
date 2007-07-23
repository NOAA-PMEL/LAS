// $Id: FormParameters.java,v 1.8 2002/08/24 23:03:03 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;

import java.lang.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

public class FormParameters {
  TreeMap mParameters = new TreeMap();
  public FormParameters(HttpServletRequest req){
    for (Enumeration e = req.getParameterNames(); e.hasMoreElements();){
      String name = (String)e.nextElement();
      String[] values = req.getParameterValues(name);
      add(name,values);
    }
  }

  public FormParameters(){
  }

  public void add(String key, String[] values){
    mParameters.put(key, values);
  }

  /**
   * Return a URL query string from the stored parameters
   * @return url query string
   */
  public String toUrl() {
    StringBuffer str = new StringBuffer("?");
    for (Iterator itr = keys(); itr.hasNext(); ){
      String[] values = get((String)itr.next());
      if (values != null){
	for (int i=0; i < values.length; ++i){
	  str.append(values[i]).append("&");
	}
      }
    }
    int length = str.length();
    if (length > 1){
      return str.substring(0, length-1);
    } else {
      return str.toString();
    }
  }

  public String toString() {
    StringBuffer str = new StringBuffer();
    for (Iterator itr = keys(); itr.hasNext();){
      String key = (String)itr.next();
      String[] values = get(key);
      str.append("Name=").append(key).append("\n");
      str.append("Values:");
      for (int i=0; i < values.length; ++i){
	str.append(" ").append(values[i]);
      }
      str.append("\n");
    }
    return str.toString();
  }

  /**
   * Add existing parameters to these parameters
   */
  public void add(FormParameters in){
    for (Iterator itr = in.keys(); itr.hasNext(); ){
      Object key = itr.next();
      mParameters.put(key, in.get((String)key));
    }
  }

  public Iterator keys() {
    return mParameters.keySet().iterator();
  }

  public String[] get(String value){
    return (String[])mParameters.get(value);
  }

  public void put(String name,String value){
    mParameters.put(name,value);
  }

  public void remove(String name){
    mParameters.remove(name);
  }

}
