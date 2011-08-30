package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class CatalogrefDocumentationXlink {
	protected int catalogrefDocumentationId;
	protected int catalogrefDocumentationXlinkId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue xlink = new Datavalue(null);
	public void setCatalogrefDocumentationId(int catalogrefDocumentationId){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
	}
	public void setCatalogrefDocumentationXlinkId(int catalogrefDocumentationXlinkId){
		this.catalogrefDocumentationXlinkId = catalogrefDocumentationXlinkId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setXlink(String xlink){
		this.xlink = new Datavalue(xlink);
	}
	public int getCatalogrefDocumentationId(){
		return this.catalogrefDocumentationId;
	}
	public int getCatalogrefDocumentationXlinkId(){
		return this.catalogrefDocumentationXlinkId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getXlink(){
		return this.xlink;
	}

	public CatalogrefDocumentationXlink(){
		this.catalogrefDocumentationXlinkId = -1;
	}
	public CatalogrefDocumentationXlink(int catalogrefDocumentationXlink){
		this.catalogrefDocumentationXlinkId = catalogrefDocumentationXlink;
	}
	public CatalogrefDocumentationXlink(int catalogrefDocumentationId, int catalogrefDocumentationXlinkId, Datavalue value, Datavalue xlink){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
		this.catalogrefDocumentationXlinkId = catalogrefDocumentationXlinkId;
		this.value=value;
		this.xlink=xlink;
	}
	public CatalogrefDocumentationXlink clone(){
		CatalogrefDocumentationXlink clone = new CatalogrefDocumentationXlink(this.catalogrefDocumentationId, -1, this.value, this.xlink);
		return clone;
	}
}
