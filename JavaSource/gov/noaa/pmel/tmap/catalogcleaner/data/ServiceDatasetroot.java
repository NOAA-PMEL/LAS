package gov.noaa.pmel.tmap.catalogcleaner.data;

public class ServiceDatasetroot {
	protected int serviceDatasetrootId;
	protected int serviceId;
	protected String path;
	protected String location;
	public void setServiceId(int serviceId){
		this.serviceId = serviceId;
	}
	public void setPath(String path){
		this.path = path;
	}
	public void setLocation(String location){
		this.location = location;
	}
	public int getServiceDatasetrootId(){
		return this.serviceDatasetrootId;
	}
	public int getServiceId(){
		return this.serviceId;
	}
	public String getPath(){
		return this.path;
	}
	public String getLocation(){
		return this.location;
	}

	public ServiceDatasetroot(int id){
		this.serviceDatasetrootId=id;
	}
	public ServiceDatasetroot(int serviceDatasetrootId, String path, String location, int serviceId){
		this.serviceDatasetrootId=serviceDatasetrootId;
		this.path=path;
		this.location=location;
		this.serviceId=serviceId;
	}
}
