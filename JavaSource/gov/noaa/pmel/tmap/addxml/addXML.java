package gov.noaa.pmel.tmap.addxml;

/**
 * @todo add an institution tag for dods servers.  Bugzilla #272
 */

/**
 * @todo If a THREDDS <dataset> element has xlink documentation it's being lost.
 * Need to figure out how to move this information up into the LAS category that
 * contains the data set.
 */

/**
 * @todo Ever since rethinking the design (a lofty word for the process of
 * deciding what this code would look like) the main code
 * is pretty messy with the logic to combine netCDF data sets into one.
 * Creating a "full" <las_data> docuement and then extracting the pieces
 * seems silly.
 */

/**
 * @todo There seem to be a number of undue kludges in the time handling.
 * I think this should be cleaner, but then again I think most of the time
 * axis information the kludges are supporting should just get rejected out
 * of hand.  We can't do everything.  (Some cover an FDS bug that seems to
 * split the time units into things like:
 * units="days"
 * time_origin="1990-MAY-19 00:00")
 */

/**
 * @todo  Add a flag that will cause a <category> element to be created
 * for each LAS <dataset> created from a -n source so that netCDF and THREDDS
 * addXML output can be used together without modification.
 */

//Standard Java stuff.

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.Comment;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Hours;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.Years;
import org.joda.time.chrono.All360Chronology;
import org.joda.time.chrono.AllLeapChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.JulianChronology;
import org.joda.time.chrono.NoLeapChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDocumentation;
import thredds.catalog.ServiceType;
import thredds.catalog.ThreddsMetadata.GeospatialCoverage;
import thredds.catalog.ThreddsMetadata.Range;
import thredds.catalog.ThreddsMetadata.Variable;
import thredds.catalog.ThreddsMetadata.Variables;
import thredds.catalog2.ThreddsMetadata;

import ucar.nc2.Attribute;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dods.DODSNetcdfFile;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.TypedDatasetFactory;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridCoordSys;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.units.DateRange;
import ucar.nc2.units.DateType;
import ucar.nc2.units.DateUnit;
import ucar.nc2.units.SimpleUnit;
import ucar.nc2.units.TimeDuration;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.projection.LambertConformal;
import ucar.unidata.geoloc.projection.LatLonProjection;

import com.martiansoftware.jsap.JSAPResult;

/**
 * <p>Title: addXML</p>
 *
 * <p>Description: Reads local or OPeNDAP netCDF files and generates LAS XML
 * configuration information.</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: NOAA/PMEL/TMAP</p>
 *
 * @author RHS
 * @version 1.5
 */
public class addXML {
	private static final Logger log = LogManager.getLogger(addXML.class);
	private static final String patterns[] = {
		"yyyy", "yyyy-MM-dd", "yyyy-MM-dd", "yyyy-MM-dd",
		"yyyy-MM-dd HH:mm:ss", "yyyy-MM-ddTHH:mm:ss",
		"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
	private static boolean verbose;
	private static int fileCount;
	private static HashMap<String, Boolean> forceAxes = new HashMap<String, Boolean>();
	private static String title;
	private static String version_string = "1.6.0.0";
	private static String global_title_attribute;
	private static String format;
	private static DateTimeFormatter fmt;
	private static String units_format;
	private static String group_type;
	private static String group_name;
	private static boolean group;
	private static boolean category;
	private static boolean use_suffix = false;
	private static boolean oneDataset = false;
	private static boolean irregular = false;
	private static boolean esg = false;

	public addXML() {
		try {
			jbInit();
		}
		catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}

	public static void main(String[] args) {

		forceAxes.put("x", new Boolean(false));
		forceAxes.put("y", new Boolean(false));
		forceAxes.put("z", new Boolean(false));
		forceAxes.put("t", new Boolean(false));

		LAS_JSAP command_parser = new LAS_JSAP();

		JSAPResult command_parameters = command_parser.parse(args);

		if (!command_parameters.success()) {
			command_parser.errorout(command_parameters);
		}

		String[] data = command_parameters.getStringArray("in_netcdf");
		String[] thredds = command_parameters.getStringArray("in_thredds");
		String in_xml = command_parameters.getString("in_xml");
		String basename = command_parameters.getString("basename");
		global_title_attribute = command_parameters.getString(
		"title_attribute");
		oneDataset = command_parameters.getBoolean("dataset");
		title = command_parameters.getString("dataset");
		category = command_parameters.getBoolean("category");
		format = command_parameters.getString("format");
		units_format = command_parameters.getString("units_format");

		boolean forceArange = command_parameters.getBoolean("arange");
		String aranges;
		if (forceArange) {
			String ax[] = command_parameters.getStringArray("arange");
			for (int a = 0; a < ax.length; a++) {
				if (ax[a].equals("x") || ax[a].equals("y") ||
						ax[a].equals("z") || ax[a].equals("t")) {
					forceAxes.put(ax[a], new Boolean(true));
				}
				else {
					log.error("Ignoring axis " + ax[a] +
					" on the --arange option. Unknown axis.  Must be x,y,z or t.");
				}
			}
		}

		group = command_parameters.getBoolean("grouptype");

		if (group) {
			String grp[] = command_parameters.getStringArray("grouptype");
			for (int g = 0; g < grp.length; g++) {
				if (grp[g].equals("ensemble") || grp[g].equals("time_series")) {
					group_type = grp[g];
				}
				else {
					log.error("Ignoring group with type " + grp[g] +
					" on the --group_type option. Must be ensemble or time_series.");
				}
			}
		}

		group_name = command_parameters.getString("groupname");

		verbose = command_parameters.getBoolean("verbose");
		boolean version = command_parameters.getBoolean("version");
		irregular = command_parameters.getBoolean("irregular");

		esg = command_parameters.getBoolean("esg");

		// Retrieve user credentials info.
		String provider = command_parameters.getString("auth_provider");
		String username = command_parameters.getString("username");
		String password = command_parameters.getString("password");

		// If credentials provider class was not given, then use the default implementation.
		LASCredentialsProvider provider_obj = null;
		if(provider == null){
			provider_obj = new SimpleCredentialsProvider();

			// Otherwise, use the given class.
		}else{

			// Load the credentials provider class.
			java.lang.ClassLoader loader = java.lang.ClassLoader.getSystemClassLoader();
			Class provider_class = null;
			try{
				provider_class = loader.loadClass(provider);
			}catch(ClassNotFoundException ee){
				log.error("Error loading authentication credentials provider class.");
				log.error(ee.getMessage());
				System.exit(1);
			}

			// Instantiate the credentials provider object.
			try{
				provider_obj = (LASCredentialsProvider)provider_class.newInstance();
			}catch(Exception ee){
				log.error("Error creating authentication credentials provider object.");
				log.error(ee.getMessage());
				System.exit(1);
			}
		}

		// If given, set user credentials with the provider object.
		if(username != null){
			provider_obj.setUsername(username);
		}
		if(password != null){
			provider_obj.setPassword(password);
		}

		// Register the credentials provider.
//		ucar.nc2.dataset.HttpClientManager.init(provider_obj,"addXML-" + version_string);	

		int total = data.length + thredds.length;
		if (!oneDataset && total > 1) {
			use_suffix = true;
		}
		else if (oneDataset && thredds.length >= 1) {
			use_suffix = true;
		}

		// If there will only be one dataset for all netCDF arguments
		// accumulate them in here.
		DatasetsGridsAxesBean oneDgab = new DatasetsGridsAxesBean();
		DatasetBean oneDb = new DatasetBean();
		if ( group ) {
			oneDb.setGroup_name(group_name);
			oneDb.setGroup_type(group_type);
			oneDb.setGroup_id(encodeID(group_name+" "+group_type));
		}
		// If we are creating categories and we are creating only
		// one dataset for the netCDF data, we'll use this category bean.
		CategoryBean oneCat = new CategoryBean();

		oneDb.setCreator(addXML.class.getName());
		oneDb.setVersion(version_string);
		// Every variable in this dataset object will have it's own fully qualified
		// data URL.  Set the data set URL to null.
		oneDb.setUrl(null);
		if (title != null && title != "") {
			oneDb.setName(title);
			oneCat.setName(title);
		}

		if (version) {
			log.error("Version: " + version_string);
		}

		if (data.length == 0 && thredds.length == 0) {
			log.error("");
			log.error("You must specify either");
			log.error("\ta THREDDS catalog with the -t option or ");
			log.error("\ta netCDF data source with the -n option.");
			log.error("");
			log.error("Usage: addXML.sh ");
			log.error(command_parser.getUsage());
			log.error("");
			log.error("");
			log.error(command_parser.getHelp());
			System.exit(1);
		}

		Document inputLasDoc = null;
		if (in_xml != null && in_xml != "") {
			SAXBuilder builder = new SAXBuilder();
			builder.setExpandEntities(false);
			builder.setEntityResolver(new MyEntityResolver());
			try {
				inputLasDoc = builder.build(in_xml);
			}
			// Failed to read input XML.  Go on without it.
			catch (IOException ex) {
				log.error(ex.getMessage());
				inputLasDoc = null;
			}
			catch (JDOMException ex) {
				log.error(ex.getMessage());
				inputLasDoc = null;
			}
		}

		int numThredds = 0;
		int numNetcdf = 0;

		for (int id = 0; id < data.length; id++) {

			DatasetsGridsAxesBean dgab = null;
			try {
				String url = DODSNetcdfFile.canonicalURL(data[id]);
				NetcdfDataset ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(url);
				dgab = createBeansFromNetcdfDataset(ncds, data[id], false, null);
				ncds.close();
			}
			catch (IOException e) {
				log.error("IO error = " + e);
			}
			Vector db = (Vector) dgab.getDatasets();
			if (db != null && db.size() > 0) {

				if (oneDataset) {
					// We're only going to use one data set.  Accumulate all the info
					// into that one data set.
					DatasetBean databean = (DatasetBean) dgab.getDatasets().get(0);

					// Check to see if the name has been set.
					if (oneDb.getName() == null || oneDb.getName() == "") {
						oneDb.setName(databean.getName());
						// If the data set name wasn't set neither was the category
						oneCat.setName(databean.getName());
					}
					// Set the category filter to include this data set.
					FilterBean filter = new FilterBean();
					filter.setAction("apply-dataset");
					filter.setContainstag(databean.getElement());
					oneCat.addFilter(filter);

					Vector variables = (Vector) databean.getVariables();
					// All the URL's must be fixed to not be relative to the data set
					// URL
					Iterator vit = variables.iterator();
					while (vit.hasNext()) {
						VariableBean vb = (VariableBean) vit.next();
						String url = databean.getUrl() + vb.getUrl();
						vb.setUrl(url);
					}
					oneDb.addAllVariables(variables);
					if (oneDb.getElement() == null || oneDb.getElement() == "") {
						oneDb.setElement(databean.getElement());
					}

					Vector grids = dgab.getGrids();
					Vector existingGrids = oneDgab.getGrids();
					if (existingGrids == null) {
						existingGrids = new Vector();
					}
					existingGrids.addAll(grids);
					oneDgab.setGrids(existingGrids);

					Vector axes = dgab.getAxes();
					Vector existingAxes = oneDgab.getAxes();
					if (existingAxes == null) {
						existingAxes = new Vector();
					}
					existingAxes.addAll(axes);
					oneDgab.setAxes(existingAxes);

				}
				else {

					Document lasdoc = createXMLfromDatasetsGridsAxesBean(dgab);

					String ofile = getOutfileName(basename);
					if (inputLasDoc != null) {

						String entityName = getEntityName(ofile);

						// Add an entity reference to the input document if it exists.
						EntityRef entityReference = new EntityRef(entityName, ofile);
						addEntityRef(inputLasDoc, entityName, ofile, entityReference);
					}
					Element lasdata = lasdoc.getRootElement();
					Element datasets = lasdata.getChild("datasets");
					numNetcdf = datasets.getChildren().size();

					if (numNetcdf > 0) {
						outputXML(ofile, datasets, false);
						Element grids = lasdata.getChild("grids");
						outputXML(ofile, grids, true);
						Element axes = lasdata.getChild("axes");
						outputXML(ofile, axes, true);
						if (category) {
							for (Iterator cit = datasets.getChildren().iterator();
							cit.hasNext(); ) {
								Element datasetElem = (Element) cit.next();
								CategoryBean ds_category = new CategoryBean();
								ds_category.setName(datasetElem.getAttribute("name").getValue());
								FilterBean filter = new FilterBean();
								filter.setAction("apply-dataset");
								filter.setContainstag(datasetElem.getName());
								ds_category.addFilter(filter);
								Element lc = new Element("las_categories");
								lc.addContent(ds_category.toXml());
								outputXML(ofile, lc, true);
							}
						}
					}
				}
			}
		}

		if (oneDataset && data.length > 0) {
			// We lumped all the netCDF arguements into one data set.  Now we
			// need to print them to an entity and fix up the --xml file if
			// it exists.

			// Edit the entity string of the existing document and print it out.

			// We've been stuffing in the dataset info into oneDb.  Stuff it into
			// oneDgab and print it.
			Vector dsets = new Vector();
			dsets.add(oneDb);
			oneDgab.setDatasets(dsets);

			Document lasdoc = createXMLfromDatasetsGridsAxesBean(oneDgab);
			String ofile = getOutfileName(basename);
			if (inputLasDoc != null) {

				String entityName = getEntityName(ofile);

				// Add an entity reference to the input document if it exists.
				EntityRef entityReference = new EntityRef(entityName, ofile);
				addEntityRef(inputLasDoc, entityName, ofile, entityReference);
			}
			Element lasdata = lasdoc.getRootElement();
			Element datasets = lasdata.getChild("datasets");
			numNetcdf = datasets.getChildren().size();
			if (numNetcdf > 0) {
				outputXML(ofile, datasets, false);
				Element grids = lasdata.getChild("grids");
				outputXML(ofile, grids, true);
				Element axes = lasdata.getChild("axes");
				outputXML(ofile, axes, true);
				if (category) {
					Element lc = new Element("las_categories");
					lc.addContent(oneCat.toXml());
					outputXML(ofile, lc, true);
				}
			}

		}

		for (int it = 0; it < thredds.length; it++) {
			Document lasdoc = LASConfig(thredds[it], "thredds");
			if (lasdoc != null) {

				String ofile = getOutfileName(basename);
				if (inputLasDoc != null) {
					String entityName = getEntityName(ofile);

					// Add an entity reference to the input document if it exists.
					EntityRef entityReference = new EntityRef(entityName, ofile);
					addEntityRef(inputLasDoc, entityName, ofile, entityReference);
				}

				Element lasdata = lasdoc.getRootElement();
				Element datasets = lasdata.getChild("datasets");
				numThredds = datasets.getChildren().size();
				if (numThredds > 0) {
					outputXML(ofile, datasets, false);
					Element grids = lasdata.getChild("grids");
					outputXML(ofile, grids, true);
					Element axes = lasdata.getChild("axes");
					outputXML(ofile, axes, true);
					if ( category ) {
						Element categories = lasdata.getChild("las_categories");
						outputXML(ofile, categories, true);
					}
				}
			}
		}

		if (numThredds == 0 && numNetcdf == 0) {
			log.error("");
			log.error("No grids were found in the input data sets.");
			log.error(
			"Check to see if the OPeNDAP servers being referenced are running.");
			log.error(
			"Verify that the netCDF files referenced are COARDS or CF compliant.");
			log.error("");
		}

		if (inputLasDoc != null) {
			String newXmlFile;
			if (basename.endsWith(".xml")) {
				newXmlFile = basename.substring(0, basename.length() - 4) +
				"_las.xml";
			}
			else {
				newXmlFile = basename + "_las.xml";
			}
			outputXML(newXmlFile, inputLasDoc);
		}
	}

	public static Document LASConfig(String data, String type) {

		// Try to read this as a netCDF file or OPeNDAP netCDF data source.
		if (type.equals("netcdf")) {

		}
		// Try to read this as a THREDDS Dataset Inventory Catalog
		if (type.equals("thredds")) {
			InvCatalogFactory factory = new InvCatalogFactory("default", false);
			InvCatalog catalog = (InvCatalog) factory.readXML(data);
			StringBuilder buff = new StringBuilder();
			boolean show = false;
			if (verbose) {
				show = true;
			}
			if (!catalog.check(buff, show)) {
				log.error("Invalid catalog <" + data + ">\n" + buff.toString());
			}
			else {
				return createXMLfromTHREDDSCatalog(catalog);
			}
		}
		// Try to read this as a THREDDS Catalog Generator Configuration file.
		// If this works, then generate the catalog and then generate the LAS
		// configuration.

		// Skip this for now...
		/*
         URL url=null;
         try {
      url = new URL(data);
         }
         catch (MalformedURLException ex) {
      log.error(ex.getMessage()+" "+data);
         }
         CatalogGen catGen = new CatalogGen(url);
         StringBuffer log = new StringBuffer();
         if (catGen.isValid(log)) {
      catGen.expand();
      catalog = catGen.getCatalog();
      return createXMLfromTHREDDSCatalog(catalog);
         }
		 */

		return null;

	}

	// end of LASConfigFactory
	/**
	 * addEntityRef
	 *
	 * @param inputLasDoc Document
	 * @param entityReference EntityRef
	 */
	public static void addEntityRef(Document inputLasDoc,
			String entityName,
			String ofile,
			EntityRef entityReference) {

		DocType docType = inputLasDoc.getDocType();
		String entityString = docType.getInternalSubset();
		// Get rid of the file:/// references to the existing entities
		String shortEntityString = "";
		StringTokenizer tokenizer = new StringTokenizer(entityString, "\n");
		while (tokenizer.hasMoreTokens()) {
			String entity = tokenizer.nextToken();
			if (entity.indexOf("file:/") >= 0) {
				entity = entity.substring(0, entity.indexOf("file")) +
				entity.substring(entity.lastIndexOf("/") + 1, entity.length());
				shortEntityString += entity + "\n";
			}
			else {
				shortEntityString += entity + "\n";
			}
		}
		entityString = shortEntityString +
		"  <!ENTITY " + entityName + " SYSTEM \"" + ofile + "\">\n";
		docType.setInternalSubset(entityString);
		Element lasdata = inputLasDoc.getRootElement();
		lasdata.addContent(entityReference);
		lasdata.addContent("\n");

	}

	public static String getEntityName(String ofile) {
		// Get rid of c:\ on PC file names.
		String entityName = ofile;
		if (entityName.startsWith(":\\", 1)) {
			entityName = entityName.substring(3, ofile.length());
			// Get rid of the ".xml" in the name.
		}
		entityName = entityName.substring(0, entityName.length() - 4);
		return entityName;
	}

	/**
	 * getOutfileName
	 *
	 * @param basename String
	 * @param b boolean
	 * @return String
	 */

	public static String getOutfileName(String basename) {

		String ofile;
		String countString = "";

		if (use_suffix) {
			countString = "_000";
			if (fileCount < 10) {
				countString = "_00" + String.valueOf(fileCount);
			}
			else if (fileCount >= 10 && fileCount < 99) {
				countString = "_0" + String.valueOf(fileCount);
			}
			else if (fileCount >= 100 && fileCount < 1000) {
				countString = String.valueOf(fileCount);
			}
			else {
				log.error("No more that 999 data sets to process.  Please.");
				System.exit(1);
			}
		}

		if (basename.endsWith(".xml")) {
			ofile = basename.substring(0, basename.length() - 4) +
			countString +
			".xml";
		}
		else {
			ofile = basename + countString + ".xml";
		}

		fileCount++;
		return ofile;
	}

	/**
	 * createXMLfromTHREDDSCatalog
	 *
	 * @param catalog InvCatalog
	 * @return Document
	 */
	private static Document createXMLfromTHREDDSCatalog(InvCatalog catalog) {

		CategoryBean top = new CategoryBean();
		String topName = catalog.getName();
		if (topName != null) {
			top.setName(catalog.getName());
		}
		else {
			top.setName(catalog.getUriString());
		}
		Vector DGABeans = new Vector();
		Vector CategoryBeans = new Vector();

		List ThreddsDatasets = catalog.getDatasets();
		Iterator di = ThreddsDatasets.iterator();
		while (di.hasNext()) {
			InvDataset ThreddsDataset = (InvDataset) di.next();
			if (ThreddsDataset.hasNestedDatasets()) {
				CategoryBean cb = processCategories(ThreddsDataset);
				CategoryBeans.add(cb);
			}
		}

		// Discover and process all the THREDDS dataset elements that actually
		// connect to a data source.

		ThreddsDatasets = catalog.getDatasets();
		di = ThreddsDatasets.iterator();
		while (di.hasNext()) {
			InvDataset ThreddsDataset = (InvDataset) di.next();
			DGABeans.addAll(processDatasets(ThreddsDataset));
		}

		// Each THREDDS "dataset" is a separate LAS data set.
		// If oneDataset is true, combine them before making the XML.

		if (oneDataset) {
			Vector newDAGBVector = new Vector();
			DatasetsGridsAxesBean newDAGB = new DatasetsGridsAxesBean();
			GridBean theGrid = (GridBean)(((DatasetsGridsAxesBean)DGABeans.get(0)).getGrids()).get(0);
			Vector newGrids = new Vector();
			newGrids.add(theGrid);
			newDAGB.setGrids(newGrids);
			newDAGB.setAxes(theGrid.getAxes());
			DatasetBean newDSB = new DatasetBean();
			if (group) {
				newDSB.setGroup_name(group_name);
				newDSB.setGroup_type(group_type);
				newDSB.setGroup_id(encodeID(group_name+" "+group_type));
			}
			newDSB.setUrl(null);
			if (title != null) {
				newDSB.setName(title);
			}


			for (Iterator dgabIter = DGABeans.iterator(); dgabIter.hasNext();) {
				DatasetsGridsAxesBean aDAGB = (DatasetsGridsAxesBean) dgabIter.next();
				Vector datasets = aDAGB.getDatasets();            
				for (Iterator ds = datasets.iterator(); ds.hasNext();) {
					DatasetBean dsb = (DatasetBean) ds.next();
					if (newDSB.getElement() == null) {
						newDSB.setElement(dsb.getElement());
					}
					Vector oldVars = dsb.getVariables();
					Vector newVariables = new Vector();
					for (Iterator ovIt = oldVars.iterator(); ovIt.hasNext();) {
						VariableBean var = (VariableBean) ovIt.next();
						String durl = dsb.getUrl();
						String vurl = var.getUrl();
						var.setUrl(durl+vurl);
						var.setGrid(theGrid);
						newVariables.add(var);
					}
					newDSB.addAllVariables(newVariables);
				}
			}
			Vector newDatasets = new Vector();
			newDatasets.add(newDSB);
			newDAGB.setDatasets(newDatasets);
			newDAGBVector.add(newDAGB);
			DGABeans = newDAGBVector;
		}

		top.setCategories(CategoryBeans);
		// create las_categories and datasets elements at this level
		Document doc = new Document();
		Element lasdata = new Element("lasdata");
		Element datasetsElement = new Element("datasets");
		Element gridsElement = new Element("grids");
		Element axesElement = new Element("axes");

		Iterator dgabit = DGABeans.iterator();
		while (dgabit.hasNext()) {
			DatasetsGridsAxesBean dgab_temp = (DatasetsGridsAxesBean) dgabit.next();
			if (dgab_temp.getError() != null) {
				lasdata.addContent(new Comment(dgab_temp.getError()));
			}
			else {
				Vector datasets = dgab_temp.getDatasets();
				Iterator dsit = datasets.iterator();
				while (dsit.hasNext()) {
					DatasetBean db = (DatasetBean) dsit.next();
					Element dsE = db.toXml();
					datasetsElement.addContent(dsE);
				}
				Vector grids = dgab_temp.getGrids();
				Iterator git = grids.iterator();
				while (git.hasNext()) {
					GridBean gb = (GridBean) git.next();
					Element gE = gb.toXml();
					gridsElement.addContent(gE);
				}
				Vector axes = dgab_temp.getAxes();
				Iterator ait = axes.iterator();
				while (ait.hasNext()) {
					AxisBean ab = (AxisBean) ait.next();
					Element aE = ab.toXml();
					axesElement.addContent(aE);
				}
			}
		}

		Element las_categories = new Element("las_categories");
		Element topElement = top.toXml();
		las_categories.addContent(topElement);
		lasdata.addContent(datasetsElement);
		lasdata.addContent(gridsElement);
		lasdata.addContent(axesElement);
		lasdata.addContent(las_categories);
		doc.setRootElement(lasdata);
		return doc;
	}

	/**
	 * processDataset
	 *
	 * @param ThreddsDataset InvDataset
	 * @return DatasetBean
	 */
	public static Vector processDatasets(InvDataset ThreddsDataset) {
		Vector beans = new Vector();
		if (ThreddsDataset.hasAccess()) {
			boolean done = false;
			for (Iterator iter = ThreddsDataset.getAccess().iterator();
			iter.hasNext(); ) {
				InvAccess access = (InvAccess) iter.next();
				if ( (access.getService().getServiceType() == ServiceType.DODS ||
						access.getService().getServiceType() == ServiceType.NETCDF ||
						access.getService().getServiceType() == ServiceType.OPENDAP) &&
						!done) {
					done = true;
					DatasetsGridsAxesBean dgab =
						createBeansFromThreddsDataset(ThreddsDataset, access);
					beans.add(dgab);
				}
			}
		}
		for (Iterator iter = ThreddsDataset.getDatasets().iterator();
		iter.hasNext(); ) {
			beans.addAll(processDatasets( (InvDataset) iter.next()));
		}
		return beans;
	}

	/**
	 * createDatasetBeanFromThreddsDataset
	 *
	 * @param ThreddsDataset InvDataset
	 * @return DatasetBean
	 */
	private static DatasetsGridsAxesBean createBeansFromThreddsDataset(
			InvDataset ThreddsDataset, InvAccess access) {
		DatasetsGridsAxesBean dgab = new DatasetsGridsAxesBean();

		String url = access.getStandardUrlName();
		try {
			if ( esg ) {
				if ( url.contains("aggregation") ) {
					// Try to get metadata from the catalog.
					dgab = createBeansFromThreddsMetadata(ThreddsDataset, url);
					if ( dgab == null ) {
						// Else open the aggregation and get it from there
						String dods = url.replaceAll("http", "dods");
						NetcdfDataset ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(dods);
						dgab = createBeansFromNetcdfDataset(ncds, url, esg, ThreddsDataset);
						ncds.close();
					}
				}
			} else {
				String dods = url.replaceAll("http", "dods");
				NetcdfDataset ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(dods);
				dgab = createBeansFromNetcdfDataset(ncds, url, false, null);
				ncds.close();
			}
		}
		catch (IOException e) {
			dgab.setError(e.getMessage());
			log.error("IO error = " + e.getMessage());
		}

		return dgab;
	}

	private static DatasetsGridsAxesBean createBeansFromThreddsMetadata(
			InvDataset threddsDataset, String url) {
		DatasetsGridsAxesBean dgab = new DatasetsGridsAxesBean();
		Vector DatasetBeans = new Vector();
		DatasetBean dataset = new DatasetBean();
		UniqueVector GridBeans = new UniqueVector();
		UniqueVector AxisBeans = new UniqueVector();

		if (verbose) {
			log.info("Processing ESG THREDDS dataset: " + threddsDataset.getFullName() + "with id: "+threddsDataset.getID());
		}

		dataset.setName(threddsDataset.getFullName());
		dataset.setElement(threddsDataset.getID());
		dataset.setVersion(version_string);
		dataset.setCreator(addXML.class.getName());
		dataset.setCreated((new DateTime()).toString());
		dataset.setUrl(url);
		List<Variables> variables = threddsDataset.getVariables();
		if (variables.size() > 0 ) {
			for (Iterator varlistIt = variables.iterator(); varlistIt.hasNext();) {
				Variables vars_container = (Variables) varlistIt.next();
				List<Variable> vars = vars_container.getVariableList();
				if ( vars.size() > 0 ) {
					for (Iterator varIt = vars.iterator(); varIt.hasNext();) {
						Variable variable = (Variable) varIt.next();

						VariableBean las_var = new VariableBean();
						las_var.setElement(threddsDataset.getID()+"-"+variable.getName());
						las_var.setName(variable.getName());
						las_var.setUnits(variable.getUnits());
						las_var.setUrl("#"+variable.getName());
						log.info("Processing ESG THREDDS variable: " + variable.getName());

						GeospatialCoverage coverage = threddsDataset.getGeospatialCoverage();
						DateRange dateRange = threddsDataset.getTimeCoverage();

						StringBuilder grid_name = new StringBuilder(threddsDataset.getID()+"-grid");
						if (coverage != null ) {

							boolean readX = false;
							boolean readY = false;

							double xsize = coverage.getLonExtent();
							double xresolution = coverage.getLonResolution();
							double xstart = coverage.getLonStart();
							String xunits = coverage.getLonUnits();

							if ( Double.isNaN(xsize) || Double.isNaN(xstart)) {
								readX = true;
							}
							if ( Double.isNaN(xresolution) ) {
								// We're going to pretend it is 1-degree data for purposes of the LAS UI.
								xresolution = 1.0;
							}
							double ysize = coverage.getLatExtent();
							double yresolution = coverage.getLatResolution();
							double ystart = coverage.getLatStart();
							String yunits = coverage.getLatUnits();
							if ( Double.isNaN(ysize) || Double.isNaN(ystart) ) {
								readY = true;
							}
							if ( Double.isNaN(yresolution) ) {
								// We're going to pretend it is 1-degree data for purposes of the LAS UI.
								yresolution = 1.0;
							}
							boolean hasZ = false;
							boolean readZ = false;
							String zvalues = null;
							Range z = coverage.getUpDownRange();
							if ( z != null ) {	
								hasZ = true;
								double zsize = z.getSize();
								double zresolution = z.getResolution();
								double zstart = z.getStart();
								zvalues = threddsDataset.findProperty("zvalues");
								if ( ( Double.isNaN(zsize) || Double.isNaN(zresolution) || Double.isNaN(zstart) ) &&
										zvalues == null ) {
									readZ = true;
								}
							}
							// One of these axes is not sufficiently specified in the metadata so prepare read the data out of the aggregation.
							NetcdfDataset ncds = null;
							GridDataset gridsDs = null;
							GridCoordSys gcs = null;
							if ( readX || readY || readZ ) {
								String dods = url.replaceAll("http", "dods");

								try {
									ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(dods);
									gridsDs = new GridDataset(ncds);
									if ( ncds != null ) {
										ncds.close();
									}

								} catch (IOException e) {
									log.error("unable to read netcdf data set.");
									return null;
								}
								GridDatatype geogrid = gridsDs.findGridDatatype(variable.getName());
								gcs = (GridCoordSys) geogrid.getCoordinateSystem();
							}
							String elementName = threddsDataset.getID()+"-x-axis";
							AxisBean xAxis = new AxisBean();
							if ( readX && ncds != null && gridsDs != null ) {
								log.info("Reading X "+elementName);
								CoordinateAxis x = gcs.getXHorizAxis();
								if ( x instanceof CoordinateAxis1D ) {
									CoordinateAxis1D x_1d = (CoordinateAxis1D) gcs.getXHorizAxis();
									xAxis = makeGeoAxis(x_1d, "x", elementName);
								} else {
									CoordinateAxis2D x_2d = (CoordinateAxis2D) gcs.getXHorizAxis();
									xAxis = makeGeoAxisFrom2D(x_2d, "x", elementName);
									las_var.setProperty("ferret", "curvi_coord_lon", x.getName());
									las_var.setProperty("ferret", "curv_lon_min", String.valueOf(x.getMinValue()));
									las_var.setProperty("ferret", "curv_lon_max", String.valueOf(x.getMaxValue()));
								}							  
							} else {
								// Get the X Axis information...
								log.info("Loading X from metadata: "+elementName);
								xAxis.setElement(elementName);
								grid_name.append("-x-axis");
								xAxis.setType("x");
								xAxis.setUnits(xunits);
								int xsizei = (int)(xsize/xresolution);
								ArangeBean xr = new ArangeBean();
								xr.setSize(String.valueOf(xsizei));
								xr.setStep(String.valueOf(xresolution));
								xr.setStart(String.valueOf(xstart));
								xAxis.setArange(xr);

							}
							if ( !AxisBeans.contains(xAxis) ) {
								AxisBeans.add(xAxis);
							} else {
								xAxis.setElement(AxisBeans.getMatchingID(xAxis));
							}
							elementName = threddsDataset.getID()+"-y-axis";
							AxisBean yAxis = new AxisBean();
							if ( readY ) {
								log.info("Reading Y "+elementName);
								CoordinateAxis y = gcs.getYHorizAxis();
								if ( y instanceof CoordinateAxis1D ) {
									CoordinateAxis1D y_1d = (CoordinateAxis1D) gcs.getYHorizAxis();
									yAxis = makeGeoAxis(y_1d, "y", elementName);
								} else {
									CoordinateAxis2D y_2d = (CoordinateAxis2D) gcs.getYHorizAxis();
									yAxis = makeGeoAxisFrom2D(y_2d, "y", elementName);
									las_var.setProperty("ferret", "curvi_coord_lat", y.getName());
								}
							} else {
								log.info("Loading Y from metadata: "+elementName);
								// Get the Y Axis information...
								yAxis.setElement(elementName);
								grid_name.append("-y-axis");
								yAxis.setType("y");
								yAxis.setUnits(yunits);
								int ysizei = (int)(ysize/yresolution);
								ArangeBean yr = new ArangeBean();
								yr.setSize(String.valueOf(ysizei));
								yr.setStep(String.valueOf(yresolution));
								yr.setStart(String.valueOf(ystart));
								yAxis.setArange(yr);
							}
							if ( !AxisBeans.contains(yAxis) ) {
								AxisBeans.add(yAxis);
							} else {
								yAxis.setElement(AxisBeans.getMatchingID(yAxis));
							}
							elementName = threddsDataset.getID()+"-z-axis";
							AxisBean zAxis = new AxisBean();
							if ( hasZ ) {
								if ( readZ ) {
									log.info("Reading Z "+elementName);
									CoordinateAxis1D z_1d = gcs.getVerticalAxis();
									zAxis = makeGeoAxis(z_1d, "z", elementName);
								} else if ( zvalues != null ) {
									log.info("Loading Z from property metadata: "+elementName);
									zAxis.setElement(elementName);
									String zunits = z.getUnits();
									zAxis.setType("z");
									zAxis.setUnits(zunits);
									zvalues = zvalues.trim().replaceAll("\\s+", "");
									String[] v = zvalues.split(",");
									zAxis.setV(v);
								} else {
									log.info("Loading Z without property metadata: "+elementName);
									zAxis.setElement(elementName);
									grid_name.append("-z-axis");
									double zsize = z.getSize();
									double zresolution = z.getResolution();
									double zstart = z.getStart();
									if ( !Double.isNaN(zsize) && !Double.isNaN(zresolution) && !Double.isNaN(zstart) ) {
										String zunits = z.getUnits();
										zAxis.setType("z");
										zAxis.setUnits(zunits);
										ArangeBean zr = new ArangeBean();
										int zsizei = (int)(zsize/zresolution);
										zr.setSize(String.valueOf(zsizei));
										zr.setStep(String.valueOf(zresolution));
										zr.setStart(String.valueOf(zstart));
										zAxis.setArange(zr);

									} 
								}
								if ( !AxisBeans.contains(zAxis) ) {
									AxisBeans.add(zAxis);
								} else {
									zAxis.setElement(AxisBeans.getMatchingID(zAxis));
								}
							}
							String time_delta = threddsDataset.findProperty("time_delta");
							String calendar = threddsDataset.findProperty("calendar");

							String[] time_parts = time_delta.split("\\s+");
							String tdelta = time_parts[0];
							String tunits = time_parts[1];

							// Use this chronology and the UTC Time Zone
							Chronology chrono = GJChronology.getInstance(DateTimeZone.UTC);

							// If calendar attribute is set, use appropriate Chronology.
							if (calendar.equals("proleptic_gregorian") ) {
								chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
							} else if (calendar.equals("noleap") || calendar.equals("365_day") ) {
								chrono = NoLeapChronology.getInstance(DateTimeZone.UTC);
							} else if (calendar.equals("julian") ) {
								chrono = JulianChronology.getInstance(DateTimeZone.UTC);
							} else if ( calendar.equals("all_leap") || calendar.equals("366_day") ) {
								chrono = AllLeapChronology.getInstance(DateTimeZone.UTC);
							} else if ( calendar.equals("360_day") ) {  /* aggiunto da lele */
								chrono = All360Chronology.getInstance(DateTimeZone.UTC);
							}

							String tstart = trimUnidataDateTimeString(dateRange.getStart());
							String tend = trimUnidataDateTimeString(dateRange.getEnd());
							DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd 00:00:00").withChronology(chrono);
							DateTime s = f.parseDateTime(tstart).withChronology(chrono);
							DateTime e = f.parseDateTime(tend).withChronology(chrono);
							Months mm = Months.monthsBetween(s, e);
							int nm = mm.getMonths();
							int size = 0;
							if ( tunits.trim().toLowerCase().contains("year") ) {
								s = s.dayOfMonth().withMinimumValue();
								e = e.dayOfMonth().withMaximumValue();
								Years y = Years.yearsBetween(s, e);
								size = y.getValue(0);
							} else if ( tunits.trim().toLowerCase().contains("month") ) {
								s = s.dayOfMonth().withMinimumValue();
								e = e.dayOfMonth().withMaximumValue();
								Months m = Months.monthsBetween(s, e);
								size = m.getMonths();
							} else if ( tunits.trim().toLowerCase().contains("day") ) {
								Days d = Days.daysBetween(s, e);
								size = d.getValue(0);
							} else if ( tunits.trim().toLowerCase().contains("hour") ) {
								Hours h = Hours.hoursBetween(s, e);
								size = h.getValue(0);
							}
							log.info("Loading T from metadata: "+elementName);
							AxisBean tAxis = new AxisBean();
							tAxis.setElement(threddsDataset.getID()+"-t-axis");
							grid_name.append("-t-axis");
							tAxis.setType("t");
							tAxis.setUnits(tunits);
							ArangeBean tr = new ArangeBean();
							tr.setStart(tstart);
							tr.setStep(tdelta);
							tr.setSize(String.valueOf(size));
							tAxis.setArange(tr);
							if ( !AxisBeans.contains(tAxis) ) {
								AxisBeans.addUnique(tAxis);
							} else {
								tAxis.setElement(AxisBeans.getMatchingID(tAxis));
							}

							dgab.setAxes(AxisBeans);
							GridBean grid = new GridBean();
							grid.setElement(grid_name.toString());
							grid.setAxes(AxisBeans);
							if ( !GridBeans.contains(grid) ) {
								GridBeans.add(grid);
							} else {
								grid.setElement(GridBeans.getMatchingID(grid));
							}



							las_var.setGrid(grid);
							dataset.addVariable(las_var);

						} // coverage != null
					} // for variables;
					DatasetBeans.add(dataset);
				} else { // vars > 0
					return null;
				}
			}// for outer variables container iterator
		} else { // outer variables list
			return null;
		}
		dgab.setGrids(GridBeans);
		dgab.setAxes(AxisBeans);
		dgab.setDatasets(DatasetBeans);
		return dgab;
	}
	public static String trimUnidataDateTimeString(DateType d) {
		String dt = d.toDateTimeString();
		if ( dt.endsWith("Z") ) return dt.substring(0, dt.length() - 1);
		return dt;
	}
	/**
	 * processCategories
	 *
	 * @param ThreddsDataset InvDataset
	 * @return CategoryBean
	 */
	public static CategoryBean processCategories(InvDataset ThreddsDataset) {
		CategoryBean cb = new CategoryBean();
		// Make any THREDDS documentation links into LAS contributor links.
		List docs = ThreddsDataset.getDocumentation();
		Vector contribs = new Vector();
		for (Iterator dit = docs.iterator(); dit.hasNext(); ) {
			InvDocumentation doc = (InvDocumentation) dit.next();
			if (doc.hasXlink()) {
				ContributorBean contributor = new ContributorBean();
				contributor.setRole("THREDDS Metadata");
				contributor.setUrl(doc.getXlinkHref());
				contributor.setName(doc.getXlinkTitle());
				contribs.add(contributor);
			}
		}
		cb.setContributors(contribs);

		String name = ThreddsDataset.getName();
		if (name != null) {
			cb.setName(ThreddsDataset.getName());
		}
		else {
			cb.setName("THREDDS Dataset");
		}
		if (ThreddsDataset.hasAccess()) {
			// This will create a filter that doesn't match anything.
			// The LAS interface generator will ignore this category.
			String url = "yadayada";
			InvAccess access = null;
			for (Iterator ait = ThreddsDataset.getAccess().iterator(); ait.hasNext(); ) {
				access = (InvAccess) ait.next();
				if (access.getService().getServiceType() == ServiceType.DODS ||
						access.getService().getServiceType() == ServiceType.OPENDAP ||
						access.getService().getServiceType() == ServiceType.NETCDF) {
					url = access.getStandardUrlName();
				}
			}
			if ( access != null && ((esg && url.contains("aggregation")) || !esg ) ) {
				// Make the filter.
				FilterBean filter = new FilterBean();
				filter.setAction("apply-dataset");
				String tag;
				if ( esg ) {
					tag = ThreddsDataset.getID();
				} else {
					if ( ThreddsDataset != null && ThreddsDataset.getID() != null && !ThreddsDataset.getID().equals("") ) {
						tag = ThreddsDataset.getID();
					} else {
						tag = encodeID(url);
					}
				}

				filter.setContainstag(tag);
				cb.addFilter(filter);
			}
		}

		Vector subCats = new Vector();
		for (Iterator subDatasetsIt = ThreddsDataset.getDatasets().iterator();
		subDatasetsIt.hasNext(); ) {
			InvDataset subDataset = (InvDataset) subDatasetsIt.next();
			// Process the sub-categories
			CategoryBean subCat = processCategories(subDataset);
			subCats.add(subCat);
		}
		cb.setCategories(subCats);

		return cb;
	}

	/**
	 * createXMLfromNetcdfDataset
	 *
	 * @param ncds NetcdfDataset
	 * @param url String
	 * @return Document
	 */
	public static DatasetsGridsAxesBean createBeansFromNetcdfDataset(
			NetcdfDataset ncds,
			String url, boolean esg, InvDataset threddsDataset) {

		DatasetsGridsAxesBean dagb = new DatasetsGridsAxesBean();
		Vector DatasetBeans = new Vector();
		DatasetBean dataset = new DatasetBean();
		UniqueVector GridBeans = new UniqueVector();
		UniqueVector AxisBeans = new UniqueVector();

		if (group) {
			dataset.setGroup_name(group_name);
			dataset.setGroup_type(group_type);
			dataset.setGroup_id(encodeID(group_name+" "+group_type));
		}
		dataset.setVersion(version_string);
		dataset.setCreator(addXML.class.getName());

		if (verbose) {
			log.error("Processing netCDF dataset: " + url);
		}

		String name = null;
		if ( esg ) {
			name = threddsDataset.getFullName();
		} else {
			Attribute nameAttribute = null;
			if (global_title_attribute == null) {
				nameAttribute = ncds.findGlobalAttributeIgnoreCase("long_name");
				if (nameAttribute == null) {
					nameAttribute = ncds.findGlobalAttributeIgnoreCase("title");
				}
			}
			else {
				nameAttribute = ncds.findGlobalAttributeIgnoreCase(global_title_attribute);
			}


			if (nameAttribute != null) {
				if (nameAttribute.isString()) {
					name = nameAttribute.getStringValue();
				}
			}

			if ( threddsDataset != null ) {
				if ( name == null ) {
					name = threddsDataset.getFullName();
				}
			}
		}

		GridDataset gridDs = null;
		StringBuilder error = new StringBuilder();
		try {
			gridDs = (GridDataset) TypedDatasetFactory.open(FeatureType.GRID, ncds, null, error);
		} catch (IOException e) {
			log.error("I/O Error converting data source to GridDataset");
		}
			//new GridDataset(ncds);
		if (name == null) {
			name = url;
		}
        log.debug("TypedDatasetFactory message " + error.toString());
		String elementName;
		if ( esg ) {
			elementName = threddsDataset.getID();
		} else {
			if ( threddsDataset != null && threddsDataset.getID() != null && !threddsDataset.getID().equals("") ) {
				elementName = threddsDataset.getID();
			} else {
				elementName = encodeID(url);
			}
		}

		dataset.setName(name);
		dataset.setElement(elementName);
		dataset.setUrl(url);

		List grids = new ArrayList();
		if ( gridDs != null ) {
		    grids = gridDs.getGrids();
		}
		if ( grids.size() == 0 ) {
			grids = gridDs.getGridsets();
		}

		if (grids.size() == 0) {
			dataset.setComment(
					"This data source has no lat/lon grids that follow a known convention.");
			log.error("File parsed.  No Lat/Lon grids found.");
		}
		for (int i = 0; i < grids.size(); i++) {
			/*
			 * A GridDataset can contain one or more GeoGrid objects each
			 * potentially defined on its own axes.
			 *
			 * A Java netCDF GeoGrid contains the same information the combination
			 * of an LAS variable, grid, and assoicated axes.
			 *
			 * To make the resulting XML fragment as compact as possible,
			 * this code attempts to get the smallest set of LAS grids
			 * necessary.
			 */

			UniqueVector GridAxisBeans = new UniqueVector();

			GridDatatype geogrid = (GeoGrid) grids.get(i);

			VariableBean variable = new VariableBean();
			variable.setUrl("#" + geogrid.getName());
			if (!geogrid.getDescription().equals(geogrid.getName())) {
				variable.setName(geogrid.getDescription());
			}
			else {
				variable.setName(geogrid.getName());
			}
			variable.setElement(geogrid.getName() + "-" + elementName);
			variable.setUnits(geogrid.getUnitsString());

			GridCoordSys gcs = (GridCoordSys) geogrid.getCoordinateSystem();

			GridBean grid = new GridBean();

			String grid_name = "grid";
			CoordinateAxis xAxis = gcs.getXHorizAxis();
			AxisBean xaxis;
			if ( xAxis instanceof CoordinateAxis1D ) {
				CoordinateAxis1D x = (CoordinateAxis1D) gcs.getXHorizAxis();
				xaxis = makeGeoAxis(x, "x", elementName);
			} else {
				CoordinateAxis2D x = (CoordinateAxis2D) gcs.getXHorizAxis();
				xaxis = makeGeoAxisFrom2D(x, "x", elementName);
				variable.setProperty("ferret", "curvi_coord_lon", x.getName());
				variable.setProperty("ferret", "curv_lon_min", String.valueOf(x.getMinValue()));
				variable.setProperty("ferret", "curv_lon_max", String.valueOf(x.getMaxValue()));
			}
			grid_name = grid_name + "-" + xAxis.getShortName();
			if (verbose) {
				log.error("\t Variable: " + geogrid.getName());
				log.error("\t\t Longitude axis: ");
			}
			GridAxisBeans.addUnique(xaxis);
			if (verbose) {
				log.error(xaxis.toString());
			}

			CoordinateAxis yAxis = gcs.getYHorizAxis();
			AxisBean yaxis;
			if ( yAxis instanceof CoordinateAxis1D ) {
				CoordinateAxis1D y = (CoordinateAxis1D) gcs.getYHorizAxis();
				yaxis = makeGeoAxis(y, "y", elementName);
			} else {
				CoordinateAxis2D y = (CoordinateAxis2D) gcs.getYHorizAxis();
				yaxis = makeGeoAxisFrom2D(y, "y", elementName);
				variable.setProperty("ferret", "curvi_coord_lat", y.getName());
			}

			grid_name = grid_name + "-" + yAxis.getShortName();
			if (verbose) {
				log.error("\t\t Latitude axis: ");
			}

			GridAxisBeans.addUnique(yaxis);
			if (verbose) {
				log.error(yaxis.toString());
			}
			if (gcs.hasVerticalAxis()) {
				CoordinateAxis1D zAxis = gcs.getVerticalAxis();
				grid_name = grid_name + "-" + zAxis.getShortName();
				if (verbose) {
					log.error("\t\t Vertical axis: ");
				}
				AxisBean zaxis = makeGeoAxis(zAxis, "z", elementName);
				GridAxisBeans.addUnique(zaxis);
				if (verbose) {
					log.error(zaxis.toString());
				}

			}
			else {
				if (verbose) {
					log.error("\t\t No vertical axis");
				}
			}

			CoordinateAxis1D tAxis = gcs.getTimeAxis1D();

			if (tAxis != null) {
				grid_name = grid_name + "-" + tAxis.getShortName();
				if (verbose) {
					log.error("\t\t Time axis: ");
				}
				AxisBean taxis = makeTimeAxis(tAxis, elementName);
				GridAxisBeans.addUnique(taxis);
				if (verbose) {
					log.error(taxis.toString());
				}
			}
			else {
				log.error("\t\t No time axis");
			}

			grid.setElement(grid_name + "-" + elementName);
			grid.setAxes(GridAxisBeans);
			variable.setGrid(grid);
			dataset.addVariable(variable);
			ProjectionImpl proj = geogrid.getProjection();

			if (verbose) {
				if (proj instanceof LatLonProjection) {
					log.error("\t\t Grid has LatLonProjection.");
				}
				else if (proj instanceof LambertConformal) {
					log.error("\t\t Grid has Lambert Conformal projection...");
				}
				else {
					log.error("\t\t Grid has unknown projection...");
				}
			}

			// Add the axis beans for this grid to the list of axis in this data source.
			for (Iterator abit = GridAxisBeans.iterator(); abit.hasNext(); ) {
				AxisBean ab = (AxisBean)abit.next();
				AxisBeans.addUnique(ab);
			}
			GridBeans.addUnique(grid);

		} // Loop over Grids
		DatasetBeans.add(dataset);
		dagb.setDatasets(DatasetBeans);
		dagb.setGrids(GridBeans);
		dagb.setAxes(AxisBeans);
		return dagb;
	}

	public static org.jdom.Document createXMLfromNetcdfDataset(NetcdfDataset
			ncds,
			String url) {
		DatasetsGridsAxesBean beans = createBeansFromNetcdfDataset(ncds, url, false, null);
		DatasetBean dataset = (DatasetBean) beans.getDatasets().get(0);
		Vector GridBeans = (Vector) beans.getGrids();
		Vector AxisBeans = (Vector) beans.getAxes();

		Document doc = new Document();
		Element lasdata = new Element("lasdata");
		doc.setRootElement(lasdata);
		Element datasetsElement = new Element("datasets");
		Element thisDataset = dataset.toXml();
		datasetsElement.addContent(thisDataset);
		lasdata.addContent(datasetsElement);
		Element gridsElement = new Element("grids");
		Iterator git = GridBeans.iterator();
		while (git.hasNext()) {
			GridBean gb = (GridBean) git.next();
			Element gridElement = gb.toXml();
			gridsElement.addContent(gridElement);
		}
		lasdata.addContent(gridsElement);

		Element axesElement = new Element("axes");
		Iterator ait = AxisBeans.iterator();
		while (ait.hasNext()) {
			AxisBean ab = (AxisBean) ait.next();
			Element axisElement = ab.toXml();
			axesElement.addContent(axisElement);
		}

		lasdata.addContent(axesElement);
		return doc;

	} // end of createXMLfromNetcdfDataset

	public static org.jdom.Document createXMLfromDatasetsGridsAxesBean(
			DatasetsGridsAxesBean beans) {

		Vector dataset = (Vector) beans.getDatasets();
		Vector GridBeans = (Vector) beans.getGrids();
		Vector AxisBeans = (Vector) beans.getAxes();

		Document doc = new Document();
		Element lasdata = new Element("lasdata");
		doc.setRootElement(lasdata);
		Element datasetsElement = new Element("datasets");
		Iterator dit = dataset.iterator();
		while (dit.hasNext()) {
			DatasetBean d = (DatasetBean) dit.next();
			Element thisDataset = d.toXml();
			datasetsElement.addContent(thisDataset);
		}
		lasdata.addContent(datasetsElement);
		Element gridsElement = new Element("grids");
		Iterator git = GridBeans.iterator();
		while (git.hasNext()) {
			GridBean gb = (GridBean) git.next();
			Element gridElement = gb.toXml();
			gridsElement.addContent(gridElement);
		}
		lasdata.addContent(gridsElement);

		Element axesElement = new Element("axes");
		Iterator ait = AxisBeans.iterator();
		while (ait.hasNext()) {
			AxisBean ab = (AxisBean) ait.next();
			Element axisElement = ab.toXml();
			axesElement.addContent(axisElement);
		}

		lasdata.addContent(axesElement);
		return doc;

	} // end of createXMLfromDatasetsGridsAxesBean

	static private AxisBean makeTimeAxis(CoordinateAxis1D axis, String id) {

		// LAS only understands time units of: 'year', 'month', 'day', and 'hour'


		String type = "t";
		AxisBean axisbean = new AxisBean();
		axisbean.setType(type);
		axisbean.setElement(axis.getShortName() + "-" + type + "-" + id);
		ArangeBean arange = new ArangeBean();


		String calendar = "standard";  // The mixed Gregorian/Julian calendar in Java and UDUNITS.
		Attribute calendarAttribute = axis.findAttribute("calendar");
		if (calendarAttribute != null) {
			calendar = calendarAttribute.getStringValue();
		}


		// Do a bunch of extra work to make this time axis into what LAS needs.

		boolean zeroOrigin = false;
		boolean useV = false;

		// Get a Java UDUNITS unit representation of this date string.
		String unitsString = axis.getUnitsString();

		unitsString = unitsString.trim();

		// If the units contains year 0000, we're going to have to
		// boost the dates over into year 1 so we don't have to deal
		// with negative years.  (There is no year 0 in the chronology
		// we're using.

		if (unitsString.indexOf("0000") >= 0) {
			unitsString.replaceFirst("0000", "0001");
			zeroOrigin = true;
		}

		// If it doesn't contain the word "since" and some ":" we probably can't
		// decode the times, so we're just going to dump them into the file.
		if (unitsString.toLowerCase().indexOf("since") < 0 && unitsString.indexOf(":") < 0  ) {
			useV = true;
		}
		// This if test checks to see if this units string ends in an alpha character
		// If it does, we're thinking there is some time zone stuff at the end that
		// should be chopped off.  It's probably a bug in SimpleUnit that should
		// be reported.

		while (unitsString.substring(unitsString.length()-1,unitsString.length()).matches("[a-zA-Z]")) {
			unitsString = unitsString.substring(0, unitsString.length()-1).trim();
		}



		// Units string is one of the bogus "day", "month", etc and there
		// is no time_origin attribute so just dump it out as <v> elements.
		if (useV) {
			double t[] = axis.getCoordValues();
			// In this case do not attempt to decode the time elements
			axisbean.setUnits(unitsString);
			axisbean.setArange(null);
			String ts[] = new String[t.length];
			for (int i = 0; i < t.length; i++) {
				ts[i] = String.valueOf(t[i]);
			}
			axisbean.setV(ts);
			return axisbean;
		}

		if ( format != null ) {
			try {
				fmt = DateTimeFormat.forPattern(format);
			} catch(IllegalArgumentException e) {
				fmt = null;
				log.error("Cannot parse supplied time format.  Will determine format instead.");
			}
		}
		DateUnit dateUnit = null;
		try {
			dateUnit = new DateUnit(unitsString);
		} catch (Exception e) {

			log.error("Cannot parse units string.");
		}

		if (dateUnit == null) {
			log.error("Not a date Unit String: " + unitsString);
		}

		// This is the Joda Time Chronology that cooresponds to the
		// Java Gregorian Calendar and the Udunits Calendar.

		// Use this chronology and the UTC Time Zone
		Chronology chrono = GJChronology.getInstance(DateTimeZone.UTC);

		// If calendar attribute is set, use appropriate Chronology.
		if (calendar.equals("proleptic_gregorian") ) {
			chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
		} else if (calendar.equals("noleap") || calendar.equals("365_day") ) {
			chrono = NoLeapChronology.getInstance(DateTimeZone.UTC);
		} else if (calendar.equals("julian") ) {
			chrono = JulianChronology.getInstance(DateTimeZone.UTC);
		} else if ( calendar.equals("all_leap") || calendar.equals("366_day") ) {
			chrono = AllLeapChronology.getInstance(DateTimeZone.UTC);
		} else if ( calendar.equals("360_day") ) {  /* aggiunto da lele */
			chrono = All360Chronology.getInstance(DateTimeZone.UTC);
		}

		if (axis.getSize() >= 2.) {
			// Only do this if the user specified the axis was irregular, meaning the axis span high frequency
			// and irregular like a time series from a sensor that reports often but at irregular intervals.
			
			if ( irregular ) {

				log.info("Time axis is irregular");
				fmt = DateTimeFormat.forPattern(patterns[4]);
				int length = (int) axis.getSize();
				double t0 = axis.getCoordValue(0);
				double t1 = axis.getCoordValue(length-1);
				DateTime jodaDate1 = makeDate(t0, dateUnit, chrono);
				DateTime jodaDate2 = makeDate(t1, dateUnit, chrono);
				Duration duration = new Duration(jodaDate1, jodaDate2);
				Period period = duration.toPeriod();
				Hours hours = period.toStandardHours();
				int hrs = hours.getHours();
				if (period.getMinutes() > 0 ) {
					hrs = hrs + 1;
				}
				arange.setSize(String.valueOf(hrs));
				arange.setStart(fmt.print(jodaDate1.withZone(DateTimeZone.UTC)));
				arange.setStep("1");
				axisbean.setUnits("hour");
				axisbean.setArange(arange);
			} else {
				
				
				// Returns the number of years, months, weeks, days,
				  // hours, minutes, seconds, and millis between these
				  // two dates.

				// Only one should be greater than 0.
				double t0 = axis.getCoordValue(0);
				double t1 = axis.getCoordValue(1);
				DateTime jodaDate1 = makeDate(t0, dateUnit, chrono);
				DateTime jodaDate2 = makeDate(t1, dateUnit, chrono);
				int step = 0;
				Period period =	new Period(jodaDate1.withZone(DateTimeZone.UTC), jodaDate2.withZone(DateTimeZone.UTC));
				int numPeriods = 0;
				String periods = "";
				int values[] = period.getValues();
				DurationFieldType types[] = period.getFieldTypes();
				for (int i = 0; i < values.length; i++) {
					if (values[i] > 0) {
						numPeriods++;
						// set if not set by command line arg.
						if (fmt == null ) {
							fmt = DateTimeFormat.forPattern(patterns[i]);
						}
						step = values[i];
						String typeName = types[i].getName();
						// Get rid of the "s" in the plural form of the name.
						typeName = typeName.substring(0, typeName.length() - 1);
						// LAS doesn't understand "week" so make it "day" and
						// multiply the step by 7.
						periods = periods + " " + typeName;
						if (typeName.equals("week")) {
							typeName = "day";
							step = step * 7;
						}
						axisbean.setUnits(typeName);
						arange.setStep(String.valueOf(step)); 
					}
				}




				if (numPeriods > 1) {

					// This is special code to deal with climatology files that define
					// the time axis in the "middle" of the month.  Since there is no
					// "middle" of months (but there is certainly always a
					// first of the month, geez-o) so we are left to figure this out by
					// looking at the period values and guessing that this really means
					// climo months.

					// Is the gap in years and months 0?
					// Are the values 4 weeks apart?
					if ( (values[0] == 0 && values[1] == 0) &&
							(values[2] == 4)) {
						// We're guessing these are months
						axisbean.setUnits("month");
						arange.setStep("1");
					} else if ( numPeriods == 2 && periods.contains("week") && periods.contains("day")  ) {
						// We can convert this to days. :-)
						axisbean.setUnits("day");
						step = 7*values[2] + values[3];
						arange.setStep(String.valueOf(step));

					} else {
						log.error("Too many periods: " + periods);
						//Try just dumping out the formatted times
						axisbean.setArange(null);
						double t[] = axis.getCoordValues();
						String ts[] = new String[t.length];
						// We don't know what these times look like.  Use a format with everything.
						if (fmt == null ) {
							fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
						}
						for (int i = 0; i < t.length; i++) {
							DateTime dt = makeDate(t[i], dateUnit, chrono);
							ts[i] = fmt.print(dt.withZone(DateTimeZone.UTC));
						}
						axisbean.setV(ts);

					}
				}
				Boolean forceAxis = (Boolean) forceAxes.get("t");
				if ( (axis.isRegular() || axisbean.getUnits().equals("month")) ||
						forceAxis.booleanValue()) {

					// Months are "regular" according to LAS, but not according
					// to the isRegular() test, so we need special code for
					// months.


					// This format should work.  LAS will drop the "day" if it's not needed.
					if (fmt == null ) {
						fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
					}
					arange.setSize(String.valueOf(axis.getSize()));
					String str = fmt.print(jodaDate1.withZone(DateTimeZone.UTC));
					if ( str.startsWith("-") ) str = str.substring(1, str.length());
					arange.setStart(str);
					axisbean.setArange(arange);
				}
				else {
					// Layout the time axis using "v" elements.

					axisbean.setArange(null);
					double t[] = axis.getCoordValues();
					String ts[] = new String[t.length];
					for (int i = 0; i < t.length; i++) {
						DateTime dt = makeDate(t[i], dateUnit, chrono);
						ts[i] = fmt.print(dt.withZone(DateTimeZone.UTC));
					}
					axisbean.setV(ts);

				}

				axisbean.setArange(arange);
			}
		}
		else {
			// Time axis has only one time step.
			// We have no idea what this single time point looks like so,
			// format it for all year, month, day, hours, minutes, seconds
			fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			double t0 = axis.getCoordValue(0);
			DateTime jodaDate1 = makeDate(t0, dateUnit, chrono);
			arange.setSize("1");
			arange.setStep("1");
			arange.setStart(fmt.print(jodaDate1));
			// The UI will eliminate this axis.  I think it's safe to set
			// the units to anything.  !?!?
			axisbean.setUnits("day");
			axisbean.setArange(arange);
		}

		return axisbean;
	}

	private static DateTime makeDate(double d, DateUnit dateUnit, Chronology chrono) {
		// Extract the bits and pieces from the dataUnit
		String pstring = dateUnit.getTimeUnitString().toLowerCase();
		int years = 0;
		int months = 0;
		int weeks = 0;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		int millis = 0;
		Period period = new Period(years, months, weeks, days, hours, minutes, seconds, millis);
		
		// Years
		if (pstring.contains("year") ) {
			years = Double.valueOf(d).intValue();
			period = new Period(years, months, weeks, days, hours, minutes, seconds, millis);
		} else if (pstring.contains("month") ) {
			// This is a really bad idea...
			months = Double.valueOf(d).intValue();
			period = new Period(years, months, weeks, days, hours, minutes, seconds, millis);
		} else if ( pstring.contains("week") ) {
			weeks = Double.valueOf(d).intValue();
			period = new Period(years, months, weeks, days, hours, minutes, seconds, millis);
		} else if ( pstring.contains("day") ) {
			Double double_day = Double.valueOf(d).doubleValue();
			Double ceiling = Math.ceil(double_day);
			Double remainder_hour = 0.;
			if ( ceiling > double_day ) { 
				remainder_hour = (double_day - Math.floor(double_day))*24.;
				hours = Double.valueOf(remainder_hour).intValue();
			}
			days = Double.valueOf(d).intValue();
			period = new Period(years, months, weeks, days, hours, minutes, seconds, millis);
		} else if ( pstring.contains("hour") ) {
			Double double_hour = Double.valueOf(d);
			Double ceiling = Math.ceil(double_hour);
			Double remainder_min = 0.;
			if ( ceiling > double_hour ) {
				remainder_min = (double_hour - Math.floor(double_hour))*60.;
				minutes = Double.valueOf(remainder_min).intValue();
			}
			// Use millis for hours, minutes, seconds and millis since they are more likely to overflow an int.
			long milli = Double.valueOf(d).longValue()*3600*1000;
			period = new Period(milli);
		} else if ( pstring.contains("minute") ) {
			long milli = Double.valueOf(d).longValue()*60*1000;
			period = new Period(milli);
		} else if ( pstring.contains("sec") ) {
			long milli = Double.valueOf(d).longValue()*1000;
			period = new Period(milli);
		} else if ( pstring.contains("milli") ) {
			long milli = Double.valueOf(d).longValue();
			period = new Period(milli);
		} else {
			log.error("Could not figure out the base time interval for this units string. "+pstring+" does not appear to be year, month, week, day, hour, minute, second or milliseconds.");
		}

		DateTime origin = getOrigin(dateUnit, chrono);

		 
		DateTime date = new DateTime(chrono);
		date = (origin).plus(period).withChronology(chrono);
		return date;
	}
	static private DateTime getOrigin(DateUnit dateUnit, Chronology chrono) {
		String units_string = dateUnit.getUnitsString();
		String origin_string = units_string.substring(units_string.indexOf("since")+5, units_string.length() ).trim();
		DateTimeFormatter fmt;
		if ( units_format != null ) {
			fmt = DateTimeFormat.forPattern(units_format);
		} else {
			fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		}
		DateTimeFormatter chrono_fmt = fmt.withChronology(chrono);
		DateTimeFormatter fmt_iso = ISODateTimeFormat.dateTime();
		DateTimeFormatter chrono_fmt_iso = fmt_iso.withChronology(chrono);
		DateTime origin;

		// Assume for now this is just like UDUNITS and Java so
		// get the date from the dateUnit object.
		origin = new DateTime(dateUnit.getDate(), chrono);

		if ( !(chrono instanceof GJChronology) ) {
			// Whoops, it a different chronology, try to parse it yourself
			try {
				origin = chrono_fmt.parseDateTime(origin_string);
			} catch ( UnsupportedOperationException  uoe) {
				try {
					origin = chrono_fmt_iso.parseDateTime(origin_string).withChronology(GregorianChronology.getInstanceUTC());
				} catch ( UnsupportedOperationException  uoe2) {
					log.error("Could not parse "+origin_string+" with yyyy-MM-dd'T'HH:mm:ss.SSSZ or yyyy-MM-dd HH:mm:ss.  Use -f option to give format of time string.");
				} catch (IllegalArgumentException iae2 ) {
					log.error("Could not parse "+origin_string+" with yyyy-MM-dd'T'HH:mm:ss.SSSZ or yyyy-MM-dd HH:mm:ss.  Use -f option to give format of time string.");
				}
			} catch (IllegalArgumentException iae ) {
				try {
					origin = chrono_fmt_iso.parseDateTime(origin_string).withChronology(chrono);
				} catch ( UnsupportedOperationException  uoe2) {
					log.error("Could not parse "+origin_string+" with yyyy-MM-dd'T'HH:mm:ss.SSSZ or yyyy-MM-dd HH:mm:ss");
				} catch (IllegalArgumentException iae2 ) {
					log.error("Could not parse "+origin_string+" with yyyy-MM-dd'T'HH:mm:ss.SSSZ or yyyy-MM-dd HH:mm:ss");
				}
			}
		}
		return origin;
	}
	static private AxisBean makeGeoAxisFrom2D(CoordinateAxis2D axis, String type, String id) {    
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		DecimalFormat fmt = (DecimalFormat) nf;
		fmt.applyPattern("####.####");

		AxisBean axisbean = new AxisBean();
		axisbean.setElement(axis.getShortName() + "-" + type + "-" + id);
		axisbean.setType(type);
		// Since the axis is curvi, represent it as a 1-degree arange.
		double min = axis.getMinValue();
		double max = axis.getMaxValue();
		int size = (int)(max-min);
		if (size > 360) {
			size = 360;
		}
		ArangeBean arange = new ArangeBean();
		axisbean.setUnits(axis.getUnitsString());
		arange.setSize(String.valueOf(size));
		arange.setStep("1.0");
		arange.setStart(fmt.format(min));
		axisbean.setArange(arange);
		return axisbean;
	}
	static private AxisBean makeGeoAxis(CoordinateAxis1D axis, String type, String id) {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		DecimalFormat fmt = (DecimalFormat) nf;
		fmt.applyPattern("####.####");
		AxisBean axisbean = new AxisBean();
		axisbean.setType(type);
		axisbean.setElement(axis.getShortName() + "-" + type + "-" + id);
		ArangeBean arange = new ArangeBean();
		axisbean.setUnits(axis.getUnitsString());
		Boolean forceAxis = (Boolean) forceAxes.get(type);
		boolean force = false;
		if ( forceAxis != null ) {
			force = forceAxis.booleanValue();
		}
		if ( (axis.isRegular() && axis.getSize() > 1) || force) {
			arange.setSize(String.valueOf(axis.getSize()));
			double delta = axis.getIncrement();
			double start = axis.getStart();
			// Flip so that the axis is South to North
			if (delta < 0 && type.equals("y")) {
				start = start + (axis.getSize() - 1) * delta;
				delta = -delta;
			}
			arange.setStep(fmt.format(delta));
			arange.setStart(fmt.format(start));
			axisbean.setArange(arange);
		}
		else {
			double[] v = axis.getCoordValues();

			// List Latitude south to north
			if (v[0] > v[v.length - 1] && type.equals("y")) {
				reverse(v);
			}
			axisbean.setArange(null);

			/** @todo we want to use 4-log[base10](max-min). */
			String[] vs = new String[v.length];
			for (int i = 0; i < v.length; i++) {
				vs[i] = fmt.format(v[i]);
			}
			axisbean.setV(vs);

		}
		return axisbean;

	}

	/**
	 * The byte[] returned by MessageDigest does not have a nice
	 * textual representation, so some form of encoding is usually performed.
	 *
	 * This implementation follows the example of David Flanagan's book
	 * "Java In A Nutshell", and converts a byte array into a String
	 * of hex characters.
	 *
	 * Another popular alternative is to use a "Base64" encoding.
	 */
	static private String hexEncode(byte[] aInput) {
		StringBuffer result = new StringBuffer();
		char[] digits = {
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
				'e', 'f'};
		for (int idx = 0; idx < aInput.length; ++idx) {
			byte b = aInput[idx];
			result.append(digits[ (b & 0xf0) >> 4]);
			result.append(digits[b & 0x0f]);
		}
		return result.toString();
	}

//	========================================================= reverse
//	Stolen directly off the web from:
//	http://leepoint.net/notes-java/data/arrays/34arrayreverse.html
//	Converted from int array to double...
	public static void reverse(double[] b) {
		int left = 0; // index of leftmost element
		int right = b.length - 1; // index of rightmost element

		while (left < right) {
			// exchange the left and right elements
			double temp = b[left];
			b[left] = b[right];
			b[right] = temp;

			// move the bounds toward the center
			left++;
			right--;
		}
	} //endmethod reverse

	static public void outputXML(String outfile, Document doc) {
		try {
			File outputFile = new File(outfile);
			FileWriter xmlout = new FileWriter(outputFile);
			org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
			format.setLineSeparator(System.getProperty("line.separator"));
			XMLOutputter outputter =
				new XMLOutputter(format);
			outputter.output(doc, xmlout);
			// Close the FileWriter
			xmlout.close();
		}
		catch (java.io.IOException e) {
			log.error(e.getMessage());
		}
	}

	static public void outputXML(String outfile, Element element, boolean append) {
		try {
			File outputFile = new File(outfile);
			FileWriter xmlout = new FileWriter(outputFile, append);
			org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
			format.setLineSeparator(System.getProperty("line.separator"));
			XMLOutputter outputter =
				new XMLOutputter(format);
			outputter.output(element, xmlout);
			xmlout.write("\n");
			// Close the FileWriter
			xmlout.close();
		}
		catch (java.io.IOException e) {
			log.error(e.getMessage());
		}
	}
	private static String encodeID (String in) {
		String encoding;
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			byte[] result = md.digest(in.getBytes());
			encoding =
				"id-" +
				hexEncode(result).substring(result.length / 2, result.length);
		}
		catch (NoSuchAlgorithmException e) {
			log.error("Cannot create SHA-1 hash." + e.getMessage());
			encoding = "id-12345";
		}
		return encoding;
	}
	private void jbInit() throws Exception {
	}
	public void resetOptions() {
		title = null;
		global_title_attribute = null;
		format = null;
		units_format = null;
		group_type = null;
		group_name = null;
		group = false;
		category = false;
		use_suffix = false;
		oneDataset = false;
		irregular = false;
		esg = false;
	}
	public void setOptions(HashMap<String, String> options) {
		forceAxes.put("x", new Boolean(false));
		forceAxes.put("y", new Boolean(false));
		forceAxes.put("z", new Boolean(false));
		forceAxes.put("t", new Boolean(false));
		String forceAxisOption = options.get("force");
		if ( forceAxisOption != null && !forceAxisOption.equals("") ) {
			String ax[] = forceAxisOption.split(",");
			for (int a = 0; a < ax.length; a++) {
				if (ax[a].equals("x") || ax[a].equals("y") ||
						ax[a].equals("z") || ax[a].equals("t")) {
					forceAxes.put(ax[a], new Boolean(true));
				}
				else {
					// Ignore unknown axes.
				}
			}
		}

		String titleOption = options.get("title");
		if ( titleOption != null && !titleOption.equals("") ) {
			title = titleOption;
		} 

		String global_title_attributeOption = options.get("global_title_attribute");
		if ( global_title_attributeOption != null && !global_title_attributeOption.equals("") ) {
			global_title_attribute = global_title_attributeOption;
		}

		String formatOption = options.get("format");
		if ( formatOption != null && !formatOption.equals("") ) {
			format = formatOption;
		}

		String units_formatOption = options.get("units_format");
		if ( units_formatOption != null && !units_formatOption.equals("") ) {
			units_format = units_formatOption;
		}

		String group_typeOption = options.get("group_type");
		if ( group_typeOption != null && !group_typeOption.equals("") ) {
			group_type = group_typeOption;
		}

		String group_nameOption = options.get("group_name");
		if ( group_nameOption != null && !group_nameOption.equals("") ) {
			group_name = group_nameOption;
		}

		String groupOption = options.get("group");
		if ( groupOption != null && (groupOption.equalsIgnoreCase("true") || groupOption.equalsIgnoreCase("false")) ) {
			group = Boolean.valueOf(groupOption).booleanValue();
		}

		String categoryOption = options.get("category");
		if ( categoryOption != null && (categoryOption.equalsIgnoreCase("true") || categoryOption.equalsIgnoreCase("false")) ) {
			category = Boolean.valueOf(categoryOption).booleanValue();
		}

		String use_suffixOption = options.get("use_suffix");
		if ( use_suffixOption != null && (use_suffixOption.equalsIgnoreCase("true") || use_suffixOption.equalsIgnoreCase("false")) ) {
			use_suffix = Boolean.valueOf(use_suffixOption).booleanValue();
		}

		String oneDatasetOption = options.get("oneDataset");
		if ( oneDatasetOption != null && (oneDatasetOption.equalsIgnoreCase("true") || oneDatasetOption.equalsIgnoreCase("false")) ) {
			oneDataset = Boolean.valueOf(oneDatasetOption).booleanValue();
		}

		String irregularOption = options.get("irregular");
		if ( irregularOption != null && (irregularOption.equalsIgnoreCase("true") || irregularOption.equalsIgnoreCase("false")) ) {
			irregular = Boolean.valueOf(irregularOption).booleanValue();
		}

		String esgOption = options.get("esg");
		if ( esgOption != null && (esgOption.equalsIgnoreCase("true") || esgOption.equalsIgnoreCase("false")) ) {
			esg = Boolean.valueOf(esgOption).booleanValue();
		}
	}

} // end of class
