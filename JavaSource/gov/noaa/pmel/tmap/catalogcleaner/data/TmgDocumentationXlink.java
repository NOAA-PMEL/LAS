package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class TmgDocumentationXlink {
	protected int tmgDocumentationId;
	protected int tmgDocumentationXlinkId;
	protected Datavalue value = new Datavalue(null);
	protected Datavalue xlink = new Datavalue(null);
	public void setTmgDocumentationId(int tmgDocumentationId){
		this.tmgDocumentationId = tmgDocumentationId;
	}
	public void setTmgDocumentationXlinkId(int tmgDocumentationXlinkId){
		this.tmgDocumentationXlinkId = tmgDocumentationXlinkId;
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public void setXlink(String xlink){
		this.xlink = new Datavalue(xlink);
	}
	public int getTmgDocumentationId(){
		return this.tmgDocumentationId;
	}
	public int getTmgDocumentationXlinkId(){
		return this.tmgDocumentationXlinkId;
	}
	public Datavalue getValue(){
		return this.value;
	}
	public Datavalue getXlink(){
		return this.xlink;
	}

	public TmgDocumentationXlink(){
		this.tmgDocumentationXlinkId = -1;
	}
	public TmgDocumentationXlink(int tmgDocumentationXlink){
		this.tmgDocumentationXlinkId = tmgDocumentationXlink;
	}
	public TmgDocumentationXlink(int tmgDocumentationId, int tmgDocumentationXlinkId, Datavalue value, Datavalue xlink){
		this.tmgDocumentationId = tmgDocumentationId;
		this.tmgDocumentationXlinkId = tmgDocumentationXlinkId;
		this.value=value;
		this.xlink=xlink;
	}
	public TmgDocumentationXlink clone(){
		TmgDocumentationXlink clone = new TmgDocumentationXlink(this.tmgDocumentationId, -1, this.value, this.xlink);
		return clone;
	}
}
