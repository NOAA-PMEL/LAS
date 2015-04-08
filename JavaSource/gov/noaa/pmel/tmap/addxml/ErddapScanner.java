package gov.noaa.pmel.tmap.addxml;

import gov.noaa.pmel.tmap.las.proxy.LASProxy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import opendap.dap.Attribute;
import opendap.dap.AttributeTable;
import opendap.dap.DAP2Exception;
import opendap.dap.DAS;
import opendap.dap.NoSuchAttributeException;
import opendap.dap.parsers.ParseException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;


public class ErddapScanner {

    protected static String url = "http://osmc.noaa.gov/erddap/tabledap/";
    protected static String id = "OSMCV4_DUO_SURFACE_TRAJECTORY";
    protected static String search = "index.json?page=1&itemsPerPage=1000";
    protected static boolean verbose = false;
    protected static String title = null;

    protected static ErddapScannerOptions opts = new ErddapScannerOptions();
    protected static CommandLine cl;

    protected static LASProxy lasProxy = new LASProxy();
    
    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            CommandLineParser parser = new GnuParser();
            
            cl = parser.parse(opts, args);
            url = cl.getOptionValue("url");
            id = cl.getOptionValue("id");
            verbose = cl.hasOption("verbose");
            title = cl.getOptionValue("title");
            if ( !url.endsWith("/") ) {
                url = url+"/";
            }
            String skipaxes = cl.getOptionValue("axes");
            List<String> axesToSkip = new ArrayList<String>();
            if ( skipaxes != null ) {
                // This is regex magic to split on empty strings, that follow the start of the string.
                axesToSkip = Arrays.asList(skipaxes.split("(?!^)"));
            }
            ErddapProcessor processor = new ErddapProcessor(axesToSkip);
            if ( id != null ) {
            	// Process the given data set.
            	processor.process(url, id, false, verbose);
            } else {
            	String s = url+search;
            	InputStream stream = lasProxy.executeGetMethodAndReturnStream(s, null);
            	InputStreamReader reader = new InputStreamReader(stream);
            	JsonStreamParser jp = new JsonStreamParser(reader);
            	JsonObject tabledap_list = (JsonObject) jp.next();
            	JsonArray rows = (JsonArray) ((JsonObject) (tabledap_list.get("table"))).get("rows");
            	File categoriesFile = new File("las_categories.xml");
            	CategoryBean topCat = new CategoryBean();
            	topCat.setID("erddap_cats");
            	if ( title == null ) title = "Data from ERDDAP";
            	topCat.setName(title);
            	// The first one is a listing of all data sets, not the first tabledap data set.
            	Vector cats = new Vector();
            	// int limit = rows.size();
            	int limit = 10; //DEBUG
            	for (int i = 1; i < limit; i++) {
					JsonArray row = (JsonArray) rows.get(i);
					String fullurl = row.get(2).getAsString();
					String title = row.get(6).getAsString();
					
					String u = fullurl.substring(0, fullurl.lastIndexOf("/"));
					String uid = fullurl.substring(fullurl.lastIndexOf("/")+1);
					
					
                    
					boolean write = processor.process(u, uid, true, verbose);
					// Only include the XML if it looks like it's fully specified.
					if ( write ) {
						CategoryBean cat = new CategoryBean();
						cat.setName(title);
						cat.setID("cat_"+uid);
						
						FilterBean filter = new FilterBean();
	                    filter.setAction("apply-dataset");
	                    filter.setContainstag(uid);
	                    cat.addFilter(filter);
	                    cats.add(cat);
	                    
	                    topCat.setCategories(cats);
	                    // For now write it every time we can stop it and still use it.
	                    Element lc = new Element("las_categories");
	                    lc.addContent(topCat.toXml());
	                    processor.outputXML(categoriesFile, lc, false);
	                    
					}
					
				}
            	
            }
            
        } catch (Exception e) {
        	String header = "An error occurred with the command line processing\n";
        	String footer = "";
        	HelpFormatter formatter = new HelpFormatter();
        	formatter.printHelp("addDiscrete", header, opts, footer, true);
        }
    }
}
