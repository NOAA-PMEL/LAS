package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDataformat {
	protected int tmgDataformatId;
	protected int tmgId;
	protected String dataformat;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setDataformat(String dataformat){
		this.dataformat = dataformat;
	}
	public int getTmgDataformatId(){
		return this.tmgDataformatId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getDataformat(){
		return this.dataformat;
	}

	public TmgDataformat(int id){
		this.tmgDataformatId=id;
	}
	public TmgDataformat(int tmgDataformatId, String dataformat, int tmgId){
		this.tmgDataformatId=tmgDataformatId;
		this.dataformat=dataformat;
		this.tmgId=tmgId;
	}
}
