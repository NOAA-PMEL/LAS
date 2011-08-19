package gov.noaa.pmel.tmap.catalogcleaner.data;

public class DatasetAccess {
	protected int datasetId;
	protected int datasetAccessId;
	protected String urlpath;
	protected String servicename;
	protected String dataformat;
	public void setDatasetId(int datasetId){
		this.datasetId = datasetId;
	}
	public void setDatasetAccessId(int datasetAccessId){
		this.datasetAccessId = datasetAccessId;
	}
	public void setUrlpath(String urlpath){
		this.urlpath = urlpath;
	}
	public void setServicename(String servicename){
		this.servicename = servicename;
	}
	public void setDataformat(String dataformat){
		this.dataformat = dataformat;
	}
	public int getDatasetId(){
		return this.datasetId;
	}
	public int getDatasetAccessId(){
		return this.datasetAccessId;
	}
	public String getUrlpath(){
		return this.urlpath;
	}
	public String getServicename(){
		return this.servicename;
	}
	public String getDataformat(){
		return this.dataformat;
	}

	public DatasetAccess(int datasetAccess){
		this.datasetAccessId = datasetAccess;
	}
	public DatasetAccess(int datasetId, int datasetAccessId, String urlpath, String servicename, String dataformat){
		this.datasetId = datasetId;
		this.datasetAccessId = datasetAccessId;
		this.urlpath=urlpath;
		this.servicename=servicename;
		this.dataformat=dataformat;
	}
}
