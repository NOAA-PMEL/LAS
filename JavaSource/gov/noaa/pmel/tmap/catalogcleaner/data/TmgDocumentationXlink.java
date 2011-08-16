package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDocumentationXlink {
	protected int tmgDocumentationXlinkId;
	protected int tmgDocumentationId;
	protected String value;
	protected String xlink;
	public void setTmgDocumentationId(int tmgDocumentationId){
		this.tmgDocumentationId = tmgDocumentationId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getTmgDocumentationXlinkId(){
		return this.tmgDocumentationXlinkId;
	}
	public int getTmgDocumentationId(){
		return this.tmgDocumentationId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public TmgDocumentationXlink(int id){
		this.tmgDocumentationXlinkId=id;
	}
	public TmgDocumentationXlink(int tmgDocumentationXlinkId, String value, String xlink, int tmgDocumentationId){
		this.tmgDocumentationXlinkId=tmgDocumentationXlinkId;
		this.value=value;
		this.xlink=xlink;
		this.tmgDocumentationId=tmgDocumentationId;
	}
}
