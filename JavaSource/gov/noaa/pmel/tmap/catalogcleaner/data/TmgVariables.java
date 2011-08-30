package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgVariables {
	protected int tmgId;
	protected int tmgVariablesId;
	protected Datavalue vocabulary = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgVariablesId(int tmgVariablesId){
		this.tmgVariablesId = tmgVariablesId;
	}
	public void setVocabulary(String vocabulary){
		this.vocabulary = new Datavalue(vocabulary);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgVariablesId(){
		return this.tmgVariablesId;
	}
	public Datavalue getVocabulary(){
		return this.vocabulary;
	}

	public TmgVariables(){
		this.tmgVariablesId = -1;
	}
	public TmgVariables(int tmgVariables){
		this.tmgVariablesId = tmgVariables;
	}
	public TmgVariables(int tmgId, int tmgVariablesId, Datavalue vocabulary){
		this.tmgId = tmgId;
		this.tmgVariablesId = tmgVariablesId;
		this.vocabulary=vocabulary;
	}
	public TmgVariables clone(){
		TmgVariables clone = new TmgVariables(this.tmgId, -1, this.vocabulary);
		return clone;
	}
}
