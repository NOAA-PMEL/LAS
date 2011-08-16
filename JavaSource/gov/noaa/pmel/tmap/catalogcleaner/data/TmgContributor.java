package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgContributor {
	protected int tmgContributorId;
	protected int tmgId;
	protected String role;
	protected String name;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setRole(String role){
		this.role = role;
	}
	public void setName(String name){
		this.name = name;
	}
	public int getTmgContributorId(){
		return this.tmgContributorId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getRole(){
		return this.role;
	}
	public String getName(){
		return this.name;
	}

	public TmgContributor(int id){
		this.tmgContributorId=id;
	}
	public TmgContributor(int tmgContributorId, String role, String name, int tmgId){
		this.tmgContributorId=tmgContributorId;
		this.role=role;
		this.name=name;
		this.tmgId=tmgId;
	}
}
