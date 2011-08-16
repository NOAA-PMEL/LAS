package gov.noaa.pmel.tmap.catalogcleaner.data;

public class MetadataXlink {
	protected int metadataXlinkId;
	protected int metadataId;
	protected String value;
	protected String xlink;
	public void setMetadataId(int metadataId){
		this.metadataId = metadataId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getMetadataXlinkId(){
		return this.metadataXlinkId;
	}
	public int getMetadataId(){
		return this.metadataId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public MetadataXlink(int id){
		this.metadataXlinkId=id;
	}
	public MetadataXlink(int metadataXlinkId, String value, String xlink, int metadataId){
		this.metadataXlinkId=metadataXlinkId;
		this.value=value;
		this.xlink=xlink;
		this.metadataId=metadataId;
	}
}
