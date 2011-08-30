package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class Dataset {
	protected int datasetId;
	protected Datavalue alias = new Datavalue(null);
	protected Datavalue authority = new Datavalue(null);
	protected Datavalue dId = new Datavalue(null);
	protected Datavalue harvest = new Datavalue(null);
	protected Datavalue name = new Datavalue(null);
	protected Datavalue resourcecontrol = new Datavalue(null);
	protected Datavalue serviceName = new Datavalue(null);
	protected Datavalue urlPath = new Datavalue(null);
	protected Datavalue collectiontype = new Datavalue(null);
	protected Datavalue datasizeUnit = new Datavalue(null);
	protected Datavalue dataType = new Datavalue(null);
	protected Datavalue status = new Datavalue(null);
	public void setDatasetId(int datasetId){
		this.datasetId = datasetId;
	}
	public void setAlias(String alias){
		this.alias = new Datavalue(alias);
	}
	public void setAuthority(String authority){
		this.authority = new Datavalue(authority);
	}
	public void setDId(String dId){
		this.dId = new Datavalue(dId);
	}
	public void setHarvest(String harvest){
		this.harvest = new Datavalue(harvest);
	}
	public void setName(String name){
		this.name = new Datavalue(name);
	}
	public void setResourcecontrol(String resourcecontrol){
		this.resourcecontrol = new Datavalue(resourcecontrol);
	}
	public void setServiceName(String serviceName){
		this.serviceName = new Datavalue(serviceName);
	}
	public void setUrlPath(String urlPath){
		this.urlPath = new Datavalue(urlPath);
	}
	public void setCollectiontype(String collectiontype){
		this.collectiontype = new Datavalue(collectiontype);
	}
	public void setDatasizeUnit(String datasizeUnit){
		this.datasizeUnit = new Datavalue(datasizeUnit);
	}
	public void setDataType(String dataType){
		this.dataType = new Datavalue(dataType);
	}
	public void setStatus(String status){
		this.status = new Datavalue(status);
	}
	public int getDatasetId(){
		return this.datasetId;
	}
	public Datavalue getAlias(){
		return this.alias;
	}
	public Datavalue getAuthority(){
		return this.authority;
	}
	public Datavalue getDId(){
		return this.dId;
	}
	public Datavalue getHarvest(){
		return this.harvest;
	}
	public Datavalue getName(){
		return this.name;
	}
	public Datavalue getResourcecontrol(){
		return this.resourcecontrol;
	}
	public Datavalue getServiceName(){
		return this.serviceName;
	}
	public Datavalue getUrlPath(){
		return this.urlPath;
	}
	public Datavalue getCollectiontype(){
		return this.collectiontype;
	}
	public Datavalue getDatasizeUnit(){
		return this.datasizeUnit;
	}
	public Datavalue getDataType(){
		return this.dataType;
	}
	public Datavalue getStatus(){
		return this.status;
	}

	public Dataset(){
		this.datasetId = -1;
	}
	public Dataset(int dataset){
		this.datasetId = dataset;
	}
	public Dataset(int datasetId, Datavalue alias, Datavalue authority, Datavalue dId, Datavalue harvest, Datavalue name, Datavalue resourcecontrol, Datavalue serviceName, Datavalue urlPath, Datavalue collectiontype, Datavalue datasizeUnit, Datavalue dataType, Datavalue status){
		this.datasetId = datasetId;
		this.alias=alias;
		this.authority=authority;
		this.dId=dId;
		this.harvest=harvest;
		this.name=name;
		this.resourcecontrol=resourcecontrol;
		this.serviceName=serviceName;
		this.urlPath=urlPath;
		this.collectiontype=collectiontype;
		this.datasizeUnit=datasizeUnit;
		this.dataType=dataType;
		this.status=status;
	}
	public Dataset clone(){
		Dataset clone = new Dataset(-1, this.alias, this.authority, this.dId, this.harvest, this.name, this.resourcecontrol, this.serviceName, this.urlPath, this.collectiontype, this.datasizeUnit, this.dataType, this.status);
		return clone;
	}
}
