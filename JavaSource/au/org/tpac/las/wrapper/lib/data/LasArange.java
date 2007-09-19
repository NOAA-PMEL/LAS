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

import org.w3c.dom.Node;
import org.w3c.dom.Document;

import java.util.Vector;

/**
 * This class represents axis ranges in LAS.  In particular,
 * when a range contains a size, step and start.  Note that this
 * can also parse discrete values (where values are in an array).
 * @author Pauline Mak <pauline@insight4.com>
 */
public class LasArange extends LasRangeInfo
{
    /**
     * Size is the amount of increment between steps
     */
    protected int size;

    /**
     * Start is where the range starts.  Therefore, "end"
     * is start + (size * step)
     */
    protected double start;

    /**
     * the number of steps to take to find the end.
     */
    protected double step;

    /**
     * The unit this range represents, can be time of axis.
     * t = time, x = latitude, y = longitude and z for elevation.
     */
    protected String type;


    /**
     * Constructor for a LasArange
     */
    public LasArange()
    {
        size = 0;
        step = 0;
        start = 0;
        cursor = 0;
        possibleValues = new Vector();
    }


    /**
     * A constructor that takes in a XML node that contains
     * an arange element.
     * @param node node to parse
     * @param _type type of this arange.
     */
    public LasArange(Node node, String _type)
    {
        type = _type;
        size = 0;
        start = 0;
        step = 0;
        cursor = 0;
        possibleValues = new Vector();
        parse(node);
    }

    protected void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "arange";
    }

    /**
     * Retrieves the number of steps.
     * @return number of steps.
     */
    public double getStep()
    {
        return step;
    }

    /**
     * Get the size of the steps.
     * @return size of the steps
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Retrieve the start value as a double.
     * @return start value
     */
    public double getDoubleStart()
    {
        return ((Double)(getStart())).doubleValue();
    }

    /**
     * Retrieves the end of the range as a double
     * @return
     */
    public double getDoubleEnd()
    {
        return ((Double)(getEnd())).doubleValue();
    }

    /**
     * Retrieves the next value as a double.
     * @return next value (with a single increment of a step)
     */
    public double getDoubleNext()
    {
        return ((Double)(getNext())).doubleValue();
    }


    public Object getStart()
    {
        if(possibleValues.size() > 0)
        {
            return possibleValues.elementAt(0);
        }
        else
        {
            return (new Double(start));
        }
    }


    public Object getEnd()
    {
        if(possibleValues.size() > 0)
        {
            return possibleValues.get(possibleValues.size() - 1);
        }
        else
        {
            double result = start + (step * size);


            return (new Double(result));
        }
    }


    public void gotoStart()
    {
        cursor = 0;
    }

    public Object getNext()
    {
        if(possibleValues.size() > 0)
        {
            if(cursor < possibleValues.size())
            {
                Object result = possibleValues.elementAt(cursor);
                cursor++;
                return result;
            }
        }
        else
        {
            if(cursor < size)
            {

                double result = start + (cursor * step);
                cursor++;
                return (new Double(result));
            }
        }

        return null;
    }

    public void addPossibleValue(String value)
    {
        possibleValues.add(value);
    }

    protected void saveAttributeData(String attributeName, String attributeValue)
    {
        if(attributeName.equalsIgnoreCase("size"))
            size = Integer.parseInt(attributeValue);
        else if(attributeName.equalsIgnoreCase("start"))
            start = Float.parseFloat(attributeValue);
        else if(attributeName.equalsIgnoreCase("step"))
            step = Float.parseFloat(attributeValue);
    }

    protected void saveNodeData(String nodeName, String nodeValue)
    {
    }

    protected boolean processChildren(String nodeName, Node childNode)
    {
        return false;
    }
}
