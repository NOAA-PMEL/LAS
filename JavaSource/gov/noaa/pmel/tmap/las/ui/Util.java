package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.util.Container;
import gov.noaa.pmel.tmap.las.util.Option;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class Util {
    public static String toXML(ArrayList containers, String wrapper) {
        
        StringBuffer xml = new StringBuffer();
        xml.append("<"+wrapper+">");
        for (Iterator conIt = containers.iterator(); conIt.hasNext();) {
            Container container = (Container) conIt.next();
            xml.append(container.toXML());
        }
        
        xml.append("</"+wrapper+">");
        
        return xml.toString();
    }

    public static JSONObject toJSON(ArrayList containers, String wrapper) throws JSONException {
        // If there is only one element in the list, build our own JSON object.
        // This is necessary because the json.org code "flattens" out the array
        // if there is only one element.
        if (containers.size() == 1) {
            JSONObject json_noarray = XML.toJSONObject(Util.toXML(containers, wrapper));
            
            // This is the outer container:  e.g. "options"
            Iterator outer_keys = json_noarray.keys();
            String outer_key = (String) outer_keys.next();
            JSONObject outer = json_noarray.getJSONObject(outer_key);
            
            // This is the inner container: e.g. "option" which should contain an array.
            Iterator inner_keys = outer.keys();
            String inner_key = (String) inner_keys.next();
            JSONObject inner = outer.getJSONObject(inner_key);
            
            // Build the new object with the inner array
            JSONObject new_outer = new JSONObject();
            JSONObject new_inner = new JSONObject();
            JSONArray inner_array = new JSONArray();

            inner_array.put(inner);
            new_inner.put(inner_key, inner_array);
            new_outer.put(outer_key, new_inner);
            
            return new_outer;
        // Otherwise let the json.org code handle it.
        } else if ( containers.size() > 1 ) {
            return XML.toJSONObject(Util.toXML(containers, wrapper));
        } else {
            return null;
        }
        
        
        
    }
}
