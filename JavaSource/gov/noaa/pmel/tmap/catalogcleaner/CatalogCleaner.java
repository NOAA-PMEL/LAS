package gov.noaa.pmel.tmap.catalogcleaner;

import gov.noaa.pmel.tmap.catalogcleaner.data.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import thredds.catalog.*;
import thredds.catalog.ThreddsMetadata.Source;
import thredds.catalog2.xml.parser.stax.StaxThreddsXmlParser;
import org.jdom.output.XMLOutputter;

public class CatalogCleaner {
	private static Logger log = LoggerFactory.getLogger(CatalogCleaner.class);
	
	public static void main(String[] args) throws Exception{
		
		// returns interesting data not available from the straight xml, but, I don't quite understand it yet
		Element m = ucar.nc2.util.xml.Parse.readRootElement(args[0]);
		
		
		//StaxThreddsXmlParser parser = StaxThreddsXmlParser.newInstance();
		//thredds.catalog2.Catalog c = parser.parse(new URI(args[0]));
		
/*		org.jdom.Document doc = m.getDocument();
		XMLOutputter printer = new XMLOutputter();
		printer.output(doc, System.out);
		
		Element mm = m.getParentElement();
		XMLOutputter printer3 = new XMLOutputter();
		printer3.output(mm, System.out);
		
		String s = doc.toString();
		
*/
		
		CatalogCleaner p = new CatalogCleaner();
		p.run(args[0]);
	}

	public void run(String uri) throws Exception{
		//		XMLReader parser = new SAXParser();
		//		parser.parse(uri);
		InvCatalog rawCatalog = (InvCatalog) (Utils.FACTORY.readXML(uri));
		StringBuilder buff = new StringBuilder();
		if (!uri.startsWith("http://")) {
			System.out.println("Summary, " + uri+ ", will not clean relative catalogs.");
		} else
		if (!rawCatalog.check(buff, false)) {
			log.error("Invalid catalog " + uri + "\n" + buff.toString(), 1);
			System.out.println("Summary, " + uri + ", invalid");
		} else {
			log.info("Cleaning: " + uri, 0);
			//			CatalogCleaner cleaner = null;
			//			try {
			//				cleaner = new CatalogCleaner(catalog, aggregations, refs, skip, stop_string);
			//				CCUtils.AGGREGATE = aggregations;
			//				CCUtils.REFS = refs;
			//				CCUtils.STOPSTRING = stop_string;
			//				CCUtils.SKIP = skip;
			//				CCUtils.KEY = CCUtils.MD5Encode(raw.getUriString());

			//			} catch (UnsupportedEncodingException e) {
			//				e.printStackTrace();
			//			}

			
			//}
			DataAccess.init(this);
			// todo: remove from database if exists
			//Crawler crawler = new Crawler();
			Crawler crawler = new Crawler();
			Catalog raw = crawler.crawlNewCatalog(uri);
			int cleanCatalogId = crawler.crawlRawCatalog(raw);
			Writer ccwriter = new Writer();
			ccwriter.run(cleanCatalogId);
		}
	}

}
