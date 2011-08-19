package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogProperty {
	protected int catalogId;
	protected int catalogPropertyId;
	protected String name;
	protected String value;
	public void setCatalogId(int catalogId){
		this.catalogId = catalogId;
	}
	public void setCatalogPropertyId(int catalogPropertyId){
		this.catalogPropertyId = catalogPropertyId;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setValue(String value){
		this.value = value;
	}
	public int getCatalogId(){
		return this.catalogId;
	}
	public int getCatalogPropertyId(){
		return this.catalogPropertyId;
	}
	public String getName(){
		return this.name;
	}
	public String getValue(){
		return this.value;
	}

	public CatalogProperty(int catalogProperty){
		this.catalogPropertyId = catalogProperty;
	}
	public CatalogProperty(int catalogId, int catalogPropertyId, String name, String value){
		this.catalogId = catalogId;
		this.catalogPropertyId = catalogPropertyId;
		this.name=name;
		this.value=value;
	}
}
