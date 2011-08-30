package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgGeospatialcoverageNorthsouth {
	protected int tmgGeospatialcoverageId;
	protected int tmgGeospatialcoverageNorthsouthId;
	protected Datavalue resolution = new Datavalue(null);
	protected Datavalue size = new Datavalue(null);
	protected Datavalue start = new Datavalue(null);
	protected Datavalue units = new Datavalue(null);
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setTmgGeospatialcoverageNorthsouthId(int tmgGeospatialcoverageNorthsouthId){
		this.tmgGeospatialcoverageNorthsouthId = tmgGeospatialcoverageNorthsouthId;
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
	public int getTmgGeospatialcoverageNorthsouthId(){
		return this.tmgGeospatialcoverageNorthsouthId;
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

	public TmgGeospatialcoverageNorthsouth(){
		this.tmgGeospatialcoverageNorthsouthId = -1;
	}
	public TmgGeospatialcoverageNorthsouth(int tmgGeospatialcoverageNorthsouth){
		this.tmgGeospatialcoverageNorthsouthId = tmgGeospatialcoverageNorthsouth;
	}
	public TmgGeospatialcoverageNorthsouth(int tmgGeospatialcoverageId, int tmgGeospatialcoverageNorthsouthId, Datavalue resolution, Datavalue size, Datavalue start, Datavalue units){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
		this.tmgGeospatialcoverageNorthsouthId = tmgGeospatialcoverageNorthsouthId;
		this.resolution=resolution;
		this.size=size;
		this.start=start;
		this.units=units;
	}
	public TmgGeospatialcoverageNorthsouth clone(){
		TmgGeospatialcoverageNorthsouth clone = new TmgGeospatialcoverageNorthsouth(this.tmgGeospatialcoverageId, -1, this.resolution, this.size, this.start, this.units);
		return clone;
	}
}
