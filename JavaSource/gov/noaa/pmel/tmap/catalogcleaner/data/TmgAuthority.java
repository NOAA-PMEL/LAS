package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgAuthority {
	protected int tmgId;
	protected int tmgAuthorityId;
	protected String authority;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgAuthorityId(int tmgAuthorityId){
		this.tmgAuthorityId = tmgAuthorityId;
	}
	public void setAuthority(String authority){
		this.authority = authority;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgAuthorityId(){
		return this.tmgAuthorityId;
	}
	public String getAuthority(){
		return this.authority;
	}

	public TmgAuthority(int tmgAuthority){
		this.tmgAuthorityId = tmgAuthority;
	}
	public TmgAuthority(int tmgId, int tmgAuthorityId, String authority){
		this.tmgId = tmgId;
		this.tmgAuthorityId = tmgAuthorityId;
		this.authority=authority;
	}
}
