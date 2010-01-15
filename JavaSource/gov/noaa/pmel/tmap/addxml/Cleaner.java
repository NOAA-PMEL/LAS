package gov.noaa.pmel.tmap.addxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogConvertIF;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDatasetImpl;
import thredds.catalog.ServiceType;

public class Cleaner {
	private static final Logger log = LogManager.getLogger(Cleaner.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InvCatalogFactory factory = new InvCatalogFactory("default", true);
		if ( args[0] == null || args[0].equals("") ) {
			System.out.println("Use: Cleaner catalog.xml");
		}
		File source = new File(args[0]);
		Document uaf = new Document();
		try {
			JDOMUtils.XML2JDOM(source, uaf);
		} catch (Exception e) {
			log.error("Trouble reading source catalog: " + e.getMessage());
		}

		Namespace xlink = Namespace.getNamespace("http://www.w3.org/1999/xlink");

		for (Iterator catalogRefs = uaf.getDescendants(new CatalogRefFilter()); catalogRefs.hasNext();) {
			Element catalogRef = (Element) catalogRefs.next();

			String data = catalogRef.getAttributeValue("href", xlink);

			InvCatalog catalog = (InvCatalog) factory.readXML(data);
			StringBuilder buff = new StringBuilder();
			if (!catalog.check(buff, true)) {
				log.error("Invalid catalog " + data + "\n" + buff.toString());
			}

			CatalogCleaner cleaner = null;
			try {
				cleaner = new CatalogCleaner(catalog, true);
			} catch (UnsupportedEncodingException e) {

				e.printStackTrace();
			} catch (URISyntaxException e) {

				e.printStackTrace();
			}
			InvCatalogImpl clean = null;
			if ( cleaner != null ) {
				try {
					clean = cleaner.cleanCatalog();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

			if ( clean != null ) {
				
				try {
					// TODO Remove this DEBUG OUTPUT!!
					factory.writeXML(clean, System.out, true);
					
					// Write to a file...
					String f = JDOMUtils.MD5Encode(data)+".xml";
					File file = new File(f);
					FileOutputStream out = new FileOutputStream(file);
					factory.writeXML(clean, out, true);
					out.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}
	}


}
