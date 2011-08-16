package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgCreatorContact {
	protected int tmgCreatorContactId;
	protected int tmgCreatorId;
	protected String email;
	protected String url;
	public void setTmgCreatorId(int tmgCreatorId){
		this.tmgCreatorId = tmgCreatorId;
	}
	public void setEmail(String email){
		this.email = email;
	}
	public void setUrl(String url){
		this.url = url;
	}
	public int getTmgCreatorContactId(){
		return this.tmgCreatorContactId;
	}
	public int getTmgCreatorId(){
		return this.tmgCreatorId;
	}
	public String getEmail(){
		return this.email;
	}
	public String getUrl(){
		return this.url;
	}

	public TmgCreatorContact(int id){
		this.tmgCreatorContactId=id;
	}
	public TmgCreatorContact(int tmgCreatorContactId, String email, String url, int tmgCreatorId){
		this.tmgCreatorContactId=tmgCreatorContactId;
		this.email=email;
		this.url=url;
		this.tmgCreatorId=tmgCreatorId;
	}
}
