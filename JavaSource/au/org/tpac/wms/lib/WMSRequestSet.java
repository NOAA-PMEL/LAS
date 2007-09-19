/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/

package au.org.tpac.wms.lib;

import org.w3c.dom.*;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * <p>
 * This class describes the type of request that a given WMS server supports.<br>
 * Currently in WMS 1.1.1, there are 3 methods, GetCapabilities, GetMap and GetFeatureInfo.<br>
 * </p>
 *
 * <p>
 * Date: 6/02/2006<br>
 * Time: 15:50:02 <br>
 * </p>
 * @author Pauline Mak, pauline@insight4.com (Insight4 Pty. Ltd.)
 */
public class WMSRequestSet extends WMSParser
{
    protected String elementName;
    protected Hashtable requestTable;     //a hashtable of WMSRequestType

    public WMSRequestSet()
    {
        elementName = "";
        requestTable = new Hashtable();
    }

    /**
     * Get a WMSRequestType of a specified type
     * @param type of WMS request, currenty supports GetMap, GetCapabilities and GetFeatureInfo
     * @return a WMSRequestType of a specified name, or null if no such request type exists
     */
    public WMSRequestType getRequestType(String type)
    {
        if(requestTable.containsKey(type))
            return (WMSRequestType)(requestTable.get(type));
        return null;
    }

    /**
     * Add a request type this WMS server supports
     * @param type a new request type
     */
    public void addRequestTable(WMSRequestType type)
    {
        requestTable.put(type.getType(), type);
    }

    /**
     * Sets the entire request table.  This table contains the keys and the type name,
     * and a WMSRequestType as the value
     * @param _requestTable request table to add to this WMSCapabilities object
     */
    public void setRequestTable(Hashtable _requestTable)
    {
        requestTable = _requestTable;
    }

    protected void setXMLElementSelf(Document doc)
    {

        this.xmlElementName = "Request";
        for(Enumeration e = requestTable.keys(); e.hasMoreElements();)
        {
            String key = (String)(e.nextElement());
            WMSRequestType type = (WMSRequestType)(requestTable.get(key));

            this.innerElements.add(type.createElement(doc));
        }
    }


    public void saveAttributeData(String attName, String attValue)
    {

    }

    public void saveNodeData(String nodeName, String nodeData)
    {

    }

    public boolean processChildren(String childName, Node childNode)
    {
        if(childNode.getParentNode().getNodeName().equalsIgnoreCase("Request"))
        {
            if(!childName.equalsIgnoreCase("#text"))
            {
                WMSRequestType type = new WMSRequestType(childName);
                type.parse(childNode);
                requestTable.put(childName, type);
            }
        }
        return true;    //no need to process children nodes...
    }
}
