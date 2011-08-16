package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgTimecoverageDuration {
	protected int tmgTimecoverageDurationId;
	protected int tmgTimecoverageId;
	protected String duration;
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setDuration(String duration){
		this.duration = duration;
	}
	public int getTmgTimecoverageDurationId(){
		return this.tmgTimecoverageDurationId;
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public String getDuration(){
		return this.duration;
	}

	public TmgTimecoverageDuration(int id){
		this.tmgTimecoverageDurationId=id;
	}
	public TmgTimecoverageDuration(int tmgTimecoverageDurationId, String duration, int tmgTimecoverageId){
		this.tmgTimecoverageDurationId=tmgTimecoverageDurationId;
		this.duration=duration;
		this.tmgTimecoverageId=tmgTimecoverageId;
	}
}
