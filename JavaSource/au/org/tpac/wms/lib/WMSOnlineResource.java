/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/

package au.org.tpac.wms.lib;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * This node stores online resources that WMS refers to, including the legend graphics
 * and URLs to call particular requests.
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class WMSOnlineResource extends WMSParser
{
    /**
     * Type of resource (URL is xlink)
     */
    protected String protocol;

    /**
     * URL of the online resource
     */
    protected String URL;


    /**
     * Empty constructor!
     */
    public WMSOnlineResource()
    {
        protocol = null;
        URL = null;
    }

    /**
     * This constructor parses a node for
     * @param node node to parse
     */
    public WMSOnlineResource(Node node)
    {
        parse(node);
    }

    /**
     * Sets the type of the resource.  E.g. URLs is xlink
     * @param _protocol
     */
    public void setProtocol(String _protocol)
    {
        protocol = _protocol;
    }


    /**
     * Sets the URL of the online resource
     * @param _URL URL fo the online resource
     */
    public void setURL(String _URL)
    {
        URL = _URL;
    }

    /**
     * Retrieves the resource type
     * @return resource type
     */
    public String getProtocol()
    {
        return protocol;
    }


    public String getURL()
    {
        return URL;
    }

    public void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "OnlineResource";

        this.attributeNS = "http://www.w3.org/1999/xlink";
        this.params.add(new String[]{"xlink:href", URL});
        this.params.add(new String[]{"xlink:type", "simple"});
    }

    protected void saveAttributeData(String attName, String attValue)
    {
        if(attName.equalsIgnoreCase("xmlns:xlink"))
            protocol = attValue;
        else if(attName.equalsIgnoreCase("xlink:href"))
            URL = attValue;
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        return false;
    }

    protected void saveNodeData(String nodeName, String nodeData)
    {
        //do nothing
    }
}
