package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgServicename {
	protected int tmgId;
	protected int tmgServicenameId;
	protected Datavalue servicename = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgServicenameId(int tmgServicenameId){
		this.tmgServicenameId = tmgServicenameId;
	}
	public void setServicename(String servicename){
		this.servicename = new Datavalue(servicename);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgServicenameId(){
		return this.tmgServicenameId;
	}
	public Datavalue getServicename(){
		return this.servicename;
	}

	public TmgServicename(){
		this.tmgServicenameId = -1;
	}
	public TmgServicename(int tmgServicename){
		this.tmgServicenameId = tmgServicename;
	}
	public TmgServicename(int tmgId, int tmgServicenameId, Datavalue servicename){
		this.tmgId = tmgId;
		this.tmgServicenameId = tmgServicenameId;
		this.servicename=servicename;
	}
	public TmgServicename clone(){
		TmgServicename clone = new TmgServicename(this.tmgId, -1, this.servicename);
		return clone;
	}
}
