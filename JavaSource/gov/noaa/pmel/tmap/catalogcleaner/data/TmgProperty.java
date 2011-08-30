package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgProperty {
	protected int tmgId;
	protected int tmgPropertyId;
	protected Datavalue name = new Datavalue(null);
	protected Datavalue value = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgPropertyId(int tmgPropertyId){
		this.tmgPropertyId = tmgPropertyId;
	}
	public void setName(String name){
		this.name = new Datavalue(name);
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgPropertyId(){
		return this.tmgPropertyId;
	}
	public Datavalue getName(){
		return this.name;
	}
	public Datavalue getValue(){
		return this.value;
	}

	public TmgProperty(){
		this.tmgPropertyId = -1;
	}
	public TmgProperty(int tmgProperty){
		this.tmgPropertyId = tmgProperty;
	}
	public TmgProperty(int tmgId, int tmgPropertyId, Datavalue name, Datavalue value){
		this.tmgId = tmgId;
		this.tmgPropertyId = tmgPropertyId;
		this.name=name;
		this.value=value;
	}
	public TmgProperty clone(){
		TmgProperty clone = new TmgProperty(this.tmgId, -1, this.name, this.value);
		return clone;
	}
}
