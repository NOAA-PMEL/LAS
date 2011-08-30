package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgMetadata extends Metadata {
	private int parentId;

	public int getParentId(){
		return this.parentId;
	}

	public int getChildId(){
		return this.metadataId;
	}

	public TmgMetadata(int parentId, int metadataId){
		super(metadataId);
		this.parentId = parentId;
	}
	public TmgMetadata(int parentId, int metadataId, Datavalue inherited, Datavalue metadatatype){
		super(metadataId, inherited, metadatatype);
		this.parentId = parentId;
	}
}

