package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
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

	public TmgPublisher(){
		this.tmgPublisherId = -1;
	}
	public TmgPublisher(int tmgPublisher){
		this.tmgPublisherId = tmgPublisher;
	}
	public TmgPublisher(int tmgId, int tmgPublisherId){
		this.tmgId = tmgId;
		this.tmgPublisherId = tmgPublisherId;
	}
	public TmgPublisher clone(){
		TmgPublisher clone = new TmgPublisher(this.tmgId, -1);
		return clone;
	}
}
