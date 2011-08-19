package gov.noaa.pmel.tmap.catalogcleaner.data;

public class DatasetAccessDatasize {
	protected int datasetAccessId;
	protected int datasetAccessDatasizeId;
	protected String value;
	protected String units;
	public void setDatasetAccessId(int datasetAccessId){
		this.datasetAccessId = datasetAccessId;
	}
	public void setDatasetAccessDatasizeId(int datasetAccessDatasizeId){
		this.datasetAccessDatasizeId = datasetAccessDatasizeId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setUnits(String units){
		this.units = units;
	}
	public int getDatasetAccessId(){
		return this.datasetAccessId;
	}
	public int getDatasetAccessDatasizeId(){
		return this.datasetAccessDatasizeId;
	}
	public String getValue(){
		return this.value;
	}
	public String getUnits(){
		return this.units;
	}

	public DatasetAccessDatasize(int datasetAccessDatasize){
		this.datasetAccessDatasizeId = datasetAccessDatasize;
	}
	public DatasetAccessDatasize(int datasetAccessId, int datasetAccessDatasizeId, String value, String units){
		this.datasetAccessId = datasetAccessId;
		this.datasetAccessDatasizeId = datasetAccessDatasizeId;
		this.value=value;
		this.units=units;
	}
}
