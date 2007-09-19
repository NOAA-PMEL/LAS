package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSStyle;
import au.org.tpac.wms.lib.WMSLayer;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 14:53:02
 * </p>
 * <p><b>Class description</b></p>
 *
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class WMSStyle_130 extends WMSStyle
{
    protected WMSResource styleSheetURL;
    protected WMSResource styleURL;

    public WMSStyle_130()
    {
        super();
    }

    public WMSStyle_130(WMSLayer_130 _parentLayer, Node styleNode)
    {
        super(_parentLayer, styleNode);
    }

    protected void saveNodeData(String nodeName, String nodeData)
    {
        super.saveNodeData(nodeName, nodeData);
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        if(childName.equalsIgnoreCase("StyleSheetURL"))
        {
            styleSheetURL = new WMSResource(childNode);
            return true;
        }
        else if(childName.equalsIgnoreCase("StyleURL"))
        {
            styleURL = new WMSResource(childNode);
            return true;
        }
        return false;
    }

}
