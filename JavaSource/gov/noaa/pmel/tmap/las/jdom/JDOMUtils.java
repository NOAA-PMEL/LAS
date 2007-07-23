package gov.noaa.pmel.tmap.las.jdom;

import gov.noaa.pmel.tmap.las.exception.LASException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
/**
 * A collection of helper methods for managing JDOM documents.
 * @author Roland Schweitzer
 *
 */
public class JDOMUtils {
    /**
     * Generates a Document from a XML input stream
     * 
     * @param is the input xml stream
     * @throws JDOMException if something goes wrong during the parsing process
     * @throws IOException if something goes wrong reading stream
     */
    public static void XML2JDOM(Reader is, Document doc) throws IOException, JDOMException {
        Document tdoc = new Document();
        SAXBuilder builder = new SAXBuilder();
        builder.setExpandEntities(false);
        builder.setEntityResolver(new MyResolver());
        tdoc = builder.build(is);
        Element root = tdoc.getRootElement();
        Element new_root = (Element) root.clone();
        doc.setRootElement(new_root);
    }
    
    /**
     * Generates a Document from a XML string.
     * 
     * @param xml the input xml string
     * @throws JDOMException if something goes wrong during the parsing process
     * @throws IOException if something goes wrong reading the stream
     */
    public static void XML2JDOM(String xml, Document doc) throws IOException, JDOMException {
        StringReader sr = new StringReader(xml);
        XML2JDOM(sr, doc);
    }
    
    /**
     * Generates a Document from a XML file
     * 
     * @param file the input xml file handle
     * @throws IOException if something goes wrong during the parsing process
     */
    public static void XML2JDOM(File file, Document doc) throws IOException, JDOMException {
        Document tdoc = new Document();
        SAXBuilder builder = new SAXBuilder();
        // builder.setExpandEntities(false);
        builder.setEntityResolver(new MyResolver());
        tdoc = builder.build(file);
        Element root = tdoc.getRootElement();
        Element new_root = (Element) root.clone();
        doc.setRootElement(new_root);
    }
    
    /**
     * Make an MD5 Hash from a particular string.
     * @param str - the string to be hashed.
     * @return md5 - the resulting hash
     * @throws UnsupportedEncodingException
     */
    public static String MD5Encode(String str) throws UnsupportedEncodingException {
        String returnVal = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte mdArr[] = md.digest(str.getBytes("UTF-16"));
            returnVal = toHexString(mdArr);
        } catch (Exception e) {
            returnVal = URLEncoder.encode(str,"UTF-8").replaceAll("\\*", "x");
        }
        return returnVal;
    }
    protected static String toHexString(byte bytes[]) {
        char chars[] = new char[bytes.length * 2];
        
        for (int i = 0; i < bytes.length; ++i) {
            chars[2 * i] = HEXCODE[(bytes[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEXCODE[bytes[i] & 0x0F];
        }
        return new String(chars);
    }
    
    protected static final char HEXCODE[] = { '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };   
    
    public static String getResourcePath(Object object, String resource) {
        
        URL resourceURL = object.getClass().getClassLoader().getResource(resource);
        
        if ( resourceURL != null ) {
            try {
                return resourceURL.toURI().getPath();
                
            } catch (URISyntaxException e) {
                return null; 
            }
            
        } else {
            return null;
        }
    }

    /**
     * @param content
     * @return
     */
    public static String toJavaScriptSafeString(String content) {
        content = content.replaceAll("'","\\\\'");
        content = content.replaceAll("\"","\\\\\"");
        content = content.replaceAll("\\r\\n","");
        content = content.replaceAll("\\r","");
        content = content.replaceAll("\\n","");
        return content;
    }
}
