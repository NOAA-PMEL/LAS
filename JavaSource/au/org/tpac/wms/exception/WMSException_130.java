/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.exception;

import org.w3c.dom.Document;
import au.org.tpac.wms.utils.DocCreator;


/**
 * This is a WMSException for WMS version 1.3.0.  Note that this is only a stubb class and
 * does not fully implement WMS 1.3.0 exceptions!!
 */
public class WMSException_130 extends WMSException
{
    public final static String version = "1.3.0";

    public WMSException_130()
    {
        super();
    }

    public WMSException_130(String error)
    {
        super(error);
    }

    public String getDocType()
    {
        return "http://schemas.opengis.net/wms/1.3.0/exceptions_1_3_0.xsd";
    }

    public String toString()
    {
        return DocCreator.writDoc(doc, getDocType());
    }

    protected Document createDocument()
    {
        return DocCreator.createDocument(getDocType());
    }
}
