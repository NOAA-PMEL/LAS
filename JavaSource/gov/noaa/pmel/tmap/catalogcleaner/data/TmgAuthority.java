package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgAuthority {
	protected int tmgId;
	protected int tmgAuthorityId;
	protected Datavalue authority = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgAuthorityId(int tmgAuthorityId){
		this.tmgAuthorityId = tmgAuthorityId;
	}
	public void setAuthority(String authority){
		this.authority = new Datavalue(authority);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgAuthorityId(){
		return this.tmgAuthorityId;
	}
	public Datavalue getAuthority(){
		return this.authority;
	}

	public TmgAuthority(){
		this.tmgAuthorityId = -1;
	}
	public TmgAuthority(int tmgAuthority){
		this.tmgAuthorityId = tmgAuthority;
	}
	public TmgAuthority(int tmgId, int tmgAuthorityId, Datavalue authority){
		this.tmgId = tmgId;
		this.tmgAuthorityId = tmgAuthorityId;
		this.authority=authority;
	}
	public TmgAuthority clone(){
		TmgAuthority clone = new TmgAuthority(this.tmgId, -1, this.authority);
		return clone;
	}
}
