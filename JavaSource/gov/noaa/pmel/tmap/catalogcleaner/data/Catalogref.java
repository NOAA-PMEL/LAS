package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class Catalogref {
	protected int catalogrefId;
	public void setCatalogrefId(int catalogrefId){
		this.catalogrefId = catalogrefId;
	}
	public int getCatalogrefId(){
		return this.catalogrefId;
	}

	public Catalogref(){
		this.catalogrefId = -1;
	}
	public Catalogref(int catalogref){
		this.catalogrefId = catalogref;
	}
	public Catalogref clone(){
		Catalogref clone = new Catalogref(-1);
		return clone;
	}
}
