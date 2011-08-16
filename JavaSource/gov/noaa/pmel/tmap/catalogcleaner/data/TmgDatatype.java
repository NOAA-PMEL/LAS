package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDatatype {
	protected int tmgDatatypeId;
	protected int tmgId;
	protected String datatype;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setDatatype(String datatype){
		this.datatype = datatype;
	}
	public int getTmgDatatypeId(){
		return this.tmgDatatypeId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getDatatype(){
		return this.datatype;
	}

	public TmgDatatype(int id){
		this.tmgDatatypeId=id;
	}
	public TmgDatatype(int tmgDatatypeId, String datatype, int tmgId){
		this.tmgDatatypeId=tmgDatatypeId;
		this.datatype=datatype;
		this.tmgId=tmgId;
	}
}
