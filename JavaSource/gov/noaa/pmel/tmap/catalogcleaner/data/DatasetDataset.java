package gov.noaa.pmel.tmap.catalogcleaner.data;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
public class DatasetDataset extends Dataset {
	private int parentId;

	public int getParentId(){
		return this.parentId;
	}

	public int getChildId(){
		return this.datasetId;
	}

	public DatasetDataset(int parentId, int datasetId){
		super(datasetId);
		this.parentId = parentId;
	}
	public DatasetDataset(int parentId, int datasetId, Datavalue alias, Datavalue authority, Datavalue d_id, Datavalue harvest, Datavalue name, Datavalue resourcecontrol, Datavalue serviceName, Datavalue urlPath, Datavalue collectiontype, Datavalue datasize_unit, Datavalue dataType, Datavalue status){
		super(datasetId, alias, authority, d_id, harvest, name, resourcecontrol, serviceName, urlPath, collectiontype, datasize_unit, dataType, status);
		this.parentId = parentId;
	}
}

