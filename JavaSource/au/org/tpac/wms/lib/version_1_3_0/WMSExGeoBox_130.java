package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.WMSParser;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 14:10:18
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
public class WMSExGeoBox_130 extends WMSParser
{
    protected float west;
    protected float east;
    protected float south;
    protected float north;

    public WMSExGeoBox_130()
    {

    }

    public WMSExGeoBox_130(Node node)
    {
        parse(node);
    }

    public float getWest()
    {
        return west;
    }

    public float getEast()
    {
        return east;
    }

    public float getSouth()
    {
        return south;
    }

    public float getNorth()
    {
        return north;
    }

    public float getMinX()
    {
        return west;
    }

    public float getMaxX()
    {
        return east;
    }

    public float getMaxY()
    {
        return north;
    }

    public float getMinY()
    {
        return south;
    }

    protected void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "EX_GeographicBoundingBox";

        Element westElement = doc.createElement("westBoundLongitude");
        Text westNode = doc.createTextNode(west + "");
        westElement.appendChild(westNode);

        Element eastElement = doc.createElement("eastBoundLongitude");
        Text eastNode = doc.createTextNode(east + "");
        eastElement.appendChild(eastNode);

        Element southElement = doc.createElement("southBoundLatitude");
        Text southNode = doc.createTextNode(south + "");
        southElement.appendChild(southNode);

        Element northElement = doc.createElement("northBoundLatitude");
        Text northNode = doc.createTextNode(north + "");
        northElement.appendChild(northNode);

        this.innerElements.add(westElement);
        this.innerElements.add(eastElement);
        this.innerElements.add(southElement);
        this.innerElements.add(northElement);
    }

    protected void saveAttributeData(String attName, String attValue)
    {

    }

    protected void saveNodeData(String nodeName, String nodeData)
    {
        if(nodeName.equalsIgnoreCase("westBoundLongitude"))
            west = Float.parseFloat(nodeData);
        else if(nodeName.equalsIgnoreCase("eastBoundLongitude"))
            east = Float.parseFloat(nodeData);
        else if(nodeName.equalsIgnoreCase("southBoundLatitude"))
            south = Float.parseFloat(nodeData);
        else if(nodeName.equalsIgnoreCase("northBoundLatitude"))
            north = Float.parseFloat(nodeData);
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        return false;
    }

}
