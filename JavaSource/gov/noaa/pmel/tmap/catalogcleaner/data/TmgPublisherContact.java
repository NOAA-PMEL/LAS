package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgPublisherContact {
	protected int tmgPublisherContactId;
	protected int tmgPublisherId;
	protected String url;
	protected String email;
	public void setTmgPublisherId(int tmgPublisherId){
		this.tmgPublisherId = tmgPublisherId;
	}
	public void setUrl(String url){
		this.url = url;
	}
	public void setEmail(String email){
		this.email = email;
	}
	public int getTmgPublisherContactId(){
		return this.tmgPublisherContactId;
	}
	public int getTmgPublisherId(){
		return this.tmgPublisherId;
	}
	public String getUrl(){
		return this.url;
	}
	public String getEmail(){
		return this.email;
	}

	public TmgPublisherContact(int id){
		this.tmgPublisherContactId=id;
	}
	public TmgPublisherContact(int tmgPublisherContactId, String url, String email, int tmgPublisherId){
		this.tmgPublisherContactId=tmgPublisherContactId;
		this.url=url;
		this.email=email;
		this.tmgPublisherId=tmgPublisherId;
	}
}
