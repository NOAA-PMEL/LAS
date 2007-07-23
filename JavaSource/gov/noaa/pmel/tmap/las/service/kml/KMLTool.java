/**
 * This software was developed by the Thermal Modeling and Analysis
 * Project(TMAP) of the National Oceanographic and Atmospheric
 * Administration's (NOAA) Pacific Marine Environmental Lab(PMEL),
 * hereafter referred to as NOAA/PMEL/TMAP.
 *
 * Access and use of this software shall impose the following
 * obligations and understandings on the user. The user is granted the
 * right, without any fee or cost, to use, copy, modify, alter, enhance
 * and distribute this software, and any derivative works thereof, and
 * its supporting documentation for any purpose whatsoever, provided
 * that this entire notice appears in all copies of the software,
 * derivative works and supporting documentation. Further, the user
 * agrees to credit NOAA/PMEL/TMAP in any publications that result from
 * the use of this software or in any product that includes this
 * software. The names TMAP, NOAA and/or PMEL, however, may not be used
 * in any advertising or publicity to endorse or promote any products
 * or commercial entity unless specific written permission is obtained
 * from NOAA/PMEL/TMAP. The user also understands that NOAA/PMEL/TMAP
 * is not obligated to provide the user with any support, consulting,
 * training or assistance of any kind with regard to the use, operation
 * and performance of this software nor to provide the user with any
 * updates, revisions, new versions or "bug fixes".
 *
 * THIS SOFTWARE IS PROVIDED BY NOAA/PMEL/TMAP "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL NOAA/PMEL/TMAP BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
 * CONTRACT, NEGLIGENCE OR OTHER TORTUOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE 
 */
package gov.noaa.pmel.tmap.las.service.kml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.HashMap;
import java.util.Properties;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jdom.JDOMException;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASFerretBackendConfig;
import gov.noaa.pmel.tmap.las.jdom.LASKMLBackendConfig;
import gov.noaa.pmel.tmap.las.jdom.LASMapScale;
import gov.noaa.pmel.tmap.las.service.TemplateTool;

/**
 * @author Roland Schweitzer
 *
 */
/**
 * @author Roland Schweitzer
 *
 */
public class KMLTool extends TemplateTool  {
    
    final Logger log = LogManager.getLogger(KMLTool.class.getName());
    LASKMLBackendConfig kmlBackendConfig;

    /**
     * @throws IOException 
     * 
     */
    public KMLTool() throws LASException, IOException {
        
        super("kml", "KMLBackendConfig.xml");
        
        kmlBackendConfig = new LASKMLBackendConfig();

        try {
            JDOMUtils.XML2JDOM(getConfigFile(), kmlBackendConfig);
        } catch (Exception e) {
            throw new LASException("Could not parse Ferret config file: " + e.toString());
        }
    }

    /**
     * @param serviceName
     * @param configFileName
     * @throws LASException
     * @throws IOException
     */
    public KMLTool(String serviceName, String configFileName)
            throws LASException, IOException {
        super(serviceName, configFileName);
        // TODO Auto-generated constructor stub
    }

    public LASBackendResponse run( LASBackendRequest lasBackendRequest) throws Exception {
        VelocityContext context = new VelocityContext(getToolboxContext());
        Properties p = new Properties();
        InputStream is;
        is = this.getClass().getClassLoader().getResourceAsStream("resources/kml/velocity.properties");
        String template = JDOMUtils.getResourcePath(this, "resources/kml/templates");
        if (is == null) {
            if ( template != null ) {
                // Can't find properties file.  Set where we look for templates.
                log.info("Setting template path to default "+template);
                p.setProperty("file.resource.loader.path", template);
            } else {
                throw new LASException("Cannot find kml backend templates directory.");
            }
        } else {
            p.load(is);
        }  
        if ( p.getProperty("file.resource.loader.path") == null ) {
            if ( template != null ) {
                // Can't find properties file.  Set where we look for templates.
                log.info("Template path not found in properties file.  Setting to default"+template);
                p.setProperty("file.resource.loader.path", template);
            } else {
                throw new LASException("Cannot find kml templates directory.");
            }
        }

        try {
            ve.init(p);
        } catch (Exception e) {
            throw new LASException("Cannot initialize the velocity engine.");
        }
        ve.init();
    
        LASBackendResponse lasBackendResponse = new LASBackendResponse();
        String kml = lasBackendRequest.getServiceAction();
        //make overlay KML
        if (kml.contains("overlay")){
            lasBackendResponse = makeOverlayKML(lasBackendRequest, context);
        }

        //make placemark KML
        if (kml.contains("placemarks")){
            lasBackendResponse = makePlacemarksKML(lasBackendRequest, context);
        }

        return lasBackendResponse;
    }

    private LASBackendResponse makeOverlayKML(LASBackendRequest lasBackendRequest, VelocityContext context)
    throws Exception {
        LASBackendResponse lasBackendResponse = new LASBackendResponse();

        String output = lasBackendRequest.getResultAsFile("kml");
        String map_scale_file = lasBackendRequest.getChainedDataFile("map_scale");
        String map_scale_URL = kmlBackendConfig.getHttpBaseURL() + "/" + map_scale_file.substring(map_scale_file.lastIndexOf(File.separator), map_scale_file.length());
        String plot_image_file = lasBackendRequest.getChainedDataFile("plot_image");
        String plot_image_URL = kmlBackendConfig.getHttpBaseURL() + "/" + plot_image_file.substring(plot_image_file.lastIndexOf(File.separator), plot_image_file.length());

        String kml = lasBackendRequest.getServiceAction();
        if (!kml.endsWith(".vm")) {
            kml = kml+".vm";
        }
        String colorbar_file="";
        String colorbar_URL="";

        LASMapScale map_scale = new LASMapScale();
        try {
            JDOMUtils.XML2JDOM(new File(map_scale_file), map_scale);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        float xll = Float.valueOf(map_scale.getXAxisLowerLeft()).floatValue();
        float xur = Float.valueOf(map_scale.getXAxisUpperRight()).floatValue();
        float xmid = (xur - xll)/2.0f;
        float yll = Float.valueOf(map_scale.getYAxisLowerLeft()).floatValue();
        float yur = Float.valueOf(map_scale.getYAxisUpperRight()).floatValue();
        float ymid = (yur-yll)/2.0f;
        context.put("longitude_center", xmid);
        context.put("latitude_center", ymid);
        context.put("las_response", lasBackendResponse);
        context.put("map_scale", map_scale);
        context.put("map_scale_file", map_scale_file);
        context.put("map_scale_URL", map_scale_URL);
        context.put("plot_image_file", plot_image_file);
        context.put("plot_image_URL", plot_image_URL);

        if(!kml.contains("vector")){
            try{
                colorbar_file = lasBackendRequest.getChainedDataFile("colorbar");
                colorbar_URL = kmlBackendConfig.getHttpBaseURL() + "/" + colorbar_file.substring(colorbar_file.lastIndexOf(File.separator), colorbar_file.length());
            }catch(NullPointerException e){
                log.info("color bar is either not defined in operation or not created correctly");
            }

            if(colorbar_file != null && colorbar_file != ""){
                context.put("colorbar_file", colorbar_file);
            }

            if(colorbar_URL != null && colorbar_URL != ""){
                context.put("colorbar_URL", colorbar_URL);
            }
        }

        PrintWriter kmlWriter = null;
        try {
            kmlWriter = new PrintWriter(new FileOutputStream(new File(output)));
        }
        catch(Exception e) {
            // We need to package these and send them back to the UI.
        }
        ve.mergeTemplate(kml,"ISO-8859-1", context, kmlWriter);
        kmlWriter.close();
        lasBackendResponse.addResponseFromRequest(lasBackendRequest);
        return lasBackendResponse;
    } 

    private LASBackendResponse makePlacemarksKML(LASBackendRequest lasBackendRequest,VelocityContext context)
    throws Exception {
        LASBackendResponse lasBackendResponse = new LASBackendResponse();
        String output = lasBackendRequest.getResultAsFile("kml");
        //log.info("lasBackendRequest: "+lasBackendRequest.toString());
        String baseURL = kmlBackendConfig.getHttpBaseURL();

        String ferret_listing_file = lasBackendRequest.getChainedDataFile("ferret_listing");

        //get the kml template
        String kml = lasBackendRequest.getServiceAction();
        if (!kml.endsWith(".vm")) {
            kml = kml+".vm";
        }

        ArrayList allPlacemarks = new ArrayList();

        //read in the lat/lon of each grid point and create a placemark for it
        File f = new File(ferret_listing_file);
/*
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
*/
            //try different way to read
        BufferedReader in = new BufferedReader(new FileReader(f));
        String nextLine;

        HashMap<String, String> initLASReq = new HashMap<String, String>();

        String dsID = in.readLine();
        String varID= in.readLine();
        String view = in.readLine();

        initLASReq.put("dsID", dsID);
        initLASReq.put("varID", varID);
        initLASReq.put("view", view);

        if(view.contains("z")){
            String zlo  = in.readLine();
            String zhi  = in.readLine();
            initLASReq.put("zlo", zlo);
            initLASReq.put("zhi", zhi);
        }
       
        if(view.contains("t")){
            String tlo  = in.readLine();
            String thi  = in.readLine();
            initLASReq.put("tlo", tlo);
            initLASReq.put("thi", thi);
        }

        //read lines(lon, lat) from the file
        while (true){
            //nextLine =dis.readLine();
            nextLine =in.readLine();

            if (nextLine !=null){
                String gridPair = nextLine;
                gridPair = gridPair.trim();
                Pattern p = Pattern.compile("\\s");
                String[] gp = gridPair.split(p.pattern());
                String gridLon = gp[0];
                String gridLat = gp[gp.length-1];
               // log.info("lat="+gridLat+" lon="+gridLon);
                GEPlacemark pl = new GEPlacemark(gridLat,gridLon,initLASReq,lasBackendRequest,baseURL);
               // log.info("placemark "+pl.toString());
                allPlacemarks.add(pl);
            }else{
                break;
            }
        }    
        in.close();

        context.put("dsID",dsID);
        context.put("varID",varID);
        context.put("allPlacemarks", allPlacemarks);

        PrintWriter kmlWriter = null;
        try {
            kmlWriter = new PrintWriter(new FileOutputStream(new File(output)));
        }
        catch(Exception e) {
            // We need to package these and send them back to the UI.
        }
        ve.mergeTemplate(kml,"ISO-8859-1", context, kmlWriter);
        kmlWriter.close();
        lasBackendResponse.addResponseFromRequest(lasBackendRequest);

        return lasBackendResponse;
    }
}
