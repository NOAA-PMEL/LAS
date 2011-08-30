package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgCreatorContact {
	protected int tmgCreatorId;
	protected int tmgCreatorContactId;
	protected Datavalue email = new Datavalue(null);
	protected Datavalue url = new Datavalue(null);
	public void setTmgCreatorId(int tmgCreatorId){
		this.tmgCreatorId = tmgCreatorId;
	}
	public void setTmgCreatorContactId(int tmgCreatorContactId){
		this.tmgCreatorContactId = tmgCreatorContactId;
	}
	public void setEmail(String email){
		this.email = new Datavalue(email);
	}
	public void setUrl(String url){
		this.url = new Datavalue(url);
	}
	public int getTmgCreatorId(){
		return this.tmgCreatorId;
	}
	public int getTmgCreatorContactId(){
		return this.tmgCreatorContactId;
	}
	public Datavalue getEmail(){
		return this.email;
	}
	public Datavalue getUrl(){
		return this.url;
	}

	public TmgCreatorContact(){
		this.tmgCreatorContactId = -1;
	}
	public TmgCreatorContact(int tmgCreatorContact){
		this.tmgCreatorContactId = tmgCreatorContact;
	}
	public TmgCreatorContact(int tmgCreatorId, int tmgCreatorContactId, Datavalue email, Datavalue url){
		this.tmgCreatorId = tmgCreatorId;
		this.tmgCreatorContactId = tmgCreatorContactId;
		this.email=email;
		this.url=url;
	}
	public TmgCreatorContact clone(){
		TmgCreatorContact clone = new TmgCreatorContact(this.tmgCreatorId, -1, this.email, this.url);
		return clone;
	}
}
