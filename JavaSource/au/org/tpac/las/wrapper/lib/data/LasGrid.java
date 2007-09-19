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
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/**
 * Grid contains information about the extent that the dataset covers.
 * For each grid, can have a number of dimension, each of a different type,
 * for exmaple, a dataset can span over time and lattitude and longitude.
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class LasGrid extends WMSParser
{
    /**
     * Each dimension X, Y, Z (or other type of dimension) is stored here.
     * The key is the dimension type (time, x, y, z), and the value is
     * a LasDimension.
     */
    protected HashMap dimensions;

    /**
     * The name of this grid. This should be unique
     */
    protected String name;


    /**
     * Empty constructor
      */
    public LasGrid()
    {
        dimensions = new HashMap();
        name = null;
    }

    /**
     * This constructor parses an XML node from las.xml to retrieve values
     * @param node node to parse
     */
    public LasGrid(Node node)
    {
        dimensions = new HashMap();
        name = null;
        parse(node);
    }

    /**
     * Sets the name of this grid
     * @param _name new name for this grid
     */
    public void setName(String _name)
    {
        name = _name;
    }

    /**
     * Retrieves the name of this grid
     * @return name of this grid
     */
    public String getName()
    {
        return name;
    }

    /**
     * Retrieves the dimension this grid contains
     * @return dimensions of this grid
     */
    public HashMap getDimensions()
    {
        return dimensions;
    }

    /**
     * Retrieves a dimension based on the dimension type
     * @param type can be of "t" for time, "x" for latitude, "y" for longitude and "z" for elevation
     * @return a LasDimension that describes the axes
     */
    public LasDimension getDimensionByType(String type)
    {
        return (LasDimension)(dimensions.get(type));
    }

    protected void setXMLElementSelf(Document doc)
    {
        xmlElementName = name;

        Iterator it = dimensions.keySet().iterator();

        while(it.hasNext())
        {
            String key = (String)(it.next());
            Element linkElement = doc.createElement("axis");
            Text txtNode = doc.createTextNode(key);
            linkElement.appendChild(txtNode);
            innerElements.add(linkElement);
        }
    }
    protected void saveAttributeData(String attributeName, String attributeValue)
    {
        if(attributeName.equalsIgnoreCase("ID"))
        {
            name = attributeValue;
        }
    }

    protected void saveNodeData(String nodeName, String nodValue)
    {

    }

    /**
     * Appends a dimension to this grid.  Note that there can only be one dimension
     * for each axis.
     * @param axisName name of the axis
     * @param dim dimension to add
     */
    public void addDimension(String axisName, LasDimension dim)
    {
        if(dimensions.containsKey(axisName))
        {
            dimensions.put(axisName, dim);
        }
    }

    protected boolean processChildren(String nodeName, Node childNode)
    {
        if(nodeName.equalsIgnoreCase("axis"))
        {
            Element el = (Element)childNode;

            dimensions.put(el.getAttribute("IDREF"), null);

            return true;
        }

        return false;
    }
}
