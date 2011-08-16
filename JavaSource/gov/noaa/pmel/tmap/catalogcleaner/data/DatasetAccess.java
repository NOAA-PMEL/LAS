package gov.noaa.pmel.tmap.catalogcleaner.data;

public class DatasetAccess {
	protected int datasetAccessId;
	protected int datasetId;
	protected String urlpath;
	protected String servicename;
	protected String dataformat;
	public void setDatasetId(int datasetId){
		this.datasetId = datasetId;
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
	public int getDatasetAccessId(){
		return this.datasetAccessId;
	}
	public int getDatasetId(){
		return this.datasetId;
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

	public DatasetAccess(int id){
		this.datasetAccessId=id;
	}
	public DatasetAccess(int datasetAccessId, String urlpath, String servicename, String dataformat, int datasetId){
		this.datasetAccessId=datasetAccessId;
		this.urlpath=urlpath;
		this.servicename=servicename;
		this.dataformat=dataformat;
		this.datasetId=datasetId;
	}
}
