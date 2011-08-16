package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogrefDocumentationNamespace {
	protected int catalogrefDocumentationNamespaceId;
	protected int catalogrefDocumentationId;
	protected String namespace;
	public void setCatalogrefDocumentationId(int catalogrefDocumentationId){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
	}
	public void setNamespace(String namespace){
		this.namespace = namespace;
	}
	public int getCatalogrefDocumentationNamespaceId(){
		return this.catalogrefDocumentationNamespaceId;
	}
	public int getCatalogrefDocumentationId(){
		return this.catalogrefDocumentationId;
	}
	public String getNamespace(){
		return this.namespace;
	}

	public CatalogrefDocumentationNamespace(int id){
		this.catalogrefDocumentationNamespaceId=id;
	}
	public CatalogrefDocumentationNamespace(int catalogrefDocumentationNamespaceId, String namespace, int catalogrefDocumentationId){
		this.catalogrefDocumentationNamespaceId=catalogrefDocumentationNamespaceId;
		this.namespace=namespace;
		this.catalogrefDocumentationId=catalogrefDocumentationId;
	}
}
