package gov.noaa.pmel.tmap.ferret.server;

import java.io.*;

import org.iges.anagram.*;
import org.iges.util.FileResolver;

/** Updates a data handle to be in synch with the actual data
 *  source. This provides a quick way for the Catalog to check for
 *  changes to the back-end data store. 
 *
 */
public class FerretUpdateModule
    extends AbstractModule {

    public String getModuleID() {
	return "updater";
    }

    public FerretUpdateModule(FerretTool tool) {
	this.tool = tool;
    }


    public void configure(Setting setting)
	throws ConfigException {
    }

    /** Checks if the DataHandle provided is out of date and updates
     * it if necessary.  A DataHandle is considered out of date iff the
     * descriptor file, source file, or supplemental DAS file have
     * been modified or deleted since the DataHandle was created.
     *  @return null if the Datahandle has not changed, or else, the
     *  new, updated DataHandle object
     *  @throws ModuleException if the data can no longer be accessed.
     */
    public boolean doUpdate(DataHandle data) 
	throws ModuleException {

	info("doUpdate for "+data.getCompleteName());
	long createTime = data.getCreateTime();
	boolean modified = false;

	if (!data.isAvailable()) {
	    modified = true;	
	}

	FerretDataInfo info = (FerretDataInfo)data.getToolInfo();
        if((info.getFormat()==null||!info.getFormat().equals("jnl"))
           && !info.getSourceFile().startsWith("http:")) {
 	    File sourceFile = FileResolver.resolve(server.getHome(), 
                                                   info.getSourceFile());
	    if (sourceFile != null) {
	        if (!sourceFile.exists()) {
		    //throw new ModuleException(this, "source file moved or deleted");
                    //Hope for the best
                    return false;
	        }
	        if (fileModified(sourceFile, createTime)) {
		    if (debug()) debug("source file has changed");
		    modified = true;	
	        }
	    } 
	}
        else {
	    return false;
	}

	if (modified) {
	    reload(data);
	}
	
	return modified;
	
    }
    
    /** Returns true if <code>file</code> has been modified since <code>createTime</code>
     *
     * @param file the file to be checked
     * @param createTime the creation time of the data handle to the file
     */
    protected boolean fileModified(File file, long createTime) {
	return !file.exists() || file.lastModified() > createTime;
    }

    /** Brings internal structures in a DataHandle up-to-date with
     *  respect to the data files. There is redundancy here with
     *  respect to the code in FerretImportModule, which indicates that
     *  the design could definitely be improved.  
     */
    protected void reload(DataHandle data) {

	if (debug()) debug(data.getCompleteName() + 
			   " is out of date; regenerating");
	if (debug()) debug("data lock: " + data.getSynch());

	// prevent any access during the update

	if(data.getSynch().tryLockExclusive()){

	    FerretDataInfo oldInfo = (FerretDataInfo)data.getToolInfo();

            data.setDescription(oldInfo.getTitle());
	    data.setAvailable(true);
            data.getSynch().releaseExclusive();
	} 
    }

    /** A reference to {@link FerretTool} module
     */
    protected FerretTool tool;
}
