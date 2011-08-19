package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogrefDocumentationNamespace {
	protected int catalogrefDocumentationId;
	protected int catalogrefDocumentationNamespaceId;
	protected String namespace;
	public void setCatalogrefDocumentationId(int catalogrefDocumentationId){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
	}
	public void setCatalogrefDocumentationNamespaceId(int catalogrefDocumentationNamespaceId){
		this.catalogrefDocumentationNamespaceId = catalogrefDocumentationNamespaceId;
	}
	public void setNamespace(String namespace){
		this.namespace = namespace;
	}
	public int getCatalogrefDocumentationId(){
		return this.catalogrefDocumentationId;
	}
	public int getCatalogrefDocumentationNamespaceId(){
		return this.catalogrefDocumentationNamespaceId;
	}
	public String getNamespace(){
		return this.namespace;
	}

	public CatalogrefDocumentationNamespace(int catalogrefDocumentationNamespace){
		this.catalogrefDocumentationNamespaceId = catalogrefDocumentationNamespace;
	}
	public CatalogrefDocumentationNamespace(int catalogrefDocumentationId, int catalogrefDocumentationNamespaceId, String namespace){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
		this.catalogrefDocumentationNamespaceId = catalogrefDocumentationNamespaceId;
		this.namespace=namespace;
	}
}
