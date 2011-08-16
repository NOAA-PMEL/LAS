package gov.noaa.pmel.tmap.catalogcleaner.data;

public class DatasetProperty {
	protected int datasetPropertyId;
	protected int datasetId;
	protected String name;
	protected String value;
	public void setDatasetId(int datasetId){
		this.datasetId = datasetId;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setValue(String value){
		this.value = value;
	}
	public int getDatasetPropertyId(){
		return this.datasetPropertyId;
	}
	public int getDatasetId(){
		return this.datasetId;
	}
	public String getName(){
		return this.name;
	}
	public String getValue(){
		return this.value;
	}

	public DatasetProperty(int id){
		this.datasetPropertyId=id;
	}
	public DatasetProperty(int datasetPropertyId, String name, String value, int datasetId){
		this.datasetPropertyId=datasetPropertyId;
		this.name=name;
		this.value=value;
		this.datasetId=datasetId;
	}
}
