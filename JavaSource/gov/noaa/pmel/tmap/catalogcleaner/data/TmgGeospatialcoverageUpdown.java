package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgGeospatialcoverageUpdown {
	protected int tmgGeospatialcoverageId;
	protected int tmgGeospatialcoverageUpdownId;
	protected String start;
	protected String resolution;
	protected String size;
	protected String units;
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setTmgGeospatialcoverageUpdownId(int tmgGeospatialcoverageUpdownId){
		this.tmgGeospatialcoverageUpdownId = tmgGeospatialcoverageUpdownId;
	}
	public void setStart(String start){
		this.start = start;
	}
	public void setResolution(String resolution){
		this.resolution = resolution;
	}
	public void setSize(String size){
		this.size = size;
	}
	public void setUnits(String units){
		this.units = units;
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
	}
	public int getTmgGeospatialcoverageUpdownId(){
		return this.tmgGeospatialcoverageUpdownId;
	}
	public String getStart(){
		return this.start;
	}
	public String getResolution(){
		return this.resolution;
	}
	public String getSize(){
		return this.size;
	}
	public String getUnits(){
		return this.units;
	}

	public TmgGeospatialcoverageUpdown(int tmgGeospatialcoverageUpdown){
		this.tmgGeospatialcoverageUpdownId = tmgGeospatialcoverageUpdown;
	}
	public TmgGeospatialcoverageUpdown(int tmgGeospatialcoverageId, int tmgGeospatialcoverageUpdownId, String start, String resolution, String size, String units){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
		this.tmgGeospatialcoverageUpdownId = tmgGeospatialcoverageUpdownId;
		this.start=start;
		this.resolution=resolution;
		this.size=size;
		this.units=units;
	}
}
