/*
*	Copyright Insight4 Pty Ltd 2005-2006 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSLayerExtent;

import java.util.Vector;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 16:32:55
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
public class WMSDimension_130 extends WMSLayerExtent
{
    protected String name;
    protected String units;
    protected String unitSymbol;

    public WMSDimension_130()
    {
        super();
    }

    public String getUnit()
    {
        return units;
    }

    public String getName()
    {
        return name;
    }

    public String getUnitSymbol()
    {
        return unitSymbol;
    }

    protected void saveAttributeData(String attName, String attData)
    {
        super.saveAttributeData(attName, attData);

        if(attName.equalsIgnoreCase("name"))
        {
            name = attData;
        }
        else if(attName.equalsIgnoreCase("unit"))
        {
            units = attData;
        }
        else if(attName.equalsIgnoreCase("unitSymbol"))
        {
            unitSymbol = attData;
        }
    }

    protected void saveNodeData(String nodeName, String nodeValue)
    {
        super.saveNodeData(nodeName, nodeValue);
        if(nodeName.equalsIgnoreCase("dimension"))
            setInnerValues(nodeValue);
    }
}
