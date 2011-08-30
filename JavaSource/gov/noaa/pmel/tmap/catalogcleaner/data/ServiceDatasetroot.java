package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class ServiceDatasetroot {
	protected int serviceId;
	protected int serviceDatasetrootId;
	protected Datavalue location = new Datavalue(null);
	protected Datavalue path = new Datavalue(null);
	public void setServiceId(int serviceId){
		this.serviceId = serviceId;
	}
	public void setServiceDatasetrootId(int serviceDatasetrootId){
		this.serviceDatasetrootId = serviceDatasetrootId;
	}
	public void setLocation(String location){
		this.location = new Datavalue(location);
	}
	public void setPath(String path){
		this.path = new Datavalue(path);
	}
	public int getServiceId(){
		return this.serviceId;
	}
	public int getServiceDatasetrootId(){
		return this.serviceDatasetrootId;
	}
	public Datavalue getLocation(){
		return this.location;
	}
	public Datavalue getPath(){
		return this.path;
	}

	public ServiceDatasetroot(){
		this.serviceDatasetrootId = -1;
	}
	public ServiceDatasetroot(int serviceDatasetroot){
		this.serviceDatasetrootId = serviceDatasetroot;
	}
	public ServiceDatasetroot(int serviceId, int serviceDatasetrootId, Datavalue location, Datavalue path){
		this.serviceId = serviceId;
		this.serviceDatasetrootId = serviceDatasetrootId;
		this.location=location;
		this.path=path;
	}
	public ServiceDatasetroot clone(){
		ServiceDatasetroot clone = new ServiceDatasetroot(this.serviceId, -1, this.location, this.path);
		return clone;
	}
}
