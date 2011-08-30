package gov.noaa.pmel.tmap.catalogcleaner;

public class Datavalue {
	boolean isnull = true;
	String value = "";
	public void NULL(){
		this.isnull = true;
	}
	public void NOTNULL(String s){
		this.isnull = false;
		this.value = s;
	}
	public Datavalue(){

	}
	public Datavalue(Object o){
		if(o!=null){
			this.NOTNULL(o.toString());
		}
	}
	public Datavalue(String s){
		if(s!=null)
			this.NOTNULL(s);
	}
	public boolean isNull(){
		return isnull;
	}
	public String getValue(){
		if(!isnull)
			return value;
		return null;
	}
}
