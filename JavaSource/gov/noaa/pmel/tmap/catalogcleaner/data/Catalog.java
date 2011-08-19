package gov.noaa.pmel.tmap.catalogcleaner.data;

public class Catalog {
	protected int catalogId;
	protected String name;
	protected String expires;
	protected String version;
	protected String base;
	protected String xmlns;
	protected String status;
	public void setCatalogId(int catalogId){
		this.catalogId = catalogId;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setExpires(String expires){
		this.expires = expires;
	}
	public void setVersion(String version){
		this.version = version;
	}
	public void setBase(String base){
		this.base = base;
	}
	public void setXmlns(String xmlns){
		this.xmlns = xmlns;
	}
	public void setStatus(String status){
		this.status = status;
	}
	public int getCatalogId(){
		return this.catalogId;
	}
	public String getName(){
		return this.name;
	}
	public String getExpires(){
		return this.expires;
	}
	public String getVersion(){
		return this.version;
	}
	public String getBase(){
		return this.base;
	}
	public String getXmlns(){
		return this.xmlns;
	}
	public String getStatus(){
		return this.status;
	}

	public Catalog(int catalog){
		this.catalogId = catalog;
	}
	public Catalog(int catalogId, String name, String expires, String version, String base, String xmlns, String status){
		this.catalogId = catalogId;
		this.name=name;
		this.expires=expires;
		this.version=version;
		this.base=base;
		this.xmlns=xmlns;
		this.status=status;
	}
}
