/**
 * 
 */
package gov.noaa.pmel.tmap.las.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

/**
 * @author rhs
 *
 */
public class DataConstraint extends Container implements DataConstraintInterface {

	/**
	 * @param element
	 */
	public DataConstraint(Element element) {
		super(element);
		// TODO Auto-generated constructor stub
	}

	public ArrayList<NameValuePair> getLhs() {
		return getPairs("lhs");
	}
	private ArrayList<NameValuePair> getPairs(String position) {
		List menus = element.getChildren("menu");
		Element theMenu = null;
		for (Iterator menuIt = menus.iterator(); menuIt.hasNext();) {
			Element menu = (Element) menuIt.next();
			if ( menu.getAttributeValue("position").equals(position) ) {
				theMenu = menu;
			}
		}

		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		if ( theMenu != null ) {
			List items = theMenu.getChildren("item");
			for (Iterator itemIt = items.iterator(); itemIt.hasNext();) {
				Element item = (Element) itemIt.next();
				pairs.add(new NameValuePair(item.getTextTrim(), item.getAttributeValue("values")));
			}
		}
		return pairs;
	}

	public ArrayList<NameValuePair> getOps() {
		return getPairs("ops");
	}
	public ArrayList<NameValuePair> getRhs() {
        return getPairs("rhs");
	}
}
