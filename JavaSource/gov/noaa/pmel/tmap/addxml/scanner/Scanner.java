package gov.noaa.pmel.tmap.addxml.scanner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import gov.noaa.pmel.tmap.addxml.ADDXMLProcessor;
import gov.noaa.pmel.tmap.addxml.CatalogRefHandler;
import gov.noaa.pmel.tmap.addxml.addXML;
import gov.noaa.pmel.tmap.las.jdom.LASDocument;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.jdom.Document;
import org.xml.sax.SAXException;

import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;


public class Scanner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ScannerOptions scannerOptions = new ScannerOptions();
		InvCatalogFactory catfactory = new InvCatalogFactory("default", false);
		String catalogURL = "";
	    String catalogName = "";
	    try {
	    	CommandLineParser parser = new GnuParser();
	        CommandLine line = parser.parse( scannerOptions, args );
	        String scan = line.getOptionValue("scan");
	        if ( scan == null ) {
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp("scanner", scannerOptions);
	        	System.exit(1);
	        }
		    String host = scan.substring(scan.indexOf("http://")+7);
		    String base = "http://"+host.substring(0, host.indexOf("/"));
			SAXParserFactory factory = SAXParserFactory.newInstance();
			CatalogRefHandler esgCatalogHandler = new CatalogRefHandler();
			SAXParser xmlparser = factory.newSAXParser();			
			try {
				xmlparser.parse(scan, esgCatalogHandler);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<String, String> catalogs = esgCatalogHandler.getCatalogs();
			ADDXMLProcessor addxml = new ADDXMLProcessor();
			addxml.setVerbose(true);
			HashMap<String, String> addxmlOptions = new HashMap<String, String>();
			addxmlOptions.put("force", "t");
			addxml.setOptions(addxmlOptions);
			for (Iterator catIt = catalogs.keySet().iterator(); catIt.hasNext();) {
				catalogName = (String) catIt.next();
				catalogURL = catalogs.get(catalogName);
				String filename;
				if ( !catalogURL.startsWith("http://") ) {
					filename = "LAS"+catalogURL.replaceAll("/", "-");
					catalogURL = base + catalogURL;
				} else {
					filename = "LAS-"+catalogURL.substring(catalogURL.indexOf("http://"+7)).replaceAll("/", "-");
				}
				System.out.println("Scanning "+catalogURL);
				
				File outfile = new File(filename);
				if ( !outfile.exists() ) {
					
					InvCatalog catalog = (InvCatalog) catfactory.readXML(catalogURL);
					LASDocument las = (LASDocument) addxml.createXMLfromTHREDDSCatalog(catalog);
					las.write(filename);
				}
			}
	    } catch( ParseException exp ) {
	        System.err.println( "Argument parsing failed.  Reason: " + exp.getMessage() );
	    } catch (ParserConfigurationException e) {
			System.err.println("Catalog parsing failed for: "+catalogURL);
		} catch (SAXException e) {
			System.err.println("Catalog parsing failed for: "+catalogURL);
		}
	    
	}
}
