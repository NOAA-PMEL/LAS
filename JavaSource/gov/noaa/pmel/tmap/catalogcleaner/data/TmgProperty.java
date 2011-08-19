package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgProperty {
	protected int tmgId;
	protected int tmgPropertyId;
	protected String name;
	protected String value;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgPropertyId(int tmgPropertyId){
		this.tmgPropertyId = tmgPropertyId;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setValue(String value){
		this.value = value;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgPropertyId(){
		return this.tmgPropertyId;
	}
	public String getName(){
		return this.name;
	}
	public String getValue(){
		return this.value;
	}

	public TmgProperty(int tmgProperty){
		this.tmgPropertyId = tmgProperty;
	}
	public TmgProperty(int tmgId, int tmgPropertyId, String name, String value){
		this.tmgId = tmgId;
		this.tmgPropertyId = tmgPropertyId;
		this.name=name;
		this.value=value;
	}
}
