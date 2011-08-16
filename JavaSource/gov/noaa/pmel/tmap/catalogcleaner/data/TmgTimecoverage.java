package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgTimecoverage {
	protected int tmgTimecoverageId;
	protected int tmgId;
	protected String resolution;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setResolution(String resolution){
		this.resolution = resolution;
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getResolution(){
		return this.resolution;
	}

	public TmgTimecoverage(int id){
		this.tmgTimecoverageId=id;
	}
	public TmgTimecoverage(int tmgTimecoverageId, String resolution, int tmgId){
		this.tmgTimecoverageId=tmgTimecoverageId;
		this.resolution=resolution;
		this.tmgId=tmgId;
	}
}
