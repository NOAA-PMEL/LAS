package gov.noaa.pmel.tmap.addxml.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gov.noaa.pmel.tmap.addxml.addXML;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.junit.Test;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dods.DODSNetcdfFile;


public class TestNGDC extends TestCase {
	 @Test
	    public final void testNGDC() {
	    	// The sole purpose of this test it to check the starting hour of the time axis.
	    	// The time axis is regular with an interval of 1 day, but for some strange reason the times are recorded at 17:00
	    	// so the hour needs to be included in the start string.
	    	String url = DODSNetcdfFile.canonicalURL("http://www.ngdc.noaa.gov/thredds/dodsC/sst-100km-aggregation");
			NetcdfDataset ncds;
			try {
				addXML addxml = new addXML();
				HashMap<String, String> options= new HashMap<String, String>();
				options.put("force","t");
				addxml.setOptions(options);
				ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(url);
				Document ngdc = addxml.createXMLfromNetcdfDataset(ncds , url);
				Iterator<Element> arangeIt = ngdc.getDescendants(new ElementFilter("arange"));
				assertTrue(arangeIt.hasNext());
				while ( arangeIt.hasNext() ) {
					Element arange = (Element) arangeIt.next();
					String start = arange.getAttributeValue("start");
					if ( start.length() > 6) {
						assertTrue(start.endsWith("17:00:00"));
					}
				}
			} catch (IOException e) {
				fail("Unable to connect to OPeNDAP server.");
			}
	    }
}
