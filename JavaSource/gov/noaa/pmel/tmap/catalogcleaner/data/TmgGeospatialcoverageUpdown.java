package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgGeospatialcoverageUpdown {
	protected int tmgGeospatialcoverageId;
	protected int tmgGeospatialcoverageUpdownId;
	protected Datavalue resolution = new Datavalue(null);
	protected Datavalue size = new Datavalue(null);
	protected Datavalue start = new Datavalue(null);
	protected Datavalue units = new Datavalue(null);
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setTmgGeospatialcoverageUpdownId(int tmgGeospatialcoverageUpdownId){
		this.tmgGeospatialcoverageUpdownId = tmgGeospatialcoverageUpdownId;
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
	public int getTmgGeospatialcoverageUpdownId(){
		return this.tmgGeospatialcoverageUpdownId;
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

	public TmgGeospatialcoverageUpdown(){
		this.tmgGeospatialcoverageUpdownId = -1;
	}
	public TmgGeospatialcoverageUpdown(int tmgGeospatialcoverageUpdown){
		this.tmgGeospatialcoverageUpdownId = tmgGeospatialcoverageUpdown;
	}
	public TmgGeospatialcoverageUpdown(int tmgGeospatialcoverageId, int tmgGeospatialcoverageUpdownId, Datavalue resolution, Datavalue size, Datavalue start, Datavalue units){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
		this.tmgGeospatialcoverageUpdownId = tmgGeospatialcoverageUpdownId;
		this.resolution=resolution;
		this.size=size;
		this.start=start;
		this.units=units;
	}
	public TmgGeospatialcoverageUpdown clone(){
		TmgGeospatialcoverageUpdown clone = new TmgGeospatialcoverageUpdown(this.tmgGeospatialcoverageId, -1, this.resolution, this.size, this.start, this.units);
		return clone;
	}
}
