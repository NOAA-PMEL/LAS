package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class Tmg {
	protected int tmgId;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public int getTmgId(){
		return this.tmgId;
	}

	public Tmg(){
		this.tmgId = -1;
	}
	public Tmg(int tmg){
		this.tmgId = tmg;
	}
	public Tmg clone(){
		Tmg clone = new Tmg(-1);
		return clone;
	}
}
