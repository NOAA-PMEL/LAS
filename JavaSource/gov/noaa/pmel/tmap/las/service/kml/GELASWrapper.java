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
public class GELASWrapper
{

    private static Logger log = Logger.getLogger(GELASWrapper.class);
    /**
     * Sends request to product server
     * @param requestURL the request URL
     * @return result the backend response in XML
     */
    public String makeProductServerRequest(String requestURL){
        String result="";
        //StringBuffer sbuf = new StringBuffer();
        //boolean inProgress = true;

        try{
          //while(inProgress){
            //send request to ProductServer
            URL url = new URL(requestURL);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream instream = conn.getInputStream();
            String contentType = conn.getContentType();
            char[] buf = new char[4096];
            BufferedReader is = new BufferedReader(new InputStreamReader(instream));
            StringBuffer sbuf = new StringBuffer();
            int length = is.read(buf, 0, 4096);
            while (length >=0 ){
                sbuf.append(buf, 0, length);
                length = is.read(buf, 0, 4096);
            }
            result =  sbuf.toString();
            //sbuf.delete(0,sbuf.length()-1);
            //inProgress = result.contains("Progress");
            //inProgress = result.matches("(?i).*progress.*");

            //if(inProgress){log.info("ProductServer is still working on the request");}
            //else{log.info("get final result-----------");}
          //}
            if(result.contains("error")){
                log.info("Error result: " + result);
                result=null;
            }
        }catch (IOException e){
            log.info("Error while making product server request in GELASWrapper: " + e.toString());
        }

        return result;
    }

    public String getGridFile(HttpServletRequest request)
    {
        LASUIRequest lasUIRequest = new LASUIRequest();
        String lasInitRequestXML = request.getParameter("xml");

        try{
            JDOMUtils.XML2JDOM(lasInitRequestXML, lasUIRequest);
        } catch (Exception e){
            log.info("error while create LASUIRequest: " + e.toString());
        }

        String gridFileLocation = "";
        String requestURL ="";

        try{
            String serverURL = getServerURL(request)+"/ProductServer.do";
                
            //update user request
            lasUIRequest.changeOperation("Data_Extract_Grid");
            requestURL = serverURL+"?xml="+lasUIRequest.toEncodedURLString();
        } catch (Exception e){
            log.info("error while update LASUIRequest: " + e.toString());
        }

        //send request to product server
        String result = makeProductServerRequest(requestURL);
        LASBackendRequest backend = new LASBackendRequest();

        try{
            JDOMUtils.XML2JDOM(result.trim(), backend);
            String resultID = backend.getResultID(0);

            if(resultID.equalsIgnoreCase("ferret_listing")){
                String type = backend.getResultType(0);
                if(type.equalsIgnoreCase("text")){
                    //showImage(backend.getResultFileName(0), response);
                    gridFileLocation = backend.getResultFileName(0);
                }else{
                    //showError(response,"Can't find image in backend reponse");
                }
            }
        }catch(Exception e){
            log.info("Error while trying to interpret backend response XML: " + e.toString());
            //showError(response, "Error while trying to interpret backend response XML");
        }

        return gridFileLocation;
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
