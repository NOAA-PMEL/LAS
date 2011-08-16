package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgMetadata extends Metadata {
	private int parentId;

	public int getParentId(){
		return this.parentId;
	}

	public TmgMetadata(int parentId, int metadataId){
		super(metadataId);
		this.parentId = parentId;
	}
	public TmgMetadata(int parentId, int metadataId, String metadatatype, String inherited){
		super(metadataId, metadatatype, inherited);
		this.parentId = parentId;
	}
}

