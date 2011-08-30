package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class DatasetProperty {
	protected int datasetId;
	protected int datasetPropertyId;
	protected Datavalue name = new Datavalue(null);
	protected Datavalue value = new Datavalue(null);
	public void setDatasetId(int datasetId){
		this.datasetId = datasetId;
	}
	public void setDatasetPropertyId(int datasetPropertyId){
		this.datasetPropertyId = datasetPropertyId;
	}
	public void setName(String name){
		this.name = new Datavalue(name);
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public int getDatasetId(){
		return this.datasetId;
	}
	public int getDatasetPropertyId(){
		return this.datasetPropertyId;
	}
	public Datavalue getName(){
		return this.name;
	}
	public Datavalue getValue(){
		return this.value;
	}

	public DatasetProperty(){
		this.datasetPropertyId = -1;
	}
	public DatasetProperty(int datasetProperty){
		this.datasetPropertyId = datasetProperty;
	}
	public DatasetProperty(int datasetId, int datasetPropertyId, Datavalue name, Datavalue value){
		this.datasetId = datasetId;
		this.datasetPropertyId = datasetPropertyId;
		this.name=name;
		this.value=value;
	}
	public DatasetProperty clone(){
		DatasetProperty clone = new DatasetProperty(this.datasetId, -1, this.name, this.value);
		return clone;
	}
}
