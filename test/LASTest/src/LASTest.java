
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
    private LASDocument las_test_config;
    //private LASDocument las_ui;
    //private LASDocument las_options;
    
    private LASTestOptions lto;
    
    public LASTest(LASTestOptions l){

    	lto = l;
    	
        las_config = new LASConfig();
        las_operationsV7 = new LASDocument();
        las_test_config = new LASDocument();
        try{
            File file = new File("las_test_config.xml");
            JDOMUtils.XML2JDOM(file, las_test_config);
            //System.out.println(las_test_config.toCompactString());
            Element lasv7E = las_test_config.getElementByXPath("lasTest/lasv7");
            String lasv7xml = lasv7E.getAttributeValue("dir")+"/lasV7.xml";
            //System.out.println(lasv7xml);
 
            //File file1 = new File("../../conf/server/las.xml");
            //File file1 = new File("/home/porter/jing/tomcat/apache-tomcat-5.5.25/webapps/las/output/lasV7.xml");
            File file1 = new File(lasv7xml);
            JDOMUtils.XML2JDOM(file1, las_config);
            //las_config.convertToSeven();////convert to  'version 7'

            File file2 = new File("../../conf/server/operationsV7.xml");
            JDOMUtils.XML2JDOM(file2, las_operationsV7);

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

    public void testFTDS(){
        LASDatasetTester ltd = new LASDatasetTester(las_config, lto);
        ltd.testFTDS();
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
        //lto.showOptions();

    	//showing usage of test code
    	if(lto.showHelp()){
    		lto.showUsage2();
    	}
    	
    	//test OPeNDAP URLs 
    	if(lto.testConn()){
            System.out.println("==== LAS test: Are the datasets alive? =================");
            testDataset();
        }
        System.out.println();
        //test F-TDS URLs 
        if(lto.testFTDS()){
            System.out.println("==== LAS test: Are the FTDS URLs working? =================");
            testFTDS();
        }
        System.out.println();
        //test product response
        if(lto.testResp()){
            System.out.println("==== LAS test: Are the product reponses correct? =======");
            testResponse();
        }
    }
    public static void main(String [] args){

    	LASTestOptions lto = new LASTestOptions();
    	
        int cnt = 0; //count number of options being set

    	//show DDS
    	if( !args[0].equals("${dds}")){
    	    lto.setDDS();
            cnt++;
    	}

    	//user defined view
    	if( !(args[1].equals("${v}")) ){
            lto.setView(args[1]);
            cnt++;
        }
    	
    	//dataset pattern
    	if(!(args[2].equals("${d}"))){
            lto.setDataset(args[2]);
            cnt++;
        }
    	
    	//all variables in each dataset
    	if(!(args[3].equals("${a}"))){
            lto.setAllVariable();
            cnt++;
        }
    	
    	//exit on first error
    	if(!(args[4].equals("${e}"))){
            lto.setExitFirst();
            cnt++;
        }
    	
        //show help
    	if(!(args[5].equals("${h}"))){
            lto.setHelp();
            cnt++;
        }
    	
        //only test dataset connection
    	if(!(args[6].equals("${c}"))){
            lto.setConnectionOnly();
            cnt++;
        }
    	
        //only test product response
    	if(!(args[7].equals("${r}"))){
            lto.setResponseOnly();
            cnt++;
        }
    	
        //verbose output of error message
        if(!(args[8].equals("${vb}"))){
            lto.setVerbose();
            cnt++;
        }

        //test URL of F-TDS
        if(!(args[9].equals("${f}"))){
            lto.setTestFTDS();
            cnt++;
        }

        //if no options being set; test opendap, ftds, and products 
        if(cnt == 0){lto.setTestAll();}

        LASTest lt = new LASTest(lto);
        lt.runTest(lto);
    }
}
