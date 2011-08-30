package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgDatatype {
	protected int tmgId;
	protected int tmgDatatypeId;
	protected Datavalue datatype = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgDatatypeId(int tmgDatatypeId){
		this.tmgDatatypeId = tmgDatatypeId;
	}
	public void setDatatype(String datatype){
		this.datatype = new Datavalue(datatype);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgDatatypeId(){
		return this.tmgDatatypeId;
	}
	public Datavalue getDatatype(){
		return this.datatype;
	}

	public TmgDatatype(){
		this.tmgDatatypeId = -1;
	}
	public TmgDatatype(int tmgDatatype){
		this.tmgDatatypeId = tmgDatatype;
	}
	public TmgDatatype(int tmgId, int tmgDatatypeId, Datavalue datatype){
		this.tmgId = tmgId;
		this.tmgDatatypeId = tmgDatatypeId;
		this.datatype=datatype;
	}
	public TmgDatatype clone(){
		TmgDatatype clone = new TmgDatatype(this.tmgId, -1, this.datatype);
		return clone;
	}
}
