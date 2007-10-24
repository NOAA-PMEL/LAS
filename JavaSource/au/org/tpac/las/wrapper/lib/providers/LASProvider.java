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

package au.org.tpac.las.wrapper.lib.providers;

import au.org.tpac.wms.lib.WMSLayer;
import au.org.tpac.wms.request.WMSRequest;
import au.org.tpac.las.wrapper.lib.data.LasDatasetInfo;
import au.org.tpac.las.wrapper.lib.data.LasGrid;
import au.org.tpac.las.wrapper.lib.data.LasDimension;

import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.MalformedURLException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;

/**
 * This class essential converts a LAS configuration file to a WMS Capabiltlies XML file.
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class LASProvider extends Provider
{
    /**
     * A Hashmap of WMSLayers which are accessible by their names as keys.
     */
    protected HashMap layersMap;

    /**
     * Contains the last known error this instance of LASProvider has encountered
     */
    protected String lastError;

    /**
     * Configuration URL for the las.xml configuraion file
     */
    protected String lasConfigLocation;

    /**
     * The LASConfig object
     */
    protected LASConfig lasConfig;


    /**
     * Constructor for a LASProvider.
     * @param _lasConfigLocation
     */
    public LASProvider(String _lasConfigLocation)
    {
        lasConfigLocation = _lasConfigLocation;
        layersMap = new HashMap();
        lastError = "";

        lasConfig = new LASConfig();

        try
        {
            File xmlFile = new File(lasConfigLocation);

            lasConfig = new LASConfig();
            JDOMUtils.XML2JDOM(xmlFile, lasConfig);
            lasConfig.convertToSeven();

            lasConfig.mergeProperites();

            //add grid type for variables
            try {
                lasConfig.addGridType();
            } catch (Exception e) {
                System.out.println("Could not add the grid_type to variables in this LAS configuration.");
            }

            //System.out.println("grid type sst of coads======"+lasConfig.getGridType("coads_climatology_cdf", "sst"));
            ByteArrayInputStream stringStream = new ByteArrayInputStream(lasConfig.toString().getBytes());

            genMapping(stringStream);
        }
        catch(IOException ioe)
        {
            lastError = "Error encountered when getting las.xml file...: " + ioe.toString();
            System.out.println(lastError);
        }
        catch(Exception e)
        {
            lastError = "Generic exception encountered when accessing las.xml file...:" + e.toString();
            System.out.println(lastError);
            e.printStackTrace(System.out);
        }
    }

    /**
     * Another constructor for LASProvider, but with an already constructed LASConfig object.
     * @param _lasConfig las configuration object to convert.
     */
    public LASProvider(LASConfig _lasConfig)
    {
        lasConfig = _lasConfig;
        ByteArrayInputStream stringStream = new ByteArrayInputStream(lasConfig.toString().getBytes());
        genMapping(stringStream);
    }

    /**
     * Retrieves the las configuration object
     * @return
     */
    public LASConfig getLasConfig()
    {
        return lasConfig;
    }

    /**
     * Retrieves grid type of a variable
     * @param dsID dataset ID
     * @param varID variable ID
     * @return the grid type, either regular or scattered
     */
    public String getGridType(String dsID, String varID){   
        String gridType = null;
        try{
            gridType = lasConfig.getGridType(dsID, varID);
        } catch(Exception e) {
            e.printStackTrace(System.out);
        }
        return gridType;
    }

    /**
     * generates a representation of the las configuration document
     * @param stream
     */
    private void genMapping(InputStream stream)
    {
        Document doc = getDoc(stream);

        if(doc != null)
        {
            doParse(doc);
        }
        else
            System.out.println(lastError);
    }

    /**
     * Building an XML document based on a stream
     * @param stream stream to build doc from
     * @return an XML Document
     */
    public Document getDoc(InputStream stream)
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

        if((builder != null) && lastError.equalsIgnoreCase(""))
        {
            try
            {
                Document doc = builder.parse(stream);
                doc.getDocumentElement().normalize();

                //got it successfully!
                return doc;
            }
            catch(MalformedURLException mla)
            {
                mla.printStackTrace(System.out);
            }
            catch(SAXException saxe)
            {
                lastError = "Error with SAX.  Exception message: " + saxe.toString();
            }
            catch(IOException e)
            {
                System.out.println(e.toString()); 
            }
        }

        lastError = "Builder is null or compounded error: previous error message: " + lastError;

        return null;
    }

    /**
     * 
     * @return
     */
    public HashMap getLayersMap()
    {
        return this.layersMap;
    }

    protected void doParse(Document doc)
    {
        HashMap grids = new HashMap();


        Element root = doc.getDocumentElement();

        NodeList list = root.getElementsByTagName("datasets");

        //parsing the layers
        for(int i = 0; i < list.getLength(); i++)
        {
            //parse all children of <datasets>, which may contain multiple datasets
            Node dssNode = list.item(i);
            NodeList children = dssNode.getChildNodes();
            for(int j=0;j<children.getLength();j++){
                LasDatasetInfo inf = new LasDatasetInfo(children.item(j));
                String dsID = inf.getDatasetId();
                if(dsID != null){
                    layersMap.put(dsID, inf);
                }
            }
            //LasDatasetInfo info = new LasDatasetInfo(list.item(i));
            //layersMap.put(info.getDatasetId(), info);
        }

        //parsing the "grids" - a grid contains contraints of each axes
        NodeList gridList = root.getElementsByTagName("grids");
        int count = 0;

        for(int i = 0; i < list.getLength(); i++)
        {
            NodeList children = gridList.item(i).getChildNodes();
            LasGrid newGrid = null;

            for(int j = 0; j < children.getLength(); j++)
            {
                Node child = children.item(j);

                if(child.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element el = (Element)(child);

                    if(newGrid == null)
                    {
                        newGrid = new LasGrid();
                    }
                    else if(newGrid.getName() != null)
                    {
                        newGrid = new LasGrid();
                    }
                    newGrid.setName(el.getNodeName());
                }
                else
                {
                    if(newGrid == null)
                    {
                        newGrid = new LasGrid();
                    }
                }

                newGrid.parse(child);
                grids.put(newGrid.getName(), newGrid);
            }
        }

        NodeList axesList = root.getElementsByTagName("axes");

        //parsing axes to add to grids
        for(int i = 0; i < axesList.getLength(); i++)
        {
            NodeList axis_elements = axesList.item(i).getChildNodes();

            for(int j = 0; j < axis_elements.getLength(); j++)
            {
                Node axis_node = axis_elements.item(j);
                String nodeName = axis_node.getNodeName().trim();

                if(!(nodeName.equals("#text")))
                {
                    LasDimension axes2 = new LasDimension(axis_node);
                    Iterator grids_it = grids.keySet().iterator();
                    while(grids_it.hasNext())
                    {
                        LasGrid myGrid = (LasGrid)(grids.get(grids_it.next()));
                        myGrid.addDimension(axes2.getName(), axes2);
                    }
                }
            }
        }

        Iterator grids_it = grids.keySet().iterator();
        while(grids_it.hasNext())
        {
            LasGrid myGrid = (LasGrid)(grids.get(grids_it.next()));
            HashMap map = myGrid.getDimensions();

            Set s = map.keySet();
            Iterator it = s.iterator();

            while(it.hasNext())
            {
                String key = (String)(it.next());
                LasDimension dim = (LasDimension)(map.get(key));
            }
        }

        Set set = layersMap.keySet();
        Iterator it = set.iterator();

        //adding grids to datasetinfo
        while(it.hasNext())
        {
            String key = (String)(it.next());
            LasDatasetInfo info = (LasDatasetInfo)(layersMap.get(key));

            Iterator grids_iterator = grids.keySet().iterator();

            while(grids_iterator.hasNext())
            {
                LasGrid grid = (LasGrid)(grids.get(grids_iterator.next()));
                info.addGrid(grid);
            }
        }
    }


    public WMSLayer[] getLayerInfo(String[] layerNames)
    {
        return null;
    }

    public String[] getTopLevelLayerNames()
    {
        return null;
    }

    public Image getImage(WMSRequest request)
    {
        return null;
    }
}
