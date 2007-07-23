package gov.noaa.pmel.tmap.las.filter;

import org.jdom.Element;
import org.jdom.filter.Filter;
/**
 * A filter that helps locate properties that are stored in the "old" style:
 * <p>
 * &lt;properties&gt;<br>
 * &nbsp;&nbsp;&lt;GROUP&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;NAME&gt;VALUE&lt;/NAME&gt;<br>
 * &nbsp;&nbsp;&lt;/GROUP&gt;<br>
 * &lt;properties&gt;<br> 
 * @author Roland Schweitzer
 *
 */
public class FindOldPropertyFilter implements Filter {
    
    protected String name = null;
    /**
     * Construct the filter by passing in the name of the property you're looking for.
     * @param name - the property name.
     */
    public FindOldPropertyFilter(String name) {
        this.name = name;
    }
    /**
     * The implementation of the matches method which returns true of the JDOM element has the same name as the property.
     */
    public boolean matches(Object node) {
        Element element=null;
        if (node instanceof Element) {
            element = (Element) node;
        } else {
            return false;
        }
        
        if (element.getName().equals(name) ) {
            return true;
        } else {
            return false;
        }
    }
}
