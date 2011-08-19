package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgProject {
	protected int tmgId;
	protected int tmgProjectId;
	protected String value;
	protected String vocabulary;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgProjectId(int tmgProjectId){
		this.tmgProjectId = tmgProjectId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = vocabulary;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgProjectId(){
		return this.tmgProjectId;
	}
	public String getValue(){
		return this.value;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}

	public TmgProject(int tmgProject){
		this.tmgProjectId = tmgProject;
	}
	public TmgProject(int tmgId, int tmgProjectId, String value, String vocabulary){
		this.tmgId = tmgId;
		this.tmgProjectId = tmgProjectId;
		this.value=value;
		this.vocabulary=vocabulary;
	}
}
