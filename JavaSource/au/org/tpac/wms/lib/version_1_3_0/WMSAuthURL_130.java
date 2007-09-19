package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSParser;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 15:02:23
 * </p>
 * <p><b>Class description</b></p>
 * <p/>
 * <p/>
 * <p/>
 * </p>
 *
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 *         </p>
 */
public class WMSAuthURL_130 extends WMSParser
{
    protected String name;
    protected WMSOnlineResource_130 resource;

    public WMSAuthURL_130()
    {

    }

    public WMSAuthURL_130(Node node)
    {
        parse(node);
    }

    public String getName()
    {
        return name;
    }

    public WMSOnlineResource_130 getResource()
    {
        return resource;
    }

     protected void setXMLElementSelf(Document doc)
    {

    }

    protected void saveNodeData(String nodeName, String nodeData)
    {
        //do nothing
    }

    protected void saveAttributeData(String attName, String attData)
    {
        if(attName.equalsIgnoreCase("name"))
            name = attData;
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        if(childName.equalsIgnoreCase("OnlineResource"))
        {
            resource = new WMSOnlineResource_130(childNode);
            return true;
        }
        return false;
    }
}
