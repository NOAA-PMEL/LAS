/**
 * This software module was contributed by Tasmanian Partnership for
 * Advanced Computing (TPAC) and Insight4 Pty. Ltd. to the Live
 * Access Server project at the US the National Oceanic and Atmospheric
 * Administration (NOAA)in as-is condition. The LAS software is
 * provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that neither NOAA nor TPAC and
 * Insight4 Pty. Ltd. assume liability for any errors contained in
 * the code.  Although this software is released without conditions
 * or restrictions in its use, it is expected that appropriate credit
 * be given to its authors, to TPAC and Insight4 Pty. Ltd. and to NOAA
 * should the software be included by the recipient as an element in
 * other product development.
 **/

package au.org.tpac.las.wrapper.lib.data;


import au.org.tpac.wms.lib.WMSParser;

import java.awt.*;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * This class contains logic for storing dimension ranges
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class LasDimension extends WMSParser
{
    /**
     * The unit of measure (e.g. distance in km, temperature in Celcius)
     */
    protected String units;

    /**
     * The type of dimension this is. x = latitude, y = longitude, z = elevation, t = time
     */
    protected String type;

    /**
     * The name of this dimension
     */
    protected String name;

    /**
     * This stores the actual range (see LasRangeInfo for details)
     */
    protected LasRangeInfo rangeInfo;

    /**
     * Empty constructor
     */
    public LasDimension()
    {
        units = null;
        rangeInfo = null;
        name = null;
    }

    /**
     * This constructor takes in an XML node (from las.xml) and parses values
     * @param node node to parse
     */
    public LasDimension(Node node)
    {
        name = null;
        units = null;
        rangeInfo = null;
        parse(node);
    }


    protected void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = name;
        this.params.add(new String[]{"type", type});
        this.params.add(new String[]{"units", units});
        innerElements.add(rangeInfo.createElement(doc));
    }

    /**
     * Retrieves the dimension type: x = latitude, y = longitude, z = elevation, t = time
     * @return a string with the dimension type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Retrieves the unit as (e.g. month)
     * @return dimension units
     */
    public String getUnits()
    {
        return units;
    }

    /**
     * Retrieves the name of this dimension
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * Retrieves the LasRangeInfo object that contains actual range.
     * @return LasRangeInfo object
     */
    public LasRangeInfo getRangeInfo()
    {
        return rangeInfo;
    }

    /**
     * Retrieves the minimum value
     * @return returns the smallest value possible.
     */
    public Object getMin()
    {
        return rangeInfo.getStart();
    }

    /**
     * Retrueves the maximum value
     * @return returns the highest value possible
     */
    public Object getMax()
    {
        return rangeInfo.getEnd();
    }

    protected void saveAttributeData(String attributeName, String attributeValue)
    {
        if(attributeName.equalsIgnoreCase("type"))
        {
            type = attributeValue;

            if(type.equalsIgnoreCase("t"))
            {
                rangeInfo = new TimeDimension();
            }
            else
            {
                rangeInfo = new LasArange();
            }
        }
        else if(attributeName.equalsIgnoreCase("units"))
        {
            units = attributeValue;

            if(type.equalsIgnoreCase("t"))
            {
                ((TimeDimension)(rangeInfo)).setUnitString(units);
            }
        }
        else if(attributeName.equalsIgnoreCase("ID"))
        {
            this.name = attributeValue;
        }
    }

    protected void saveNodeData(String nodeName, String nodValue)
    {
        if(nodeName.equalsIgnoreCase("v"))
        {
            if(rangeInfo == null)
            {
                System.err.println("rangeInfo = null");
            }
            else
            {
                rangeInfo.addPossibleValue(nodValue);
            }
        }
    }

    protected boolean processChildren(String nodeName, Node childNode)
    {
        if(nodeName.equalsIgnoreCase("arange"))
        {
            rangeInfo.parse(childNode);
        }


        return false;
    }
}
