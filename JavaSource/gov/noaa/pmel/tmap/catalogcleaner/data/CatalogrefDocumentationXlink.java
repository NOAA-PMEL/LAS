package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogrefDocumentationXlink {
	protected int catalogrefDocumentationXlinkId;
	protected int catalogrefDocumentationId;
	protected String value;
	protected String xlink;
	public void setCatalogrefDocumentationId(int catalogrefDocumentationId){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getCatalogrefDocumentationXlinkId(){
		return this.catalogrefDocumentationXlinkId;
	}
	public int getCatalogrefDocumentationId(){
		return this.catalogrefDocumentationId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public CatalogrefDocumentationXlink(int id){
		this.catalogrefDocumentationXlinkId=id;
	}
	public CatalogrefDocumentationXlink(int catalogrefDocumentationXlinkId, String value, String xlink, int catalogrefDocumentationId){
		this.catalogrefDocumentationXlinkId=catalogrefDocumentationXlinkId;
		this.value=value;
		this.xlink=xlink;
		this.catalogrefDocumentationId=catalogrefDocumentationId;
	}
}
