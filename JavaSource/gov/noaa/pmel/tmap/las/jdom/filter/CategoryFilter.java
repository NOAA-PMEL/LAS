/**
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development. 
 */
package gov.noaa.pmel.tmap.las.jdom.filter;

import org.jdom.Element;
import org.jdom.filter.Filter;

/**
 * This class builds a filter that will find categories by ID.
 * @author rhs
 *
 */
public class CategoryFilter implements Filter {
    String ID;
    /**
     * A constructor to build a filter for a particular ID.
     * @param id the desired category ID.
     */
    public CategoryFilter(String id) {
        ID = id;
    }
    /**
     * This is the required matches method that returns true if an Object should be selected by the filter.
     * @param obj the object to be tested.
     */
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
