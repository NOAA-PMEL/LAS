package gov.noaa.pmel.tmap.ferret.test;

import java.io.*;

/** Provides a convenient access mechanism for
 *  temporary disk storage. Each thread receives a separate namespace
 *  for its entries, thus minimizing the possibility of namespace
 *  conflicts.
 */
public class Store
    extends AbstractModule {

    public String getModuleID() {
	return "store";
    }

    public Store(String homeDir) 
       throws Exception {

        baseDir = new File(homeDir, "temp");
	if (!baseDir.exists()) {
	    if (!baseDir.mkdirs()) {
		throw new Exception("couldn't create temp dir " + 
					  baseDir.getAbsolutePath());
	    }
	}
    }
    
    /** Returns the file handle associated with the given entry name<p> 
     *  It is guaranteed that all parent directories 
     *  exist for the handle before it is returned.
     */
    public File get(String entryName) {
        String taskName = Task.currentTask().getFullName();
        return get(taskName, entryName);
    }

    /** Returns the file handle associated with the given entry name, for 
     *  the given thread.<p> 
     *  It is guaranteed that all parent directories 
     *  exist for the handle before it is returned.
     */
    public File get(String taskName, String entryName) {
	File entryFile = new File(baseDir.getAbsolutePath()+"/"+taskName, 
                                  entryName);
	if (!entryFile.exists()) {
	    File entryDir = entryFile.getParentFile();
	    entryDir.mkdirs();
	}
	return entryFile;
    }

    public void clear(){
        File[] fileArray = baseDir.listFiles();
        for(int i=0;i<fileArray.length; i++){
            remove(fileArray[i]);
        }
    }

    protected void remove(File file){
        if(file.isDirectory()){
            File[] fileArray = file.listFiles();
            for(int i=0;i<fileArray.length; i++){
               remove(fileArray[i]);
            }
        }
        file.delete();
    }

    protected File baseDir;

}
