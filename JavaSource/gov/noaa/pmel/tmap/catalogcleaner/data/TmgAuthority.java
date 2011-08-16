package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgAuthority {
	protected int tmgAuthorityId;
	protected int tmgId;
	protected String authority;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setAuthority(String authority){
		this.authority = authority;
	}
	public int getTmgAuthorityId(){
		return this.tmgAuthorityId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getAuthority(){
		return this.authority;
	}

	public TmgAuthority(int id){
		this.tmgAuthorityId=id;
	}
	public TmgAuthority(int tmgAuthorityId, String authority, int tmgId){
		this.tmgAuthorityId=tmgAuthorityId;
		this.authority=authority;
		this.tmgId=tmgId;
	}
}
