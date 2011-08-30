package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgTimecoverageDuration {
	protected int tmgTimecoverageId;
	protected int tmgTimecoverageDurationId;
	protected Datavalue duration = new Datavalue(null);
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setTmgTimecoverageDurationId(int tmgTimecoverageDurationId){
		this.tmgTimecoverageDurationId = tmgTimecoverageDurationId;
	}
	public void setDuration(String duration){
		this.duration = new Datavalue(duration);
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public int getTmgTimecoverageDurationId(){
		return this.tmgTimecoverageDurationId;
	}
	public Datavalue getDuration(){
		return this.duration;
	}

	public TmgTimecoverageDuration(){
		this.tmgTimecoverageDurationId = -1;
	}
	public TmgTimecoverageDuration(int tmgTimecoverageDuration){
		this.tmgTimecoverageDurationId = tmgTimecoverageDuration;
	}
	public TmgTimecoverageDuration(int tmgTimecoverageId, int tmgTimecoverageDurationId, Datavalue duration){
		this.tmgTimecoverageId = tmgTimecoverageId;
		this.tmgTimecoverageDurationId = tmgTimecoverageDurationId;
		this.duration=duration;
	}
	public TmgTimecoverageDuration clone(){
		TmgTimecoverageDuration clone = new TmgTimecoverageDuration(this.tmgTimecoverageId, -1, this.duration);
		return clone;
	}
}
