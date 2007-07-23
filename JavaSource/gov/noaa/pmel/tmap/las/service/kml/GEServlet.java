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
public class GEServlet extends HttpServlet
{
    private static Logger log = Logger.getLogger(GEServlet.class);

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * Processes requests either from the LAS UI
     * or from Google Earth's view-based queries
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {

        String lasUIRequestXML = request.getParameter("xml");
        //String plotkml_URL = request.getParameter("plotkml");
        String bbox = request.getParameter("BBOX");
        String ge = request.getParameter("GE");

        KMLGenerator kgen = new KMLGenerator();
        GELASWrapper gwra = new GELASWrapper();

        //handle requsets from web browsers
        if(bbox == null){

            //generate a place mark kml
            if(ge!=null && ge.equalsIgnoreCase("placemarks")){
                String placemarksKML = kgen.genPlacemarksKML(request);
                sendKMLResponse(placemarksKML, "placemarks",response);
                return;
            }
         
            //make a LAS request using the xml parameter
            //this is used by the pop-up page of a place mark
            if (ge!=null && ge.equalsIgnoreCase("makeLASRequest")) {
                makeLASRequest(request,response);
                return;
            }

            //generate an overlay kml
            if (ge!=null && ge.equalsIgnoreCase("overlay")) {
                String overlayKML = kgen.genOverlayKML(request);
/*
                String fileName = "/home/porter/webuser/html/tomcat_jing/apache-tomcat-5.5.17/webapps/las_latest/output/tmp_overlay.kml";
                File outFile = new File(fileName);
                FileWriter out = new FileWriter(outFile);
		out.write(overlayKML);
                out.close();
*/
                //return to HTTP response
                sendKMLResponse(overlayKML, "overlay",response);

                return;
            }

            //generate an animation kml
            if (ge!=null && ge.equalsIgnoreCase("animation")) {
                String animationKML = kgen.genAnimationKML(request);
                sendKMLResponse(animationKML, "animation",response);
                return;
            }

        //handle (view-based) request from Google Earth with BBOX parameter
        }else{
            doGERequest(request,response);
        }
    }

    private void sendKMLResponse(String kml, String fileName, HttpServletResponse response)
    throws ServletException, IOException{
        response.setContentType("application/keyhole");

        ByteArrayOutputStream baos = (ByteArrayOutputStream) convert(kml);
        StringBuffer sbContentDispValue = new StringBuffer();
        //sbContentDispValue.append("inline");
        sbContentDispValue.append("attachment");
        sbContentDispValue.append("; filename=");
        sbContentDispValue.append(fileName+".kml");

        response.setHeader("Content-disposition", sbContentDispValue.toString());
        response.setContentLength(baos.size());
        ServletOutputStream sos;
        sos = response.getOutputStream();
        baos.writeTo(sos);
        sos.flush();
    }

    private OutputStream convert(String aString){
        //Convert the string to a byte array
        byte[] byteArray = aString.getBytes();

        //Create a stream of that byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream(byteArray.length);
        try
        {
            //Write the data to that stream
            out.write(byteArray);
        } catch(Exception e)
        {
            e.printStackTrace();
        }
        //Cast to OutputStream and return
        return (OutputStream) out;
    }

    public void doPost (HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException { 
        log.info("in doPost0000000000000000000");
        doGet(request, response); 
    }


    /**
     * Send a LAS request to ProductServer.do
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private void makeLASRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {

        log.info("enter makeLASRequest");

        String requestURL="";
        LASUIRequest lasUIRequest = new LASUIRequest();
        String lasRequestXML = request.getParameter("xml");
        try{
            JDOMUtils.XML2JDOM(lasRequestXML, lasUIRequest);
        } catch (Exception e){
            log.info("error while create LASUIRequest: " + e.toString());
            showError(response,e.toString());
        }

        try{
            String serverURL = getServerURL(request)+"/ProductServer.do";
            requestURL = serverURL+"?xml="+lasUIRequest.toEncodedURLString();
        } catch (Exception e){
            log.info("error while update LASUIRequest: " + e.toString());
            showError(response, requestURL);
        }
        //send request to product server
        GELASWrapper gwra = new GELASWrapper();
        String result = gwra.makeProductServerRequest(requestURL);
        log.info("result: "+result);

        //handle the backend response (XML) returned from product server 
        handleResult(result, request, response);
    }

    /**
     * Show message on browser -- for debug purpose only
     * @param response the HttpServletResponse
     * @param msg the message to show
     */
    private void showError(HttpServletResponse response, String msg)
    throws ServletException, IOException
    {
         response.setContentType("text/html");
         PrintWriter out = response.getWriter();
         out.println(msg);
         return; 
    }

    /**
     * Handles the LAS backend response
     * @param result the LAS backend response in XML format
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     */
    //NOTE: the output velocity template for the corresponding LAS operation
    //      must only contain $las_response.toString().
    protected void handleResult(String result, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        LASBackendRequest backend = new LASBackendRequest();
        try{
            JDOMUtils.XML2JDOM(result.trim(), backend);
            String resultID = backend.getResultID(0);

            if(resultID.equalsIgnoreCase("plot_image")){
                String type = backend.getResultType(0);
                if(type.equalsIgnoreCase("image")){
                    //log.info("image location" + backend.getResultFileName(0));
                    showImage(backend.getResultFileName(0), backend.getResultMimeType(0),response);
                    
                }else{
                    showError(response,"Can't find image in backend reponse");
                }
            }
        }catch(Exception e){
            log.info("Error while trying to interpret backend response XML: " + e.toString());
            showError(response, "Error while trying to interpret backend response XML");
        }
    }

    /**
     * Show an image 
     * @param filename path to the image
     * @param response the HttpServletResponse
     * 
     */
    protected void showImage(String filename, String mimeType, HttpServletResponse response)
    throws ServletException, IOException
    {
        log.info("enter showImage");
        //String mimeType = "image/png";
/*
        ServletContext sc = getServletContext();
        //filename="/home/porter/jing/tomcat/tomcat/webapps/lasge2/images/rtbox.gif";

        // Get the MIME type of the image
        String mimeType = sc.getMimeType(filename);
        if (mimeType == null) {
            sc.log("Could not get MIME type of "+filename);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
*/
        log.info("mime type:" + mimeType);
        // Set content type
        response.setContentType(mimeType);

        // Set content size
        File file = new File(filename);
        response.setContentLength((int)file.length());

        // Open the file and output streams
        FileInputStream ins = new FileInputStream(file);
        OutputStream outs = response.getOutputStream();

        // Copy the contents of the file to the output stream
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = ins.read(buf)) >= 0) {
            outs.write(buf, 0, count);
        }
        ins.close();
        outs.close();
        return;
    }

    /**
    * Processes request from Google Earth for dynamic overlay operation
    *
    */
    private void doGERequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        String coords = request.getParameter("BBOX");
        String lasInitRequestXML = request.getParameter("xml");

        //BBOX is defined as [longitude_west, latitude_south, longitude_east, latitude_north]

        if (coords==null){
            return;
        }

        //Google Earth Mime type
        response.setContentType("application/keyhole");
        //response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String[] coParts= coords.split(",");

        //view center
        float userlon;
        float userlat;

        //view boundary
        float minlon;
        float maxlon;
        float minlat;
        float maxlat;
        float[] bbox = new float[4];

        //String serverURL = "http://"+name+ports+contextPath+"/ProductServer.do";
        String serverURL = getServerURL(request)+"/ProductServer.do";

        //process bbox info
        try{
            userlon = ((Float.parseFloat(coParts[2]) - Float.parseFloat(coParts[0]))/2) + Float.parseFloat(coParts[0]);
            userlat = ((Float.parseFloat(coParts[3]) - Float.parseFloat(coParts[1]))/2) + Float.parseFloat(coParts[1]);
            minlon  = Float.parseFloat(coParts[0]);
            maxlon  = Float.parseFloat(coParts[2]);
            minlat  = Float.parseFloat(coParts[1]);
            maxlat  = Float.parseFloat(coParts[3]);
            bbox[0] = minlon;
            bbox[1] = minlat;
            bbox[2] = maxlon;
            bbox[3] = maxlat;
         }catch(NumberFormatException e){
            log.info("error while process BBOX from Google Earth: " + e.toString());
            return;
        }

        KMLGenerator kgen = new KMLGenerator();
        String kmlString = kgen.genXYPlotKMLForOverlay(bbox, serverURL, lasInitRequestXML);
        out.println(kmlString);
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
