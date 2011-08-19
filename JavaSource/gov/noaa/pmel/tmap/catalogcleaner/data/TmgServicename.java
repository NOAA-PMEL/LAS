package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgServicename {
	protected int tmgId;
	protected int tmgServicenameId;
	protected String servicename;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgServicenameId(int tmgServicenameId){
		this.tmgServicenameId = tmgServicenameId;
	}
	public void setServicename(String servicename){
		this.servicename = servicename;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgServicenameId(){
		return this.tmgServicenameId;
	}
	public String getServicename(){
		return this.servicename;
	}

	public TmgServicename(int tmgServicename){
		this.tmgServicenameId = tmgServicename;
	}
	public TmgServicename(int tmgId, int tmgServicenameId, String servicename){
		this.tmgId = tmgId;
		this.tmgServicenameId = tmgServicenameId;
		this.servicename=servicename;
	}
}
