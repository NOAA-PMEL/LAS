package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgGeospatialcoverage {
	protected int tmgId;
	protected int tmgGeospatialcoverageId;
	protected Datavalue upordown = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setUpordown(String upordown){
		this.upordown = new Datavalue(upordown);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
	}
	public Datavalue getUpordown(){
		return this.upordown;
	}

	public TmgGeospatialcoverage(){
		this.tmgGeospatialcoverageId = -1;
	}
	public TmgGeospatialcoverage(int tmgGeospatialcoverage){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverage;
	}
	public TmgGeospatialcoverage(int tmgId, int tmgGeospatialcoverageId, Datavalue upordown){
		this.tmgId = tmgId;
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
		this.upordown=upordown;
	}
	public TmgGeospatialcoverage clone(){
		TmgGeospatialcoverage clone = new TmgGeospatialcoverage(this.tmgId, -1, this.upordown);
		return clone;
	}
}
