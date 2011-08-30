package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgDocumentationNamespace {
	protected int tmgDocumentationId;
	protected int tmgDocumentationNamespaceId;
	protected Datavalue namespace = new Datavalue(null);
	public void setTmgDocumentationId(int tmgDocumentationId){
		this.tmgDocumentationId = tmgDocumentationId;
	}
	public void setTmgDocumentationNamespaceId(int tmgDocumentationNamespaceId){
		this.tmgDocumentationNamespaceId = tmgDocumentationNamespaceId;
	}
	public void setNamespace(String namespace){
		this.namespace = new Datavalue(namespace);
	}
	public int getTmgDocumentationId(){
		return this.tmgDocumentationId;
	}
	public int getTmgDocumentationNamespaceId(){
		return this.tmgDocumentationNamespaceId;
	}
	public Datavalue getNamespace(){
		return this.namespace;
	}

	public TmgDocumentationNamespace(){
		this.tmgDocumentationNamespaceId = -1;
	}
	public TmgDocumentationNamespace(int tmgDocumentationNamespace){
		this.tmgDocumentationNamespaceId = tmgDocumentationNamespace;
	}
	public TmgDocumentationNamespace(int tmgDocumentationId, int tmgDocumentationNamespaceId, Datavalue namespace){
		this.tmgDocumentationId = tmgDocumentationId;
		this.tmgDocumentationNamespaceId = tmgDocumentationNamespaceId;
		this.namespace=namespace;
	}
	public TmgDocumentationNamespace clone(){
		TmgDocumentationNamespace clone = new TmgDocumentationNamespace(this.tmgDocumentationId, -1, this.namespace);
		return clone;
	}
}
