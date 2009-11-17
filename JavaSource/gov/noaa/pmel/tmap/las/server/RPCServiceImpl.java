package gov.noaa.pmel.tmap.las.server;


import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.client.RPCException;
import gov.noaa.pmel.tmap.las.client.RPCService;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OptionSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
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

import org.apache.commons.httpclient.HttpException;
import org.jdom.Element;
import org.jdom.JDOMException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class RPCServiceImpl extends RemoteServiceServlet implements RPCService {
	private static final LASProxy lasProxy = new LASProxy();
	/**
	 * @throws RPCException 
	 * 
	 */
	public CategorySerializable[] getTimeSeries() throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);	
		CategorySerializable[] cats = null;
		if ( true ) {
			// Proxy off to the appropriate server...
			// TODO ** need a getTimeSeriesDatasets.do that does this to allow us to implement the proxy...
		} else {

			try {
				ArrayList<Category> categories = lasConfig.getTimeSeriesDatasets();
				cats = lasConfig.getCategorySerializable(categories);
			} catch (LASException e) {
				throw new RPCException(e.getMessage());
			} catch (JDOMException e) {
				throw new RPCException(e.getMessage());
			}
		}
		return cats;
	}
	public DatasetSerializable getDataset(String id) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY); 
		try {
			if ( id != null ) {
				if ( lasConfig.isLocal(id) ) {
					Dataset dataset = lasConfig.getDataset(id);
					return dataset.getDatasetSerializable();
				} else {
					String[] parts = id.split(Constants.NAME_SPACE_SPARATOR);
					String server_key = null;
					if ( parts != null ) {
						server_key = parts[0];
						if ( server_key != null ) {
							Tributary trib = lasConfig.getTributary(server_key);
							String las_url = trib.getURL() + Constants.GET_FULLDATASET + "?format=xml&dsid=" + id;
							String dataset_xml = lasProxy.executeGetMethodAndReturnResult(las_url);
							LASDocument dsdoc = new LASDocument();
							JDOMUtils.XML2JDOM(dataset_xml, dsdoc);
							Element dsElement = dsdoc.getRootElement();
							Dataset ds = new Dataset(dsElement);
							return ds.getDatasetSerializable();
						}
					}
				}
			} else {
				throw new RPCException("No server key found.");
			}
		} catch (JDOMException e) {
			throw new RPCException(e.getMessage());
		} catch (Exception e) {
			throw new RPCException(e.getMessage());
		} 
		return null;
	}
	public VariableSerializable[] getVariables(String id) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY); 
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ArrayList<Grid> grids = new ArrayList<Grid>();
		try {
			if ( id != null ) {
				if ( lasConfig.isLocal(id) ) {
					variables = lasConfig.getFullVariables(id);
				} else {
					String[] parts = id.split(Constants.NAME_SPACE_SPARATOR);
					String server_key = null;
					if ( parts != null ) {
						server_key = parts[0];
						if ( server_key != null ) {
							Tributary trib = lasConfig.getTributary(server_key);
							String las_url = trib.getURL() + Constants.GET_FULLVARIABLES + "?format=xml&dsid=" + id;
							String variables_xml = lasProxy.executeGetMethodAndReturnResult(las_url);
							LASDocument varsdoc = new LASDocument();
							JDOMUtils.XML2JDOM(variables_xml, varsdoc);
							List varElements = varsdoc.getRootElement().getChildren("variable");
							for (Iterator varsIt = varElements.iterator(); varsIt.hasNext();) {
								Element varElement = (Element) varsIt.next();
								Variable variable = new Variable((Element)varElement.clone(), id, varElement.getAttributeValue("DSName"));
								variables.add(variable);
							}
						} else {
							throw new RPCException("No server key found.");
						}
					} else {
						throw new RPCException("No server key found.");
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new RPCException(e.getMessage());
		} catch (HttpException e) {
			throw new RPCException(e.getMessage());
		} catch (JDOMException e) {
			throw new RPCException(e.getMessage());
		} catch (IOException e) {
			throw new RPCException(e.getMessage());
		} catch (LASException e) {
			throw new RPCException(e.getMessage());
		}
		VariableSerializable[] wire_vars = new VariableSerializable[variables.size()];
		for (int i = 0; i < variables.size(); i++ ) {
			Variable variable = variables.get(i);	
			wire_vars[i] = variable.getVariableSerializable();
		}
		return wire_vars;
	}
	public CategorySerializable[] getCategories(String id) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY); 
		ArrayList<Category> categories = new ArrayList<Category>();
		
		try {
			if ( id == null ) {
				// Do the local top level category
				categories = lasConfig.getFullCategories(id);

				// Do the remote categories...

				ArrayList<Tributary> tributaries = lasConfig.getTributaries();
				for (Iterator tribIt = tributaries.iterator(); tribIt.hasNext();) {
					Tributary tributary = (Tributary) tribIt.next();
					String url = tributary.getURL() + Constants.GET_FULL_CATEGORIES;
					String catxml = lasProxy.executeGetMethodAndReturnResult(url);
					categories.addAll(JDOMUtils.getCategories(catxml));
				}

				Collections.sort(categories, new ContainerComparator("name"));

			} else {
				// Figure out if the category is from the local or remote server and process accordingly.
				
				// TODO as above
				
			}
			CategorySerializable[] cats = new CategorySerializable[categories.size()];
			int i = 0;
			for (Iterator catIt = categories.iterator(); catIt.hasNext();) {
				Category category = (Category) catIt.next();
				cats[i] = category.getCategorySerializable();
			}
            return cats;

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
	}
	public GridSerializable getGrid(String dsID, String varID) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
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
	public OperationSerializable[] getOperations(String view, String dsID, String varID) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
		OperationSerializable[] wireOps = null;
		try {
			ArrayList<Operation> operations;
			if ( view != null) {
				operations = lasConfig.getOperations(view, dsID, varID);

				if ( operations.size() <= 0 ) {
					throw new RPCException("No operations found.");
				} else {
					// Check to see if there's something in there.
					Operation op = operations.get(0);
					String name = op.getName();
					if ( name == null || name.equals("") ) {
						throw new RPCException("No operations found.");
					}
				}
			} else {
				operations = new ArrayList<Operation>();
				ArrayList<View> views = lasConfig.getViewsByDatasetAndVariable(dsID, varID);
				HashMap<String, Operation> allOps = new HashMap<String, Operation>();
				for (Iterator viewIt = views.iterator(); viewIt.hasNext();) {
					View aView = (View) viewIt.next();
					ArrayList<Operation> ops = lasConfig.getOperations(aView.getValue(), dsID, varID);
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
			wireOps = new OperationSerializable[operations.size()];
			int k=0;
			for (Iterator opsIt = operations.iterator(); opsIt.hasNext();) {
				Operation op = (Operation) opsIt.next();
				wireOps[k] = op.getOperationSerializable();
				k++;
			}
		} catch (JDOMException e) {
			throw new RPCException(e.getMessage());
		} catch (RPCException e) {
			throw new RPCException(e.getMessage());
		} catch (LASException e) {
			throw new RPCException(e.getMessage());
		}

		return wireOps;
	}
//	public OptionSerializable[] getOptionsByOperationID(String operationID) throws RPCException {
//		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
//		ArrayList<Option> options = new ArrayList<Option>();
//		OptionSerializable[] wireOptions;
//
//		try {
//
//			if ( operationID != null ) {
//				if ( lasConfig.isLocal(operationID) ) {
//					options = lasConfig.getOptionsByOperationID(operationID);
//				} else {
//					String[] parts = operationID.split(Constants.NAME_SPACE_SPARATOR);
//					String server_key = null;
//					if ( parts != null ) {
//						server_key = parts[0];
//						if ( server_key != null ) {
//							Tributary trib = lasConfig.getTributary(server_key);	
//
//
//
//							String las_url = trib.getURL() + Constants.GET_OPTIONS + "?format=xml&dsid=" + operationID;
//							String variables_xml = lasProxy.executeGetMethodAndReturnResult(las_url);
//							LASDocument optionsdoc = new LASDocument();
//							JDOMUtils.XML2JDOM(variables_xml, optionsdoc);
//							List opElements = optionsdoc.getRootElement().getChildren("option");
//							for (Iterator opIt = opElements.iterator(); opIt.hasNext();) {
//								Element opElement = (Element) opIt.next();
//								Option option = new Option((Element)opElement.clone());
//								options.add(option);
//							}
//
//
//
//						}
//					}
//				}
//			}
//			options = lasConfig.getOptionsByOperationID(operationID);
//		} catch (JDOMException e) {
//			throw new RPCException(e.getMessage());
//		} catch (UnsupportedEncodingException e) {
//			throw new RPCException(e.getMessage());
//		} catch (HttpException e) {
//			throw new RPCException(e.getMessage());
//		} catch (IOException e) {
//			throw new RPCException(e.getMessage());
//		}
//		int i=0;
//		if ( options != null ) {
//			wireOptions = new OptionSerializable[options.size()];
//			for (Iterator optionIt = options.iterator(); optionIt.hasNext();) {
//				Option option = (Option) optionIt.next();
//				wireOptions[i] = option.getOptionSerializable();
//				i++;
//			}
//			return wireOptions;
//		} else {
//			return null;
//		}
//	}
	/**
	 * (non-Javadoc)
	 * @see gov.noaa.pmel.tmap.las.client.RPCService#getOptions(java.lang.String)
	 * @deprecated
	 */
	public OptionSerializable[] getOptions(String opid) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
		// The id is actually the name in the options section of the XML, but it's unique and functions as an id...
		ArrayList<Option> options = new ArrayList<Option>();
		OptionSerializable[] wireOptions;
		try {
			if ( opid != null ) {
				if ( lasConfig.isLocal(opid) ) {
					options = lasConfig.getOptions(opid);
				} else {
					String[] parts = opid.split(Constants.NAME_SPACE_SPARATOR);
					String server_key = null;
					if ( parts != null ) {
						server_key = parts[0];
						if ( server_key != null ) {
							Tributary trib = lasConfig.getTributary(server_key);	



							String las_url = trib.getURL() + Constants.GET_OPTIONS + "?format=xml&dsid=" + opid;
							String variables_xml = lasProxy.executeGetMethodAndReturnResult(las_url);
							LASDocument optionsdoc = new LASDocument();
							JDOMUtils.XML2JDOM(variables_xml, optionsdoc);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
