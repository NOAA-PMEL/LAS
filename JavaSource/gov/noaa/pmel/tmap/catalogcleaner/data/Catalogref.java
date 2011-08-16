package gov.noaa.pmel.tmap.catalogcleaner.data;

public class Catalogref {
	protected int catalogrefId;
	protected int childId;
	protected int parentId;
	public void setChildId(int childId){
		this.childId = childId;
	}
	public void setParentId(int parentId){
		this.parentId = parentId;
	}
	public int getCatalogrefId(){
		return this.catalogrefId;
	}
	public int getChildId(){
		return this.childId;
	}
	public int getParentId(){
		return this.parentId;
	}

	public Catalogref(int id){
		this.catalogrefId=id;
	}
	public Catalogref(int catalogrefId, int childId, int parentId){
		this.catalogrefId=catalogrefId;
		this.childId=childId;
		this.parentId=parentId;
	}
}
