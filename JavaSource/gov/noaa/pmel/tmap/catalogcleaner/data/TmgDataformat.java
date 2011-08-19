package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDataformat {
	protected int tmgId;
	protected int tmgDataformatId;
	protected String dataformat;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgDataformatId(int tmgDataformatId){
		this.tmgDataformatId = tmgDataformatId;
	}
	public void setDataformat(String dataformat){
		this.dataformat = dataformat;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgDataformatId(){
		return this.tmgDataformatId;
	}
	public String getDataformat(){
		return this.dataformat;
	}

	public TmgDataformat(int tmgDataformat){
		this.tmgDataformatId = tmgDataformat;
	}
	public TmgDataformat(int tmgId, int tmgDataformatId, String dataformat){
		this.tmgId = tmgId;
		this.tmgDataformatId = tmgDataformatId;
		this.dataformat=dataformat;
	}
}
