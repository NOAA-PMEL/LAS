package gov.noaa.pmel.tmap.las.service.kml;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.net.*;
import java.io.*;
import java.util.Date;

import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASMapScale;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.jdom.ServerConfig;
import gov.noaa.pmel.tmap.las.jdom.LASDocument;
import gov.noaa.pmel.tmap.las.product.request.ProductRequest;
import gov.noaa.pmel.tmap.las.service.ProductWebService;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;

/**
 *@author Jing Yang Li
 *
 */
public class KMLGenerator
{

    private static Logger log = Logger.getLogger(KMLGenerator.class);

    /**
     * Generates a KML file of place marks
     * @param request the HttpServletRequest which contains the initial LASUIRequest
     * @return plKML the KML file of place marks
     */
    public String genPlacemarksKML(HttpServletRequest request)
    throws ServletException, IOException
    {
        String lasUIRequestXML = request.getParameter("xml");

        //viewType is either xyt or xyt
        String viewType = getViewType(lasUIRequestXML);

        String plotType = "";
        if(viewType.contains("t")){
            //for xyt view, show time series of a grid point 
            //when the corresponding place mark is clicked
            plotType = "Time Series";
        }else if (viewType.contains("z")){
            //for xyz view, show vertical profile of a grid point
            //when the corresponding place mark is clicked
            plotType = "Depth Profile";
        }

        LASUIRequest lasUIRequest = new LASUIRequest();
        try{
            JDOMUtils.XML2JDOM(lasUIRequestXML, lasUIRequest);
        } catch (Exception e){
            log.info("error while create LASUIRequest: " + e.toString());
        }

        String plKML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<kml xmlns=\"http://earth.google.com/kml/2.1\">"
        + "<Document>"
        + "<name>"+lasUIRequest.getDatasetName()+": " +lasUIRequest.getVarName()+"</name>"
        + "<Folder>"
        + "<name>Placemarks</name>";

        GELASWrapper gwrapper = new GELASWrapper();

        //extract grid locations 
        String gridFile = gwrapper.getGridFile(request);
        String gridLat = "";
        String gridLon = "";
        String gridPair= "";
        String nextLine;

        BufferedReader input = null;

        URL url = null;
        URLConnection urlConn = null;
        InputStreamReader  inStream = null;
        BufferedReader buff = null;
        try{
            // Create the URL object that points at the file that contains grid locations
/*
            url  = new URL(gridFile);
            urlConn = url.openConnection();
            inStream = new InputStreamReader(urlConn.getInputStream());
            buff= new BufferedReader(inStream);
*/
            //read the file saved locally
            File f = new File(gridFile);
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            //read lines(lon, lat) from the file
            while (true){
                nextLine =dis.readLine();
                if (nextLine !=null){
                    gridPair = nextLine;
                    gridPair = gridPair.trim();
                    Pattern p = Pattern.compile("\\s");
                    String[] gp = gridPair.split(p.pattern());
                    gridLon = gp[0];
                    gridLat = gp[gp.length-1];

                    //create a place mark for each grid point
                    plKML = plKML + "<Placemark>"
                           //+ "<name>Grid point (lon="+ gridLon + ", lat=" + gridLat +")</name>"
                           + "<Snippet maxLines=\"2\">"
                           + "show time series or depth profile of a grid point"
                           + "</Snippet>"
                           + "<description>"
                           + "<![CDATA["+plotType+"<br /><img src='"
                           + createGERequest(request, gridLon, gridLat)
                           + "' alt='If no image load here, please upgrade Google Earth ...' /><br />"
                           + "<a href='"
                           + createGERequest(request, gridLon, gridLat)
                           + "'>Link to plot</a><br />"
                           + "<a href='"
                           + createLASRequest(request, gridLon, gridLat)
                           + "'> View data</a>" 
                           + "<br />]]>"
                           + "</description>"
                           + "<Style>"
		           + "<IconStyle>"
			   + "<Icon>"
			   //+ "<href>http://maps.google.com/mapfiles/kml/pal4/icon22.png</href>"
                           + "<href>"+getServerURL(request)+"/images/icon22.png</href>" 
			   + "</Icon>"
		           + "</IconStyle>"
	                   + "</Style>";
                    //log.info("grid location = ["+gridLon+", "+gridLat+"]");
 
                    //need to check gridLon and convert it to be in [-180,180]
                    double glon = Double.parseDouble(gridLon);
                    if(glon > 180.0){
                        double glon360 = glon % 360.0;
                        //west
                        if(glon360 > 180.0){
                            gridLon = Double.toString(glon360-360.0);
                        //east
                        }else{
                            gridLon = Double.toString(glon360);
                        }
                    }
                    if(glon < -180.0){
                        double glon360 = glon % 360.0;
                        //east
                        if(glon360 < -180.0){
                            gridLon = Double.toString(glon360+360.0);
                        //west
                        }else{
                            gridLon = Double.toString(glon360);
                        }
                    }

                    plKML = plKML + "<Point>"
                           + "<coordinates>"+gridLon+","+gridLat+","+"0</coordinates>"
                           + "</Point>"
                           + "</Placemark>";
                    //log.info("grid location = ["+gridLon+", "+gridLat+"]");
                }else{
                    break;
                }
            }
        } catch(MalformedURLException e){
            log.info("error while creating a place mark kml: " + e.toString());
        }

        plKML = plKML + "</Folder></Document></kml>";
        return plKML;
    }

    private String getTimeFramesXML(String timeFramesFile){
        String timeFramesXML="";
        String nextLine;
        try{
            File f = new File(timeFramesFile);
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            while (true){
                nextLine =dis.readLine();
                if (nextLine !=null){
                    timeFramesXML = timeFramesXML + nextLine;
                }else{
                    break;
                }
            }
        }catch(Exception e){
            log.info("error in reading time frames");
        }
        return timeFramesXML;
    }
     /**
     * Generates an animation KML file 
     * @param request the HttpServletRequest which contain the initial LASUIRequest
     * @return animationKML the animation KML file
     */
    public String genAnimationKML(HttpServletRequest request)
    throws ServletException, IOException
    {
        String lasUIRequestXML = request.getParameter("xml");
        String timeFramesFile = request.getParameter("FRAMES");

        log.info("time frames xml-------------->"+timeFramesFile);
        String timeFramesXML = getTimeFramesXML(timeFramesFile);

        LASUIRequest lasUIRequest = new LASUIRequest();
        LASDocument timeFrames = new LASDocument();

        String animationKML=null;

        try{
            JDOMUtils.XML2JDOM(lasUIRequestXML, lasUIRequest);

            animationKML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<kml xmlns=\"http://earth.google.com/kml/2.1\">"
                + "<Document>"
                + "<name>"+lasUIRequest.getDatasetName()+": " +lasUIRequest.getVarName()+"</name>"
                + "<Folder>"
                + "<name>Animation</name>";

            String serverURL = getServerURL(request)+"/ProductServer.do";

            //get time points
            JDOMUtils.XML2JDOM(timeFramesXML,timeFrames);
            //log.info("time frames=============="+timeFrames.toString());

            Element framesElement = timeFrames.getElementByXPath("/lasAnimation/frames");
            Element resolutionElement = timeFrames.getElementByXPath("/lasAnimation/resolution");
            String resolution = resolutionElement.getText();
            //Element frame = frames.getChild("frame");
            //log.info("first child ************"+frame.getText());

            int i =1;

            Iterator itr = (framesElement.getChildren()).iterator();
            while(itr.hasNext()) {
                Element frameElement = (Element)itr.next();
                String timeStep = "-"+i+"-15";
                if(i<10){timeStep="-0"+i+"-15";}

                log.info("dates************"+frameElement.getText());

                GEDate geDate = new GEDate(frameElement.getText(),resolution);
                
                // Do something with these children
                animationKML = animationKML +
                       "<GroundOverlay>" +
                       //"<name>Coads - Jan</name>"+
                       "<name>"+geDate.toString()+"</name>"+
                       //"<TimeSpan>"+
                       //"<begin>0000-"+i+"</begin>"+
                       //"</TimeSpan>"+

                       //TimeStamp
                       "<TimeStamp>"+
                       "<when>"+geDate.toString()+"</when>"+
                       "</TimeStamp>"+ 

                       //TimeSpan
/*
                       "<TimeSpan>"+
                       "<begin>"+geDate.toString()+"-01</begin>"+
                       "<end>"+geDate.toString()+"-28</end>"+
                       "</TimeSpan>"+
*/
                       "<drawOrder>"+i+"</drawOrder>"+

                       //"<href>http://porter.pmel.noaa.gov:8765/las_latest/images/rtbox.gif</href>"+
                       //"<href>"+genXYPlot(serverURL, lasUIRequestXML, "15-Jan")+"</href>"+
/*
                       "<Icon>"+
                       "<href>"+genXYPlot(serverURL, lasUIRequestXML, frameElement.getText())+"</href>"+
                       "<viewBoundScale>0.75</viewBoundScale>"+
                       "</Icon>"+
                       "<LatLonBox>"+
                       "<north>90</north>"+
                       "<south>-90</south>"+
                       "<east>180</east>"+
                       "<west>-180</west>"+
                       "</LatLonBox>"+
*/                     genXYPlotKMLForAnimation(serverURL, lasUIRequestXML, frameElement.getText())+
                       "</GroundOverlay>";
                 i++;
            }

            animationKML = animationKML + "</Folder></Document></kml>";

        } catch (Exception e){
            log.info("error while generate animation KML: " + e.toString());
        }

        log.info("animation KML =======>"+animationKML);

        return animationKML;
    }
    /**
     * Creates a request for ProductServer.do
     * @param request the HttpServletRequest which contain the initial LASUIRequest
     * @param gridLon longitude of a grid point
     * @param gridLat latitude of a grid point
     * @return lasRequest the request string for ProductServer.do
     */
    private String createLASRequest(HttpServletRequest request, String gridLon, String gridLat)
    throws ServletException, IOException
    {
        LASUIRequest lasUIRequest = to1DRequest(request, gridLon, gridLat);
        lasUIRequest.changeOperation("Data_Extract");
        lasUIRequest.setProperty("ferret","format","txt");
        String lasRequest = getServerURL(request)+"/ProductServer.do?xml="
                           + lasUIRequest.toEncodedURLString()
                           + "&stream=true&stream_ID=ferret_listing";
        return lasRequest;
    }

    /**
     * Creates a request for GEServlet
     * @param request the HttpServletRequest which contain the initial LASUIRequest
     * @param gridLon longitude of a grid point
     * @param gridLat latitude of a grid point
     * @return geRequest the request string for GEServlet
     */
    private String createGERequest(HttpServletRequest request, String gridLon, String gridLat)
    throws ServletException, IOException 
    {
        LASUIRequest lasUIRequest = to1DRequest(request, gridLon, gridLat);
        lasUIRequest.changeOperation("Plot_1D_GE");
        String geRequest = getServerURL(request)+"/GEServlet?xml="
                           + lasUIRequest.toEncodedURLString()
                           + "&GE=makeLASRequest";
        return geRequest;
    }

    /**
     * Converts a LASUIRequest to a 1-D request
     * @param request the HttpServletRequest which contain the initial LASUIRequest
     * @param gridLon longitude of a grid point
     * @param gridLat latitude of a grid point
     * @return lasUIRequest the 1-D LASUIRequest 
     */
    private LASUIRequest to1DRequest(HttpServletRequest request, String gridLon, String gridLat)
    throws ServletException, IOException
    {
        String lasUIRequestXML = request.getParameter("xml");
        LASUIRequest lasUIRequest = new LASUIRequest();
        try{
            JDOMUtils.XML2JDOM(lasUIRequestXML, lasUIRequest);
        } catch (Exception e){
            log.info("error while create LASUIRequest: " + e.toString());
        }

        //convert the initial LAS UI request to a 1D request (t or z)
        String view = lasUIRequest.getProperty("ferret","view");
        if(view.equals("xt")){
            lasUIRequest.rangeToPoint("x", gridLon);
            lasUIRequest.changeView("t");
        }
        if(view.equals("yt")){
            lasUIRequest.rangeToPoint("y", gridLat);
            lasUIRequest.changeView("t");
        }
        if(view.equals("xz")){
            lasUIRequest.rangeToPoint("x", gridLon);
            lasUIRequest.changeView("z");
        }
        if(view.equals("yz")){
            lasUIRequest.rangeToPoint("y", gridLat);
            lasUIRequest.changeView("z");
        }
        if(view.equals("xyt")){
            lasUIRequest.rangeToPoint("x", gridLon);
            lasUIRequest.rangeToPoint("y", gridLat);
            lasUIRequest.changeView("t");
        }
        if(view.equals("xyz")){
            lasUIRequest.rangeToPoint("x", gridLon);
            lasUIRequest.rangeToPoint("y", gridLat);
            lasUIRequest.changeView("z");
        }

        return lasUIRequest;
    }

    /**
     * Gets view type from a LASUIRequest XML String 
     * @param lasUIRequestXML the LASUIRequest XML String
     * @return view the view type
     */
    private String getViewType(String lasUIRequestXML)
    throws ServletException, IOException
    {
        LASUIRequest lasUIRequest = new LASUIRequest();
        try{
            JDOMUtils.XML2JDOM(lasUIRequestXML, lasUIRequest);
        } catch (Exception e){
            log.info("error while create LASUIRequest: " + e.toString());
        }
        String view = lasUIRequest.getProperty("ferret","view");
        return view;
    }

    /**
     * Generate a KML string for dynamic overlay
     * @param request the HttpServletRequest which contains the initial LASUIResquest
     *                with Plot_GE_Overlay or Vector_GE_Overlay as the operation ID
     * @return overlayKML the KML string for dynamic overlay
     */
    public String genOverlayKML(HttpServletRequest request)
    throws ServletException, IOException
    {
        String lasUIRequestXML = request.getParameter("xml");
        LASUIRequest lasUIRequest = new LASUIRequest();

        try{
            JDOMUtils.XML2JDOM(lasUIRequestXML, lasUIRequest);
        } catch (Exception e){
            log.info("error while create LASUIRequest: " + e.toString());
        }

        String overlayKML =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<kml xmlns=\"http://earth.google.com/kml/2.1\">"
            + "<Folder>"
            + "<name>"+lasUIRequest.getDatasetName()+"</name>"
            + "<NetworkLink>"
            + "<description>Plot generated by LAS</description>"
            + "<name>Image Overlay</name>"
            + "<visibility>1</visibility>"
            + "<open>1</open>"
            + "<refreshVisibility>1</refreshVisibility>"
            + "<flyToView>0</flyToView>"
            + "<Link>"
            + "<href>"+getServerURL(request)+"/GEServlet?xml="
            + lasUIRequest.toEncodedURLString() //Google Earth will append BBOX to here 
            + "</href>"
            //+ "<refreshMode>onInterval</refreshMode>"
            //+ "<refreshInverval>4</refreshInverval>"
            //+ "<refreshMode>onChange</refreshMode>"
            + "<viewRefreshMode>onStop</viewRefreshMode>"
            + "<viewRefreshTime>2</viewRefreshTime>"
            + "</Link>"
            + "</NetworkLink>"
            + "</Folder>"
            + "</kml>";

         return overlayKML;
    }

    /**
    * Generate KML file for a LAS 2D(XY) plot
    * @param bbox The bounding box of the view on Google Earth
    * @param serverURL URL of the LAS product server
    * @param lasInitRequestXML the XML of initial LASUIRequest 
    */
    public String genXYPlotKMLForOverlay(float[] bbox, String serverURL, String lasInitRequestXML)
    {
        LASUIRequest lasUIRequest = new LASUIRequest();
        try{
            JDOMUtils.XML2JDOM(lasInitRequestXML, lasUIRequest);
        } catch (Exception e){
            log.info("error while create LASUIRequest: " + e.toString());
        }

        //get the bbox information
        Float minlon = new Float(bbox[0]);
        Float maxlon = new Float(bbox[2]);
        Float minlat = new Float(bbox[1]);
        Float maxlat = new Float(bbox[3]);

        String kmlURL = "";
        String requestURL ="";
        try{
            //update xy range using the bbox information
            lasUIRequest.setRange("x", minlon.toString(),maxlon.toString());
            lasUIRequest.setRange("y", minlat.toString(),maxlat.toString());

            //update operation
            //the KML for non-vector plot contains a screen overlay of colorbar
            //the KML for vector plot doesn't contain a screen overlay of colorbar
            String op = lasUIRequest.getOperationXPath();
            if(op.contains("Plot_GE_Overlay")){
                lasUIRequest.changeOperation("Plot_GE_kml");
            }else if(op.contains("Vector_GE_Overlay")){
                lasUIRequest.changeOperation("Vector_GE_kml");
            }
            //build the request
            //turn debug
            //requestURL = serverURL+"?xml="+lasUIRequest.toEncodedURLString()+"&debug=debug";
            requestURL = serverURL+"?xml="+lasUIRequest.toEncodedURLString();

        } catch (Exception e){
            log.info("error while update LASUIRequest: " + e.toString());
        }

        //make product server request through GELASWrapper        
        GELASWrapper gwra = new GELASWrapper();
        String result = gwra.makeProductServerRequest(requestURL);
        String kmlString="";

        if(result != null){

            //get URL of the KML file
            LASBackendRequest backend = new LASBackendRequest();
            try{
                JDOMUtils.XML2JDOM(result.trim(), backend);
                kmlURL = backend.getResult("kml");

            }catch(Exception e){
                log.info("Error while trying to interpret backend response XML: " + e.toString());
                log.info("backend response is: "+result);
                //showError(response, "Error while trying to interpret backend response XML");
            }

            kmlString = "<NetworkLink>"
                       + "<description>Variable</description>"
                       + "<name>"+lasUIRequest.getVarName()+"</name>"
                       + "<Link><href>"+kmlURL+"</href></Link></NetworkLink>";
        }else{
            //TODO: it would be nice to return an error page 
            log.info("An error occurs while making request for overlay kml");
        }
        return kmlString;
    }
        
    //for test only
    public String genXYPlotKMLForOverlay01(float[] bbox, String serverURL, String lasInitRequestXML)
    {
        String kmlURL = "http://porter.pmel.noaa.gov:8765/las_latest/output/7B10DDC3B7182B065488E3BE3211B5C6_kml.kml";
        String kmlString = "<NetworkLink>"
                       + "<description>Variable</description>"
                       + "<Url><href>"+kmlURL+"</href></Url></NetworkLink>";
        return kmlString;
    }

    /**
    * Generate KML file for a 2D(XY) LAS plot for animation
    *@param bbox The bounding box of the view on Google Earth
    *@param serverURL URL of the LAS product server
    */
    public String genXYPlotKMLForAnimation(String serverURL, String lasInitRequestXML, String timeFrame)
    {
        LASUIRequest lasUIRequest = new LASUIRequest();
        try{
            JDOMUtils.XML2JDOM(lasInitRequestXML, lasUIRequest);
        } catch (Exception e){
            log.info("error while create LASUIRequest: " + e.toString());
        }

        //TODO: need to separate clas to check if timeFrame is a valid Ferret time
        if(timeFrame.length() == 8){
            //date is in format of MMM-yyyy
            timeFrame = "15-"+timeFrame;
        }

        String plotURL = "";
        String requestURL ="";
        String plotKML = "";
        try{
            //update user request
            //lasUIRequest.setRange("x", minlon.toString(),maxlon.toString());
            //lasUIRequest.setRange("y", minlat.toString(),maxlat.toString());

            //change to 2D request
            lasUIRequest.rangeToPoint("t", timeFrame);
            lasUIRequest.setProperty("ferret","view","xy");
            lasUIRequest.setProperty("ferret", "format","shade");

            String op = lasUIRequest.getOperationXPath();
            //if(op.contains("Google_Earth_Overlay")){
            //    lasUIRequest.changeOperation("Google_Earth_kml");
            /*
            if(op.contains("Plot_GE_Overlay")){
                lasUIRequest.changeOperation("Plot_GE_kml");
            }else if(op.contains("Vector_GE_Overlay")){
                lasUIRequest.changeOperation("Vector_GE_kml");
            }*/
            lasUIRequest.changeOperation("Plot_GE_kml");

            requestURL = serverURL+"?xml="+lasUIRequest.toEncodedURLString();
            log.info("new las request=====>"+requestURL);
        } catch (Exception e){
            log.info("error while update LASUIRequest:----------- " + e.toString());
        }

        GELASWrapper gwra = new GELASWrapper();
        String result = gwra.makeProductServerRequest(requestURL);
        //log.info("backend response: " + result);

        //get URL of the plot
        LASBackendRequest backend = new LASBackendRequest();
        try{
            JDOMUtils.XML2JDOM(result.trim(), backend);
            plotURL = backend.getResult("plot_image");

            //get map scales
            String mapScaleFile   = backend.getResultAsFile("map_scale");
            String mapScaleXML = getTimeFramesXML(mapScaleFile);
            LASDocument mapScale = new LASDocument();
            JDOMUtils.XML2JDOM(mapScaleXML,mapScale);
            String xll = mapScale.getElementByXPath("/map_scale/x_axis_lower_left").getText();
            String yll = mapScale.getElementByXPath("/map_scale/y_axis_lower_left").getText();
            String xur = mapScale.getElementByXPath("/map_scale/x_axis_upper_right").getText();
            String yur = mapScale.getElementByXPath("/map_scale/y_axis_upper_right").getText();
            
            plotKML =  "<Icon>"+
                       "<href>"+plotURL+"</href>"+
                       "<viewBoundScale>0.75</viewBoundScale>"+
                       "</Icon>"+
                       "<LatLonBox>"+
                       "<north>"+yur+"</north>"+
                       "<south>"+yll+"</south>"+
                       "<east>"+xur+"</east>"+
                       "<west>"+xll+"</west>"+
                       "</LatLonBox>";

        }catch(Exception e){
            log.info("Error while trying to interpret backend response XML: " + e.toString());
            log.info("backend response is: "+result);
            //showError(response, "Error while trying to interpret backend response XML");
        }

        return plotKML;
    }

    /**
    * Get the product server URL
    * @param request the HttpServletRequest
    * @return serverURL URL of the LAS product server
    */
    private String getServerURL(HttpServletRequest request)
    throws ServletException, IOException
    {
        //get server info
        String name = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();
        String ports;
        if ( port != 80 ) {
           ports = ":"+String.valueOf(port);
        } else {
           ports = "";
        }
        String serverURL = "http://"+name+ports+contextPath;
        return serverURL;
    }
}
