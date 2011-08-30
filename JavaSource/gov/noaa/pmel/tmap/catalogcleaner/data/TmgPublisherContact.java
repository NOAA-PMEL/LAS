package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgPublisherContact {
	protected int tmgPublisherId;
	protected int tmgPublisherContactId;
	protected Datavalue email = new Datavalue(null);
	protected Datavalue url = new Datavalue(null);
	public void setTmgPublisherId(int tmgPublisherId){
		this.tmgPublisherId = tmgPublisherId;
	}
	public void setTmgPublisherContactId(int tmgPublisherContactId){
		this.tmgPublisherContactId = tmgPublisherContactId;
	}
	public void setEmail(String email){
		this.email = new Datavalue(email);
	}
	public void setUrl(String url){
		this.url = new Datavalue(url);
	}
	public int getTmgPublisherId(){
		return this.tmgPublisherId;
	}
	public int getTmgPublisherContactId(){
		return this.tmgPublisherContactId;
	}
	public Datavalue getEmail(){
		return this.email;
	}
	public Datavalue getUrl(){
		return this.url;
	}

	public TmgPublisherContact(){
		this.tmgPublisherContactId = -1;
	}
	public TmgPublisherContact(int tmgPublisherContact){
		this.tmgPublisherContactId = tmgPublisherContact;
	}
	public TmgPublisherContact(int tmgPublisherId, int tmgPublisherContactId, Datavalue email, Datavalue url){
		this.tmgPublisherId = tmgPublisherId;
		this.tmgPublisherContactId = tmgPublisherContactId;
		this.email=email;
		this.url=url;
	}
	public TmgPublisherContact clone(){
		TmgPublisherContact clone = new TmgPublisherContact(this.tmgPublisherId, -1, this.email, this.url);
		return clone;
	}
}
