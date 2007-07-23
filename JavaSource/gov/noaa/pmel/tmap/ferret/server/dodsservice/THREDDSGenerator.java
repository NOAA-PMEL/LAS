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

/** Provides Info service.<p>
 *
 *  @author Yonghua Wei
 */
public class THREDDSGenerator 
    extends AbstractGenerator {

    public String getModuleID() {
	return "thredds";
    }

    /** Constructs a FerretDODSModule class instance
     * 
     * @param tool reference to {@link FerretTool} module
     */
    public THREDDSGenerator(FerretTool tool) {
	this.tool = tool;
    }


    /** Writes an info page directly to an output stream. The info page
     *  is cached and streamed directly from disk.
     *
     * @param data the data handle of the input dataset
     * @param privilege The privilege assciated with this request
     * @param out the OutputStream THREDDS catalog tag should be sent to
     * @param useCache if this request uses cached file
     */
public void writeTHREDDSTag(DataHandle data, 
                            Privilege privilege,
			    OutputStream out, 
                            boolean useCache) 
	throws ModuleException {
	info("writeTHREDDSTag for "+data.getCompleteName());

        writeMeta(data, null, privilege, out, useCache);
    }

    protected void writeMetaFromInput(DataHandle data,
                                      String ce,
                                      Privilege privilege,
                                      OutputStream out,
                                      InputStream is, 
                                      boolean useCache)
          throws ModuleException{

	if (debug()) log.debug(this, "loading THREDDS tag for " + data);
	
	try {
	    Spooler.spool(is, out);
	} catch (FileNotFoundException fnfe) {
	    throw new ModuleException(this, "thredds file for " + data + 
				      " not found");
	} catch (IOException ioe){
	    throw new ModuleException(this, "io error on THREDDS write", ioe);
	} finally{
            try {
	       is.close();
	    } catch (IOException ioe){}
        }
        
    }

    protected Object getMetaFromInput(DataHandle data,
                                      String ce,
                                      Privilege privilege,
                                      InputStream is)
       throws ModuleException {
         throw new ModuleException(this, "getMetaFromInput is not implemented.");
    }
}
