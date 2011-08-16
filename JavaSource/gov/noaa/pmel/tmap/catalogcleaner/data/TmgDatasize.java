package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDatasize {
	protected int tmgDatasizeId;
	protected int tmgId;
	protected String value;
	protected String units;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setUnits(String units){
		this.units = units;
	}
	public int getTmgDatasizeId(){
		return this.tmgDatasizeId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getValue(){
		return this.value;
	}
	public String getUnits(){
		return this.units;
	}

	public TmgDatasize(int id){
		this.tmgDatasizeId=id;
	}
	public TmgDatasize(int tmgDatasizeId, String value, String units, int tmgId){
		this.tmgDatasizeId=tmgDatasizeId;
		this.value=value;
		this.units=units;
		this.tmgId=tmgId;
	}
}
