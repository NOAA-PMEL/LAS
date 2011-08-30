package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgContributor {
	protected int tmgId;
	protected int tmgContributorId;
	protected Datavalue name = new Datavalue(null);
	protected Datavalue role = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgContributorId(int tmgContributorId){
		this.tmgContributorId = tmgContributorId;
	}
	public void setName(String name){
		this.name = new Datavalue(name);
	}
	public void setRole(String role){
		this.role = new Datavalue(role);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgContributorId(){
		return this.tmgContributorId;
	}
	public Datavalue getName(){
		return this.name;
	}
	public Datavalue getRole(){
		return this.role;
	}

	public TmgContributor(){
		this.tmgContributorId = -1;
	}
	public TmgContributor(int tmgContributor){
		this.tmgContributorId = tmgContributor;
	}
	public TmgContributor(int tmgId, int tmgContributorId, Datavalue name, Datavalue role){
		this.tmgId = tmgId;
		this.tmgContributorId = tmgContributorId;
		this.name=name;
		this.role=role;
	}
	public TmgContributor clone(){
		TmgContributor clone = new TmgContributor(this.tmgId, -1, this.name, this.role);
		return clone;
	}
}
