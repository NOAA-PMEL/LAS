/**
 * 
 */
package gov.noaa.pmel.tmap.iosp;

import gov.noaa.pmel.tmap.iosp.IOSPException;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

/**
 * @author Roland Schweitzer
 *
 */
public class IOSPDocument extends Document {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public IOSPDocument() {
        super();
    }
    public IOSPDocument(Document doc) {
         setContent(doc.cloneContent());
    }
    
    public Element getElementByXPath(String xpathValue) throws JDOMException {
        // E.g. xpathValue="/lasdata/operations/operation[@ID='Plot']"
        Object jdomO = this;
        XPath xpath = XPath.newInstance(xpathValue);
        return (Element) xpath.selectSingleNode(jdomO);   
    }
    
    public String toCompactString() {
        Format format = Format.getCompactFormat();
        format.setOmitDeclaration(true);
        return "<?xml version=\"1.0\"?>"+toString(format);
    }
    
    public String toEncodedURLString() throws UnsupportedEncodingException {
        Format format = Format.getCompactFormat();
        format.setOmitDeclaration(true);
        String xml = toString(format);
        xml = xml.replaceAll("\\r\\n","");
        xml = xml.replaceAll("\\r","");
        xml = xml.replaceAll("\\n","");
        return URLEncoder.encode("<?xml version=\"1.0\"?>"+xml, "UTF-8");
    }
    
    public String toEncodedJavaScriptSafeURLString() throws UnsupportedEncodingException  {
        Format format = Format.getCompactFormat();
        format.setOmitDeclaration(true);
        String xml = toString(format);
        
        xml = xml.replaceAll("\"","\\\\\"");
        xml = xml.replaceAll("'","\\\\'");;
        xml = xml.replaceAll("\\r\\n","");
        xml = xml.replaceAll("\\r","");
        xml = xml.replaceAll("\\n","");
        return URLEncoder.encode("<?xml version=\"1.0\"?>"+xml, "UTF-8");
    }
    
    public String toJavaScriptSafeString() {
        Format format = Format.getCompactFormat();
        format.setOmitDeclaration(true);
        String xml = toString(format);
        xml = xml.replaceAll("'","\\\\'");
        xml = xml.replaceAll("\"","\\\\\"");
        xml = xml.replaceAll("\\r\\n","");
        xml = xml.replaceAll("\\r","");
        xml = xml.replaceAll("\\n","");
        return "<?xml version=\"1.0\"?>"+xml;
    }
    
    public String toString(Format format) {
        StringWriter xmlout = new StringWriter();
        try {
            format.setLineSeparator(System.getProperty("line.separator"));
            XMLOutputter outputter = new XMLOutputter(format);
            outputter.output(this, xmlout);
            // Close the FileWriter
            xmlout.close();
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return xmlout.toString();
    }
    
    public String toString() {
        Format format = Format.getPrettyFormat();
        return toString(format);
    }
    
    public void write(String fileName) {
        File file = new File(fileName);
        write(file);
    }
    
    public void write(File file) {
        try {
            FileWriter xmlout = new FileWriter(file);
            org.jdom.output.Format format = org.jdom.output.Format
            .getPrettyFormat();
            format.setLineSeparator(System.getProperty("line.separator"));
            XMLOutputter outputter = new XMLOutputter(format);
            outputter.output(this, xmlout);
            // Close the FileWriter
            xmlout.close();
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        
    }
    
}
