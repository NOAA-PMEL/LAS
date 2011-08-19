package gov.noaa.pmel.tmap.catalogcleaner.data;

public class DatasetProperty {
	protected int datasetId;
	protected int datasetPropertyId;
	protected String name;
	protected String value;
	public void setDatasetId(int datasetId){
		this.datasetId = datasetId;
	}
	public void setDatasetPropertyId(int datasetPropertyId){
		this.datasetPropertyId = datasetPropertyId;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setValue(String value){
		this.value = value;
	}
	public int getDatasetId(){
		return this.datasetId;
	}
	public int getDatasetPropertyId(){
		return this.datasetPropertyId;
	}
	public String getName(){
		return this.name;
	}
	public String getValue(){
		return this.value;
	}

	public DatasetProperty(int datasetProperty){
		this.datasetPropertyId = datasetProperty;
	}
	public DatasetProperty(int datasetId, int datasetPropertyId, String name, String value){
		this.datasetId = datasetId;
		this.datasetPropertyId = datasetPropertyId;
		this.name=name;
		this.value=value;
	}
}
