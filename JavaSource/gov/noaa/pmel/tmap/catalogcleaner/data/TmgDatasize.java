package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDatasize {
	protected int tmgId;
	protected int tmgDatasizeId;
	protected String value;
	protected String units;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgDatasizeId(int tmgDatasizeId){
		this.tmgDatasizeId = tmgDatasizeId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setUnits(String units){
		this.units = units;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgDatasizeId(){
		return this.tmgDatasizeId;
	}
	public String getValue(){
		return this.value;
	}
	public String getUnits(){
		return this.units;
	}

	public TmgDatasize(int tmgDatasize){
		this.tmgDatasizeId = tmgDatasize;
	}
	public TmgDatasize(int tmgId, int tmgDatasizeId, String value, String units){
		this.tmgId = tmgId;
		this.tmgDatasizeId = tmgDatasizeId;
		this.value=value;
		this.units=units;
	}
}
