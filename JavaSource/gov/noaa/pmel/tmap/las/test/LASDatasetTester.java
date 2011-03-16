package gov.noaa.pmel.tmap.las.test;


import gov.noaa.pmel.tmap.addxml.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASDocument;
import gov.noaa.pmel.tmap.las.ui.LASProxy;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Dataset;
import gov.noaa.pmel.tmap.las.util.Variable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import dods.dap.DConnect;
import dods.dap.DDS;
import dods.dap.DDSException;
import dods.dap.DODSException;
import dods.dap.parser.ParseException;

/**
 * This classes tests the remote OPeNDAP URLs used by this LAS server
 * and the F-TDS URLs provided by this LAS server
 *
 * @author Jing Yang Li
 * 
 */
public class LASDatasetTester{

    private LASTestOptions lto;
    private LASConfig lasConfig;
    private static LASProxy lasProxy = new LASProxy();
    
    public LASDatasetTester(LASConfig lasConfig, LASTestOptions l){
        this.lasConfig = lasConfig;
        lto = l;
    }

    /**
     * Test remote OPeNDAP URLs
     *
     */
    public void testDataset(){
        
        ArrayList<Category> categories = new ArrayList<Category>();
        ArrayList<Variable> variables = new ArrayList<Variable>();

        try{
            //get datasets
            categories = lasConfig.getDatasets(false);
            //loop over each dataset
            for(Iterator catIt = categories.iterator(); catIt.hasNext();) {
            	Category cat = (Category) catIt.next();
            	ArrayList<Dataset> datasets = cat.getAllDatasets();
            	if ( datasets != null ) {
            		for (Iterator dsIt = cat.getAllDatasets().iterator(); dsIt.hasNext();) {
            			Dataset dataset = (Dataset) dsIt.next();
            			Variable var = dataset.getVariables().get(0);

            			//get the data URL
            			String dsURL = lasConfig.getFullDataObjectURL(dataset.getID(), var.getID());

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
            	}

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test F-TDS URLs
     *
     */
    public void testFTDS(){
        String dsID;
        String varID;
        String varpath;

        ArrayList<Dataset> datasets = new ArrayList<Dataset>();
        ArrayList<Variable> variables = new ArrayList<Variable>();

        try{
            //get datasets
            Element datasetsE = lasConfig.getDatasetsAsElement();
            List datasetElements = datasetsE.getChildren("dataset");


            //loop over each dataset
            for(Iterator dsIt = datasetElements.iterator(); dsIt.hasNext();){
                Element datasetE = (Element) dsIt.next();
                dsID = datasetE.getAttributeValue("ID");

                //get first variable of this dataset
                variables = lasConfig.getVariables(dsID);
                varID = variables.get(0).getID();

                //build XPath for this variable
                varpath = "/lasdata/datasets/dataset[@ID='"+dsID+"']/variables/variable[@ID='"+varID+"']";

                //test FTDS URLs
                String ftdsURL= lasConfig.getDataAccessURL(varpath, true);
                
                if(ftdsURL != null && ! ftdsURL.equals("") && ftdsURL.contains("http")){ 
                    getDDS(ftdsURL);
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
            System.out.println("The URL given to the constructor has an error.");
            //e.printStackTrace();
            System.out.println(e.getMessage());

        }catch (IOException e){
            //java.io.IOException - if an error connecting to the remote server
            System.out.println("An error occurs when connecting to the remote server.");
            System.out.println(e.getMessage());

        }catch (ParseException e){
            //dods.dap.parser.ParseException - if the DDS parser returned an error
            System.out.println("The DDS parser returned an error.");
            System.out.println(e.getMessage());

        }catch (DDSException e){
            //dods.dap.DDSException - on an error constructing the DDS
            System.out.println("An error occurs when constructing the DDS.");
            System.out.println(e.getErrorMessage());

        }catch (DODSException e){
            //dods.dap.DODSException - if an error returned by the remote server
            System.out.println("An error was returned by the remote server.");
            System.out.println(e.getErrorMessage());
        }catch (Exception e){
            System.out.println("An error occurs when connecting to the data server.");
            e.printStackTrace();
        }

    }
}
