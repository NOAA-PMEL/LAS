package au.org.tpac.wms.lib.version_1_3_0;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import au.org.tpac.wms.lib.WMSParser;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 14:42:34
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
public class WMSLogoURL_130 extends WMSParser
{
    protected int width;
    protected int height;
    protected String format;
    protected WMSOnlineResource_130 resource;

    public WMSLogoURL_130()
    {
        width = 0;
        height = 0;
        format = null;
    }

    public WMSLogoURL_130(Node node)
    {
        parse(node);
    }

    protected void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "LogoURL";
        params.add(new String[]{"width", width + ""});
        params.add(new String[]{"height", height + ""});

        Element formatElement = doc.createElement("Format");
        Text txtNode = doc.createTextNode(format);
        formatElement.appendChild(txtNode);
        innerElements.add(formatElement);

        innerElements.add(resource.createElement(doc));

    }


    protected void saveAttributeData(String attName, String attData)
    {
        if(attName.equalsIgnoreCase("width"))
            width = Integer.parseInt(attData);
        else if(attName.equalsIgnoreCase("height"))
            height = Integer.parseInt(attData);
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
