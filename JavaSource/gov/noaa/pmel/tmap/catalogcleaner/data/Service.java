package gov.noaa.pmel.tmap.catalogcleaner.data;

public class Service {
	protected int serviceId;
	protected String suffix;
	protected String name;
	protected String base;
	protected String desc;
	protected String servicetype;
	protected String status;
	public void setSuffix(String suffix){
		this.suffix = suffix;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setBase(String base){
		this.base = base;
	}
	public void setDesc(String desc){
		this.desc = desc;
	}
	public void setServicetype(String servicetype){
		this.servicetype = servicetype;
	}
	public void setStatus(String status){
		this.status = status;
	}
	public int getServiceId(){
		return this.serviceId;
	}
	public String getSuffix(){
		return this.suffix;
	}
	public String getName(){
		return this.name;
	}
	public String getBase(){
		return this.base;
	}
	public String getDesc(){
		return this.desc;
	}
	public String getServicetype(){
		return this.servicetype;
	}
	public String getStatus(){
		return this.status;
	}

	public Service(int id){
		this.serviceId=id;
	}
	public Service(int serviceId, String suffix, String name, String base, String desc, String servicetype, String status){
		this.serviceId=serviceId;
		this.suffix=suffix;
		this.name=name;
		this.base=base;
		this.desc=desc;
		this.servicetype=servicetype;
		this.status=status;
	}
}
