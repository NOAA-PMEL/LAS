
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASMapScale;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.jdom.ServerConfig;
import gov.noaa.pmel.tmap.las.jdom.LASDocument;

//import gov.noaa.pmel.tmap.las.ui.state.NameValueBean;
import gov.noaa.pmel.tmap.las.ui.state.OptionBean;
import gov.noaa.pmel.tmap.las.ui.state.StateNameValueList;
import gov.noaa.pmel.tmap.las.ui.state.TimeSelector;

import gov.noaa.pmel.tmap.las.util.NameValuePair;
import gov.noaa.pmel.tmap.las.util.Dataset;
import gov.noaa.pmel.tmap.las.util.Variable;
import gov.noaa.pmel.tmap.las.util.TimeAxis;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.net.URLDecoder;
import java.net.*;

import ucar.nc2.*;
import ucar.nc2.dataset.*;
import dods.dap.*;

import dods.dap.parser.ParseException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * This class tests the responses form a LAS product server
 * @author Jing Yang Li
 *
 */
public class LASResponseTester{

    private LASConfig las_config;
    private LASDocument las_operationsV7;
    
    private LASTestOptions lto;

    //public LASResponseTester(LASConfig config, LASDocument operations, LASDocument options, LASTestOptions l){
    /**
     * Constructor
     */
    public LASResponseTester(LASConfig config, LASDocument operations, LASTestOptions l){
        las_config = config;
        las_operationsV7 = operations;
        lto = l;
    }

    /**
     * Test whether responses from product server are correct
     *
     */
    public void testResponse(){
        String dsID;
        String varID;
        String varpath;

        ArrayList<Dataset> datasets = new ArrayList<Dataset>();
        ArrayList<Variable> variables = new ArrayList<Variable>();

        String productServerURL;

        try{
            productServerURL = getProductServerURL(las_config);

            //get datasets
            Element datasetsE = las_config.getDatasetsAsElement();
            List datasetElements = datasetsE.getChildren("dataset");

            System.out.println("==== Product Server: "+ productServerURL);

            //loop over each dataset
            for(Iterator dsIt = datasetElements.iterator(); dsIt.hasNext();){
                //Dataset ds = (Dataset)dsIt.next();
                Element datasetE = (Element) dsIt.next();
                dsID = datasetE.getAttributeValue("ID");
                
                //get first variable of this dataset
                variables = las_config.getVariables(dsID);
                boolean allVar = lto.allVariable();
                
                if(!allVar){
                	Variable firstVar = variables.get(0);
                	varLASRequest(datasetE, firstVar);
                }else{
                	for(Iterator varIt = variables.iterator(); varIt.hasNext();){
                        Variable theVar = (Variable)varIt.next();
                        varLASRequest(datasetE,theVar);
                	}
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test the LAS response for a variable
     * @param dsE the dataset element
     * @param theVar the variable to test
     */
    public void varLASRequest(Element dsE, Variable theVar){
    	String dsID = dsE.getAttributeValue("ID");
        String dsName = dsE.getAttributeValue("name");
    	String varID = theVar.getID();

        //build XPath for this variable
        String varpath = "/lasdata/datasets/dataset[@ID='"+dsID+"']/variables/variable[@ID='"+varID+"']";

        try{
            //get the data URL
            String dsURL = las_config.getFullDataObjectURL(varpath);
            String userds = lto.getDataset();
            
            //if url is in format of ....xyz.nc#var
            if(dsURL.contains("#")){
                String[] tmp = dsURL.split("#");
                dsURL = tmp[0];
            }

            boolean isAvailable = false;
            //check if a remote dataset is available

            if(dsURL == null && dsURL == ""){
                System.out.println("The dataset URL is not valid.");
            }

            //check if a dataset is available
/*
            if( userds == null || userds==""){
                if(dsURL.contains("http")){
                    isAvailable = isAvailable(dsURL); //remote dataset
                }else{
                    isAvailable = true; //local dataset
                }
            }else{
                if(dsURL.contains(userds)){
                    isAvailable = isAvailable(dsURL);
                }
            }
*/
            //if(isAvailable){
            if( userds == null || userds=="" || dsURL.contains(userds) ){
                System.out.println("---- Dataset Name: "+ dsName);
        	System.out.println("     -- Dataset  URL: "+ dsURL);   

                if(dsURL.contains("http")){
                    isAvailable = isAvailable(dsURL); //remote dataset
                }else{
                    isAvailable = true; //local dataset
                }

                if(isAvailable){        
        	    System.out.println("        Variable: "+ theVar.getName());
   
            
        	    boolean hasZ = las_config.hasZ(varpath);            
        	    boolean hasT = las_config.hasT(dsID, varID);
            
        	String v = lto.getView();
                   
        	//by default, test each view           
        	if(v == null || v.equals("")){                

        	    //1D plots                
        	    System.out.print("         -- test x line plot: ");                
        	    makeLASRequest("x", dsID, varID);                    
        	    System.out.print("         -- test y line plot: ");
        	    makeLASRequest("y", dsID, varID);                
        	    if(hasZ){                    
        		System.out.print("         -- test z line plot: ");                    
        		makeLASRequest("z", dsID, varID);                
        	    }
              
        	    if(hasT){                
        		System.out.print("         -- test time series: ");
        		makeLASRequest("t", dsID, varID);
        	    }
                
        	    //2D plots                
        	    System.out.print("         -- test XY 2D  plot: ");
        	    makeLASRequest("xy", dsID, varID);                
        	    if(hasZ){                
        	        System.out.print("         -- test XZ 2D  plot: ");
        		makeLASRequest("xz", dsID, varID);      
        				
        		System.out.print("         -- test YZ 2D  plot: ");
        		makeLASRequest("yz", dsID, varID);
        				
        		if(hasT){
        		    System.out.print("         -- test ZT 2D  plot: ");
        		    makeLASRequest("zt", dsID, varID);
        		}
        	    }
                
                    if(hasT){                
        		System.out.print("         -- test XT 2D  plot: ");
        		makeLASRequest("xt", dsID, varID);
        		System.out.print("         -- test YT 2D  plot: ");
        		makeLASRequest("yt", dsID, varID);       				
        	    }
            
        	    }else{ 
                	
        	    //only make request for the plot defined by command-line parameter
        	    if( v.contains("z") && (!hasZ) ){
        		System.out.println("       -- No Z axis!");
        	    }else if( v.contains("t") && (!hasT) ){    	    	
        		System.out.println("       -- No T axis!");
        	    }else{               	            	    
        		System.out.print("        -- test "+v+" plot: ");
        		makeLASRequest(v, dsID, varID);                
        	    }            
        	    }
                }else{
                    System.out.println("        ******** WARNING ******** The dataset is not available");
                }
                System.out.println("");
            }	    
          /*    
            }else{
                if( userds == null || userds==""){
                    System.out.println("---- Dataset Name: "+ dsName);
                    System.out.println("     -- Dataset  URL: "+ dsURL);
                    System.out.println("        ******** WARNING ******** The dataset is not available");
                }else{
                    if(dsURL.contains(userds)){
                        System.out.println("---- Dataset Name: "+ dsName);
                        System.out.println("     -- Dataset  URL: "+ dsURL);
                        System.out.println("     ******** WARNING ******** The dataset is not available");
                    }
                }
            }
           */
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Check if a dataset with the given URL is available
     * @param url the URL of the dataset
     * @return isAvailable
     */
    public boolean isAvailable(String url) throws Exception{
        boolean isAvailable = false;
        try{
            DConnect dc = new DConnect(url);
            DDS mydds = dc.getDDS();

            if(mydds != null){
                isAvailable = true;
            }else{
                isAvailable = false;
            }
        }catch (MalformedURLException e){
            //java.net.MalformedURLException - if the URL given to the constructor has an error
            isAvailable = false;
        }catch (IOException e){
            //java.io.IOException - if an error connecting to the remote server
            isAvailable = false;
        }catch (ParseException e){
            //dods.dap.parser.ParseException - if the DDS parser returned an error
            isAvailable = false;
        }catch (DDSException e){
            //dods.dap.DDSException - on an error constructing the DDS
            isAvailable = false;
        }catch (DODSException e){
            //dods.dap.DODSException - if an error returned by the remote server
            isAvailable = false;
        }catch (Exception e){
            //e.printStackTrace(); 
            isAvailable = false;
        }

        return isAvailable;
    }    
    /**
     * Test LAS request for 1D and 2D plots
     * @param viewtype view type of the plot
     * @param dsID dataset ID
     * @param varID variable ID
     */
    public void makeLASRequest(String viewtype, String dsID, String varID){
        String requestURL ="";
        String serverURL;
        String varpath = "/lasdata/datasets/dataset[@ID='"+dsID+"']/variables/variable[@ID='"+varID+"']";
        LASUIRequest lr = new LASUIRequest();

        lr = buildLASUIRequest(viewtype,dsID,varID);
        serverURL = getProductServerURL(las_config);
        try{
            requestURL = serverURL+"?xml=" + lr.toEncodedURLString()+"&debug=true"; 
        } catch (Exception e){
             e.printStackTrace();
        }

        boolean inProgress = true;
        int noProgress = 0;
        while(inProgress && (noProgress < 2)){
            try{
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
                int size = sbuf.length();
            
                if(sbuf.toString().contains("error")){
                    String debugFile = extractDebugFile(sbuf.toString());
                    System.out.println("              ---------- ERROR !!!");
                    if(debugFile !=null && debugFile !=""){
                        System.out.println("        The debug file is "+ debugFile);
                    }
                    //print the whole error message
                    if(lto.isVerbose() || debugFile == null || debugFile == ""){
                 	System.out.println(sbuf.toString());
                    } 
                    inProgress = false;
                    if(lto.exitFirst()){System.exit(0);}
                }else if(sbuf.toString().contains("Progress") || sbuf.toString().contains("progress")){
                    inProgress = true;
                    noProgress++;
                    System.out.println("The response is:");
                    System.out.println(sbuf.toString());
                }else{//correct response (hope so!)
                    inProgress = false;
                    System.out.println("              ---------- PASS!");
                }
            }catch (Exception e){
                System.out.println("error in making request to product server");
                e.printStackTrace();
                break;
            }
        }
        //if the request was sent more than 3 times; kill it
        //it may be taking too long or there are errors (e.g. Ferret script hangs)
        if(inProgress && (noProgress == 2)){
            System.out.println("----------------------------cancel request");
            //send cancel request to ProductServer
            try{
                URL url = new URL(requestURL+"&cancel=Cancel");
                URLConnection conn = url.openConnection();
                conn.connect();
                System.out.println("The request either takes too long or may have error --- cancel it!");
            }catch (IOException e){
               System.out.println("error in canceling request");
            }
        }
    }

    /**
    * Extract the debug file from the error message
    * @param errMsg the error message returned from LAS
    */
    private String extractDebugFile(String errMsg){
        //System.out.println(errMsg);
        String debugFile="";
        int i1 = errMsg.indexOf("debug.txt");
        if(i1 > 0){
            String s1 = errMsg.substring(0,i1+9);
            int i2 = s1.lastIndexOf("http");
            if(i2>0){debugFile = s1.substring(i2);}
        } 
        return debugFile;
    }

    /**
     * Build a LASUIRequest for 1D and 2D plots
     * @param viewtype view type of the plot
     * @param dsID dataset ID
     * @param varID variable ID
     */
    public LASUIRequest buildLASUIRequest(String viewtype, String dsID, String varID){
        ArrayList<OptionBean> options = null;

        LASUIRequest lr = new LASUIRequest();
        lr.addVariable(dsID,varID);

        if(viewtype.length() == 1){
            lr.setOperation("Plot_1D");
            //options = extractOptions("Options_1D");
            options = makeOptions(viewtype);
            lr.setOptions("ferret", options);
            lr.setProperty("ferret","line_or_sym","default");
            lr.setProperty("ferret","line_color","default");
            lr.setProperty("ferret","line_thickness","default");
        }else if(viewtype.length() == 2){
            if(viewtype.equals("xy")){
            	lr.setOperation("Plot_2D_XY");
            }else{
            	lr.setOperation("Plot_2D");
            }
            //options = extractOptions("Options_2D");
            options = makeOptions(viewtype);
            lr.setOptions("ferret", options);
            lr.setProperty("ferret","palette","default");
        }

        HashMap<String, HashMap<String,String[]>> region = new HashMap<String, HashMap<String,String[]>>();

        region = makeRegion(viewtype, dsID, varID);
        if(region != null){lr.setRegion(region);}

        return lr;
    }

    /**
     * Make a region for a plot
     * @param viewtype view type of the plot
     * @param dsID dataset ID
     * @param varID variable ID
     */
    public ArrayList<OptionBean> makeOptions(String viewtype){
        ArrayList<OptionBean> options = new ArrayList<OptionBean>();

        OptionBean opb1 = new OptionBean();
        opb1.setWidget_name("interpolate_data");
        opb1.setValue("false");
        options.add(opb1);

        OptionBean opb2 = new OptionBean();
        opb2.setWidget_name("image_format");
        opb2.setValue("default");
        options.add(opb2);

        OptionBean opb3 = new OptionBean();
        opb3.setWidget_name("size");
        opb3.setValue(".5");
        options.add(opb3);
 
        OptionBean opb4 = new OptionBean();
        opb4.setWidget_name("use_ref_map");
        opb4.setValue("default");
        options.add(opb4);

        OptionBean opb5 = new OptionBean();
        opb5.setWidget_name("use_graticules");
        opb5.setValue("default");
        options.add(opb5);

        OptionBean opb6 = new OptionBean();
        opb6.setWidget_name("no_margins");
        opb6.setValue("default");
        options.add(opb6);

        OptionBean opb7 = new OptionBean();
        opb7.setWidget_name("deg_min_sec");
        opb7.setValue("default");
        options.add(opb7);

        OptionBean opb8 = new OptionBean();
        opb8.setWidget_name("view");
        opb8.setValue(viewtype);
        options.add(opb8);

        return options;
    } 
    /**
     * Make a region for a plot
     * @param viewtype view type of the plot
     * @param dsID dataset ID
     * @param varID variable ID
     */
    public HashMap<String, HashMap<String,String[]>> makeRegion(String viewtype, String dsID, String varID){

        String xLo = "";
        String xHi = "";
        String yLo = "";
        String yHi = "";
        String zLo = "";
        String zHi = "";
        String tLo = "";
        String tHi = "";
        boolean hasZ = false;
        boolean hasT = false;
        DateTime lodt=null;
        DateTime hidt=null;
        
        HashMap<String, HashMap<String,String[]>> region = new HashMap<String, HashMap<String,String[]>>();
        HashMap<String,String[]> points = new HashMap<String,String[]>();
        HashMap<String,String[]> intervals = new HashMap<String,String[]>();

        String varpath = "/lasdata/datasets/dataset[@ID='"+dsID+"']/variables/variable[@ID='"+varID+"']";

        try{
            //get the end points of each axis
            xLo = las_config.getLo("x",varpath);
            xHi = las_config.getHi("x",varpath);
            yLo = las_config.getLo("y",varpath);
            yHi = las_config.getHi("y",varpath);
            if(las_config.hasZ(varpath)){
                hasZ = true;
                zLo = las_config.getLo("z",varpath);
                zHi = las_config.getHi("z",varpath);
            }
            //time axis
            if(las_config.hasT(dsID, varID)){
                hasT = true;

                //get time format for this variable defined in dataset configuration file
                tLo = las_config.getLo("t",varpath);
                LASDateFormat ldf = new LASDateFormat(tLo);
                String tFormat = ldf.getDateFormat();

                DateTimeFormatter fmt = DateTimeFormat.forPattern(tFormat).withZone(DateTimeZone.UTC);
	        DateTimeFormatter ferretfmt = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss").withZone(DateTimeZone.UTC);

                lodt = fmt.parseDateTime(tLo);   
                tLo = lodt.toString(ferretfmt);
                
                LASTimeAxis lta = getLASTimeAxis(varpath);
                lta.setLoDateTime(lodt);                
                String time_style = lta.getStyle();
                if(time_style.equals("arange")){                   
                	hidt = lta.getHiDateTime();   
                	tHi = hidt.toString(ferretfmt);
                }else if(time_style.equals("v")){                    
                    tHi = las_config.getHi("t",varpath);
                    hidt= fmt.parseDateTime(tHi); 
                    tHi = hidt.toString(ferretfmt);
                }
            }
        }catch (Exception e){
             e.printStackTrace();
        }

        //make a x interval or point
        if(viewtype.contains("x")){
            //make a small x interval in case it's a large dataset
            String[] x = new String[2];
            double lox = Double.valueOf(xLo).doubleValue();
            double hix = Double.valueOf(xHi).doubleValue();
            double dx = hix - lox;
            x[0] = xLo;
            x[1] = Double.toString(lox+(dx/4));
            intervals.put("x",x);
        }else{
            //make a x point
            String[] x = new String[1];
            x[0] =  xLo;
            points.put("x",x);
        }

        //make a y interval or point
        if(viewtype.contains("y")){
            //make a small y interval in case it's a large dataset
            String[] y = new String[2];
            double loy = Double.valueOf(yLo).doubleValue();
            double hiy = Double.valueOf(yHi).doubleValue();
            double dy = hiy - loy;
            y[0] =  yLo;
            y[1] =  Double.toString(loy+(dy/4));
            intervals.put("y",y);
        }else{
            //make a y point
            String[] y = new String[1];
            y[0] =  yLo;
            points.put("y",y);
        }

        //make a z interval or point
        if(hasZ){
            if(viewtype.contains("z")){
                //make a z interval
                String[] z = new String[2];
                z[0] =  zLo;
                z[1] =  zHi;
                intervals.put("z",z);
            }else{
                //make a z point
                String[] z = new String[1];
                z[0] =  zLo;
                points.put("z",z);
            }
        }

        //make a t interval or point
        if(hasT){
            if(viewtype.contains("t")){
                //make a t interval
                String[] t = new String[2];
                t[0] =  tLo;
                t[1] =  tHi;
                intervals.put("t",t);
            }else{
                //make a t point
                String[] t = new String[1];
                t[0] = tLo;
                points.put("t",t);
            }
        }

        region.put("intervals", intervals);
        region.put("points", points);

        return region;
    }

    public LASTimeAxis getLASTimeAxis(String varpath){
        LASTimeAxis lta = new LASTimeAxis();

        String timeStyle = null;
        try{
            if (!varpath.contains("@ID")) {
                String[] parts = varpath.split("/");
                // Throw away index 0 since the string has a leading "/".
                varpath = "/"+parts[1]+"/"+parts[2]+"/dataset[@ID='"+parts[3]+"']/"+parts[4]+"/variable[@ID='"+parts[5]+"']";
            }
            Element variable = las_config.getElementByXPath(varpath);
            if (variable == null) {
                return null;
            }
            String gridID = variable.getChild("grid").getAttributeValue("IDREF");
            Element grid = las_config.getElementByXPath("/lasdata/grids/grid[@ID='"+gridID+"']");
            if (grid == null) {
            //System.out.println("grid");
                return null;
            }
            List axes = grid.getChildren("axis");
            for (Iterator axIt = axes.iterator(); axIt.hasNext();) {
                Element axis = (Element) axIt.next();
                String axisID = axis.getAttributeValue("IDREF");
                axis = las_config.getElementByXPath("/lasdata/axes/axis[@ID='"+axisID+"']");
                String t = axis.getAttributeValue("type");
       
                if ( t.equals("t") ) {
                    lta.setUnit(axis.getAttributeValue("units"));
                    Element arange = axis.getChild("arange");
                    if (arange == null) {
                        List v = axis.getChildren("v");
                        if(v != null){
                            timeStyle = "v";
                            lta.setStyle("v");
                        }
                    } else {
                        timeStyle = "arange";
                        lta.setStyle("arange");
                        double size = Double.valueOf(arange.getAttributeValue("size")).doubleValue();
                        double step = Double.valueOf(arange.getAttributeValue("step")).doubleValue();
                        lta.setSize(size);
                        lta.setStep(step);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();    
        }
        return lta;
    }

    /**
     * Get URL of product server, which is specfied in las.xml
     * @param las_config JDOM object LASConfig for this installation
     * @return productServerURL
     */
    public String getProductServerURL(LASConfig las_config){
        String productServerURL = null;
        Element operations =  las_config.getRootElement().getChild("operations");
        productServerURL = operations.getAttributeValue("url");
        return productServerURL;
    }
}
