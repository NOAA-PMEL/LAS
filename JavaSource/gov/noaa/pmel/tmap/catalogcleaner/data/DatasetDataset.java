package gov.noaa.pmel.tmap.catalogcleaner.data;

public class DatasetDataset extends Dataset {
	private int parentId;

	public int getParentId(){
		return this.parentId;
	}

	public DatasetDataset(int parentId, int datasetId){
		super(datasetId);
		this.parentId = parentId;
	}
	public DatasetDataset(int parentId, int datasetId, String harvest, String name, String alias, String authority, String d_id, String servicename, String urlpath, String resourcecontrol, String collectiontype, String status, String datatype, String datasize_unit){
		super(datasetId, harvest, name, alias, authority, d_id, servicename, urlpath, resourcecontrol, collectiontype, status, datatype, datasize_unit);
		this.parentId = parentId;
	}
}

