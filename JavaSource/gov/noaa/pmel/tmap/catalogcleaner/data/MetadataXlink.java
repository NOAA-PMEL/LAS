package gov.noaa.pmel.tmap.catalogcleaner.data;

public class MetadataXlink {
	protected int metadataId;
	protected int metadataXlinkId;
	protected String value;
	protected String xlink;
	public void setMetadataId(int metadataId){
		this.metadataId = metadataId;
	}
	public void setMetadataXlinkId(int metadataXlinkId){
		this.metadataXlinkId = metadataXlinkId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getMetadataId(){
		return this.metadataId;
	}
	public int getMetadataXlinkId(){
		return this.metadataXlinkId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public MetadataXlink(int metadataXlink){
		this.metadataXlinkId = metadataXlink;
	}
	public MetadataXlink(int metadataId, int metadataXlinkId, String value, String xlink){
		this.metadataId = metadataId;
		this.metadataXlinkId = metadataXlinkId;
		this.value=value;
		this.xlink=xlink;
	}
}
