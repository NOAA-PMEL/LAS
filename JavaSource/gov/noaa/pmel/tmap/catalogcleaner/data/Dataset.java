package gov.noaa.pmel.tmap.catalogcleaner.data;

public class Dataset {
	protected int datasetId;
	protected String harvest;
	protected String name;
	protected String alias;
	protected String authority;
	protected String dId;
	protected String servicename;
	protected String urlpath;
	protected String resourcecontrol;
	protected String collectiontype;
	protected String status;
	protected String datatype;
	protected String datasizeUnit;
	public void setDatasetId(int datasetId){
		this.datasetId = datasetId;
	}
	public void setHarvest(String harvest){
		this.harvest = harvest;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setAlias(String alias){
		this.alias = alias;
	}
	public void setAuthority(String authority){
		this.authority = authority;
	}
	public void setDId(String dId){
		this.dId = dId;
	}
	public void setServicename(String servicename){
		this.servicename = servicename;
	}
	public void setUrlpath(String urlpath){
		this.urlpath = urlpath;
	}
	public void setResourcecontrol(String resourcecontrol){
		this.resourcecontrol = resourcecontrol;
	}
	public void setCollectiontype(String collectiontype){
		this.collectiontype = collectiontype;
	}
	public void setStatus(String status){
		this.status = status;
	}
	public void setDatatype(String datatype){
		this.datatype = datatype;
	}
	public void setDatasizeUnit(String datasizeUnit){
		this.datasizeUnit = datasizeUnit;
	}
	public int getDatasetId(){
		return this.datasetId;
	}
	public String getHarvest(){
		return this.harvest;
	}
	public String getName(){
		return this.name;
	}
	public String getAlias(){
		return this.alias;
	}
	public String getAuthority(){
		return this.authority;
	}
	public String getDId(){
		return this.dId;
	}
	public String getServicename(){
		return this.servicename;
	}
	public String getUrlpath(){
		return this.urlpath;
	}
	public String getResourcecontrol(){
		return this.resourcecontrol;
	}
	public String getCollectiontype(){
		return this.collectiontype;
	}
	public String getStatus(){
		return this.status;
	}
	public String getDatatype(){
		return this.datatype;
	}
	public String getDatasizeUnit(){
		return this.datasizeUnit;
	}

	public Dataset(int dataset){
		this.datasetId = dataset;
	}
	public Dataset(int datasetId, String harvest, String name, String alias, String authority, String dId, String servicename, String urlpath, String resourcecontrol, String collectiontype, String status, String datatype, String datasizeUnit){
		this.datasetId = datasetId;
		this.harvest=harvest;
		this.name=name;
		this.alias=alias;
		this.authority=authority;
		this.dId=dId;
		this.servicename=servicename;
		this.urlpath=urlpath;
		this.resourcecontrol=resourcecontrol;
		this.collectiontype=collectiontype;
		this.status=status;
		this.datatype=datatype;
		this.datasizeUnit=datasizeUnit;
	}
}
