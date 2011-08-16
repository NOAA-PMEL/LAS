package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgCreatorName {
	protected int tmgCreatorNameId;
	protected int tmgCreatorId;
	protected String value;
	protected String vocabulary;
	public void setTmgCreatorId(int tmgCreatorId){
		this.tmgCreatorId = tmgCreatorId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = vocabulary;
	}
	public int getTmgCreatorNameId(){
		return this.tmgCreatorNameId;
	}
	public int getTmgCreatorId(){
		return this.tmgCreatorId;
	}
	public String getValue(){
		return this.value;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}

	public TmgCreatorName(int id){
		this.tmgCreatorNameId=id;
	}
	public TmgCreatorName(int tmgCreatorNameId, String value, String vocabulary, int tmgCreatorId){
		this.tmgCreatorNameId=tmgCreatorNameId;
		this.value=value;
		this.vocabulary=vocabulary;
		this.tmgCreatorId=tmgCreatorId;
	}
}
