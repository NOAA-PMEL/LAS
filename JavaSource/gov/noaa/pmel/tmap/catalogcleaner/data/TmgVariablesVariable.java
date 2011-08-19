package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgVariablesVariable {
	protected int tmgVariablesId;
	protected int tmgVariablesVariableId;
	protected String units;
	protected String name;
	protected String vocabularyName;
	public void setTmgVariablesId(int tmgVariablesId){
		this.tmgVariablesId = tmgVariablesId;
	}
	public void setTmgVariablesVariableId(int tmgVariablesVariableId){
		this.tmgVariablesVariableId = tmgVariablesVariableId;
	}
	public void setUnits(String units){
		this.units = units;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setVocabularyName(String vocabularyName){
		this.vocabularyName = vocabularyName;
	}
	public int getTmgVariablesId(){
		return this.tmgVariablesId;
	}
	public int getTmgVariablesVariableId(){
		return this.tmgVariablesVariableId;
	}
	public String getUnits(){
		return this.units;
	}
	public String getName(){
		return this.name;
	}
	public String getVocabularyName(){
		return this.vocabularyName;
	}

	public TmgVariablesVariable(int tmgVariablesVariable){
		this.tmgVariablesVariableId = tmgVariablesVariable;
	}
	public TmgVariablesVariable(int tmgVariablesId, int tmgVariablesVariableId, String units, String name, String vocabularyName){
		this.tmgVariablesId = tmgVariablesId;
		this.tmgVariablesVariableId = tmgVariablesVariableId;
		this.units=units;
		this.name=name;
		this.vocabularyName=vocabularyName;
	}
}
