package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSParser;

import java.util.Vector;
import java.util.Hashtable;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 15:16:07
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
public class WMSFeatureList_130 extends WMSParser
{
    protected Hashtable formats;
    private String currentFormat;

    public WMSFeatureList_130()
    {
        formats = new Hashtable();
    }

    public WMSFeatureList_130(Node node)
    {
        formats = new Hashtable();
        parse(node);
    }

    protected void setXMLElementSelf(Document doc)
    {

    }

    protected void saveNodeData(String nodeName, String nodeData)
    {
        if(nodeName.equalsIgnoreCase("Format"))
            currentFormat = nodeData;
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        if(childName.equalsIgnoreCase("OnlineResource"))
        {
            WMSOnlineResource_130 res = new WMSOnlineResource_130(childNode);
            formats.put(currentFormat, res);
            return true;
        }
        return false;
    }

    protected void saveAttributeData(String attName, String attData)
    {
        //do nothing
    }
}
