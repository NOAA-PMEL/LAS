package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogXlink {
	protected int catalogId;
	protected int catalogXlinkId;
	protected String value;
	protected String xlink;
	public void setCatalogId(int catalogId){
		this.catalogId = catalogId;
	}
	public void setCatalogXlinkId(int catalogXlinkId){
		this.catalogXlinkId = catalogXlinkId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getCatalogId(){
		return this.catalogId;
	}
	public int getCatalogXlinkId(){
		return this.catalogXlinkId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public CatalogXlink(int catalogXlink){
		this.catalogXlinkId = catalogXlink;
	}
	public CatalogXlink(int catalogId, int catalogXlinkId, String value, String xlink){
		this.catalogId = catalogId;
		this.catalogXlinkId = catalogXlinkId;
		this.value=value;
		this.xlink=xlink;
	}
}
