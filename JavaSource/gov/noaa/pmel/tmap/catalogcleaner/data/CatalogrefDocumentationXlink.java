package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogrefDocumentationXlink {
	protected int catalogrefDocumentationId;
	protected int catalogrefDocumentationXlinkId;
	protected String value;
	protected String xlink;
	public void setCatalogrefDocumentationId(int catalogrefDocumentationId){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
	}
	public void setCatalogrefDocumentationXlinkId(int catalogrefDocumentationXlinkId){
		this.catalogrefDocumentationXlinkId = catalogrefDocumentationXlinkId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getCatalogrefDocumentationId(){
		return this.catalogrefDocumentationId;
	}
	public int getCatalogrefDocumentationXlinkId(){
		return this.catalogrefDocumentationXlinkId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public CatalogrefDocumentationXlink(int catalogrefDocumentationXlink){
		this.catalogrefDocumentationXlinkId = catalogrefDocumentationXlink;
	}
	public CatalogrefDocumentationXlink(int catalogrefDocumentationId, int catalogrefDocumentationXlinkId, String value, String xlink){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
		this.catalogrefDocumentationXlinkId = catalogrefDocumentationXlinkId;
		this.value=value;
		this.xlink=xlink;
	}
}
