package gov.noaa.pmel.tmap.catalogcleaner.data;

public class CatalogrefXlink {
	protected int catalogrefId;
	protected int catalogrefXlinkId;
	protected String value;
	protected String xlink;
	public void setCatalogrefId(int catalogrefId){
		this.catalogrefId = catalogrefId;
	}
	public void setCatalogrefXlinkId(int catalogrefXlinkId){
		this.catalogrefXlinkId = catalogrefXlinkId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setXlink(String xlink){
		this.xlink = xlink;
	}
	public int getCatalogrefId(){
		return this.catalogrefId;
	}
	public int getCatalogrefXlinkId(){
		return this.catalogrefXlinkId;
	}
	public String getValue(){
		return this.value;
	}
	public String getXlink(){
		return this.xlink;
	}

	public CatalogrefXlink(int catalogrefXlink){
		this.catalogrefXlinkId = catalogrefXlink;
	}
	public CatalogrefXlink(int catalogrefId, int catalogrefXlinkId, String value, String xlink){
		this.catalogrefId = catalogrefId;
		this.catalogrefXlinkId = catalogrefXlinkId;
		this.value=value;
		this.xlink=xlink;
	}
}
