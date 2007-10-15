/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.request;


import au.org.tpac.wms.lib.*;

import java.util.*;


/**
 * <p>
 * This class represents a WMS GetMap request.
 * </p>
 * <p>
 * Date: 6/02/2006 <br>
 * Time: 09:53:01  <br>
 * </p>
 * @author Pauline Mak, pauline@insight4.com (Insight4 Pty. Ltd.)
 */
public class WMSRequest
{
	/**
	* The version number of the WMS this request is based on.
	*/
    public final static String version = "1.1.1";
    protected String finalRequestString;
    private WMSCapabilities cap;
    private int imageWidth;
    private int imageHeight;
    protected TreeSet layers;
    protected Hashtable styles;
    protected String format;
    protected Hashtable extents;
    private String SRS;
    protected float minX, minY, maxX, maxY;
    protected String errorFormatType;
    protected float minXLimit, minYLimit, maxXLimit, maxYLimit;

    private boolean isValid;
    private String exceptionCode;

    /**
     * an empty constructor
     */
    public WMSRequest()
    {
        finalRequestString = "";

        //defualt width & height
        imageWidth = 800;
        imageHeight = 600;
        format = "image/gif";
        SRS = "EPSG:4326";  //default...
        minX = -180;
        minY = -90;
        maxX = 180;
        maxY = 90;

        errorFormatType = "text/plain";

        layers = new TreeSet();
        extents = new Hashtable();
        styles = new Hashtable();

        isValid =  true;
        exceptionCode = null;
    }

    /**
     * Constructs a WMSRequest based on a give WMSCapabilities
     * @param _cap a WMSCapabilities_111.  Note that this request is based on the 1.1.1 schema
     */
    public WMSRequest(WMSCapabilities _cap)
    {
        cap = _cap;
        finalRequestString = "";

        //defualt width & height
        imageWidth = 800;
        imageHeight = 600;
        format = "image/gif";
        SRS = "EPSG:4326";
        minX = -180;
        minY = -90;
        maxX = 180;
        maxY = 90;
        errorFormatType = "text/plain";
        minXLimit = Float.MAX_VALUE;
        minYLimit = Float.MAX_VALUE;
        maxXLimit = Float.MAX_VALUE;
        maxYLimit = Float.MAX_VALUE;

        layers = new TreeSet();
        styles = new Hashtable();
        extents = new Hashtable();

//jli
        isValid =  true;
        exceptionCode = null;

        Vector excp = cap.getExceptionTypes();

        //put in the first one :)
        if(excp.size() > 0)
            this.errorFormatType = (String)(excp.elementAt(0));
    }

    /**
     * Re-constitutes a WMS string into a new WMS object.
     * @param wmsString original WMS request string
     * @param wmsCap WMS Capabilities
     */
    public WMSRequest(String wmsString, WMSCapabilities wmsCap)
    {
//jli
        isValid =  true;
        exceptionCode = null;

        finalRequestString = wmsString;

        cap = wmsCap;
 
        //defualt width & height
        imageWidth = 800;
        imageHeight = 600;
        format = "image/gif";
        SRS = "EPSG:4326";  //default...
        minX = -180;
        minY = -90;
        maxX = 180;
        maxY = 90;

        errorFormatType = "text/plain";

        layers = new TreeSet();
        extents = new Hashtable();
        styles = new Hashtable();

        //first part = http:/.....
        //second part = ?....&
        String[] params = wmsString.split("&");

        String[] splitParam;
        String key;
        String value;
        String stylesValue = "";
        String[] layersNames = null;

        System.out.println("wms string"+wmsString);

        for(int i = 0; i < params.length; i++)
        {
            splitParam = params[i].split("=");

            if(splitParam.length == 2)
            {
                key = splitParam[0];
                value = splitParam[1];

                if(key.equalsIgnoreCase("VERSION"))
                {
                    if(value.equalsIgnoreCase("1.1.1"))
                    {
                        //all good :)
                    }
                }
                else if(key.equalsIgnoreCase("REQUEST"))
                {
                    if(value.equalsIgnoreCase("GetMap"))
                    {
                        //all good :)
                    }
                }
                else if(key.equalsIgnoreCase("LAYERS"))
                {
                    layersNames = value.split(",");

                    System.out.println("value: " + value);

                    for(int j = 0; j < layersNames.length; j++)
                    {
                        this.addLayer(wmsCap.getLayer(layersNames[j]));
                    }
                }
                else if(key.equalsIgnoreCase("FORMAT"))
                {
                    this.format = value;
                }
                else if(key.equalsIgnoreCase("WIDTH"))
                {
                    this.imageWidth = Integer.parseInt(value);
                }
                else if(key.equalsIgnoreCase("HEIGHT"))
                {
                    this.imageHeight = Integer.parseInt(value);
                }
                else if(key.equalsIgnoreCase("SRS"))
                {
                    this.SRS = value;
                }
                else if(key.equalsIgnoreCase("BBOX"))
                {
                    String[] coords = value.split(",");

                    minX = Float.parseFloat(coords[0]);
                    minY = Float.parseFloat(coords[1]);
                    maxX = Float.parseFloat(coords[2]);
                    maxY = Float.parseFloat(coords[3]);
 //jli
                    //check if BBOX is valid
                    checkBBOX(layersNames);
                }
                else if(key.equalsIgnoreCase("EXCEPTION"))
                {
                    this.errorFormatType = value;
                }
                else if(key.equalsIgnoreCase("TIME"))
                {
                    extents.put(key, value);
                }
                else if(key.equalsIgnoreCase("ELEVATION"))
                {
                    extents.put(key, value);
                }
                else if(key.startsWith("DIM_"))
                {
                    //jli --  TODO: use default value if a parameter value is missing
                    extents.put(key.substring("DIM_".length(), key.length()), value);
                }
                else if(key.equalsIgnoreCase("STYLES"))
                {
                    stylesValue = value;
                    System.out.println("styles="+stylesValue);
                }
            }

            if(splitParam.length == 1){
                System.out.println("styles is null");
            }
        }

        if((stylesValue != "") && (stylesValue != null)){
            String[] styleSplits = stylesValue.split(",");

             if((layersNames != null) && (layersNames.length == styleSplits.length))
            {
                for(int i = 0; i < layersNames.length; i++)
                {
                    styles.put(layersNames[i], styleSplits[i]);
                }
            }
            else
            {
                System.err.println("Cannot create WMS request object. No layers were specified or the number of styles or layers do not match.");
            }
        }else{
            //jli
            styles =  null;
            System.out.println("styles was not sepcified");
        }
    }

    public TreeSet getLayers()
    {
        return layers;
    }

    /*
     * Check if BBOX parameter is valid
     */
    private void checkBBOX(String[] layersNames){
        if((minX >= maxX) || (minY >= maxY)){
            isValid = false;
            exceptionCode = "Invalid BBOX value";
        }

        //check if BBOX is in range
        for(int j = 0; j < layersNames.length; j++){

            //get a layer's LatLonBoundingBox
            WMSLayer wmsLay = cap.getLayer(layersNames[j]);
            WMSLatLongBox wmsLatLongBBOX = wmsLay.getLatLongBox();
            float wmsMinX = wmsLatLongBBOX.getMinX();
            float wmsMaxX = wmsLatLongBBOX.getMaxX();
            float wmsMinY = wmsLatLongBBOX.getMinY();
            float wmsMaxY = wmsLatLongBBOX.getMaxY();

            //System.out.println("minY="+minY+"; wmsMaxY="+wmsMaxY);
            if( (maxY < wmsMinY) || (minY > wmsMaxY) ){
                isValid = false;
                exceptionCode = "Invalid BBOX value";
            }

/* use relaxed constraint for X
            //[minX, maxX] is not inside, or does not overlap, or does not include [wmsMinX, wmsMaxX]
            if( isNotInRange(minX, wmsMinX, wmsMaxX) && isNotInRange(maxX, wmsMinX, wmsMaxX)
                && isNotInRange(wmsMinX, minX, maxX) && isNotInRange(wmsMaxX, minX, maxX)){
                isValid = false;
                exceptionCode = "Invalid BBOX value";
            }
*/
        }
    }

    //assume lon form BBOX parameter is in range of [-180,360]
    private boolean isNotInRange(float lon, float minLon, float maxLon){
       System.out.println("lon="+lon+"; minlon="+minLon +"; maxLon="+maxLon);
       if( (lon > minLon) && (lon < maxLon) ){
           return false;
       } 

       //global
       if(maxLon - minLon >= 360.0){return false;}

       if( lon < 0.0){
           if(maxLon < 180.0){
               return true;
           }else if( (maxLon >= 180.0) && (maxLon <= 360.0) ){
               float tmp = maxLon-360.0f;
               if(lon < tmp){return false;}
           }
       }else{
           if(lon < 180.0){
               return true;
           }else{
               float tmp = lon-360.0f;
               if(tmp > minLon){return false;}
           }           
       }
       return true;
    }

    /**
     * Duplicates this WMSRequest
     * @return a deep copy of this WMSRequest
     */
    public WMSRequest cloneReq()
    {
        WMSRequest result = new WMSRequest();
        result.imageWidth = imageWidth;
        result.imageHeight = imageHeight;
        result.SRS = SRS;
        result.minX = minX;
        result.minY = minY;
        result.maxX = maxX;
        result.maxY = maxY;

        result.cap = cap;
        result.minXLimit = minXLimit;
        result.minYLimit = minYLimit;
        result.maxXLimit = maxXLimit;
        result.maxYLimit = maxYLimit;

        result.errorFormatType = errorFormatType;
        result.layers = new TreeSet();
        result.extents = new Hashtable();
        result.styles = new Hashtable();

        Iterator it = layers.iterator();
        while(it.hasNext())
        {
            result.layers.add(it.next());
        }

        for(Enumeration e = extents.keys(); e.hasMoreElements();)
        {
            Object key = e.nextElement();
            result.extents.put(key, extents.get(key));
        }

        for(Enumeration e = styles.keys(); e.hasMoreElements();)
        {
            Object key = e.nextElement();
            result.styles.put(key, styles.get(key));
        }

        return result;
    }

    /**
     * Get the requested image width
     * @return width of image in pixels
     */
    public int getImageWidth()
    {
        return imageWidth;
    }

    /**
     * Get the height of the requested image
     * @return height of image in pixels
     */
    public int getImageHeight()
    {
        return imageHeight;
    }



    public void setCoordSystem(String system)
    {
        this.SRS = system;
    }

    /**
     * Get the selected styles each selected layer.
     * @return a Hashtable, where the key is the layer name, and the value is a WMSStyle
     */
    public Hashtable getStyles()
    {
        return styles;
    }

    /**
     * Get the chosen image format type
     * @param _format format type (e.g. png)
     */
    public void setImageFormat(String _format)
    {
        this.format = _format;
    }

    /**
     * Set the dimension of the request
     * @param _minX left coordinate
     * @param _minY bottom coordinate
     * @param _maxX right coordinate
     * @param _maxY top coordinate
     */
    public void setDim(float _minX, float _minY, float _maxX, float _maxY)
    {
        minX = _minX;
        minY = _minY;
        maxX = _maxX;
        maxY = _maxY;
    }

    /**
     * Get the common left and right <b>limit</b>  of the chosen layers.
     * @return a float with length of 2, index if 0 is the minimum value, and 1 is the maximum value
     */
    public float[] getXLimits()
    {
        float[] result = new float[2];
        result[0] = minXLimit;
        result[1] = maxXLimit;
        return result;
    }

    /**
     * This is a method taken from the LAS interface
     * @param angle
     * @return angle translated to the usual 360 by 180 coordinates...
     */
    public int convert_modulo_int(float angle)
    {
        int mod = (int)angle % 360;

        if(mod < 0)
        {
            mod += 360;
        }
        if(mod > 180)
        {
            mod -= 360;
        }

        return mod;
    }

    /**
     * Get the common top and bottom <b>limit</b> of the chosen layers.
     * @return a float with length of 2, index if 0 is the minimum value, and 1 is the maximum value
     */
    public float[] getYLimits()
    {
        float[] result = new float[2];

        result[0] = minYLimit;
        result[1] = maxYLimit;
        return result;
    }

    /**
     * Get the left coordinate chosen.<br>
     * (note that this must be within the boundary)
     * @return a float of the left coordinate
     */
    public float getMinX()
    {
        return minX;
    }

    /**
     * Get the bottom coordinate chosen.<br>
     * (note that this must be within the boundary)
     * @return a float of the bottom coordinate
     */
    public float getMinY()
    {
        return minY;
    }

    /**
     * Get the right coordinate chosen.<br>
     * (note that this must be within the boundary)
     * @return a float of the right coordinate
     */
    public float getMaxX()
    {
        return maxX;
    }

    /**
     * Get the top coordinate chosen.<br>
     * (note that this must be within the boundary)
     * @return a float of the top coordinate
     */
    public float getMaxY()
    {
        return maxY;
    }

    /**
     * Return the image format for the requested image
     * @return image format a MIME type string.  E.g. "image/png" indicates a PNG image.
     */
    public String getImageFormat()
    {
        return format;
    }

    public void setSRS(String _SRS)
    {
        SRS = _SRS;
    }

    public Hashtable getExtents()
    {
        return extents;
    }

    /**
     * Set any extent
     * @param extentName extent name
     * @param value value of the extent
     */
    public void setExtent(String extentName, String value)
    {
        extents.put(extentName.trim(), value.trim());
    }


    /**
     * The image format of resulting iamge
     * @param _format e.g. "image/png" or "image/gif".  This should conform to MIME types.
     */
    public void setFormat(String _format)
    {
        this.format = _format;
    }
    /**
     * Convert request type into the final request string
     * @return a string
     */
    protected String requstToString()
    {
        return "REQUEST=GetMap";
    }

    /**
     * Convert version into the final request string
     * @return a string
     */
    protected String addVersion()
    {
        return "VERSION=" + cap.getVersion();
    }

    /**
     * Set image width
     * @param width width of the image
     */
    public void setWidth(int width)
    {
        imageWidth = width;
    }

    /**
     * Set image height
     * @param height width of the image
     */
    public void setHeight(int height)
    {
        imageHeight = height;
    }

    /**
     * Add a WMSLayer to this request
     * @param layer selects layer to display
     */
    public void addLayer(WMSLayer layer)
    {
        if(layer != null)
        {
            layers.add(layer);
            autoSetLimits(layer);
        }
    }

    /**
     * Returns a set of selected WMS layers
     * @return a set of WMS layers which are in this WMS request.
     */
    public TreeSet getSelectedLayerList()
    {
        return layers;
    }

    /**
     * Returns a list of comma separated layers names.  Each layer name is defined from the root layer and
     * each sub-layer is separated by a "//".  i.e. root//sublayer1//layer2,root//sublayer2//layer3
     * @return a string of comma separated layers.
     */
    public String getSelectedLayers()
	{
		String result = "";
		
		Iterator it = layers.iterator();

        String val = "";
        while(it.hasNext())
		{
            WMSLayer layer = (WMSLayer)it.next();
            String path = layer.getLayerName();
            WMSLayer tmpLayer = layer;

            while(tmpLayer.getParent() != null)
            {
                if(tmpLayer.getParent().getLayerName() == null)
                    val = tmpLayer.getParent().getTitle();
                else
                    val = tmpLayer.getParent().getLayerName();
                path = val + "//" + path;
                tmpLayer = tmpLayer.getParent();
            }

			result += path + ",";
        }
		
		return result;
	}

    /**
     * Sets a style for a give layer
     * @param layerName name of the layer
     * @param styleName style of the layer
     */
    public void setStyle(String layerName, String styleName)
    {
        this.styles.put(layerName, styleName);
    }

    /**
     * Convert styles into the final request string
     * @return a string
     */
    protected String stylesToString()
    {
        String result = "";

        Iterator it = layers.iterator();

        while(it.hasNext())
        {
            WMSLayer layer = (WMSLayer)it.next();
            String layerName = layer.getLayerName();

            if(styles.containsKey(layerName))
            {
                result += (String)(styles.get(layerName));
            }

            if(!(layer.getLayerName().equals(((WMSLayer)layers.last()).getLayerName())))
            {
                result += ",";
            }

        }

        return "STYLES=" + result;
    }

    /**
     * Removes all selected layers information from this WMSRequest.
     */
    public void clearLayers()
    {
        layers.clear();
        styles.clear();
        extents.clone();
    }

    /**
     * Add extent to the final request string
     * @return a string
     */
    protected String addExtents()
    {
        String result = "";

        for(Enumeration e = extents.keys(); e.hasMoreElements();)
        {
            String key = (String)(e.nextElement());
            String val = (String)(extents.get(key));

            if(key.compareToIgnoreCase("time") == 0)
            {
                result += "TIME=" + val;
            }
            else if(key.compareToIgnoreCase("ELAVATION") == 0)
            {
                result += "ELEVATION=" + val;
            }
            else
            {
                result += "DIM_" + key.toUpperCase() + "=" + val;
            }

            if(e.hasMoreElements())
                result += "&";
        }

        return result;
    }

    /**
     * automatically set the limits with chosen layers
     * @param myLayer
     */
    protected void autoSetLimits(WMSLayer myLayer)
    {
        if(myLayer.getLatLongBox() != null)
        {
            minXLimit = Math.min((myLayer.getLatLongBox().getMinX()), minXLimit);
            minYLimit = Math.min(myLayer.getLatLongBox().getMinY(), minYLimit);
            maxXLimit = Math.min((myLayer.getLatLongBox().getMaxX()), maxXLimit);   //still trying to find the smallest common maximum
            maxYLimit = Math.min(myLayer.getLatLongBox().getMaxY(), maxYLimit);
        }
    }

    public String getFirstLayerName()
    {
        if(layers != null)
        {
            Iterator it = layers.iterator();
            WMSLayer layer = (WMSLayer)it.next();
            return layer.getLayerName();
        }

        return null;
    }

    //constructing for the final query string

    /**
     * Convert chosen layers into the final request string
     * @return a string
     */
    protected String layersToString()
    {
        String result = "LAYERS=";

        Iterator it = layers.iterator();


        while(it.hasNext())
        {
            WMSLayer layer = (WMSLayer)it.next();

            if(!layer.getLayerName().equals(((WMSLayer)layers.last()).getLayerName())) //last one, so no commas
            {
                result += layer.getLayerName()+ ",";
            }
            else
                result += layer.getLayerName();
        }

        return result;
    }

    /**
     * Convert the image type to the final request string
     * @return a string
     */
    protected String formatToString()
    {
        return "FORMAT=" + this.format;
    }

    /**
     * Convert the image width and height to the final request string
     * @return a string
     */
    protected String dimToString()
    {
        return "WIDTH=" + imageWidth + "&HEIGHT=" + imageHeight;
    }

    /**
     * Convert the chosen SRS to the final request string
     * @return a string
     */
    protected String SRStoString()
    {
        return "SRS=" + this.SRS;
    }

    /**
     * Convert the bounding box into the final request string
     * @return a string
     */
    protected String bboxToString()
    {
        return "BBOX=" + minX + "," + minY + "," + maxX + "," + maxY;
    }

    /**
     * Set the exception type this request would like the errors to report as<br>
     * This must be a supported type in WMSCapabiltiies
     * @param _errorFormatType error type
     */
    public void setException(String _errorFormatType)
    {
        errorFormatType = _errorFormatType;
    }

    /**
     * Convert the error format into the final request string
     * @return a string
     */
    protected String errorFormatToString()
    {
        return "EXCEPTION=" + errorFormatType;
    }

    /**
     * Combine all attributes to make the full request string
     * @return full request string, including the URL
     */
    public String getFinalRequestString()
    {
        finalRequestString = cap.getServerAddress();

        if(finalRequestString.indexOf("?") > -1)
             finalRequestString += "&" + this.addVersion();
        else
            finalRequestString += "?" + this.addVersion();
        finalRequestString += "&" + requstToString();
        finalRequestString += "&" + layersToString();
        finalRequestString += "&" + stylesToString();
        finalRequestString += "&" + dimToString();
        finalRequestString += "&" + formatToString();
        finalRequestString += "&" + SRStoString();
        finalRequestString += "&" + bboxToString();
        finalRequestString += "&" + errorFormatToString();

        String ext = addExtents();

        if(ext.compareToIgnoreCase("") != 0)
            finalRequestString += "&" + this.addExtents();

        return finalRequestString;
    }

    /**
     * Get a stripped version of the request string<br>
     * This does not include the image width, height or image format.
     * @return a reduced version of the full final request string
     */
    public String getBaseReq()
    {
        String reqStr = cap.getServerAddress();

        if(reqStr.indexOf("?") > -1)
             reqStr += "&" + this.addVersion();
        else
            reqStr += "?" + this.addVersion();

        finalRequestString += "&SERVICE=WMS";

        reqStr += "&" + requstToString();
        reqStr += "&" + layersToString();
        reqStr += "&" + stylesToString();
        reqStr += "&" + SRStoString();
        reqStr += "&" + bboxToString();
        reqStr += "&" + errorFormatToString();

        String ext = addExtents();

        if(ext.compareToIgnoreCase("") != 0)
            reqStr += "&" + this.addExtents();

        return reqStr;
    }

    /**
     * Returns the WMS version of this WMSRequest
     * @return the version as a string (should be 1.1.1)
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the isValid
     * @return the isValid 
     */
    public boolean isValid()
    {
        return isValid;
    }

    /**
     * Returns exception code of this WMSRequest if it is not valid
     * @return the exception code as a string
     */
    public String getExceptionCode()
    {
        return exceptionCode;
    }
}
