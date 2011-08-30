package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class DatasetNcml {
	protected int datasetId;
	protected int datasetNcmlId;
	public void setDatasetId(int datasetId){
		this.datasetId = datasetId;
	}
	public void setDatasetNcmlId(int datasetNcmlId){
		this.datasetNcmlId = datasetNcmlId;
	}
	public int getDatasetId(){
		return this.datasetId;
	}
	public int getDatasetNcmlId(){
		return this.datasetNcmlId;
	}

	public DatasetNcml(){
		this.datasetNcmlId = -1;
	}
	public DatasetNcml(int datasetNcml){
		this.datasetNcmlId = datasetNcml;
	}
	public DatasetNcml(int datasetId, int datasetNcmlId){
		this.datasetId = datasetId;
		this.datasetNcmlId = datasetNcmlId;
	}
	public DatasetNcml clone(){
		DatasetNcml clone = new DatasetNcml(this.datasetId, -1);
		return clone;
	}
}
