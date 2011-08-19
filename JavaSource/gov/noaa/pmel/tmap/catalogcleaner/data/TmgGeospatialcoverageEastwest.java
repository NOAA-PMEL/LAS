package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgGeospatialcoverageEastwest {
	protected int tmgGeospatialcoverageId;
	protected int tmgGeospatialcoverageEastwestId;
	protected String size;
	protected String units;
	protected String start;
	protected String resolution;
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setTmgGeospatialcoverageEastwestId(int tmgGeospatialcoverageEastwestId){
		this.tmgGeospatialcoverageEastwestId = tmgGeospatialcoverageEastwestId;
	}
	public void setSize(String size){
		this.size = size;
	}
	public void setUnits(String units){
		this.units = units;
	}
	public void setStart(String start){
		this.start = start;
	}
	public void setResolution(String resolution){
		this.resolution = resolution;
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
	}
	public int getTmgGeospatialcoverageEastwestId(){
		return this.tmgGeospatialcoverageEastwestId;
	}
	public String getSize(){
		return this.size;
	}
	public String getUnits(){
		return this.units;
	}
	public String getStart(){
		return this.start;
	}
	public String getResolution(){
		return this.resolution;
	}

	public TmgGeospatialcoverageEastwest(int tmgGeospatialcoverageEastwest){
		this.tmgGeospatialcoverageEastwestId = tmgGeospatialcoverageEastwest;
	}
	public TmgGeospatialcoverageEastwest(int tmgGeospatialcoverageId, int tmgGeospatialcoverageEastwestId, String size, String units, String start, String resolution){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
		this.tmgGeospatialcoverageEastwestId = tmgGeospatialcoverageEastwestId;
		this.size=size;
		this.units=units;
		this.start=start;
		this.resolution=resolution;
	}
}
