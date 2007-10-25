/**
 * ObisBackendService
 */
package gov.noaa.pmel.tmap.las.service.obis;

import com.cohort.array.Attributes;
import com.cohort.util.Calendar2;
import com.cohort.util.String2;
import com.cohort.util.Test;

import gov.noaa.pfel.coastwatch.pointdata.DigirHelper;
import gov.noaa.pfel.coastwatch.pointdata.Table;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASDapperBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.service.BackendService;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.jdom.JDOMException;

/**
 * OBIS is an XML schema (http://www.iobis.org/tech/provider/questions)
 * which extends the Darwin schema. Essentially, these schemas define
 * an abstract table of data with standard column names (e.g., 
 * darwin:ScientificName, darwin:Longitude, darwin:Latitude). Different
 * data sources (usually museums) use the Digir engine (http://digir.sourceforge.net/)
 * to provide an Digir/OBIS frontend to their internal database of 
 * data (usually data about specimens in their collections).
 * DiGIR is an engine which takes XML requests 
 * (in the request format defined in the DiGIR schema)
 * for data from a database table 
 * (as defined in a schema like Darwin and OBIS) and returns a 
 * subset table stored as XML data 
 * (in the response format defined in the DiGIR schema).
 *
 * <p>This class serves data from any a Darwin2 or OBIS data source which uses
 * the Digir protocol for XML requests and responses.
 *
 * <p> Note that the main OBIS source 
 * (http://iobis.marine.rutgers.edu/digir2/DiGIR.php)
 * is often down. 
 * (Contact: Phoebe Y. Zhang, phoebe@imcs.marine.rutgers.edu ).
 * For a way to get quick access to the data from all of the 
 * obis data providers/resources at www.iobis.org,
 * see IobisBackendService, which is also more reliable.
 *
 * <p>Digir information: http://digir.sourceforge.net/
 * Most useful info about the whole Digir system: 
 *   http://diveintodigir.ecoforge.net/draft/digirdive.html
 *   and http://digir.net/prov/prov_manual.html .
 * A list of Digir providers: http://bigdig.ecoforge.net/wiki/SchemaStatus .
 *
 * <p>Darwin2 is the original schema for use with the Digir engine.
 *
 * <p>OBIS is an oceanography-related schema which extends Darwin2.
 * Obis info: http://www.iobis.org .
 * Obis schema info: http://www.iobis.org/tech/provider/questions .
 *
 * <p>Namespace and schema urls are stored as gov.noaa.pfel.coastwatch.pointdata.Table
 * constants.
 *
 * <p>Many helper methods (e.g., for getting the metadata/list of resources from a 
 * provider) are in gov.noaa.pfel.coastwatch.pointdata.DigirHelper.
 *
 * <p>For bounding box queries: <ul>
 * <li> x constraints apply to darwin:Longitude    (which is returned as the LON column in the results file)
 * <li> y constraints apply to darwin:Latitude     (which is returned as the LAT column in the results file)
 * <li> z constraints apply to darwin:MinimumDepth (which is returned as the DEPTH column in the results file)
 * <li> t constraints apply to a composite created from 
 *    darwin:YearCollected, darwin:MonthCollected, darwin:DayCollected and darwin:TimeOfDay
 *    (which is returned as the TIME column in the results file).
 * <li> the ID column in the results file is a composite of 
 *    [darwin:InstitutionCode]:[darwin:CollectionCode]:[darwin:CatalogNumber]
 * </ul>

 * <p>!!!In addition to the various classes this uses, this needs access to 
 * two properties files: gov/noaa/pfel/coastwatch/pointdata/DigirDarwin2.properties
 * and gov/noaa/pfel/coastwatch/pointdata/DigirObis.properties
 * which have lists of the columns (variables) defined by the Darwin2 and
 * OBIS schemas (including the data types, units, and comments).
 *
 * <p> Note to Bob: 
 * Problems can occur from compiling the Table class with one version of
 * the netcdf libraries and compiling this class with another.
 * For this to run correctly, first use delcwexperimentalclasses.bat,
 * then compile all with tool#3 (java-armstrong).
 *
 * @author Roland Schweitzer
 * @author Bob Simons (bob.simons@noaa.gov)
 *
 */
public class ObisBackendService extends BackendService {

    private static Logger log = LogManager.getLogger(ObisBackendService.class.getName());

    /**
     * This processes the backendRequestXML and returns the requested data.
     *
     * @param backendRequestXML the information from an Obis-compatible LASBackendRequest.xml file.
     * @param cacheFileName [I'm not sure what this is for. 
     *     It may be a name that this method can used for a cacheFile, if one is needed.]
     *     Currently, this is not used.
     * @return the results of the request (from lasBackendResponse.toString()).
     *    This indicates either success and failure.
     */
    public String getProduct(String backendRequestXML, String cacheFileName) 
            throws Exception, LASException, IOException, JDOMException {       

        //convert the backendRequestXML to an lasBackendRequest
        LASDapperBackendRequest backendRequest = new LASDapperBackendRequest();      
        JDOMUtils.XML2JDOM(backendRequestXML, backendRequest);        
        
        //report logging level only for "debug" and "trace" levels
        String debug = backendRequest.getProperty("las", "debug");      
        setLogLevel(debug);
        log.debug("Logging set to " + log.getEffectiveLevel().toString() + " for " + log.getName());
        
        //get the lasBackendResponse
        ObisTool tool = new ObisTool();
        LASBackendResponse lasBackendResponse = tool.run(backendRequest);
        return lasBackendResponse.toString();
    }

    /** This is the directory where the test results files will be placed. */
    public static String testResultsDirectory = "C:\\";

    /** This is the file name for the test results file (should match the name in the xml info). */
    public static String testResultsNcName = "TestObisBackendService.nc";

    /** 
     * This is the Obis-compatible LASBackendRequest.xml info that is used to test this class via main(null). 
     * The tags which are used by this method (either required or optional) are marked below.
     * Other tags may be used by other parts of LAS.
     *
     */
    public static String testRequestXml = 
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<backend_request>\n" +
//In addition to the region bounds, the request can have "variable" constraints 
//(always in the form <variable><op><value>)
//for any variable (but usually "darwin:ScientificName", "darwin:Genus" or "darwin:Species")
//to identify the desired subset of obis data:
"<constraint type=\"variable\">\n" +
"  <lhs>darwin:Genus</lhs>\n" +
"  <op>eq</op>\n" +
"  <rhs>Abietinaria</rhs>\n" +
"</constraint>\n" +
" <region ID=\"region_0\">\n" +              //required
"   <x_lo>-180</x_lo>\n" +                     //all lo and hi tags are optional
"   <x_hi>2</x_hi>\n" +
//"   <t_lo>01-Jan-1970 00:00:00</t_lo>\n" +   //If present, date info must be in Ferret format (as here).
//"   <t_hi>01-Jan-2100 00:00:00</t_hi>\n" +   //But this data doesn't have time values.
" </region>\n" +
" <properties>\n" +
"   <property_group type=\"las\">\n" +
"     <property>\n" +
"       <name>debug</name>\n" +
"       <value>debug</value>\n" +            //required, a log4j logging level
"     </property>\n" +
"   </property_group>\n" +
"   <property_group type=\"obis\">\n" +     
"     <property>\n" +
"       <name>service_action</name>\n" +
"       <value>obis</value>\n" +
"     </property>\n" +
"   </property_group>\n" +
"   <property_group type=\"operation\">\n" +
"     <property>\n" +
"       <name>service</name>\n" +
"       <value>obis</value>\n" +
"     </property>\n" +
"     <property>\n" +
"       <name>service_action</name>\n" +  //required
"       <value>obis</value>\n" +        //required
"     </property>\n" +
"     <property>\n" +
"       <name>name</name>\n" +
"       <value>Database Extraction</value>\n" +
"     </property>\n" +
"     <property>\n" +
"       <name>ID</name>\n" +
"       <value>ObisExtract</value>\n" +
"     </property>\n" +
"     <property>\n" +
"       <name>key</name>\n" +
"       <value>F9B6C974354E316D391E061962284987</value>\n" +  //dapper ends in 6
"     </property>\n" +
"   </property_group>\n" +
" </properties>\n" +
" <dataObjects>\n" +
"   <data url=\"http://www.vliz.be/digir/DiGIR.php\" \n" +  //required (especially 'url')
  "var=\"darwin:InstitutionCode\" title=\"InstitutionCode\" \n" +
  "dataset_name=\"Flanders Marine Insitute\" dataset_ID=\"FLANDERS\" " +
  "units=\"\" name=\"Institution Code\" ID=\"obis_InstitutionCode\" \n" +
  "points=\"xyzt\" intervals=\"xyzt\" grid_type=\"scattered\">\n" +
"     <region IDREF=\"region_0\" />\n" +
"     <properties>\n" +
"       <property_group type=\"database_access\">\n" +
//"         <property>\n" +                           //ignored? ObisTool always returns positive down
//"           <name>positive</name>\n" +
//"           <value>down</value>\n" +
//"         </property>\n" +
//ignored: time is always constructed from darwin:YearCollected-MonthCollected-DayCollected and TimeOfDay
//"         <property>\n" +
//"           <name>time</name>\n" +                  
//"           <value>location.profile.TIME</value>\n" +
//"         </property>\n" +
//"         <property>\n" +
//"           <name>depth_units</name>\n" +           //ignored? ObisTool always returns depth in meters
//"           <value>meters</value>\n" +
//"         </property>\n" +
//required: the resources (1, or a comma-separated list) that will be queried.
//Also known as values from the "code" column from DigirHelper.getMetadataTable.
//See the options by going to the "data url" above and perusing (or doing xpath search for) the metatdata for
//  /response/content/metadata/provider/resource/code
"         <property>\n" +                           
"           <name>db_table</name>\n" +
"           <value>tisbe</value>\n" +
"         </property>\n" +
//ignored: depth is always from darwin:MinimumDepth
//"         <property>\n" +
//"           <name>depth</name>\n" +                 //required
//"           <value>darwin:MinimumDepth</value>\n" +
//"         </property>\n" +
"         <property>\n" +                           //used for? 
"           <name>db_title</name>\n" +
"           <value>Taxonomic Information Sytem for the Belgian coastal area</value>\n" +
"         </property>\n" +
//"         <property>\n" +
//"           <name>missing</name>\n" +               //not used
//"           <value>NaN</value>\n" +
//"         </property>\n" +
//darwin:Longitude domain is specified in the schema to be -180 to 180
"         <property>\n" +
"           <name>lon_domain</name>\n" +            //used by LASBackendRequest
"           <value>-180:180</value>\n" +
"         </property>\n" +
//ObisTool constructs the time and converts to "s since 1970-01-01 00:00:00"
//"         <property>\n" +
//"           <name>time_units</name>\n" +            //ignored?
//"           <value>msec since 1970-01-01 00:00:00</value>\n" +
//"         </property>\n" +
//ObisTool always uses darwin:Longitude
//"         <property>\n" +
//"           <name>longitude</name>\n" +             //ignored?
//"           <value>location.LON</value>\n" +
//"         </property>\n" +
//"         <property>\n" +                           //ignored?
//"           <name>db_name</name>\n" +
//"           <value>OBIS</value>\n" +
//"         </property>\n" +
//"         <property>\n" +                           //ignored? a label only, not used as a url
//"           <name>db_server</name>\n" +
//"           <value>www.iobis.org</value>\n" +
//"         </property>\n" +
//ignored: ObisTool always returns time as a double column
//"         <property>\n" +
//"           <name>time_type</name>\n" +             //not used?
//"           <value>double</value>\n" +
//"         </property>\n" +
"         <property>\n" +                             //used by ? 
"           <name>db_type</name>\n" +
"           <value>obis</value>\n" +
"         </property>\n" +
//ignored: ObisTool always uses darwin:Latitude
//"         <property>\n" +
//"           <name>latitude</name>\n" +              //ignored?
//"           <value>location.LAT</value>\n" +
//"         </property>\n" +
"       </property_group>\n" +
"       <property_group type=\"ui\">\n" +           //used by ?
"         <property>\n" +
"           <name>default</name>\n" +
"           <value>file:ui.xml#nwioos_demo</value>\n" +
"         </property>\n" +
"       </property_group>\n" +
"       <property_group type=\"product_server\">\n" +
"         <property>\n" +
"           <name>ui_timeout</name>\n" +
"           <value>1000</value>\n" +
"         </property>\n" +
"         <property>\n" +
"           <name>ps_timeout</name>\n" +
"           <value>3600</value>\n" +
"         </property>\n" +
"         <property>\n" +
"           <name>use_cache</name>\n" +
"           <value>false</value>\n" +
"         </property>\n" +
"       </property_group>\n" +
"     </properties>\n" +
"   </data>\n" +

//Test getting other variables via other <data> objects.
//!!!This BackendService only looks at 'var'. Everything else is assumed to be the same!
//Requested variables and variable order match test in Table.testObis5354Table.
//var=darwin:CollectionCode
"   <data url=\"http://www.vliz.be/digir/DiGIR.php\" \n" +  //required (especially 'url')
  "var=\"darwin:CollectionCode\" title=\"CollectionCode\" \n" +
  "dataset_name=\"Flanders Marine Insitute\" dataset_ID=\"FLANDERS\" " +
  "units=\"\" name=\"Collection Code\" ID=\"obis_CollectionCode\" \n" +
  "points=\"xyzt\" intervals=\"xyzt\" grid_type=\"scattered\">\n" +
"     <region IDREF=\"region_0\" />\n" +
"   </data>\n" +
//var=darwin:ScientificName
"   <data url=\"http://www.vliz.be/digir/DiGIR.php\" \n" +  //required (especially 'url')
  "var=\"darwin:ScientificName\" title=\"ScientificName\" \n" +
  "dataset_name=\"Flanders Marine Insitute\" dataset_ID=\"FLANDERS\" " +
  "units=\"\" name=\"Scientific Name\" ID=\"obis_ScientificName\" \n" +
  "points=\"xyzt\" intervals=\"xyzt\" grid_type=\"scattered\">\n" +
"     <region IDREF=\"region_0\" />\n" +
"   </data>\n" +
//var=obis:Temperature
"   <data url=\"http://www.vliz.be/digir/DiGIR.php\" \n" +  //required (especially 'url')
  "var=\"obis:Temperature\" title=\"Temperature\" \n" +
  "dataset_name=\"Flanders Marine Insitute\" dataset_ID=\"FLANDERS\" " +
  "units=\"\" name=\"Temperature\" ID=\"obis_Temperature\" \n" +
  "points=\"xyzt\" intervals=\"xyzt\" grid_type=\"scattered\">\n" +
"     <region IDREF=\"region_0\" />\n" +
"   </data>\n" +

" </dataObjects>\n" +
" <response ID=\"DBExtractResponse\">\n" +
"   <result type=\"debug\" ID=\"db_debug\" \n" +   //recommended
  "file=\"" + testResultsDirectory + "F9B6C974354E316D391E061962284987_db_debug.txt\" \n" + //dapper ends in 6
  "index=\"0\" />\n" +
"   <result type=\"netCDF\" ID=\"netcdf\" \n" +    //results file; required 
  "file=\"" + testResultsDirectory + testResultsNcName + "\" \n" +
  "index=\"1\" />\n" +
" </response>\n" +
"</backend_request>\n";

    /**
     * This runs ObisBackendService with the xml file you specify, or with
     * a standard test xml file 
     * (which puts the results in testResultsDirectory + testResultsName).
     * This method prints getProduct's result to System.out.
     *
     * @param args If present, args[0] should be the name of an Obis-compatible
     *   LASBackendRequest.xml file.
     *   If args==null or args.length==0, this runs a standard test.
     * @throws Exception if there is an error
     */
    public static void main(String args[]) throws Exception {
        String2.log("\n*** ObisBackendService");
        long time = System.currentTimeMillis();
        DigirHelper.verbose = true;
        DigirHelper.reallyVerbose = true;
        Table.verbose = true;

        //get the requestXml
        String requestXml = testRequestXml;
        boolean doStandardTest = true;
        if (args != null && args.length > 0) {
            doStandardTest = false;
            DigirHelper.reallyVerbose = false;
            String results[] = String2.readFromFile(args[0]);
            if (results[0].equals(""))
                requestXml = results[1];
            else throw new Exception(results[0]);
        }

        //run ObisBackendService
        ObisBackendService service = new ObisBackendService();
        String lasResultXml = service.getProduct(requestXml, null);
        System.out.println("lasResultXml=\n" + lasResultXml); 

        //check the results of the standard test
        if (doStandardTest) {
            Table table = new Table();
            table.readFlatNc(testResultsDirectory + testResultsNcName, null, 1);
            String2.log(table.toString());
            DigirHelper.testObisAbietinariaTable(table);

//Need test of request that returns no data. Is table as desired?
//Need test of xLo > xHi, so split lon request.
        }

        //it is possible to pass the standard test but get an error code back from the service
        Test.ensureTrue(lasResultXml.indexOf("type=\"error\"") < 0, 
            String2.ERROR + " in lasResultXml=\n" + lasResultXml);
        
        String2.log("  ObisBackendService finished successfully. TIME=" +
            (System.currentTimeMillis() - time));

    }
}
