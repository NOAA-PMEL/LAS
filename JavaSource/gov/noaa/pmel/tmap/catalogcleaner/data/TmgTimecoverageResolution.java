package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgTimecoverageResolution {
	protected int tmgTimecoverageId;
	protected int tmgTimecoverageResolutionId;
	protected Datavalue duration = new Datavalue(null);
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setTmgTimecoverageResolutionId(int tmgTimecoverageResolutionId){
		this.tmgTimecoverageResolutionId = tmgTimecoverageResolutionId;
	}
	public void setDuration(String duration){
		this.duration = new Datavalue(duration);
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public int getTmgTimecoverageResolutionId(){
		return this.tmgTimecoverageResolutionId;
	}
	public Datavalue getDuration(){
		return this.duration;
	}

	public TmgTimecoverageResolution(){
		this.tmgTimecoverageResolutionId = -1;
	}
	public TmgTimecoverageResolution(int tmgTimecoverageResolution){
		this.tmgTimecoverageResolutionId = tmgTimecoverageResolution;
	}
	public TmgTimecoverageResolution(int tmgTimecoverageId, int tmgTimecoverageResolutionId, Datavalue duration){
		this.tmgTimecoverageId = tmgTimecoverageId;
		this.tmgTimecoverageResolutionId = tmgTimecoverageResolutionId;
		this.duration=duration;
	}
	public TmgTimecoverageResolution clone(){
		TmgTimecoverageResolution clone = new TmgTimecoverageResolution(this.tmgTimecoverageId, -1, this.duration);
		return clone;
	}
}
