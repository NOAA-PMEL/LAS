package gov.noaa.pmel.tmap.las.client.serializable;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ConfigSerializable implements IsSerializable {
	GridSerializable grid;
	OperationSerializable[] operations;
	RegionSerializable[] regions;
	List<ERDDAPConstraintGroup> constraintGroups;
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
    public List<ERDDAPConstraintGroup> getConstraintGroups() {
        return constraintGroups;
    }
    public void setConstraintGroups(List<ERDDAPConstraintGroup> groups) {
        constraintGroups = groups;
    }
}
