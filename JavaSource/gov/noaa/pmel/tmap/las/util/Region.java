/**
 * 
 */
package gov.noaa.pmel.tmap.las.util;

import gov.noaa.pmel.tmap.las.client.serializable.RegionSerializable;

import org.jdom.Element;

/**
 * @author rhs
 *
 */
public class Region extends Container {

	/**
	 * @param element
	 */
	public Region(Element element) {
		super(element);
		// TODO Auto-generated constructor stub
	}

	public RegionSerializable getRegionSerializable() {
		RegionSerializable wire_region = new RegionSerializable();
		/*
		 *      The XML looks like this...
		 *          
		        region.setAttribute("name", name);
    			region.setAttribute("ID", name);
    			region.setAttribute("values", values);
    			String[] corners = values.split(",");
    			region.setAttribute("xhi", corners[0]);
    			region.setAttribute("xlo", corners[1]);
    			region.setAttribute("yhi", corners[2]);
    			region.setAttribute("ylo", corners[3]);
		 */
		wire_region.setName(getAttributeValue("name"));
		String values = getAttributeValue("values");
		String[] corners = values.split(",");
		wire_region.setEastLon(Double.valueOf(corners[0]));
		wire_region.setWestLon(Double.valueOf(corners[1]));
		wire_region.setNorthLat(Double.valueOf(corners[2]));
		wire_region.setSouthLat(Double.valueOf(corners[3]));
		return wire_region;
	}

}
