package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgDatasize {
	protected int tmgId;
	protected int tmgDatasizeId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue units = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgDatasizeId(int tmgDatasizeId){
		this.tmgDatasizeId = tmgDatasizeId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setUnits(String units){
		this.units = new Datavalue(units);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgDatasizeId(){
		return this.tmgDatasizeId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getUnits(){
		return this.units;
	}

	public TmgDatasize(){
		this.tmgDatasizeId = -1;
	}
	public TmgDatasize(int tmgDatasize){
		this.tmgDatasizeId = tmgDatasize;
	}
	public TmgDatasize(int tmgId, int tmgDatasizeId, Datavalue value, Datavalue units){
		this.tmgId = tmgId;
		this.tmgDatasizeId = tmgDatasizeId;
		this.value=value;
		this.units=units;
	}
	public TmgDatasize clone(){
		TmgDatasize clone = new TmgDatasize(this.tmgId, -1, this.value, this.units);
		return clone;
	}
}
