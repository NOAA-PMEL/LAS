package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgVariablesVariablemap {
	protected int tmgVariablesVariablemapId;
	protected int tmgVariablesId;
	protected String value;
	protected String xlink;
	public void setTmgVariablesId(int tmgVariablesId){
		this.tmgVariablesId = tmgVariablesId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getTmgVariablesVariablemapId(){
		return this.tmgVariablesVariablemapId;
	}
	public int getTmgVariablesId(){
		return this.tmgVariablesId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public TmgVariablesVariablemap(int id){
		this.tmgVariablesVariablemapId=id;
	}
	public TmgVariablesVariablemap(int tmgVariablesVariablemapId, String value, String xlink, int tmgVariablesId){
		this.tmgVariablesVariablemapId=tmgVariablesVariablemapId;
		this.value=value;
		this.xlink=xlink;
		this.tmgVariablesId=tmgVariablesId;
	}
}
