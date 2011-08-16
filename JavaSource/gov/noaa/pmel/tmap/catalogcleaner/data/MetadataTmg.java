package gov.noaa.pmel.tmap.catalogcleaner.data;

public class MetadataTmg extends Tmg {
	private int parentId;

	public int getParentId(){
		return this.parentId;
	}

	public MetadataTmg(int parentId, int tmgId){
		super(tmgId);
		this.parentId = parentId;
	}
}

