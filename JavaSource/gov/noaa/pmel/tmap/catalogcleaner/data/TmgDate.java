package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgDate {
	protected int tmgId;
	protected int tmgDateId;
	protected Datavalue format = new Datavalue(null);
	protected Datavalue value = new Datavalue(null);
	protected Datavalue dateenum = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgDateId(int tmgDateId){
		this.tmgDateId = tmgDateId;
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
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgDateId(){
		return this.tmgDateId;
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

	public TmgDate(){
		this.tmgDateId = -1;
	}
	public TmgDate(int tmgDate){
		this.tmgDateId = tmgDate;
	}
	public TmgDate(int tmgId, int tmgDateId, Datavalue format, Datavalue value, Datavalue dateenum){
		this.tmgId = tmgId;
		this.tmgDateId = tmgDateId;
		this.format=format;
		this.value=value;
		this.dateenum=dateenum;
	}
	public TmgDate clone(){
		TmgDate clone = new TmgDate(this.tmgId, -1, this.format, this.value, this.dateenum);
		return clone;
	}
}
