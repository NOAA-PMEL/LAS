package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgVariablesVariablemap {
	protected int tmgVariablesId;
	protected int tmgVariablesVariablemapId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue xlink = new Datavalue(null);
	public void setTmgVariablesId(int tmgVariablesId){
		this.tmgVariablesId = tmgVariablesId;
	}
	public void setTmgVariablesVariablemapId(int tmgVariablesVariablemapId){
		this.tmgVariablesVariablemapId = tmgVariablesVariablemapId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setXlink(String xlink){
		this.xlink = new Datavalue(xlink);
	}
	public int getTmgVariablesId(){
		return this.tmgVariablesId;
	}
	public int getTmgVariablesVariablemapId(){
		return this.tmgVariablesVariablemapId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getXlink(){
		return this.xlink;
	}

	public TmgVariablesVariablemap(){
		this.tmgVariablesVariablemapId = -1;
	}
	public TmgVariablesVariablemap(int tmgVariablesVariablemap){
		this.tmgVariablesVariablemapId = tmgVariablesVariablemap;
	}
	public TmgVariablesVariablemap(int tmgVariablesId, int tmgVariablesVariablemapId, Datavalue value, Datavalue xlink){
		this.tmgVariablesId = tmgVariablesId;
		this.tmgVariablesVariablemapId = tmgVariablesVariablemapId;
		this.value=value;
		this.xlink=xlink;
	}
	public TmgVariablesVariablemap clone(){
		TmgVariablesVariablemap clone = new TmgVariablesVariablemap(this.tmgVariablesId, -1, this.value, this.xlink);
		return clone;
	}
}
