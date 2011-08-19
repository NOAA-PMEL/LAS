package gov.noaa.pmel.tmap.catalogcleaner.data;

public class TmgDocumentation {
	protected int tmgId;
	protected int tmgDocumentationId;
	protected String value;
	protected String documentationenum;
	public void setTmgId(int tmgId){
		this.tmgId = tmgId;
	}
	public void setTmgDocumentationId(int tmgDocumentationId){
		this.tmgDocumentationId = tmgDocumentationId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setDocumentationenum(String documentationenum){
		this.documentationenum = documentationenum;
	}
	public int getTmgId(){
		return this.tmgId;
	}
	public int getTmgDocumentationId(){
		return this.tmgDocumentationId;
	}
	public String getValue(){
		return this.value;
	}
	public String getDocumentationenum(){
		return this.documentationenum;
	}

	public TmgDocumentation(int tmgDocumentation){
		this.tmgDocumentationId = tmgDocumentation;
	}
	public TmgDocumentation(int tmgId, int tmgDocumentationId, String value, String documentationenum){
		this.tmgId = tmgId;
		this.tmgDocumentationId = tmgDocumentationId;
		this.value=value;
		this.documentationenum=documentationenum;
	}
}
