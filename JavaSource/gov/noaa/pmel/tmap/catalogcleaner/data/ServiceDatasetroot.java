package gov.noaa.pmel.tmap.catalogcleaner.data;

public class ServiceDatasetroot {
	protected int serviceId;
	protected int serviceDatasetrootId;
	protected String path;
	protected String location;
	public void setServiceId(int serviceId){
		this.serviceId = serviceId;
	}
	public void setServiceDatasetrootId(int serviceDatasetrootId){
		this.serviceDatasetrootId = serviceDatasetrootId;
	}
	public void setPath(String path){
		this.path = path;
	}
	public void setLocation(String location){
		this.location = location;
	}
	public int getServiceId(){
		return this.serviceId;
	}
	public int getServiceDatasetrootId(){
		return this.serviceDatasetrootId;
	}
	public String getPath(){
		return this.path;
	}
	public String getLocation(){
		return this.location;
	}

	public ServiceDatasetroot(int serviceDatasetroot){
		this.serviceDatasetrootId = serviceDatasetroot;
	}
	public ServiceDatasetroot(int serviceId, int serviceDatasetrootId, String path, String location){
		this.serviceId = serviceId;
		this.serviceDatasetrootId = serviceDatasetrootId;
		this.path=path;
		this.location=location;
	}
}
