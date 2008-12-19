package gov.noaa.pmel.tmap.las.server;


import gov.noaa.pmel.tmap.las.client.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.GridSerializable;
import gov.noaa.pmel.tmap.las.client.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.OptionSerializable;
import gov.noaa.pmel.tmap.las.client.RPCException;
import gov.noaa.pmel.tmap.las.client.RPCService;
import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Grid;
import gov.noaa.pmel.tmap.las.util.Operation;
import gov.noaa.pmel.tmap.las.util.Option;
import gov.noaa.pmel.tmap.las.util.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jdom.JDOMException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class RPCServiceImpl extends RemoteServiceServlet implements RPCService {
	/**
	 * @throws RPCException 
	 * 
	 */
	public CategorySerializable[] getTimeSeries() throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);		
		CategorySerializable[] cats;
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
	public CategorySerializable[] getCategories(String id) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY); 
		
		ArrayList<Category> categories = new ArrayList<Category>();
		try {
			categories = lasConfig.getCategories(id);
		} catch (JDOMException e) {
			throw new RPCException(e.getMessage());
		}
		CategorySerializable[] cats;
		try {
			cats = lasConfig.getCategorySerializable(categories);
		} catch (LASException e) {
			throw new RPCException(e.getMessage());
		} catch (JDOMException e) {
			throw new RPCException(e.getMessage());
		}
		return cats;
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
	public OptionSerializable[] getOptions(String opid) throws RPCException {
		LASConfig lasConfig = (LASConfig) getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
		ArrayList<Option> options;
		OptionSerializable[] wireOptions;
		try {
			options = lasConfig.getOptions(opid);
		} catch (JDOMException e) {
			throw new RPCException(e.getMessage());
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
