package gov.noaa.pmel.tmap.las.client.serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ConfigSerializable implements IsSerializable {
	GridSerializable grid;
	OperationSerializable[] operations;
	RegionSerializable[] regions;
	public GridSerializable getGrid() {
		return grid;
	}
	public void setGrid(GridSerializable grid) {
		this.grid = grid;
	}
	public OperationSerializable[] getOperations() {
		return operations;
	}
	public void setOperations(OperationSerializable[] operations) {
		this.operations = operations;
	}
	public RegionSerializable[] getRegions() {
		return regions;
	}
	public void setRegions(RegionSerializable[] regions) {
		this.regions = regions;
	}
}
