package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgPublisherName {
	protected int tmgPublisherNameId;
	protected int tmgPublisherId;
	protected String value;
	protected String vocabulary;
	public void setTmgPublisherId(int tmgPublisherId){
		this.tmgPublisherId = tmgPublisherId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = vocabulary;
	}
	public int getTmgPublisherNameId(){
		return this.tmgPublisherNameId;
	}
	public int getTmgPublisherId(){
		return this.tmgPublisherId;
	}
	public String getValue(){
		return this.value;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}

	public TmgPublisherName(int id){
		this.tmgPublisherNameId=id;
	}
	public TmgPublisherName(int tmgPublisherNameId, String value, String vocabulary, int tmgPublisherId){
		this.tmgPublisherNameId=tmgPublisherNameId;
		this.value=value;
		this.vocabulary=vocabulary;
		this.tmgPublisherId=tmgPublisherId;
	}
}
