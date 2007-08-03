package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Container;
import gov.noaa.pmel.tmap.las.util.Option;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
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

	/*
	 * This does not work because it eats the Array container when there is only one element.
	 
	public static JSONObject toJSON(ArrayList containers, String wrapper) throws JSONException {

		return XML.toJSONObject(Util.toXML(containers, wrapper));

	}

	*/
    //Various abandoned attempts that need to be removed in due time...
    public static JSONObject toJSON_keep_array (ArrayList containers, String wrapper) throws JSONException {
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
/*
    public static JSONObject toJSONarray(ArrayList containers, String wrapper) throws JSONException {
        JSONObject json_response = new JSONObject();
        JSONObject container_object = new JSONObject();
        for (Iterator conIt = containers.iterator(); conIt.hasNext();) {
            Container con = (Container) conIt.next();
            JSONObject container = con.toJSON();
            String name = (String) container.keys().next();
            JSONArray contents = (JSONArray)container.get(name);
            container_object.array_accumulate(name,contents.get(0));
        }
        json_response.put(wrapper, container_object);
        return json_response;
    }

    public static JSONObject toJSONarrays(ArrayList containers, String wrapper, ArrayList arrays) throws JSONException {
        JSONObject json_response = new JSONObject();
        JSONObject container_object = new JSONObject();
        for (Iterator conIt = containers.iterator(); conIt.hasNext();) {
            Container con = (Container) conIt.next();
            // Categories is the only thing that uses this method.  :-)

            JSONObject container = con.toJSON();
            for (Iterator keys  = (Iterator) container.keys(); keys.hasNext();) {
                String key = (String) keys.next();
                JSONObject subcontainer = container.getJSONObject(key);

                boolean hasvars = false;
                Category cat = null;
                if ( con instanceof Category ) {
                    cat = (Category)con;
                    hasvars = cat.hasVariableChildren();
                }
                if ( hasvars ) {
                    JSONObject sub_container_object = new JSONObject();
                    for (Iterator vkeys = (Iterator) subcontainer.keys(); vkeys.hasNext(); ) {
                        String vkey = (String) vkeys.next();
                        JSONObject subsubcontainer = subcontainer.getJSONObject(vkey);
                        if ( arrays.contains(key) ) {
                            sub_container_object.array_accumulate(vkey, subsubcontainer);
                        } else {
                            sub_container_object.accumulate(vkey, subsubcontainer);
                        }
                    }

                    if ( arrays.contains(key) ) {
                        container_object.array_accumulate(key, subcontainer);
                    } else {
                        container_object.accumulate(key, subcontainer);
                    }
                }  
            }
        }
        json_response.put(wrapper, container_object);
        return json_response;
    }
	 */
	public static JSONObject toJSON(Element element, ArrayList<String> asArrays) throws JSONException {

		JSONObject json = new JSONObject();
		List attributes = element.getAttributes();
		for (Iterator attrIt = attributes.iterator(); attrIt.hasNext();) {
			Attribute attr = (Attribute) attrIt.next();
			json.accumulate(attr.getName(), attr.getValue());
		}
		List children = element.getChildren();
		if ( children.size() > 0 ) {
			for (Iterator childIt = children.iterator(); childIt.hasNext();) {
				Element child = (Element) childIt.next();
				if ( asArrays.contains(child.getName())) {
					if ( child.getChildren().size() > 0 ) {
						json.array_accumulate(child.getName(), toJSON(child, asArrays));
					} else {
						json.accumulate(child.getName(), child.getTextTrim());
					}
				} else {
					if ( child.getChildren().size() > 0 ) {
						json.accumulate(child.getName(), toJSON(child, asArrays));
					} else {
						if ( child.getTextTrim().length() > 0 && child.getAttributes().size() == 0 ) {
							json.accumulate(child.getName(), child.getTextTrim());
						} else {
							json.accumulate(child.getName(), toJSON(child, asArrays));
						}
					}
				}
			}
		} else {
			if ( element.getTextTrim().length() > 0 ) { 
				json.accumulate("content", element.getTextTrim() );
			}
		}
		return json;
	}
}

