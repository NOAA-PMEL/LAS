package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSParser;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 10:16:49
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
public class WMSAttribution_130 extends WMSParser
{
    protected String title;
    protected WMSOnlineResource_130 resource;
    protected WMSLogoURL_130 logo;

    public WMSAttribution_130()
    {
        logo = null;
    }

    public String getTitle()
    {
        return title;
    }

    public WMSOnlineResource_130 getResource()
    {
        return resource;
    }


    public WMSAttribution_130(Node node)
    {
        parse(node);
    }

    protected void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "Attribution";

        Element titleElement = doc.createElement("title");
        Text txtNode = doc.createTextNode(title);
        titleElement.appendChild(txtNode);
        innerElements.add(titleElement);
        innerElements.add(resource.createElement(doc));

        if(logo != null)
            innerElements.add(logo.createElement(doc));
    }

    protected void saveAttributeData(String attName, String attData)
    {

    }

    protected void saveNodeData(String nodeName, String nodeData)
    {

    }

    protected boolean processChildren(String childName, Node childNode)
    {
        if(childName.equalsIgnoreCase("OnlineResource"))
        {
            resource = new WMSOnlineResource_130(childNode);
            return true;
        }
        else if(childName.equalsIgnoreCase("LogoURL"))
        {
            logo = new WMSLogoURL_130(childNode);
            return true;
        }

        return false;
    }
}
