package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgGeospatialcoverageEastwest {
	protected int tmgGeospatialcoverageEastwestId;
	protected int tmgGeospatialcoverageId;
	protected String size;
	protected String units;
	protected String start;
	protected String resolution;
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
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
	public int getTmgGeospatialcoverageEastwestId(){
		return this.tmgGeospatialcoverageEastwestId;
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
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

	public TmgGeospatialcoverageEastwest(int id){
		this.tmgGeospatialcoverageEastwestId=id;
	}
	public TmgGeospatialcoverageEastwest(int tmgGeospatialcoverageEastwestId, String size, String units, String start, String resolution, int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageEastwestId=tmgGeospatialcoverageEastwestId;
		this.size=size;
		this.units=units;
		this.start=start;
		this.resolution=resolution;
		this.tmgGeospatialcoverageId=tmgGeospatialcoverageId;
	}
}
