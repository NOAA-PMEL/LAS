package gov.noaa.pmel.tmap.catalogcleaner;

import gov.noaa.pmel.tmap.catalogcleaner.data.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import thredds.catalog.*;
import thredds.catalog.ThreddsMetadata.Source;

public class Controller {
	private static Logger log = LoggerFactory.getLogger(Controller.class);
	
	public static void main(String[] args) throws Exception{
		Controller p = new Controller();
		p.run(args[0]);
	}

	public void run(String uri) throws Exception{
		//		XMLReader parser = new SAXParser();
		//		parser.parse(uri);
		InvCatalog rawT = (InvCatalog) (Utils.FACTORY.readXML(uri));
		StringBuilder buff = new StringBuilder();
		if (!uri.startsWith("http://")) {
			System.out.println("Summary, " + uri+ ", will not clean relative catalogs.");
		} else
		if (!rawT.check(buff, false)) {
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
		Crawler crawler = new Crawler();
		int catalogId = crawler.crawlNew(rawT);
		Writer ccwriter = new Writer();
		ccwriter.run(catalogId);
	}
	}

}
