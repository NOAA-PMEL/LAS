package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgVariablesVariable {
	protected int tmgVariablesId;
	protected int tmgVariablesVariableId;
	protected Datavalue name = new Datavalue(null);
	protected Datavalue units = new Datavalue(null);
	protected Datavalue vocabularyName = new Datavalue(null);
	public void setTmgVariablesId(int tmgVariablesId){
		this.tmgVariablesId = tmgVariablesId;
	}
	public void setTmgVariablesVariableId(int tmgVariablesVariableId){
		this.tmgVariablesVariableId = tmgVariablesVariableId;
	}
	public void setName(String name){
		this.name = new Datavalue(name);
	}
	public void setUnits(String units){
		this.units = new Datavalue(units);
	}
	public void setVocabularyName(String vocabularyName){
		this.vocabularyName = new Datavalue(vocabularyName);
	}
	public int getTmgVariablesId(){
		return this.tmgVariablesId;
	}
	public int getTmgVariablesVariableId(){
		return this.tmgVariablesVariableId;
	}
	public Datavalue getName(){
		return this.name;
	}
	public Datavalue getUnits(){
		return this.units;
	}
	public Datavalue getVocabularyName(){
		return this.vocabularyName;
	}

	public TmgVariablesVariable(){
		this.tmgVariablesVariableId = -1;
	}
	public TmgVariablesVariable(int tmgVariablesVariable){
		this.tmgVariablesVariableId = tmgVariablesVariable;
	}
	public TmgVariablesVariable(int tmgVariablesId, int tmgVariablesVariableId, Datavalue name, Datavalue units, Datavalue vocabularyName){
		this.tmgVariablesId = tmgVariablesId;
		this.tmgVariablesVariableId = tmgVariablesVariableId;
		this.name=name;
		this.units=units;
		this.vocabularyName=vocabularyName;
	}
	public TmgVariablesVariable clone(){
		TmgVariablesVariable clone = new TmgVariablesVariable(this.tmgVariablesId, -1, this.name, this.units, this.vocabularyName);
		return clone;
	}
}
