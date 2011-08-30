package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class Service {
	protected int serviceId;
	protected Datavalue base = new Datavalue(null);
	protected Datavalue desc = new Datavalue(null);
	protected Datavalue name = new Datavalue(null);
	protected Datavalue suffix = new Datavalue(null);
	protected Datavalue serviceType = new Datavalue(null);
	protected Datavalue status = new Datavalue(null);
	public void setServiceId(int serviceId){
		this.serviceId = serviceId;
	}
	public void setBase(String base){
		this.base = new Datavalue(base);
	}
	public void setDesc(String desc){
		this.desc = new Datavalue(desc);
	}
	public void setName(String name){
		this.name = new Datavalue(name);
	}
	public void setSuffix(String suffix){
		this.suffix = new Datavalue(suffix);
	}
	public void setServiceType(String serviceType){
		this.serviceType = new Datavalue(serviceType);
	}
	public void setStatus(String status){
		this.status = new Datavalue(status);
	}
	public int getServiceId(){
		return this.serviceId;
	}
	public Datavalue getBase(){
		return this.base;
	}
	public Datavalue getDesc(){
		return this.desc;
	}
	public Datavalue getName(){
		return this.name;
	}
	public Datavalue getSuffix(){
		return this.suffix;
	}
	public Datavalue getServiceType(){
		return this.serviceType;
	}
	public Datavalue getStatus(){
		return this.status;
	}

	public Service(){
		this.serviceId = -1;
	}
	public Service(int service){
		this.serviceId = service;
	}
	public Service(int serviceId, Datavalue base, Datavalue desc, Datavalue name, Datavalue suffix, Datavalue serviceType, Datavalue status){
		this.serviceId = serviceId;
		this.base=base;
		this.desc=desc;
		this.name=name;
		this.suffix=suffix;
		this.serviceType=serviceType;
		this.status=status;
	}
	public Service clone(){
		Service clone = new Service(-1, this.base, this.desc, this.name, this.suffix, this.serviceType, this.status);
		return clone;
	}
}
