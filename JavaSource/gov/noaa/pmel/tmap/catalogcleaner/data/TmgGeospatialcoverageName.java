package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgGeospatialcoverageName {
	protected int tmgGeospatialcoverageNameId;
	protected int tmgGeospatialcoverageId;
	protected String vocabulary;
	protected String value;
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = vocabulary;
	}
	public void setValue(String value){
		this.value = value;
	}
	public int getTmgGeospatialcoverageNameId(){
		return this.tmgGeospatialcoverageNameId;
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}
	public String getValue(){
		return this.value;
	}

	public TmgGeospatialcoverageName(int id){
		this.tmgGeospatialcoverageNameId=id;
	}
	public TmgGeospatialcoverageName(int tmgGeospatialcoverageNameId, String vocabulary, String value, int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageNameId=tmgGeospatialcoverageNameId;
		this.vocabulary=vocabulary;
		this.value=value;
		this.tmgGeospatialcoverageId=tmgGeospatialcoverageId;
	}
}
