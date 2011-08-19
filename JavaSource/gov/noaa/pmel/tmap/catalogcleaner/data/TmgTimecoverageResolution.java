package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgTimecoverageResolution {
	protected int tmgTimecoverageId;
	protected int tmgTimecoverageResolutionId;
	protected String duration;
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setTmgTimecoverageResolutionId(int tmgTimecoverageResolutionId){
		this.tmgTimecoverageResolutionId = tmgTimecoverageResolutionId;
	}
	public void setDuration(String duration){
		this.duration = duration;
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public int getTmgTimecoverageResolutionId(){
		return this.tmgTimecoverageResolutionId;
	}
	public String getDuration(){
		return this.duration;
	}

	public TmgTimecoverageResolution(int tmgTimecoverageResolution){
		this.tmgTimecoverageResolutionId = tmgTimecoverageResolution;
	}
	public TmgTimecoverageResolution(int tmgTimecoverageId, int tmgTimecoverageResolutionId, String duration){
		this.tmgTimecoverageId = tmgTimecoverageId;
		this.tmgTimecoverageResolutionId = tmgTimecoverageResolutionId;
		this.duration=duration;
	}
}
