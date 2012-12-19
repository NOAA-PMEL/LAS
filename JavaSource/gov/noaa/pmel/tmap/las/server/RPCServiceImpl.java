package gov.noaa.pmel.tmap.las.server;


import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.jdom.LASDocument;
import gov.noaa.pmel.tmap.las.client.lastest.TestConstants;
import gov.noaa.pmel.tmap.las.client.rpc.RPCException;
import gov.noaa.pmel.tmap.las.client.rpc.RPCService;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ESGFDatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.FacetMember;
import gov.noaa.pmel.tmap.las.client.serializable.FacetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OptionSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.RegionSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.TestSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import gov.noaa.pmel.tmap.las.client.util.Util;
import gov.noaa.pmel.tmap.las.confluence.Confluence;
import gov.noaa.pmel.tmap.las.jdom.ESGFSearchDocument;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASJDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASTestResults;
import gov.noaa.pmel.tmap.las.jdom.ServerConfig;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.ui.LASProxy;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Constants;
import gov.noaa.pmel.tmap.las.util.ContainerComparator;
import gov.noaa.pmel.tmap.las.util.Dataset;
import gov.noaa.pmel.tmap.las.util.Grid;
import gov.noaa.pmel.tmap.las.util.Operation;
import gov.noaa.pmel.tmap.las.util.Option;
import gov.noaa.pmel.tmap.las.util.Region;
import gov.noaa.pmel.tmap.las.util.Tributary;
import gov.noaa.pmel.tmap.las.util.Variable;
import gov.noaa.pmel.tmap.las.util.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class RPCServiceImpl extends RemoteServiceServlet implements RPCService {
	private static final LASProxy lasProxy = new LASProxy();
	

	/**
	 * @return
	 * @throws RPCException
	 */
	LASConfig getLASConfig() throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(
				LASConfigPlugIn.LAS_CONFIG_KEY);
		if (lasConfig == null) {
			throw new RPCException(LASConfigPlugIn.LAS_CONFIG_NOTFOUND_MESSAGE);
		}
		return lasConfig;
	}
	ServerConfig getServerConfig() throws RPCException {
	    ServerConfig serverConfig = (ServerConfig) getServletContext().getAttribute(LASConfigPlugIn.SERVER_CONFIG_KEY);
	    if ( serverConfig == null ) {
	        throw new RPCException(LASConfigPlugIn.LAS_CONFIG_NOTFOUND_MESSAGE);
	    }
	    return serverConfig;
	}

	public HashMap<String, String> getPropertyGroup(String name) throws RPCException {
        LASConfig lasConfig = getLASConfig(); 
        HashMap<String, String> property_group;
        try {
            property_group = lasConfig.getGlobalPropertyGroupAsHashMap(name);
        } catch (LASException e) {
            throw new RPCException(e.getLocalizedMessage());
        }
        return property_group;
    }

    public TestSerializable[] getTestResults(String test_key) throws RPCException {
        LASTestResults testResults = new LASTestResults();
        LASConfig lasConfig = getLASConfig();
        String test_output_file = lasConfig.getOutputDir()+File.separator+TestConstants.TEST_RESULTS_FILE;
        File c = new File(test_output_file);
        if ( c.exists() ) {
            try {
                JDOMUtils.XML2JDOM(new File(test_output_file), testResults);
            } catch (IOException e) {
                throw new RPCException(e.getMessage());
            } catch (JDOMException e) {
                throw new RPCException(e.getMessage());
            }
        }
        if ( test_key == null ) {
            return testResults.getTests();
        } else {
            TestSerializable[] tests = new TestSerializable[1];
            tests[0] = testResults.getTest(test_key);
            return tests;
        }
    }
    /**
     * Get the dsid and varid based on an opendap URL.  Will not work with sister servers!!
     * @param data_url
     * @return
     * @throws RPCException 
     */
    public Map<String, String> getIDMap(String data_url) throws RPCException {
        LASConfig lasConfig = getLASConfig(); 
        try {
            return lasConfig.getIDMap(data_url);
        } catch ( JDOMException e ) {
            throw new RPCException(e.getMessage());
        } catch ( LASException e ) {
            throw new RPCException(e.getMessage());
        }
    }
    /**
     * @throws RPCException 
     * 
     */
    public CategorySerializable[] getTimeSeries() throws RPCException {
        LASConfig lasConfig = getLASConfig(); 
        

        CategorySerializable[] cats = null;
        // TODO Do we need a confluence implementation of this?
        
            try {
                ArrayList<Category> categories = lasConfig.getTimeSeriesDatasets();
                cats = lasConfig.getCategorySerializable(categories);
            } catch (LASException e) {
                throw new RPCException(e.getMessage());
            } catch (JDOMException e) {
                throw new RPCException(e.getMessage());
            }
        
        return cats;
    }
    public ConfigSerializable getConfig(String view, String[] xpaths) throws RPCException {
        ConfigSerializable wire_config = new ConfigSerializable();
        String dsid = Util.getDSID(xpaths[0]);
        String varid = Util.getVarID(xpaths[0]);
        GridSerializable wire_grid = getGridSerializable(dsid, varid);
        wire_config.setGrid(wire_grid);
        OperationSerializable[] wire_operations = getOperations(view, xpaths);
        wire_config.setOperations(wire_operations);
        RegionSerializable[] wire_regions = getRegionsSerializable(dsid, varid);
        wire_config.setRegions(wire_regions);
        return wire_config;
    }
    public ConfigSerializable getConfig(String view, String dsid, String varid) throws RPCException {
        ConfigSerializable wire_config = new ConfigSerializable();
        GridSerializable wire_grid = getGridSerializable(dsid, varid);
        wire_config.setGrid(wire_grid);
        OperationSerializable[] wire_operations = getOperationsSerialziable(view, dsid, varid);
        wire_config.setOperations(wire_operations);
        RegionSerializable[] wire_regions = getRegionsSerializable(dsid, varid);
        wire_config.setRegions(wire_regions);
        return wire_config;
    }
    public VariableSerializable getVariable(String dsid, String varid ) throws RPCException {
        LASConfig lasConfig = getLASConfig();
        Variable variable = null;
        try {
            Dataset dataset = null;
            if ( !dsid.contains(Constants.NAME_SPACE_SPARATOR) || lasConfig.isLocal(dsid) ) {
                dataset = lasConfig.getDataset(dsid);
                if ( dataset != null ) {
                    variable = dataset.getVariable(varid);
                } else {
                    throw new RPCException("Cannot find data set for this id: "+dsid);
                }
            } else {
                String[] parts = dsid.split(Constants.NAME_SPACE_SPARATOR);
                String server_key = null;
                if ( parts != null ) {
                    server_key = parts[0];
                    if ( server_key != null ) {
                        Tributary trib = lasConfig.getTributary(server_key);
                        String las_url = trib.getURL() + Constants.GET_VARIABLE + "?format=xml&dsid="+dsid+"&varid="+varid;
                        String varxml = lasProxy.executeGetMethodAndReturnResult(las_url);
                        LASDocument vardoc = new LASDocument();
                        JDOMUtils.XML2JDOM(varxml, vardoc);
                        variable = new Variable(vardoc.getRootElement(), dsid);
                        variable.setDSName(variable.getAttributeValue("dsname"));
                        variable.setDSID(variable.getAttributeValue("dsid"));
                    }
                }
            }       
        } catch (JDOMException e) {
            throw new RPCException(e.getMessage());
        } catch (LASException e) {
            throw new RPCException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RPCException(e.getMessage());
        } catch (HttpException e) {
            throw new RPCException(e.getMessage());
        } catch (IOException e) {
            throw new RPCException(e.getMessage());
        }
        if ( variable != null ) {
            return variable.getVariableSerializable();
        } else {
            throw new RPCException("Variable not found.");
        }
    }
    public DatasetSerializable getFullDataset(String id) throws RPCException {
        LASConfig lasConfig = getLASConfig();
        try {
            return lasConfig.getFullDataset(id).getDatasetSerializable();
        } catch (JDOMException e) {
            throw new RPCException(e.getMessage());
        } catch (LASException e) {
            throw new RPCException(e.getMessage());
        }
    }

	public CategorySerializable[] getCategories(String id) throws RPCException {
		LASConfig lasConfig = getLASConfig();
        
        ArrayList<Category> categories = new ArrayList<Category>();
        if ( lasConfig.allowsSisters() ) {
            try {
                if ( id == null ) {
                    if ( lasConfig.pruneCategories() ) {
                        HttpServletRequest request = this.getThreadLocalRequest();
                        HttpSession session = request.getSession();
                        String [] catids = (String[]) request.getSession().getAttribute("catid");
                        if ( catids != null ) {
                            for ( int i = 0; i < catids.length; i++ ) {
                                categories.addAll(lasConfig.getCategories(catids[i]));
                            }
                        }
                    } else {
                        Category local_cat = new Category(lasConfig.getTitle(), lasConfig.getTopLevelCategoryID()); 
                        // Do the local top level category
                        ArrayList<Category> local_cats = lasConfig.getCategories(id);
                        for (Iterator catIt = local_cats.iterator(); catIt.hasNext();) {
                            Category category = (Category) catIt.next();
                            local_cat.addCategory(category);
                        }
                        categories.add(local_cat);
                        // Do the remote categories...

                        ArrayList<Tributary> tributaries = lasConfig.getTributaries();
                        for (Iterator tribIt = tributaries.iterator(); tribIt.hasNext();) {
                            Tributary tributary = (Tributary) tribIt.next();
                            Category server_cat = new Category(tributary.getName(), tributary.getTopLevelCategoryID());
                            server_cat.setAttribute("remote_las", tributary.getURL()+Constants.GET_AUTH);
                            String url = tributary.getURL() + Constants.GET_CATEGORIES + "?format=xml";
                            String catxml = lasProxy.executeGetMethodAndReturnResult(url);
                            ArrayList<Category> trib_cats = LASJDOMUtils.getCategories(catxml);
                            for (Iterator tribCatsIt = trib_cats.iterator(); tribCatsIt.hasNext();) {
                                Category category = (Category) tribCatsIt.next();
                                server_cat.addCategory(category);
                            }
                            categories.add(server_cat);
                        }

                    }

                } else {
                    if ( lasConfig.pruneCategories() ) {
                        // This is the ESGF case... 
                        
                        categories = lasConfig.getCategories(id);
                        
                    } else {
                        // This is the non-ESGF case...
                        if ( !id.contains(Constants.NAME_SPACE_SPARATOR) || lasConfig.isLocal(id) ) {
                            // Handle the case where we're getting the local top level catagories
                            if ( id.equals(lasConfig.getTopLevelCategoryID()) ) id=null;
                            categories = lasConfig.getCategories(id);
                        } else {
                            String[] parts = id.split(Constants.NAME_SPACE_SPARATOR);
                            String server_key = null;
                            if ( parts != null ) {
                                server_key = parts[0];
                                if ( server_key != null ) {
                                    Tributary trib = lasConfig.getTributary(server_key);
                                    String las_url = trib.getURL() + Constants.GET_CATEGORIES + "?format=xml&catid="+id;
                                    String catxml = lasProxy.executeGetMethodAndReturnResult(las_url);
                                    ArrayList<Category> trib_cats = LASJDOMUtils.getCategories(catxml);
                                    for (Iterator tribCatsIt = trib_cats.iterator(); tribCatsIt.hasNext();) {
                                        Category category = (Category) tribCatsIt.next();
                                        categories.add(category);
                                    }
                                }
                            }
                        }
                    }

                }
            } catch (JDOMException e) {
                throw new RPCException(e.getMessage());
            } catch (LASException e) {
                throw new RPCException(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                throw new RPCException(e.getMessage());
            } catch (HttpException e) {
                throw new RPCException(e.getMessage());
            } catch (IOException e) {
                throw new RPCException(e.getMessage());
            }
        } else {
            try {
                categories = lasConfig.getCategories(id);
            } catch (JDOMException e) {
                throw new RPCException(e.getMessage());
            } catch (LASException e) {
                throw new RPCException(e.getMessage());
            }
        }
        
        Collections.sort(categories, new ContainerComparator("name"));

        
        CategorySerializable[] cats = new CategorySerializable[categories.size()];

        for (int i = 0; i < cats.length; i++) {
            try {
                cats[i] = categories.get(i).getCategorySerializable();
            } catch (LASException e) {
                throw new RPCException(e.getMessage());
            }
        }
        for ( int i = 0; i < cats.length; i++ ) {
            if ( cats[i].isVariableChildren() ) {
                cats[i].sortVariables();
            }
        }
        return cats;    
    }
//  public Map<String, String> getEnembleMembers(String[] dsID) throws RPCException {
//      LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
//      Map<String, String> members = null;
//      if ( lasConfig.allowsSisters() ) {
//          // Group the dsIDS by the node where they reside.
//          
//          // Get the local names.
//          
//          // Fire a request to each host to get the names that belong on the remote host
//          
//          // Collect the lot into a single Map and return the result
//      } else {
//          try {
//              members = lasConfig.getEnsembleMembers(dsID);
//          } catch (JDOMException e) {
//              throw new RPCException(e.getMessage());
//          } catch (LASException e) {
//              throw new RPCException(e.getMessage());
//          }
//      }
//      return members;
//  }
    public GridSerializable getGrid(String dsID, String varID) throws RPCException {
        return getGridSerializable(dsID, varID);
    }
    private GridSerializable getGridSerializable(String dsID, String varID) throws RPCException {
        LASConfig lasConfig = getLASConfig();
        Grid grid = null;
        
            try {
                grid = lasConfig.getGrid(dsID, varID);
            } catch (JDOMException e) {
                throw new RPCException(e.getMessage());
            } catch (LASException e) {
                throw new RPCException(e.getMessage());
            }
        
        if ( grid != null ) {
            return grid.getGridSerializable();
        } else {
            return null;
        }
    }
    public OperationSerializable[] getOperations(String view, String[] xpath ) throws RPCException {
        if ( xpath.length == 1 ) {
            String dsid = LASConfig.getDSIDfromXPath(xpath[0]);
            String varid = LASConfig.getVarIDfromXPath(xpath[0]);
            return getOperations(view, dsid, varid);
        }
        LASConfig lasConfig = getLASConfig();
        List<OperationSerializable> multi_variable_operations = new ArrayList<OperationSerializable>();
        if ( lasConfig.allowsSisters() ) {

            Map<String, OperationSerializable> operations = new HashMap<String, OperationSerializable>();
            for (int i = 0; i < xpath.length; i++) {
                String dsid = LASConfig.getDSIDfromXPath(xpath[i]);
                String varid = LASConfig.getVarIDfromXPath(xpath[i]);
                OperationSerializable[] ops = getOperations(view, dsid, varid);
                for (int j = 0; j < ops.length; j++) {
                    operations.put(ops[j].getID(), ops[j]);
                }
            }
            int var_count = xpath.length;
            int minvars = -1;
            int maxvars = -1;
            
            for (Iterator opIt = operations.keySet().iterator(); opIt.hasNext();) {
                String opKey = (String) opIt.next();
                OperationSerializable op = operations.get(opKey);
                String min = null;
                String max = null;
                if ( op.getAttributes() != null ) {
                    min = op.getAttributes().get("minvars");
                    max = op.getAttributes().get("maxvars");
                }
                if ( min != null && !min.equals("") ) {
                    try {
                        minvars = Integer.valueOf(min).intValue();
                    } catch (Exception e) {
                        throw new RPCException("Cannot parse the minvars attribute value.");
                    }
                }
                if ( max != null && !max.equals("") ) {
                    try {
                        maxvars = Integer.valueOf(max).intValue();
                    } catch (Exception e) {
                        throw new RPCException("Cannot parse the maxvars attribute value.");
                    }
                }
                if ( minvars > 0 ) {
                    if ( maxvars > 0 ) {
                        if ( var_count >= minvars && var_count <= maxvars ) {
                            multi_variable_operations.add(op);
                            minvars = -1;
                            maxvars = -1;
                        }
                    } else {
                        if ( var_count >= minvars ) {
                            multi_variable_operations.add(op);
                            minvars = -1;
                            maxvars = -1;
                        }
                    }
                }
            }
        } else {
            try {

                List<Operation> os = lasConfig.getOperations(view, xpath);
                for ( Iterator iterator = os.iterator(); iterator.hasNext(); ) {
                    Operation operation = (Operation) iterator.next();
                    multi_variable_operations.add(operation.getOperationSerializable());
                }
            } catch ( LASException e ) {
               throw new RPCException(e.getMessage());
            } catch ( JDOMException e ) {
               throw new RPCException(e.getMessage());
            }
        }

        int o = 0;
        OperationSerializable[] returnOps = new OperationSerializable[multi_variable_operations.size()];
        for (Iterator opIt = multi_variable_operations.iterator(); opIt.hasNext();) {
            
            OperationSerializable op = (OperationSerializable) opIt.next();
            returnOps[o] = op;
            o++;
        }
        return returnOps;
    }
    public OperationSerializable[] getOperations(String view, String dsID, String varID) throws RPCException {
        return getOperationsSerialziable(view, dsID, varID);
    }
    private OperationSerializable[] getOperationsSerialziable(String view, String dsID, String varID) throws RPCException {
    	LASConfig lasConfig = getLASConfig();

    	// Remove the climate analysis if it is not an ESGF session.
    	HttpServletRequest request = this.getThreadLocalRequest();
    	String [] catids = (String[]) request.getSession().getAttribute("catid");
    	boolean include_climate_analysis = false;
    	if ( catids != null && catids.length > 0 ) {
    		include_climate_analysis = true;
    	}

    	ArrayList<Operation> operations = new ArrayList<Operation>();
    	OperationSerializable[] wireOps = null;
    	try {


    		if ( dsID != null ) {

    			operations = lasConfig.getOperations(view, dsID, varID);

    			if ( operations.size() <= 0 ) {
    				throw new RPCException("No operations found.");
    			} else {
    				// Check to see if there's something in there.
    				Operation op = operations.get(0);
    				String id = op.getID();
    				if ( id == null || id.equals("") ) {
    					throw new RPCException("No operations found.");
    				}
    			}

    		}
    	} catch (JDOMException e) {
    		throw new RPCException(e.getMessage());
    	} catch (RPCException e) {
    		throw new RPCException(e.getMessage());
    	}
    	if ( !include_climate_analysis ) {
    		Operation remove = null;
    		for (Iterator opsIt = operations.iterator(); opsIt.hasNext();) {
    			Operation op = (Operation) opsIt.next();
    			if ( op.getID().toLowerCase().contains("climate_analysis") ) {
    				remove = op;
    			}
    		}
    		if ( remove != null ) {
    			operations.remove(remove);
    		}
    	}
    	int k=0;
    	wireOps = new OperationSerializable[operations.size()];
    	for (Iterator opsIt = operations.iterator(); opsIt.hasNext();) {
    		Operation op = (Operation) opsIt.next();
    		wireOps[k] = op.getOperationSerializable();
    		k++;
    	}
    	return wireOps;
    }
    public RegionSerializable[] getRegions(String dsID, String varID) throws RPCException {
        return getRegionsSerializable(dsID, varID);
    }
    private RegionSerializable[] getRegionsSerializable(String dsID, String varID) throws RPCException {
        LASConfig lasConfig = getLASConfig();
        ArrayList<Region> regions = new ArrayList<Region>();
        RegionSerializable[] wire_regions = null;
        if ( lasConfig.allowsSisters() ) {
            try {
                if ( dsID != null ) {
                    regions = lasConfig.getRegions(dsID, varID);
                    
                }
            } catch(Exception e) {
                throw new RPCException(e.getMessage());
            }
        } else {
            try {
                // Get the regions according to data set and variable.
                regions = lasConfig.getRegions(dsID, varID);
            } catch ( JDOMException e ) {
                throw new RPCException(e.getMessage());
            }
        }
        if ( regions.size() <= 0 ) {
            // TODO throw this.  In the meantime it's ok to send back an empty list since we're not using it on the client.
            //throw new RPCException("No regions found.");
        }
        wire_regions = new RegionSerializable[regions.size()];
        for( int i = 0; i < regions.size(); i++ ) {
            wire_regions[i] = regions.get(i).getRegionSerializable();
        }
        return wire_regions;
    }
    /**
     * (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.client.rpc.RPCService#getOptions(java.lang.String)
     * 
     */
    public OptionSerializable[] getOptions(String opid) throws RPCException {
        LASConfig lasConfig = getLASConfig();
        // The id is actually the name in the options section of the XML, but it's unique and functions as an id...
        ArrayList<Option> options = new ArrayList<Option>();
        OptionSerializable[] wireOptions;
        if ( lasConfig.allowsSisters() ) {
            try {
                if ( opid != null ) {

                    if ( !opid.contains(Constants.NAME_SPACE_SPARATOR) || lasConfig.isLocal(opid) ) {
                        if ( opid.contains(Constants.NAME_SPACE_SPARATOR) ) {
                            String[] parts = opid.split(Constants.NAME_SPACE_SPARATOR);
                            opid = parts[1];
                        }
                        options = lasConfig.getOptions(opid);
                    } else {
                        String[] parts = opid.split(Constants.NAME_SPACE_SPARATOR);
                        String server_key = null;
                        if ( parts != null ) {
                            server_key = parts[0];
                            if ( server_key != null ) {
                                Tributary trib = lasConfig.getTributary(server_key);    
                                // The server key is in the operations definition, we need to get it from the options without the server key.
                                opid = parts[1];
                                String las_url = trib.getURL() + Constants.GET_OPTIONS + "?format=xml&opid=" + opid;
                                String options_xml = lasProxy.executeGetMethodAndReturnResult(las_url);
                                LASDocument optionsdoc = new LASDocument();
                                JDOMUtils.XML2JDOM(options_xml, optionsdoc);
                                List opElements = optionsdoc.getRootElement().getChildren("option");
                                for (Iterator opIt = opElements.iterator(); opIt.hasNext();) {
                                    Element opElement = (Element) opIt.next();
                                    Option option = new Option((Element)opElement.clone());
                                    options.add(option);
                                }
                            }
                        }
                    }
                }
            } catch (JDOMException e) {
                throw new RPCException(e.getMessage());
            } catch (HttpException e) {
                throw new RPCException(e.getMessage());
            } catch (IOException e) {
                throw new RPCException(e.getMessage());
            } 
        } else {
            try {
                options = lasConfig.getOptions(opid);
            } catch (JDOMException e) {
                throw new RPCException(e.getMessage());
            }
        }
        int i=0;
        if ( options != null ) {
            wireOptions = new OptionSerializable[options.size()];
            for (Iterator optionIt = options.iterator(); optionIt.hasNext();) {
                Option option = (Option) optionIt.next();
                wireOptions[i] = option.getOptionSerializable();
                i++;
            }
            return wireOptions;
        } else {
            return null;
        }
    }
    /*
     * Everything from here down is for the ESGF search interface.
     */
    /**
     * Get ESGF Facets
     * Returns a list of ESGF search facet objects
     */
    public List<FacetSerializable> getFacets() throws RPCException {
        try {
            List<FacetSerializable> facets = new ArrayList<FacetSerializable>();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            lasProxy.executeGetMethodAndStreamResult(Constants.SEARCH_URL+"?limit=0&facets=project,institute,model,submodel,instrument,experiment_family,experiment,subexperiment,time_frequency,product,realm,variable,variable_long_name,cmip_table,cf_standard_name,ensemble,data_node", stream);
            ESGFSearchDocument doc = new ESGFSearchDocument();
            JDOMUtils.XML2JDOM(stream.toString(), doc);
            if ( doc.getStatus() == 0 ) {
                facets = doc.getFacets();
            }
            return facets;
        } catch ( HttpException e ) {
            throw new RPCException(e.getMessage());
        } catch ( IOException e ) {
            throw new RPCException(e.getMessage());
        } catch ( JDOMException e ) {
            throw new RPCException(e.getMessage());
        }
    }
    /**
     * Return ESGF data sets matching a particular query string.
     * @see <a href="http://www.esgf.org/wiki/ESGF_Search_REST_API">http://www.esgf.org/wiki/ESGF_Search_REST_API</a>
     */
    public List<ESGFDatasetSerializable> getESGFDatasets(String query) throws RPCException {
        List<ESGFDatasetSerializable> datasets = new ArrayList<ESGFDatasetSerializable>();
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            lasProxy.executeGetMethodAndStreamResult(Constants.SEARCH_URL+"?"+query, stream);
            ESGFSearchDocument doc = new ESGFSearchDocument();
            JDOMUtils.XML2JDOM(stream.toString(), doc);
            if ( doc.getStatus() == 0 ) {
                datasets = doc.getDatasets();
            }
        } catch ( HttpException e ) {
            throw new RPCException(e.getMessage());
        } catch ( IOException e ) {
            throw new RPCException(e.getMessage());
        } catch ( JDOMException e ) {
            throw new RPCException(e.getMessage());
        }
        return datasets;
    }
    public String addESGFDataset(String id) throws RPCException {
        LASConfig lasConfig = getLASConfig();
        ServerConfig serverConfig = getServerConfig();
        String keyid = null;
        try {
            keyid = lasConfig.addDataset(id);
            lasConfig.convertToSeven(true);
            lasConfig.mergeProperites();
            lasConfig.addIntervalsAndPoints();    
            lasConfig.addGridType();
            String fds_base = serverConfig.getFTDSBase();
            String fds_dir = serverConfig.getFTDSDir();
            lasConfig.addFDS(fds_base, fds_dir);
            
            HttpServletRequest request = this.getThreadLocalRequest();
            String [] catids = (String[]) request.getSession().getAttribute("catid");
            boolean add = true;          
            if ( catids != null ) {
                for ( int i = 0; i < catids.length; i++ ) {
                    if ( keyid.equals(catids[i]) ) {
                        add = false;
                    }
                }
                if ( add ) {
                    String[] c = new String[catids.length+1];
                    for ( int i = 0; i < catids.length; i++ ) {
                        c[i] = catids[i];
                    }
                    c[catids.length] = keyid;
                    request.getSession().setAttribute("catid", c);
                }
            } else {
                String[] c = new String[1];
                c[0] = keyid;
                request.getSession().setAttribute("catid", c);
            }
            return keyid;
        } catch ( HttpException e ) {
            throw new RPCException(e.getMessage());
        } catch ( JDOMException e ) {
            throw new RPCException(e.getMessage());
        } catch ( IOException e ) {
            throw new RPCException(e.getMessage());
        } catch ( LASException e ) {
            throw new RPCException(e.getMessage());
        }       
    }
    /**
     * @deprecated
     */
    public List<String> getTributaryServers() {
        List<String> wiretribs = new ArrayList<String>();
        LASConfig lasConfig;
		try {
			lasConfig = getLASConfig();
	        ArrayList<Tributary> tribs = lasConfig.getTributaries();
	        for (Iterator<Tributary> tribIt = tribs.iterator(); tribIt.hasNext();) {
	            Tributary tributary = (Tributary) tribIt.next();
	            String url = tributary.getURL()+"/auth.do";
	            wiretribs.add(url);
	        }
	        return wiretribs;
		} catch (RPCException e) {
			// Ignoring the exception since this method is deprecated anyway
			e.printStackTrace();
		}
		return null;
    }
    @Override
    protected void checkPermutationStrongName() throws SecurityException {
        // we are going to completely forego this check...
    }
    
}
