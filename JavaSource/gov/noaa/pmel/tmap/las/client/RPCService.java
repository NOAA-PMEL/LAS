package gov.noaa.pmel.tmap.las.client;


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
