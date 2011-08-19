package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgCreatorContact {
	protected int tmgCreatorId;
	protected int tmgCreatorContactId;
	protected String email;
	protected String url;
	public void setTmgCreatorId(int tmgCreatorId){
		this.tmgCreatorId = tmgCreatorId;
	}
	public void setTmgCreatorContactId(int tmgCreatorContactId){
		this.tmgCreatorContactId = tmgCreatorContactId;
	}
	public void setEmail(String email){
		this.email = email;
	}
	public void setUrl(String url){
		this.url = url;
	}
	public int getTmgCreatorId(){
		return this.tmgCreatorId;
	}
	public int getTmgCreatorContactId(){
		return this.tmgCreatorContactId;
	}
	public String getEmail(){
		return this.email;
	}
	public String getUrl(){
		return this.url;
	}

	public TmgCreatorContact(int tmgCreatorContact){
		this.tmgCreatorContactId = tmgCreatorContact;
	}
	public TmgCreatorContact(int tmgCreatorId, int tmgCreatorContactId, String email, String url){
		this.tmgCreatorId = tmgCreatorId;
		this.tmgCreatorContactId = tmgCreatorContactId;
		this.email=email;
		this.url=url;
	}
}
