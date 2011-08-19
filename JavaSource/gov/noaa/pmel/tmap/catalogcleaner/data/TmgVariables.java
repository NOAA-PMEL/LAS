package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgVariables {
	protected int tmgId;
	protected int tmgVariablesId;
	protected String vocabulary;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgVariablesId(int tmgVariablesId){
		this.tmgVariablesId = tmgVariablesId;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = vocabulary;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgVariablesId(){
		return this.tmgVariablesId;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}

	public TmgVariables(int tmgVariables){
		this.tmgVariablesId = tmgVariables;
	}
	public TmgVariables(int tmgId, int tmgVariablesId, String vocabulary){
		this.tmgId = tmgId;
		this.tmgVariablesId = tmgVariablesId;
		this.vocabulary=vocabulary;
	}
}
