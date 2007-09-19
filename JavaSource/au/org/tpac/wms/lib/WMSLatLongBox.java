/*
*	Copyright Insight4 Pty Ltd 2005-2006 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/

package au.org.tpac.wms.lib;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * <p>
 * This class is used for defining a latitude and longitude bounding box.<br>
 * This generally defines the data area of a layer. <br>
 * Where X defines the left to right values, and Y defines top to bottom values.<br>
 * It is based on a rectangle defined by 4 decimals, minX = left,
 * maxX = right, maxY = top, minY = bottom.<br>
 * For more information, please see Section 6.5.6 of the WMS 1.1.1 Spec.
 * </p>
 *
 * <p>
 * Date: 3/02/2006 <br>
 * Time: 11:18:15 <br>
 * </p>
 * @author Pauline Mak, pauline@inisight4.com (Insight4 Pty. Ltd.)
 *

 */
public class WMSLatLongBox extends WMSParser
{
    /**
    * The right-most coordinate.
    */
    protected float minX;

    /**
    * The bottom-most coordinate
    */
    protected float minY;

    /**
    * The left-most coordinate
    */
    protected float maxX;

    /**
    * The top-most coordinate
    */
    protected float maxY;

    /**
     * Sole constructor for this class
     * @param element DOM element containing data for this latitude/longitude bounding box
     */
    public WMSLatLongBox(Node element)
    {
        minX = 0;
        minY = 0;
        maxX = 0;
        maxY = 0;
        parse(element);
    }

    /**
     * Empty constructor
     */
    public WMSLatLongBox()
    {
        minX = 0;
        minY = 0;
        maxX = 0;
        maxY = 0;   
    }

    protected void saveAttributeData(String attributeString, String attributeValue)
    {

        if(attributeString.compareToIgnoreCase("minx") == 0)
        {
            minX = Float.parseFloat(attributeValue);
        }
        else if(attributeString.compareToIgnoreCase("miny") == 0)
        {
            minY = Float.parseFloat(attributeValue);
        }
        else if(attributeString.compareToIgnoreCase("maxX") == 0)
        {
            maxX = Float.parseFloat(attributeValue);
        }
        else if(attributeString.compareToIgnoreCase("maxY") == 0)
        {
            maxY = Float.parseFloat(attributeValue);
        }
    }

    protected void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "LatLonBoundingBox";

        this.params.add(new String[]{"minx", this.minX + ""});
        this.params.add(new String[]{"miny", this.minY + ""});

        this.params.add(new String[]{"maxx", this.maxX + ""});
        this.params.add(new String[]{"maxy", this.maxY + ""});
    }

    protected boolean processChildren(String nodeName, Node childNode)
    {
        return false;//no children to process
    }

    protected void saveNodeData(String nodeName, String nodeValue)
    {
        //do nothing - no node to parse   
    }

    /**
     * Get the minimum X value (left)
     * @return minimum X value
     */
    public float getMinX()
    {
        return minX;
    }

    public void setMinX(float _minX)
    {
        minX = _minX;
    }

    /**
     * Get the minimum Y value (bottom)
     * @return minimum Y value of the boundary
     */
    public float getMinY()
    {
        return minY;
    }

    public void setMinY(float _minY)
    {
        minY = _minY;
    }

    /**
     * Get the maximum X value (right)
     * @return maximum X value of the boundary
     */
    public float getMaxX()
    {
        return maxX;
    }

    /**
     * Sets the maximum X value
     * @param _maxX maximum X value of the boundary
     */
    public void setMaxX(float _maxX)
    {
        maxX = _maxX;
    }

    /**
     * Get the maximum Y value (top)
     * @return maximum Y value of the boundary
     */
    public float getMaxY()
    {
        return maxY;
    }

    /**
     * Sets the maximum Y value
     * @param _maxY maximum Y value of the boundary
     */
    public void setMaxY(float _maxY)
    {
        maxY = _maxY;
    }
}
