package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class Metadata {
	protected int metadataId;
	protected Datavalue inherited = new Datavalue(null);
	protected Datavalue metadatatype = new Datavalue(null);
	public void setMetadataId(int metadataId){
		this.metadataId = metadataId;
	}
	public void setInherited(String inherited){
		this.inherited = new Datavalue(inherited);
	}
	public void setMetadatatype(String metadatatype){
		this.metadatatype = new Datavalue(metadatatype);
	}
	public int getMetadataId(){
		return this.metadataId;
	}
	public Datavalue getInherited(){
		return this.inherited;
	}
	public Datavalue getMetadatatype(){
		return this.metadatatype;
	}

	public Metadata(){
		this.metadataId = -1;
	}
	public Metadata(int metadata){
		this.metadataId = metadata;
	}
	public Metadata(int metadataId, Datavalue inherited, Datavalue metadatatype){
		this.metadataId = metadataId;
		this.inherited=inherited;
		this.metadatatype=metadatatype;
	}
	public Metadata clone(){
		Metadata clone = new Metadata(-1, this.inherited, this.metadatatype);
		return clone;
	}
}
