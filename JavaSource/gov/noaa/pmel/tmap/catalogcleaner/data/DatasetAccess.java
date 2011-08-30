package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class DatasetAccess {
	protected int datasetId;
	protected int datasetAccessId;
	protected Datavalue servicename = new Datavalue(null);
	protected Datavalue urlpath = new Datavalue(null);
	protected Datavalue dataformat = new Datavalue(null);
	public void setDatasetId(int datasetId){
		this.datasetId = datasetId;
	}
	public void setDatasetAccessId(int datasetAccessId){
		this.datasetAccessId = datasetAccessId;
	}
	public void setServicename(String servicename){
		this.servicename = new Datavalue(servicename);
	}
	public void setUrlpath(String urlpath){
		this.urlpath = new Datavalue(urlpath);
	}
	public void setDataformat(String dataformat){
		this.dataformat = new Datavalue(dataformat);
	}
	public int getDatasetId(){
		return this.datasetId;
	}
	public int getDatasetAccessId(){
		return this.datasetAccessId;
	}
	public Datavalue getServicename(){
		return this.servicename;
	}
	public Datavalue getUrlpath(){
		return this.urlpath;
	}
	public Datavalue getDataformat(){
		return this.dataformat;
	}

	public DatasetAccess(){
		this.datasetAccessId = -1;
	}
	public DatasetAccess(int datasetAccess){
		this.datasetAccessId = datasetAccess;
	}
	public DatasetAccess(int datasetId, int datasetAccessId, Datavalue servicename, Datavalue urlpath, Datavalue dataformat){
		this.datasetId = datasetId;
		this.datasetAccessId = datasetAccessId;
		this.servicename=servicename;
		this.urlpath=urlpath;
		this.dataformat=dataformat;
	}
	public DatasetAccess clone(){
		DatasetAccess clone = new DatasetAccess(this.datasetId, -1, this.servicename, this.urlpath, this.dataformat);
		return clone;
	}
}
