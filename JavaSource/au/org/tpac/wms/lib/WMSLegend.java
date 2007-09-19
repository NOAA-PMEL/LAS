/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.lib;

import org.w3c.dom.*;

/**
 * <p>
 * This class represents legends for layers.<br>
 * Each layer can contain a number of legends, in most
 * cases, this would be an image with a scale (such as distance,
 * of colour indicators).
 * </p>
 *
 * <p>
 * Date: 2/02/2006
 * Time: 17:09:27
 * </p>
 *
 * @author Pauline Mak, pauline@insight4.com (Insight4 Pty. Ltd.)
 */
public class WMSLegend extends WMSParser
{
    /**
     * Width of the legend graphic in pixels
     */
    protected int width;

    /**
     * height of the legend graphic  in pixels
     */
    protected int height;

    /**
     * MUME type of the legend graphi
     */
    protected String format;

    /**
     * URL to the legend graphic
     */
    protected WMSOnlineResource resource;

    /**
     * Empty constructor
     */
    public WMSLegend()
    {
        width = 0;
        height = 0;
        format = "";
        resource = null;
    }

    public WMSLegend(Node node)
    {
        width = 0;
        height = 0;
        format = "";
        resource = null;
        parse(node);
    }

    /**
     * Sets the URL to the legend graphic
     * @param _resource WMSOnlineResource object
     */
    public void setResource(WMSOnlineResource _resource)
    {
        resource = _resource;
    }

    /**
     * Sets the mime type of the legend graphic type
     * @param _format MIME type of the legend graphic
     */
    public void setFormat(String _format)
    {
        format = _format;
    }

    /**
     * Sets the width of the legend graphic
     * @param _width width in pixels
     */
    public void setWidth (int _width)
    {
        width = _width;
    }

    /**
     * Sets the height of the legend graphic
     * @param _height height in pixels
     */
    public void setHeight(int _height)
    {
        height = _height;
    }

    /**
     * Get the width of the legend image
     * @return width in pixels
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Get the height of the legend image
     * @return the height in pixels
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Get the image format (e.g. png, gif, etc)
     * @return the format of the image.
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * Get the full URL to the legend image
     * @return URL to the legend image
     */
    public String getLegendURL()
    {
        return resource.getURL();
    }


    public void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "LegendURL";

        this.params.add(new String[]{"width", this.width + ""});
        this.params.add(new String[]{"height", this.height + ""});

        Element formatElement = doc.createElement("Format");
        Text txtNode = doc.createTextNode(format);
        formatElement.appendChild(txtNode);
        this.innerElements.add(formatElement);
        this.innerElements.add(resource.createElement(doc));
    }

    protected void saveAttributeData(String attName, String attValue)
    {
        if(attName.equalsIgnoreCase("width"))
        {
            width = Integer.parseInt(attValue);
        }
        else if(attName.equalsIgnoreCase("height"))
        {
            height = Integer.parseInt(attValue);
        }

    }

    protected void saveNodeData(String nodeName, String nodeValue)
    {
        if(nodeName.equalsIgnoreCase("format"))
        {
            format = nodeValue;
        }
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        if(childName.equalsIgnoreCase("OnlineResource"))
        {
            resource = new WMSOnlineResource();
            resource.parse(childNode);           
            return true;
        }
        return false;
    }
}
