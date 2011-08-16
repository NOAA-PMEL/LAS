package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgPublisher {
	protected int tmgPublisherId;
	protected int tmgId;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public int getTmgPublisherId(){
		return this.tmgPublisherId;
	}
	public int getTmgId(){
		return this.tmgId;
	}

	public TmgPublisher(int id){
		this.tmgPublisherId=id;
	}
	public TmgPublisher(int tmgPublisherId, int tmgId){
		this.tmgPublisherId=tmgPublisherId;
		this.tmgId=tmgId;
	}
}
