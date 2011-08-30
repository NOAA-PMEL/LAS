package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class MetadataNamespace {
	protected int metadataId;
	protected int metadataNamespaceId;
	protected Datavalue namespace = new Datavalue(null);
	public void setMetadataId(int metadataId){
		this.metadataId = metadataId;
	}
	public void setMetadataNamespaceId(int metadataNamespaceId){
		this.metadataNamespaceId = metadataNamespaceId;
	}
	public void setNamespace(String namespace){
		this.namespace = new Datavalue(namespace);
	}
	public int getMetadataId(){
		return this.metadataId;
	}
	public int getMetadataNamespaceId(){
		return this.metadataNamespaceId;
	}
	public Datavalue getNamespace(){
		return this.namespace;
	}

	public MetadataNamespace(){
		this.metadataNamespaceId = -1;
	}
	public MetadataNamespace(int metadataNamespace){
		this.metadataNamespaceId = metadataNamespace;
	}
	public MetadataNamespace(int metadataId, int metadataNamespaceId, Datavalue namespace){
		this.metadataId = metadataId;
		this.metadataNamespaceId = metadataNamespaceId;
		this.namespace=namespace;
	}
	public MetadataNamespace clone(){
		MetadataNamespace clone = new MetadataNamespace(this.metadataId, -1, this.namespace);
		return clone;
	}
}
