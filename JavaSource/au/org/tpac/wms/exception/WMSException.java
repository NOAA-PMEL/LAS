/*
*	Copyright Insight4 Pty Ltd 2005-2006 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.exception;

import org.w3c.dom.*;


import au.org.tpac.wms.utils.DocCreator;


/**
 * This class represents a WMS exception - when WMS cannot fulfill a request.
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class WMSException
{
    /**
     * Version of this exception
     */
    public final static String version = "1.1.1";

    /**
     * Last error that this exception has encountered
     */
    protected String lastError;
    protected Document doc;

    /**
     * Empty constructor
     */
    public WMSException()
    {
        lastError = "";
    }

    private String getDocType()
    {
        return "http://schemas.opengis.net/wms/1.1.1/exception_1_1_1.dtd";
    }

    /**
     * A WMSException with a specified error message
     * @param error error message
     */
    public WMSException(String error)
    {
        lastError = "";
        doc = createDocument();
        if(doc != null)
        {
            Element se = doc.createElement("ServiceException");
            CDATASection cdata = doc.createCDATASection(error);
            se.appendChild(cdata);
            doc.getDocumentElement().appendChild(se);
        }
    }


    /**
     * Returns the exception as a formatted XML string
     * @return exception as an XML string
     */
    public String toString()
    {
        return DocCreator.writDoc(doc, getDocType());
    }

    /**
     * Creates an XML document
     * @return an XML document
     */
    protected Document createDocument()
    {
        return DocCreator.createDocument(getDocType());
    }


}
