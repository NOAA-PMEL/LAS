package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSParser;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 15:37:20
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
public class WMSResource extends WMSParser
{
    protected String format;
    protected WMSOnlineResource_130 resource;

    public WMSResource()
    {

    }

    public WMSResource(Node node)
    {
        parse(node);
    }


    public void setXMLElementSelf(Document doc)
    {
        
    }

     protected void saveAttributeData(String attName, String attData)
    {
        //do nothing
    }

    protected void saveNodeData(String nodeName, String nodeData)
    {
        if(nodeName.equalsIgnoreCase("Format"))
            format = nodeData;
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
