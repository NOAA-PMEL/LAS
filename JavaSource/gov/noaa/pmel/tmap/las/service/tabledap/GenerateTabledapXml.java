/**
 * GenerateTabledapXml
 */
package gov.noaa.pmel.tmap.las.service.tabledap;

import com.cohort.array.Attributes;
import com.cohort.array.DoubleArray;
import com.cohort.array.PrimitiveArray;
import com.cohort.array.StringArray;
import com.cohort.util.Calendar2;
import com.cohort.util.Math2;
import com.cohort.util.MustBe;
import com.cohort.util.String2;
import com.cohort.util.Test;

import gov.noaa.pfel.coastwatch.pointdata.Table;
import gov.noaa.pfel.coastwatch.util.SSR;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;


/**
 * This generates the dataset xml file describing all of the datasets
 * available from a Tabledap 
 * (http://coastwatch.pfel.noaa.gov/coastwatch/erddap/tabledap/index.html) server.
 *     
 * @author Bob Simons (bob.simons@noaa.gov)
 *
 */
public class GenerateTabledapXml {
  
    final Logger log = LogManager.getLogger(GenerateTabledapXml.class.getName());
    
    /**
     * This generates the dataset xml file describing all of the datasets
     * available from a Tabledap server.
     *
     * @param args 
     *   args[0] must be the name to be given to the server (e.g., "ERD TableDAP")
     *   args[1] must be the id to be given to the server (e.g., erd_tabledap)
     *   args[2] must be the url of the tabledap server
     *     (e.g., http://coastwatch.pfel.noaa.gov/cwexperimental/erddap/tabledap/).
     *   args[3] must be complete name for the .xml file to be created.
     * @throws Exception if there is an error
     */
    public static void main(String args[]) throws Exception {
        String serverName = args[0];
        String serverID = args[1];
        String serverUrl = args[2];
        String xmlFileName = args[3];
        String2.log("GenerateTabledapXml serverName=" + serverName + 
            " serverID=" + serverID + 
            "\n  serverUrl=" + serverUrl + "\n  xmlFileName=" + xmlFileName);

        //get the list of datasets
        //  "table": {
        //    "columnNames": ["griddap Access", "tabledap Access", "Title", "Institution", "Summary", "Info", "Background Info", "ID"],
        //    "rows": [
        //      ["", "http://coastwatch.pfel.noaa.gov/cwexperimental/erddap/tabledap/csc_mwfs/wt", "Buoy Data (Water Temperature) from the NOAA CSC microWFS", "NOAA CSC", "[Normally, the summary describes the dataset. Here, it describes \nthe server.] \nThe mission of the NOAA CSC Data Transport Laboratory (DTL) is to \nsupport the employment of data transport technologies that are \ncompatible with Ocean.US Data Management and Communications (DMAC) \nguidance at the local and regional levels. This is accomplished \nthrough the identification, evaluation, and documentation of \nrelevant data transport technology candidates. In following that \nmission, the DTL is exploring the use of the Open Geospatial \nConsortium (OGC) Web Feature Service (WFS) and the Geography \nMarkup Language (GML) Simple Feature Profile to transport in-situ \ntime series data.", "http://www.csc.noaa.gov/DTL/dtl_proj4_gmlsfp_wfs.html", "csc_mwfs/wt"],
        Table datasetsTable = new Table();
        String jsonDatasets = SSR.getUrlResponseString(serverUrl + "index.json");
        String2.log("\njsonDatasets=" + jsonDatasets);
        datasetsTable.readJson(serverUrl, jsonDatasets);
        int nDatasets = datasetsTable.nRows();
        String notFound = " column not found in datasets table (colNames=" + 
            datasetsTable.getColumnNamesCSVString() + ").";
        int accessCol = datasetsTable.findColumnNumber("tabledap Access");
        if (accessCol < 0)
            throw new Exception("'tabledap Access'" + notFound);
        int titleCol = datasetsTable.findColumnNumber("Title");
        if (titleCol < 0)
            throw new Exception("'Title'" + notFound);
        int infoCol = datasetsTable.findColumnNumber("Info");
        if (infoCol < 0)
            throw new Exception("'Info'" + notFound);
        int backgroundCol = datasetsTable.findColumnNumber("Background Info");
        if (backgroundCol < 0)
            throw new Exception("'Background Info'" + notFound);
        int idCol = datasetsTable.findColumnNumber("ID");
        if (idCol < 0)
            throw new Exception("'ID'" + notFound);

        //create the outputFile
        //LAS doesn't specify or use character encoding information(!)
        Writer writer = new BufferedWriter(new FileWriter(xmlFileName));
        writer.write("<datasets>\n");

        //for each dataset
        //nDatasets = 1; //while developing        
        for (int dsRow = 0; dsRow < nDatasets; dsRow++) {
            //get the info table
            String id = datasetsTable.getStringData(idCol, dsRow);
            String idForLAS = String2.replaceAll(id, '/', '_');  //serverID + "_" + 
            String title = datasetsTable.getStringData(titleCol, dsRow);
            String access = datasetsTable.getStringData(accessCol, dsRow);
            String info = datasetsTable.getStringData(infoCol, dsRow);
            String background = datasetsTable.getStringData(backgroundCol, dsRow);
            String2.log("\ngetting erddapInfo for id=" + id + "\n  url=" + info);
            Table infoTable = new Table();
            infoTable.readErddapInfo(info);
            writer.write(
                "  <" + idForLAS + "\n" +
                "    name = \"" + title + "\"\n" +
                "    url = \"" + access + "\"\n" +
                "    doc = \"" + background + "\">\n" +  
                "    <properties>\n" +
                "      <ui>\n" +
                "        <default>file:ui.xml#dapper_demo</default>\n" + //???
                "      </ui>\n" +
                "      <tabledap_access>\n" +
                "        <server>" + serverName + "</server>\n" + //???
                "        <id>" + id + "</id>\n" +
                "        <title>" + title + "</title>\n");  //???how differ from 'name' above
            int col = infoTable.findColumnNumber("longitude");
            if (col >= 0) {
                PrimitiveArray pa = infoTable.columnAttributes(col).get("actual_range");
                String domain = pa == null || pa.getDouble(1) > 180? "0:360" : "-180:180";
                writer.write(
                    "        <longitude>longitude</longitude>\n" +
                    "        <lon_domain>" + domain + "</lon_domain>\n");
            }
            col = infoTable.findColumnNumber("latitude");
            if (col >= 0) {
                writer.write(
                "        <latitude>latitude</latitude>\n");
            }
            col = infoTable.findColumnNumber("altitude");
            if (col >= 0) {
                writer.write(
                "        <altitude>altitude</altitude>\n" +
                "        <altitude_units>meters</altitude_units>\n" +
                "        <positive>up</positive>\n");
            }
            col = infoTable.findColumnNumber("time");
            if (col >= 0) {
                writer.write(
                "        <time>time</time>\n" +
                "        <time_units>sec since 1970-01-01T00:00:00Z</time_units>\n" +
                "        <time_type>double</time_type>\n");
            }
            writer.write(
                //"        <missing>NaN</missing>\n" +  //must be same for all variables???
                "      </tabledap_access>\n" +
                "      <product_server>\n" +
                "        <ui_timeout>120000</ui_timeout>\n" +  //seconds
                "        <ps_timeout>120000</ps_timeout>\n" +  //seconds
                "        <use_cache>false</use_cache>\n" +
                "      </product_server>\n" +
                "    </properties>\n" +
                "    <variables>\n");

            //write the info for the non-LonLatAltTime variables
            for (col = 0; col < infoTable.nColumns(); col++) {
                String colName = infoTable.getColumnName(col);
                if (colName.equals("longitude") || colName.equals("latitude") || 
                    colName.equals("altitude")  || colName.equals("time"))
                    continue;
                Attributes atts = infoTable.columnAttributes(col);
                String units = atts.getString("units");
                String longName = atts.getString("long_name");
                if (longName == null) longName = atts.getString("standard_name");
                if (longName == null) longName = colName;
                writer.write(
                    "      <" + colName + 
                        (units == null? "" : " units=\"" + units + "\"") +
                        " name=\"" + longName + "\"\n" +
                    "        url=\"#" + colName + "\">\n" +  //huh ???
                    //"        <link match=\"" + /lasdata/grids/pfeg_grid + "\"/>\n" +  //huh???
                    "      <" + colName + ">\n");
            }
            writer.write(
                "    </variables>\n" +
                "  </" + idForLAS + ">\n");
        }

        writer.write("</datasets>\n");
        writer.close();
    }

    /**
     * This tests this class by generating some xml for ERD's tabledap server
     * and displaying it.
     *
     * @throws Exception if trouble
     */
    public static void test() throws Exception {
//!!! currently uses cwexperimental; eventually switch to coastwatch
        String fileName = "C:/pmelsvn/conf/example/tabledap_erd.xml";
        GenerateTabledapXml.main(new String[]{"ERD TableDAP", "erd_tabledap",
            "http://coastwatch.pfel.noaa.gov/cwexperimental/erddap/tabledap/",
            fileName});
        String2.log("GenerateTabledapXml fileName=" + fileName + "\n" + String2.readFromFile(fileName)[1]);
    }

}
