/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.lib;

import org.w3c.dom.*;

/**
 * <p>
 * This class represents styles that a layer can be viewed with.<br>
 * </p>
 * <p>
 * Date: 2/02/2006 <br>
 * Time: 16:31:32  <br>
 * </p>
 * @author Pauline Mak, pauline@insight4.com (Insight4 Pty. Ltd.)
 */
public class WMSStyle extends WMSParser
{
    /**
     * The layer that contains this style
     */
    private WMSLayer parentLayer;

    /**
     * A unique name for this style
     */
    private String name;

    /**
     * A human readable title
     */
    private String title;

    /**
     * A blurb about this style
     */
    private String styleAbstract;

    /**
     * The legend associated with this style.  Note that styles do not always have
     * to have a legend
     */
    private WMSLegend legend;

    /**
     * An empty constructor
     */
    public WMSStyle()
    {
        name = "";
        title = "";
        styleAbstract = "";
        legend = null;
        elementName = "";
    }

    /**
     * A constructor with a specified WMSLayer to associate with and also
     * a node to parse
     * @param _parentLayer parent WMSLayer
     * @param styleNode a DOM element to parse
     */
    public WMSStyle(WMSLayer _parentLayer, Node styleNode)
    {
        parentLayer = _parentLayer;
        elementName = "";
        parse(styleNode);

    }

    public void setLegend(WMSLegend _legend)
    {
        legend = _legend;
    }

    public void setStyleAbstract(String _styleAbstract)
    {
        styleAbstract = _styleAbstract;
    }

    public void setTitle(String _title)
    {
        title = _title;
    }

    public void setName(String _name)
    {
        name = _name;
    }

    public void setParentLayer(WMSLayer _parentLayer)
    {
        parentLayer = _parentLayer;
    }

    public void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "Style";

        if(name.length() > 0)
        {
            Element nameElement = doc.createElement("Name");
            Text txtNode = doc.createTextNode(this.name);
            nameElement.appendChild(txtNode);
            innerElements.add(nameElement);
        }

        if(title.length() > 0)
        {
            Element titleElement = doc.createElement("Title");
            Text txtNode = doc.createTextNode(title);
            titleElement.appendChild(txtNode);
            innerElements.add(titleElement);
        }

        if(styleAbstract.length() > 0)
        {
            Element abstractElement = doc.createElement("Abstract");
            Text txtNode = doc.createTextNode(styleAbstract);
            abstractElement.appendChild(txtNode);
            this.innerElements.add(abstractElement);
        }

        if(legend != null)
        {
            this.innerElements.add(legend.createElement(doc));
        }
    }



    /**
     * Get the parent WMSLayer
     * @return the WMSLayer this style belongs to
     */
    public WMSLayer getParent()
    {
        return this.parentLayer;
    }

    /**
     * Get the name of this style (this is unique)
     * @return name of this style
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the title of this style
     * @return the title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Get the abstract of this style
     * @return the abstract
     */
    public String getAbstract()
    {
        return styleAbstract;
    }

    /**
     * Get the legend associated with this layer
     * @return a WMSLegend - note this can be null
     */
    public WMSLegend getLegend()
    {
        return legend;
    }

    protected void saveNodeData(String nodeName, String nodeValue)
    {
        if(nodeName.equalsIgnoreCase("name"))
        {
            name = nodeValue;
        }
        else if(nodeName.equalsIgnoreCase("title"))
            this.title = nodeValue;
        else if(nodeName.equalsIgnoreCase("abstract"))
            this.styleAbstract = nodeValue;
    }

    protected void saveAttributeData(String attName, String attValue)
    {

    }

    protected boolean processChildren(String childName, Node childNode)
    {
        if(childName.equalsIgnoreCase("LegendURL"))
        {
            legend = new WMSLegend();
            legend.parse(childNode);
            return true;
        }
        return false;
    }

}
