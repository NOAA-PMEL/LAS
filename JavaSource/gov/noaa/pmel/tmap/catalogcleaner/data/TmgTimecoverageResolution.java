package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgTimecoverageResolution {
	protected int tmgTimecoverageResolutionId;
	protected int tmgTimecoverageId;
	protected String duration;
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setDuration(String duration){
		this.duration = duration;
	}
	public int getTmgTimecoverageResolutionId(){
		return this.tmgTimecoverageResolutionId;
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public String getDuration(){
		return this.duration;
	}

	public TmgTimecoverageResolution(int id){
		this.tmgTimecoverageResolutionId=id;
	}
	public TmgTimecoverageResolution(int tmgTimecoverageResolutionId, String duration, int tmgTimecoverageId){
		this.tmgTimecoverageResolutionId=tmgTimecoverageResolutionId;
		this.duration=duration;
		this.tmgTimecoverageId=tmgTimecoverageId;
	}
}
