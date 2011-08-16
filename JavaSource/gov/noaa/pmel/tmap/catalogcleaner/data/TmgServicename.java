package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgServicename {
	protected int tmgServicenameId;
	protected int tmgId;
	protected String servicename;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setServicename(String servicename){
		this.servicename = servicename;
	}
	public int getTmgServicenameId(){
		return this.tmgServicenameId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getServicename(){
		return this.servicename;
	}

	public TmgServicename(int id){
		this.tmgServicenameId=id;
	}
	public TmgServicename(int tmgServicenameId, String servicename, int tmgId){
		this.tmgServicenameId=tmgServicenameId;
		this.servicename=servicename;
		this.tmgId=tmgId;
	}
}
