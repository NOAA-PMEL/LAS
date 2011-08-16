package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgCreator {
	protected int tmgCreatorId;
	protected int tmgId;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public int getTmgCreatorId(){
		return this.tmgCreatorId;
	}
	public int getTmgId(){
		return this.tmgId;
	}

	public TmgCreator(int id){
		this.tmgCreatorId=id;
	}
	public TmgCreator(int tmgCreatorId, int tmgId){
		this.tmgCreatorId=tmgCreatorId;
		this.tmgId=tmgId;
	}
}
