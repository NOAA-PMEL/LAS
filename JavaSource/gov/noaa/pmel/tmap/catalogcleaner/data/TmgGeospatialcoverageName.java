package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgGeospatialcoverageName {
	protected int tmgGeospatialcoverageId;
	protected int tmgGeospatialcoverageNameId;
	protected String vocabulary;
	protected String value;
	public void setTmgGeospatialcoverageId(int tmgGeospatialcoverageId){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
	}
	public void setTmgGeospatialcoverageNameId(int tmgGeospatialcoverageNameId){
		this.tmgGeospatialcoverageNameId = tmgGeospatialcoverageNameId;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = vocabulary;
	}
	public void setValue(String value){
		this.value = value;
	}
	public int getTmgGeospatialcoverageId(){
		return this.tmgGeospatialcoverageId;
	}
	public int getTmgGeospatialcoverageNameId(){
		return this.tmgGeospatialcoverageNameId;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}
	public String getValue(){
		return this.value;
	}

	public TmgGeospatialcoverageName(int tmgGeospatialcoverageName){
		this.tmgGeospatialcoverageNameId = tmgGeospatialcoverageName;
	}
	public TmgGeospatialcoverageName(int tmgGeospatialcoverageId, int tmgGeospatialcoverageNameId, String vocabulary, String value){
		this.tmgGeospatialcoverageId = tmgGeospatialcoverageId;
		this.tmgGeospatialcoverageNameId = tmgGeospatialcoverageNameId;
		this.vocabulary=vocabulary;
		this.value=value;
	}
}
