package gov.noaa.pmel.tmap.ferret.server.dodsservice;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import dods.dap.*;
import dods.dap.Server.*;
import dods.dap.parser.*;

import org.iges.util.*;
import org.iges.anagram.*;
import gov.noaa.pmel.tmap.ferret.server.*;
import gov.noaa.pmel.tmap.ferret.server.dodstype.*;

/** Provides meta data service such as dds, das, info or xml<p>
 *
 *  @author Yonghua Wei
 */
public abstract class AbstractGenerator 
    extends AbstractModule {

    public void configure(Setting setting) 
        throws ConfigException {

	store = server.getStore();
        baseTypeFactory = new FerretTypeFactory();
    }

    /** Return the meta data object
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to this request
     * @param privilege privilege associated with this request
     * @param useCache if this request uses cached file
     * @return the meta data object
     */
    protected Object getMeta(DataHandle data,
                             String ce,
                             Privilege privilege,
                             boolean useCache)
	throws ModuleException {

        Object returnVal = null;

        File cacheFile;
        String query = ""+data+"."+getModuleID();

        if(useCache){
	    cacheFile =  tool.getCacher().getMeta(query, data.getCreateTime());

            if(cacheFile==null){
	        synchronized(data) {
                    cacheFile =  tool.getCacher().getMeta(query, data.getCreateTime());
	            if (cacheFile==null) {
	  	        Map extractedFiles = tool.getDODS().getExtracter().extract(data, privilege, useCache);

                        cacheFile = (File)extractedFiles.get(query);
                        InputStream is = getInputStream(cacheFile);
                        returnVal = getMetaFromInput(data, ce, privilege, is);

                        Iterator it = extractedFiles.entrySet().iterator();
                        while(it.hasNext()){
                            Map.Entry current = (Map.Entry)it.next();
                            tool.getCacher().addMeta((String)current.getKey(), (File)current.getValue());
                        }
                        return returnVal;
                    }
	        }
	    }

            InputStream is = getInputStream(cacheFile);
            returnVal = getMetaFromInput(data, ce, privilege, is);
            return returnVal;
        }
        else{
	    Map extractedFiles = tool.getDODS().getExtracter().extract(data, privilege, useCache);
            cacheFile = (File)extractedFiles.get(query);
            InputStream is = getInputStream(cacheFile);
            returnVal = getMetaFromInput(data, ce, privilege, is);
            Iterator it = extractedFiles.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry current = (Map.Entry)it.next();
                ((File)current.getValue()).delete();
            }
            return returnVal;
        }
    }

    /** Return the meta data object by reading it from a input stream
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to this request
     * @param privilege privilege associated with this request
     * @return the meta data object
     */
    protected abstract Object getMetaFromInput(DataHandle data,
                                               String ce,
                                               Privilege privilege,
                                               InputStream is)
          throws ModuleException;

    /** Write the meta data object directly to the output stream
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to this request
     * @param privilege privilege associated with this request
     * @param out the OutputStream the meta data object should be sent to
     * @param useCache if this request uses cached file
     */
    protected void writeMeta(DataHandle data,
                             String ce,
                             Privilege privilege,
                             OutputStream out, 
                             boolean useCache)
	throws ModuleException {

        File cacheFile;
        String query = ""+data+"."+getModuleID();
        if(useCache){
	    cacheFile =  tool.getCacher().getMeta(query, data.getCreateTime());

            if(cacheFile==null){
	        synchronized(data) {
                    cacheFile =  tool.getCacher().getMeta(query, data.getCreateTime());
	            if (cacheFile==null) {
	  	        Map extractedFiles = tool.getDODS().getExtracter().extract(data, privilege, useCache);

                        cacheFile = (File)extractedFiles.get(query);
                        InputStream is = getInputStream(cacheFile);
                        writeMetaFromInput(data, ce, privilege, out, is, useCache);

                        Iterator it = extractedFiles.entrySet().iterator();
                        while(it.hasNext()){
                            Map.Entry current = (Map.Entry)it.next();
                            tool.getCacher().addMeta((String)current.getKey(), (File)current.getValue());
                        }
                        return;
                    }
	        }
	    }

            InputStream is = getInputStream(cacheFile);
            writeMetaFromInput(data, ce, privilege, out, is, useCache);
        }
        else{
	    Map extractedFiles = tool.getDODS().getExtracter().extract(data, privilege, useCache);

            cacheFile = (File)extractedFiles.get(query);
            InputStream is = getInputStream(cacheFile);
            writeMetaFromInput(data, ce, privilege, out, is, useCache);

            Iterator it = extractedFiles.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry current = (Map.Entry)it.next();
                ((File)current.getValue()).delete();
            }
            return;
        }
    }

    /** Write the meta data object directly from an input stream 
     * to the output stream
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to this request
     * @param privilege privilege associated with this request
     * @param out the OutputStream the meta data object should be sent to
     * @param is the InputStream the meta data object should be read from
     * @param useCache if this request uses cached file
     */
    protected abstract void writeMetaFromInput(DataHandle data,
                                               String ce,
                                               Privilege privilege,
                                               OutputStream out,
                                               InputStream is,
                                               boolean useCache)
          throws ModuleException;

    /** Create a input stream from a local file */
    protected InputStream  getInputStream(File cacheFile)
       throws ModuleException { 
	try {
	    return new BufferedInputStream
		(new FileInputStream
		    (cacheFile));
	} catch (Exception e) {
	    throw new ModuleException(this, cacheFile.getAbsolutePath() + 
				      " not found" + e);
	} 
    }

    /** Reference to {@link FerretTool} module
     */
    protected FerretTool tool;

    /** Reference to {@link Store} module
     */
    protected Store store;

    /** An instance of {@link FerretTypeFactory} class
     */
    protected FerretTypeFactory baseTypeFactory;
    
}
