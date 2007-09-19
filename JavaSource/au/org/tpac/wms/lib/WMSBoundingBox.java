/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/

package au.org.tpac.wms.lib;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * <p>
 * This specifies a bounding box and also a project type.
 * It is based on retangular area defined by 4 decimals. <br>
 * For more information, please see Section 6.5.5.1 of the
 * WMS 1.1.1 specification.
 * @see tpac.wms.lib.WMSLatLongBox
 * </p>
 *
 * <p>
 * Date: 3/02/2006<br>
 * Time: 11:21:18<br>
 * </p>
 * @author Pauline Mak, pauline@insight4.com (Insight Pty. Ltd)
 */
public class WMSBoundingBox extends WMSLatLongBox
{
    protected String SRS;

    /**
     * Sole constructor for this class
     * @param element The DOM element containing data for this WMSBoundingBox
     */
    public WMSBoundingBox(Node element)
    {
        super(element);
    }

    protected void setXMLElementSelf(Document doc)
    {
        super.setXmlElements(doc);
        this.params.add(new String[]{"SRS", SRS});
    }

    /**
     * Sets the coordinate system
     * @param _SRS type of SRS
     */
    public void setSRS(String _SRS)
    {
        SRS = _SRS;
    }

    protected void saveAttributeData(String attributeName, String attributeValue)
    {
        super.saveAttributeData(attributeName, attributeValue);   //save all the min,max stuff

        if(attributeName.compareToIgnoreCase("SRS") == 0)
            SRS = attributeValue;
    }

    /**
     * Get the SRS that this bounding box is associated with
     * @return the SRS type.
     */
    public String getSRS()
    {
        return SRS;
    }
}
