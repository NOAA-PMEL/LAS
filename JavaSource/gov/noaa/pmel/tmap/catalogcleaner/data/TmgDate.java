package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDate {
	protected int tmgId;
	protected int tmgDateId;
	protected String format;
	protected String value;
	protected String dateenum;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgDateId(int tmgDateId){
		this.tmgDateId = tmgDateId;
	}
	public void setFormat(String format){
		this.format = format;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setDateenum(String dateenum){
		this.dateenum = dateenum;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgDateId(){
		return this.tmgDateId;
	}
	public String getFormat(){
		return this.format;
	}
	public String getValue(){
		return this.value;
	}
	public String getDateenum(){
		return this.dateenum;
	}

	public TmgDate(int tmgDate){
		this.tmgDateId = tmgDate;
	}
	public TmgDate(int tmgId, int tmgDateId, String format, String value, String dateenum){
		this.tmgId = tmgId;
		this.tmgDateId = tmgDateId;
		this.format=format;
		this.value=value;
		this.dateenum=dateenum;
	}
}
