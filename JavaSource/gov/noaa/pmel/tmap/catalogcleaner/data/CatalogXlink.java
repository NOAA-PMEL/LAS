package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogXlink {
	protected int catalogXlinkId;
	protected int catalogId;
	protected String value;
	protected String xlink;
	public void setCatalogId(int catalogId){
		this.catalogId = catalogId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getCatalogXlinkId(){
		return this.catalogXlinkId;
	}
	public int getCatalogId(){
		return this.catalogId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public CatalogXlink(int id){
		this.catalogXlinkId=id;
	}
	public CatalogXlink(int catalogXlinkId, String value, String xlink, int catalogId){
		this.catalogXlinkId=catalogXlinkId;
		this.value=value;
		this.xlink=xlink;
		this.catalogId=catalogId;
	}
}
