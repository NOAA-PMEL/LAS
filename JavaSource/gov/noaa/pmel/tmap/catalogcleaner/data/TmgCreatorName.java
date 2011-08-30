package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgCreatorName {
	protected int tmgCreatorId;
	protected int tmgCreatorNameId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue vocabulary = new Datavalue(null);
	public void setTmgCreatorId(int tmgCreatorId){
		this.tmgCreatorId = tmgCreatorId;
	}
	public void setTmgCreatorNameId(int tmgCreatorNameId){
		this.tmgCreatorNameId = tmgCreatorNameId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = new Datavalue(vocabulary);
	}
	public int getTmgCreatorId(){
		return this.tmgCreatorId;
	}
	public int getTmgCreatorNameId(){
		return this.tmgCreatorNameId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getVocabulary(){
		return this.vocabulary;
	}

	public TmgCreatorName(){
		this.tmgCreatorNameId = -1;
	}
	public TmgCreatorName(int tmgCreatorName){
		this.tmgCreatorNameId = tmgCreatorName;
	}
	public TmgCreatorName(int tmgCreatorId, int tmgCreatorNameId, Datavalue value, Datavalue vocabulary){
		this.tmgCreatorId = tmgCreatorId;
		this.tmgCreatorNameId = tmgCreatorNameId;
		this.value=value;
		this.vocabulary=vocabulary;
	}
	public TmgCreatorName clone(){
		TmgCreatorName clone = new TmgCreatorName(this.tmgCreatorId, -1, this.value, this.vocabulary);
		return clone;
	}
}
