package gov.noaa.pmel.tmap.las.client;

import java.util.List;

import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RPCServiceAsync {
	public void getCategories(String id, AsyncCallback categoryCallback);
	public void getVariable(String dsid, String varid, AsyncCallback getVariableCallback);
    public void getGrid(String dsid, String varid, AsyncCallback gridCallback);
    public void getOperations(String view, String dsid, String varid, AsyncCallback opCallback);
	public void getOptions(String opid, AsyncCallback optionsCallback);
    public void getTimeSeries(AsyncCallback timeSeriesCallback);
    public void getConfig(String view, String dsid, String varid, AsyncCallback<ConfigSerializable> timeSeriesCallback);
    public void getPropertyGroup(String name, AsyncCallback propertyGroupCallback);
	public void getOperations(String view, String[] xpath,	AsyncCallback<OperationSerializable[]> callback);
}
