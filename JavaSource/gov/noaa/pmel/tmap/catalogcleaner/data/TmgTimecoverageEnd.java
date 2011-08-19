package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgTimecoverageEnd {
	protected int tmgTimecoverageId;
	protected int tmgTimecoverageEndId;
	protected String format;
	protected String value;
	protected String dateenum;
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setTmgTimecoverageEndId(int tmgTimecoverageEndId){
		this.tmgTimecoverageEndId = tmgTimecoverageEndId;
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
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public int getTmgTimecoverageEndId(){
		return this.tmgTimecoverageEndId;
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

	public TmgTimecoverageEnd(int tmgTimecoverageEnd){
		this.tmgTimecoverageEndId = tmgTimecoverageEnd;
	}
	public TmgTimecoverageEnd(int tmgTimecoverageId, int tmgTimecoverageEndId, String format, String value, String dateenum){
		this.tmgTimecoverageId = tmgTimecoverageId;
		this.tmgTimecoverageEndId = tmgTimecoverageEndId;
		this.format=format;
		this.value=value;
		this.dateenum=dateenum;
	}
}
