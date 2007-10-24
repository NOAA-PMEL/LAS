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

import au.org.tpac.las.wrapper.lib.providers.LASProvider;
import au.org.tpac.las.wrapper.lib.data.*;
import au.org.tpac.wms.request.WMSCapFactory;
import au.org.tpac.wms.lib.*;

import java.util.*;
import java.text.SimpleDateFormat;

import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import org.jdom.Element;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * This class converts a LAS XML to WMS Capabilties XML.
 * @author Pauline Mak (pauline@insight4.com)
 */
public class ConverterLasToWms
{
    protected String wmsVersion;    //the WMS version - currently only tested on 1.1.1 for generating WMS XML
    protected WMSCapabilities converted; //the converted WMSCapabilities object.
    protected LASProvider lasProvider; //Provider for "understanding" the las.xml document - perhaps I should have use LASConfig instead...
    protected String wmsURL; // URL to the WMS servlet
    protected String lasURL; // URL to las (i.e. http://some.server/las/) Note that th

    /**
     * Sole constructor for the converter and also performs a conversion before the end of construction.
     * @param _wmsVersion wms version should be a string, either "1.1.1" or "1.3.0".  Currently only tested on 1.1.1
     * @param _lasProvider Provider for "understanding" the las.xml document
     * @param _wmsURL the URL to the WMS servlet
     * @param _lasURL the URL to the las server  (i.e. http://some.server/las/).  Note - this doesn't include ProductServer.do or datasets/)
     */
    public ConverterLasToWms(String _wmsVersion, LASProvider _lasProvider, String _wmsURL, String _lasURL)
    {
        wmsVersion = _wmsVersion;
        WMSCapFactory factory = new WMSCapFactory();
        converted = factory.createEmptyCap(_wmsVersion);
        wmsURL = _wmsURL;
        lasURL = _lasURL;

        if(converted != null)
        {
            //yeah!
        }

        lasProvider = _lasProvider;
        convert();
    }


    /**
     * Get the converted WMSCapabilites.  Conversion occurs in the constructor
     * @return a WMSCapabilities object
     */
    public WMSCapabilities getConverted()
    {
        return converted;
    }

    /**
     * Initialises the WMSCapabilities object with a few default values
     */
    protected void initWMSCap()
    {
        WMSRequestSet reqSet = new WMSRequestSet();

        //<Request><GetMap>
        WMSRequestType reqType1 = new WMSRequestType();
        reqType1.setTypeName("GetMap");
        Vector types1 = new Vector();
        types1.add("image/png");
        reqType1.setSupportedFormat(types1);
        WMSOnlineResource resource1 = new WMSOnlineResource();
        resource1.setProtocol("HTTP");
        resource1.setURL(wmsURL);
        reqType1.setResource(resource1);
        reqSet.addRequestTable(reqType1);

        //<Request><GetCapabilities>
        WMSRequestType reqType2 = new WMSRequestType();
        reqType2.setTypeName("GetCapabilities");
        Vector types2 = new Vector();
        types2.add("application/vnd.ogc.wms_xml");
        reqType2.setSupportedFormat(types2);
        WMSOnlineResource resource2 = new WMSOnlineResource();
        resource2.setProtocol("HTTP");
        resource2.setURL(wmsURL);
        reqType2.setResource(resource2);
        reqSet.addRequestTable(reqType2);

        converted.setReqSet(reqSet);


        Vector exceptionTypes = new Vector();
        exceptionTypes.add("application/vnd.ogc.se_xml");
        converted.setExceptionTypes(exceptionTypes);

        converted.setTitle("LAS-WMS Map Server");
        converted.setName("OGC:WMS");
        //this URL is for test only; it should be configurable
        converted.setOnlineResourceURL("http://ferret.pmel.noaa.gov/Ferret/LAS/");
    }


    /**
     * The conversion begins here
     */
    protected void convert()
    {
        initWMSCap();

        //this should be top most layers.
        HashMap layersMap = lasProvider.getLayersMap();

        converted.setServerAddress(wmsURL);

        WMSLayer topLayer = new WMSLayer();

        topLayer.setTitle("Converted LAS to WMS Layers");

        //should not set name for a layer that is not a map but only a category
        //topLayer.setName("LAS_WMSLayers");

        topLayer.addSupportedSRS("EPSG:4326");  //most commonly used standard for lat/lon specification.
        converted.setTopLayer(topLayer);

        Set keys = layersMap.keySet();
        Iterator keysIt = keys.iterator();

        while(keysIt.hasNext())
        {
            String key = (String)(keysIt.next());
            LasDatasetInfo info = (LasDatasetInfo)(layersMap.get(key));
            System.out.println("dataset ID == " + info.getDatasetId());
            addDataset(topLayer, info);
        }
    }

    /**
     * Adds a dataset from LAS into a set of "layers" in WMS.  Datasets are considered as top layers, with their variables as sublayers
     * @param parentLayer the parent of this current layer.
     * @param info
     */
    protected void addDataset(WMSLayer parentLayer, LasDatasetInfo info)
    {
        WMSLayer subLayer = new WMSLayer(parentLayer);

        parentLayer.addSubLayer(subLayer);

        //only a variable layer has a name
        //subLayer.setName(info.getDatasetId());

        subLayer.setTitle(info.getTitle());

        HashMap varMap = info.getVariables();

        Iterator keyIt = varMap.keySet().iterator();

        while(keyIt.hasNext())
        {
            String key = (String)(keyIt.next());
            LasVariable var = (LasVariable)(varMap.get(key));
            //addVariable(subLayer, var);

            String dsID = info.getDatasetId();
            String varID= var.getVarId();
            //only list variable with regular grid type; not list insitu data 
            if(lasProvider.getGridType(dsID,varID).equalsIgnoreCase("regular")){
                addVariable(subLayer, var, info);
            }
        }
    }

    /**
     * Converts a calendar to a date string format
     * @param cal with a date to convert to string
     * @return a string in the form of "yyyy-MM-dd" will be returned if successful
     * @throws IllegalArgumentException
     */
    protected String convertCalendarToDateStrings(Object cal) throws IllegalArgumentException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(cal);
    }

    /**
     * Adds a style to a WMS layer.
     * This currently supports a "ferret_default" layer (i.e. standard fill levels) and "ferret_contour" with contouring lines
     * @param layer the layer to add the styles into.
     */
    protected void addStyles(WMSLayer layer, float xMin, float xMax, float yMin, float yMax, String zMin, String tMin )
    {

        WMSLegend legend = new WMSLegend();
        legend.setFormat("image/png");
        WMSOnlineResource onlineRes = new WMSOnlineResource();
        onlineRes.setProtocol("http://www.w3.org/1999/xlink");
        //onlineRes.setURL(lasURL + "/legendConfig/" + layer.getParent().getLayerName() + "_" + layer.getLayerName() + "_legend.png");
        onlineRes.setURL(lasURL + "/ProductServer.do?xml=" + createLASRequest(layer,xMin,xMax,yMin,yMax,zMin,tMin)+"&stream=true&stream_ID=colorbar");
        legend.setResource(onlineRes);

        //now add style option (contour lines)
        WMSStyle contourDefaultStyle = new WMSStyle();
        contourDefaultStyle.setTitle("Default");
        contourDefaultStyle.setName("ferret_default");
        contourDefaultStyle.setParentLayer(layer);
        contourDefaultStyle.setLegend(legend);


        WMSStyle contourStyle = new WMSStyle();
        contourStyle.setTitle("Show Contour");
        contourStyle.setName("ferret_contour");
        contourStyle.setParentLayer(layer);
        contourStyle.setLegend(legend);

        layer.addStyle(contourDefaultStyle);
        layer.addStyle(contourStyle);
    }

     /**
     * Creates a LAS stresmable request for color key for a WMS layer
     * @param layer the layer to create a las request
     */
    protected String createLASRequest(WMSLayer layer,float xMin, float xMax, float yMin, float yMax, String zMin, String tMin){
        LASUIRequest lasUIReq = new LASUIRequest();
        String lasUIReqXML = "";

        lasUIReq.getRootElement().setAttribute("package","");
        lasUIReq.getRootElement().setAttribute("href","file:las.xml");
        //set operation
        lasUIReq.setOperation("Plot_2D_ColorKey");

        //add properties
        Element properties = new Element("properties");
        lasUIReq.getRootElement().addContent(properties);
        lasUIReq.setProperty("ferret", "view","xy");
        lasUIReq.setProperty("ferret", "format","shade");
/*
        lasUIReq.setProperty("ferret", "size",".5");
        lasUIReq.setProperty("ferret", "contour_style","default");
        lasUIReq.setProperty("ferret", "fill_levels","default");
        lasUIReq.setProperty("ferret", "image_format","default");
        lasUIReq.setProperty("ferret", "set_aspect","default");
        lasUIReq.setProperty("ferret", "use_graticules","default");
        lasUIReq.setProperty("ferret", "use_ref_map","default");
*/
        //add <args>
        Element args = new Element("args");

        //String dsID = layer.getParent().getLayerName();
        //layer name is changed to dsID:varID
        //String varID= layer.getLayerName();
        String layName[] = layer.getLayerName().split(":");
        String dsID = layName[0];
        String varID = layName[1];

        Element link = new Element("link");
        link.setAttribute("match", "/lasdata/datasets/dataset[@ID='"+dsID+"']/variables/variable[@ID='"+varID+"']");
        args.addContent(link);

        Element region = new Element("region");

        Element xRange = new Element("range");
        xRange.setAttribute("low", Float.toString(xMin));
        xRange.setAttribute("high",Float.toString(xMax));
        xRange.setAttribute("type", "x");
        region.addContent(xRange);

        Element yRange = new Element("range");
        yRange.setAttribute("type", "y");
        yRange.setAttribute("low", Float.toString(yMin));
        yRange.setAttribute("high",Float.toString(yMax));
        region.addContent(yRange);

        if(!zMin.equals("")){
            Element zPoint = new Element("point");
            zPoint.setAttribute("type", "z");
            zPoint.setAttribute("v",zMin);
            region.addContent(zPoint);
        }
        if(!tMin.equals("")){
            Element tPoint = new Element("point");
            tPoint.setAttribute("type", "t");
            tPoint.setAttribute("v",tMin);
            region.addContent(tPoint);
        }

        args.addContent(region);

        lasUIReq.getRootElement().addContent(args);
        //<args><link>
        //String dsID = layer.getParent().getLayerName();
        //String varID= layer.getLayerName();
        //lasUIReq.addVariable(dsID, varID);

        try{
            lasUIReqXML=lasUIReq.toEncodedURLString();
        } catch (Exception e){

        }
        return lasUIReqXML;
    }

    /**
     * Adds a varaible a WMS layer
     * @param parentLayer parent (usually the LAS dataset)
     * @param var variable (or layer in WMS) to add/convert.
     */
    protected void addVariable(WMSLayer parentLayer, LasVariable var, LasDatasetInfo info)
    {
        WMSLayer varLayer = new WMSLayer(parentLayer);
        parentLayer.addSubLayer(varLayer);
        varLayer.setName(info.getDatasetId()+":"+var.getVarId());
        varLayer.setTitle(info.getTitle()+": "+var.getName());

//        addStyles(varLayer);

        LasGrid varGrid = var.getGrid();

        HashMap dimMap = varGrid.getDimensions();

        Iterator keyIt = dimMap.keySet().iterator();

        float xMin = Float.MIN_VALUE;
        float xMax = Float.MIN_VALUE;
        float yMin = Float.MIN_VALUE;
        float yMax = Float.MIN_VALUE;

        String tMin = "";
        String zMin = "";

        while(keyIt.hasNext())
        {
            String key = (String)(keyIt.next());
            LasDimension dim = (LasDimension)(dimMap.get(key));

            String type = dim.getType();

            if(type.equalsIgnoreCase("x"))
            {
               xMin = Float.parseFloat(dim.getMin().toString());
               xMax = Float.parseFloat(dim.getMax().toString());
            }
            else if(type.equalsIgnoreCase("y"))
            {
                yMin = Float.parseFloat(dim.getMin().toString());
                yMax = Float.parseFloat(dim.getMax().toString());
            }
            else if(type.equalsIgnoreCase("t"))
            {
                WMSDimension wmsDim = new WMSDimension();
                wmsDim.setUnitSymbol(dim.getUnits());
                WMSLayerExtent extent = new WMSLayerExtent();
                TimeDimension timeRange = (TimeDimension)(dim.getRangeInfo());

                if(timeRange.hasMultipleValues())
                {
                    Object end = timeRange.getEnd();
                    tMin =  timeRange.getStart().toString();

                    wmsDim.setName(dim.getName());
                    //add in manually
                    timeRange.gotoStart();
                    Object nextItem = timeRange.getNext();

                    while(nextItem != null)
                    {
                        //extent.addPossibleValue(convertCalendarToDateStrings(timeRange.getNext()));
                        String next = nextItem.toString();
                        extent.addPossibleValue(next);
                        nextItem = timeRange.getNext();
                    }

                    wmsDim.setName(dim.getName());
                    extent.setExtentName(dim.getName());
                    varLayer.addExtents(extent.getExtentName(), extent);
                    converted.getTopLayer().addDimension(wmsDim);
                }
                else
                {
                    wmsDim.setName("time"); //time is a special one in WMS....
                    wmsDim.setUnit("ISO8601");

                    extent.setExtentName("time");
                    extent.addMinValue(convertCalendarToDateStrings(timeRange.getStart()));
                    extent.addMaxValue(convertCalendarToDateStrings(timeRange.getEnd()));

                    DateTimeFormatter ferretfmt = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss").withZone(DateTimeZone.UTC);
                    DateTime dt_min = new DateTime(timeRange.getStart());
                    tMin = dt_min.toString(ferretfmt);
                    //tMin = timeRange.getStart().toString();

                    String result = "P";

                    switch(timeRange.getUnit())
                    {
                        case Calendar.YEAR:
                            result += timeRange.getStep() + "Y";
                            break;
                        case Calendar.MINUTE:
                            result += "T" + timeRange.getStep() + "M";
                            break;
                        case Calendar.SECOND:
                            result += "T" + timeRange.getStep() + "S";
                            break;
                        case Calendar.HOUR_OF_DAY:
                            result += "T" + timeRange.getStep()+ "H";
                            break;
                        case Calendar.MONTH:
                            result += timeRange.getStep() + "M";
                            break;
                        case Calendar.DAY_OF_YEAR:
                            result += timeRange.getStep() + "D";
                            break;
                        default:
                            break;
                    }

                    wmsDim.setName("time");
                    extent.addResolution(result);
                    varLayer.addExtents("time", extent);
                    converted.getTopLayer().addDimension(wmsDim);
                }
                //set default value for <Extent>
                if(tMin != null){extent.setDefaultValue(tMin);}

            }
            else
            {
                WMSDimension wmsDim = new WMSDimension();

                //adding this dimension to the top most layer (then the actual value woudl go into the
                //extent element.
                wmsDim.setName(dim.getName());
                wmsDim.setUnit(dim.getUnits());
                wmsDim.setUnitSymbol(dim.getUnits());
                converted.getTopLayer().addDimension(wmsDim);

                WMSLayerExtent extent = new WMSLayerExtent();
                extent.setExtentName(dim.getName());

                LasArange aRange = (LasArange)(dim.getRangeInfo());
                extent.addResolution(aRange.getStep() + "");


                if(aRange.hasMultipleValues())
                {
                    zMin = aRange.getStart().toString();

                    Object end = aRange.getEnd();
                    //add in manually
                    aRange.gotoStart();
                    Object nextItem = aRange.getNext();

                    while(nextItem != null)
                    {
                        extent.addPossibleValue(nextItem.toString());
                        nextItem = aRange.getNext();
                    }
                }
                else
                {
                    extent.addMinValue(aRange.getDoubleStart() + "");
                    extent.addMaxValue(aRange.getDoubleEnd() + "");

                    zMin = Double.toString(aRange.getDoubleStart());
                }
                //set default value for <Extent>
                if(zMin != null){extent.setDefaultValue(zMin);}

                varLayer.addExtents(extent.getExtentName(), extent);
            }
        }

        if((xMin != Float.MIN_VALUE) && (yMin != Float.MIN_VALUE) && (xMax != Float.MIN_VALUE) && (yMax != Float.MIN_VALUE))
        {
            WMSLatLongBox latLongBox = new WMSLatLongBox();
            latLongBox.setMinX(xMin);
            latLongBox.setMinY(yMin);
            latLongBox.setMaxX(xMax);
            latLongBox.setMaxY(yMax);
            varLayer.setLatLongBox(latLongBox);
        }

        addStyles(varLayer, xMin, xMax, yMin,yMax,zMin,tMin);
    }
}
