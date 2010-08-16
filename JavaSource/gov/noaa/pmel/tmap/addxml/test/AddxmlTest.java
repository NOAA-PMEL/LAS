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
	public final void testWOA() {
		String url = DODSNetcdfFile.canonicalURL("http://ferret.pmel.noaa.gov/thredds/dodsC/data/PMEL/WOA01/english/seasonal/sili_sea_mean_1deg.nc");
		NetcdfDataset ncds;
		try {
			ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(url);
			Document woa = addXML.createXMLfromNetcdfDataset(ncds , url);
			Iterator<Element> arangeIt = woa.getDescendants(new ElementFilter("arange"));
			assertTrue(arangeIt.hasNext());
			// X-Axis
			// <arange start="0.5" size="360" step="1" />
			Element arange = arangeIt.next();
			assertTrue(arange.getAttributeValue("size").equals("360"));
			assertTrue(arange.getAttributeValue("start").equals("0.5"));
			assertTrue(arange.getAttributeValue("step").equals("1"));
			// Y-Axis
			// <arange start="-89.5" size="180" step="1" />
			assertTrue(arangeIt.hasNext());
			arange = arangeIt.next();
			assertTrue(arange.getAttributeValue("size").equals("180"));
			assertTrue(arange.getAttributeValue("start").equals("-89.5"));
			assertTrue(arange.getAttributeValue("step").equals("1"));
			// T-Axis
			// <arange start="0000-02-15" size="4" step="3" />
			assertTrue(arangeIt.hasNext());
			arange = arangeIt.next();
			assertTrue(arange.getAttributeValue("size").equals("4"));
			assertTrue(arange.getAttributeValue("start").equals("0000-02-15"));
			assertTrue(arange.getAttributeValue("step").equals("3"));
		} catch (IOException e) {
			fail("Unable to connect to OPeNDAP server.");
		}
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
    public final void testGFDL() {
    	// Test to make sure variables with names that start with u or v but aren't vectors don't get included.  There should only be one composite.
    	String url = DODSNetcdfFile.canonicalURL("http://data1.gfdl.noaa.gov:8380/thredds3/dodsC/dc_CM2.1_R1_Cntr-ITFblock_monthly_ocean_tripolar_01010101-02001231");
		String[] name = new String[]{"Vector of Grid_eastward Sea Water Velocity and Grid_northward Sea Water Velocity and Upward Sea Water Velocity"};
		String[] units = new String[]{"m s-1"};
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
			assertTrue(index == 1);
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
