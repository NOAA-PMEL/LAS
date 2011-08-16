package gov.noaa.pmel.tmap.catalogcleaner.data;

public class DatasetAccessDatasize {
	protected int datasetAccessDatasizeId;
	protected int datasetAccessId;
	protected String value;
	protected String units;
	public void setDatasetAccessId(int datasetAccessId){
		this.datasetAccessId = datasetAccessId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setUnits(String units){
		this.units = units;
	}
	public int getDatasetAccessDatasizeId(){
		return this.datasetAccessDatasizeId;
	}
	public int getDatasetAccessId(){
		return this.datasetAccessId;
	}
	public String getValue(){
		return this.value;
	}
	public String getUnits(){
		return this.units;
	}

	public DatasetAccessDatasize(int id){
		this.datasetAccessDatasizeId=id;
	}
	public DatasetAccessDatasize(int datasetAccessDatasizeId, String value, String units, int datasetAccessId){
		this.datasetAccessDatasizeId=datasetAccessDatasizeId;
		this.value=value;
		this.units=units;
		this.datasetAccessId=datasetAccessId;
	}
}
