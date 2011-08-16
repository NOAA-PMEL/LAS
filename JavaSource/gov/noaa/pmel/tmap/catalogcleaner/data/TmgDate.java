package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDate {
	protected int tmgDateId;
	protected int tmgId;
	protected String format;
	protected String value;
	protected String dateenum;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
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
	public int getTmgDateId(){
		return this.tmgDateId;
	}
	public int getTmgId(){
		return this.tmgId;
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

	public TmgDate(int id){
		this.tmgDateId=id;
	}
	public TmgDate(int tmgDateId, String format, String value, String dateenum, int tmgId){
		this.tmgDateId=tmgDateId;
		this.format=format;
		this.value=value;
		this.dateenum=dateenum;
		this.tmgId=tmgId;
	}
}
