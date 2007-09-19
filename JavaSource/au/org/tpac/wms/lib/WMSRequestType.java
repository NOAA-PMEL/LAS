/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.lib;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.Vector;

/**
 * <p>
 * A request type defines how to make a request of a specified name. <br>
 * Curerntly WMS 1.1.1 supports 3 types, GetMap, GetCapabilities and GetFeatureInfo.<br>
 * <b>This library only supports GetMap and GetCapabilities</b>.
 * </p>
 * <p>
 * Date: 7/02/2006<br>
 * Time: 10:02:59
 * </p>
 *
 * @author Pauline Mak, pauline@insight4.com (Insight4 Pty. Ltd.)
 */
public class WMSRequestType extends WMSParser
{
    protected String typeName;
    protected Vector supportedFormats;
    protected WMSOnlineResource resource;

    /**
     * Get the request type
     */
    public WMSRequestType()
    {
        elementName = "";
        typeName = "";
        supportedFormats = new Vector();
        resource = null;
    }

    public WMSRequestType(String _typeName)
    {
        elementName = "";
        typeName = _typeName;
        supportedFormats = new Vector();
        resource = null;
    }

    public void setResource(WMSOnlineResource _resource)
    {
        resource = _resource;
    }

    public void addSupportedFormat(String _format)
    {
        supportedFormats.add(_format);
    }

    public void setSupportedFormat(Vector _supportedFormats)
    {
        supportedFormats = _supportedFormats;
    }

    public void setTypeName(String _typeName)
    {
        typeName = _typeName;
    }

    /**
     * Get te type of this requestType
     * @return the name of this request type
     */
    public String getType()
    {
        return typeName;
    }

    /**
     * Get URLs associated with this request type
     * @return base URL for making a request
     */
    public String getResourceURL()
    {
        return resource.getURL();
    }

    /**
     * Get supported formats.
     * E.g. GetMap can support a large number of images, such as Gif, png, jpeg, etc.
     * @return a Vector of Strings that this request with respond with.
     */
    public Vector getSupportedFormat()
    {
        return supportedFormats;
    }

    protected void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = typeName;

        for(int i = 0; i < supportedFormats.size(); i++)
        {
            String formatValue = (String)(supportedFormats.elementAt(i));
            Element formatElement = doc.createElement("Format");
            Text txtNode = doc.createTextNode(formatValue);
            formatElement.appendChild(txtNode);
            innerElements.add(formatElement);
        }

        Element dcpTypeElement = doc.createElement("DCPType");
        Element protocolElement = doc.createElement("HTTP");
        Element getElement = doc.createElement("Get");

        if(resource != null)
            getElement.appendChild(resource.createElement(doc));
        
        protocolElement.appendChild(getElement);
        dcpTypeElement.appendChild(protocolElement);
        this.innerElements.add(dcpTypeElement);
    }

    public boolean processChildren(String childName, Node childNode)
    {
        if((resource == null) && (childName.equalsIgnoreCase("OnlineResource")))
        {
            resource = new WMSOnlineResource();
            resource.parse(childNode);
            return true;
        }

        return false;
    }

    public void saveAttributeData(String attName, String attValue)
    {

    }

    public void saveNodeData(String nodeName, String nodeValue)
    {
        if(nodeName.equalsIgnoreCase("format"))
           supportedFormats.add(nodeValue);
    }

}
