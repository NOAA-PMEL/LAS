package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgCreator {
	protected int tmgId;
	protected int tmgCreatorId;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgCreatorId(int tmgCreatorId){
		this.tmgCreatorId = tmgCreatorId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgCreatorId(){
		return this.tmgCreatorId;
	}

	public TmgCreator(){
		this.tmgCreatorId = -1;
	}
	public TmgCreator(int tmgCreator){
		this.tmgCreatorId = tmgCreator;
	}
	public TmgCreator(int tmgId, int tmgCreatorId){
		this.tmgId = tmgId;
		this.tmgCreatorId = tmgCreatorId;
	}
	public TmgCreator clone(){
		TmgCreator clone = new TmgCreator(this.tmgId, -1);
		return clone;
	}
}
