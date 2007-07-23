
package gov.noaa.pmel.tmap.ferret.server;

import java.lang.*;
import java.io.*;
import org.iges.anagram.*;

/** This class holds information about a dataset
 *
 * @author Richard Roger, Yonghua Wei
 */

public class FerretDataInfo 
      extends ToolInfo {

    /** Construct a FerretDataInfo class
     *
     * @param dodsName the name for this dataset used by DODS
     * @param sourceFile the source file name for this dataset
     * @param variables the specified output variables
     * @param format the format of the source file
     * @param title title for this dataset
     * @param docURL document URL for this dataset
     * @param userDAS a local DAS file provided by user for this dataset
     * @param environment environment variables used for this dataset
     * @param workDir the working directory for Ferret when using this dataset
     */
    public FerretDataInfo (String dodsName,
                           String sourceFile,
                           String variables,
                           String format,
                           String title,
                           String docURL,
                           String userDAS,
                           String environment,
                           String workDir) {
        this.dodsName   = dodsName;
	this.sourceFile = sourceFile;
        this.variables  = variables;
	this.format     = format;
        this.title      = title;
        this.docURL     = docURL;
        this.userDAS    = userDAS;
        this.environment= environment;
        this.workDir    = workDir;
        this.createTime = System.currentTimeMillis();
    }

    /**Returns the name for this dataset
     */
    public String getDODSName() {
        return dodsName;
    }

    /**Returns the source file name for this dataset
     */
    public String getSourceFile() {
	return sourceFile;
    }

    /**Returns the temporary data file for virtual dataset
     */
    public String getDataCache() {
         File dataCacheFile = 
             ((FerretTool)Server.getServer().getTool()).getCacher().getDataset(getDODSName(), getCreateTime());
         if(dataCacheFile==null)
             return null;
         return dataCacheFile.getAbsolutePath();
    }

    /**Returns the enviroment parameters used by this dataset
     */
    public String getEnvironment() {
        return environment;
    }

    /**Returns the work directory used by this dataset
     */
    public String getWorkDir() {
        return workDir;
    }

    /**Returns the output variables
     */
    public String getVariables(){
        return variables;
    }

    /**Returns true if this dataset is a real dataset
     */
    public String getFormat() {
	return format;
    }

    /**Returns the title of this dataset
     */
    public String getTitle() {
        return title;
    }

    /**Returns the document URL for this dataset
     */
    public String getDocURL() {
        return docURL;
    }

    /**Returns the user DAS file path for this dataset
     */
    public String getUserDAS(){
        return userDAS;
    }

    /**Returns the creation time for this dataset
     */
    public long getCreateTime() {
	return createTime;
    }

    /**Returns the Ferret command for the dataset.
     * The returned value can be "use", "set data" or "go"
     * according to the state of the dataset.
     */
    public String getFerretCommand(boolean useCache) {
        String command = null;
        if(useCache&&getDataCache()!=null){
	    command = "use";
	}
        else if(getTargetName(useCache).startsWith("http")){
            command = "use";
        }
        else if(format==null||format.equals("")){
            command ="set data";
        }
        else if(format.equals("jnl")) {
	    command = "go";
	}
        else {
            command = "set data/format="+format;
	}
	
        return command;
    }

    /** Returns the appropriate target name
     * of this dataset according the state of
     * this dataset. 
     */
    public String getTargetName(boolean useCache){
        String target = null;
        if(useCache){
           target = getDataCache();
           if(target==null) {
              target = getSourceFile();
           }
        }
        else{
           target = getSourceFile();
        }
        return target;
    }

    /** Test if an object is equal to this FerretDataInfo object */
    public boolean equals(Object o) {
        if(o==null)
           return false;
        if(!o.getClass().getName().equals(this.getClass().getName()))
           return false;
        FerretDataInfo o1 = (FerretDataInfo)o;
        if(isEqual(o1.getDODSName(), getDODSName()) &&
           isEqual(o1.getSourceFile(), getSourceFile()) &&
           isEqual(o1.getVariables(), getVariables()) &&
           isEqual(o1.getDocURL(), getDocURL()) &&
           isEqual(o1.getUserDAS(), getUserDAS()) &&
           isEqual(o1.getFormat(), getFormat()))
           return true;
        else
           return false;
    }

    /** Helper function for testing if two strings are equal */
    protected boolean isEqual(String a, String b) {
        if(a==null && b==null)
           return true;
        if(a==null || b==null)
           return false;
        return a.equals(b);
    } 

    /** The name for this dataset used by DODS
     */
    protected String dodsName;

    /** The source file name for this dataset
     */
    protected String sourceFile;

    /** The output variables for dataset
     */
    protected String variables;

    /** True if this dataset is a real (not virtual) dataset
     */
    protected String format;

    /** Title for this dataset
     */
    protected String title;

    /**The document URL for this dataset
     */
    protected String docURL;

    /** The user DAS file name */
    protected String userDAS;

    /**The environment parameters for this dataset
     */
    protected String environment;

    /** The Ferret working directory when using this dataset */
    protected String workDir;

    /** The create time for this dataset
     */
    protected long createTime;
}

