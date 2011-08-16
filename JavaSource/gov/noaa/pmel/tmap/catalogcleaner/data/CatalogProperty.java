package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogProperty {
	protected int catalogPropertyId;
	protected int catalogId;
	protected String name;
	protected String value;
	public void setCatalogId(int catalogId){
		this.catalogId = catalogId;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setValue(String value){
		this.value = value;
	}
	public int getCatalogPropertyId(){
		return this.catalogPropertyId;
	}
	public int getCatalogId(){
		return this.catalogId;
	}
	public String getName(){
		return this.name;
	}
	public String getValue(){
		return this.value;
	}

	public CatalogProperty(int id){
		this.catalogPropertyId=id;
	}
	public CatalogProperty(int catalogPropertyId, String name, String value, int catalogId){
		this.catalogPropertyId=catalogPropertyId;
		this.name=name;
		this.value=value;
		this.catalogId=catalogId;
	}
}
