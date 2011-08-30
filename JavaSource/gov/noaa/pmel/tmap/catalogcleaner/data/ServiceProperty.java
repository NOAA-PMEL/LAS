package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class ServiceProperty {
	protected int serviceId;
	protected int servicePropertyId;
	protected Datavalue name = new Datavalue(null);
	protected Datavalue value = new Datavalue(null);
	public void setServiceId(int serviceId){
		this.serviceId = serviceId;
	}
	public void setServicePropertyId(int servicePropertyId){
		this.servicePropertyId = servicePropertyId;
	}
	public void setName(String name){
		this.name = new Datavalue(name);
	}
	public void setValue(String value){
		this.value = new Datavalue(value);
	}
	public int getServiceId(){
		return this.serviceId;
	}
	public int getServicePropertyId(){
		return this.servicePropertyId;
	}
	public Datavalue getName(){
		return this.name;
	}
	public Datavalue getValue(){
		return this.value;
	}

	public ServiceProperty(){
		this.servicePropertyId = -1;
	}
	public ServiceProperty(int serviceProperty){
		this.servicePropertyId = serviceProperty;
	}
	public ServiceProperty(int serviceId, int servicePropertyId, Datavalue name, Datavalue value){
		this.serviceId = serviceId;
		this.servicePropertyId = servicePropertyId;
		this.name=name;
		this.value=value;
	}
	public ServiceProperty clone(){
		ServiceProperty clone = new ServiceProperty(this.serviceId, -1, this.name, this.value);
		return clone;
	}
}
