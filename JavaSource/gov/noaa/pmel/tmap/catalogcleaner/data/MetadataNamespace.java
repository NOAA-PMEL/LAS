package gov.noaa.pmel.tmap.catalogcleaner.data;

public class MetadataNamespace {
	protected int metadataId;
	protected int metadataNamespaceId;
	protected String namespace;
	public void setMetadataId(int metadataId){
		this.metadataId = metadataId;
	}
	public void setMetadataNamespaceId(int metadataNamespaceId){
		this.metadataNamespaceId = metadataNamespaceId;
	}
	public void setNamespace(String namespace){
		this.namespace = namespace;
	}
	public int getMetadataId(){
		return this.metadataId;
	}
	public int getMetadataNamespaceId(){
		return this.metadataNamespaceId;
	}
	public String getNamespace(){
		return this.namespace;
	}

	public MetadataNamespace(int metadataNamespace){
		this.metadataNamespaceId = metadataNamespace;
	}
	public MetadataNamespace(int metadataId, int metadataNamespaceId, String namespace){
		this.metadataId = metadataId;
		this.metadataNamespaceId = metadataNamespaceId;
		this.namespace=namespace;
	}
}
