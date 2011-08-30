package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgDataformat {
	protected int tmgId;
	protected int tmgDataformatId;
	protected Datavalue dataformat = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgDataformatId(int tmgDataformatId){
		this.tmgDataformatId = tmgDataformatId;
	}
	public void setDataformat(String dataformat){
		this.dataformat = new Datavalue(dataformat);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgDataformatId(){
		return this.tmgDataformatId;
	}
	public Datavalue getDataformat(){
		return this.dataformat;
	}

	public TmgDataformat(){
		this.tmgDataformatId = -1;
	}
	public TmgDataformat(int tmgDataformat){
		this.tmgDataformatId = tmgDataformat;
	}
	public TmgDataformat(int tmgId, int tmgDataformatId, Datavalue dataformat){
		this.tmgId = tmgId;
		this.tmgDataformatId = tmgDataformatId;
		this.dataformat=dataformat;
	}
	public TmgDataformat clone(){
		TmgDataformat clone = new TmgDataformat(this.tmgId, -1, this.dataformat);
		return clone;
	}
}
