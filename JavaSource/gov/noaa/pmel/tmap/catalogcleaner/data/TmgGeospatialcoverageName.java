package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgGeospatialcoverageName {
	protected int tmgGeospatialcoverageId;
	protected int tmgGeospatialcoverageNameId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue vocabulary = new Datavalue(null);
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setTmgGeospatialcoverageNameId(int tmgGeospatialcoverageNameId){
		this.tmgGeospatialcoverageNameId = tmgGeospatialcoverageNameId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = new Datavalue(vocabulary);
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
	}
	public int getTmgGeospatialcoverageNameId(){
		return this.tmgGeospatialcoverageNameId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getVocabulary(){
		return this.vocabulary;
	}

	public TmgGeospatialcoverageName(){
		this.tmgGeospatialcoverageNameId = -1;
	}
	public TmgGeospatialcoverageName(int tmgGeospatialcoverageName){
		this.tmgGeospatialcoverageNameId = tmgGeospatialcoverageName;
	}
	public TmgGeospatialcoverageName(int tmgGeospatialcoverageId, int tmgGeospatialcoverageNameId, Datavalue value, Datavalue vocabulary){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
		this.tmgGeospatialcoverageNameId = tmgGeospatialcoverageNameId;
		this.value=value;
		this.vocabulary=vocabulary;
	}
	public TmgGeospatialcoverageName clone(){
		TmgGeospatialcoverageName clone = new TmgGeospatialcoverageName(this.tmgGeospatialcoverageId, -1, this.value, this.vocabulary);
		return clone;
	}
}
