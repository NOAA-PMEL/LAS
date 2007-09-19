package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSParser;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 15:11:16
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
public class WMSIdentifier_130 extends WMSParser
{
    protected String authority;
    protected WMSAuthURL_130 auth;
    protected String identifier;

    public WMSIdentifier_130()
    {

    }

    public WMSIdentifier_130(Node node, WMSAuthURL_130 _auth)
    {
        auth = _auth;
        parse(node);
    }

    protected void setXMLElementSelf(Document doc)
    {

    }

    protected void saveAttributeData(String attName, String attData)
    {
        //do nothing
        if(attName.equalsIgnoreCase("authority"))
            authority = attData;
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        return false;   //do nothing
    }

    protected void saveNodeData(String nodeName, String nodeData)
    {
        identifier = nodeData;
    }

}
