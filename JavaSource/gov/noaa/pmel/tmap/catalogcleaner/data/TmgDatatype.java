package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDatatype {
	protected int tmgId;
	protected int tmgDatatypeId;
	protected String datatype;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgDatatypeId(int tmgDatatypeId){
		this.tmgDatatypeId = tmgDatatypeId;
	}
	public void setDatatype(String datatype){
		this.datatype = datatype;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgDatatypeId(){
		return this.tmgDatatypeId;
	}
	public String getDatatype(){
		return this.datatype;
	}

	public TmgDatatype(int tmgDatatype){
		this.tmgDatatypeId = tmgDatatype;
	}
	public TmgDatatype(int tmgId, int tmgDatatypeId, String datatype){
		this.tmgId = tmgId;
		this.tmgDatatypeId = tmgDatatypeId;
		this.datatype=datatype;
	}
}
