package gov.noaa.pmel.tmap.catalogcleaner.data;

public class DatasetNcml {
	protected int datasetNcmlId;
	protected int datasetId;
	public void setDatasetId(int datasetId){
		this.datasetId = datasetId;
	}
	public int getDatasetNcmlId(){
		return this.datasetNcmlId;
	}
	public int getDatasetId(){
		return this.datasetId;
	}

	public DatasetNcml(int id){
		this.datasetNcmlId=id;
	}
	public DatasetNcml(int datasetNcmlId, int datasetId){
		this.datasetNcmlId=datasetNcmlId;
		this.datasetId=datasetId;
	}
}
