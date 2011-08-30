package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgTimecoverageStart {
	protected int tmgTimecoverageId;
	protected int tmgTimecoverageStartId;
	protected Datavalue format = new Datavalue(null);
	protected Datavalue value = new Datavalue(null);
	protected Datavalue dateenum = new Datavalue(null);
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setTmgTimecoverageStartId(int tmgTimecoverageStartId){
		this.tmgTimecoverageStartId = tmgTimecoverageStartId;
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
	public int getTmgTimecoverageStartId(){
		return this.tmgTimecoverageStartId;
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

	public TmgTimecoverageStart(){
		this.tmgTimecoverageStartId = -1;
	}
	public TmgTimecoverageStart(int tmgTimecoverageStart){
		this.tmgTimecoverageStartId = tmgTimecoverageStart;
	}
	public TmgTimecoverageStart(int tmgTimecoverageId, int tmgTimecoverageStartId, Datavalue format, Datavalue value, Datavalue dateenum){
		this.tmgTimecoverageId = tmgTimecoverageId;
		this.tmgTimecoverageStartId = tmgTimecoverageStartId;
		this.format=format;
		this.value=value;
		this.dateenum=dateenum;
	}
	public TmgTimecoverageStart clone(){
		TmgTimecoverageStart clone = new TmgTimecoverageStart(this.tmgTimecoverageId, -1, this.format, this.value, this.dateenum);
		return clone;
	}
}
