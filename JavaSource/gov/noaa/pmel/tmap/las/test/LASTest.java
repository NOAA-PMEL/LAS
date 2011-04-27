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
    
    public LASTest(LASTestOptions l, LASConfig c) {
    	lto = l;
    	lasConfig = c;
    }
    public LASTest(LASTestOptions l){

    	lto = l;
    	String url = l.getLAS();
    	if ( !url.startsWith("http://") ) {
    		url = "http://"+url;
    	}
    	if ( !url.endsWith("/") ) {
    		url = url+"/";
    	}
    
        try{
        	     	
        	String config = lasProxy.executeGetMethodAndReturnResult(url+"getConfig.do?format=xml");
        	JDOMUtils.XML2JDOM(config, lasConfig);
        	
        } catch (Exception e){
            
        	try {
        		String config = lasProxy.executeGetMethodAndReturnResult(url+"/output/lasV7.xml");
        		JDOMUtils.XML2JDOM(config, lasConfig);
        	} catch (Exception exp){
        		System.err.println("Unable to get configuration information from "+l.getLAS()+".  Is the server running?");
        		System.exit(-1);
        	}
        	
        }
    }
    public void runTest(LASTestOptions lto, boolean web_output){

    	//test OPeNDAP URLs 
    	if(lto.testConn()){
    		if ( !web_output ) System.out.println("==== LAS test: Are the datasets alive? =================");
    		LASDatasetTester ltd = new LASDatasetTester(lasConfig, lto);
    	    ltd.testDataset(web_output);
        }
    	if ( !web_output ) System.out.println();
        //test F-TDS URLs 
        if(lto.testFTDS()){
        	if ( !web_output ) System.out.println("==== LAS test: Are the FTDS URLs working? =================");
        	 LASDatasetTester ltd = new LASDatasetTester(lasConfig, lto);
             ltd.testFTDS(web_output);
        }
        if ( !web_output ) System.out.println();
        //test product response
        if(lto.testResp()){
        	if ( !web_output ) System.out.println("==== LAS test: Are the product reponses correct? =======");
        	LASResponseTester ltr = new LASResponseTester(lasConfig, lto);
            ltr.testResponse(web_output);
        }
    }
    public void runTest(LASTestOptions lto) {
    	runTest(lto, false);
    }
    public static void main(String [] args){

    	LASTestOptions lto = new LASTestOptions();
    	LASCLIOptions cliOptions = new LASCLIOptions();
    	CommandLineParser parser = new GnuParser();
    	CommandLine cl = null;
    	int width = 110;
    	
    	System.setProperty("log4j.logger.org.apache.http", "ERROR");
    	System.setProperty("log4j.logger.org.apache.http.wire", "ERROR");


    	
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
