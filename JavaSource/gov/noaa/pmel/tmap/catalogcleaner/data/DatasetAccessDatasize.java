package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class DatasetAccessDatasize {
	protected int datasetAccessId;
	protected int datasetAccessDatasizeId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue units = new Datavalue(null);
	public void setDatasetAccessId(int datasetAccessId){
		this.datasetAccessId = datasetAccessId;
	}
	public void setDatasetAccessDatasizeId(int datasetAccessDatasizeId){
		this.datasetAccessDatasizeId = datasetAccessDatasizeId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setUnits(String units){
		this.units = new Datavalue(units);
	}
	public int getDatasetAccessId(){
		return this.datasetAccessId;
	}
	public int getDatasetAccessDatasizeId(){
		return this.datasetAccessDatasizeId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getUnits(){
		return this.units;
	}

	public DatasetAccessDatasize(){
		this.datasetAccessDatasizeId = -1;
	}
	public DatasetAccessDatasize(int datasetAccessDatasize){
		this.datasetAccessDatasizeId = datasetAccessDatasize;
	}
	public DatasetAccessDatasize(int datasetAccessId, int datasetAccessDatasizeId, Datavalue value, Datavalue units){
		this.datasetAccessId = datasetAccessId;
		this.datasetAccessDatasizeId = datasetAccessDatasizeId;
		this.value=value;
		this.units=units;
	}
	public DatasetAccessDatasize clone(){
		DatasetAccessDatasize clone = new DatasetAccessDatasize(this.datasetAccessId, -1, this.value, this.units);
		return clone;
	}
}
