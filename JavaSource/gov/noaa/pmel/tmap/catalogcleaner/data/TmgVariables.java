package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgVariables {
	protected int tmgVariablesId;
	protected int tmgId;
	protected String vocabulary;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = vocabulary;
	}
	public int getTmgVariablesId(){
		return this.tmgVariablesId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}

	public TmgVariables(int id){
		this.tmgVariablesId=id;
	}
	public TmgVariables(int tmgVariablesId, String vocabulary, int tmgId){
		this.tmgVariablesId=tmgVariablesId;
		this.vocabulary=vocabulary;
		this.tmgId=tmgId;
	}
}
