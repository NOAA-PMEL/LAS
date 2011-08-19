package gov.noaa.pmel.tmap.catalogcleaner.data;

public class Tmg {
	protected int tmgId;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public int getTmgId(){
		return this.tmgId;
	}

	public Tmg(int tmg){
		this.tmgId = tmg;
	}
}
