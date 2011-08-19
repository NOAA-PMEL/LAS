package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgPublisherContact {
	protected int tmgPublisherId;
	protected int tmgPublisherContactId;
	protected String url;
	protected String email;
	public void setTmgPublisherId(int tmgPublisherId){
		this.tmgPublisherId = tmgPublisherId;
	}
	public void setTmgPublisherContactId(int tmgPublisherContactId){
		this.tmgPublisherContactId = tmgPublisherContactId;
	}
	public void setUrl(String url){
		this.url = url;
	}
	public void setEmail(String email){
		this.email = email;
	}
	public int getTmgPublisherId(){
		return this.tmgPublisherId;
	}
	public int getTmgPublisherContactId(){
		return this.tmgPublisherContactId;
	}
	public String getUrl(){
		return this.url;
	}
	public String getEmail(){
		return this.email;
	}

	public TmgPublisherContact(int tmgPublisherContact){
		this.tmgPublisherContactId = tmgPublisherContact;
	}
	public TmgPublisherContact(int tmgPublisherId, int tmgPublisherContactId, String url, String email){
		this.tmgPublisherId = tmgPublisherId;
		this.tmgPublisherContactId = tmgPublisherContactId;
		this.url=url;
		this.email=email;
	}
}
