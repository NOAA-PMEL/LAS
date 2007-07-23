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


/** Provides DDS service <p>
 *
 *  @author Yonghua Wei
 */
public class DDSGenerator 
    extends AbstractGenerator {

    public String getModuleID() {
	return "dds";
    }

    /** Constructs a FerretDODSModule class instance
     * 
     * @param tool reference to {@link FerretTool} module
     */
    public DDSGenerator(FerretTool tool) {
	this.tool = tool;
    }


    /** Creates a constrained DDS object for the given dataset (which
     *  is the Java-DODS encapsulation of a subset request).<p>
     *
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to the DDS
     * @param privilege The privilege assciated with this request
     * @param useCache if this request uses cached file
     */
    public ServerDDS getDDS(DataHandle data, 
			    String ce,
                            Privilege privilege,
                            boolean useCache)
	throws ModuleException {
	info("getDDS for "+data.getCompleteName());
	
        return (ServerDDS)getMeta(data, ce, privilege, useCache);
    }

    protected Object getMetaFromInput(DataHandle data,
                                      String ce,
                                      Privilege privilege,
                                      InputStream is)
       throws ModuleException {
	if (debug()) log.debug(this, "creating dds object for " + data);
	ServerDDS dds = new ServerDDS(baseTypeFactory);
	try {
	    dds.parse(is);
        } catch (Exception e){
            throw new ModuleException(this, "dds load failed", e);
	} finally {
	    try {
		is.close();
	    } catch (IOException ioe) {}
	}	    

	if (ce == null) {
	    return dds;
	} 

	if (debug()) log.debug(this, "parsing constraint " + ce + 
			       " for " + data);
	try {
	    CEEvaluator evaluator = new CEEvaluator(dds);
	    evaluator.parseConstraint(ce);
	} 
	catch (Exception e) {
	    debug ("Exception: "+e.getMessage());
	    throw new ModuleException (this, 
			"unable to parse constraint expression");
	} 

	if (debug()) log.debug(this, "dds created successfully for " + data);
	    
	return dds;
    }

    /** Writes a DDS object directly to an output stream. If there is 
     *  no constraint to apply, the DDS can simply be streamed directly
     *  from disk, avoiding the parsing overhead of getDDS().
     *
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to the DDS
     * @param privilege The privilege assciated with this request
     * @param out the OutputStream DAS object should be sent to
     * @param useCache if this request uses cached file
     */
    public void writeDDS(DataHandle data, 
			 String ce, 
                         Privilege privilege,
			 OutputStream out,
                         boolean useCache)
	throws ModuleException {
	info("writeDDS for "+data.getCompleteName());
        writeMeta(data, ce, privilege, out, useCache);

    }

    protected void writeMetaFromInput(DataHandle data,
                                      String ce,
                                      Privilege privilege,
                                      OutputStream out,
                                      InputStream is, 
                                      boolean useCache)
          throws ModuleException{
	if (ce == null) {
	    if (debug()) log.debug(this, "writing dds for " + data + 
				   " to stream");
	    try {
		Spooler.spool(is, out);
	    } catch (IOException ioe){
		throw new ModuleException(this, "io error on dds write", ioe);
	    } finally {
		try {
		    is.close();
		} catch (IOException ioe) {}
	    }
	} else {
	    try {
	       is.close();
	    } catch (IOException ioe) {}

	    ServerDDS dds = getDDS(data, ce, privilege, useCache);
	    if (debug()) log.debug(this, "writing constrained dds for " + 
				   data + " to stream");
	    dds.printConstrained(out);
	} 
    }
}
