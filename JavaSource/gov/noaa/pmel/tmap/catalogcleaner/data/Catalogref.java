package gov.noaa.pmel.tmap.catalogcleaner.data;

public class Catalogref {
	protected int catalogrefId;
	public void setCatalogrefId(int catalogrefId){
		this.catalogrefId = catalogrefId;
	}
	public int getCatalogrefId(){
		return this.catalogrefId;
	}

	public Catalogref(int catalogref){
		this.catalogrefId = catalogref;
	}
}
