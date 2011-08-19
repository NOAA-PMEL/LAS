package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgVariablesVariablemap {
	protected int tmgVariablesId;
	protected int tmgVariablesVariablemapId;
	protected String value;
	protected String xlink;
	public void setTmgVariablesId(int tmgVariablesId){
		this.tmgVariablesId = tmgVariablesId;
	}
	public void setTmgVariablesVariablemapId(int tmgVariablesVariablemapId){
		this.tmgVariablesVariablemapId = tmgVariablesVariablemapId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getTmgVariablesId(){
		return this.tmgVariablesId;
	}
	public int getTmgVariablesVariablemapId(){
		return this.tmgVariablesVariablemapId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public TmgVariablesVariablemap(int tmgVariablesVariablemap){
		this.tmgVariablesVariablemapId = tmgVariablesVariablemap;
	}
	public TmgVariablesVariablemap(int tmgVariablesId, int tmgVariablesVariablemapId, String value, String xlink){
		this.tmgVariablesId = tmgVariablesId;
		this.tmgVariablesVariablemapId = tmgVariablesVariablemapId;
		this.value=value;
		this.xlink=xlink;
	}
}
