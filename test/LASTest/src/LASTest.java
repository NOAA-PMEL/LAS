
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASMapScale;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.jdom.ServerConfig;
import gov.noaa.pmel.tmap.las.jdom.LASDocument;

import org.jdom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.net.URLDecoder;
import java.net.*;

/**
 * This is the suite for testing LAS installations
 * @author Jing Yang Li
 *
 */
public class LASTest{

    private LASConfig las_config;
    private LASDocument las_operationsV7;
    //private LASDocument las_ui;
    //private LASDocument las_options;
    
    private LASTestOptions lto;
    
    public LASTest(LASTestOptions l){

    	lto = l;
    	
        las_config = new LASConfig();
        las_operationsV7 = new LASDocument();
        //las_ui = new LASDocument();
        //las_options = new LASDocument();

        try{
            //File file1 = new File("/home/porter/kobrien/gfdl/armstrong/conf/server/las.xml");
            File file1 = new File("../../conf/server/las.xml");
            //for run in eclipse
            //File file1 = new File("Z:/armstrong/LASTest/xml/las.xml");
            JDOMUtils.XML2JDOM(file1, las_config);
            //convert to  'version 7'
            las_config.convertToSeven();
            //System.out.println(las_config.toString());

            //File file2 = new File("/home/porter/kobrien/gfdl/armstrong/conf/server/operationsV7.xml");
            File file2 = new File("../../conf/server/operationsV7.xml");
            //File file2 = new File("Z:/armstrong/LASTest/xml/operationsV7.xml");
            JDOMUtils.XML2JDOM(file2, las_operationsV7);
            //System.out.println(las_operationsV7.toString());

            //File file3 = new File("LASTest/xml/options.xml");
            //File file3 = new File("Z:/armstrong/LASTest/xml/options.xml");
            //JDOMUtils.XML2JDOM(file3, las_options);
            //System.out.println(las_options.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test datasets are alive or not
     *
     */
    public void testDataset(){
        LASDatasetTester ltd = new LASDatasetTester(las_config, lto);
        ltd.testDataset();
    }
    
    /**
     * Test whether responses from product server are correct
     *
     */
    public void testResponse(){
        //LASResponseTester ltr = new LASResponseTester(las_config,las_operationsV7,las_options, lto);
        LASResponseTester ltr = new LASResponseTester(las_config,las_operationsV7,lto);
        ltr.testResponse();
    } 

    public void runTest(LASTestOptions lto){
    	//showing usage of test code
    	if(lto.showHelp()){
    		lto.showUsage();
    	}
    	
    	//test data connection
    	if(lto.testConn()){
            System.out.println("==== LAS test: Are the datasets alive? =================");
            testDataset();
        }
        System.out.println();
        
        //test product response
        if(lto.testResp()){
            System.out.println("==== LAS test: Are the product reponses correct? =======");
            testResponse();
        }
    }
    public static void main(String [] args){

    	System.out.println(args[0]);
    	System.out.println(args[1]);
    	System.out.println(args[2]);
    	LASTestOptions lto = new LASTestOptions();
    	
    	//show DDS
    	if( !args[0].equals("${dds}")){
    	    lto.setDDS();
    	}
    	//user defined view
    	if( !(args[1].equals("${v}")) ){lto.setView(args[1]);}
    	
    	//dataset pattern
    	if(!(args[2].equals("${d}"))){lto.setDataset(args[2]);}
    	
    	//all variables in each dataset
    	if(!(args[3].equals("${a}"))){lto.setAllVariable();}
    	
    	//exit on first error
    	if(!(args[4].equals("${e}"))){lto.setExitFirst();}
    	
        //show help
    	if(!(args[5].equals("${h}"))){lto.setHelp();}
    	
        //only test dataset connection
    	if(!(args[6].equals("${c}"))){lto.setConnectionOnly();}
    	
        //only test product response
    	if(!(args[7].equals("${r}"))){lto.setResponseOnly();}
    	
        //verbose output of error message
        if(!(args[8].equals("${vb}"))){lto.setVerbose();}

        LASTest lt = new LASTest(lto);
        lt.runTest(lto);
        
        //if(lto.testConn()){
          //  System.out.println("==== LAS test: Are the datasets alive? =================");
          //  lt.testDataset();
        //}
        //System.out.println();
        
        //if(lto.testResp()){
          //  System.out.println("==== LAS test: Are the product reponses correct? =======");
          //  lt.testResponse();
        //}
    }
}
