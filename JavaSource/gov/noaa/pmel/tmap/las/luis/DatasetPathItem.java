package gov.noaa.pmel.tmap.las.luis;

import java.lang.String;

public class DatasetPathItem {
    String mPath, mID;
    public DatasetPathItem(String path, String ID){
	mPath = path;
	mID = ID;
    }
    public String getPath() { return mPath; }
    public String getID() { return mID; }
}
