package gov.noaa.pmel.tmap.catalogcleaner.data;

public class MetadataNamespace {
	protected int metadataNamespaceId;
	protected int metadataId;
	protected String namespace;
	public void setMetadataId(int metadataId){
		this.metadataId = metadataId;
	}
	public void setNamespace(String namespace){
		this.namespace = namespace;
	}
	public int getMetadataNamespaceId(){
		return this.metadataNamespaceId;
	}
	public int getMetadataId(){
		return this.metadataId;
	}
	public String getNamespace(){
		return this.namespace;
	}

	public MetadataNamespace(int id){
		this.metadataNamespaceId=id;
	}
	public MetadataNamespace(int metadataNamespaceId, String namespace, int metadataId){
		this.metadataNamespaceId=metadataNamespaceId;
		this.namespace=namespace;
		this.metadataId=metadataId;
	}
}
