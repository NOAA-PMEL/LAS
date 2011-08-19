package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgGeospatialcoverageNorthsouth {
	protected int tmgGeospatialcoverageId;
	protected int tmgGeospatialcoverageNorthsouthId;
	protected String size;
	protected String resolution;
	protected String start;
	protected String units;
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setTmgGeospatialcoverageNorthsouthId(int tmgGeospatialcoverageNorthsouthId){
		this.tmgGeospatialcoverageNorthsouthId = tmgGeospatialcoverageNorthsouthId;
	}
	public void setSize(String size){
		this.size = size;
	}
	public void setResolution(String resolution){
		this.resolution = resolution;
	}
	public void setStart(String start){
		this.start = start;
	}
	public void setUnits(String units){
		this.units = units;
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
	}
	public int getTmgGeospatialcoverageNorthsouthId(){
		return this.tmgGeospatialcoverageNorthsouthId;
	}
	public String getSize(){
		return this.size;
	}
	public String getResolution(){
		return this.resolution;
	}
	public String getStart(){
		return this.start;
	}
	public String getUnits(){
		return this.units;
	}

	public TmgGeospatialcoverageNorthsouth(int tmgGeospatialcoverageNorthsouth){
		this.tmgGeospatialcoverageNorthsouthId = tmgGeospatialcoverageNorthsouth;
	}
	public TmgGeospatialcoverageNorthsouth(int tmgGeospatialcoverageId, int tmgGeospatialcoverageNorthsouthId, String size, String resolution, String start, String units){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
		this.tmgGeospatialcoverageNorthsouthId = tmgGeospatialcoverageNorthsouthId;
		this.size=size;
		this.resolution=resolution;
		this.start=start;
		this.units=units;
	}
}
