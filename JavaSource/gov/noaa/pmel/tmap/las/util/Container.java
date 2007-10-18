package gov.noaa.pmel.tmap.las.util;

import gov.noaa.pmel.tmap.las.ui.Util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class Container {
    
    String container_type;
    Element element;
    
    public Container(Element element) {
        this.element = element;
    }
    
    public String getID() {
        return element.getAttributeValue("ID");
    }
    public void setID(String id) {
        element.setAttribute("ID", id);
    }
    public String getName() {
        return element.getAttributeValue("name");
    }
    public void setName(String name) {
        element.setAttribute("name", name);
    }
    public ArrayList<NameValuePair> getAttributes() {
        ArrayList<NameValuePair> attributes = new ArrayList<NameValuePair>();
        List attrs = element.getAttributes();
        for (Iterator attrIt = attrs.iterator(); attrIt.hasNext();) {
            Attribute attribute = (Attribute) attrIt.next();
            attributes.add(new NameValuePair(attribute.getName(), attribute.getValue()));
        }
        return attributes;
    }
    public void setAttributes(ArrayList<NameValuePair> attributes) {
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        for (Iterator attrIt = attributes.iterator(); attrIt.hasNext();) {
            NameValuePair attr = (NameValuePair) attrIt.next();
            attrs.add(new Attribute(attr.getName(), attr.getValue()));
        }
        element.setAttributes(attrs);
    }
    public HashMap<String, ArrayList<NameValuePair>> getProperties() {
        HashMap<String, ArrayList<NameValuePair>> properties = new HashMap<String, ArrayList<NameValuePair>>();
        List propGroups = element.getChild("properties").getChildren("property_group");
        for (Iterator propGroupIt = propGroups.iterator(); propGroupIt.hasNext();) {
            ArrayList<NameValuePair> propGroup = new ArrayList<NameValuePair>();
            Element propGroupE = (Element) propGroupIt.next();
            String type = propGroupE.getAttributeValue("type");
            List props = propGroupE.getChildren("property");
            for (Iterator propsIt = props.iterator(); propsIt.hasNext();) {
                Element prop = (Element) propsIt.next();
                NameValuePair property = new NameValuePair(prop.getChild("name").getTextNormalize(),prop.getChild("value").getTextNormalize());
                propGroup.add(property);
            }
            properties.put(type, propGroup);
        }
        return properties;
    }
    /**
     * Returns an ArrayList of properties in a particular property group.
     *
     * @param property_group property group identifier
     * @return properties all properties within the named property group
     */
    public ArrayList<NameValuePair> getProperties(String property_group) {
        ArrayList<NameValuePair> noProperties = new ArrayList<NameValuePair>();
        List propGroups = element.getChild("properties").getChildren("property_group");
        for (Iterator propGroupIt = propGroups.iterator(); propGroupIt.hasNext();) {
            ArrayList<NameValuePair> propGroup = new ArrayList<NameValuePair>();
            Element propGroupE = (Element) propGroupIt.next();
            String type = propGroupE.getAttributeValue("type");
            if (type.equals(property_group)) {
                List props = propGroupE.getChildren("property");
                for (Iterator propsIt = props.iterator(); propsIt.hasNext();) {
                    Element prop = (Element) propsIt.next();
                    NameValuePair property = new NameValuePair(prop.getChild("name").getTextNormalize(),prop.getChild("value").getTextNormalize());
                    propGroup.add(property);
                }
                return propGroup;
            }
        }
        return noProperties;
    }
    public void setProperties(HashMap<String, ArrayList<NameValuePair>> propertiesHash) {
        Element properties = new Element("properties");
        for (Iterator groupsIt = propertiesHash.keySet().iterator(); groupsIt.hasNext();) {
            String group_name = (String) groupsIt.next();
            Element property_group = new Element("property_group");
            property_group.setAttribute("type", group_name);
            ArrayList<NameValuePair> props = propertiesHash.get(group_name);
            for (Iterator propsIt = props.iterator(); propsIt.hasNext();) {
                NameValuePair prop = (NameValuePair) propsIt.next();
                Element property = new Element("property");
                Element name = new Element("name");
                Element value = new Element("value");
                name.setText(prop.getName());
                value.setText(prop.getValue());
                property.addContent(name);
                property.addContent(value);
                property_group.addContent(property);
            }
            properties.addContent(property_group);
        }
        element.removeChildren("properties");
        element.addContent(properties);
    }
       
    public String getAttributeValue(String name) {
        String value = element.getAttributeValue(name);
        if ( value != null ) {
            return value;
        }
        return "";
    }
    public void setAttribute(String name, String value) {
        element.setAttribute(name, value);
    }
    
    public String toXML() {
        return toCompactString();
    }
    
    public String toString(Format format) {
        StringWriter xmlout = new StringWriter();
        try {
            format.setLineSeparator(System.getProperty("line.separator"));
            XMLOutputter outputter = new XMLOutputter(format);
            outputter.output(this.element, xmlout);
            // Close the FileWriter
            xmlout.close();
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return xmlout.toString();
    }
    
    public String toString() {
        Format format = Format.getPrettyFormat();
        return toString(format);
    }
    public String toCompactString() {
        Format format = Format.getCompactFormat();
        format.setOmitDeclaration(true);
        String xml = toString(format);
        xml = xml.replaceAll("\\r","");
        xml = xml.replaceAll("\\n","");
        return xml;
    }
    
    public Element getElement() {
        return element;
    }
    
    public JSONObject toJSON() throws JSONException {
        return XML.toJSONObject(toXML());
    }
}
