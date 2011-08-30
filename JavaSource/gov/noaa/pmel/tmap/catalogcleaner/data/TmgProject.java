package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgProject {
	protected int tmgId;
	protected int tmgProjectId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue vocabulary = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgProjectId(int tmgProjectId){
		this.tmgProjectId = tmgProjectId;
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
	public int getTmgProjectId(){
		return this.tmgProjectId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getVocabulary(){
		return this.vocabulary;
	}

	public TmgProject(){
		this.tmgProjectId = -1;
	}
	public TmgProject(int tmgProject){
		this.tmgProjectId = tmgProject;
	}
	public TmgProject(int tmgId, int tmgProjectId, Datavalue value, Datavalue vocabulary){
		this.tmgId = tmgId;
		this.tmgProjectId = tmgProjectId;
		this.value=value;
		this.vocabulary=vocabulary;
	}
	public TmgProject clone(){
		TmgProject clone = new TmgProject(this.tmgId, -1, this.value, this.vocabulary);
		return clone;
	}
}
