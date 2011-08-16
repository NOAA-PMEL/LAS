package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgProject {
	protected int tmgProjectId;
	protected int tmgId;
	protected String value;
	protected String vocabulary;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = vocabulary;
	}
	public int getTmgProjectId(){
		return this.tmgProjectId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getValue(){
		return this.value;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}

	public TmgProject(int id){
		this.tmgProjectId=id;
	}
	public TmgProject(int tmgProjectId, String value, String vocabulary, int tmgId){
		this.tmgProjectId=tmgProjectId;
		this.value=value;
		this.vocabulary=vocabulary;
		this.tmgId=tmgId;
	}
}
