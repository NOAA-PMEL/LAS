package gov.noaa.pmel.tmap.las.util;

import gov.noaa.pmel.tmap.las.ui.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.json.JSONException;
import org.json.JSONObject;

public class Category extends Container implements CategoryInterface {
    public Category(Element category) {
        super(category);
    }
    public boolean hasVariableChildren() {
        Element dataset = element.getChild("dataset");
        Element variables = null;
        if ( dataset != null ) {
            variables = dataset.getChild("variables");
        }
         
        List vars = new ArrayList();
        
        if ( variables != null ) {
            vars = variables.getChildren("variable");
        }
        
        if ( variables != null && vars.size() > 0) {
            return true;
        }
        return false;
    }
    public JSONObject toJSON() throws JSONException {
        ArrayList<String> asArrays = new ArrayList<String>();
        asArrays.add("variable");
        asArrays.add("category");
        return Util.toJSON(element, asArrays);
    }
}
