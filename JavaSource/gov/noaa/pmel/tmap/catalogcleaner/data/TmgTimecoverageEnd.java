package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgTimecoverageEnd {
	protected int tmgTimecoverageId;
	protected int tmgTimecoverageEndId;
	protected Datavalue format = new Datavalue(null);
	protected Datavalue value = new Datavalue(null);
	protected Datavalue dateenum = new Datavalue(null);
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setTmgTimecoverageEndId(int tmgTimecoverageEndId){
		this.tmgTimecoverageEndId = tmgTimecoverageEndId;
	}
	public void setFormat(String format){
		this.format = new Datavalue(format);
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setDateenum(String dateenum){
		this.dateenum = new Datavalue(dateenum);
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public int getTmgTimecoverageEndId(){
		return this.tmgTimecoverageEndId;
	}
	public Datavalue getFormat(){
		return this.format;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getDateenum(){
		return this.dateenum;
	}

	public TmgTimecoverageEnd(){
		this.tmgTimecoverageEndId = -1;
	}
	public TmgTimecoverageEnd(int tmgTimecoverageEnd){
		this.tmgTimecoverageEndId = tmgTimecoverageEnd;
	}
	public TmgTimecoverageEnd(int tmgTimecoverageId, int tmgTimecoverageEndId, Datavalue format, Datavalue value, Datavalue dateenum){
		this.tmgTimecoverageId = tmgTimecoverageId;
		this.tmgTimecoverageEndId = tmgTimecoverageEndId;
		this.format=format;
		this.value=value;
		this.dateenum=dateenum;
	}
	public TmgTimecoverageEnd clone(){
		TmgTimecoverageEnd clone = new TmgTimecoverageEnd(this.tmgTimecoverageId, -1, this.format, this.value, this.dateenum);
		return clone;
	}
}
