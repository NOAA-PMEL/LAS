package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogrefXlink {
	protected int catalogrefXlinkId;
	protected int catalogrefId;
	protected String value;
	protected String xlink;
	public void setCatalogrefId(int catalogrefId){
		this.catalogrefId = catalogrefId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getCatalogrefXlinkId(){
		return this.catalogrefXlinkId;
	}
	public int getCatalogrefId(){
		return this.catalogrefId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public CatalogrefXlink(int id){
		this.catalogrefXlinkId=id;
	}
	public CatalogrefXlink(int catalogrefXlinkId, String value, String xlink, int catalogrefId){
		this.catalogrefXlinkId=catalogrefXlinkId;
		this.value=value;
		this.xlink=xlink;
		this.catalogrefId=catalogrefId;
	}
}
