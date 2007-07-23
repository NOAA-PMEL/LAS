package gov.noaa.pmel.tmap.las.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

public class Axis extends Container implements AxisInterface {
    public Axis(Element element){
        super(element);
    }
    
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.AxisInterface#getType()
     */
    public String getType() {
        return getAttributeValue("type");
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.AxisInterface#setType(java.lang.String)
     */
    public void setType(String type) {
        setAttribute("type",type);
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.AxisInterface#getVerticies()
     */
    public ArrayList<NameValuePair> getVerticies() {
        ArrayList<NameValuePair> verticies = new ArrayList<NameValuePair>();
        List vs = element.getChildren("v");
        for (Iterator vsIt = vs.iterator(); vsIt.hasNext();) {
            Element v = (Element) vsIt.next();
            String name = v.getAttributeValue("label");
            String value = v.getTextNormalize();
            verticies.add(new NameValuePair(name,value));
        }
        return verticies;
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.AxisInterface#setVerticies(java.util.ArrayList)
     */
     public void setVerticies(ArrayList<NameValuePair> verticies) {
         element.removeChildren("v");
         for (Iterator vertIt = verticies.iterator(); vertIt.hasNext();) {
            NameValuePair vert = (NameValuePair) vertIt.next();
            Element v = new Element("v");
            v.setAttribute(new Attribute("label", vert.getName()));
            element.addContent(v);
        }
     }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.AxisInterface#getArange()
     */
    public Arange getArange() {
        Element arange = element.getChild("arange");
        if ( arange != null ) {
            return new Arange(arange);
        }
        return null;
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.AxisInterface#setArange(gov.noaa.pmel.tmap.las.util.Arange)
     */
    public void setArange(Arange arange) {
        element.removeChild("arange");
        element.addContent(arange.getElement());
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.AxisInterface#hasV()
     */
    public boolean hasV() {
        List v = element.getChildren("v");
        if ( v != null && v.size() > 0) {
            return true;
        }
        return false;
    }
}
