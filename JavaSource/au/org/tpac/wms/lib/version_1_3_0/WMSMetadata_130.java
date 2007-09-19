package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSParser;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 14:42:43
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
public class WMSMetadata_130 extends WMSParser
{
    protected String type;
    protected String format;
    protected WMSOnlineResource_130 resource;

    public WMSMetadata_130()
    {

    }

    public WMSMetadata_130(Node node)
    {
        parse(node);
    }

    public String getType()
    {
        return type;
    }

    public String getFormat()
    {
        return format;
    }

    public WMSOnlineResource_130 getResource()
    {
        return resource;
    }

    protected void setXMLElementSelf(Document doc)
    {
        xmlElementName = "MetadataURL";
        params.add(new String[]{"type", type});

        Element formatElement = doc.createElement("Format");
        Text txtNode = doc.createTextNode(format);
        formatElement.appendChild(txtNode);
        innerElements.add(formatElement);

        innerElements.add(resource.createElement(doc));
    }


    protected void saveAttributeData(String attName, String attData)
    {
        if(attName.equalsIgnoreCase("type"))
            type = attData;
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
