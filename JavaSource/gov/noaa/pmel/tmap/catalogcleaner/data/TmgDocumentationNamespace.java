package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDocumentationNamespace {
	protected int tmgDocumentationNamespaceId;
	protected int tmgDocumentationId;
	protected String namespace;
	public void setTmgDocumentationId(int tmgDocumentationId){
		this.tmgDocumentationId = tmgDocumentationId;
	}
	public void setNamespace(String namespace){
		this.namespace = namespace;
	}
	public int getTmgDocumentationNamespaceId(){
		return this.tmgDocumentationNamespaceId;
	}
	public int getTmgDocumentationId(){
		return this.tmgDocumentationId;
	}
	public String getNamespace(){
		return this.namespace;
	}

	public TmgDocumentationNamespace(int id){
		this.tmgDocumentationNamespaceId=id;
	}
	public TmgDocumentationNamespace(int tmgDocumentationNamespaceId, String namespace, int tmgDocumentationId){
		this.tmgDocumentationNamespaceId=tmgDocumentationNamespaceId;
		this.namespace=namespace;
		this.tmgDocumentationId=tmgDocumentationId;
	}
}
