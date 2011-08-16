package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgGeospatialcoverageUpdown {
	protected int tmgGeospatialcoverageUpdownId;
	protected int tmgGeospatialcoverageId;
	protected String start;
	protected String resolution;
	protected String size;
	protected String units;
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
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
	public int getTmgGeospatialcoverageUpdownId(){
		return this.tmgGeospatialcoverageUpdownId;
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
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

	public TmgGeospatialcoverageUpdown(int id){
		this.tmgGeospatialcoverageUpdownId=id;
	}
	public TmgGeospatialcoverageUpdown(int tmgGeospatialcoverageUpdownId, String start, String resolution, String size, String units, int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageUpdownId=tmgGeospatialcoverageUpdownId;
		this.start=start;
		this.resolution=resolution;
		this.size=size;
		this.units=units;
		this.tmgGeospatialcoverageId=tmgGeospatialcoverageId;
	}
}
