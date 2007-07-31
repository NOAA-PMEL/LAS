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

    public static JSONObject toJSON(ArrayList containers, String wrapper) throws JSONException {
        
        return XML.toJSONObject(Util.toXML(containers, wrapper));
        
    }
    
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

    public static JSONObject toJSON(Element element, ArrayList<String> asArrays) throws JSONException {
        
        JSONObject json = new JSONObject();
        List attributes = element.getAttributes();
        for (Iterator attrIt = attributes.iterator(); attrIt.hasNext();) {
            Attribute attr = (Attribute) attrIt.next();
            json.accumulate(attr.getName(), attr.getValue());
        }
        List children = element.getChildren();
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
        return json;
    }
}

