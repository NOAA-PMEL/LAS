package au.org.tpac.wms.request;

import au.org.tpac.wms.lib.version_1_3_0.WMSCapabilities_130;
import au.org.tpac.wms.lib.version_1_3_0.WMSLayer_130;
import au.org.tpac.wms.lib.WMSLayer;

/**
 * <p>Created by IntelliJ IDEA.
 * Date: 20/03/2006
 * Time: 16:21:52
 * </p>
 * <p><b>Class description</b></p>
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 *         </p>
 */
public class WMSRequest_130 extends WMSRequest
{
    public final static String version = "1.3.0";
    protected String CRS;
    protected WMSCapabilities_130 cap;

    public WMSRequest_130(WMSCapabilities_130 _cap)
    {
        super(_cap);
        cap = _cap;
    }

    public void setSRS(String _CRS)
    {
        CRS = _CRS;
    }

    public void setCoordSystem(String system)
    {
        this.CRS = system;
    }

    public String addCRS()
    {
        return "CRS=" + CRS;
    }

    public String getVersion()
    {
        return version;
    }

    /**
     * automatically set the limits with chosen layers
     * @param myLayer
     */
    protected void autoSetLimits(WMSLayer myLayer)
    {
        WMSLayer_130 my_130 = (WMSLayer_130)myLayer;
        if(my_130.getGeoBox() != null)
        {
            minXLimit = Math.min(my_130.getGeoBox().getWest(), minXLimit);
            minYLimit = Math.min(my_130.getGeoBox().getSouth(), minYLimit);
            maxXLimit = Math.min(my_130.getGeoBox().getEast(), maxXLimit);   //still trying to find the smallest common maximum
            maxYLimit = Math.min(my_130.getGeoBox().getNorth(), maxYLimit);
        }
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

        finalRequestString += "&SERVICE=WMS";

        finalRequestString += "&" + this.requstToString();
        finalRequestString += "&" + this.layersToString();
        finalRequestString += "&" + this.stylesToString();
        finalRequestString += "&" + this.dimToString();
        finalRequestString += "&" + this.formatToString();
        finalRequestString += "&" + this.bboxToString();
        finalRequestString += "&" + addCRS();
        finalRequestString += "&" + this.errorFormatToString();

        String ext = addExtents();

        if(ext.compareToIgnoreCase("") != 0)
            finalRequestString += "&" + this.addExtents();

        return finalRequestString;
    }

}
