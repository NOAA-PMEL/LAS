package gov.noaa.pmel.tmap.addxml.scanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

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
import org.jdom.Element;
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
	        String regex = line.getOptionValue("regex");
	        boolean noregex = false;
	        if ( regex == null || regex.equals("") ) noregex = true;
	        if ( scan == null ) {
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp("scanner", scannerOptions);
	        	System.exit(1);
	        }
		    String host = scan.substring(scan.indexOf("http://")+7);
		    String base = "http://"+host.substring(0, host.lastIndexOf("/"));
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
			addxmlOptions.put("oneDataset", "true");
			addxmlOptions.put("category", "false");
			addxmlOptions.put("esg", "true");
			addxml.setOptions(addxmlOptions);
			for (Iterator catIt = catalogs.keySet().iterator(); catIt.hasNext();) {
				catalogName = (String) catIt.next();
				catalogURL = catalogs.get(catalogName);
				String filename;
				if ( !catalogURL.startsWith("http://") ) {
					filename = "LAS-"+catalogURL.replaceAll("/", "-");
					catalogURL = base + "/" + catalogURL;
				} else {
					filename = "LAS-"+catalogURL.substring(catalogURL.indexOf("http://"+7)).replaceAll("/", "-");
				}
				System.out.println("Scanning "+catalogURL);
				
				File outfile = new File(filename);
				if ( !outfile.exists() ) {
					if ( noregex || Pattern.matches(regex, catalogURL)) {
						InvCatalog catalog = (InvCatalog) catfactory.readXML(catalogURL);
						LASDocument las = (LASDocument) addxml.createXMLfromTHREDDSCatalog(catalog);
						
						// I just want the datasets, grids and axes stubs written to a file...
						Element datasets = (Element) las.getRootElement().getChild("datasets").clone();
						LASDocument dsDoc = new LASDocument(datasets);
						Element grids = (Element) las.getRootElement().getChild("grids").clone();
						LASDocument gridsDoc = new LASDocument(grids);
						Element axes = (Element) las.getRootElement().getChild("axes").clone();
						LASDocument axesDoc = new LASDocument(axes);						
						FileWriter xmlout = new FileWriter(filename);
						String dsout = dsDoc.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
						String gout = gridsDoc.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
						String aout = axesDoc.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
						xmlout.write(dsout+gout+aout);
					}
				}
			}
	    } catch( ParseException exp ) {
	        System.err.println( "Argument parsing failed.  Reason: " + exp.getMessage() );
	    } catch (ParserConfigurationException e) {
			System.err.println("Catalog parsing failed for: "+catalogURL);
		} catch (SAXException e) {
			System.err.println("Catalog parsing failed for: "+catalogURL);
		} catch (IOException e) {
			System.err.println("File writing failed for: "+catalogURL);
		}
	    
	}
}
