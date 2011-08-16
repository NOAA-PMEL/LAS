package gov.noaa.pmel.tmap.catalogcleaner.data;

public class ServiceProperty {
	protected int servicePropertyId;
	protected int serviceId;
	protected String value;
	protected String name;
	public void setServiceId(int serviceId){
		this.serviceId = serviceId;
	}
	public void setValue(String value){
		this.value = value;
	}
	public void setName(String name){
		this.name = name;
	}
	public int getServicePropertyId(){
		return this.servicePropertyId;
	}
	public int getServiceId(){
		return this.serviceId;
	}
	public String getValue(){
		return this.value;
	}
	public String getName(){
		return this.name;
	}

	public ServiceProperty(int id){
		this.servicePropertyId=id;
	}
	public ServiceProperty(int servicePropertyId, String value, String name, int serviceId){
		this.servicePropertyId=servicePropertyId;
		this.value=value;
		this.name=name;
		this.serviceId=serviceId;
	}
}
