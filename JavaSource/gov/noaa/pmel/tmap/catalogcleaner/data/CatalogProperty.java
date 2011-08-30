package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class CatalogProperty {
	protected int catalogId;
	protected int catalogPropertyId;
	protected Datavalue name = new Datavalue(null);
	protected Datavalue value = new Datavalue(null);
	public void setCatalogId(int catalogId){
		this.catalogId = catalogId;
	}
	public void setCatalogPropertyId(int catalogPropertyId){
		this.catalogPropertyId = catalogPropertyId;
	}
	public void setName(String name){
		this.name = new Datavalue(name);
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public int getCatalogId(){
		return this.catalogId;
	}
	public int getCatalogPropertyId(){
		return this.catalogPropertyId;
	}
	public Datavalue getName(){
		return this.name;
	}
	public Datavalue getValue(){
		return this.value;
	}

	public CatalogProperty(){
		this.catalogPropertyId = -1;
	}
	public CatalogProperty(int catalogProperty){
		this.catalogPropertyId = catalogProperty;
	}
	public CatalogProperty(int catalogId, int catalogPropertyId, Datavalue name, Datavalue value){
		this.catalogId = catalogId;
		this.catalogPropertyId = catalogPropertyId;
		this.name=name;
		this.value=value;
	}
	public CatalogProperty clone(){
		CatalogProperty clone = new CatalogProperty(this.catalogId, -1, this.name, this.value);
		return clone;
	}
}
