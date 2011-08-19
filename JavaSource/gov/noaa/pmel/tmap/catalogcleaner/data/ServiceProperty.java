package gov.noaa.pmel.tmap.catalogcleaner.data;

public class ServiceProperty {
	protected int serviceId;
	protected int servicePropertyId;
	protected String value;
	protected String name;
	public void setServiceId(int serviceId){
		this.serviceId = serviceId;
	}
	public void setServicePropertyId(int servicePropertyId){
		this.servicePropertyId = servicePropertyId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setName(String name){
		this.name = name;
	}
	public int getServiceId(){
		return this.serviceId;
	}
	public int getServicePropertyId(){
		return this.servicePropertyId;
	}
	public String getValue(){
		return this.value;
	}
	public String getName(){
		return this.name;
	}

	public ServiceProperty(int serviceProperty){
		this.servicePropertyId = serviceProperty;
	}
	public ServiceProperty(int serviceId, int servicePropertyId, String value, String name){
		this.serviceId = serviceId;
		this.servicePropertyId = servicePropertyId;
		this.value=value;
		this.name=name;
	}
}
