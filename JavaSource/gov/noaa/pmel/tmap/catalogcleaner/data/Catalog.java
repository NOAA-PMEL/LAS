package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class Catalog {
	protected int catalogId;
	protected Datavalue cleanCatalogId = new Datavalue(null);
	protected Datavalue base = new Datavalue(null);
	protected Datavalue expires = new Datavalue(null);
	protected Datavalue name = new Datavalue(null);
	protected Datavalue version = new Datavalue(null);
	protected Datavalue xmlns = new Datavalue(null);
	protected Datavalue status = new Datavalue(null);
	public void setCatalogId(int catalogId){
		this.catalogId = catalogId;
	}
	public void setCleanCatalogId(String cleanCatalogId){
			this.cleanCatalogId = new Datavalue(cleanCatalogId);
		}
	public void setBase(String base){
		this.base = new Datavalue(base);
	}
	public void setExpires(String expires){
		this.expires = new Datavalue(expires);
	}
	public void setName(String name){
		this.name = new Datavalue(name);
	}
	public void setVersion(String version){
		this.version = new Datavalue(version);
	}
	public void setXmlns(String xmlns){
		this.xmlns = new Datavalue(xmlns);
	}
	public void setStatus(String status){
		this.status = new Datavalue(status);
	}
	public int getCatalogId(){
		return this.catalogId;
	}
	public Datavalue getCleanCatalogId(){
		return this.cleanCatalogId;
	}
	public Datavalue getBase(){
		return this.base;
	}
	public Datavalue getExpires(){
		return this.expires;
	}
	public Datavalue getName(){
		return this.name;
	}
	public Datavalue getVersion(){
		return this.version;
	}
	public Datavalue getXmlns(){
		return this.xmlns;
	}
	public Datavalue getStatus(){
		return this.status;
	}

	public Catalog(){
		this.catalogId = -1;
	}
	public Catalog(int catalog){
		this.catalogId = catalog;
	}
	public Catalog(int catalogId, Datavalue cleanCatalogId, Datavalue base, Datavalue expires, Datavalue name, Datavalue version, Datavalue xmlns, Datavalue status){
		this.catalogId = catalogId;
		this.cleanCatalogId = cleanCatalogId;
		this.base=base;
		this.expires=expires;
		this.name=name;
		this.version=version;
		this.xmlns=xmlns;
		this.status=status;
	}
	public Catalog clone(){
		Catalog clone = new Catalog(-1, new Datavalue(null), this.base, this.expires, this.name, this.version, this.xmlns, this.status);
		return clone;
	}
}
