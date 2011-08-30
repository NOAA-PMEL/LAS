package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class CatalogXlink {
	protected int catalogId;
	protected int catalogXlinkId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue xlink = new Datavalue(null);
	public void setCatalogId(int catalogId){
		this.catalogId = catalogId;
	}
	public void setCatalogXlinkId(int catalogXlinkId){
		this.catalogXlinkId = catalogXlinkId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setXlink(String xlink){
		this.xlink = new Datavalue(xlink);
	}
	public int getCatalogId(){
		return this.catalogId;
	}
	public int getCatalogXlinkId(){
		return this.catalogXlinkId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getXlink(){
		return this.xlink;
	}

	public CatalogXlink(){
		this.catalogXlinkId = -1;
	}
	public CatalogXlink(int catalogXlink){
		this.catalogXlinkId = catalogXlink;
	}
	public CatalogXlink(int catalogId, int catalogXlinkId, Datavalue value, Datavalue xlink){
		this.catalogId = catalogId;
		this.catalogXlinkId = catalogXlinkId;
		this.value=value;
		this.xlink=xlink;
	}
	public CatalogXlink clone(){
		CatalogXlink clone = new CatalogXlink(this.catalogId, -1, this.value, this.xlink);
		return clone;
	}
}
