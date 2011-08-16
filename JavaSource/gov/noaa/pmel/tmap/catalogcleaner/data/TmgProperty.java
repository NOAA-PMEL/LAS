package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgProperty {
	protected int tmgPropertyId;
	protected int tmgId;
	protected String name;
	protected String value;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setValue(String value){
		this.value = value;
	}
	public int getTmgPropertyId(){
		return this.tmgPropertyId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getName(){
		return this.name;
	}
	public String getValue(){
		return this.value;
	}

	public TmgProperty(int id){
		this.tmgPropertyId=id;
	}
	public TmgProperty(int tmgPropertyId, String name, String value, int tmgId){
		this.tmgPropertyId=tmgPropertyId;
		this.name=name;
		this.value=value;
		this.tmgId=tmgId;
	}
}
