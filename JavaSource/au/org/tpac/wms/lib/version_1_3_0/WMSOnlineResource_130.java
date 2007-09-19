package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSOnlineResource;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 14:17:43
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
public class WMSOnlineResource_130 extends WMSOnlineResource
{
    protected String type;

    public WMSOnlineResource_130()
    {
        super();
    }

    public WMSOnlineResource_130(Node node)
    {
        super(node);
    }

    public String getType()
    {
        return type;
    }

    public void setXMLElementSelf(Document doc)
    {
        super.setXMLElementSelf(doc);

        this.params.add(new String[]{"xlink:type", type});
    }

    protected void saveAttributeData(String attName, String attData)
    {
        super.saveAttributeData(attName, attData);

        if(attName.equalsIgnoreCase("xlink:type"))
            type = attData;
    }

}
