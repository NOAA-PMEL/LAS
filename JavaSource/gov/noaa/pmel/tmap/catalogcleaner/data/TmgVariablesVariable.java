package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgVariablesVariable {
	protected int tmgVariablesVariableId;
	protected int tmgVariablesId;
	protected String units;
	protected String name;
	protected String vocabularyName;
	public void setTmgVariablesId(int tmgVariablesId){
		this.tmgVariablesId = tmgVariablesId;
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
	public int getTmgVariablesVariableId(){
		return this.tmgVariablesVariableId;
	}
	public int getTmgVariablesId(){
		return this.tmgVariablesId;
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

	public TmgVariablesVariable(int id){
		this.tmgVariablesVariableId=id;
	}
	public TmgVariablesVariable(int tmgVariablesVariableId, String units, String name, String vocabularyName, int tmgVariablesId){
		this.tmgVariablesVariableId=tmgVariablesVariableId;
		this.units=units;
		this.name=name;
		this.vocabularyName=vocabularyName;
		this.tmgVariablesId=tmgVariablesId;
	}
}
