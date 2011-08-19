package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogrefDocumentation {
	protected int catalogrefId;
	protected int catalogrefDocumentationId;
	protected String value;
	protected String documentationenum;
	public void setCatalogrefId(int catalogrefId){
		this.catalogrefId = catalogrefId;
	}
	public void setCatalogrefDocumentationId(int catalogrefDocumentationId){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setDocumentationenum(String documentationenum){
		this.documentationenum = documentationenum;
	}
	public int getCatalogrefId(){
		return this.catalogrefId;
	}
	public int getCatalogrefDocumentationId(){
		return this.catalogrefDocumentationId;
	}
	public String getValue(){
		return this.value;
	}
	public String getDocumentationenum(){
		return this.documentationenum;
	}

	public CatalogrefDocumentation(int catalogrefDocumentation){
		this.catalogrefDocumentationId = catalogrefDocumentation;
	}
	public CatalogrefDocumentation(int catalogrefId, int catalogrefDocumentationId, String value, String documentationenum){
		this.catalogrefId = catalogrefId;
		this.catalogrefDocumentationId = catalogrefDocumentationId;
		this.value=value;
		this.documentationenum=documentationenum;
	}
}
