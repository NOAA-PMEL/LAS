package gov.noaa.pmel.tmap.catalogcleaner.data;

public class DatasetTmg extends Tmg {
	private int parentId;

	public int getParentId(){
		return this.parentId;
	}

	public DatasetTmg(int parentId, int tmgId){
		super(tmgId);
		this.parentId = parentId;
	}
}

