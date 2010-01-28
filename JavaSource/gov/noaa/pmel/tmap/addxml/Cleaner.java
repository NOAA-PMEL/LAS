package gov.noaa.pmel.tmap.addxml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.jdom.output.XMLOutputter;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogConvertIF;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDatasetImpl;
import thredds.catalog.ServiceType;

public class Cleaner {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
	public static final String OUTPUT_REFERENCES_CATALOGS = "catalogs";
	public static final String OUTPUT_REFERENCES_DATASETS = "datasets";

	public static final String INPUT_TYPE_CLIENT = "client";
	public static final String INPUT_TYPE_SERVER = "server";

	private static InvCatalogFactory factory = new InvCatalogFactory("default", true);
	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		if ( args[0] == null || args[0].equals("") ) {
			error("Cleaner catalog.xml true|false catalogs|datasets (file to clean and whether to make aggregation ncML, weather to write catalogRefs or individual datasets.", 0);
		}
        String data = args[0];
		
		Document uaf = new Document();

		boolean aggregations = false;
		if ( args.length > 1 ) {
			if ( args[1] != null && (args[1].equals("true") || args[1].equals("false")) ) {
				aggregations = Boolean.valueOf(args[1]);
			}
		}

		String refs = "catalogs";
		if ( args.length > 2 ) {
			if ( args[2] != null && (args[2].equalsIgnoreCase(OUTPUT_REFERENCES_CATALOGS) || args[2].equalsIgnoreCase(OUTPUT_REFERENCES_DATASETS)) ) {
				refs = args[2];
			}
		}

		String type = "server";
		if ( args.length > 3 ) {
			if ( args[3] != null ) {
				type = args[3];
			}
		}
		Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
		Namespace thredds = Namespace.getNamespace("http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0");
		if ( type.equals("server") ) {
			try {
				File source = new File(data);
				JDOMUtils.XML2JDOM(source, uaf);
			} catch (Exception e) {
				error("Trouble reading source catalog: " + e.getMessage(), 0);
			}
			String stop_string = "";
			Set<String> skip = new HashSet<String>();
			
			for (Iterator catalogRefs = uaf.getDescendants(new CatalogRefFilter()); catalogRefs.hasNext();) {
				Element catalogRef = (Element) catalogRefs.next();
				String catalog_url = catalogRef.getAttributeValue("href", xlink).trim();
				List<Element> properties = catalogRef.getChildren("property", thredds);
				for (Iterator propIt = properties.iterator(); propIt.hasNext();) {
					Element property = (Element) propIt.next();
					String name = property.getAttributeValue("name");
					String value = property.getAttributeValue("value");
					if (name.equalsIgnoreCase("stop")) {
						stop_string = value;
					} else if (name.equalsIgnoreCase("skip")) {
						skip.add(value);
					}
				}

				
				String f = getOutputFile(catalog_url);

				InvCatalogImpl clean = cleanCatalog(catalog_url, aggregations, refs, skip, stop_string);

				if (clean != null) {
					catalogRef.setAttribute("href",	"geoIDECleanCatalogs/" + f, xlink);
					writeCleanCatalog(clean, f);
				}
			}
			try {
				File file = new File("geoIDECleanCatalog.xml");
				FileWriter xmlout = new FileWriter(file);
				org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
				format.setLineSeparator(System.getProperty("line.separator"));
				XMLOutputter outputter = new XMLOutputter(format);
				outputter.output(uaf, xmlout);
				// Close the FileWriter
				xmlout.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Set<String> skip = new HashSet<String>();
			String stop_string = "";
			InvCatalogImpl clean = cleanCatalog(data, aggregations, refs, skip, stop_string);
			String f = getOutputFile(data);
			writeCleanCatalog(clean, f);
		}
	}
	private static void writeCleanCatalog(InvCatalogImpl clean, String f) {
		try {
			// Write to a file...			
			File file = new File(f);
			FileOutputStream out = new FileOutputStream(file);
			factory.writeXML(clean, out, true);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static String getOutputFile(String catalog_url) {
		String f = "";
		try {
			f = JDOMUtils.MD5Encode(catalog_url) + ".xml";
		} catch (UnsupportedEncodingException e) {
			error("Cannot create sub-catalog file name." + catalog_url + "\n",	1);
			e.printStackTrace();
		}
		return f;
	}
	private static InvCatalogImpl cleanCatalog(String data, boolean aggregations, String refs, Set<String> skip, String stop_string) {

		InvCatalog catalog;
		InvCatalogImpl clean = null;
		try {
			catalog = (InvCatalog) factory.readXML(data);

			StringBuilder buff = new StringBuilder();
			if (!data.startsWith("http://")) {
				System.out.println("Summary, " + data+ ", will not clean relative catalogs.");
			} else if (!catalog.check(buff, false)) {
				error("Invalid catalog " + data + "\n" + buff.toString(), 1);
				System.out.println("Summary, " + data + ", invalid");
			} else {
				info("Cleaning: " + data, 0);
				CatalogCleaner cleaner = null;
				try {
					cleaner = new CatalogCleaner(catalog, aggregations, refs, skip, stop_string);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

				if (cleaner != null) {
					try {
						clean = cleaner.cleanCatalog();
					} catch (Exception e) {

						System.out.println("Summary, " + data+ ", crashed with " + e.toString());
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			error("Could not read catalog " + data + "\n"
					+ e.toString(), 1);
		}
		return clean;
	}
	public static void info(String message, int level) {
		out(message, System.out, level);   	
	}
	public static void error(String message, int level) {
		out(message, System.err, level);
	}
	private static void out(String message, PrintStream stream, int level) {
		if ( level == 0 ) {
			stream.println(dateFormat.format(new Date())+" "+message);
		} else if ( level == 1 ) {
			stream.println(dateFormat.format(new Date())+"\t ... "+message);
		} else if ( level >= 1 ) {
			stream.println(dateFormat.format(new Date())+"\t\t ... "+message);   	
		}
	}
}
