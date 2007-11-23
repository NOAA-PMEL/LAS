
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASMapScale;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.jdom.ServerConfig;
import gov.noaa.pmel.tmap.las.jdom.LASDocument;

import gov.noaa.pmel.tmap.las.ui.state.OptionBean;
import gov.noaa.pmel.tmap.las.ui.state.StateNameValueList;
import gov.noaa.pmel.tmap.las.ui.state.TimeSelector;

import gov.noaa.pmel.tmap.las.util.NameValuePair;
import gov.noaa.pmel.tmap.las.util.Dataset;
import gov.noaa.pmel.tmap.las.util.Variable;
import gov.noaa.pmel.tmap.las.util.TimeAxis;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.net.URLDecoder;
import java.net.*;

import ucar.nc2.*;
import ucar.nc2.dataset.*;
import dods.dap.*;

import dods.dap.parser.ParseException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * This is the suite for testing LAS installations
 * @author Jing Yang Li
 *
 */
public class LASDatasetTester{

    private LASConfig las_config;
    private LASDocument las_operationsV7;
    private LASDocument las_ui;
    private LASDocument las_options;
    private LASTestOptions lto;
    
    public LASDatasetTester(LASConfig config, LASTestOptions l){
        las_config = config;
        lto = l;
    }

    /**
     * Test datasets are alive or not
     *
     */
    public void testDataset(){
        String dsURL;
        String dsID;
        String varID;
        String varpath;

        ArrayList<Dataset> datasets = new ArrayList<Dataset>();
        ArrayList<Variable> variables = new ArrayList<Variable>();

        try{
            //get datasets
            Element datasetsE = las_config.getDatasetsAsElement();
            List datasetElements = datasetsE.getChildren("dataset");


            //loop over each dataset
            for(Iterator dsIt = datasetElements.iterator(); dsIt.hasNext();){
                Element datasetE = (Element) dsIt.next();
                dsID = datasetE.getAttributeValue("ID");

                //get first variable of this dataset
                variables = las_config.getVariables(dsID);
                varID = variables.get(0).getID();

                //build XPath for this variable
                varpath = "/lasdata/datasets/dataset[@ID='"+dsID+"']/variables/variable[@ID='"+varID+"']";

                //get the data URL
                dsURL = las_config.getFullDataObjectURL(varpath);

                //if url is in format of ....xyz.nc#var
                if(dsURL.contains("#")){
                    String[] tmp = dsURL.split("#");
                    dsURL = tmp[0];
                }

                //get dds for a remote dataset
                if(dsURL != null && dsURL.contains("http")){
                	String ds = lto.getDataset();
                	if( (ds == null) || ((ds != null)&&(dsURL.contains(ds))) ){                
                        getDDS(dsURL);
                	}
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Get URL of product server, which is specfied in las.xml
     * @param las_config JDOM object LASConfig for this installation
     *
     */
    public String getProductServerURL(LASConfig las_config){
        String productServerURL = null;
        Element operations =  las_config.getRootElement().getChild("operations");
        productServerURL = operations.getAttributeValue("url");
        return productServerURL;
    }

    /**
     * Get DDS for a dataset and print it on console
     * @param url URL of the dataset
     *
     */
    public void getDDS(String url) throws Exception{
        //DConnect dc = new DConnect(url);
        //DDS mydds = dc.getDDS();

        boolean printDDS = lto.showDDS();

        System.out.println("---- Check dataset: " + url);
        try{
            DConnect dc = new DConnect(url);
            DDS mydds = dc.getDDS();

            if(printDDS){
                mydds.print(System.out);
            }else{
                System.out.println("OK!");
            }

        }catch (MalformedURLException e){
            //java.net.MalformedURLException - if the URL given to the constructor has an error
            System.out.println("the URL given to the constructor has an error");
            //e.printStackTrace();
            System.out.println(e.getMessage());

        }catch (IOException e){
            //java.io.IOException - if an error connecting to the remote server
            System.out.println("an error occurs when connecting to the remote server");
            System.out.println(e.getMessage());

        }catch (ParseException e){
            //dods.dap.parser.ParseException - if the DDS parser returned an error
            System.out.println("the DDS parser returned an error");
            System.out.println(e.getMessage());

        }catch (DDSException e){
            //dods.dap.DDSException - on an error constructing the DDS
            System.out.println("an error occurs when constructing the DDS");
            System.out.println(e.getErrorMessage());

        }catch (DODSException e){
            //dods.dap.DODSException - if an error returned by the remote server
            System.out.println("an error returned by the remote server");
            System.out.println(e.getErrorMessage());
        }
    }
}
