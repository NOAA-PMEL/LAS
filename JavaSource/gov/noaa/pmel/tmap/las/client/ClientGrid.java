package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ClientGrid {
	RPCServiceAsync gridService;
	GridSerializable grid;
	public ClientGrid (String dsID, String varID, RPCServiceAsync rpcService) {
		this.gridService = rpcService;
		gridService.getGrid(dsID, varID, gridCallback);
	}
	public void setGridData(String dsID, String varID) {
		gridService.getGrid(dsID, varID, gridCallback);
	}
	AsyncCallback gridCallback = new AsyncCallback() {
		public void onSuccess(Object result) {
			grid = (GridSerializable) result;
		}
		public void onFailure(Throwable caught) {
			grid = null;
		}
	};
	public GridSerializable getGrid() {
		return grid;
	}
}
