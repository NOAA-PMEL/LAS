package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDocumentationNamespace {
	protected int tmgDocumentationId;
	protected int tmgDocumentationNamespaceId;
	protected String namespace;
	public void setTmgDocumentationId(int tmgDocumentationId){
		this.tmgDocumentationId = tmgDocumentationId;
	}
	public void setTmgDocumentationNamespaceId(int tmgDocumentationNamespaceId){
		this.tmgDocumentationNamespaceId = tmgDocumentationNamespaceId;
	}
	public void setNamespace(String namespace){
		this.namespace = namespace;
	}
	public int getTmgDocumentationId(){
		return this.tmgDocumentationId;
	}
	public int getTmgDocumentationNamespaceId(){
		return this.tmgDocumentationNamespaceId;
	}
	public String getNamespace(){
		return this.namespace;
	}

	public TmgDocumentationNamespace(int tmgDocumentationNamespace){
		this.tmgDocumentationNamespaceId = tmgDocumentationNamespace;
	}
	public TmgDocumentationNamespace(int tmgDocumentationId, int tmgDocumentationNamespaceId, String namespace){
		this.tmgDocumentationId = tmgDocumentationId;
		this.tmgDocumentationNamespaceId = tmgDocumentationNamespaceId;
		this.namespace=namespace;
	}
}
