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

        //log.info("entering GEServerAction");

        //use Google Earth Mime type
        response.setContentType("application/keyhole");
        PrintWriter out = response.getWriter();

        //create the ground overlay KML for a single plot
        String kmlString = genPlotOverlayKML(request);

        //return back the KML string
        if( (kmlString != "") && (kmlString != null)){
            out.println(kmlString);
        }else{
            //should ouput an error message to GE
            log.info("error while generting the plot kml");
        }

        return null;
    }

    /**
     * Check if it's dynamic GE overlay 
     * @param lasInitRequestXML The initail LAS UI request
     */
    private boolean isDynamic(HttpServletRequest request)
        throws ServletException, IOException
    {
        String lasInitRequestXML = request.getParameter("xml");
        LASUIRequest lasUIRequest = new LASUIRequest();

        try{
            JDOMUtils.XML2JDOM(lasInitRequestXML, lasUIRequest);
        } catch (Exception e){
            log.info("error while create LASUIRequest: " + e.toString());
        }

        if(lasUIRequest.getProperty("ferret", "ge_overlay_style").equals("dynamic")){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Check the GE overlay style
     * @param request The HttpServletRequest
     */
    private LASUIRequest getLASUIRequest(HttpServletRequest request)
        throws ServletException, IOException
    {
        String lasInitRequestXML = request.getParameter("xml");
        LASUIRequest lasUIRequest = new LASUIRequest();
        try{
            JDOMUtils.XML2JDOM(lasInitRequestXML, lasUIRequest);
        } catch (Exception e){
            log.info("error while create LASUIRequest: " + e.toString());
        }
        return lasUIRequest;
    }

    /**
     * Retrieve the BBOX coming from Google Earth
     * @param request The HttpServletRequest
     */
    private float[] getBBOX(HttpServletRequest request)
        throws ServletException, IOException 
    {
        //get BBOX coming from GE
        String coords = request.getParameter("BBOX");

            //BBOX is defined as [longitude_west, latitude_south, longitude_east, latitude_north]
            if (coords==null || coords==""){
                log.info("There is no BBOX information in this request.");
                return null;
            }
            String[] coParts= coords.split(",");

            float userlon;
            float userlat;//view center
            float[] bbox = new float[4];//view boundary

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
        return bbox;
    }

    /**
    * Generate a KML file for a XY plot; it is just a ground overlay
    * @param request The HttpServletRequest
    */
    public String genPlotOverlayKML(HttpServletRequest request)
        throws ServletException, IOException 
    {
        LASUIRequest lasUIRequest = getLASUIRequest(request);
        boolean isDynamic = isDynamic(request);

        String requestURL ="";
        String kmlString ="";
        String serverURL = getServerURL(request)+"/ProductServer.do";

        try{

            //update XY range for dynamic overlay
            if(isDynamic){
                float[] bbox = getBBOX(request);
                Float minlon = new Float(bbox[0]);
                Float maxlon = new Float(bbox[2]);
                Float minlat = new Float(bbox[1]);
                Float maxlat = new Float(bbox[3]);
                lasUIRequest.setRange("x", minlon.toString(),maxlon.toString());
                lasUIRequest.setRange("y", minlat.toString(),maxlat.toString());
            }

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
