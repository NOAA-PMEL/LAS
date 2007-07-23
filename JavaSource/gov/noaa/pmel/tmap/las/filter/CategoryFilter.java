package gov.noaa.pmel.tmap.las.filter;

import org.jdom.Element;
import org.jdom.filter.Filter;

public class CategoryFilter implements Filter {
    String ID;
    public CategoryFilter(String id) {
        ID = id;
    }
    public boolean matches(Object obj) {
        Element element=null;
        if (obj instanceof Element) {
            element = (Element) obj;
        } else {
            return false;
        }
        
        if (element.getName().equals("category")) {
            if (element.getAttributeValue("ID").equals(ID)) {
                return true;
            }
            else {
                return false;
            }
        } else {
            return false;
        }

    }
}
