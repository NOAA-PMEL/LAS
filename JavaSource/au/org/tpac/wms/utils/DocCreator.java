/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.utils;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.StringWriter;

/**
 * This class is created as a convinience for generated XML documents
 * which can be manipulated by the WMS library.
 * @author Pauline Mak (pauline@insight4.com)
 */
public class DocCreator
{
    /**
     * Creates a document!
     * @param docType
     * @return A document
     */
    static public Document createDocument(String docType)
    {
        String jaxpPropertyName = "javax.xml.parsers.DocumentBuilderFactory";
        if (System.getProperty(jaxpPropertyName) == null)
        {
            String apacheXercesPropertyValue = "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";
            System.setProperty(jaxpPropertyName, apacheXercesPropertyValue);
        }

        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();
            DocumentType type = impl.createDocumentType("wms", "ServiceExceptionReport", docType);
            return builder.getDOMImplementation().createDocument("", "ServiceExceptionReport", type);
        }
        catch (ParserConfigurationException e)
        {
            System.out.println(e.toString());
        }

        return null;
    }

    /**
     * Exports the document as a stirng!
     * @param doc  document to write
     * @param docType the link to the document DTD
     * @return a string representation of the document (with indentations)
     */
   static public String writDoc(Document doc, String docType)
   {
       try
       {
           TransformerFactory xformFactory = TransformerFactory.newInstance();
           Transformer transformer = xformFactory.newTransformer();
           transformer.setOutputProperty(OutputKeys.INDENT, "yes");
           transformer.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM, docType);
           transformer.setOutputProperty(javax.xml.transform.OutputKeys.STANDALONE, "no");
           transformer.setOutputProperty(javax.xml.transform.OutputKeys.VERSION, "1.0");
           transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "ISO-8859-1");

           Source input = new DOMSource(doc);
           StringWriter writer = new StringWriter();
           Result output = new StreamResult(writer);
           transformer.transform(input, output);

           return writer.toString();
       }
       catch(FactoryConfigurationError e)
       {
            System.out.println("Error with Factory configuration.  Error message was: " + e.toString());
       }
       catch(TransformerConfigurationException e2)
       {
           System.out.println("Error with transformer configurtaion.  Error message was: " + e2.toString());
       }
       catch(TransformerException e3)
       {
           System.out.println("Error with transformer.  Error message was: " + e3.toString());
       }
       return null;
   }
}
