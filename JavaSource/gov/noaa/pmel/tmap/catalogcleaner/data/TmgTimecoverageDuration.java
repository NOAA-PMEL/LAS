package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgTimecoverageDuration {
	protected int tmgTimecoverageId;
	protected int tmgTimecoverageDurationId;
	protected String duration;
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setTmgTimecoverageDurationId(int tmgTimecoverageDurationId){
		this.tmgTimecoverageDurationId = tmgTimecoverageDurationId;
	}
	public void setDuration(String duration){
		this.duration = duration;
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public int getTmgTimecoverageDurationId(){
		return this.tmgTimecoverageDurationId;
	}
	public String getDuration(){
		return this.duration;
	}

	public TmgTimecoverageDuration(int tmgTimecoverageDuration){
		this.tmgTimecoverageDurationId = tmgTimecoverageDuration;
	}
	public TmgTimecoverageDuration(int tmgTimecoverageId, int tmgTimecoverageDurationId, String duration){
		this.tmgTimecoverageId = tmgTimecoverageId;
		this.tmgTimecoverageDurationId = tmgTimecoverageDurationId;
		this.duration=duration;
	}
}
