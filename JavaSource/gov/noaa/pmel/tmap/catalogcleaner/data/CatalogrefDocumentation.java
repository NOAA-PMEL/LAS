package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogrefDocumentation {
	protected int catalogrefDocumentationId;
	protected int catalogrefId;
	protected String value;
	protected String documentationenum;
	public void setCatalogrefId(int catalogrefId){
		this.catalogrefId = catalogrefId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setDocumentationenum(String documentationenum){
		this.documentationenum = documentationenum;
	}
	public int getCatalogrefDocumentationId(){
		return this.catalogrefDocumentationId;
	}
	public int getCatalogrefId(){
		return this.catalogrefId;
	}
	public String getValue(){
		return this.value;
	}
	public String getDocumentationenum(){
		return this.documentationenum;
	}

	public CatalogrefDocumentation(int id){
		this.catalogrefDocumentationId=id;
	}
	public CatalogrefDocumentation(int catalogrefDocumentationId, String value, String documentationenum, int catalogrefId){
		this.catalogrefDocumentationId=catalogrefDocumentationId;
		this.value=value;
		this.documentationenum=documentationenum;
		this.catalogrefId=catalogrefId;
	}
}
