package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgGeospatialcoverageEastwest {
	protected int tmgGeospatialcoverageId;
	protected int tmgGeospatialcoverageEastwestId;
	protected Datavalue resolution = new Datavalue(null);
	protected Datavalue size = new Datavalue(null);
	protected Datavalue start = new Datavalue(null);
	protected Datavalue units = new Datavalue(null);
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setTmgGeospatialcoverageEastwestId(int tmgGeospatialcoverageEastwestId){
		this.tmgGeospatialcoverageEastwestId = tmgGeospatialcoverageEastwestId;
	}
	public void setResolution(String resolution){
		this.resolution = new Datavalue(resolution);
	}
	public void setSize(String size){
		this.size = new Datavalue(size);
	}
	public void setStart(String start){
		this.start = new Datavalue(start);
	}
	public void setUnits(String units){
		this.units = new Datavalue(units);
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
	}
	public int getTmgGeospatialcoverageEastwestId(){
		return this.tmgGeospatialcoverageEastwestId;
	}
	public Datavalue getResolution(){
		return this.resolution;
	}
	public Datavalue getSize(){
		return this.size;
	}
	public Datavalue getStart(){
		return this.start;
	}
	public Datavalue getUnits(){
		return this.units;
	}

	public TmgGeospatialcoverageEastwest(){
		this.tmgGeospatialcoverageEastwestId = -1;
	}
	public TmgGeospatialcoverageEastwest(int tmgGeospatialcoverageEastwest){
		this.tmgGeospatialcoverageEastwestId = tmgGeospatialcoverageEastwest;
	}
	public TmgGeospatialcoverageEastwest(int tmgGeospatialcoverageId, int tmgGeospatialcoverageEastwestId, Datavalue resolution, Datavalue size, Datavalue start, Datavalue units){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
		this.tmgGeospatialcoverageEastwestId = tmgGeospatialcoverageEastwestId;
		this.resolution=resolution;
		this.size=size;
		this.start=start;
		this.units=units;
	}
	public TmgGeospatialcoverageEastwest clone(){
		TmgGeospatialcoverageEastwest clone = new TmgGeospatialcoverageEastwest(this.tmgGeospatialcoverageId, -1, this.resolution, this.size, this.start, this.units);
		return clone;
	}
}
