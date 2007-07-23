package gov.noaa.pmel.tmap.las.service.database;

import java.io.File;

import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import junit.framework.TestCase;

public class DatabaseToolTest extends TestCase {
    
    public static void main(String[] args) {
        if ( args.length < 1 ) {
            System.out.println("Using the default test case.");
            String requestString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
"<backend_request>"+
" <constraint type=\"variable\">"+
"    <op>ge</op>"+
"    <rhs>4.5</rhs>"+
"    <lhs>Temperature</lhs>"+
"  </constraint>"+
"  <constraint type=\"text\">"+
"    <lhs>CruiseNum</lhs>"+
"    <op>=</op>"+
"    <rhs>23</rhs>"+
"  </constraint>"+
"  <region ID=\"region_0\">"+
"    <x_lo>0.0</x_lo>"+
"    <x_hi>129.0</x_hi>"+
"    <y_lo>-75.0</y_lo>"+
"    <y_hi>34.0</y_hi>"+
"    <z_lo>0</z_lo>"+
"    <z_hi>200</z_hi>"+
"    <t_lo>01-Jan-1977</t_lo>"+
"    <t_hi>31-Dec-1989</t_hi>"+
"  </region>"+
"  <properties>"+
"    <property_group type=\"database\">"+
"      <property>"+
"        <name>script</name>"+
"        <value>sql</value>"+
"      </property>"+
"    </property_group>"+
"    <property_group type=\"operation\">"+
"      <property>"+
"        <name>name</name>"+
"        <value>Database Extraction</value>"+
"      </property>"+
"      <property>"+
"        <name>ID</name>"+
"        <value>DBExtract_1</value>"+
"      </property>"+
"      <property>"+
"         <name>script</name>"+
"         <value>example</value>"+
"       </property>"+
"    </property_group>"+
"    <property_group type=\"ferret\">"+
"      <property>"+
"        <name>fill_type</name>"+
"        <value>fill</value>"+
"      </property>"+
"      <property>"+
"        <name>size</name>"+
"        <value>.5</value>"+
"      </property>"+
"      <property>"+
"        <name>land_type</name>"+
"        <value>shade</value>"+
"      </property>"+
"      <property>"+
"        <name>format</name>"+
"        <value>gif</value>"+
"      </property>"+
"    </property_group>"+
"  </properties>"+
"  <response ID=\"DBExtractResponse\">"+
"    <result type=\"debug\" ID=\"db_debug\" url=\"81D94713B391DB01F371114C4BC64780_db_debug.txt\" index=\"0\" />"+
"    <result type=\"netCDF\" ID=\"interNetCDF\" url=\"81D94713B391DB01F371114C4BC64780_interNetCDF.nc\" index=\"1\" />"+
"  </response>"+
"  <dataObjects>"+
"    <data url=\"Indian\" var=\"Temperature\" title=\"Temperature: \" xpath=\"/lasdata/datasets/Indian_Data/variables/Temperature\">"+
"      <attributes>"+
"        <attribute>"+
"          <name>units</name>"+
"          <value>umol/kg</value>"+
"        </attribute>"+
"        <attribute>"+
"          <name>name</name>"+
"          <value>Temperature:</value>"+
"        </attribute>"+
"      </attributes>"+
"      <region IDREF=\"region_0\" />"+
"      <properties>"+
"        <property_group type=\"database_access\">"+
"          <property>"+
"            <name>cruiseID</name>"+
"            <value>CruiseNum</value>"+
"          </property>"+
"          <property>"+
"            <name>timeout</name>"+
"            <value>180</value>"+
"          </property>"+
"          <property>"+
"            <name>depth_units</name>"+
"            <value>meters</value>"+
"          </property>"+
"          <property>"+
"            <name>time</name>"+
"            <value>t</value>"+
"          </property>"+
"          <property>"+
"            <name>db_table</name>"+
"            <value>Indian</value>"+
"          </property>"+
"          <property>"+
"            <name>depth</name>"+
"            <value>Depth</value>"+
"          </property>"+
"          <property>"+
"            <name>db_title</name>"+
"            <value>LAS in-situ demo</value>"+
"          </property>"+
"          <property>"+
"            <name>missing</name>"+
"            <value>-999</value>"+
"          </property>"+
"          <property>"+
"            <name>lon_domain</name>"+
"            <value>0:360</value>"+
"          </property>"+
"          <property>"+
"            <name>db_host</name>"+
"            <value>localhost</value>"+
"          </property>"+
"          <property>"+
"            <name>db_passwd</name>"+
"            <value>lasrules</value>"+
"          </property>"+
"          <property>"+
"            <name>db_login</name>"+
"            <value>las</value>"+
"          </property>"+
"          <property>"+
"            <name>longitude</name>"+
"            <value>Longitude</value>"+
"          </property>"+
"          <property>"+
"            <name>db_name</name>"+
"            <value>LAS_insitu_demo</value>"+
"          </property>"+
"          <property>"+
"            <name>equal_op</name>"+
"            <value>=</value>"+
"          </property>"+
"          <property>"+
"            <name>db_type</name>"+
"            <value>mysql</value>"+
"          </property>"+
"          <property>"+
"            <name>latitude</name>"+
"            <value>Latitude</value>"+
"          </property>"+
"          <property>"+
"            <name>time_sample</name>"+
"            <value>19990101</value>"+
"          </property>"+
"        </property_group>"+
"        <property_group type=\"ui\">"+
"          <property>"+
"            <name>default</name>"+
"            <value>file:ui.xml#insitu_demo</value>"+
"          </property>"+
"        </property_group>"+
"        <property_group type=\"product_server\">"+
"          <property>"+
"            <name>ui_timeout</name>"+
"            <value>20</value>"+
"          </property>"+
"          <property>"+
"            <name>ps_timeout</name>"+
"            <value>3600</value>"+
"          </property>"+
"          <property>"+
"            <name>use_cache</name>"+
"            <value>false</value>"+
"          </property>"+
"        </property_group>"+
"      </properties>"+
"    </data>"+
"  </dataObjects>"+
"</backend_request>";
            LASBackendRequest lasBackendRequest = new LASBackendRequest();      
            try {
                JDOMUtils.XML2JDOM(requestString, lasBackendRequest);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            LASBackendResponse lasBackendResponse = new LASBackendResponse();
            DatabaseTool databaseTool;
            try {
                databaseTool = new DatabaseTool();
                lasBackendResponse = databaseTool.run(lasBackendRequest);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }               
            System.out.println(lasBackendResponse.toString());
        } else {
            for (int i=0; i<args.length; i++ ) {
                String request = args[i];
                File file = new File(request);
                LASBackendRequest lasBackendRequest = new LASBackendRequest();      
                try {
                    JDOMUtils.XML2JDOM(file, lasBackendRequest);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                LASBackendResponse lasBackendResponse = new LASBackendResponse();
                DatabaseTool databaseTool;
                try {
                    databaseTool = new DatabaseTool();
                    lasBackendResponse = databaseTool.run(lasBackendRequest);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }               
                System.out.println(lasBackendResponse.toString());
            }
        }
    }
}
