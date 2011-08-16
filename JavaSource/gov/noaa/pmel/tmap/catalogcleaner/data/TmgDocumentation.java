package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDocumentation {
	protected int tmgDocumentationId;
	protected int tmgId;
	protected String value;
	protected String documentationenum;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setDocumentationenum(String documentationenum){
		this.documentationenum = documentationenum;
	}
	public int getTmgDocumentationId(){
		return this.tmgDocumentationId;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public String getValue(){
		return this.value;
	}
	public String getDocumentationenum(){
		return this.documentationenum;
	}

	public TmgDocumentation(int id){
		this.tmgDocumentationId=id;
	}
	public TmgDocumentation(int tmgDocumentationId, String value, String documentationenum, int tmgId){
		this.tmgDocumentationId=tmgDocumentationId;
		this.value=value;
		this.documentationenum=documentationenum;
		this.tmgId=tmgId;
	}
}
