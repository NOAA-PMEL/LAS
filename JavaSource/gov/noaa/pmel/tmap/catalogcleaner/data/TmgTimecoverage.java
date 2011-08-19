package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgTimecoverage {
	protected int tmgId;
	protected int tmgTimecoverageId;
	protected String resolution;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setResolution(String resolution){
		this.resolution = resolution;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public String getResolution(){
		return this.resolution;
	}

	public TmgTimecoverage(int tmgTimecoverage){
		this.tmgTimecoverageId = tmgTimecoverage;
	}
	public TmgTimecoverage(int tmgId, int tmgTimecoverageId, String resolution){
		this.tmgId = tmgId;
		this.tmgTimecoverageId = tmgTimecoverageId;
		this.resolution=resolution;
	}
}
