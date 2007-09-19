/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.request;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;

import au.org.tpac.wms.lib.WMSCapabilities;
import au.org.tpac.wms.lib.WMSCapabilities_111;
import au.org.tpac.wms.lib.version_1_3_0.WMSCapabilities_130;
import au.org.tpac.wms.request.WMSRequest;
import au.org.tpac.wms.request.WMSRequest_130;

/**
 * A factory class for creating WMSCapabilities and WMSRequest based on
 * the version number.
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class WMSCapFactory
{
    protected String lastError;
   // protected String capabilitiesAddress;

    public WMSCapFactory()
    {
        //capabilitiesAddress = _capabilitiesAddress;
        lastError = "";
    }

    public WMSCapabilities createEmptyCap(String version)
    {
        if(version.equals("1.1.1"))
            return new WMSCapabilities_111();
        else if(version.equals("1.3.0"))
            return new WMSCapabilities_130();
        return null;
    }

    /**
     *
     * @param _capabilitiesAddress
     * @return a WMSRequest object based on the address (which also determines the version nunmber)
     */
    public WMSCapabilities createCap(String _capabilitiesAddress)
    {
        Document doc = getDoc(_capabilitiesAddress);

        if(doc != null)
        {
            Element top = doc.getDocumentElement();

            if(top.getAttribute("version").equals("1.1.1"))
            {
               return new WMSCapabilities_111(_capabilitiesAddress, doc.getDocumentElement());
            }
            else if(top.getAttribute("version").equals("1.3.0"))
            {
                return new WMSCapabilities_130(_capabilitiesAddress, doc.getDocumentElement());
            }
        }

        return null;    //throw unsupported exception??
    }


    /**
     * Creates a WMSRequest based on a WMSCapabilties object
     * @param cap capabilities to generate a WMSRequest from
     * @return an appropriately versioned WMSRequest object
     */
    public WMSRequest createRequest(WMSCapabilities cap)
    {
        if(cap.getVersion().equalsIgnoreCase("1.1.1"))
        {
            return new WMSRequest(cap);
        }
        else if(cap.getVersion().equalsIgnoreCase("1.3.0"))
        {
            return new WMSRequest_130((WMSCapabilities_130)(cap));
        }

        return null;
    }

    /**
     * Get the string used for getting the capabilites statement of the given WMS server
     * @return the request string for getting the aapabilities statement.
     */
    public String getCapRequestString(String _capabilitiesAddress)
    {
        String result = "";
        if(_capabilitiesAddress.indexOf("?") == -1)
            result =_capabilitiesAddress + "?SERVICE=WMS&REQUEST=GetCapabilities";
        else
            result = _capabilitiesAddress+ "&SERVICE=WMS&REQUEST=GetCapabilities";

        System.out.println(result);
        return result;
    }


    /**
     * Get the DOM document as retrieved containing the WMS GetCapabilites response<br>
     * This method connects to the server and sends a GetCapabilites request to the WMS
     * address and retrieves the WMS response and constructs a DOM document.
     * @return DOM document containing the WMS GetCapabilites response
     */
    protected Document getDoc(String _capabilitiesAddress)
    {
         //setting up document
        String jaxpPropertyName = "javax.xml.parsers.DocumentBuilderFactory";
        if (System.getProperty(jaxpPropertyName) == null)
        {
            String apacheXercesPropertyValue = "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";
            System.setProperty(jaxpPropertyName, apacheXercesPropertyValue);
        }

        DocumentBuilderFactory builderFactory = null;
        DocumentBuilder builder = null;

        try
        {
            builderFactory = DocumentBuilderFactory.newInstance();
            builder = builderFactory.newDocumentBuilder();
        }
        catch(ParserConfigurationException pce)
        {
            lastError = "Error with parser configuration: error message was: " + pce;
        }

        InputStream stream  = null;

        if((builder != null) && lastError.equalsIgnoreCase(""))
        {

            try
            {
                URL url;
                url = new URL(getCapRequestString(_capabilitiesAddress));
                stream = url.openStream();

                Document doc = builder.parse(stream);

                doc.getDocumentElement().normalize();
                stream.close();

                //got it successfully!
                return doc;
            }
            catch(MalformedURLException e)
            {
                lastError = "Error connecting to server address: " + _capabilitiesAddress + " exception message: " + e.toString();
                e.printStackTrace(System.out);
            }
            catch(IOException ex)
            {
                lastError = "Error opening connection to server address:"  + _capabilitiesAddress + " exception message: " + ex.toString();
                ex.printStackTrace(System.out);
            }
            catch(SAXParseException spe)
            {

                String err = spe.toString() +
                "\n  Line number: " + spe.getLineNumber() +
                "\nColumn number: " + spe.getColumnNumber()+
                "\n Public ID: " + spe.getPublicId() +
                "\n System ID: " + spe.getSystemId() ;
                System.out.println( err );
            }
            catch(SAXException saxe)
            {
                lastError = "Error with SAX.  Exception message: " + saxe.toString();
                saxe.printStackTrace(System.out);

            }

        }

        lastError = "Builder is null or compounded error: previous error message: " + lastError;

        try
        {
            if(stream != null)
                stream.close();
        }
        catch(IOException ioe)
        {
            lastError = "cannot close stream..." + lastError;
        }

        System.out.println("lastError: " + lastError);



        return null;
    }
}
