package gov.noaa.pmel.tmap.catalogcleaner.data;

public class Metadata {
	protected int metadataId;
	protected String metadatatype;
	protected String inherited;
	public void setMetadataId(int metadataId){
		this.metadataId = metadataId;
	}
	public void setMetadatatype(String metadatatype){
		this.metadatatype = metadatatype;
	}
	public void setInherited(String inherited){
		this.inherited = inherited;
	}
	public int getMetadataId(){
		return this.metadataId;
	}
	public String getMetadatatype(){
		return this.metadatatype;
	}
	public String getInherited(){
		return this.inherited;
	}

	public Metadata(int metadata){
		this.metadataId = metadata;
	}
	public Metadata(int metadataId, String metadatatype, String inherited){
		this.metadataId = metadataId;
		this.metadatatype=metadatatype;
		this.inherited=inherited;
	}
}
