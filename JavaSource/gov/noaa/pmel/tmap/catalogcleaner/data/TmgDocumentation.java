package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgDocumentation {
	protected int tmgId;
	protected int tmgDocumentationId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue documentationenum = new Datavalue(null);
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgDocumentationId(int tmgDocumentationId){
		this.tmgDocumentationId = tmgDocumentationId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setDocumentationenum(String documentationenum){
		this.documentationenum = new Datavalue(documentationenum);
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgDocumentationId(){
		return this.tmgDocumentationId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getDocumentationenum(){
		return this.documentationenum;
	}

	public TmgDocumentation(){
		this.tmgDocumentationId = -1;
	}
	public TmgDocumentation(int tmgDocumentation){
		this.tmgDocumentationId = tmgDocumentation;
	}
	public TmgDocumentation(int tmgId, int tmgDocumentationId, Datavalue value, Datavalue documentationenum){
		this.tmgId = tmgId;
		this.tmgDocumentationId = tmgDocumentationId;
		this.value=value;
		this.documentationenum=documentationenum;
	}
	public TmgDocumentation clone(){
		TmgDocumentation clone = new TmgDocumentation(this.tmgId, -1, this.value, this.documentationenum);
		return clone;
	}
}
