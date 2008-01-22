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
import gov.noaa.pmel.tmap.las.product.request.ProductRequest;
import gov.noaa.pmel.tmap.las.service.ProductWebService;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.apache.log4j.Logger;

/**
 *@author Jing Yang Li
 *
 */
public final class GEServerAction extends Action {
    private static Logger log = Logger.getLogger(GEServerAction.class);

    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) 
    throws ServletException, IOException{

        String coords = request.getParameter("BBOX");
        String lasInitRequestXML = request.getParameter("xml");
        log.info("GEServerAction, bbox="+coords);

        //BBOX is defined as [longitude_west, latitude_south, longitude_east, latitude_north]
        if (coords==null || coords==""){
            log.info("There is no BBOX information in this request.");
            return null;
        }

        //use Google Earth Mime type
        response.setContentType("application/keyhole");

        PrintWriter out = response.getWriter();
        String[] coParts= coords.split(",");

        //view center
        float userlon;
        float userlat;

        //view boundary
        float[] bbox = new float[4];

        String serverURL = getServerURL(request)+"/ProductServer.do";

        //process bbox info
        try{
            userlon = ((Float.parseFloat(coParts[2]) - Float.parseFloat(coParts[0]))/2) + Float.parseFloat(coParts[0]);
            userlat = ((Float.parseFloat(coParts[3]) - Float.parseFloat(coParts[1]))/2) + Float.parseFloat(coParts[1]);
            bbox[0] = Float.parseFloat(coParts[0]);
            bbox[2] = Float.parseFloat(coParts[2]);
            bbox[1] = Float.parseFloat(coParts[1]);
            bbox[3] = Float.parseFloat(coParts[3]);
         }catch(NumberFormatException e){
            log.info("error while process BBOX from Google Earth: " + e.toString());
            return null;
        }

        //create the static ground overlay KML for a single plot
        String kmlString = genPlotOverlayKML(bbox, serverURL, lasInitRequestXML);

        if( (kmlString != "") && (kmlString != null)){
            out.println(kmlString);
        }else{
            //should ouput an error message to GE
            log.info("error while generting the plot kml");
        }

        return null;
    }

    /**
    * Generate a KML for a XY plot; it is just a static overlay
    * @param bbox The bounding box of the view on Google Earth
    * @param serverURL URL of the LAS product server
    * @param lasInitRequestXML the XML of initial LASUIRequest
    */
    public String genPlotOverlayKML(float[] bbox, String serverURL, String lasInitRequestXML)
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

        String requestURL ="";
        String kmlString ="";

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
            requestURL = serverURL+"?xml="+lasUIRequest.toEncodedURLString();

        } catch (Exception e){
            log.info("error while building LAS Request: " + e.toString());
        }

        if( (requestURL != "") && (requestURL != null)){
            kmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                     +"<kml xmlns=\"http://earth.google.com/kml/2.1\">"
                     +"<Folder>"
                     +"<NetworkLink>"
                     +"<Link>"
                     +"<href>"
                     +requestURL
                     +"</href>"
                     +"</Link>"
                     +"</NetworkLink>"
                     +"</Folder>"
                     +"</kml>";
         }
         return kmlString;
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
