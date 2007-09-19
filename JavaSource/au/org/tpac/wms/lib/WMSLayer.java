/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/

package au.org.tpac.wms.lib;

import org.w3c.dom.*;

import java.util.*;

/**
 * <p>
 * WMS can contain multiple maps, each map is contained with a "layer".  Each
 * WMS layer contain information, such as its latitude and longitude boundaries,
 * what styles this layer can be drawn at and any special dimension attributes
 * and their limits.<br>
 * A layer may contain sublayers, and certain properties can be inherited by them.
 * For full details, please refer to Section 7.1.4.4 of the WMS 1.1.1 Specification.
 * For inheritance information, please see Section 7.1.4.7.
 * </p>
 * <p>
 * Date: 2/02/2006 <br>
 * Time: 15:07:27 <br>
 * </p>
 *
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class WMSLayer extends WMSParser implements Comparable
{
    //plain attributes

    /**
     * Title of this layer - this is a human readable name
     */
    protected String title;

    /**
     * Name of this layer - a unique identifier that distinguishes one layer from another
     */
    protected String name;

    /**
     * A blurb about this layer
     */
    protected String layerAbstract;

    /**
     * Each layer can have a set of "extents", which are limitations of related dimensions.
     * i.e. dimension can specify the format of "time",
     * and the layers "time" extent limits it between 2001-01-01 to 2003-01-01
     */
    protected Hashtable extents;

    //flags
    /**
     * Whether this layer contains transparency (and can potentially affects ordering of layers)
     */
    protected String opaque;

    protected String queryable;

    protected String cascaded;

    //list of attributes
    /**
     * Types of coordinates this layer supports.
     */
    private Vector supportedSRS;

    /**
     * A list of children layers
     */
    protected Vector subLayers;

    /**
     * A list of styles this layer supports
     */
    protected Vector styles;

    /**
     * Dimensions that this layer supports - dimension dictattes what types are supported where as
     * extents defines the value range that applies to the layer.  Sublayers inherit this value.
     */
    protected Vector dimensions;

    /**
     * The parent to this layer.  Note that the top most layer will have NULL as its parent.
     * Each layer can only have one parent
     */
    protected WMSLayer parent;

    /**
     * A box that defines the latitude and longitude range
     */
    private WMSLatLongBox latLongBox;

    /**
     * The bounding box can be of any type of coordinate systems that defines the boundary of this layer
     */
    private WMSBoundingBox boundingBox;

    /**
     * This table contains a set of "extra" dumension this layer supports (which are not of type time or elevation)
     */
    private TreeSet dimNames;


    /**
     * Name of this element (for generating XML)
     */
    protected String elementName = "";

    /**
     * Creates an empty WMSLayer
     */
    public WMSLayer()
    {
        title = null;
        name = null;
        layerAbstract = null;
        subLayers = null;
        supportedSRS = null;
        parent = null;
        styles = null;
        dimensions = null;
        extents = null;
        opaque = null;
        queryable = null;
        cascaded = null;
        latLongBox = null;
        boundingBox = null;
        dimNames = new TreeSet();
    }

    /**
     * Creates a "sublayer", a child of the specified parent
     * @param _parent parent layer of this newly created layer.
     */
    public WMSLayer(WMSLayer _parent)
    {
        parent = _parent;
        title = null;
        name = null;
        layerAbstract = null;
        subLayers = null;
        supportedSRS = null;
        styles = null;
        dimensions = null;
        extents = null;
        opaque = null;
        queryable = null;
        cascaded = null;
        latLongBox = null;
        boundingBox = null;
        dimNames = new TreeSet();
    }

    /**
     * Sets the boundary box
     * @param _boundingBox boundary box of this layer
     */
    public void setBoundingBox(WMSBoundingBox _boundingBox)
    {
        boundingBox = _boundingBox;
    }

    public void setLatLongBox(WMSLatLongBox _latLongBox)
    {
        latLongBox = _latLongBox;
    }

    public void setCascaded(boolean _cascaded)
    {
        if(_cascaded)
            cascaded = "1";
        else
            cascaded = "0";
    }

    public void setQueryable(boolean _queryable)
    {
        if(_queryable)
            queryable = "1";
        else
            queryable = "0";
    }

    public void setOpaque(boolean _opaque)
    {
        if(_opaque)
            opaque = "1";
        else
            opaque = "0";
    }

    public void addExtents(String name, WMSLayerExtent extent)
    {
        if(extents == null)
            extents = new Hashtable();

        extents.put(name, extent);
    }

    public void setExtents(Hashtable _extents)
    {
        extents = _extents;
    }

    /**
     *
     * @param dim
     */
    public void addDimension(WMSDimension dim)
    {
        if(dimensions == null)
        {
            dimensions = new Vector();
        }

        if(!dimNames.contains(dim.getName()))
        {
            dimensions.add(dim);
            this.dimNames.add(dim.getName());
        }
    }

    public void setDimensions(Vector _dimensions)
    {
        dimensions = _dimensions;
    }

    public void addStyle(WMSStyle style)
    {
        if(styles == null)
            styles = new Vector();

        styles.add(style);
    }

    public void setStyles(Vector _styles)
    {
        styles = _styles;
    }

    public void addSupportedSRS(String _SRS)
    {
        if(supportedSRS == null)
            supportedSRS = new Vector();

        supportedSRS.add(_SRS);
    }

    public void setSupportedSRS(Vector _supportedSRS)
    {
        supportedSRS = _supportedSRS;
    }

    public void setLayerAbstract(String _layerAbstract)
    {
        layerAbstract = _layerAbstract;
    }

    public void addSubLayer(WMSLayer _subLayer)
    {
        if(subLayers == null)
            subLayers = new Vector();

        subLayers.add(_subLayer);
    }

    public void setSubLayers(Vector _subLayers)
    {
        subLayers = _subLayers;
    }


    public void setName(String _name)
    {
        name = _name;
    }

    public void setTitle(String _title)
    {
        title = _title;
    }

    public void setParemt(WMSLayer _parent)
    {
        parent = _parent;
    }

    /**
     * Get the extents for this layer.
     * Extents sets the possible value that a layer can have on a given
     * dimension.  A layer must contain a dimension (or inherited dimension)
     * for which this extent belongs to.
     * <br>
     * This property is inherited
     * @return a Hashtable of extents, where the key is the <b>Dimension</b> name and the value is a WMSExtent
     */
    public Hashtable getExtents()
    {
        if(parent != null)
        {
            Hashtable parentHash = parent.getExtents();

            if(this.extents == null)
                return parentHash;

            if(parentHash != null)  // && (extent != null)
            {
                Hashtable result = (Hashtable)extents.clone();
                for(Enumeration e = parentHash.keys(); e.hasMoreElements();)
                {
                    String key = (String)(e.nextElement());
                    if(!extents.containsKey(key))
                    {
                        result.put(key, parentHash.get(key));
                    }
                }
                return result;
            }
        }

        return extents;
    }

    /**
     * Get the parent of this layer.
     * @return the parent of this WMSLayer.  Only possible to have one parent
     */
    public WMSLayer getParent()
    {
        return parent;
    }

    /**
     * Get the unique name of this layer.
     * @return name of this layer (a unique identifer)
     */
    public String getLayerName()
    {
        return this.name;
    }

    /**
     * Get the title of this layer.
     * A more detailed description of this layer. This does not have to be unique.
     * @return title of this layer
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Get a vector of sub layers
     * @return a Vector of WMSLayers
     */
    public Vector getSubLayers()
    {
        return subLayers;
    }

    /**
     * Get the abstract of this layer.
     * A more lengthy description of this layer
     * @return abstract of this layer.
     */
    public String getAbstract()
    {
        return layerAbstract;
    }

    //inheritence as defined by the WMS standard!!!! :)

    public Vector getCoordSystem()
    {
        return getSupportedSRS();
    }

    /**
     * Get SRS that this layer supports.
     * SRS is the type of projection this layer supports.<br>
     * This property can be inherited
     * @return a Vector of Strings, where each value is the name of the supported SRS.
     */
    public Vector getSupportedSRS()
    {
        Vector parentSRS = null;

        if(parent != null)
        {
            parentSRS = (parent.getSupportedSRS());
        }

        if(supportedSRS != null)
        {
            if(parentSRS != null)
            {
                parentSRS = (Vector)(parentSRS.clone());
                parentSRS.addAll(this.supportedSRS);
                return parentSRS;
            }
            else return supportedSRS;
        }
        else
            return parentSRS;
    }

    /**
     * Get any dimensions associated with this layer.
     * This is an inherited property
     * @return A Vector of WMSDimension
     */
    public Vector getDimensions()
    {
        Vector parentDimension = null;
        if(parent != null)
        {
            parentDimension = (parent.getDimensions());
        }

        if(dimensions != null)
        {
            if(parentDimension != null)
            {
                parentDimension = (Vector)(parentDimension.clone());
                parentDimension.addAll(this.dimensions);
                return parentDimension;
            }

            else
            {
                return dimensions;
            }

        }
        else
            return parentDimension;
    }

    /**
     * Get the style of a specified style name
     * This property can be inherited.
     * @param styleName name of this style.
     * @return the WMSStyle if a style by given name exists, null otherwise
     */
    public WMSStyle getStyleByName(String styleName)
    {
        Vector allStyles = getStyles();

        for(int i = 0; i < allStyles.size(); i++)
        {
            WMSStyle style = (WMSStyle)(allStyles.elementAt(i));
            if(style.getName().equalsIgnoreCase(styleName))
            {
                return style;
            }
        }
        return null;
    }

    /**
     * Get all supported styles of this layer.
     * This property can be inherited.
     * @return A Vector of WMSStyle
     */
    public Vector getStyles()
    {
        Vector parentStyle = null;
        if(parent != null)
        {
            parentStyle = (parent.getStyles());
        }

        if(styles != null)
        {
            if(parentStyle != null)
            {
                parentStyle = (Vector)(parentStyle.clone());
                parentStyle.addAll(this.styles);
                return parentStyle;
            }
            else
            {
                return styles;
            }
        }
        else
            return parentStyle;
    }

    /**
     * Get the bounding box of this layer.
     * This property can be inherited
     * @return a WMSBoundingBox associated with this layer
     */
    public WMSBoundingBox getBoundingBox()
    {
        if(this.boundingBox != null)
        {
            return boundingBox;
        }

        if(this.parent != null)
            return parent.getBoundingBox();
        return null;
    }

    /**
     * Get the latitude and latitude bounding box of this layer.
     * This property can be inherited.
     */
    public WMSLatLongBox getLatLongBox()
    {
        if(this.latLongBox != null)
        {
            return latLongBox;
        }

        if(this.parent != null)
        {
            return parent.getLatLongBox();
        }
        return null;
    }

    /**
     * Get the "cascaded" attribute of this layer
     * @return a String, with can either be "0" or positive integer
     */
    public String getCascaded()
    {
        return cascaded;
    }

    /**
     * Get the "opaque" value of this layer.
     * Generally, opaque layers are maps containing a lot of data, such
     * that it "should not" be layered on top of other layers.  Thus,
     * opaque layers should be rendered at the bottom most layer if possible.
     * @return a String indicating whether this layer is opaque, "0" = not completely filled, "1" = filled.
     */
    public String getOpaque()
    {
        return opaque;
    }

    /**
     * Get the "queryable" value of this layer.
     * Whether this layer supports GetFeatureInfo.
     * @return a String indicating whether this layer is queryable, "0" = does not supports GetFeautreInfo , "1" =  supports GetFeautreInfo
     */
    public String getQueryable()
    {
        return queryable;
    }


    protected void saveAttributeData(String attName, String attValue)
    {
         if(attName.equalsIgnoreCase("opaque"))
            opaque = attValue;
        else if(attName.equalsIgnoreCase("queryable"))
        {
            queryable = attValue;
        }
        else if(attName.equalsIgnoreCase("cascade"))
            cascaded = attValue;
    }



    protected void saveNodeData(String nodeName, String nodeData)
    {
        if((title == null) && (nodeName.compareToIgnoreCase("title") == 0))
            title = nodeData;
        else if((name == null) && (nodeName.compareToIgnoreCase("name") == 0))
            name = nodeData;
        else if((layerAbstract == null) && (nodeName.compareToIgnoreCase("abstract") == 0))
            layerAbstract = nodeData;
        else if(nodeName.compareToIgnoreCase("SRS") == 0)
        {
            if(this.supportedSRS == null)
                supportedSRS = new Vector();

            String[] sepSRS = nodeData.split(" ");

            if(sepSRS.length > 1)
            {
                for(int i = 0; i < sepSRS.length; i++)
                {
                    supportedSRS.add(sepSRS[i]);
                }
            }
            else
                supportedSRS.add(nodeData);
        }
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        if(childName.equalsIgnoreCase("layer"))
        {
            WMSLayer myLayer = new WMSLayer(this);
            myLayer.parse(childNode);
            if(subLayers == null)
                subLayers = new Vector();
            this.subLayers.add(myLayer);
            return true;
        }
        else if(childName.equalsIgnoreCase("boundingbox"))
        {
            this.boundingBox = new WMSBoundingBox((Element)childNode);
            return true;
        }
        else if(childName.equalsIgnoreCase("latlonboundingbox"))
        {
            this.latLongBox = new WMSLatLongBox((Element)childNode);
            return true;
        }
        else if(childName.equalsIgnoreCase("style"))
        {
            WMSStyle myStyle = new WMSStyle();
            myStyle.parse(childNode);
            if(this.styles == null)
                styles = new Vector();
            styles.add(myStyle);

            return true;
        }
        else if(childName.equalsIgnoreCase("dimension"))
        {
            WMSDimension myDim = new WMSDimension((Element)childNode);
            if(this.dimensions == null)
                dimensions = new Vector();

            dimensions.add(myDim);
            return true;
        }
        else if(childName.equalsIgnoreCase("extent"))
        {
            WMSLayerExtent extent = new WMSLayerExtent();
            extent.parse(childNode);
            Vector dims = getDimensions();
            for(int i = 0; i < dims.size(); i++)
            {
                WMSDimension dim = (WMSDimension)(dims.elementAt(i));
                if(dim.getName().equalsIgnoreCase(extent.getExtentName()))
                {
                  extent.setDim(dim);
                }
            }

            if(extents == null)
                extents = new Hashtable();

            extents.put(extent.getExtentName(), extent);
            return true;
        }

        return false;
    }

    /**
     * Checks whether this layer contains subLayers
     * @return true if there are sublayers, false otherwise
     */
    public boolean hasSubLayers()
    {
        return (subLayers != null);
    }

    /**
     * Searches the layers to find a layer by its title (human readable name)
     * @param titleName title to search for
     * @return the found layer or null if no such layer is found
     */
    public WMSLayer getLayerByTitle(String titleName)
    {
        if((title != null) && (title.equalsIgnoreCase(titleName)) )
            return this;

        if(subLayers != null)
        {
            for(int i = 0; i < subLayers.size(); i++)
            {
                WMSLayer sub = (WMSLayer)(subLayers.elementAt(i));

                WMSLayer newLayer = sub.getLayerByTitle(titleName);
                if(newLayer != null)
                    return newLayer;
            }

        }
        return null;
    }

    /**
     * Get a layer with the given name.
     * This will recurssively search through its sublayers.
     * @param layerName name of the layer.
     * @return a WMSLayer if one can be found, false otherwise.
     */
    public WMSLayer getLayer(String layerName)
    {
        if((name != null) && (name.equalsIgnoreCase(layerName)))
            return this;

        if(subLayers != null)
        {

            for(int i = 0; i < subLayers.size(); i++)
            {
                WMSLayer sub = (WMSLayer)(subLayers.elementAt(i));

                WMSLayer newLayer = sub.getLayer(layerName);

                if(newLayer != null)
                    return newLayer;
            }
        }
            return null;
    }

    /**
     * Checks whether this layer contains a layer of the specified name in its sublayers
     * @param layerName name of the layer to check
     * @return true if its immediate sublayer contains the given layer, false otherwise.
     */
    public boolean containsLayer(String layerName)
    {
        if((name != null) && (name.equalsIgnoreCase(layerName)))
        {
            return true;
        }

        if(subLayers != null)
        {
            for(int i = 0; i < subLayers.size(); i++)
            {

                WMSLayer sub = (WMSLayer)(subLayers.elementAt(i));
                if((sub.name != null) && (sub.name.equalsIgnoreCase(layerName)))
                    return true;

                //if(sub.containsLayer(layerName))
                //    return true;
            }
        }
        return false;
    }

    protected void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "Layer";

        if(this.opaque != null)
        {
            this.params.add(new String[]{"opaque", opaque});
        }

        if(this.queryable != null)
        {
            this.params.add(new String[]{"queryable", queryable});
        }

        if(this.cascaded != null)
        {
            this.params.add(new String[]{"cascaded", cascaded});
        }

        if(name != null)
        {
            Element nameElement = doc.createElement("Name");
            Text txtNode = doc.createTextNode(name);
            nameElement.appendChild(txtNode);
            innerElements.add(nameElement);
        }

        if(title != null)
        {
            Element titleElement = doc.createElement("Title");
            Text txtNode = doc.createTextNode(title);
            titleElement.appendChild(txtNode);
            innerElements.add(titleElement);
        }

        if(layerAbstract != null)
        {
            Element abstractElement = doc.createElement("Abstract");
            Text txtNode = doc.createTextNode(layerAbstract);
            abstractElement.appendChild(txtNode);
            innerElements.add(abstractElement);
        }

        if(supportedSRS != null)
        {
            for(int i = 0; i < this.supportedSRS.size(); i++)
            {
                String anSrs = (String)(supportedSRS.elementAt(i));
                Element srsElement = doc.createElement("SRS");
                Text txtNode = doc.createTextNode(anSrs);
                srsElement.appendChild(txtNode);
                innerElements.add(srsElement);
            }
        }

        if(latLongBox != null)
            this.innerElements.add(this.latLongBox.createElement(doc));

        if(boundingBox != null)
            this.innerElements.add(boundingBox.createElement(doc));

        if(this.dimensions != null)
        {
            for(int i = 0; i < dimensions.size(); i++)
            {
                WMSDimension dim = (WMSDimension)(dimensions.elementAt(i));
                this.innerElements.add(dim.createElement(doc));
            }
        }

        if(this.extents != null)
        {
            Iterator extentIt = extents.keySet().iterator();
            while(extentIt.hasNext())
            {
                String key = (String)(extentIt.next());
                WMSLayerExtent extent = (WMSLayerExtent)(extents.get(key));
                this.innerElements.add(extent.createElement(doc));
            }

        }

        if(styles != null)
        {
            for(int i = 0; i < styles.size(); i++)
            {
                WMSStyle styleELemnt = (WMSStyle)(styles.elementAt(i));
                this.innerElements.add(styleELemnt.createElement(doc));
            }
        }

        if(subLayers != null)
        {
            for(int i = 0; i < subLayers.size(); i++)
            {
                WMSLayer sLayer = (WMSLayer)(subLayers.elementAt(i));
                this.innerElements.add(sLayer.createElement(doc));
            }
        }


    }


    /**
     * Compares one WMSLayer with another.
     * Order is defined by its opacity.
     * @param obj1 first object to compare
     * @param obj2 second object to compare
     * @return -1 if obj1 is opaque, 1 if obj2 is opaque, if undefined or is not opaque, 1 will be returned
     */
    public int compare(Object obj1, Object obj2)
    {
        WMSLayer layer1 = (WMSLayer)(obj1);
        WMSLayer layer2 = (WMSLayer)(obj2);

        if(layer1.getOpaque() == null)
            return 1;
        else if(layer2.getOpaque() == null)
            return 1;
        else if(layer1.getOpaque().equals("1"))
            return -1;
        else
            return 1;
    }

    /**
     * @param obj compares another WMSLayer with this WMSLayer.
     * @return  -1 if obj1 is opaque, 1 if obj2 is opaque, if undefined or is not opaque, 1 will be returned
     */
    public int compareTo(Object obj)
    {
        return compare(this, obj);
    }
}
