package gov.noaa.pmel.tmap.catalogcleaner.data;

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

	public DatasetNcml(int datasetNcml){
		this.datasetNcmlId = datasetNcml;
	}
	public DatasetNcml(int datasetId, int datasetNcmlId){
		this.datasetId = datasetId;
		this.datasetNcmlId = datasetNcmlId;
	}
}
