package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgPublisherName {
	protected int tmgPublisherId;
	protected int tmgPublisherNameId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue vocabulary = new Datavalue(null);
	public void setTmgPublisherId(int tmgPublisherId){
		this.tmgPublisherId = tmgPublisherId;
	}
	public void setTmgPublisherNameId(int tmgPublisherNameId){
		this.tmgPublisherNameId = tmgPublisherNameId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = new Datavalue(vocabulary);
	}
	public int getTmgPublisherId(){
		return this.tmgPublisherId;
	}
	public int getTmgPublisherNameId(){
		return this.tmgPublisherNameId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getVocabulary(){
		return this.vocabulary;
	}

	public TmgPublisherName(){
		this.tmgPublisherNameId = -1;
	}
	public TmgPublisherName(int tmgPublisherName){
		this.tmgPublisherNameId = tmgPublisherName;
	}
	public TmgPublisherName(int tmgPublisherId, int tmgPublisherNameId, Datavalue value, Datavalue vocabulary){
		this.tmgPublisherId = tmgPublisherId;
		this.tmgPublisherNameId = tmgPublisherNameId;
		this.value=value;
		this.vocabulary=vocabulary;
	}
	public TmgPublisherName clone(){
		TmgPublisherName clone = new TmgPublisherName(this.tmgPublisherId, -1, this.value, this.vocabulary);
		return clone;
	}
}
