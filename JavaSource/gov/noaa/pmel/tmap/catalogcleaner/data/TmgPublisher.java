package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgPublisher {
	protected int tmgId;
	protected int tmgPublisherId;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgPublisherId(int tmgPublisherId){
		this.tmgPublisherId = tmgPublisherId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgPublisherId(){
		return this.tmgPublisherId;
	}

	public TmgPublisher(int tmgPublisher){
		this.tmgPublisherId = tmgPublisher;
	}
	public TmgPublisher(int tmgId, int tmgPublisherId){
		this.tmgId = tmgId;
		this.tmgPublisherId = tmgPublisherId;
	}
}
