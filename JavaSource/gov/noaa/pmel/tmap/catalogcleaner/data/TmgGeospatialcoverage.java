package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgGeospatialcoverage {
	protected int tmgGeospatialcoverageId;
	protected int tmgId;
	protected String upordown;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setUpordown(String upordown){
		this.upordown = upordown;
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getUpordown(){
		return this.upordown;
	}

	public TmgGeospatialcoverage(int id){
		this.tmgGeospatialcoverageId=id;
	}
	public TmgGeospatialcoverage(int tmgGeospatialcoverageId, String upordown, int tmgId){
		this.tmgGeospatialcoverageId=tmgGeospatialcoverageId;
		this.upordown=upordown;
		this.tmgId=tmgId;
	}
}
