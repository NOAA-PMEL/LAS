/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/

package au.org.tpac.wms.lib;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * <p>
 * This class contains details on dimensions that a layer has.
 * Dimensions can be special parameters such as time and
 * elevation.  For further details on how a dimension is
 * specified, please see Section 6.5.7 - 6.5.9.
 * </p>
 *
 * </p>
 * Date: 3/02/2006 <br>
 * Time: 10:49:23  <br>
 * </p>
 *
 * @author Pauline Mak, pauline@insight4.com (Insight4 Pty. Ltd.)
 *
 */
public class WMSDimension extends WMSParser
{
    protected String name;
    protected String unit;
    protected String unitSymbol;


    /**
     * Duplicate this Dimension, note that all values are copied
     * across (not referencing the same thing!)
     * @return a fresh new copy of WMSDimension
     */
    public WMSDimension deepCopy()
    {
        WMSDimension newDim = new WMSDimension();
        newDim.name = name;
        newDim.unit = unit;
        newDim.unitSymbol = unitSymbol;
        return newDim;
    }


    /**
     * Empty conastuctor
     */
    public WMSDimension()
    {

    }

    /**
     * A constructor with a give DOM element to parse
     * @param element element to parse
     */
    public WMSDimension(Element element)
    {
        parse(element);
    }

    public void setName(String _name)
    {
        name = _name;
    }

    public void setUnit(String _unit)
    {
        unit = _unit;
    }

    public void setUnitSymbol(String _unitSymbol)
    {
        unitSymbol = _unitSymbol;
    }

    protected void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "Dimension";

        this.params.add(new String[]{"name", name});
        this.params.add(new String[]{"units", unit});
        this.params.add(new String[]{"unitSymbol", unitSymbol});
    }

    protected void saveAttributeData(String attName, String attValue)
    {
        if(attName.compareToIgnoreCase("name") == 0)
        {
            name = attValue;
        }
        else if(attName.compareToIgnoreCase("units") == 0)
        {
            unit = attValue;
        }
        else if(attName.compareToIgnoreCase("unitSymbol") == 0)
        {
            unitSymbol = attValue;
        }
    }

    protected void saveNodeData(String nodeName, String nodeData)
    {
           //do nothing
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        return false;
    }

    /**
     * Get the name of the dimension, e.g. "time"
     * @return name of this dimension
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get th unit of this dimension e.g. elavation can be in metres.
     * @return unit that this dimension is measured in
     */
    public String getUnit()
    {
        return unit;
    }

    /**
     * Get the unit symbol.
    * e.g. metres is "m".
     * @return unit symbol of this dimension.
     */
    public String getUnitSymbol()
    {
        return unitSymbol;
    }
}
