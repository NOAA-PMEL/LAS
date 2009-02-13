package gov.noaa.pmel.tmap.las.rpc;


import gov.noaa.pmel.tmap.las.client.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.GridSerializable;
import gov.noaa.pmel.tmap.las.client.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.OptionSerializable;

import com.google.gwt.user.client.rpc.RemoteService;

public interface RPCService extends RemoteService {
	/**
	 * 
	 */
	public CategorySerializable[] getCategories(String id) throws RPCException;
	public GridSerializable getGrid(String dsID, String varID) throws RPCException;
	public OperationSerializable[] getOperations(String view, String dsID, String varID) throws RPCException;
	public OptionSerializable[] getOptions(String opid) throws RPCException;
	public CategorySerializable[] getTimeSeries() throws RPCException;
}
