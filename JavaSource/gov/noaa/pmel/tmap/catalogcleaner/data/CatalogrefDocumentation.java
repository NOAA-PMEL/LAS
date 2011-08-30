package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class CatalogrefDocumentation {
	protected int catalogrefId;
	protected int catalogrefDocumentationId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue documentationenum = new Datavalue(null);
	public void setCatalogrefId(int catalogrefId){
		this.catalogrefId = catalogrefId;
	}
	public void setCatalogrefDocumentationId(int catalogrefDocumentationId){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setDocumentationenum(String documentationenum){
		this.documentationenum = new Datavalue(documentationenum);
	}
	public int getCatalogrefId(){
		return this.catalogrefId;
	}
	public int getCatalogrefDocumentationId(){
		return this.catalogrefDocumentationId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getDocumentationenum(){
		return this.documentationenum;
	}

	public CatalogrefDocumentation(){
		this.catalogrefDocumentationId = -1;
	}
	public CatalogrefDocumentation(int catalogrefDocumentation){
		this.catalogrefDocumentationId = catalogrefDocumentation;
	}
	public CatalogrefDocumentation(int catalogrefId, int catalogrefDocumentationId, Datavalue value, Datavalue documentationenum){
		this.catalogrefId = catalogrefId;
		this.catalogrefDocumentationId = catalogrefDocumentationId;
		this.value=value;
		this.documentationenum=documentationenum;
	}
	public CatalogrefDocumentation clone(){
		CatalogrefDocumentation clone = new CatalogrefDocumentation(this.catalogrefId, -1, this.value, this.documentationenum);
		return clone;
	}
}
