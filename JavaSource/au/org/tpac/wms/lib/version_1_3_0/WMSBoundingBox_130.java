package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSBoundingBox;
import org.w3c.dom.Node;
//import org.w3c.dom.Element;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 14:31:59
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
public class WMSBoundingBox_130 extends WMSBoundingBox
{
    protected String CRS;
    protected float resX;
    protected float resY;

    public WMSBoundingBox_130(Node node)
    {
        super(node);
    }

    public float getResX()
    {
        return resX;
    }

    public float getResY()
    {
        return resY;
    }

    protected void saveAttributeData(String attName, String attData)
    {
        super.saveAttributeData(attName, attData);

        if(attName.equalsIgnoreCase("resx"))
        {
            resX = Float.parseFloat(attData);
        }
        else if(attName.equalsIgnoreCase("resy"))
        {
            resY = Float.parseFloat(attData);
        }
        else if(attName.equalsIgnoreCase("CRS"))
        {
            CRS = attData;
        }
    }
}
