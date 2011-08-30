package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class CatalogrefDocumentationNamespace {
	protected int catalogrefDocumentationId;
	protected int catalogrefDocumentationNamespaceId;
	protected Datavalue namespace = new Datavalue(null);
	public void setCatalogrefDocumentationId(int catalogrefDocumentationId){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
	}
	public void setCatalogrefDocumentationNamespaceId(int catalogrefDocumentationNamespaceId){
		this.catalogrefDocumentationNamespaceId = catalogrefDocumentationNamespaceId;
	}
	public void setNamespace(String namespace){
		this.namespace = new Datavalue(namespace);
	}
	public int getCatalogrefDocumentationId(){
		return this.catalogrefDocumentationId;
	}
	public int getCatalogrefDocumentationNamespaceId(){
		return this.catalogrefDocumentationNamespaceId;
	}
	public Datavalue getNamespace(){
		return this.namespace;
	}

	public CatalogrefDocumentationNamespace(){
		this.catalogrefDocumentationNamespaceId = -1;
	}
	public CatalogrefDocumentationNamespace(int catalogrefDocumentationNamespace){
		this.catalogrefDocumentationNamespaceId = catalogrefDocumentationNamespace;
	}
	public CatalogrefDocumentationNamespace(int catalogrefDocumentationId, int catalogrefDocumentationNamespaceId, Datavalue namespace){
		this.catalogrefDocumentationId = catalogrefDocumentationId;
		this.catalogrefDocumentationNamespaceId = catalogrefDocumentationNamespaceId;
		this.namespace=namespace;
	}
	public CatalogrefDocumentationNamespace clone(){
		CatalogrefDocumentationNamespace clone = new CatalogrefDocumentationNamespace(this.catalogrefDocumentationId, -1, this.namespace);
		return clone;
	}
}
