package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDocumentationXlink {
	protected int tmgDocumentationId;
	protected int tmgDocumentationXlinkId;
	protected String value;
	protected String xlink;
	public void setTmgDocumentationId(int tmgDocumentationId){
		this.tmgDocumentationId = tmgDocumentationId;
	}
	public void setTmgDocumentationXlinkId(int tmgDocumentationXlinkId){
		this.tmgDocumentationXlinkId = tmgDocumentationXlinkId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getTmgDocumentationId(){
		return this.tmgDocumentationId;
	}
	public int getTmgDocumentationXlinkId(){
		return this.tmgDocumentationXlinkId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public TmgDocumentationXlink(int tmgDocumentationXlink){
		this.tmgDocumentationXlinkId = tmgDocumentationXlink;
	}
	public TmgDocumentationXlink(int tmgDocumentationId, int tmgDocumentationXlinkId, String value, String xlink){
		this.tmgDocumentationId = tmgDocumentationId;
		this.tmgDocumentationXlinkId = tmgDocumentationXlinkId;
		this.value=value;
		this.xlink=xlink;
	}
}
