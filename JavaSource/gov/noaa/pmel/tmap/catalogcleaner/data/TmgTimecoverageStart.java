package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgTimecoverageStart {
	protected int tmgTimecoverageStartId;
	protected int tmgTimecoverageId;
	protected String format;
	protected String value;
	protected String dateenum;
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
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
	public int getTmgTimecoverageStartId(){
		return this.tmgTimecoverageStartId;
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
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

	public TmgTimecoverageStart(int id){
		this.tmgTimecoverageStartId=id;
	}
	public TmgTimecoverageStart(int tmgTimecoverageStartId, String format, String value, String dateenum, int tmgTimecoverageId){
		this.tmgTimecoverageStartId=tmgTimecoverageStartId;
		this.format=format;
		this.value=value;
		this.dateenum=dateenum;
		this.tmgTimecoverageId=tmgTimecoverageId;
	}
}
