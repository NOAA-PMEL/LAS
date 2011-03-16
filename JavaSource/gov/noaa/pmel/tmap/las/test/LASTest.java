package gov.noaa.pmel.tmap.las.test;


import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASDocument;
import gov.noaa.pmel.tmap.las.ui.LASProxy;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.jdom.Element;



/**
 * This is the suite for testing LAS installations
 * @author Jing Yang Li
 *
 */
public class LASTest{

	private static LASProxy lasProxy = new LASProxy();
	
    private LASConfig lasConfig = new LASConfig();
    private LASTestOptions lto;
    
    public LASTest(LASTestOptions l){

    	lto = l;
        
        try{
        
        	String config = lasProxy.executeGetMethodAndReturnResult(l.getLAS()+"/getConfig.do?format=xml");
        	JDOMUtils.XML2JDOM(config, lasConfig);
        	
        	
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test datasets are alive or not
     *
     */
    public void testDataset(){
        LASDatasetTester ltd = new LASDatasetTester(lasConfig, lto);
        ltd.testDataset();
    }

    public void testFTDS(){
        LASDatasetTester ltd = new LASDatasetTester(lasConfig, lto);
        ltd.testFTDS();
    }

    /**
     * Test whether responses from product server are correct
     *
     */
    public void testResponse(){
        //LASResponseTester ltr = new LASResponseTester(las_config,las_operationsV7,las_options, lto);
        LASResponseTester ltr = new LASResponseTester(lasConfig, lto);
        ltr.testResponse();
    } 

    public void runTest(LASTestOptions lto){

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
    	LASCLIOptions cliOptions = new LASCLIOptions();
    	CommandLineParser parser = new GnuParser();
    	CommandLine cl = null;
    	int width = 100;
    	try {
			cl = parser.parse(new LASCLIOptions(), args, true);
		} catch (Exception e) {
			System.err.println( e.getMessage() );
			HelpFormatter formatter = new HelpFormatter();
        	formatter.setWidth(width);
        	formatter.printHelp("lasTest", cliOptions, true);
			System.exit(-1);

		}
        
        if ( cl == null ) {
        	HelpFormatter formatter = new HelpFormatter();
        	formatter.setWidth(width);
        	formatter.printHelp("lasTest", cliOptions, true);
        	System.exit(-1);
        }

        if ( cl.hasOption("h") || cl.hasOption("help") ) {
        	HelpFormatter formatter = new HelpFormatter();
        	formatter.setWidth(width);
        	formatter.printHelp("lasTest", cliOptions, true);
        	System.exit(0);
        }
        
        int cnt = 0;
        
    	//show DDS
        if ( cl.hasOption("D") ) {
        	lto.setDDS();
        	cnt++;
        }
    	
        //user defined view
        if ( cl.hasOption("v") ) {
        	lto.setView(cl.getOptionValue("v") );
        	cnt++;
        }
        
        if ( cl.hasOption("d") ) {
        	lto.setDataset(cl.getOptionValue("d"));
        	cnt++;
        }
    	
    	//all variables in each dataset
    	if( cl.hasOption("a") ){
            lto.setAllVariable();
            cnt++;
        }
    	
    	//exit on first error
    	if( cl.hasOption("e") ){
            lto.setExitFirst();
            cnt++;
        }
    	
        //only test dataset connection
    	if( cl.hasOption("c") ){
            lto.setConnectionOnly();
            cnt++;
        }
    	
        //only test product response
    	if( cl.hasOption("r") ){
            lto.setResponseOnly();
            cnt++;
        }
    	
        //verbose output of error message
        if( cl.hasOption("V") ){
            lto.setVerbose();
            cnt++;
        }

        //test URL of F-TDS
        if( cl.hasOption("f") ){
            lto.setTestFTDS();
            cnt++;
        }
        
        if ( cl.hasOption("l") ) {
        	lto.setLAS(cl.getOptionValue("l") );
        }

        //if no options being set; test opendap, ftds, and products 
        if(cnt == 0){lto.setTestAll();}

        LASTest lt = new LASTest(lto);
        lt.runTest(lto);
       
    }
}
