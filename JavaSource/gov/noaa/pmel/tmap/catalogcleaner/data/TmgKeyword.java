package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgKeyword {
	protected int tmgKeywordId;
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
	public int getTmgKeywordId(){
		return this.tmgKeywordId;
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

	public TmgKeyword(int id){
		this.tmgKeywordId=id;
	}
	public TmgKeyword(int tmgKeywordId, String value, String vocabulary, int tmgId){
		this.tmgKeywordId=tmgKeywordId;
		this.value=value;
		this.vocabulary=vocabulary;
		this.tmgId=tmgId;
	}
}
