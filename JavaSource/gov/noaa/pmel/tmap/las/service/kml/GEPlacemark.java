package gov.noaa.pmel.tmap.las.service.kml;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.joda.time.Period;

import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;

import java.util.HashMap;
import java.io.IOException;

import org.apache.log4j.Logger;

/*
 * A single Google Earth placemark
 */
public class GEPlacemark{

    private static Logger log = Logger.getLogger(GEPlacemark.class);

    String kmlString = "";
    String point_lat;
    String point_lon;
    HashMap<String, String> initLASInfo;
    String dataset_ID;
    String var_ID;
    String view;
    String thi;
    String tlo;
    String zhi;
    String zlo;
    String dsIntervals;

    String base_URL;

    String style = "";
    String description ="";
    LASBackendRequest lasBackendRequest;

    public GEPlacemark(String lat, String lon, HashMap<String, String> initLAS, LASBackendRequest lasBackendReq, String baseURL) throws IOException{
        point_lat   = lat;
        point_lon   = lon;
        initLASInfo = initLAS;
        lasBackendRequest = lasBackendReq;
        base_URL = baseURL;
        init();
        checkPointLon();
        makeKMLString();
    }

    private void init(){
        dataset_ID = initLASInfo.get("dsID");
        var_ID = initLASInfo.get("varID");
        view   = initLASInfo.get("view");
        dsIntervals = initLASInfo.get("dsIntervals");

        if(dsIntervals.contains("t")){
            tlo = initLASInfo.get("tlo");
            thi = initLASInfo.get("thi");
        }

        if(dsIntervals.contains("z")){
            zlo = initLASInfo.get("zlo");
            zhi = initLASInfo.get("zhi");
        }
    }
    /**
     * check longitude and convert it to be in [-180,180]
     */
    private void checkPointLon(){
        double glon = Double.parseDouble(point_lon);
        if(glon > 180.0){
            double glon360 = glon % 360.0;
            //west
            if(glon360 > 180.0){
                point_lon = Double.toString(glon360-360.0);
            //east
            }else{
                point_lon = Double.toString(glon360);
            }
        }
        if(glon < -180.0){
            double glon360 = glon % 360.0;
            //east
            if(glon360 < -180.0){
                point_lon = Double.toString(glon360+360.0);
            //west
            }else{
                point_lon = Double.toString(glon360);
            }
        }
    }

    /**
     * make a placemark kml
     */
    private void makeKMLString() throws IOException{
        setIconStyle();
        setDescription();

        kmlString = "<Placemark>";

        if(description != ""){
            kmlString = kmlString + description;
        }

        if(style != ""){
            kmlString = kmlString + style;
        }

        kmlString = kmlString + "<Point>"
                    + "<coordinates>"+point_lon+","+point_lat+","+"0</coordinates>"
                    + "</Point>"
                    + "</Placemark>";
    }

    /**
     * set style for icon
     */
    private void setIconStyle(){
        style = "<Style>"
                + "<IconStyle>"
                + "<Icon>"
                + "<href>http://maps.google.com/mapfiles/kml/pal4/icon22.png</href>"
                //+ "<href>"+getServerURL(request)+"/images/icon22.png</href>"
                + "</Icon>"
                + "</IconStyle>"
                + "</Style>";
    }

    /**
     * set description 
     */
    private void setDescription() throws IOException{
        
        description = "<description>"
                      + "<![CDATA[";

        if(view.contains("t")){
            description = description  + "<a href='"
                      + make1DPlotRequest()
                      + "'><h2>Time Series Plot</h2></a><br />";
        }
        if(view.contains("z")){
            description = description +  "<a href='"
                      + make1DPlotRequest()
                      + "'><h2>Vertical Profile Plot</h2></a><br />";
        }
        description = description +  "<br />]]>"
                      + "</description>";
    }

    private String make1DPlotRequest( )throws IOException{

        LASUIRequest lasUIRequest = new LASUIRequest();

        //set datset and variable
        lasUIRequest.addVariable(dataset_ID, var_ID);
        //set operation
        lasUIRequest.setOperation("Plot_1D");
        //set region
        HashMap<String, HashMap<String,String[]>> region = new HashMap<String, HashMap<String,String[]>>();
        HashMap<String, String[]> points = new HashMap<String, String[]>();
        HashMap<String, String[]> intervals = new HashMap<String, String[]>();
        String[] xp = new String[] {point_lon};
        String[] yp = new String[] {point_lat};
        points.put("x", xp);
        points.put("y", yp); 

/*
        if(view.equals("xyt")){
            String[] ti = new String[] {tlo,thi};  
            intervals.put("t",ti);
        }

        if(view.equals("xyz")){
            String[] zi = new String[] {zlo,zhi};
            intervals.put("z",zi);
        }
*/
        if(dsIntervals.contains("t")){
            String[] ti = new String[] {tlo,thi};
            intervals.put("t",ti);
        }

        if(dsIntervals.contains("z")){
            String[] zi = new String[] {zlo,zhi};
            intervals.put("z",zi);
        }

        region.put("points", points);
        region.put("intervals",intervals);

        lasUIRequest.setRegion(region);

        //set ferret properties
        if(view.contains("t")){lasUIRequest.setProperty("ferret", "view", "t");}
        if(view.contains("z")){lasUIRequest.setProperty("ferret", "view", "z");}

        lasUIRequest.setProperty("ferret", "format", "line");
        lasUIRequest.setProperty("ferret", "deg_min_sec", "default");
        lasUIRequest.setProperty("ferret", "image_format", "default");
        lasUIRequest.setProperty("ferret", "interpolate_data", "false");
        lasUIRequest.setProperty("ferret", "line_color", "default");
        lasUIRequest.setProperty("ferret", "line_or_sym", "default"); 
        lasUIRequest.setProperty("ferret", "line_thickness", "default");
        lasUIRequest.setProperty("ferret", "line_margins", "default");
        lasUIRequest.setProperty("ferret", "size", "0.5");
        lasUIRequest.setProperty("ferret", "use_graticules", "default");   
        lasUIRequest.setProperty("ferret", "line_ref_map", "default");

        String serverURL = base_URL.replaceFirst("output","ProductServer");
        String lasReq = serverURL+".do?xml="
                           + lasUIRequest.toEncodedURLString()
                           + "&stream=true&stream_ID=plot_image";
        return lasReq;
    }

    public String toString(){
        return kmlString;
    }
}
