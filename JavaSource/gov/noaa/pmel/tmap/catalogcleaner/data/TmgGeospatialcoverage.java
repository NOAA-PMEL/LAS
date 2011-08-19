package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgGeospatialcoverage {
	protected int tmgId;
	protected int tmgGeospatialcoverageId;
	protected String upordown;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setUpordown(String upordown){
		this.upordown = upordown;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
	}
	public String getUpordown(){
		return this.upordown;
	}

	public TmgGeospatialcoverage(int tmgGeospatialcoverage){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverage;
	}
	public TmgGeospatialcoverage(int tmgId, int tmgGeospatialcoverageId, String upordown){
		this.tmgId = tmgId;
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
		this.upordown=upordown;
	}
}
