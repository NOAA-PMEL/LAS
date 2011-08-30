package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class MetadataXlink {
	protected int metadataId;
	protected int metadataXlinkId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue xlink = new Datavalue(null);
	public void setMetadataId(int metadataId){
		this.metadataId = metadataId;
	}
	public void setMetadataXlinkId(int metadataXlinkId){
		this.metadataXlinkId = metadataXlinkId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setXlink(String xlink){
		this.xlink = new Datavalue(xlink);
	}
	public int getMetadataId(){
		return this.metadataId;
	}
	public int getMetadataXlinkId(){
		return this.metadataXlinkId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getXlink(){
		return this.xlink;
	}

	public MetadataXlink(){
		this.metadataXlinkId = -1;
	}
	public MetadataXlink(int metadataXlink){
		this.metadataXlinkId = metadataXlink;
	}
	public MetadataXlink(int metadataId, int metadataXlinkId, Datavalue value, Datavalue xlink){
		this.metadataId = metadataId;
		this.metadataXlinkId = metadataXlinkId;
		this.value=value;
		this.xlink=xlink;
	}
	public MetadataXlink clone(){
		MetadataXlink clone = new MetadataXlink(this.metadataId, -1, this.value, this.xlink);
		return clone;
	}
}
