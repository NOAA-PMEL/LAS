package gov.noaa.pmel.tmap.addxml.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.noaa.pmel.tmap.addxml.addXML;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dods.DODSNetcdfFile;

public class AddxmlTest {

	private addXML addxml = new addXML();
	@Before
	public void setUp() throws Exception {
		HashMap<String, String> options= new HashMap<String, String>();
		addxml.setOptions(options);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testCOADS() {
		String url = DODSNetcdfFile.canonicalURL("http://ferret.pmel.noaa.gov/thredds/dodsC/data/PMEL/coads_climatology.nc");
		NetcdfDataset ncds;
		try {
			ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(url);
			// Test from COADS, which tests the new vectors capability
			Document coads = addXML.createXMLfromNetcdfDataset(ncds , url);
			
			
			Iterator<Element> compIt = coads.getDescendants(new ElementFilter("composite"));
			
			assertTrue(compIt.hasNext());
			
			Element composite = compIt.next();
			
			assertTrue(composite != null);
			
			List children = composite.getChildren();
			
			assertTrue(children.size() == 1);
			
			Element variable = (Element) children.get(0);
			
			assertTrue(variable != null);
			
			String name = variable.getAttributeValue("name");
			String units = variable.getAttributeValue("units");
			
			assertTrue(name.equals("Vector of ZONAL WIND and MERIDIONAL WIND"));
			assertTrue(units.equals("M/S"));
			
		} catch (IOException e) {
			fail("Unable to connect to OPeNDAP server.");
		}
		
	}
    
	@Test
	public final void testLeetmaa() {
		String url = DODSNetcdfFile.canonicalURL("http://iridl.ldeo.columbia.edu/SOURCES/.NOAA/.NCEP/.EMC/.CMB/.Pacific/.monthly/dods");
		String[] name = new String[]{"Vector of zonal wind stress and meridional wind stress", "Vector of zonal velocity and meridional velocity"};
		String[] units = new String[]{"unitless", "cm/s"};
		NetcdfDataset ncds;
		try {
			ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(url);
			// Test from COADS, which tests the new vectors capability
			Document leetmaa = addXML.createXMLfromNetcdfDataset(ncds , url);
			Iterator compIt = leetmaa.getRootElement().getDescendants(new ElementFilter("composite"));
			assertTrue(compIt.hasNext());
			int index = 0;
			while ( compIt.hasNext() ) {
				Element composite = (Element) compIt.next();
				List children = composite.getChildren();				
				assertTrue(children.size() == 1);
				Element variable = (Element) children.get(0);
				assertTrue(variable != null);
				String vname = variable.getAttributeValue("name");
				String vunits = variable.getAttributeValue("units");
				assertTrue(vname.equals(name[index]));
				assertTrue(vunits.equals(units[index]));
				index++;
			}
		} catch (IOException e) {
			fail("Unable to connect to OPeNDAP server.");
		}
	}
}
