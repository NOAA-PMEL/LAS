/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/

package au.org.tpac.wms.lib;

import org.w3c.dom.*;

import java.util.Vector;

/**
 * <p>
 * This class contains information on the extent that a layer can contain.<br>
 * Extents are associated with dimension, such that a layer may contain
 * a dimension called "time" and the extent defines what range that "time" has.
 * </p>
 *
 * <p>
 * Date: 22/02/2006<br>
 * Time: 14:05:54
 * </p>
 *
 * @author Pauline Mak, pauline@insight4.com (Insight4 Pty. Ltd.)
 */
public class WMSLayerExtent extends WMSParser
{
    protected WMSDimension dim;

    protected String elementName;
    protected String extentName;
    protected String defaultValue;

    protected Vector possibleValues;
    protected Vector minValues;
    protected Vector maxValues;
    protected Vector resolutions;
    protected boolean multiValues;
    protected boolean nearestValues;
    protected boolean haveCurrent;


    /**
     * "clones" a WMSLayerExtent.
     * Note that this is a deep copy.
     * @return a new copy of the WMSLayerExtent
     */
    public WMSLayerExtent deepCopy()
    {
        WMSLayerExtent copy = new WMSLayerExtent();
        copy.elementName = elementName;
        copy.extentName = extentName;
        copy.defaultValue = defaultValue;

        if(dim != null)
            copy.dim = dim.deepCopy();

        if(possibleValues != null)
            copy.possibleValues = (Vector)(possibleValues.clone()); //it's ok, since everything is string

        if(minValues != null)
            copy.minValues = (Vector)(minValues.clone());

        if(maxValues != null)
            copy.maxValues = (Vector)maxValues.clone();

        if(resolutions != null)
            copy.resolutions = (Vector)resolutions.clone();

        copy.multiValues = multiValues;
        copy.nearestValues = nearestValues;
        copy.haveCurrent = haveCurrent;
        return copy;
    }

    /**
     * Empty constructur
     */
    public WMSLayerExtent()
    {
        multiValues = false;
        nearestValues = false;
        haveCurrent = false;
        possibleValues = new Vector();
        minValues = new Vector();
        maxValues = new Vector();
        resolutions = new Vector();
        this.defaultValue = "";
    }

    public void setExtentName(String _extentName)
    {
        extentName = _extentName;
    }

    public void setDefaultValue(String _defaultValue)
    {
        defaultValue = _defaultValue;
    }

    public void addResolution(String _resolution)
    {
        resolutions.add(_resolution);
    }

    public void setResolutions(Vector _resolutions)
    {
        resolutions = _resolutions;
    }

    public void setMaxValues(Vector _maxValues)
    {
        maxValues = _maxValues;
    }

    public void setMinValues(Vector _minValues)
    {
        minValues = _minValues;
    }

    public void addPossibleValues(String _posValue)
    {
        possibleValues.add(_posValue);
    }

    public void setPossibleValues(Vector _possibleValues)
    {
        possibleValues = _possibleValues;
    }

    public void setHaveCurrent(boolean _haveCurrent)
    {
        haveCurrent = _haveCurrent;
    }

    public void setNearestValues(boolean _nearestValues)
    {
        nearestValues = _nearestValues;
    }

    public void setMultiValues(boolean _multiValues)
    {
        multiValues = _multiValues;
    }

    /**
     * Get the unit of this extent
     * @return the unit.
     */
    public String getUnit()
    {
        return dim.getUnit();
    }

    /**
     * Get the unit symbol
     * @return the symbol of the uni
     */
    public String getUnitSymbol()
    {
        return dim.getUnitSymbol();
    }

    /**
     * A constructor with a WMSDimension
     * @param _dim dimension this WMSLayerExtent is associated with
     */
    public WMSLayerExtent(WMSDimension _dim)
    {
        dim = _dim;
    }

    /**
     * Set the WMSDimension this extent should be associated with
     * @param _dim WMSDimension to associate this extent with.
     */
    public void setDim(WMSDimension _dim)
    {
        dim = _dim;
    }

    /**
     * Get the default value for this extent
     * @return default value
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Get the name of the extent.
     * Note that the name of this extent MUST BE the same as the
     * associated dimension's name.
     * @return the name of this extent
     */
    public String getExtentName()
    {
        return extentName;
    }

    /**
     * Add a maximum value.
     * It is possible to have a list of max/min pairs
     * @param value new maximum value to add
     */
    public void addMaxValue(String value)
    {
        this.maxValues.add(value);
    }

    /**
     * Add a minimum value.
     * It is possible to have a list of max/min pairs
     * @param value new minimum value to add
     */
    public void addMinValue(String value)
    {
        minValues.add(value);
    }

    /**
     * Possible values are unique - cannot be repeated.
     * @param value value to add to a set of possible values
     */
    public void addPossibleValue(String value)
    {

        if(!possibleValues.contains(value))
            possibleValues.add(value);
    }

    /**
     * Get a list of possible values.
     * Extents can specify a list of value
     * @return a Vector of String
     */
    public Vector getPossibleValues()
    {
        return possibleValues;
    }

    /**
     * Get a lilst of minimum values
     * @return a Vector of string
     */
    public Vector getMinValues()
    {
        return minValues;
    }

    /**
     * Get a list of maximum values
     * @return a Vector of string
     */
    public Vector getMaxValues()
    {
        return maxValues;
    }

    /**
     * Get the resolution an extent is sampled at
     * @return resolution of extent values
     */
    public Vector getResolution()
    {
        return resolutions;
    }

    /**
     * Check whether this extent contains multiple values
     * @return true if the multiValue attribute has been set to "1"
     */
    public boolean getMultiValues()
    {
        return multiValues;
    }

    /**
     * Check whether nearest values are used for this extent
     * @return true if the nearestValue attribute has been set to "1"
     */
    public boolean getNearestValues()
    {
        return nearestValues;
    }

    /**
     * Checks whether a "current" value can be used for this extent.
     * This only applies to "time"
     * @return true of haveCurrent has been set to "1"
     */
    public boolean getHaveCurrent()
    {
        return haveCurrent;
    }

    public void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "Extent";

        this.params.add(new String[]{"name", extentName});
        this.params.add(new String[]{"default", defaultValue});

        if(multiValues)
            this.params.add(new String[]{"multiValues", "1"});

        if(nearestValues)
            this.params.add(new String[]{"nearestValues", "1"});

        if(haveCurrent)
            this.params.add(new String[]{"current", "1"});

        if(possibleValues.size() > 0)
        {
            elementValue = "";

            for(int i = 0; i < possibleValues.size(); i++)
            {
                elementValue += (String)(possibleValues.elementAt(i));

                if(i < (possibleValues.size() -1))
                {
                    elementValue += ", ";
                }
            }
        }

        else if((minValues.size() > 0) && (maxValues.size() > 0) && (resolutions.size() > 0))
        {
            if((minValues.size() == maxValues.size()) && (minValues.size() == resolutions.size()))
            {
                elementValue = "";
                for(int i = 0; i < minValues.size(); i++)
                {
                    elementValue +=  minValues.elementAt(i) + "/" + maxValues.elementAt(i) + "/" + resolutions.elementAt(i);

                    if(i < (minValues.size() -1))
                    {
                        elementValue += ", ";
                    }
                }
            }
            else
            {
                System.out.println("sizes are not the same :(");
            }
        }
    }

    protected void saveAttributeData(String attributeName, String attributeValue)
    {
        if(attributeName.equalsIgnoreCase("name"))
            extentName = attributeValue;
        else if(attributeName.equalsIgnoreCase("default"))
            defaultValue = attributeValue;
        else if (attributeName.equalsIgnoreCase("multiValues"))
            multiValues = (attributeValue.equalsIgnoreCase("1"));
        else if(attributeName.equalsIgnoreCase("nearestValues"))
            nearestValues = (attributeValue.equalsIgnoreCase("1"));
        else if(attributeName.equalsIgnoreCase("current"))
            haveCurrent = (attributeValue.equalsIgnoreCase("1"));
   }

    protected void saveNodeData(String nodeName, String nodeValue)
    {
        if(nodeName.equalsIgnoreCase("extent"))
            setInnerValues(nodeValue);
    }

    protected boolean processChildren(String nodeName, Node childNode)
    {
        return false;
    }

    /**
     * Parse the content with the <extent> tag.
     * This extracts possible values, and ranges.
     * @param valString value to parses
     */
    protected void setInnerValues(String valString)
    {
        if(valString.indexOf(",") > -1)
        {
            //values are separated by a comma
            String[] sep = valString.split(",");

            if(valString.indexOf("/") > -1)
            {
                String[] sep2 = valString.split("/");
                minValues.add(sep2[0]);
                maxValues.add(sep2[1]);
                resolutions.add(sep2[2]);
            }
            else
            {
                for(int i = 0; i < sep.length; i++)
                {
                    possibleValues.add(sep[i]);
                }
            }
        }
        else if(valString.indexOf("/") > -1)
        {
            String[] sep = valString.split("/");
            minValues.add(sep[0]);
            maxValues.add(sep[1]);
            resolutions.add(sep[2]);
        }
        else    //just a single value
        {
            possibleValues.add(valString);
        }
    }
}
