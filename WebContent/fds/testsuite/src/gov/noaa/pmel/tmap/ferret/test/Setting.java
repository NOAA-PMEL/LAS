package gov.noaa.pmel.tmap.ferret.test;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

/** An encapsulation of XML tag data from the configuration file. 
 *  Each Setting object provides access to the attributes and subtags of 
 *  a single XML tag. */
public class Setting 
       implements Cloneable{

    /** Creates a Setting object from the given XML tag. */
    public Setting(Element xml) {
	this.xml = xml;
    }

    /** Creates a Setting object from the top-level tag of the given 
     *  XML document. 
     * @param name The name of the top-level tag 
     */
    public Setting(Document document){
	this.xml = document.getDocumentElement();
    }
    
    /** Returns a list of Setting objects representing all sub-tags
     *  of this XML tag with the given name.
     */
    public List getSubSettings(String name) {
	List subSettings = new ArrayList();
	for (Node childNode = xml.getFirstChild(); 
               childNode != null; 
               childNode = childNode.getNextSibling()) {
            if(!(childNode instanceof Element)) continue;
            if(name.equals("*")||((Element)childNode).getTagName().equals(name)){
	       subSettings.add(new Setting((Element)childNode));
            }
	}
	return subSettings;
    }

    /** Returns the sub-tag of this XML tag with the given name.
     * @throws Exception If more than one sub-tag exists with that name.
     */
    public Setting getUniqueSubSetting(String name) 
    throws Exception {
        List subSettings = getSubSettings(name);
        if(subSettings.size()!=1)
           throw new Exception("Subsetting "+ name + " not found!");
        else
           return (Setting)subSettings.get(0);
    }

    public Object clone() {
        return new Setting((Element)(xml.cloneNode(true)));
    }

    /** Returns the numerical value of the named attribute of this tag.
     * @param defaultValue Returned if the tag has no such attribute, 
     *  or the attribute's value is non-numeric.
     */
    public long getNumAttribute(String name, long defaultValue) {
	if (xml.getAttribute(name) == "") {
	    return defaultValue;
	}
	try {
	    return Long.valueOf(xml.getAttribute(name)).longValue();
	} catch (NumberFormatException nfe) {
	    return defaultValue;
	}
    }

    /** Returns the value of the named attribute of this tag. Returns
     * "" if the tag has no such attribute.
     */
    public String getAttribute(String name) {
	return getAttribute(name, "");
    }

    /** Returns the value of the named attribute of this tag. 
     * @param defaultValue Returned if the tag has no such attribute.
     */
    public String getAttribute(String name, String defaultValue) {
	if (xml.getAttribute(name) == "") {
	    return defaultValue;
	} else {
	    return xml.getAttribute(name);
	}
    }

    public void setAttribute(String name, String value) {
	xml.setAttribute(name, value);
    }

    public void removeAttribute(String name){
        xml.removeAttribute(name);
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("<");
	sb.append(xml.getTagName());
	NamedNodeMap map = xml.getAttributes();
	for (int i = 0; i < map.getLength(); i++) {
	    sb.append(" ");
	    sb.append(((Attr)map.item(i)).getName());
	    sb.append("=\"");
	    sb.append(((Attr)map.item(i)).getValue());
	    sb.append("\"");
	}
	List subSettings = getSubSettings("*");
	if (subSettings.size() == 0) {
	    sb.append("/>");
	} else {
	    sb.append(">");
	    for (Iterator it = subSettings.iterator(); it.hasNext(); ) {
                StringTokenizer st=new StringTokenizer(it.next().toString(), "\n");
                while(st.hasMoreTokens()){
		   sb.append("\n  " + st.nextToken());
                }
	    }
	    sb.append("\n</");
	    sb.append(xml.getTagName());
	    sb.append(">");
	}
	return sb.toString();
    }

    protected Element xml;
}
