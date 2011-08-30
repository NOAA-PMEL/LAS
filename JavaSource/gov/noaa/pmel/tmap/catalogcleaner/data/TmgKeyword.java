package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgKeyword {
	protected int tmgId;
	protected int tmgKeywordId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue vocabulary = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgKeywordId(int tmgKeywordId){
		this.tmgKeywordId = tmgKeywordId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = new Datavalue(vocabulary);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgKeywordId(){
		return this.tmgKeywordId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getVocabulary(){
		return this.vocabulary;
	}

	public TmgKeyword(){
		this.tmgKeywordId = -1;
	}
	public TmgKeyword(int tmgKeyword){
		this.tmgKeywordId = tmgKeyword;
	}
	public TmgKeyword(int tmgId, int tmgKeywordId, Datavalue value, Datavalue vocabulary){
		this.tmgId = tmgId;
		this.tmgKeywordId = tmgKeywordId;
		this.value=value;
		this.vocabulary=vocabulary;
	}
	public TmgKeyword clone(){
		TmgKeyword clone = new TmgKeyword(this.tmgId, -1, this.value, this.vocabulary);
		return clone;
	}
}
