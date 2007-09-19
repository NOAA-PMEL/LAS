package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSCapabilities;
import au.org.tpac.wms.lib.WMSLayer;
import au.org.tpac.wms.lib.WMSContactInfo;
import org.w3c.dom.*;

import java.util.Vector;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 09:52:10
 * </p>
 * <p><b>Class description</b></p>
 *
 *
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)

 */
public class WMSCapabilities_130 extends WMSCapabilities
{
    public static final String version = "1.3.0";
    private String nodeName;
     /**
     * Empty constructor
     */
    public WMSCapabilities_130()
    {

    }

   public String getDocType()
   {
       return "http://schemas.opengis.net/wms/1.3.0/capabilities_1_3_0.xsd";
   }


    /**
     * A constructor with the GetCapabilities URL specified
     * @param _capabilitiesAddress GetCapabilities URL.
     */
    public WMSCapabilities_130(String _capabilitiesAddress)
    {
        super(_capabilitiesAddress);
        lastError = "";

        Document doc = getDoc();

        if(doc != null)
        {
            Element rootElement = doc.getDocumentElement();
            parse(rootElement);
        }
    }

    public WMSCapabilities_130(String _capabilitiesAddress, Node node)
    {

        super(_capabilitiesAddress, node);
        capabilitiesAddress = _capabilitiesAddress; //somehow, superclass didn't like this
        lastError = "";
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
            layer = new WMSLayer_130();
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
    }


    /**
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
