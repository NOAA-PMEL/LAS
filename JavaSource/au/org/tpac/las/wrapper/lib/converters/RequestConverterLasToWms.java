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

package au.org.tpac.las.wrapper.lib.converters;

import au.org.tpac.wms.request.WMSRequest;
import au.org.tpac.wms.lib.WMSCapabilities;
import au.org.tpac.wms.lib.WMSLayer;
import au.org.tpac.wms.lib.WMSLayerExtent;
import au.org.tpac.wms.lib.WMSStyle;
import au.org.tpac.las.wrapper.lib.providers.LASProvider;
import au.org.tpac.las.wrapper.lib.data.LasDatasetInfo;
import au.org.tpac.las.wrapper.lib.data.LasVariable;
import au.org.tpac.las.wrapper.lib.data.LasGrid;
import au.org.tpac.las.wrapper.lib.data.LasDimension;

import java.util.*;
import java.net.URLEncoder;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class converts WMSRequest to a LAS ProductServer request.
 * @author Pauline Mak <pauline@insight4.com>
 */
public class RequestConverterLasToWms
{
    private static Logger log = LoggerFactory.getLogger(RequestConverterLasToWms.class);

    /**
     * A WMSRequest object that's about to be converted
     */
    protected WMSRequest wmsReq;

    /**
     * URL to the ProductServer.do
     */
    protected String productServerURL;

    /**
     * A WMSCapabilties XML - note that this is a converted WMSCapabilities object
     * based on an existing las.xml configuration file
     */
    protected WMSCapabilities wmsCap;

    /**
     *  Converted for a LAS configurationn file to a WMSCapabilities XML
     */
    protected LASProvider provider;

    /**
     * If this option is used, the XML text will be returned.
     * In particular, the location of the resulting image or an XML of the
     * error will be returned.
     */
    public static String OP_PLOT_WMS = "Plot_2D_WMS";

    /**
     * If this option is used, then a straight plot (i.e. streams the resulting image)
     * will be returned at the end of the request.
     */
    public static String OP_PLOT_2D_WITHOUT_HTML = "Plot_2D_Without_HTML";

    /**
     * The LAS operation to use.  By default, this will be OP_PLOT_WMS - which
     * will return an XML as a response to the request.
     */
    protected String opName;

    /**
     * The fill levels for this current request.
     */
    protected String levels;


    /**
     * Ths an empty constructor for a RequestConverterLasToWms
     */
    public RequestConverterLasToWms()
    {
        opName = OP_PLOT_WMS;
        levels = null;
    }

    /**
     * This is a constructor based on a WMSRequest object
     * @param _wmsReq WMS object to convert
     */
    public RequestConverterLasToWms( WMSRequest _wmsReq)
    {
        wmsReq = _wmsReq;
    }


    /**
     * This constructor will take in the following arguments and returns a converted  object.
     * After the constructor, call convert() to retrieve the LAS request XML.
     * @param _lasConfigURL URL to las.xml configuration file.
     * @param _wmsReq the WMSRequest to convert - note that this only needs to be the WMS request string
     * @param _wmsCapURL URL to the WMSCapabilities XML.  Note that you DO NOTE need to include any WMS related REST parameters.
     * @param _opName Operation name, either OP_PLOT_WMS or Plot_2D_Without_HTML.
     * @param _productServerURL URL to ProductServer.do
     */
    public RequestConverterLasToWms(String _lasConfigURL, String _wmsReq, String _wmsCapURL, String _opName, String _productServerURL)
    {
        levels = null;
        productServerURL = _productServerURL;
        provider = new LASProvider(_lasConfigURL);

        //!!! CHECK WMS REQUEST VERSION
        ConverterLasToWms converter = new ConverterLasToWms("1.1.1", provider, _wmsCapURL, productServerURL.substring(0, productServerURL.lastIndexOf("/")));

        wmsCap = converter.getConverted();
        this.opName = _opName;

        //jli: should us different operation for insitu datasets
        //this.opName = "Insitu_extract_location_plot";
        wmsReq = new WMSRequest(_wmsReq, wmsCap);
    }

    /**
     * Retrieves the WMS request to convert.  Note that this may not have been initialised (if the empty
     * constructor has been used to create the object).
     * @return the WMS request to be converted.
     */
    public WMSRequest getWmsReq()
    {
        return wmsReq;
    }


    /**
     * Sets the fill/contour levels
     * @param _levels
     */
    public void setLevels(String _levels)
    {
        levels = _levels;
    }


    /**
     * Get the WMSCapabilities object that's been used to construct the LAS request object
     * @return a WMSCapabilities. This may be uninitialised if the empty constructor has been called.
     */
    public WMSCapabilities getWmsCap()
    {
        return wmsCap;
    }

    /**
     * Construct the first part of the LAS request XML.
     * All Ferret settings are constructed here, including fill levels, etc.
     * @return a String with most of the Ferret arguments for a LAS request XML.
     */
    protected String getHeader()
    {
        String header = "";
        header += "<?xml+version=\"1.0\"?>";
        header += "<lasRequest+package=\"\"+href=\"file:las.xml\">";
        header += "<link+match=\"/lasdata/operations/operation[@ID='"+ opName + "']\"+/>";
        header += "<properties>";
        header += "<ferret>";
        header += "<view>xy</view>";
        header += "<format>shade</format>";

        TreeSet set = wmsReq.getLayers();
        WMSLayer firstLayer  = (WMSLayer)(set.first());
        //String reqStyleName = (String)(wmsReq.getStyles().get(firstLayer.getLayerName()));
        //WMSStyle reqStyle = firstLayer.getStyleByName(reqStyleName);

        //use default style if missing
        if(wmsReq.getStyles() == null){
	    header += "<contour_style>default</contour_style>";
        }else{
            String reqStyleName = (String)(wmsReq.getStyles().get(firstLayer.getLayerName()));
	    WMSStyle reqStyle = firstLayer.getStyleByName(reqStyleName);

            if(reqStyle.getName().equalsIgnoreCase("ferret_contour"))
            {
                header += "<contour_style>color_filled_plus_lines</contour_style>";
            }
            else
            {
                header += "<contour_style>default</contour_style>";    
            }
        }

        if(levels != null)
        {
            header += "<contour_levels>" + levels + "</contour_levels>";
            header += "<fill_levels>" + levels + "</fill_levels>";
        }
        else
        {
            header += "<contour_levels+/>";
            header += "<fill_levels+/>";
        }

        header += "<expression+/>";
        header += "<image_format>default</image_format>";
        header += "<interpolate_data>false</interpolate_data>";
        header += "<mark_grid>default</mark_grid>";
        header += "<palette>default</palette>";
        header += "<set_aspect>default</set_aspect>";
        header += "<size>0.5</size>";  //!!This will be irrelevant!
        header += "<imgHeight>" + wmsReq.getImageHeight() + "</imgHeight>";
        header += "<imgWidth>" + wmsReq.getImageWidth() + "</imgWidth>";
        //if we can somehow fit size here...

        //STYLES SHOULD BE APPLIED HERE :)
        header += "<use_graticules>default</use_graticules>";
        header += "<use_ref_map>default</use_ref_map>";

        header += "</ferret>";
        header += "</properties>";
        header += "<args>";
        return header;
    }


    /**
     * Convert the WMS Request into a LAS request URL (that includes
     * an XML parameter for ProductServer.do.)
     * Note that the XML part of the URL is HTTP encoded.
     * @return a LAS request XML that points to the product server.
     */
    public String convert()
    {
        log.info("enter convert");

        TreeSet set = wmsReq.getLayers();
        WMSLayer firstLayer  = (WMSLayer)(set.first());
        String result = getHeader();

        //System.out.println("first layer name "+ firstLayer.getLayerName());
        String layName[] = firstLayer.getLayerName().split(":");
        String dstID = layName[0];
        String varID = layName[1];

        //result += "<link+match=\"/lasdata/datasets/" + firstLayer.getParent().getLayerName() + "/variables/" + firstLayer.getLayerName() + "\"+/>";

        result += "<link+match=\"/lasdata/datasets/" + dstID + "/variables/" + varID + "\"+/>";

        result += "<region>";
        result += "<range+low=\"" + wmsReq.getMinX()  + "\"+type=\"x\"+high=\"" + wmsReq.getMaxX() +"\"+/>";
        result += "<range+low=\"" + wmsReq.getMinY()  + "\"+type=\"y\"+high=\"" + wmsReq.getMaxY() +"\"+/>";

        //LasDatasetInfo datasetInfo = (LasDatasetInfo)(provider.getLayersMap().get(firstLayer.getParent().getLayerName()));
        System.out.println("dstID " + dstID);
        System.out.println("varID " + varID);

        LasDatasetInfo datasetInfo = (LasDatasetInfo)(provider.getLayersMap().get(dstID));
        System.out.println("dstID " + dstID);

        //LasVariable var = datasetInfo.getVariable(firstLayer.getLayerName());
        LasVariable var = datasetInfo.getVariable(varID);
        LasGrid grid = var.getGrid();
        HashMap map = grid.getDimensions();
        Hashtable extents = wmsReq.getExtents();

        Hashtable wmsExtents = firstLayer.getExtents();

/*
        if(extents.isEmpty()){
            //find all extents for this variable
            extents = firstLayer.getExtents();
            for(Enumeration exEnum = extents.keys(); exEnum.hasMoreElements();)
            {
                String key = exEnum.nextElement().toString();
                WMSLayerExtent layerExtent = (WMSLayerExtent)extents.get(key);
                Iterator it = map.keySet().iterator();

                while(it.hasNext())
                {
                    String mapKey = (String)(it.next());
                    LasDimension dim = (LasDimension)(grid.getDimensions().get(mapKey));

                    //System.out.println("mapKey is " + mapKey);
                    //System.out.println("key is " + key);
                    //System.out.println("dim type is " + dim.getType());

                    //for z dimension or time specified as a list of <v>
                    if(mapKey.equalsIgnoreCase(key))
                    {
                        result += "<point type=\"" + dim.getType() + "\" v=\""+ layerExtent.getDefaultValue() + "\"/>";
                    }

                    //for time dimension specified as <arange> in data config XML file
                    if((dim.getType()).equalsIgnoreCase("t") && (key.equalsIgnoreCase("time")))
                    {
                         result += "<point type=\"" + "t" + "\" v=\""+ layerExtent.getDefaultValue() + "\"/>";
                    }
                }        
            }

        }else if( (!extents.isEmpty()) && (extents.size() != wmsExtents.size()) ){
*/
        //some datasets don't have <extent>, such as topography data
        if(extents !=null & wmsExtents!=null){
         //use default values when dimension parameters are not specified in GetMap request
         if( extents.size() != wmsExtents.size() ){
            //check each wmsExtent to see if it is set; if not, use default value
            for(Enumeration wmsExtentEnum = wmsExtents.keys(); wmsExtentEnum.hasMoreElements();)
            {
                String wmsKey = wmsExtentEnum.nextElement().toString();
                WMSLayerExtent layerExtent = (WMSLayerExtent)wmsExtents.get(wmsKey);

                //check if it is set by the GetMap request
                boolean isSet = false;
                for(Enumeration exEnum = extents.keys(); exEnum.hasMoreElements();){
                    String key = exEnum.nextElement().toString();
                    if(wmsKey.equalsIgnoreCase(key)){isSet=true;}
                }
                
                //the extent identified by wmsKey if not set; use default value
                if(!isSet){
                    Iterator it = map.keySet().iterator();

                    while(it.hasNext())
                    {
                        String mapKey = (String)(it.next());
                        LasDimension dim = (LasDimension)(grid.getDimensions().get(mapKey));

                        //for z dimension or time specified as a list of <v>
                        if(mapKey.equalsIgnoreCase(wmsKey))
                        {
                            result += "<point type=\"" + dim.getType() + "\" v=\""+ layerExtent.getDefaultValue() + "\"/>";
                        }

                        //for time dimension specified as <arange> in data config XML file
                        if((dim.getType()).equalsIgnoreCase("t") && (wmsKey.equalsIgnoreCase("time")))
                        {
                            result += "<point type=\"" + "t" + "\" v=\""+ layerExtent.getDefaultValue() + "\"/>";
                        }
                    }
                }else{
                //the extent identified by wmsKey if set
                    String dimType = "";
                    if(wmsKey.equalsIgnoreCase("time")){
                        dimType = "t";
                    }else if( wmsKey.equalsIgnoreCase("elevation")){
                        dimType = "z";
                    }

                    Iterator it = map.keySet().iterator();

                    while(it.hasNext())
                    {
                        String mapKey = (String)(it.next());
                        LasDimension dim = (LasDimension)(grid.getDimensions().get(mapKey));

                        if(dim.getType().equalsIgnoreCase(dimType))
                        {
                            Object value = extents.get(wmsKey);
                            System.out.println(value.getClass());
                            if(dimType.equalsIgnoreCase("t"))
                            {
                                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                DateFormat toFormat = new SimpleDateFormat("dd-MMM-yyyy");
                                try
                                {
                                    Date toDate = format.parse((String)value);
                                    value = toFormat.format(toDate);
                                }
                                catch(ParseException pe)
                                {
                                    System.out.println("cannot parse date: " + value);
                                }
                            }
                            result += "<point type=\"" + dimType + "\" v=\""+ value + "\"/>";
                        }
                        else if(mapKey.equalsIgnoreCase(wmsKey))
                        {
                            result += "<point type=\"" + dim.getType() + "\" v=\""+ extents.get(wmsKey) + "\"/>";
                        }
                    }
                }
            } 
          }else{
          //extents are all set by the GetMap request
            for(Enumeration exEnum = extents.keys(); exEnum.hasMoreElements();)
            {
                String key = exEnum.nextElement().toString();
                String dimType = "";
                if(key.equalsIgnoreCase("time"))
                    dimType = "t";
                else if( key.equalsIgnoreCase("elevation"))
                    dimType = "z";

                Iterator it = map.keySet().iterator();
                while(it.hasNext())
                {
                    String mapKey = (String)(it.next());
                    LasDimension dim = (LasDimension)(grid.getDimensions().get(mapKey));

                    if(dim.getType().equalsIgnoreCase(dimType))
                    {
                        Object value = extents.get(key);
                        System.out.println(value.getClass());
                        if(dimType.equalsIgnoreCase("t"))
                        {
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            DateFormat toFormat = new SimpleDateFormat("dd-MMM-yyyy");
                            try
                            {
                                Date toDate = format.parse((String)value);
                                value = toFormat.format(toDate);
                            }
                            catch(ParseException pe)
                            {
                                System.out.println("cannot parse date: " + value);
                            }
                        }
                        result += "<point type=\"" + dimType + "\" v=\""+ value + "\"/>";
                    }
                    else if(mapKey.equalsIgnoreCase(key))
                    {
                        result += "<point type=\"" + dim.getType() + "\" v=\""+ extents.get(key) + "\"/>";
                    }
                }
            }
          }
        }

        result += "</region>";
        result += "</args>";
        result += "</lasRequest>";

        try
        {
            return productServerURL + "?xml=" + URLEncoder.encode(result, "UTF-8");//making it stream the image;
        }
        catch(UnsupportedEncodingException uee)
        {
            System.out.println("canot support encoding: " + uee.toString());
            return null;
        }
    }
}
