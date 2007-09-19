/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.lib;

import org.w3c.dom.*;

/**
 * <p>
 * This is a concrete subclass of WMSCapabilities, implementing the
 * WMS 1.1.1 specification
 * </p>
 * 
 * <p>
 * Date: 2/02/2006 <br>
 * Time: 13:32:13 <br>
 * </p>
 * @author Pauline Mak, pauline@insight4.com (Insight4 Pty. Ltd.)
 *

 */
public class WMSCapabilities_111 extends WMSCapabilities
{
    public static final String version = "1.1.1";
    private String nodeName;

    /**
     * Empty constructor
     */
    public WMSCapabilities_111()
    {
        super();
    }

    /**
     * A constructor with the GetCapabilities URL specified
     * @param _capabilitiesAddress GetCapabilities URL.
     */
    public WMSCapabilities_111(String _capabilitiesAddress)
    {
        super(_capabilitiesAddress);
        capabilitiesAddress = _capabilitiesAddress;
        lastError = "";

        Document doc = getDoc();

        if(doc != null)
        {
            Element rootElement = doc.getDocumentElement();
            parse(rootElement);
        }
    }

    public String getDocType()
    {
        return "http://schemas.opengis.net/wms/1.1.1/capabilities_1_1_1.dtd";
    }

    /**
     * Get the string used for getting the capabilites statement of the given WMS server
     * @return the request string for getting the aapabilities statement.
     */
    public String getCapRequestString()
    {
        String result = "";
        if(capabilitiesAddress.indexOf("?") == -1)
            result = capabilitiesAddress + "?SERVICE=WMS&REQUEST=GetCapabilities";
        else
            result = capabilitiesAddress + "&SERVICE=WMS&REQUEST=GetCapabilities";

        return result;
    }

    /**
     * Creates a WMSCapabilties object based on an XML node
     * @param _capabilitiesAddress address for retrieving the WMSCapabilites XML document
     * @param node node to parse
     */
    public WMSCapabilities_111(String _capabilitiesAddress, Node node)
    {
        super(_capabilitiesAddress, node);
        capabilitiesAddress = _capabilitiesAddress;
    }

    /**
     * Saves data based on the the element name.
     * @param name element for which this value belongs to
     * @param val value of the element
     */
    private void saveData(String name, String val)
    {
        if(name.compareToIgnoreCase("Format") == 0)
        {
            exceptionTypes.add(val);
        }
        else if(name.compareToIgnoreCase("Title") == 0)
        {
            title = val;
        }
    }

    protected boolean processChildren(String nodeName, Node childNode)
    {
        if(nodeName.equalsIgnoreCase("ContactInformation"))
        {
            info = new WMSContactInfo(childNode);
        }
        else if((layer == null) && (nodeName.compareToIgnoreCase("layer") == 0))
        {
            layer = new WMSLayer();
            layer.parse(childNode);
            return true;
        }
        else if(nodeName.compareToIgnoreCase("Exception") == 0)
        {
            parseNode(childNode);
            return true;
        }
        else if(nodeName.compareToIgnoreCase("Request") == 0)
        {
            reqSet.parse(childNode);
            serverAddress = reqSet.getRequestType("GetMap").getResourceURL();
            return true;
        }

        return false;
    }

    protected void saveAttributeData(String attributeName, String attributeValue)
    {
        //do nothing.
    }

    protected void saveNodeData(String nodeName, String nodeValue)
    {
        if((title == null) && (nodeName.equalsIgnoreCase("title")))
        {
            title = nodeValue;
        }

        if((serverAbstract == null) && (nodeName.equalsIgnoreCase("Abstract")))
        {
            serverAbstract = nodeValue;
        }
    }


    /**
     * parses the document
     * @param node a node to parse
     */
    private void parseNode(Node node)
    {
        switch(node.getNodeType())
        {
            case Node.ELEMENT_NODE:
                nodeName = node.getNodeName();
                NodeList children = node.getChildNodes();

                for(int j = 0; j < children.getLength(); j++)
                {
                    Node childNode = children.item(j);
                    parseNode(childNode);
                }

                break;
            case Node.TEXT_NODE:
                String val =  node.getNodeValue();
                if(val.trim().compareToIgnoreCase("") != 0)
                    saveData(nodeName, val);
                break;
        }
    }


    /**
     * Get the WMS version that this object can parse
     * @return WMS version.
     */
    public String getVersion()
    {
        return version;
    }
}
