package gov.noaa.pmel.tmap.ferret.server.importer;

import java.io.*;
import java.text.*;
import java.util.*;
import org.w3c.dom.*;

import org.iges.util.FileResolver;
import org.iges.anagram.*;
import gov.noaa.pmel.tmap.ferret.server.*;

/** A handler for importing various kinds of data
 */
public abstract class Importer
    extends AbstractModule {

    public void configure(Setting setting) {
    }

    public String getModuleID() {
	if (moduleID == null) {
	    moduleID = "import-" + getImporterName();
	}
	return moduleID;
    }

    /** Returns the name of this importer. Used to 
     * map tag name to importer. 
     */
    public abstract String getImporterName();

    /** Returns a list of handles from a tag
     *  @param tag the xml tag to be imported
     *  @param baseDir the current base directory
     *  @return a list of handles
     */
    public abstract List getHandlesFromTag(Element tag, String baseDir);
 
    /** Creates a data handle according to the given information
     * @param name the name for data handle
     * @param sourceFile the path name of the source file
     * @param docURL the document URL for the data handle
     * @param init_script the init_script file name
     * @param format the format of the dataset, either "jnl" or "use"
     * @return a new data handle to the specified data set
     */
    public DataHandle createHandle(String name, 
				   String sourceFile, 
				   String docURL,
                                   String userDAS,
				   String format,
                                   String variables,
                                   String environment,
                                   String workDir) 
	throws AnagramException {

	Handle handle = server.getCatalog().getLocked(name);

	if (handle instanceof DirHandle) {
            if(handle!=null){
                handle.getSynch().release();
	    }
	    throw new AnagramException("a directory exists by that name");
	}

	FerretDataInfo info = null;

	File file = FileResolver.resolve(server.getHome(), sourceFile);
	if (file.exists()) {
	    sourceFile = file.getAbsolutePath();
	} 

	info = new FerretDataInfo(name, 
                                  sourceFile,
                                  variables,
                                  format, 
                                  name, 
                                  docURL,
                                  userDAS,
                                  environment,
                                  workDir);

	if (handle != null) {
	    DataHandle oldData = (DataHandle)handle;
	    FerretDataInfo oldInfo = (FerretDataInfo)oldData.getToolInfo();

	    handle.getSynch().release();
	    if (oldInfo.equals(info)) {
		if (debug()) debug("settings for " + 
				   oldData.getCompleteName() + 
				   " are unchanged");
		return oldData;
	    }
	}

	DataHandle newData = new DataHandle(name, 
					   info.getTitle(), 
					   info,
					   System.currentTimeMillis());
	log.info(this, "imported dataset " + name);
	return newData;
    }
    

    protected boolean enabled = true;
    protected String moduleID;
    
}
