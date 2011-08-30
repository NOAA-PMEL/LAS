package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class CatalogrefXlink {
	protected int catalogrefId;
	protected int catalogrefXlinkId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue xlink = new Datavalue(null);
	public void setCatalogrefId(int catalogrefId){
		this.catalogrefId = catalogrefId;
	}
	public void setCatalogrefXlinkId(int catalogrefXlinkId){
		this.catalogrefXlinkId = catalogrefXlinkId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setXlink(String xlink){
		this.xlink = new Datavalue(xlink);
	}
	public int getCatalogrefId(){
		return this.catalogrefId;
	}
	public int getCatalogrefXlinkId(){
		return this.catalogrefXlinkId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getXlink(){
		return this.xlink;
	}

	public CatalogrefXlink(){
		this.catalogrefXlinkId = -1;
	}
	public CatalogrefXlink(int catalogrefXlink){
		this.catalogrefXlinkId = catalogrefXlink;
	}
	public CatalogrefXlink(int catalogrefId, int catalogrefXlinkId, Datavalue value, Datavalue xlink){
		this.catalogrefId = catalogrefId;
		this.catalogrefXlinkId = catalogrefXlinkId;
		this.value=value;
		this.xlink=xlink;
	}
	public CatalogrefXlink clone(){
		CatalogrefXlink clone = new CatalogrefXlink(this.catalogrefId, -1, this.value, this.xlink);
		return clone;
	}
}
