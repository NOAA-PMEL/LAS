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

import gov.noaa.pmel.tmap.jdom.LASDocument;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

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
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Hours;
import org.joda.time.Period;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.JulianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDocumentation;
import thredds.catalog.InvProperty;
import thredds.catalog.ServiceType;
import thredds.catalog.ThreddsMetadata.GeospatialCoverage;
import thredds.catalog.ThreddsMetadata.Range;
import thredds.catalog.ThreddsMetadata.Variable;
import thredds.catalog.ThreddsMetadata.Variables;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dods.DODSNetcdfFile;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.TrajectoryObsDataset;
import ucar.nc2.dt.grid.GridCoordSys;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.units.DateRange;
import ucar.nc2.units.DateType;
import ucar.nc2.units.DateUnit;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.projection.LambertConformal;
import ucar.unidata.geoloc.projection.LatLonProjection;
import uk.ac.rdg.resc.edal.time.AllLeapChronology;
import uk.ac.rdg.resc.edal.time.NoLeapChronology;
import uk.ac.rdg.resc.edal.time.ThreeSixtyDayChronology;

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
public class ADDXMLProcessor {

    private static final String patterns[] = {
        "yyyy-MM-dd", "yyyy-MM-dd", "yyyy-MM-dd", "yyyy-MM-dd",
        "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};

    private static final DecimalFormat decimalFormat = new DecimalFormat("###############.###############");

    private static final String pattern_with_hours = "yyyy-MM-dd HH:mm:ss";

    private static DateTimeFormatter ferret_time_formatter = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss");

    // Constants
    private static final String Z_VALUES = "updownValues";
    private static final String ZVALUES = "zvalues";
    private static final String ESGF_Z_VALUES = "z_values";

    private static boolean verbose;
    private static boolean generate_names;
    private static int fileCount;
    private static HashMap<String, Boolean> forceAxes = new HashMap<String, Boolean>();
    private static String title;
    private static String version_string = "1.7.3.0";
    private static String global_title_attribute;
    private static String format;
    private static String units_format;
    private static String group_type;
    private static String group_name;
    private static boolean group;
    private static boolean category;
    private static boolean use_suffix = false;
    private static boolean oneDataset = false;
    private static boolean irregular = false;
    private static boolean esg = false;
    private static boolean uaf = false;
    private static int limit = 0;
    private static String[] clregex;
    private static List<String> regex = new ArrayList<String>();

    public ADDXMLProcessor() {
        
    }

    public void run(String[] args) {

        forceAxes.put("x", new Boolean(false));
        forceAxes.put("y", new Boolean(false));
        forceAxes.put("z", new Boolean(false));
        forceAxes.put("t", new Boolean(false));

        LAS_JSAP command_parser = new LAS_JSAP();

        JSAPResult command_parameters = command_parser.parse(args);

        if (!command_parameters.success()) {
            command_parser.errorout(command_parameters);
        }
        
        String l = command_parameters.getString("limit");
        if ( l != null ) {
            limit = Integer.valueOf(l);
        }

        clregex = command_parameters.getStringArray("in_regex");
        for ( int i = 0; i < clregex.length; i++ ) {
            regex.add(clregex[i]);
        }
        URL skipFile = ClassLoader.getSystemResource("skip.xml");
        if ( skipFile != null ) {
            Document skipdoc = new Document();
            try {
                JDOMUtils.XML2JDOM(new File(skipFile.getFile()), skipdoc);
            } catch ( IOException e ) {
                System.err.println("Failed reading skip file. "+e);
            } catch ( JDOMException e ) {
                System.err.println("Failed reading skip file. "+e);
            }

            Element skip = skipdoc.getRootElement();
            List<Element> regexEls = skip.getChildren("regex");
            for ( Iterator rIt = regexEls.iterator(); rIt.hasNext(); ) {
                Element rx = (Element) rIt.next();
                String value = rx.getAttributeValue("value");
                if ( value != null ) {
                    regex.add(value);
                }
            }
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
                    System.err.println("Ignoring axis " + ax[a] +
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
                    System.err.println("Ignoring group with type " + grp[g] +
                    " on the --group_type option. Must be ensemble or time_series.");
                }
            }
        }

        group_name = command_parameters.getString("groupname");

        verbose = command_parameters.getBoolean("verbose");
        generate_names = command_parameters.getBoolean("generate_names");
        boolean version = command_parameters.getBoolean("version");
        irregular = command_parameters.getBoolean("irregular");
        

        esg = command_parameters.getBoolean("esg");
        
        uaf = command_parameters.getBoolean("metadata");

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
                System.err.println("Error loading authentication credentials provider class.");
                System.err.println(ee.getMessage());
                System.exit(1);
            }

            // Instantiate the credentials provider object.
            try{
                provider_obj = (LASCredentialsProvider)provider_class.newInstance();
            }catch(Exception ee){
                System.err.println("Error creating authentication credentials provider object.");
                System.err.println(ee.getMessage());
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
        //      ucar.nc2.dataset.HttpClientManager.init(provider_obj,"addXML-" + version_string);   

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

        oneDb.setCreator(ADDXMLProcessor.class.getName());
        oneDb.setVersion(version_string);
        // Every variable in this dataset object will have it's own fully qualified
        // data URL.  Set the data set URL to null.
        oneDb.setUrl(null);
        if (title != null && title != "") {
            oneDb.setName(title);
            oneCat.setName(title);
        }

        if (version) {
            System.err.println("Version: " + version_string);
        }

        if (data.length == 0 && thredds.length == 0) {
            System.err.println("");
            System.err.println("You must specify either");
            System.err.println("\ta THREDDS catalog with the -t option or ");
            System.err.println("\ta netCDF data source with the -n option.");
            System.err.println("");
            System.err.println("Usage: addXML.sh ");
            System.err.println(command_parser.getUsage());
            System.err.println("");
            System.err.println("");
            System.err.println(command_parser.getHelp());
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
                System.err.println(ex.getMessage());
                inputLasDoc = null;
            }
            catch (JDOMException ex) {
                System.err.println(ex.getMessage());
                inputLasDoc = null;
            }
        }

        int numThredds = 0;
        int numNetcdf = 0;

        for (int id = 0; id < data.length; id++) {

            DatasetsGridsAxesBean dgab = createBeansFromNetcdfDataset(data[id], false, null);
            if ( dgab != null ) {
                Vector db = (Vector) dgab.getDatasets();
                if (db != null && db.size() > 0) {

                    if (oneDataset) {
                        // We're only going to use one data set.  Accumulate all the info
                        // into that one data set.
                        DatasetBean databean = (DatasetBean) dgab.getDatasets().get(0);

                        // Check to see if the name has been set.
                        if (oneDb.getName() == null || oneDb.getName().equals("")) {
                            oneDb.setName(databean.getName());
                            // If the data set name wasn't set neither was the category
                            oneCat.setName(databean.getName());
                        }
                        
                        if ( category ) {
                            // Set the category filter to include this data set.
                            FilterBean filter = new FilterBean();
                            filter.setAction("apply-dataset");
                            filter.setContainstag(databean.getElement());
                            oneCat.addFilter(filter);
                        }
                        ArrayList variables = (ArrayList) databean.getVariables();
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
                                for (Iterator cit = datasets.getChildren().iterator(); cit.hasNext(); ) {
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
            String xml = thredds[it];
            if ( thredds[it].endsWith(".html") ) {
                xml = thredds[it].replace(".html", ".xml");
                System.err.println("Changeing "+thredds[it]+" to "+xml);
            }
            Document lasdoc = LASConfig(xml, "thredds");
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
            System.err.println("");
            System.err.println("No grids were found in the input data sets.");
            System.err.println(
            "Check to see if the OPeNDAP servers being referenced are running.");
            System.err.println(
            "Verify that the netCDF files referenced are COARDS or CF compliant.");
            System.err.println("");
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

    public Document LASConfig(String data, String type) {

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
                System.err.println("Invalid catalog <" + data + ">\n" + buff.toString());
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
      System.err.println(ex.getMessage()+" "+data);
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
                System.err.println("No more that 999 data sets to process.  Please.");
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
    public LASDocument createXMLfromTHREDDSCatalog(InvCatalog catalog) {

        CategoryBean top = new CategoryBean();
        String topName = catalog.getName();
        if (topName != null) {
            top.setName(catalog.getName());
        }
        else {
            top.setName(catalog.getUriString());
        }
        Set<DatasetBean> DatasetBeans = new HashSet<DatasetBean>();
        Set<GridBean> GridBeans = new HashSet<GridBean>();
        Set<AxisBean> AxisBeans = new HashSet<AxisBean>();
        Vector CategoryBeans = new Vector();

        
            List ThreddsDatasets = catalog.getDatasets();
            Iterator di = ThreddsDatasets.iterator();
            if ( category ) {
                while (di.hasNext()) {
                    InvDataset ThreddsDataset = (InvDataset) di.next();
                    CategoryBean cb = null;
                    if ( esg ) {
                        // Do this below for ESG...
                    } else if ( uaf ) {
                        cb = processUAFCategories(ThreddsDataset);
                    }   else {
                        cb = processCategories(ThreddsDataset);
                    } 
                    if ( cb != null && (cb.getCategories().size() > 0 || cb.getFilters().size() > 0 )) {
                        CategoryBeans.add(cb);
                    }
                }
            }

        // Discover and process all the THREDDS dataset elements that actually
        // connect to a data source.
        Vector DGABeans = new Vector();
        ThreddsDatasets = catalog.getDatasets();
        di = ThreddsDatasets.iterator();
        while (di.hasNext()) {
            InvDataset ThreddsDataset = (InvDataset) di.next();
            Vector d = processDatasets(ThreddsDataset);
            DGABeans.addAll(d);
        }

        // Each THREDDS "dataset" is a separate LAS data set.
        // If oneDataset is true, combine them before making the XML.

        if (oneDataset && DGABeans.size() > 0) {
            Vector newDAGBVector = new Vector();
            DatasetsGridsAxesBean newDAGB = new DatasetsGridsAxesBean();
            
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

            Vector newGrids = new Vector();
            Vector newAxes = new Vector();
            for (Iterator dgabIter = DGABeans.iterator(); dgabIter.hasNext();) {
                DatasetsGridsAxesBean aDAGB = (DatasetsGridsAxesBean) dgabIter.next();
                Vector datasets = aDAGB.getDatasets();            
                for (Iterator ds = datasets.iterator(); ds.hasNext();) {
                    DatasetBean dsb = (DatasetBean) ds.next();
                    if (newDSB.getElement() == null) {
                        newDSB.setElement(dsb.getElement());
                    }
                    newDSB.setName(dsb.getName());
                    ArrayList oldVars = dsb.getVariables();
                    ArrayList newVariables = new ArrayList();
            
                    for (Iterator ovIt = oldVars.iterator(); ovIt.hasNext();) {
                        VariableBean var = (VariableBean) ovIt.next();
                        GridBean g = var.getGrid();
                        if (!newGrids.contains(g)) newGrids.add(g);
                        Vector axes = g.getAxes();
                        for (Iterator axesIt = axes.iterator(); axesIt.hasNext(); ) {
                            AxisBean a = (AxisBean) axesIt.next();
                            if ( !newAxes.contains(a) ) newAxes.add(a);
                        }
                        String durl = dsb.getUrl();
                        String vurl = var.getUrl();
                        if ( vurl.startsWith("#") ) {
                            var.setUrl(durl+vurl);
                        } else {
                            var.setUrl(vurl);
                        }
                        
                        newVariables.add(var);
                    }
                    
                    
                    newDSB.setUrl(null);
                    newDSB.addAllVariables(newVariables);
                }
            }
            Vector newDatasets = new Vector();
            newDatasets.add(newDSB);
            newDAGB.setDatasets(newDatasets);
            newDAGB.setGrids(newGrids);
            newDAGB.setAxes(newAxes);
            newDAGBVector.add(newDAGB);
            DGABeans = newDAGBVector;
        }

        if ( category ) {
            top.setCategories(CategoryBeans);
            // create las_categories and datasets elements at this level
        }
        LASDocument doc = new LASDocument();
        Element lasdata = new Element("lasdata");
        Element datasetsElement = new Element("datasets");
        Element gridsElement = new Element("grids");
        Element axesElement = new Element("axes");

        Iterator dgabit = DGABeans.iterator();
        while (dgabit.hasNext()) {
            DatasetsGridsAxesBean dgab_temp = (DatasetsGridsAxesBean) dgabit.next();
            if ( dgab_temp != null ) {
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
        }
        if ( category ) {
            Element las_categories = new Element("las_categories");
            Element topElement = top.toXml();
            las_categories.addContent(topElement);
            lasdata.addContent(las_categories);
        }
        lasdata.addContent(datasetsElement);
        lasdata.addContent(gridsElement);
        lasdata.addContent(axesElement);
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
        if ( uaf ) {
            if ( ThreddsDataset.hasAccess() && ThreddsDataset.getAccess(ServiceType.OPENDAP) != null ) {
                if ( !ThreddsDataset.getName().contains("automated cleaning process") ) {
                    DatasetsGridsAxesBean dgab = createBeansFromUAFThreddsMetadata(ThreddsDataset);
                    beans.add(dgab);
                }
            }
            for (Iterator iter = ThreddsDataset.getDatasets().iterator();iter.hasNext(); ) {
                beans.addAll(processDatasets( (InvDataset) iter.next()));
            }
        } else if ( esg ) {
            if (ThreddsDataset.hasAccess() && ThreddsDataset.getAccess(ServiceType.OPENDAP) != null ) {
                DatasetsGridsAxesBean dgab = createBeansFromThreddsDataset(ThreddsDataset, ThreddsDataset.getAccess(ServiceType.OPENDAP) );
                beans.add(dgab);
            }
            for (Iterator iter = ThreddsDataset.getDatasets().iterator(); iter.hasNext(); ) {
                beans.addAll(processDatasets( (InvDataset) iter.next()));
            }
        } else {
            if (ThreddsDataset.hasAccess() && ThreddsDataset.getAccess(ServiceType.OPENDAP) != null ) {
                DatasetsGridsAxesBean dgab = createBeansFromNetcdfDataset(ThreddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName(), false, ThreddsDataset);
                beans.add(dgab);
            }
            for (Iterator iter = ThreddsDataset.getDatasets().iterator(); iter.hasNext(); ) {
                beans.addAll(processDatasets( (InvDataset) iter.next()));
            }
        }
        return beans;
    }
    public static boolean containsLASDatasets(InvCatalog subCatalog) {
        boolean hasData = false;
        for (Iterator topLevelIt = subCatalog.getDatasets().iterator(); topLevelIt.hasNext(); ) {
            InvDataset topDS = (InvDataset) topLevelIt.next();
            for (Iterator subDatasetsIt = topDS.getDatasets().iterator(); subDatasetsIt.hasNext(); ) {
                InvDataset subDataset = (InvDataset) subDatasetsIt.next();
                if ( !subDataset.hasNestedDatasets() && subDataset.hasAccess() && subDataset.getName().contains("aggregation")) {
                    InvAccess access = null;
                    for (Iterator ait = subDataset.getAccess().iterator(); ait.hasNext(); ) {
                        access = (InvAccess) ait.next();
                        if (access.getService().getServiceType() == ServiceType.DODS ||
                                access.getService().getServiceType() == ServiceType.OPENDAP ||
                                access.getService().getServiceType() == ServiceType.NETCDF) {
                            return true;
                        }
                    }
                } else {
                    for (Iterator grandChildrenIt = subDataset.getDatasets().iterator(); grandChildrenIt.hasNext(); ) {
                        InvDataset grandChild = (InvDataset) grandChildrenIt.next();
                        if ( grandChild.hasAccess() && grandChild.getName().contains("aggregation")) {
                            InvAccess access = null;
                            for (Iterator ait = grandChild.getAccess().iterator(); ait.hasNext(); ) {
                                access = (InvAccess) ait.next();
                                if (access.getService().getServiceType() == ServiceType.DODS ||
                                        access.getService().getServiceType() == ServiceType.OPENDAP ||
                                        access.getService().getServiceType() == ServiceType.NETCDF) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return hasData;
    }
    public static Vector processESGDatasets(Set<String> time_freqs, InvCatalog subCatalog) {

        Vector beans = new Vector();
        DatasetsGridsAxesBean bean = new DatasetsGridsAxesBean();
        DatasetBean ds = new DatasetBean();
        Vector datasets = new Vector();
        Vector allGrids = new Vector();
        Vector allAxes = new Vector();
        ArrayList<VariableBean> variables = new ArrayList<VariableBean>();
        for (Iterator topLevelIt = subCatalog.getDatasets().iterator(); topLevelIt.hasNext(); ) {
            InvDataset topDS = (InvDataset) topLevelIt.next();
            ds.setName(topDS.getName());
            ds.setElement(topDS.getID());
            for (Iterator subDatasetsIt = topDS.getDatasets().iterator(); subDatasetsIt.hasNext(); ) {
                InvDataset subDataset = (InvDataset) subDatasetsIt.next();
                if ( !subDataset.hasNestedDatasets() && subDataset.hasAccess() && subDataset.getName().contains("aggregation")) {
                    // We are done.
                    String url = null;
                    InvAccess access = null;
                    for (Iterator ait = subDataset.getAccess().iterator(); ait.hasNext(); ) {
                        access = (InvAccess) ait.next();
                        if (access.getService().getServiceType() == ServiceType.DODS ||
                                access.getService().getServiceType() == ServiceType.OPENDAP ||
                                access.getService().getServiceType() == ServiceType.NETCDF) {
                            url = access.getStandardUrlName();
                        }
                    }
                    DatasetsGridsAxesBean dgab = createBeansFromThreddsMetadata(time_freqs, subDataset, url, allGrids, allAxes);
                    if ( dgab != null ) {
                        for (Iterator dsit = dgab.getDatasets().iterator(); dsit.hasNext();) {
                            DatasetBean dsb = (DatasetBean) dsit.next();
                            variables.addAll(dsb.getVariables());
                        }
                    }
                } else {
                    // These will be the catalog containers that will contain the aggregations...
                    for (Iterator grandChildrenIt = subDataset.getDatasets().iterator(); grandChildrenIt.hasNext(); ) {
                        InvDataset grandChild = (InvDataset) grandChildrenIt.next();
                        if ( grandChild.hasAccess() && grandChild.getName().contains("aggregation")) {
                            // We are done.
                            String url = null;
                            InvAccess access = null;
                            for (Iterator ait = grandChild.getAccess().iterator(); ait.hasNext(); ) {
                                access = (InvAccess) ait.next();
                                if (access.getService().getServiceType() == ServiceType.DODS ||
                                        access.getService().getServiceType() == ServiceType.OPENDAP ||
                                        access.getService().getServiceType() == ServiceType.NETCDF) {
                                    url = access.getStandardUrlName();
                                }
                            }
                            DatasetsGridsAxesBean dgab = createBeansFromThreddsMetadata(time_freqs, grandChild, url, allGrids, allAxes);
                            if ( dgab != null ) {
                                for (Iterator dsit = dgab.getDatasets().iterator(); dsit.hasNext();) {
                                    DatasetBean dsb = (DatasetBean) dsit.next();
                                    variables.addAll(dsb.getVariables());
                                }
                            }
                        } 
                    }
                }
            }
        }
        ds.setVariables(variables);
        datasets.add(ds);
        bean.setDatasets(datasets);
        bean.setGrids(allGrids);
        bean.setAxes(allAxes);
        beans.add(bean);
        return beans;
    }
    /**
     * createDatasetBeanFromThreddsDataset
     *
     * @param ThreddsDataset InvDataset
     * @return DatasetBean
     */
    private static DatasetsGridsAxesBean createBeansFromThreddsDataset(InvDataset threddsDataset, InvAccess access) {
        DatasetsGridsAxesBean dgab = new DatasetsGridsAxesBean();

        String url = access.getStandardUrlName();

        if ( esg ) {
            if ( url.contains("aggregation") ) {
                // Try to get metadata from the catalog.
                //dgab = createBeansFromThreddsMetadata(ThreddsDataset, url);
                if ( dgab == null ) {
                    /* 
                     * Doing the initialization from the files is just too expensive.
                     * If we can't get it from the metadata, then forget it.
                     * 

                        // Else open the aggregation and get it from there
                        String dods = url.replaceAll("http", "dods");
                        NetcdfDataset ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(dods);
                        dgab = createBeansFromNetcdfDataset(ncds, url, esg, ThreddsDataset);
                        ncds.close();
                     *
                     * Log the data set and move on...
                     */
                    System.err.println("Not enough metadata to create LAS configuration for "+threddsDataset.getName()+".  Skipping...");
                }
            }
        } else {
            if ( uaf ) {
                dgab = createBeansFromNetcdfDataset(url, false, threddsDataset);
            } else {
                dgab = createBeansFromNetcdfDataset(url, false, null);
            }
        }
        return dgab;
    }
    private static DatasetsGridsAxesBean createBeansFromUAFThreddsMetadata(InvDataset threddsDataset) {
        
        DatasetsGridsAxesBean dgab = new DatasetsGridsAxesBean();
        
        String scan = threddsDataset.findProperty("LAS_scan");
        if ( scan != null && Boolean.valueOf(scan).booleanValue() ) {
            forceAxes.put("t", new Boolean(true));
            dgab = createBeansFromThreddsDataset(threddsDataset, threddsDataset.getAccess(ServiceType.OPENDAP));
            return dgab;
        }
        
        Vector DatasetBeans = new Vector();
        DatasetBean dataset = new DatasetBean();
        UniqueVector GridBeans = new UniqueVector();
        Set AllAxesBeans = new HashSet();

        if ( threddsDataset.getName().toLowerCase().contains("fmrc") || threddsDataset.getName().toLowerCase().contains("forecast model run collection") ) return dgab;
        if ( skip(threddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName() ) ) {
            System.out.println("Skipping "+threddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName()+" because it matches a regular expression in the skip.xml file.");
            return dgab;
        }
        
        if (verbose) {
            System.out.println("Processing UAF THREDDS dataset: " + threddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName()+" from "+threddsDataset.getParentCatalog().getUriString());
        }

        dataset.setName(threddsDataset.getFullName());
        String id = fixid(threddsDataset);
        
        dataset.setElement(id);
        dataset.setVersion(version_string);
        dataset.setCreator(ADDXMLProcessor.class.getName());
        dataset.setCreated((new DateTime()).toString());
        
            List<Variables> variables = threddsDataset.getVariables();
            if (variables.size() > 0 ) {
                
                for (Iterator varlistIt = variables.iterator(); varlistIt.hasNext();) {
                    
                    Variables vars_container = (Variables) varlistIt.next();
                    List<Variable> vars = vars_container.getVariableList();
                    if ( vars.size() > 0 && vars_container.getVocabulary().equals("netCDF_contents")) {
                        for (Iterator varIt = vars.iterator(); varIt.hasNext();) {
                            Variable variable = (Variable) varIt.next();
                            if ( !variable.getVocabularyName().equalsIgnoreCase("time") &&
                                    !variable.getVocabularyName().equalsIgnoreCase("latitude") && 
                                    !variable.getVocabularyName().equalsIgnoreCase("longitude") &&
                                    !variable.getVocabularyName().equalsIgnoreCase("longtitude") &&  // Handle PFEG misspelling...
                                    !variable.getVocabularyName().equalsIgnoreCase("altitude") &&
                                    !variable.getVocabularyName().equalsIgnoreCase("depth")) {
                                UniqueVector AxisBeans = new UniqueVector();
                                VariableBean las_var = new VariableBean();
                                las_var.setElement(id+"-"+variable.getName());
                                String vocab = variable.getVocabularyName();
                                if ( vocab != null ) {
                                    las_var.setName(vocab);
                                } else {
                                    las_var.setName(variable.getName());
                                }
                                if ( variable.getUnits() != null && !variable.getUnits().equals("") ) {
                                    las_var.setUnits(variable.getUnits());
                                } else {
                                    las_var.setUnits("no units");
                                }
                                las_var.setUrl(threddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName()+"#"+variable.getName());
                                System.out.println("Processing UAF THREDDS variable: " + variable.getName());

                                GeospatialCoverage coverage = threddsDataset.getGeospatialCoverage();
                                DateRange dateRange = threddsDataset.getTimeCoverage();

                                StringBuilder grid_name = new StringBuilder(variable.getName()+"-"+id+"-grid");
                                if (coverage != null ) {
                                    int xsizei;
                                    int ysizei;
                                    String eastwestNumberOfPoints = threddsDataset.findProperty("eastwestNumberOfPoints");
                                    String eastwestResolution = threddsDataset.findProperty("eastwestResolution");
                                    String northsouthNumberOfPoints = threddsDataset.findProperty("northsouthNumberOfPoints");
                                    String northsouthResolution = threddsDataset.findProperty("northsouthResolution");

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
                                        if ( eastwestNumberOfPoints != null ) {
                                                try {
                                                    xsizei = Integer.valueOf(eastwestNumberOfPoints);
                                                } catch ( Exception e ) {
                                                    xsizei = 50;
                                                }
                                        } else {
                                            xsizei = 50;
                                        }
                                        // Set the resolution so we get 50 grid cells in each direction.
                                        
                                        xresolution = xsize / (double) xsizei;
                                    } else {
                                        xsizei = (int)(xsize / xresolution);
                                        if ( xsizei == 0 ) xsizei = 1;
                                    }
                                    double ysize = coverage.getLatExtent();
                                    double yresolution = coverage.getLatResolution();
                                    double ystart = coverage.getLatStart();
                                    String yunits = coverage.getLatUnits();
                                    if ( Double.isNaN(ysize) || Double.isNaN(ystart) ) {
                                        readY = true;
                                    }
                                    if ( Double.isNaN(yresolution) ) {
                                        if ( northsouthNumberOfPoints != null ) {
                                            try {
                                                ysizei = Integer.valueOf(northsouthNumberOfPoints);
                                            } catch ( Exception e ) {
                                                ysizei = 50;
                                            }
                                        } else {
                                            ysizei = 50;
                                        }
                                        yresolution = ysize / (double) ysizei;
                                    } else {
                                        ysizei = (int)(ysize/yresolution);
                                        if ( ysizei == 0 ) ysizei = 1;
                                    }
                                    boolean hasZ = false;
                                    boolean readZ = false;
                                    String zvalues = null;
                                    String zvariables = null;
                                    Range z = coverage.getUpDownRange();
                                    if ( z != null ) {  
                                        double zsize = z.getSize();
                                        double zresolution = z.getResolution();
                                        double zstart = z.getStart();
                                        try {
                                            List<InvProperty> properites = threddsDataset.getProperties();
                                            for ( Iterator propIt = properites.iterator(); propIt.hasNext(); ) {
                                                InvProperty invProperty = (InvProperty) propIt.next();
                                                String name = invProperty.getName();
                                                String value = invProperty.getValue();
                                                if ( name.equals("hasZ") ) {
                                                    zvariables = threddsDataset.findProperty("hasZ");
                                                    String[] zvars = zvariables.split("\\s+");
                                                    List<String> hasZvars = new ArrayList(Arrays.asList(zvars));
                                                    if ( hasZvars.contains(variable.getName())) {
                                                        hasZ = true;
                                                    } else {
                                                        hasZ = false;
                                                    }
                                                    if ( hasZ ) {
                                                        try {
                                                            zvalues = threddsDataset.findProperty(Z_VALUES);
                                                        } catch (Exception e) {
                                                            try {
                                                                zvalues = threddsDataset.findProperty(ZVALUES);
                                                            } catch (Exception e1) {
                                                                zvalues = null;
                                                            }
                                                        }
                                                        if ( Double.compare(zsize, 0.0d) == 0.0 ) {
                                                            zvalues = String.valueOf(zstart);
                                                        }
                                                        if ( zvalues != null ) {
                                                            zvalues = zvalues.trim();
                                                        }
                                                        if ( ( Double.isNaN(zsize) || Double.isNaN(zresolution) || Double.isNaN(zstart) ) &&
                                                                zvalues == null ) {
                                                            readZ = true;
                                                        }
                                                        // Having found the z for this variable and read the values, break out of the property loop.
                                                        break;
                                                    }
                                                } else if ( name.contains("hasZ_") ) {
                                                    String zname = name.split("_")[1];
                                                    String[] zvars = value.split("\\s+");
                                                    List<String> hasZvars = new ArrayList(Arrays.asList(zvars));
                                                    if ( hasZvars.contains(variable.getName())) {
                                                        hasZ = true;
                                                    } else {
                                                        hasZ = false;
                                                    }
                                                    if ( hasZ ) {
                                                        zvalues = threddsDataset.findProperty("updownValues_"+zname);
                                                        if ( Double.compare(zsize, 0.0d) == 0.0 ) {
                                                            zvalues = String.valueOf(zstart);
                                                        }
                                                        if ( zvalues != null ) {
                                                            zvalues = zvalues.trim();
                                                        }
                                                        if ( ( Double.isNaN(zsize) || Double.isNaN(zresolution) || Double.isNaN(zstart) ) &&
                                                                zvalues == null ) {
                                                            readZ = true;
                                                        }
                                                        // Having found the z and read the values, break out of the property loop.
                                                        break;
                                                    }
                                                }
                                            }
                                            
                                        } catch (Exception e) {
                                            hasZ = false;
                                        }
                                        

                                        
                                    }

                                    // One of these axes is not sufficiently specified in the metadata so prepare read the data out of the aggregation.
                                    NetcdfDataset ncds = null;
                                    GridDataset gridsDs = null;
                                    GridCoordSys gcs = null;

                                    if ( readX || readY || readZ ) {
                                        System.out.println("Problem found in " + threddsDataset.getParentCatalog().getUriString());
                                        System.out.println("Unable to create LAS configuration for " + threddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName() + " Read X=" + readX + " Read Y=" + readY + " Read Z=" + readZ);
                                        return null;
                                    }

                                    // Grab the properties...
                                    String timeCoverageNumberOfPoints = threddsDataset.findProperty("timeCoverageNumberOfPoints");
                                    String timeUnits = threddsDataset.findProperty("timeAxisUnits");
                                    
                                    String elementName = variable.getName()+"-"+id+"-x-axis";
                                    AxisBean xAxis = new AxisBean();

                                    // Get the X Axis information...
                                    System.out.println("Loading X from metadata: "+elementName);
                                    xAxis.setElement(elementName);
                                    grid_name.append("-x-axis");
                                    xAxis.setType("x");
                                    xAxis.setUnits(xunits);
                                    
                                    ArangeBean xr = new ArangeBean();
                                    xr.setSize(String.valueOf(xsizei));
                                    xr.setStep(String.valueOf(decimalFormat.format(xresolution)));
                                    xr.setStart(String.valueOf(xstart));
                                    xAxis.setArange(xr);


                                    if ( !AxisBeans.contains(xAxis) ) {
                                        AxisBeans.add(xAxis);
                                    } else {
                                        xAxis.setElement(AxisBeans.getMatchingID(xAxis));
                                    }
                                    elementName = variable.getName()+"-"+id+"-y-axis";
                                    AxisBean yAxis = new AxisBean();

                                    System.out.println("Loading Y from metadata: "+elementName);
                                    // Get the Y Axis information...
                                    yAxis.setElement(elementName);
                                    grid_name.append("-y-axis");
                                    yAxis.setType("y");
                                    yAxis.setUnits(yunits);
                                    ArangeBean yr = new ArangeBean();
                                    yr.setSize(String.valueOf(ysizei));
                                    yr.setStep(String.valueOf(decimalFormat.format(yresolution)));
                                    yr.setStart(String.valueOf(ystart));
                                    yAxis.setArange(yr);

                                    if ( !AxisBeans.contains(yAxis) ) {
                                        AxisBeans.add(yAxis);
                                    } else {
                                        yAxis.setElement(AxisBeans.getMatchingID(yAxis));
                                    }
                                    elementName = variable.getName()+"-"+id+"-z-axis";
                                    AxisBean zAxis = new AxisBean();
                                    if ( hasZ ) {
                                        grid_name.append("-z-axis");
                                        if ( zvalues != null ) {
                                            
                                            System.out.println("Loading Z from property metadata: "+elementName);
                                            zAxis.setElement(elementName);
                                            String zunits = z.getUnits();
                                            zAxis.setType("z");
                                            zAxis.setUnits(zunits);
                                            String[] zvs = zvalues.split("\\s+");
                                            for (int zi = 0; zi < zvs.length; zi++ ) {
                                                double zd = Double.valueOf(zvs[zi]).doubleValue();
                                                zvs[zi] = decimalFormat.format(zd);
                                            }
                                            zAxis.setV(zvs);
                                        } else {
                                            System.out.println("Loading Z without property metadata: "+elementName);
                                            zAxis.setElement(elementName);
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

                                    String calendar = threddsDataset.findProperty("timeCoverageCalendar");
                                    // Use this chronology and the UTC Time Zone
                                    Chronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
                                    if ( calendar != null ) {

                                        // If calendar attribute is set, use appropriate Chronology.
                                        if (calendar.equals("proleptic_gregorian") ) {
                                            chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
                                        } else if (calendar.equals("noleap") || calendar.equals("365_day") ) {
                                            chrono = NoLeapChronology.getInstanceUTC();
                                        } else if (calendar.equals("julian") ) {
                                            chrono = JulianChronology.getInstanceUTC();
                                        } else if ( calendar.equals("all_leap") || calendar.equals("366_day") ) {
                                            chrono = AllLeapChronology.getInstanceUTC();
                                        } else if ( calendar.equals("360_day") ) {  /* aggiunto da lele */
                                            chrono = ThreeSixtyDayChronology.getInstanceUTC();
                                        }
                                    }
                                    // Get the date time from the metadata TimeCoverage if it exists...
                                    // Try the generic parser
                                    DateTimeFormatter f = ISODateTimeFormat.dateTimeParser().withChronology(chrono);
                                    // DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").withChronology(chrono);
                                    if ( dateRange != null ) {
                                        AxisBean tAxis = new AxisBean();
                                        DateType sd = dateRange.getStart();
                                        
                                           
                                        DateTime metadata_sd = null;
                                        if ( sd != null ) {
                                            String date = sd.toDateTimeString();
                                            if ( date.startsWith("0000") ) {
                                                date = date.replace("0000", "0001");
                                                tAxis.setModulo(true);
                                            }
                                            if ( date.startsWith("-0001") ) {
                                                date = date.replace("-0001", "0001");
                                            }
                                            try {
                                                metadata_sd = f.parseDateTime(date).withChronology(chrono);
                                            } catch (Exception e) {
                                                System.out.println("Problem found in "+threddsDataset.getParentCatalog().getUriString());
                                                System.out.println("Unable to create LAS configuration for "+threddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName()+" Trouble parsing time.");
                                                return null;
                                            }
                                        }
                                        DateType ed = dateRange.getEnd();
                                        DateTime metadata_ed = null;
                                        if ( ed != null ) {
                                            String date = ed.toDateTimeString();
                                            if ( date.startsWith("0000") ) {
                                                date = date.replace("0000","0001");
                                                tAxis.setModulo(true);
                                            }
                                            if ( date.startsWith("-0001") ) {
                                                date = date.replace("-0001", "0001");
                                            }
                                            if ( date.equalsIgnoreCase("present")) {
                                                metadata_ed = new DateTime().withChronology(chrono);
                                            } else {
                                                try {
                                                    metadata_ed = f.parseDateTime(date).withChronology(chrono);
                                                } catch (Exception e) {
                                                    System.out.println("Problem found in "+threddsDataset.getParentCatalog().getUriString());
                                                    System.out.println("Unable to create LAS configuration for "+threddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName()+" Trouble parsing time.");
                                                    return null;
                                                }
                                            }
                                        }


                                        
                                      
                                        if ( timeUnits == null ) {
                                            // Something went wrong, do a regular scan...
                                            forceAxes.put("t", new Boolean(true));
                                            dgab = createBeansFromThreddsDataset(threddsDataset, threddsDataset.getAccess(ServiceType.OPENDAP));
                                            return dgab;
                                        }
                                        if ( timeUnits.contains("0000-") ) {
                                            tAxis.setModulo(true);
                                        }
                                        if ( calendar != null ) {
                                            tAxis.setCalendar(calendar);
                                        }
                                        tAxis.setElement(variable.getName()+"-"+id+"-t-axis");
                                        System.out.println("Loading T from metadata: "+variable.getName()+"-"+id+"-t-axis");
                                        grid_name.append("-t-axis");
                                        tAxis.setType("t");

                                        ArangeBean tr = new ArangeBean();
                                        DateTimeFormatter hoursfmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                                        tr.setStart(hoursfmt.print(metadata_sd));
                                        double dm = (metadata_ed.getMillis() - metadata_sd.getMillis())/(Double.valueOf(timeCoverageNumberOfPoints) - 1.d);
                                        long delta_millis = (long) dm;
                                        Duration duration = new Duration(delta_millis);
                                        if ( metadata_sd.equals(metadata_ed) || Integer.valueOf(timeCoverageNumberOfPoints) == 1 ) {
                                            DateTimeFormatter fmt;
                                            fmt = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss");
                                            tAxis.setArange(null);
                                            tAxis.setUnits("time");
                                            String[] v = new String[1];
                                            v[0] = fmt.print(metadata_sd);
                                            tAxis.setV(v);
                                        } else if ( Integer.valueOf(timeCoverageNumberOfPoints) > 0 && Integer.valueOf(timeCoverageNumberOfPoints) <= 10 ) {
                                            tAxis.setArange(null);
                                            tAxis.setUnits("time");
                                            String[] v = new String[Integer.valueOf(timeCoverageNumberOfPoints)];
                                            DateTimeFormatter fmt;
                                            if ( timeUnits.contains("0000-") ) {
                                                fmt = DateTimeFormat.forPattern("dd-MMM");
                                                tAxis.setModulo(true);
                                            } else {
                                                if ( duration.getStandardHours() < 24 ) {
                                                    fmt = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss");
                                                } else {
                                                    fmt = DateTimeFormat.forPattern("dd-MMM-yyyy");
                                                }
                                            }
                                            for ( int i = 0; i < Integer.valueOf(timeCoverageNumberOfPoints); i++ ) {
                                                v[i] = fmt.print(metadata_sd);
                                                metadata_sd = metadata_sd.plus(duration);
                                            }
                                            tAxis.setV(v);
                                        } else {

                                            Period p = duration.toPeriod(chrono);
                                            int values[] = p.getValues();
                                            int numPeriods = 0;
                                            DurationFieldType types[] = p.getFieldTypes();
                                            String periods = "";
                                            // Finds spacing in terms of years, months, weeks, days, hours, minutes, seconds, millis
                                            // We will check years, months, weeks, days, hours
                                            int step = -1;
                                            String typeName = "";
                                            for (int i = 0; i < 6; i++) {
                                                if (values[i] > 0) {
                                                    numPeriods++;
                                                    String tn = types[i].getName();
                                                    // Get rid of the "s" in the plural form of the name.
                                                    tn = tn.substring(0, tn.length() - 1);
                                                    // LAS doesn't understand "week" so make it "day" and
                                                    // multiply the step by 7.
                                                    periods = periods + " " + tn;
                                                    if (tn.contains("week")) {
                                                        typeName = "day";
                                                        step = values[i] * 7;
                                                    }
                                                    // Keep only the largest whole period...
                                                    if ( typeName.equals("") ) {
                                                        typeName = tn;
                                                        step = values[i];
                                                    }
                                                }

                                            }

                                            if (numPeriods == 0 || numPeriods > 1) {

                                                // This is special code to deal with climatology files that define
                                                // the time axis in the "middle" of the month.  Since there is no
                                                // "middle" of months (but there is certainly always a
                                                // first of the month, geez-o) so we are left to figure this out by
                                                // looking at the period values and guessing that this really means
                                                // climo months.

                                                // Is the gap in years and months 0?
                                                // Are the values 4 weeks apart?
                                                if ( (values[0] == 0 && values[1] == 0) && (values[2] == 4)) {
                                                    // We're guessing these are months
                                                    typeName = "month";
                                                    step = 1;
                                                } else if ( values[1] > 0 ) {
                                                    // We're again guessing that the value is months (and everything else is in the noise)
                                                    typeName = "month";
                                                    step = values[1];
                                                } else if ( !periods.contains("year") && !periods.contains("month") && periods.contains("week") && periods.contains("day")  ) {
                                                    // We can convert this to days. :-)
                                                    typeName = "day";
                                                    step = 7*values[2] + values[3];

                                                }  else if ( !periods.contains("year") && !periods.contains("month") && !periods.contains("week") && !periods.contains("day") && periods.contains("hour") && periods.contains("minute") ) {
                                                    // Hours is the best we can do...
                                                    typeName = "hour";
                                                    step = values[4];

                                                } else {
                                                    // Guess based on the size of the period in well-known units.
                                                    long days = duration.getStandardDays();

                                                    if ( days > 359 ) {
                                                        typeName = "year";
                                                        step = 1;
                                                    } else if ( days > 26 ) {
                                                        typeName = "month";
                                                        step = 1;
                                                    }

                                                }
                                            }
                                            String sstep;
                                            if ( typeName.contains("minute") ) {
                                                double s = Double.valueOf(step)/60d;
                                                sstep = String.valueOf(s);
                                                typeName = "hours";
                                            } else {
                                                sstep = String.valueOf(step);
                                            }

                                            tr.setStep(sstep);
                                            tAxis.setUnits(typeName);

                                            try {
                                                // Do a sanity check.  If the delta is a regular interval, but the axis is irregular calculate how many steps does it take to cover the range at this delta.
                                                if ( typeName.contains("day") || typeName.contains("hour") ) {
                                                    Period delta;
                                                    if ( typeName.contains("day") ) {
                                                        delta = new Period(0, 0, 0, step, 0, 0, 0, 0);
                                                    } else {
                                                        delta = new Period(0, 0, 0, 0, step, 0, 0, 0);
                                                    }
                                                    int i = 0;
                                                    DateTime computed_ed = metadata_sd.plus(delta); 
                                                    while ( computed_ed.isBefore(metadata_ed) ) {
                                                        i++;
                                                        computed_ed = computed_ed.plus(delta);
                                                    }
                                                    i = i-1;
                                                    if ( Integer.valueOf(timeCoverageNumberOfPoints) < i ) {
                                                        System.out.println("Calculated number of points is: "+i+"  Metadata number of points is: "+timeCoverageNumberOfPoints);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                // Sanity check was insane, we'll have to live with that.
                                            }
                                            tr.setSize(timeCoverageNumberOfPoints);
                                            tAxis.setArange(tr);
                                        }
                                        if ( !AxisBeans.contains(tAxis) ) {
                                            AxisBeans.addUnique(tAxis);
                                        } else {
                                            tAxis.setElement(AxisBeans.getMatchingID(tAxis));
                                        }
                                    }
                                    GridBean grid = new GridBean();
                                    grid.setElement(grid_name.toString());
                                    grid.setAxes(AxisBeans);
                                    if ( !GridBeans.contains(grid) ) {
                                        GridBeans.add(grid);
                                        AllAxesBeans.addAll(AxisBeans);
                                    } else {
                                        grid.setElement(GridBeans.getMatchingID(grid));
                                    }

                                    las_var.setGrid(grid);
                                    boolean contains = false;
                                    for (Iterator invarsIt = dataset.getVariables().iterator(); invarsIt.hasNext();) {
										VariableBean vbean = (VariableBean) invarsIt.next();
										if ( vbean.getElement().equals(las_var.getElement())) contains = true;
									}
                                    if ( !contains ) {
                                    	dataset.addVariable(las_var);
                                    }


                                } // coverage != null
                            }
                        } // for variables;

                        
                    } else if ( vars.size() == 0 && vars_container.getVocabulary().equals("netCDF_contents") ) {
                        System.out.println("Problem found in "+threddsDataset.getParentCatalog().getUriString());
                        System.out.println("Unable to create LAS configuration for "+threddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName()+" No variables.");
                        return null;
                    }

                }// for outer variables container iterator
            } else { // outer variables list
                System.out.println("Problem found in "+threddsDataset.getParentCatalog().getUriString());
                System.out.println("Unable to create LAS configuration for "+threddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName()+" No variables.");
                return null;
            }
        
        DatasetBeans.add(dataset);
        dgab.setGrids(GridBeans);
        Vector aa = new Vector();
        for (Iterator axIt = AllAxesBeans.iterator(); axIt.hasNext();) {
            AxisBean ab = (AxisBean) axIt.next();
            aa.add(ab);
        }
        dgab.setAxes(aa);
        dgab.setDatasets(DatasetBeans);
        return dgab;
    }
    private static String fixid(InvDataset t) {
        String tid = t.getID();
        return fixid(tid, t.getFullName());
    }
    public static String fixid(String tid, String name) {
        String id;
        if ( tid != null ) {
            id = tid;
            id = id.replace("/", ".");
            id = id.replace(":", ".");
            id = id.replace(",","");
            if ( Pattern.matches("^[0-9].*", id) ) id = id + "dataset-";
            id = id.replaceAll(" ", "-"); 
        } else {
            // This should never happen in the UAF case since the cleaner should have assigned the ID already.
            try {
                id = "data-"+JDOMUtils.MD5Encode(name)+String.valueOf(Math.random());
            } catch (UnsupportedEncodingException e) {
                id = "data-"+String.valueOf(Math.random());
            }
        }
        return id;
    }
    private static DatasetsGridsAxesBean createBeansFromThreddsMetadata(Set<String> time_freqs, InvDataset threddsDataset, String url, Vector allGrids, Vector allAxes) {
        DatasetsGridsAxesBean dgab = new DatasetsGridsAxesBean();
        Vector DatasetBeans = new Vector();
        DatasetBean dataset = new DatasetBean();
        
        
        if (verbose) {
            System.out.println("Processing ESG THREDDS dataset: " + threddsDataset.getFullName() + "with id: "+threddsDataset.getID());
        }
        System.out.println("working with: "+threddsDataset.getID());
        dataset.setName(threddsDataset.getFullName());
        dataset.setElement(threddsDataset.getID());
        dataset.setVersion(version_string);
        dataset.setCreator(addXML.class.getName());
        dataset.setCreated((new DateTime()).toString());
        dataset.setUrl(url);
        List<Variables> variables = threddsDataset.getVariables();

        if ( variables.size() == 0 ) {
            // See if the variables list is in the parent data set.
            InvDataset parent = threddsDataset.getParent();
            variables = parent.getVariables();
        }
        if (variables.size() > 0 ) {
            for (Iterator varlistIt = variables.iterator(); varlistIt.hasNext();) {
                Variables vars_container = (Variables) varlistIt.next();
                List<Variable> vars = vars_container.getVariableList();
                if ( vars.size() > 0 ) {
                    for (Iterator varIt = vars.iterator(); varIt.hasNext();) {
                        UniqueVector axisBeans = new UniqueVector();

                        Variable variable = (Variable) varIt.next();

                        VariableBean las_var = new VariableBean();
                        las_var.setElement(threddsDataset.getID()+"-"+variable.getName());
                        String vname = variable.getDescription();
                        if ( vname != null && vname.length() > 0 ) {
                            las_var.setName(vname);
                        } else {
                            las_var.setName(variable.getName());
                        }
                        if ( variable.getUnits() != null && !variable.getUnits().equals("") ) {
                            las_var.setUnits(variable.getUnits());
                        } else {
                            las_var.setUnits("no units");
                        }
                        las_var.setUrl(url+"#"+variable.getName());
                        System.out.println("Processing ESG THREDDS variable: " + variable.getName());

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
                                try {
                                    zvalues = threddsDataset.findProperty(ESGF_Z_VALUES);
                                } catch (Exception e) {
                                    try {
                                        zvalues = threddsDataset.findProperty(ZVALUES);
                                    } catch (Exception e1) {
                                        zvalues = null;
                                    }
                                }
                                if ( zvalues != null ) {
                                    zvalues = zvalues.trim();
                                }
                                if ( ( Double.isNaN(zsize) || Double.isNaN(zresolution) || Double.isNaN(zstart) ) &&
                                        zvalues == null ) {
                                    readZ = true;
                                }
                            }

                            // If we can't get it from the metadata, we'll just give up.

                            if ( readX || readY || readZ ) {
                                return null;
                            }
                            String elementName = threddsDataset.getID()+"-x-axis";
                            AxisBean xAxis = new AxisBean();

                            // Get the X Axis information...
                            System.out.println("Loading X from metadata: "+elementName);
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

                            axisBeans.add(xAxis);
                            
                            elementName = threddsDataset.getID()+"-y-axis";
                            AxisBean yAxis = new AxisBean();

                            System.out.println("Loading Y from metadata: "+elementName);
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

                            axisBeans.add(yAxis);
                        
                            elementName = threddsDataset.getID()+"-z-axis";
                            AxisBean zAxis = new AxisBean();
                            if ( hasZ ) {
                                if ( zvalues != null ) {
                                    System.out.println("Loading Z from property metadata: "+elementName);
                                    zAxis.setElement(elementName);
                                    String zunits = z.getUnits();
                                    zAxis.setType("z");
                                    zAxis.setUnits(zunits);
                                    String[] zvs = zvalues.split("\\s+");
                                    DecimalFormat format = new DecimalFormat("###############.###############");
                                    for (int zi = 0; zi < zvs.length; zi++ ) {
                                        double zd = Double.valueOf(zvs[zi]).doubleValue();
                                        zvs[zi] = format.format(zd);
                                    }
                                    zAxis.setV(zvs);
                                } else {
                                    System.out.println("Loading Z without property metadata: "+elementName);
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
                                grid_name.append("-z-axis");
                                axisBeans.add(zAxis);
                            }
                            String start_time = threddsDataset.findProperty("start");
                            String time_length = threddsDataset.findProperty("time_length");
                            String time_delta = threddsDataset.findProperty("time_delta");
                            String calendar = threddsDataset.findProperty("calendar");
                            String tdelta = "1";
                            String tunits = "month";
                            String tclimo = "false";
                            if ( time_freqs.size() == 1 ) {
                                String units = time_freqs.iterator().next();
                                if ( units.equalsIgnoreCase("3hr") ) {
                                    tdelta = "3";
                                    tunits = "hour";
                                } else if ( units.equalsIgnoreCase("6hr") ) {
                                    tdelta = "6";
                                    tunits = "hour";
                                } else if ( units.equalsIgnoreCase("day") ) {
                                    tdelta = "1";
                                    tunits = "day";
                                } else if ( units.equalsIgnoreCase("mon") ) {
                                    tdelta = "1";
                                    tunits = "month";
                                } else if ( units.equalsIgnoreCase("monclim") ) {
                                    tdelta = "1";
                                    tunits = "month";
                                    tclimo = "true";
                                } else if ( units.equalsIgnoreCase("monthly") ) {
                                    tdelta = "1";
                                    tunits = "month";
                                } else if ( units.equalsIgnoreCase("monthly_mean") ) {
                                    tdelta = "1";
                                    tunits = "month";
                                } else if ( units.equalsIgnoreCase("yr") ) {
                                    tdelta = "1";
                                    tunits = "year";
                                }
                            } else {

                                if ( time_delta != null && time_delta.contains(" ") ) {
                                    String[] time_parts = time_delta.split("\\s+");
                                    tdelta = time_parts[0];
                                    tunits = time_parts[1];
                                }

                            }
                            String calendar_name = null;
                            // Use this chronology and the UTC Time Zone
                            Chronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
                            if ( calendar != null ) {

                                // If calendar attribute is set, use appropriate Chronology.
                                if (calendar.equals("proleptic_gregorian") ) {
                                    calendar_name = "proleptic_gregorian";
                                    chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
                                } else if (calendar.equals("noleap") || calendar.equals("365_day") ) {
                                    chrono = NoLeapChronology.getInstanceUTC();
                                    calendar_name = "noleap";
                                } else if (calendar.equals("julian") ) {
                                    calendar_name = "julian";
                                    chrono = JulianChronology.getInstanceUTC();
                                } else if ( calendar.equals("all_leap") || calendar.equals("366_day") ) {
                                    chrono = AllLeapChronology.getInstanceUTC();
                                    calendar_name = "all_leap";
                                } else if ( calendar.equals("360_day") ) {  /* aggiunto da lele */
                                    chrono = ThreeSixtyDayChronology.getInstanceUTC();
                                    calendar_name = "360_day";
                                }
                            }
                            
                            String modulo = null;

                            // Get the date time from the metadata TimeCoverage if it exists...
                            DateType sd = dateRange.getStart();
                            DateTime metadata_sd = null;
                            if ( sd != null ) {
                                String date = sd.getText();
                                if ( date.startsWith("0000") ) {
                                    date = date.replace("0000", "0001");
                                    modulo = "true";
                                }
                                DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").withChronology(chrono);
                                metadata_sd = f.parseDateTime(date).withChronology(chrono);
                            }

                            String esg_formats[] = {"yyyy-MM-dd HH:mm:ss.s",
                                    "yyyy-MM-dd HH:mm:s.s",
                                    "yyyy-MM-dd HH:m:ss.s",
                                    "yyyy-MM-dd HH:m:s.s",


                                    "yyyy-MM-dd HH:mm:ss.s",
                                    "yyyy-MM-dd HH:mm:s.s",
                                    "yyyy-MM-dd HH:m:ss.s",
                                    "yyyy-MM-dd HH:m:s.s",
                                    "yyyy-MM-dd H:mm:ss.s",
                                    "yyyy-MM-dd H:mm:s.s",
                                    "yyyy-MM-dd H:m:ss.s",
                                    "yyyy-MM-dd H:m:s.s",

                                    "yyyy-MM-dd HH:mm:ss.s",
                                    "yyyy-MM-dd HH:mm:s.s",
                                    "yyyy-MM-dd HH:m:ss.s",
                                    "yyyy-MM-dd HH:m:s.s",
                                    "yyyy-MM-dd H:mm:ss.s",
                                    "yyyy-MM-dd H:mm:s.s",
                                    "yyyy-MM-dd H:m:ss.s",
                                    "yyyy-MM-dd H:m:s.s",

                                    "yyyy-MM-d HH:mm:ss.s",
                                    "yyyy-MM-d HH:mm:s.s",
                                    "yyyy-MM-d HH:m:ss.s",
                                    "yyyy-MM-d HH:m:s.s",
                                    "yyyy-MM-d H:mm:ss.s",
                                    "yyyy-MM-d H:mm:s.s",
                                    "yyyy-MM-d H:m:ss.s",
                                    "yyyy-MM-d H:m:s.s",                

                                    "yyyy-MM-dd HH:mm:ss.s",
                                    "yyyy-MM-dd HH:mm:s.s",
                                    "yyyy-MM-dd HH:m:ss.s",
                                    "yyyy-MM-dd HH:m:s.s",
                                    "yyyy-MM-dd H:mm:ss.s",
                                    "yyyy-MM-dd H:mm:s.s",
                                    "yyyy-MM-dd H:m:ss.s",
                                    "yyyy-MM-dd H:m:s.s",

                                    "yyyy-MM-d HH:mm:ss.s",
                                    "yyyy-MM-d HH:mm:s.s",
                                    "yyyy-MM-d HH:m:ss.s",
                                    "yyyy-MM-d HH:m:s.s",
                                    "yyyy-MM-d H:mm:ss.s",
                                    "yyyy-MM-d H:mm:s.s",
                                    "yyyy-MM-d H:m:ss.s",
                                    "yyyy-MM-d H:m:s.s", 

                                    "yyyy-M-dd HH:mm:ss.s",
                                    "yyyy-M-dd HH:mm:s.s",
                                    "yyyy-M-dd HH:m:ss.s",
                                    "yyyy-M-dd HH:m:s.s",
                                    "yyyy-M-dd H:mm:ss.s",
                                    "yyyy-M-dd H:mm:s.s",
                                    "yyyy-M-dd H:m:ss.s",
                                    "yyyy-M-dd H:m:s.s",

                                    "yyyy-M-d HH:mm:ss.s",
                                    "yyyy-M-d HH:mm:s.s",
                                    "yyyy-M-d HH:m:ss.s",
                                    "yyyy-M-d HH:m:s.s",
                                    "yyyy-M-d H:mm:ss.s",
                                    "yyyy-M-d H:mm:s.s",
                                    "yyyy-M-d H:m:ss.s",
                                    "yyyy-M-d H:m:s.s"              

                            };
                            DateTime s = null;
                            for ( int i = 0; i < esg_formats.length; i++) {
                                try {
                                    DateTimeFormatter f = DateTimeFormat.forPattern(esg_formats[i]).withChronology(chrono);
                                    s = f.parseDateTime(start_time).withChronology(chrono);
                                    break;
                                } catch (Exception e) {
                                    // Try again...
                                }
                            }                           

                            if ( metadata_sd != null ) {
                                s = metadata_sd;
                            }

                            System.out.println("Loading T from metadata: "+elementName);
                            AxisBean tAxis = new AxisBean();
                            if ( calendar_name != null ) {
                                tAxis.setCalendar(calendar_name);
                            }
                            tAxis.setElement(threddsDataset.getID()+"-t-axis");
                            grid_name.append("-t-axis");
                            if ( modulo != null && modulo.equals("true") ) {
                                tAxis.setModulo(true);
                            }
                            tAxis.setType("t");
                            tAxis.setUnits(tunits);
                            ArangeBean tr = new ArangeBean();
                            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                            System.out.println("Using start time: "+fmt.print(s)+ " from input "+start_time);
                            tr.setStart(fmt.print(s));
                            tr.setStep(tdelta);
                            tr.setSize(time_length);
                            if ( tclimo.equals("true") ) {
                                tAxis.setModulo(true);
                            }
                            tAxis.setArange(tr);
                            axisBeans.add(tAxis);

                            // We have the axes for this variable.  See if we can find it in the list of existing axes.
                            
                            
                            
                            GridBean grid = getMatchingGrid(axisBeans, allGrids);
                            
                            if ( grid == null ) {

                                grid = new GridBean();
                                grid.setElement(grid_name.toString());
                                grid.setAxes(axisBeans);
                                allAxes.addAll(axisBeans);
                                allGrids.add(grid);
                                
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
        dgab.setDatasets(DatasetBeans);
        return dgab;
    }
    private static GridBean getMatchingGrid(UniqueVector ab, Vector g) {
        AxisBean x = null;
        AxisBean y = null;
        AxisBean z = null;
        AxisBean t = null;
        for (Iterator abIt = ab.iterator(); abIt.hasNext();) {
            AxisBean a = (AxisBean) abIt.next();
            if ( a.getType().equals("x") ) {
                x = a;
            } else if ( a.getType().equals("y") ) {
                y = a;
            } else if ( a.getType().equals("z") ) {
                z = a;
            } else if ( a.getType().equals("t") ) {
                t = a;
            }
        }
       
        for (Iterator gIt = g.iterator(); gIt.hasNext();) {
            GridBean gb = (GridBean) gIt.next();
            Vector axs = gb.getAxes();
            if ( axs.size() == ab.size() ) {
                AxisBean gx = null;
                AxisBean gy = null;
                AxisBean gz = null;
                AxisBean gt = null;
                for (Iterator axsIt = axs.iterator(); axsIt.hasNext();) {
                    AxisBean a = (AxisBean) axsIt.next();
                    if ( a.getType().equals("x") ) {
                        gx = a;
                    } else if ( a.getType().equals("y") ) {
                        gy = a;
                    } else if ( a.getType().equals("z") ) {
                        gz = a;
                    } else if ( a.getType().equals("t") ) {
                        gt = a;
                    }
                }
                boolean match = true;
                
                if ( x != null && !x.equals(gx) ) {
                    match = false; 
                }
                if ( y != null && !y.equals(gy) ) {
                    match = false;
                }
                if ( z != null && !z.equals(gz) ) {
                    match = false;
                }
                if ( t != null && !t.equals(gt) ) {
                    match = false;
                }
                if ( match ) {
                    return gb;
                }
            }
        }
        return null;
    }
    public static String trimUnidataDateTimeString(DateType d) {
        String dt = d.toDateTimeString();
        if ( dt.endsWith("Z") ) return dt.substring(0, dt.length() - 1);
        return dt;
    }
    private static Vector getContributors(InvDataset ThreddsDataset) {
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
        return contribs;
    }
    /**
     * processESGCategories
     * 
     * @param ThreddsDataset InvDataset
     * @return CategoryBean
     */
    public static CategoryBean processESGCategories(InvCatalog subCatalog) {
        // This is the top level...
        Vector topCats = new Vector();
        CategoryBean topCB = new CategoryBean();
        for (Iterator topLevelIt = subCatalog.getDatasets().iterator(); topLevelIt.hasNext(); ) {
            InvDataset topDS = (InvDataset) topLevelIt.next();
            
            topCB.setContributors(getContributors(topDS));
            topCB.setName(topDS.getName());
            String id = topDS.getID();
            topCB.setID(id);
            
            
            for (Iterator subDatasetsIt = topDS.getDatasets().iterator(); subDatasetsIt.hasNext(); ) {
                InvDataset subDataset = (InvDataset) subDatasetsIt.next();
                if ( !subDataset.hasNestedDatasets() && subDataset.hasAccess() && subDataset.getName().contains("aggregation") ) {
                    String url = null;
                    InvAccess access = null;
                    for (Iterator ait = subDataset.getAccess().iterator(); ait.hasNext(); ) {
                        access = (InvAccess) ait.next();
                        if (access.getService().getServiceType() == ServiceType.DODS ||
                                access.getService().getServiceType() == ServiceType.OPENDAP ||
                                access.getService().getServiceType() == ServiceType.NETCDF) {
                            url = access.getStandardUrlName();
                        }
                    }
                    if ( url != null && url.contains("aggregation") ){
                        FilterBean filter = new FilterBean();
                        filter.setAction("apply-variable");
                        String tag = subDataset.getID();
                        filter.setContainstag(tag);
                        topCB.addFilter(filter);
                        topCB.addCatID(subDataset.getID());
                    }
                } else {
                    

                    // These will be the catalog containers that will contain the aggregations...
                    for (Iterator grandChildrenIt = subDataset.getDatasets().iterator(); grandChildrenIt.hasNext(); ) {
                        InvDataset grandChild = (InvDataset) grandChildrenIt.next();
                        if ( grandChild.hasAccess() && grandChild.getName().contains("aggregation")) {
                            topCB.addCatID(subDataset.getID());
                            // We are done.
                            String url = null;
                            InvAccess access = null;
                            for (Iterator ait = grandChild.getAccess().iterator(); ait.hasNext(); ) {
                                access = (InvAccess) ait.next();
                                if (access.getService().getServiceType() == ServiceType.DODS ||
                                        access.getService().getServiceType() == ServiceType.OPENDAP ||
                                        access.getService().getServiceType() == ServiceType.NETCDF) {
                                    url = access.getStandardUrlName();
                                }
                            }
                            if ( url != null && url.contains("aggregation") ){
                                FilterBean filter = new FilterBean();
                                filter.setAction("apply-variable");
                                String tag = grandChild.getID();
                                filter.setContainstag(tag);
                                topCB.addFilter(filter);
                                topCB.addCatID(grandChild.getID());
                            }
                        } 
                    }
                }
            }
            if ( topCB.getFilters().size() > 0 ) {
                topCats.add(topCB);
            }
        }
        return topCB;
    }
    public static CategoryBean processUAFCategories(InvDataset ThreddsDataset) {
        String tid = ThreddsDataset.getID();
        String id = fixid(ThreddsDataset);  
        //String name = ThreddsDataset.getFullName();
        String name  = ThreddsDataset.getName();
        CategoryBean category = makeParent(ThreddsDataset);
        category.setName(name);
        category.setID(id);


        if ( ThreddsDataset.hasAccess() && ThreddsDataset.getAccess(ServiceType.OPENDAP) != null ) {
            String url = ThreddsDataset.getAccess(ServiceType.OPENDAP).getStandardUrlName();
            String curl = DODSNetcdfFile.canonicalURL(url);
            if ( !skip(curl) ) {
                FilterBean filter = new FilterBean();
                filter.setAction("apply-dataset");
                String tag = fixid(ThreddsDataset);
                filter.setEqualstag(tag);
                category.addFilter(filter);
            }
        } else {
            Vector subCats = new Vector();
            for (Iterator subDatasetsIt = ThreddsDataset.getDatasets().iterator(); subDatasetsIt.hasNext(); ) {
                InvDataset subDataset = (InvDataset) subDatasetsIt.next();
                // Process the sub-categories
                if (!subDataset.getName().contains("automated cleaning process") ) {
                    CategoryBean subCat = processUAFCategories(subDataset);
                    if ( !subCat.equals(category) && (subCat.getCategories().size() > 0 || subCat.getFilters().size() > 0 ) ) {
                        subCats.add(subCat);
                    }
                }
            }
            category.setCategories(subCats);
        }

        return category;
    }
    private static CategoryBean makeParent(InvDataset t) {
        String id = t.getID();  
        CategoryBean parent = new CategoryBean();
        // Make any THREDDS documentation links into LAS contributor links.
        parent.setContributors(getContributors(t));

        String name = t.getName();
        if (name != null) {
            parent.setName(t.getName());    
        }
        else {
            parent.setName("THREDDS Dataset");
        }
        
        if ( id != null && !id.equals("") ) {
            id = id.replace("/", ".");
            parent.setID(id);
        }
        return parent;
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
        cb.setContributors(getContributors(ThreddsDataset));

        String name = ThreddsDataset.getName();
        if (name != null) {
            cb.setName(ThreddsDataset.getName());   
        }
        else {
            cb.setName("THREDDS Dataset");
        }
        String id = fixid(ThreddsDataset);
        cb.setID(id);
        
        if (ThreddsDataset.hasAccess()) {
            String url = "empty";
            InvAccess access = null;
            for (Iterator ait = ThreddsDataset.getAccess().iterator(); ait.hasNext(); ) {
                access = (InvAccess) ait.next();
                if (access.getService().getServiceType() == ServiceType.DODS ||
                        access.getService().getServiceType() == ServiceType.OPENDAP ||
                        access.getService().getServiceType() == ServiceType.NETCDF) {
                    url = access.getStandardUrlName();
                    String curl = DODSNetcdfFile.canonicalURL(url);
                    // Replace the name with a bunch of stuff from the file if possible...
                    if ( generate_names ) {
                        StringBuilder dataset_name = new StringBuilder();
                        try {
                            Formatter error = new Formatter();
                            GridDataset gds = (GridDataset) FeatureDatasetFactoryManager.open(FeatureType.GRID, curl, null, error);   
                            if ( gds == null ) {
                                System.err.println("Unable to read dataset at "+url+" "+error.toString());
                                return null;
                            }
                            List<GridDatatype> grids = gds.getGrids();
                            if ( grids != null && grids.size() > 0 ) {
                                GridDatatype grid = (GridDatatype) grids.get(0);
                                dataset_name.append(grid.getDescription() + " (" + grid.getUnitsString() + ")");
                                if ( grids.size() > 1 ) {
                                    dataset_name.append(" more...");
                                }
                            }
                        } catch (IOException e) {
                            // Oh well, couldn't enhance the data set name...
                        }
                        cb.setName(dataset_name.toString());
                    }
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
                    if ( id != null ) {
                        tag = id;
                    } else {
                        tag = encodeID(url);
                    }
                }

                filter.setContainstag(tag);
                cb.addFilter(filter);
            }
        }

        Vector subCats = new Vector();
        for (Iterator subDatasetsIt = ThreddsDataset.getDatasets().iterator(); subDatasetsIt.hasNext(); ) {
            InvDataset subDataset = (InvDataset) subDatasetsIt.next();
            // Process the sub-categories
            CategoryBean subCat = processCategories(subDataset);
            if ( subCat.getCategories().size() > 0 || subCat.getFilters().size() > 0 ) {
                subCats.add(subCat);
            }
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
        dataset.setCreator(ADDXMLProcessor.class.getName());
        String curl = DODSNetcdfFile.canonicalURL(url);
        if (verbose) {
            System.out.println("Processing netCDF dataset: " + url);
        }
        Formatter error = new Formatter();

        GridDataset gridDs = null;
        TrajectoryObsDataset pointData = null;

        if (skip(curl)) {
            System.out.println("Skipping "+curl);
            return dagb;
        } 
        try {
            //             
            // Wondering where a particular class is coming from because there are muliple dependencies in multiple jars?
            // Use the class loader to find out...
            /*
                         ClassLoader loader = ADDXMLProcessor.class.getClassLoader();
                         System.out.println(loader.getResource("org/slf4j/spi/LocationAwareLogger.class"));
                         System.out.println(loader.getResource("org/apache/commons/logging/impl/SLF4JLocationAwareLog.class"));
                         System.out.println(loader.getResource("opendap/dap/BaseType.class"));
                         */
            //
            //             
            //             
            gridDs = (GridDataset) FeatureDatasetFactoryManager.open(FeatureType.GRID, curl, null, error);
            if ( gridDs == null ) {
                gridDs = (GridDataset) FeatureDatasetFactoryManager.open(FeatureType.GRID, curl, null, error);
            }

            if ( gridDs == null ) {
                pointData = (TrajectoryObsDataset) FeatureDatasetFactoryManager.open(FeatureType.TRAJECTORY, curl, null, error);
            }


        } catch (IOException e) {
            System.err.println("Unable to open dataset at "+DODSNetcdfFile.canonicalURL(url));
            return null;
        }

        if ( gridDs == null && pointData == null ) {
            System.err.println("Unable to read dataset at "+url+" "+error.toString());
            return null;
        }
        if ( gridDs != null ) {
            String name = null;
            if ( esg ) {
                name = threddsDataset.getFullName();
            } else {
                Attribute nameAttribute = null;
                if (global_title_attribute == null) {
                    nameAttribute = gridDs.findGlobalAttributeIgnoreCase("long_name");
                    if (nameAttribute == null) {
                        nameAttribute = gridDs.findGlobalAttributeIgnoreCase("title");
                    }
                }
                else {
                    nameAttribute = gridDs.findGlobalAttributeIgnoreCase(global_title_attribute);
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

            //new GridDataset(ncds);
            if (name == null) {
                name = url;
            }
            String elementName;
            if ( esg ) {
                elementName = threddsDataset.getID();
            } else {
                if ( threddsDataset != null && threddsDataset.getID() != null && !threddsDataset.getID().equals("") ) {
                    elementName = threddsDataset.getID();
                    elementName = elementName.replace("/", ".");
                    if ( Pattern.matches("^[0-9].*", elementName) ) elementName = "dataset-" + elementName;
                    elementName = elementName.replaceAll(" ", "-");
                } else {
                    elementName = encodeID(url);
                }
            }
            if ( uaf ) {
                elementName = fixid(threddsDataset);
            }
            dataset.setName(name);
            dataset.setElement(elementName);
            dataset.setUrl(url);

            List<GridDatatype> grids = new ArrayList();
            if ( gridDs != null ) {
                grids = gridDs.getGrids();
            }

            if (grids.size() == 0) {
                dataset.setComment(
                        "This data source has no lat/lon grids that follow a known convention.");
                System.err.println("File parsed.  No Lat/Lon grids found.");
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

                GridDatatype geogrid = grids.get(i);

                VariableBean variable = new VariableBean();
                variable.setUrl("#" + geogrid.getName());
                if (!geogrid.getDescription().equals(geogrid.getName())) {
                    variable.setName(geogrid.getDescription());
                }
                else {
                    variable.setName(geogrid.getName());
                }
                String gn = geogrid.getName();

                if ( Pattern.matches("^[0-9].*", gn) ) gn = "variable" + gn;
                gn = gn.replaceAll(" ", "-");
                variable.setElement(gn + "-" + elementName);
                if ( geogrid.getUnitsString() != null && !geogrid.getUnitsString().equals("") ) {
                    variable.setUnits(geogrid.getUnitsString());
                } else {
                    variable.setUnits("no units");
                }

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
                    System.out.println("\t Variable: " + geogrid.getName());
                    System.out.println("\t\t Longitude axis: ");
                }
                GridAxisBeans.addUnique(xaxis);
                if (verbose) {
                    System.out.println(xaxis.toString());
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
                    System.out.println("\t\t Latitude axis: ");
                }

                GridAxisBeans.addUnique(yaxis);
                if (verbose) {
                    System.out.println(yaxis.toString());
                }
                if (gcs.hasVerticalAxis()) {
                    CoordinateAxis1D zAxis = gcs.getVerticalAxis();
                    grid_name = grid_name + "-" + zAxis.getShortName();
                    if (verbose) {
                        System.out.println("\t\t Vertical axis: ");
                    }
                    AxisBean zaxis = makeGeoAxis(zAxis, "z", elementName);
                    GridAxisBeans.addUnique(zaxis);
                    if (verbose) {
                        System.out.println(zaxis.toString());
                    }

                }
                else {
                    if (verbose) {
                        System.out.println("\t\t No vertical axis");
                    }
                }

                CoordinateAxis1DTime tAxis = gcs.getTimeAxis1D();

                if (tAxis != null) {
                    grid_name = grid_name + "-" + tAxis.getShortName();
                    if (verbose) {
                        System.out.println("\t\t Time axis: ");
                    }

                    AxisBean taxis = null;
                    try {
                        taxis = makeTimeAxis(tAxis, elementName);
                    } catch (Exception e) {
                        return dagb;
                    }
                    if ( taxis != null ) {
                        GridAxisBeans.addUnique(taxis);
                        if (verbose) {
                            System.out.println(taxis.toString());
                        }
                    }
                }
                else {
                    if (verbose) {
                       System.out.println("\t\t No time axis");
                    }
                }
                
                CoordinateAxis1D eAxis = gcs.getEnsembleAxis();
                AxisBean eaxis = null;
                if ( eAxis != null ) {
                    eaxis = new AxisBean();
                    grid_name = grid_name + "-" + eAxis.getShortName();
                    if ( verbose ) {
                        System.out.println("\t\t Ensemble Axis: ");
                    }
                    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
                    DecimalFormat fmt = (DecimalFormat) nf;
                    fmt.applyPattern("####.####");
                    eaxis.setType("e");
                    eaxis.setElement(eAxis.getShortName() + "-" + "e" + "-" + elementName);
                    eaxis.setArange(null);

                    // We want these axis values as strings...
                    if ( eAxis.isNumeric() ) {
                        double[] v = eAxis.getCoordValues();

                        /** @todo we want to use 4-log[base10](max-min). */
                        String[] vs = new String[v.length];
                        for (int vvv = 0; vvv < v.length; vvv++) {
                            vs[vvv] = fmt.format(v[vvv]);
                        }
                        eaxis.setV(vs);
                    }
                    if ( eaxis != null ) {
                        GridAxisBeans.addUnique(eaxis);
                        if (verbose) {
                            System.out.println(eaxis.toString());
                        }
                    }
                    // Search for a String variable that contains the labels for the ensemble axis.
                    // It will have the same dimension as this axis in the first dimension and the size of the string in the second.
                    List<Dimension> dims = eAxis.getDimensions();
                    if ( dims.size() == 1 ) {
                        String dimname = dims.get(0).getName();
                        NetcdfFile nc = gridDs.getNetcdfFile();
                        List<ucar.nc2.Variable> allvars = nc.getVariables();
                        for (Iterator varIt = allvars.iterator(); varIt.hasNext();) {
                                ucar.nc2.Variable v = (ucar.nc2.Variable) varIt.next();
                                if ( v.getDataType() == DataType.STRING && v.getDimensions().size() == 1 ) {
                                    List<String> labels = new ArrayList<String>();
                                    if ( v.getDimensions().get(0).getName().equals(dimname) ) {
                                        Attribute vatn = v.findAttributeIgnoreCase("long_name");
                                        String van = v.getShortName();
                                        if (vatn != null ) {
                                            van = vatn.getStringValue();
                                        }
                                        if ( van != null ) {
                                            eaxis.setLabel(van);
                                        }
                                        try {
                                            Array vvalues = v.read();
                                            for (IndexIterator vvIt = vvalues.getIndexIterator(); vvIt.hasNext(); ) {
                                                String label = (String) vvIt.next();
                                                labels.add(label);
                                            }
                                        } catch (IOException e) {
                                            System.err.println("Warning: unable to get list of ensemble labels.");
                                        }
                                        if ( labels.size() > 0 ) {
                                            eaxis.setLabels(labels);
                                        }
                                    }
                                }
                            }
                        } else {
                        System.err.println("Warning: Ensemble axis has issue with size of dimensions list.");
                    }
                }

                grid.setElement(grid_name + "-" + elementName);
                grid.setAxes(GridAxisBeans);
                variable.setGrid(grid);
                dataset.addVariable(variable);
                ProjectionImpl proj = geogrid.getProjection();

                if (verbose) {
                    if (proj instanceof LatLonProjection) {
                        System.out.println("\t\t Grid has LatLonProjection.");
                    }
                    else if (proj instanceof LambertConformal) {
                        System.out.println("\t\t Grid has Lambert Conformal projection...");
                    }
                    else {
                        System.out.println("\t\t Grid has unknown projection...");
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
            try {
                gridDs.close();
            } catch (IOException e) {
                System.err.println("Unable to close "+url);
            }
        } else if ( pointData != null ) {
            
            System.out.println(pointData.getDescription());

            List<VariableSimpleIF> vars =  pointData.getDataVariables();
            
            
                            for (Iterator iterator = vars.iterator(); iterator.hasNext();) {
                                VariableSimpleIF variableSimpleIF = (VariableSimpleIF) iterator.next();
                                if ( variableSimpleIF instanceof ucar.nc2.Variable ) {
                                    ucar.nc2.Variable v = (ucar.nc2.Variable) variableSimpleIF;
                                    
                                    System.out.println("Found variable "+v.getShortName()+" : "+v.getFullName());
                                }
                            }
            
//            for ( Iterator it = trajDs.getTrajectories().iterator(); it.hasNext(); ) {
//                TrajectoryObsDatatype traj = (TrajectoryObsDatatype) it.next();
//                LatLonRect bb = traj.getBoundingBox();
//                System.out.println("Data bounded by: "+bb);
//                List<VariableSimpleIF> vars = traj.getDataVariables();
//
//
//                for (Iterator iterator = vars.iterator(); iterator.hasNext();) {
//                    VariableSimpleIF variableSimpleIF = (VariableSimpleIF) iterator.next();
//                    if ( variableSimpleIF instanceof ucar.nc2.Variable ) {
//                        ucar.nc2.Variable v = (ucar.nc2.Variable) variableSimpleIF;
//                        System.out.println("Found variable "+v.getShortName()+" : "+v.getFullName());
//                    } else {
//                        System.out.println("Found variable "+variableSimpleIF.getName());
//                    }
//                }
//            }
        }
        return dagb;
    }

    private static boolean skip(String curl) {
        // Check if the URL matches the regexs on input if supplied.
        boolean match = false;
        if ( regex != null && regex.size() > 0 ) {
            match = false;
            
            for (int i = 0; i < regex.size(); i++) {
                String rx = regex.get(i);
                if ( rx.startsWith( "\"" ) && rx.endsWith( "\"" ) )
                      rx = regex.get(i).substring( 1, regex.get(i).length( ) - 1 ); 
                match = match || Pattern.matches(rx, curl);
            }

        }
        return match;
    }

    public org.jdom.Document createXMLfromNetcdfDataset(NetcdfDataset
            ncds,
            String url) {
        DatasetsGridsAxesBean beans = createBeansFromNetcdfDataset(url, false, null);
        if ( beans == null ) {
            System.err.println("Unable to create XML for "+url);
            return null;
        }
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

    static public AxisBean makeTimeAxisStartEnd(CoordinateAxis1DTime axis){
    	// id doesn't matter
    	return makeTimeAxis(axis, "", true);
    }
    static private AxisBean makeTimeAxis(CoordinateAxis1DTime axis, String id) {
    	return makeTimeAxis(axis, id, false);
    }
    static private AxisBean makeTimeAxis(CoordinateAxis1DTime axis, String id, boolean setEnd) {

        // LAS only understands time units of: 'year', 'month', 'day', and 'hour'

        DateTimeFormatter fmt = null;

        String type = "t";
        AxisBean axisbean = new AxisBean();
        axisbean.setType(type);
        axisbean.setElement(axis.getShortName() + "-" + type + "-" + id);
        ArangeBean arange = new ArangeBean();


        String calendar = "standard";  // The mixed Gregorian/Julian calendar in Java and UDUNITS.
        Attribute calendarAttribute = axis.findAttribute("calendar");
        if (calendarAttribute != null) {
            calendar = calendarAttribute.getStringValue().toLowerCase();
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
                System.err.println("Cannot parse supplied time format.  Will determine format instead.");
            }
        }
        DateUnit dateUnit = null;
        try {
            // Why, oh why would somebody do this, but they did.
            if (unitsString.startsWith("\"") ) unitsString = unitsString.substring(1, unitsString.length());
            if (unitsString.endsWith("\"") ) unitsString = unitsString.substring(0, unitsString.length()-1);
            dateUnit = new DateUnit(unitsString);
        } catch (Exception e) {

            System.err.println("Cannot parse units string.");
        }

        if (dateUnit == null) {
            System.err.println("Not a date Unit String: " + unitsString);
        }

        // This is the Joda Time Chronology that corresponds to the
        // Java Gregorian Calendar and the Udunits Calendar.

        // Use this chronology and the UTC Time Zone
        Chronology chrono = GJChronology.getInstance(DateTimeZone.UTC);

        // If calendar attribute is set, use appropriate Chronology.
        if (calendar.equals("proleptic_gregorian") ) {
            chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        } else if (calendar.equals("noleap") || calendar.equals("365_day") ) {
            chrono = NoLeapChronology.getInstanceUTC();
        } else if (calendar.equals("julian") ) {
            chrono = JulianChronology.getInstance(DateTimeZone.UTC);
        } else if ( calendar.equals("all_leap") || calendar.equals("366_day") ) {
            chrono = AllLeapChronology.getInstanceUTC();
        } else if ( calendar.equals("360_day") ) {  /* aggiunto da lele */
            chrono = ThreeSixtyDayChronology.getInstanceUTC();
        }

        if (axis.getSize() >= 2.) {
            arange.setSize(String.valueOf(axis.getSize()));
            // Only do this if the user specified the axis was irregular, meaning the axis span high frequency
            // and irregular like a time series from a sensor that reports often but at irregular intervals.
            double t0 = axis.getCoordValue(0);
            double t1 = axis.getCoordValue(1);
            DateTime jodaDate1 = makeDate(t0, dateUnit, chrono);
            DateTime jodaDate2 = makeDate(t1, dateUnit, chrono);
            if ( t0 < -1000 && t1 >= 0 ) {
                // This start time looks suspicious, let's leave it out...
                t0 = axis.getCoordValue(1);
                t1 = axis.getCoordValue(2);
                jodaDate1 = makeDate(t0, dateUnit, chrono);
                jodaDate2 = makeDate(t1, dateUnit, chrono);
                arange.setSize(String.valueOf(axis.getSize() - 1));
            }

            if ( Math.abs(jodaDate2.getMillis() - jodaDate1.getMillis()) < 3600*1000 ) {
                irregular = true;
            }
            if ( irregular ) {

                System.out.println("Time axis is irregular");
                fmt = DateTimeFormat.forPattern(patterns[4]);
                int length = (int) axis.getSize();
                // Get the entire span of time
                t1 = axis.getCoordValue(length-1);
                jodaDate2 = makeDate(t1, dateUnit, chrono);

                Duration duration = new Duration(jodaDate1, jodaDate2);
                Period period = duration.toPeriod();
                Hours hours = period.toStandardHours();
                int hrs = hours.getHours();
                if (period.getMinutes() > 0 ) {
                    hrs = hrs + 1;
                }
                arange.setSize(String.valueOf(hrs));
                arange.setStart(fmt.print(jodaDate1.withZone(DateTimeZone.UTC)));
                if(setEnd){
                    double t3 = axis.getCoordValue(axis.getCoordValues().length - 1);
                    DateTime jodaDate3 = makeDate(t3, dateUnit, chrono);
                    arange.setEnd(fmt.print(jodaDate3.withZone(DateTimeZone.UTC)));
                }
                arange.setStep("1");
                axisbean.setUnits("hour");
                axisbean.setArange(arange);
            } else {


                // Returns the number of years, months, weeks, days,
                // hours, minutes, seconds, and millis between these
                // two dates.

                // Only one should be greater than 0.


                if ( unitsString.contains("0001") || unitsString.contains("1-1-1") ) {
                    // ESRL/PSD climo hack...
                    int year1 = jodaDate1.get(DateTimeFieldType.year());
                    int year2 = jodaDate2.get(DateTimeFieldType.year());
                    // If both dates 'decode' as year 1 then it's likely a climo.
                    if ( year1 == 1 && year2 == 1 ) {
                        axisbean.setModulo(true);
                    }
                }

                int step = 0;
                Period period = new Period(jodaDate1.withZone(DateTimeZone.UTC), jodaDate2.withZone(DateTimeZone.UTC));
                int numPeriods = 0;
                String periods = "";
                int values[] = period.getValues();
                DurationFieldType types[] = period.getFieldTypes();
                // Finds spacing in terms of years, months, weeks, days, hours, minutes, seconds, millis
                // We will check years, months, weeks, days, hours
                // If it's less than an hour it should get caught by the "irregular (should be high frequency)" test
                for (int i = 0; i < 5; i++) {
                    if (values[i] > 0) {
                        numPeriods++;
                        // set if not set by command line arg.
                        if (fmt == null ) {
                            // Sometimes the start time is not 0 even though the interval between times is in whole units.
                            // If it is, include the hours.
                            if ( jodaDate1.get(DateTimeFieldType.hourOfDay()) > 0 )  {
                                fmt = DateTimeFormat.forPattern(pattern_with_hours);
                            } else {
                                fmt = DateTimeFormat.forPattern(patterns[i]);
                            }
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
                        if ( zeroOrigin ) {
                            axisbean.setModulo(true);
                        }
                    } else if ( values[1] > 0 ) {
                        // We're again guessing that the value is months (and everything else is in the noise)
                        axisbean.setUnits("month");
                        arange.setStep(String.valueOf(values[1]));
                        if ( zeroOrigin ) {
                            axisbean.setModulo(true);
                        }
                    } else if ( numPeriods == 2 && periods.contains("week") && periods.contains("day")  ) {
                        // We can convert this to days. :-)
                        axisbean.setUnits("day");
                        step = 7*values[2] + values[3];
                        arange.setStep(String.valueOf(step));

                    } else {
                        System.err.println("Too many periods: " + periods);
                        //Try just dumping out the formatted times
                        axisbean.setArange(null);
                        double t[] = axis.getCoordValues();
                        String ts[] = new String[t.length];
                        // We don't know what these times look like.  Use a format with everything.

                        for (int i = 0; i < t.length; i++) {
                            DateTime dt = makeDate(t[i], dateUnit, chrono);
                            ts[i] = ferret_time_formatter.print(dt.withZone(DateTimeZone.UTC));
                        }
                        axisbean.setV(ts);

                    }
                }
                Boolean forceAxis = (Boolean) forceAxes.get("t");
                if ( forceAxis == null ) {
                    forceAxis = true;
                }
                String units = axisbean.getUnits();
                if ( (axis.isRegular() || axisbean.getUnits().equals("month")) ||
                        forceAxis.booleanValue()) {

                    // Months are "regular" according to LAS, but not according
                    // to the isRegular() test, so we need special code for
                    // months.


                    // This format should work.  LAS will drop the "day" if it's not needed.
                    if (fmt == null ) {
                        fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
                    }

                    String str = fmt.print(jodaDate1.withZone(DateTimeZone.UTC));

                    if ( str.startsWith("-") ) {

                        str = str.substring(1, str.length());
                        str = str.replace("0001", "0000");
                        axisbean.setModulo(true);
                    }
                    arange.setStart(str);
                    if(setEnd){
                        double t3 = axis.getCoordValue(axis.getCoordValues().length - 1);
                        DateTime jodaDate3 = makeDate(t3, dateUnit, chrono);
                        String str3 = fmt.print(jodaDate3.withZone(DateTimeZone.UTC));

                        if ( str.startsWith("-") ) {

                            str3 = str3.substring(1, str.length());
                            str3 = str3.replace("0001", "0000");
                        }
                        arange.setEnd(str3);
                    }
                    axisbean.setArange(arange);
                }
                else {
                    // Layout the time axis using "v" elements.
                    axisbean.setArange(null);
                    double t[] = axis.getCoordValues();
                    String ts[] = new String[t.length];
                    for (int i = 0; i < t.length; i++) {
                        DateTime dt = makeDate(t[i], dateUnit, chrono);
                        ts[i] = ferret_time_formatter.print(dt.withZone(DateTimeZone.UTC));
                    }
                    axisbean.setV(ts);

                }
            }
        }
        else {
            axisbean.setArange(null);
            if ( axis.getSize() > 0 ) {

                double t[] = axis.getCoordValues();
                String ts[] = new String[t.length];
                // We don't know what these times look like.  Use a format with everything.

                for (int i = 0; i < t.length; i++) {
                    DateTime dt = makeDate(t[i], dateUnit, chrono);
                    ts[i] = ferret_time_formatter.print(dt.withZone(DateTimeZone.UTC));
                }

                axisbean.setV(ts);        
            }
        }
        return axisbean;
    }
    private static DateTime makeDate(double d, DateUnit dateUnit, Chronology chrono) {
        // Extract the bits and pieces from the dataUnit
        String pstring = dateUnit.getUnitsString().toLowerCase();
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
                if ( double_hour > 0 ) {
                    remainder_min = (double_hour - Math.floor(double_hour))*60.;
                } else {
                    remainder_min = (Math.floor(double_hour) - double_hour)*60.;
                }
                minutes = Double.valueOf(remainder_min).intValue();
            } 
            // Use millis for hours, minutes, seconds and millis since they are more likely to overflow an int.
            long milli = (long)(d*3600.*1000.);
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
            System.err.println("Could not figure out the base time interval for this units string. "+pstring+" does not appear to be year, month, week, day, hour, minute, second or milliseconds.");
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
                    System.err.println("Could not parse "+origin_string+" with yyyy-MM-dd'T'HH:mm:ss.SSSZ or yyyy-MM-dd HH:mm:ss.  Use -f option to give format of time string.");
                } catch (IllegalArgumentException iae2 ) {
                    System.err.println("Could not parse "+origin_string+" with yyyy-MM-dd'T'HH:mm:ss.SSSZ or yyyy-MM-dd HH:mm:ss.  Use -f option to give format of time string.");
                }
            } catch (IllegalArgumentException iae ) {
                try {
                    origin = chrono_fmt_iso.parseDateTime(origin_string).withChronology(chrono);
                } catch ( UnsupportedOperationException  uoe2) {
                    System.err.println("Could not parse "+origin_string+" with yyyy-MM-dd'T'HH:mm:ss.SSSZ or yyyy-MM-dd HH:mm:ss");
                } catch (IllegalArgumentException iae2 ) {
                    System.err.println("Could not parse "+origin_string+" with yyyy-MM-dd'T'HH:mm:ss.SSSZ or yyyy-MM-dd HH:mm:ss");
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
        double diff = Math.abs(max - min);
        // divide the range into 50 equal increments...
        ArangeBean arange = new ArangeBean();
        axisbean.setUnits(axis.getUnitsString());
        arange.setSize("50");
        arange.setStep(String.valueOf(diff/50.));
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

    //  ========================================================= reverse
    //  Stolen directly off the web from:
    //  http://leepoint.net/notes-java/data/arrays/34arrayreverse.html
    //  Converted from int array to double...
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
            System.err.println(e.getMessage());
        }
    }

    static public void outputXML(String outfile, Element element, boolean append) {
        File outputFile = new File(outfile);
        outputXML(outputFile, element, append);
    }
    static public void outputXML(File outputFile, Element element, boolean append) {
        try {
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
            System.err.println(e.getMessage());
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
            System.err.println("Cannot create SHA-1 hash." + e.getMessage());
            encoding = "id-12345";
        }
        return encoding;
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

        String regexOption = options.get("regex");
        if ( regexOption != null && !regexOption.equals("") ) {
            clregex = new String[]{"regexOption"};
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

    public DateTimeFormatter getFerret_time_formatter() {
        return ferret_time_formatter;
    }

    public void setFerret_time_formatter(
            DateTimeFormatter ferretTimeFormatter) {
        ferret_time_formatter = ferretTimeFormatter;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        ADDXMLProcessor.verbose = verbose;
    }

    public boolean isGenerate_names() {
        return generate_names;
    }

    public void setGenerate_names(boolean generateNames) {
        generate_names = generateNames;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        fileCount = fileCount;
    }

    public HashMap<String, Boolean> getForceAxes() {
        return forceAxes;
    }
    public void setRegex(String[] regex) {
        this.clregex = regex;
    }
    public void setForceAxes(HashMap<String, Boolean> forceAxes) {
        forceAxes = forceAxes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        title = title;
    }

    public static String getVersion_string() {
        return version_string;
    }

    public static void setVersion_string(String versionString) {
        version_string = versionString;
    }

    public String getGlobal_title_attribute() {
        return global_title_attribute;
    }

    public void setGlobal_title_attribute(String globalTitleAttribute) {
        global_title_attribute = globalTitleAttribute;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        format = format;
    }

    public String getUnits_format() {
        return units_format;
    }

    public void setUnits_format(String unitsFormat) {
        units_format = unitsFormat;
    }

    public String getGroup_type() {
        return group_type;
    }

    public void setGroup_type(String groupType) {
        group_type = groupType;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String groupName) {
        group_name = groupName;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        group = group;
    }

    public boolean isCategory() {
        return category;
    }

    public void setCategory(boolean category) {
        category = category;
    }

    public boolean isUse_suffix() {
        return use_suffix;
    }

    public void setUse_suffix(boolean useSuffix) {
        use_suffix = useSuffix;
    }

    public boolean isOneDataset() {
        return oneDataset;
    }

    public void setOneDataset(boolean oneDataset) {
        ADDXMLProcessor.oneDataset = oneDataset;
    }

    public boolean isIrregular() {
        return irregular;
    }

    public void setIrregular(boolean irregular) {
        ADDXMLProcessor.irregular = irregular;
    }

    public boolean isEsg() {
        return esg;
    }

    public void setEsg(boolean esg) {
        esg = esg;
    }

    public String[] getPatterns() {
        return patterns;
    }

    public String getPatternWithHours() {
        return pattern_with_hours;
    }

} // end of class
