package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgPublisherName {
	protected int tmgPublisherId;
	protected int tmgPublisherNameId;
	protected String value;
	protected String vocabulary;
	public void setTmgPublisherId(int tmgPublisherId){
		this.tmgPublisherId = tmgPublisherId;
	}
	public void setTmgPublisherNameId(int tmgPublisherNameId){
		this.tmgPublisherNameId = tmgPublisherNameId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = vocabulary;
	}
	public int getTmgPublisherId(){
		return this.tmgPublisherId;
	}
	public int getTmgPublisherNameId(){
		return this.tmgPublisherNameId;
	}
	public String getValue(){
		return this.value;
	}
	public String getVocabulary(){
		return this.vocabulary;
	}

	public TmgPublisherName(int tmgPublisherName){
		this.tmgPublisherNameId = tmgPublisherName;
	}
	public TmgPublisherName(int tmgPublisherId, int tmgPublisherNameId, String value, String vocabulary){
		this.tmgPublisherId = tmgPublisherId;
		this.tmgPublisherNameId = tmgPublisherNameId;
		this.value=value;
		this.vocabulary=vocabulary;
	}
}
