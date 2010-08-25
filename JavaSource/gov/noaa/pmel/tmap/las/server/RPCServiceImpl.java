package gov.noaa.pmel.tmap.las.server;


import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.client.RPCException;
import gov.noaa.pmel.tmap.las.client.RPCService;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OptionSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.RegionSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;
import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASDocument;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.httpclient.HttpException;
import org.jdom.Element;
import org.jdom.JDOMException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class RPCServiceImpl extends RemoteServiceServlet implements RPCService {
	private static final LASProxy lasProxy = new LASProxy();
	public HashMap<String, String> getPropertyGroup(String name) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);	
		HashMap<String, String> property_group;
		try {
			property_group = lasConfig.getGlobalPropertyGroupAsHashMap(name);
		} catch (LASException e) {
			throw new RPCException(e.getLocalizedMessage());
		}
		return property_group;
	}
	/**
	 * @throws RPCException 
	 * 
	 */
	public CategorySerializable[] getTimeSeries() throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);	
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
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
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
	public CategorySerializable[] getCategories(String id) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY); 
		ArrayList<Category> categories = new ArrayList<Category>();
		if ( lasConfig.allowsSisters() ) {
			try {
				if ( id == null ) {
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
						String url = tributary.getURL() + Constants.GET_CATEGORIES + "?format=xml";
						String catxml = lasProxy.executeGetMethodAndReturnResult(url);
						ArrayList<Category> trib_cats = JDOMUtils.getCategories(catxml);
						for (Iterator tribCatsIt = trib_cats.iterator(); tribCatsIt.hasNext();) {
							Category category = (Category) tribCatsIt.next();
							server_cat.addCategory(category);
						}
						categories.add(server_cat);
					}

					Collections.sort(categories, new ContainerComparator("name"));

				} else {
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
								ArrayList<Category> trib_cats = JDOMUtils.getCategories(catxml);
								for (Iterator tribCatsIt = trib_cats.iterator(); tribCatsIt.hasNext();) {
									Category category = (Category) tribCatsIt.next();
									categories.add(category);
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
		CategorySerializable[] cats = new CategorySerializable[categories.size()];

		for (int i = 0; i < cats.length; i++) {
			try {
				cats[i] = categories.get(i).getCategorySerializable();
			} catch (LASException e) {
				throw new RPCException(e.getMessage());
			}
		}
		return cats;	
	}
	public GridSerializable getGrid(String dsID, String varID) throws RPCException {
		return getGridSerializable(dsID, varID);
	}
	private GridSerializable getGridSerializable(String dsID, String varID) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
		Grid grid = null;
		if ( lasConfig.allowsSisters() ) {
			try {
				if ( dsID != null ) {
					if ( !dsID.contains(Constants.NAME_SPACE_SPARATOR) || lasConfig.isLocal(dsID) ) {
						grid = lasConfig.getGrid(dsID, varID);
					} else {
						String[] parts = dsID.split(Constants.NAME_SPACE_SPARATOR);
						String server_key = null;
						if ( parts != null ) {
							server_key = parts[0];
							if ( server_key != null ) {
								Tributary trib = lasConfig.getTributary(server_key);
								String las_url = trib.getURL() + Constants.GET_GRID+"?format=xml&dsid="+dsID+"&varid="+varID;
								String grid_xml = lasProxy.executeGetMethodAndReturnResult(las_url);
								LASDocument griddoc = new LASDocument();
								JDOMUtils.XML2JDOM(grid_xml, griddoc);
								grid = new Grid((Element)griddoc.getRootElement().clone());
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
			} catch (IOException e) {
				throw new RPCException(e.getMessage());
			}
		} else {
			try {
				grid = lasConfig.getGrid(dsID, varID);
			} catch (JDOMException e) {
				throw new RPCException(e.getMessage());
			} catch (LASException e) {
				throw new RPCException(e.getMessage());
			}
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
		// For now require them to all be from the same data set.
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
		List<OperationSerializable> multi_variable_operations = new ArrayList<OperationSerializable>();
		for (int o = 0; o < operations.size(); o++) {
			OperationSerializable op = operations.get(o);
			String min = op.getAttributes().get("minvars");
			String max = op.getAttributes().get("maxvars");
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
		return (OperationSerializable[]) multi_variable_operations.toArray();
	}
	public OperationSerializable[] getOperations(String view, String dsID, String varID) throws RPCException {
		return getOperationsSerialziable(view, dsID, varID);
	}
	private OperationSerializable[] getOperationsSerialziable(String view, String dsID, String varID) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
		ArrayList<Operation> operations = new ArrayList<Operation>();
		OperationSerializable[] wireOps = null;
		if ( lasConfig.allowsSisters() ) {
			try {


				if ( dsID != null ) {
					if ( !dsID.contains(Constants.NAME_SPACE_SPARATOR) || lasConfig.isLocal(dsID) ) {
						if ( view != null) {	
							operations = lasConfig.getOperations(view, dsID, varID);
						} else {
							ArrayList<View> views = lasConfig.getViewsByDatasetAndVariable(dsID, varID);
							HashMap<String, Operation> allOps = new HashMap<String, Operation>();
							for (Iterator viewIt = views.iterator(); viewIt.hasNext();) {
								View aView = (View) viewIt.next();
								String[] xpaths = new String[] {Util.getVariableXPATH(dsID, varID)};
								ArrayList<Operation> ops = lasConfig.getOperations(aView.getValue(), xpaths);
								for (Iterator opsIt = ops.iterator(); opsIt.hasNext();) {
									Operation op = (Operation) opsIt.next();
									String id = op.getID();
									allOps.put(id, op);
								}
							}
							for (Iterator idIt = allOps.keySet().iterator(); idIt.hasNext();) {
								String id = (String) idIt.next();
								operations.add(allOps.get(id));
							}
						}
					} else {
						String[] parts = dsID.split(Constants.NAME_SPACE_SPARATOR);
						String server_key = null;
						if ( parts != null ) {
							server_key = parts[0];
							if ( server_key != null ) {
								Tributary trib = lasConfig.getTributary(server_key);
								String xpath = Util.getVariableXPATH(dsID, varID);
								String las_url = trib.getURL() + Constants.GET_OPERATIONS + "?format=xml&xpath=" + xpath;
								if ( view != null) {	
									las_url = las_url + "&view="+view;
								} 
								String op_xml = lasProxy.executeGetMethodAndReturnResult(las_url);
								LASDocument opdoc = new LASDocument();
								JDOMUtils.XML2JDOM(op_xml, opdoc);
								Element opsElement = opdoc.getRootElement();
								List ops = opsElement.getChildren("operation");
								for (Iterator opIt = ops.iterator(); opIt.hasNext();) {
									Element op = (Element) opIt.next();
									Operation operation = new Operation(op);
									operations.add(operation);
								}									
							}
						}
					}
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
				String[] xpaths = new String[] {Util.getVariableXPATH(dsID, varID)};
				if ( view != null) {						
					operations = lasConfig.getOperations(view, xpaths);
				} else {
					ArrayList<View> views = lasConfig.getViewsByDatasetAndVariable(dsID, varID);
					HashMap<String, Operation> allOps = new HashMap<String, Operation>();					
					for (Iterator viewIt = views.iterator(); viewIt.hasNext();) {
						View aView = (View) viewIt.next();
						ArrayList<Operation> ops = lasConfig.getOperations(aView.getValue(), xpaths);
						for (Iterator opsIt = ops.iterator(); opsIt.hasNext();) {
							Operation op = (Operation) opsIt.next();
							String id = op.getID();
							allOps.put(id, op);
						}
					}
					for (Iterator idIt = allOps.keySet().iterator(); idIt.hasNext();) {
						String id = (String) idIt.next();
						operations.add(allOps.get(id));
					}
				}
			} catch (JDOMException e) {
				throw new RPCException(e.getMessage());
			} catch (LASException e) {
				throw new RPCException(e.getMessage());
			}
		}
		wireOps = new OperationSerializable[operations.size()];
		int k=0;
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
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
		ArrayList<Region> regions = new ArrayList<Region>();
		RegionSerializable[] wire_regions = null;
		if ( lasConfig.allowsSisters() ) {
			try {
				if ( dsID != null ) {
					if ( !dsID.contains(Constants.NAME_SPACE_SPARATOR) || lasConfig.isLocal(dsID) ) {
					    regions = lasConfig.getRegions(dsID, varID);
					} else {
						String[] parts = dsID.split(Constants.NAME_SPACE_SPARATOR);
						String server_key = null;
						if ( parts != null ) {
							server_key = parts[0];
							if ( server_key != null ) {
								Tributary trib = lasConfig.getTributary(server_key);
								String las_url = trib.getURL() + Constants.GET_REGIONS + "?format=xml&dsid="+dsID+"&varid="+varID;
								String regionsxml = lasProxy.executeGetMethodAndReturnResult(las_url);
								LASDocument regionsdoc = new LASDocument();
								JDOMUtils.XML2JDOM(regionsxml, regionsdoc);
								Element regionsElement = regionsdoc.getRootElement();
								List regionsList = regionsElement.getChildren("region");
								for (Iterator regionIt = regionsList.iterator(); regionIt.hasNext();) {
									Element region = (Element) regionIt.next();
									Region r = new Region(region);
									regions.add(r);
								}
							}
						}
					}
				}
			} catch(Exception e) {
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
	 * @see gov.noaa.pmel.tmap.las.client.RPCService#getOptions(java.lang.String)
	 * 
	 */
	public OptionSerializable[] getOptions(String opid) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
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
}
