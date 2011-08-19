package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgKeyword {
	protected int tmgId;
	protected int tmgKeywordId;
	protected String value;
	protected String vocabulary;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgKeywordId(int tmgKeywordId){
		this.tmgKeywordId = tmgKeywordId;
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
	public int getTmgKeywordId(){
		return this.tmgKeywordId;
	}
	public String getValue(){
		return this.value;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}

	public TmgKeyword(int tmgKeyword){
		this.tmgKeywordId = tmgKeyword;
	}
	public TmgKeyword(int tmgId, int tmgKeywordId, String value, String vocabulary){
		this.tmgId = tmgId;
		this.tmgKeywordId = tmgKeywordId;
		this.value=value;
		this.vocabulary=vocabulary;
	}
}
