package gov.noaa.pmel.tmap.addxml;

import thredds.catalog.InvDataset;
import ucar.nc2.dt.GridDataset;

public class DatasetGridPair {
	private InvDataset dataset;
	private GridDataset grid;
	public DatasetGridPair(InvDataset dataset, GridDataset grid) {
		this.dataset = dataset;
		this.grid = grid;
	}
	public InvDataset getDataset() {
		return dataset;
	}
	public void setDataset(InvDataset dataset) {
		this.dataset = dataset;
	}
	public GridDataset getGrid() {
		return grid;
	}
	public void setGrid(GridDataset grid) {
		this.grid = grid;
	}

}
