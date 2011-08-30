package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgTimecoverage {
	protected int tmgId;
	protected int tmgTimecoverageId;
	protected Datavalue resolution = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgTimecoverageId(int tmgTimecoverageId){
		this.tmgTimecoverageId = tmgTimecoverageId;
	}
	public void setResolution(String resolution){
		this.resolution = new Datavalue(resolution);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgTimecoverageId(){
		return this.tmgTimecoverageId;
	}
	public Datavalue getResolution(){
		return this.resolution;
	}

	public TmgTimecoverage(){
		this.tmgTimecoverageId = -1;
	}
	public TmgTimecoverage(int tmgTimecoverage){
		this.tmgTimecoverageId = tmgTimecoverage;
	}
	public TmgTimecoverage(int tmgId, int tmgTimecoverageId, Datavalue resolution){
		this.tmgId = tmgId;
		this.tmgTimecoverageId = tmgTimecoverageId;
		this.resolution=resolution;
	}
	public TmgTimecoverage clone(){
		TmgTimecoverage clone = new TmgTimecoverage(this.tmgId, -1, this.resolution);
		return clone;
	}
}
