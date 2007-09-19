/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.lib;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;

/**
 * <p>
 * WMSCapabilities is the top most object for parsing and getting values of
 * a GetCapabilities response. For detailed description of this file, please
 * refer to Section 7.1.4 of the WMS 1.1.1 Specification.<br>
 * </p>
 *
 * <p>
 * Date: 7/02/2006  <br>
 * Time: 09:32:14 <br>
 * </p>
 * @author Pauline Mak, pauline@insight4.com (Insight4 Pty. Ltd.)
 *

 */
abstract public class WMSCapabilities extends WMSParser
{

	/**
		Last know error encoutnered by the WMSCapabilities object.
	*/
    protected String lastError;

    /**
	* The address for issueing a GetMap request.
	*/
    protected String serverAddress;

	/**
	* The address for issueing a GetCapabilities request.
	*/
    protected String capabilitiesAddress;

	/**
	* The top most layer found on the give server
	*/
    protected WMSLayer layer;

	/**
	* A set of request types that can be made to this server
	*/
    protected WMSRequestSet reqSet;

	/**
	* Possible exception types associated with the GetMap request.
	*/
    protected Vector exceptionTypes;


    //General Service Metadata
    protected String name;
    protected String title;
    protected String OnlineResourceURL;

    /**
     * Abstract associated with the WMS server
     */
    protected String serverAbstract;

    protected WMSContactInfo info;

    /**
     * Get the title of this WMS
     * @return title of the WMS
     */
    public String getTitle()
    {
        return title;
    }

     /**
     * Get the Name of this WMS
     * @return Name of the WMS
     */
    public String getName()
    {
        return name;
    }

     /**
     * Get the OnlineResourceURL of this WMS
     * @return OnlineResourceURL of the WMS
     */
    public String getOnlineResourceURL()
    {
        return OnlineResourceURL;
    }


    /**
     * Empty Constructor
     */
    public WMSCapabilities()
    {
        reqSet = new WMSRequestSet();
        exceptionTypes = new Vector();
        title = null;
        layer = null;
        lastError = "";
        serverAddress = null;
        capabilitiesAddress = null;
        serverAbstract = null;
    }

    /**
     * Constructor with an address that can be queried
     * @param _capabilitiesAddress address of WMS server - DO NOT put REQUEST=GetCapapbilities in the string.
     */
    public WMSCapabilities(String _capabilitiesAddress)
    {
        capabilitiesAddress = _capabilitiesAddress;
        reqSet = new WMSRequestSet();
        exceptionTypes = new Vector();
        title = null;
        layer = null;
        lastError = "";
        serverAddress = null;
        serverAbstract = null;
    }

    /**
     * This constructor "reconstitutes" a WMSCapabilties object based on an existing XML document
     * @param _capabilitiesAddress The address for retrieving the WMSCapabilties XML
     * @param node node to parse (and reconsitute the object from)
     */
    public WMSCapabilities(String _capabilitiesAddress, Node node)
    {
        capabilitiesAddress = _capabilitiesAddress;
        reqSet = new WMSRequestSet();
        exceptionTypes = new Vector();
        title = null;
        layer = null;
        lastError = "";
        serverAddress = null;
        serverAbstract = null;
        parse(node);
    }

    /**
     * Sets a description of the server
     * @param _serverAbstract description of the server
     */
    public void setServerAbstract(String _serverAbstract)
    {
        serverAbstract = _serverAbstract;
    }

    /**
     * Sets the server address (this address is used for getting the resulting map).
     * Note that any WMS related URL parameters MUST NOT be included
     * @param _serverAddress server address: Note that any WMS related URL parameters <b>MUST NOT</b> be included
     */
    public void setServerAddress(String _serverAddress)
    {
        serverAddress = _serverAddress;
    }

    /**
     * Sets the top most layer
     * @param _topLayer top WMS layer
     */
    public void setTopLayer(WMSLayer _topLayer)
    {
        layer = _topLayer;
    }

    /**
     * Sets the type of exception this WMS server will produce.
     * Please see page 14-15 of the WMS 1.1.1 spec to see what types are supported.
     * @param _exceptionTypes MIME type of the exception
     */
    public void setExceptionTypes(Vector _exceptionTypes)
    {
        exceptionTypes = _exceptionTypes;
    }

    /**
     * Sets the URL for retrieving the WMS Capabilties XML.
     * @param _capabilitiesAddress URL for retrieving the WMS Capabilties XML.  DO NOT include any WMS related
     * URL parameters
     */
    public void setCapabilitiesAddress(String _capabilitiesAddress)
    {
        capabilitiesAddress = _capabilitiesAddress;
    }

    /**
     * Set the type of requests that this WMS server can handle.
     * WMS must support at least two request methods: GetMap and GetCapabilties.
     * @param _reqSet a set
     */
    public void setReqSet(WMSRequestSet _reqSet)
    {
        reqSet = _reqSet;
    }

    /**
     * Retrieves the document type - the URL to the document DTD
     * @return the URL to the document DTD
     */
    abstract public String getDocType();

    /**
     * Creates an XML documnet
     * @return document
     */
    protected Document createDocument()
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
            //DocumentType type = impl.createDocumentType("wms", "WMT_MS_Capabilities", getDocType());
//jli
             DocumentType type = impl.createDocumentType("wms", "WMT_MS_Capabilities", "http://schemas.opengis.net/wms/1.1.1/capabilities_1_1_1.dtd");
            return builder.getDOMImplementation().createDocument("", "WMT_MS_Capabilities", type);

        }
        catch (ParserConfigurationException e)
        {
            lastError = "config exception: " + e.toString();
        }

        System.out.println(lastError);
        return null;
    }

    protected void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "WMT_MS_Capabilities";

        this.params.add(new String[]{"version", this.getVersion()});

        //Service -> Titte
        Element serviceElement = doc.createElement("Service");

        //jli: add a name for <Service>
        if(name != null)
	{
	    Element nameElement = doc.createElement("Name");
	    Text txtNode = doc.createTextNode(getName());
	    nameElement.appendChild(txtNode);
	    serviceElement.appendChild(nameElement);
        }

        if(title != null)
        {
            Element titleElement = doc.createElement("Title");
            Text txtNode = doc.createTextNode(getTitle());
            titleElement.appendChild(txtNode);
            serviceElement.appendChild(titleElement);
        }

        //jli: add OnlineResourceURL for <Service>
        if(OnlineResourceURL != null)
        {
            Element OnlineResourceURLElement = doc.createElement("OnlineResource");
            Text txtNode = doc.createTextNode(getOnlineResourceURL());
            OnlineResourceURLElement.appendChild(txtNode);
            serviceElement.appendChild(OnlineResourceURLElement);
        }

        if(info != null)
        {
            serviceElement.appendChild(info.createElement(doc));
        }

        innerElements.add(serviceElement);

        //<Capability>
        Element capElement = doc.createElement("Capability");

        //<Request>
        capElement.appendChild(reqSet.createElement(doc));
        innerElements.add(capElement);

        //<Exception>
        Element exceptionElement = doc.createElement("Exception");
        for(int i = 0; i < this.exceptionTypes.size(); i++)
        {
            String exp = (String)(exceptionTypes.elementAt(i));
            Element formatElement = doc.createElement("Format");
            Text txtNode = doc.createTextNode(exp);
            formatElement.appendChild(txtNode);
            exceptionElement.appendChild(formatElement);
        }

        capElement.appendChild(exceptionElement);

        WMSLayer topLayer = this.getTopLayer();

        NodeList list = doc.getElementsByTagName("Capability");

        capElement.appendChild(topLayer.createElement(doc));
 }

    public String writeCapabilities()
    {
        Document doc = this.createDocument();

        Element el = this.createElement(doc);

        System.out.println("getting root element ready");
        doc.getDocumentElement().appendChild(el.getFirstChild());
        doc.getDocumentElement().appendChild(el.getLastChild());

        doc.getDocumentElement().setAttribute("version", getVersion());

        try
        {

            //http://marc2.theaimsgroup.com/?l=xalan-j-users&m=111815659815111&w=2

            //TransformerFactory xformFactory = TransformerFactory.newInstance();
            /*Transformer transformer = xformFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM, getDocType());
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.STANDALONE, "no");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.VERSION, "1.0");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "ISO-8859-1");*/

            StringWriter writer = new StringWriter();
            org.apache.xml.serialize.OutputFormat format = new org.apache.xml.serialize.OutputFormat("xml", "UTF-8", true);
            org.apache.xml.serialize.XMLSerializer output = new org.apache.xml.serialize.XMLSerializer(writer, format);
            output.setNamespaces(true);
            output.serialize(doc);
            String result = writer.toString();
            writer.close();

            /*Source input = new DOMSource(doc);

            Result output = new StreamResult(writer);
            transformer.transform(input, output); */

            return result;
        }
        catch(FactoryConfigurationError e)
        {
            this.lastError = "Error with Factory configuration.  Error message was: " + e.toString();
        }
        catch(IOException e)
        {
            this.lastError = "Error with serializing XML document!";
        }

        /*catch(TransformerConfigurationException e2)
        {
            this.lastError = "Error with transformer configurtaion.  Error message was: " + e2.toString();
        }
        catch(TransformerException e3)
        {
            this.lastError = "Error with transformer.  Error message was: " + e3.toString();
        }  */
        return null;
    }

    public void setTitle(String _title)
    {
        this.title = _title;
    }

    //jli
    public void setName(String _name)
	    {
	        this.name = _name;
    }

    //jli
    public void setOnlineResourceURL(String _OnlineResourceURL)
            {
                this.OnlineResourceURL = _OnlineResourceURL;
    }

    /**
     * Get the string used for getting the capabilites statement of the given WMS server
     * @return the request string for getting the aapabilities statement.
     */
    public String getCapRequestString()
    {
        String result = "";
        if(capabilitiesAddress.indexOf("?") == -1)
            result = capabilitiesAddress + "?SERVICE=WMS&REQUEST=GetCapabilities";
        else
            result = capabilitiesAddress+ "&SERVICE=WMS&REQUEST=GetCapabilities";

        return result;
    }

    /**
     * Get the DOM document as retrieved containing the WMS GetCapabilites response<br>
     * This method connects to the server and sends a GetCapabilites request to the WMS
     * address and retrieves the WMS response and constructs a DOM document.
     * @return DOM document containing the WMS GetCapabilites response
     */
    protected Document getDoc()
    {
         //setting up document
        /*String jaxpPropertyName = "javax.xml.parsers.DocumentBuilderFactory";
        if (System.getProperty(jaxpPropertyName) == null)
        {
            String apacheXercesPropertyValue = "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";
            System.setProperty(jaxpPropertyName, apacheXercesPropertyValue);
        }
              */
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
                url = new URL(getCapRequestString());

                stream = url.openStream();
                Document doc = builder.parse(stream);

                doc.getDocumentElement().normalize();
                stream.close();

                //got it successfully!
                return doc;
            }
            catch(MalformedURLException e)
            {
                lastError = "Error connecting to server address: " + capabilitiesAddress + " exception message: " + e.toString();
            }
            catch(IOException ex)
            {
                lastError = "Error opening connection to server address:"  + capabilitiesAddress + " exception message: " + ex.toString();
            }
            catch(SAXException saxe)
            {
                lastError = "Error with SAX.  Exception message: " + saxe.toString();
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

        return null;
    }

    /**
     * Get a layer based on a path.
     * @param path this is in the form of "somelayer//some-sub-layer//some-further-sub-layer"  - must be double forward slashes!!!
     * @return null if no such layer exists, or the layer if it does exists :)
     */
    public WMSLayer getLayerFromPath(String path)
    {
        String[] split = path.split("//");
        WMSLayer layer = getLayer(split[0]);

        if(layer == null)
        {
            layer = getLayerByTitle(split[0]);
        }

        if(layer != null)
        {
            if(split.length > 1)
            {
                for(int i = 1; i < split.length; i++)
                {
                    layer = layer.getLayer(split[i]);

                    if(layer == null)
                    {
                        //System.out.println("Canot find layer by name, trying with title");
                        layer = getLayerByTitle(split[i]);
                    }

                    if(layer == null)
                        return null;    //no good!
                }
            }
        }

        return layer;
    }

    /**
     * Get an exception type, generally, this is either in the form of an
     * XML or an image file.  For more information, please see Section 6.5.4
     * of the WMS 1.1.1 Specification.
     * @return A Vector of String containing the "MIME" type of how errors will be reported by this WMS server
     */
    public Vector getExceptionTypes()
    {
        return exceptionTypes;
    }

    /**
     * The GetCapabilities address.  Please note that this address is NOT
     * always the same as the GetMap address.
     * @return URL for connection to the server for GetCapabilites reponse
     */
    public String getCapabilitiesAddress()
    {
        return capabilitiesAddress;
    }

    /**
     * Get the server base address for GetMap requests
     * @return The base server address for GetMap requests
     */
    public String getServerAddress()
    {
        return this.serverAddress;
    }

    /**
     * Get the last error this WMSCapabilites object has encountered, this
     * can be some parsing error, or incorrect address, etc.
     * @return A string containing the last error this object has encountered
     */
    public String getLastError()
    {
        return this.lastError;
    }

    /**
     * Get the top layer
     * @return Get the top most layer.
     */
    public WMSLayer getTopLayer()
    {
        return this.layer;
    }

    /**
     * Get a layer based on a name
     * @param layerName the name of the layer (note that this is not the human-readable title)
     * @return a WMS layer if one is found of the given name, null of none can be found
     */
    public WMSLayer getLayer(String layerName)
    {
        return this.getTopLayer().getLayer(layerName);
    }

    public WMSLayer getLayerByTitle(String titleSearch)
    {
        WMSLayer topLayer = getTopLayer();

        if(topLayer != null)
        {
            if(topLayer.getTitle().compareToIgnoreCase(titleSearch) == 0)
            {
                return topLayer;
            }
            else
            {
                return topLayer.getLayerByTitle(titleSearch);
            }
        }
        return null;
    }

    /**
     * Get the request structure as specified by the GetCapabilities response.  Currently
     * WMS 1.1.1 supports "GetCapabilities", "GetMap" and "GetFeaturInfo".
     * @param reqType the type of request, i.e. "GetCapabilities", "GetMap" and "GetFeaturInfo".
     * @return a WMSRequestType of a specified name
     */
    public WMSRequestType getRequestType(String reqType)
    {
        return reqSet.getRequestType(reqType);
    }

    /**
     * Parses an element
     * @param element
     */
    //abstract public void parse(Element element);

    /**
     * Get the WMS version.
     * @return a String with the version number.
     */
    abstract public String getVersion();

}
