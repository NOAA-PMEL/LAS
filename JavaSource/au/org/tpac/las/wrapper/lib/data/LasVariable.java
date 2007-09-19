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

/**
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class LasVariable extends WMSParser
{
    protected String link;
    protected String name;
    protected String units;
    protected String varId;
    protected LasGrid grid;
    
    public LasVariable()
    {
        link = null;
        name = null;
        units = null;
        varId = null;
    }

    public LasVariable(Node node)
    {
        link = null;
        name = null;
        units = null;
        varId = null;

        parse(node);
    }

    protected void setXMLElementSelf(Document doc)
    {

    }

    public LasGrid getGrid()
    {
        return grid;
    }

    public void setGrid(LasGrid _grid)
    {
        grid = _grid;
    }

    public String getLink()
    {
        return link;
    }

    public String getName()
    {
        return name;
    }

    public String getUnits()
    {
        return units;
    }

    public String getVarId()
    {
        return varId;
    }

    protected void saveAttributeData(String attributeName, String attributeValue)
    {
        if(attributeName.equalsIgnoreCase("name"))
        {
            name = attributeValue;
        }
        else if(attributeName.equalsIgnoreCase("units"))
        {
            units = attributeValue;
        }
        else if(attributeName.equalsIgnoreCase("ID"))
        {
            this.varId = attributeValue;
        }
    }

    protected void saveNodeData(String nodeName, String nodValue)
    {

    }

    protected boolean processChildren(String nodeName, Node childNode)
    {
        if(nodeName.equalsIgnoreCase("grid"))
        {
            link = ((Element)(childNode)).getAttribute("IDREF");
            return true;
        }
        return false;
    }
}
