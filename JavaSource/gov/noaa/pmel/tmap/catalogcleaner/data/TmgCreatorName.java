package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgCreatorName {
	protected int tmgCreatorId;
	protected int tmgCreatorNameId;
	protected String value;
	protected String vocabulary;
	public void setTmgCreatorId(int tmgCreatorId){
		this.tmgCreatorId = tmgCreatorId;
	}
	public void setTmgCreatorNameId(int tmgCreatorNameId){
		this.tmgCreatorNameId = tmgCreatorNameId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = vocabulary;
	}
	public int getTmgCreatorId(){
		return this.tmgCreatorId;
	}
	public int getTmgCreatorNameId(){
		return this.tmgCreatorNameId;
	}
	public String getValue(){
		return this.value;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}

	public TmgCreatorName(int tmgCreatorName){
		this.tmgCreatorNameId = tmgCreatorName;
	}
	public TmgCreatorName(int tmgCreatorId, int tmgCreatorNameId, String value, String vocabulary){
		this.tmgCreatorId = tmgCreatorId;
		this.tmgCreatorNameId = tmgCreatorNameId;
		this.value=value;
		this.vocabulary=vocabulary;
	}
}
