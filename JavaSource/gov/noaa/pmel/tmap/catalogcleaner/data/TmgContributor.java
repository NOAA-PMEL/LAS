package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgContributor {
	protected int tmgId;
	protected int tmgContributorId;
	protected String role;
	protected String name;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgContributorId(int tmgContributorId){
		this.tmgContributorId = tmgContributorId;
	}
	public void setRole(String role){
		this.role = role;
	}
	public void setName(String name){
		this.name = name;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgContributorId(){
		return this.tmgContributorId;
	}
	public String getRole(){
		return this.role;
	}
	public String getName(){
		return this.name;
	}

	public TmgContributor(int tmgContributor){
		this.tmgContributorId = tmgContributor;
	}
	public TmgContributor(int tmgId, int tmgContributorId, String role, String name){
		this.tmgId = tmgId;
		this.tmgContributorId = tmgContributorId;
		this.role=role;
		this.name=name;
	}
}
