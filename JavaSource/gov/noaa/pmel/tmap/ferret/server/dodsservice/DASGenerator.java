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

/** Provides DAS service<p>
 *
 *  @author Yonghua Wei
 */
public class DASGenerator 
    extends AbstractGenerator {

    public String getModuleID() {
	return "das";
    }

    /** Constructs a FerretDODSModule class instance
     * 
     * @param tool reference to {@link FerretTool} module
     */
    public DASGenerator(FerretTool tool) {
	this.tool = tool;
    }

    /** Creates a DAS object for the given dataset (which
     *  is the Java-DODS encapsulation of a subset request).<p>
     *
     * @param data the data handle of the input dataset
     * @param privilege The privilege assciated with this request
     * @param useCache if this request uses cached file
     */
    public DAS getDAS(DataHandle data,
                      Privilege privilege,
                      boolean useCache)
	throws ModuleException {

	info("getDDS for "+data.getCompleteName());

        String userDAS = ((FerretDataInfo)data.getToolInfo()).getUserDAS();
        try {
            if(userDAS != null){
               File dasFile = new File(userDAS);
               DAS das = new DAS();
               InputStream is = getInputStream(dasFile);
               das.parse(is);
               is.close();
               return das;
            }
        }
        catch(Exception e){}

        return (DAS)getMeta(data, null, privilege, useCache);
    }

    protected Object getMetaFromInput(DataHandle data,
                                      String ce, 
                                      Privilege privilege,
                                      InputStream is)
       throws ModuleException {

	if (debug()) log.debug(this, "creating das object for " + data);

	DAS das = new DAS();
	try {
	    das.parse(is);
        } catch (Exception e){
            throw new ModuleException(this, "das load failed", e);
	} finally {
	    try {
		is.close();
	    } catch (IOException ioe) {}
	}

	return das;
    }

    /** Writes a DAS object directly to an output stream. The DAS
     *  is cached and streamed directly from disk, avoiding the parsing
     *  overhead of getDAS(). 
     *  
     * @param data the data handle of the input dataset
     * @param out the OutputStream DAS object should be sent to
     */
    public void writeDAS(DataHandle data, 
                         Privilege privilege,
			 OutputStream out, 
                         boolean useCache)
	throws ModuleException {

	info("writeDAS for "+data.getCompleteName());

        String userDAS = ((FerretDataInfo)data.getToolInfo()).getUserDAS();
        try {
            if(userDAS != null){
               File dasFile = new File(userDAS);
               DAS das = new DAS();
               InputStream is = getInputStream(dasFile);
               das.parse(is);
               Spooler.spool(is, out);
               is.close();
               return;
            }
        }
        catch(Exception e){}

        writeMeta(data, null, privilege, out, useCache);
    }

    protected void writeMetaFromInput(DataHandle data,
                                      String ce,
                                      Privilege privilege,
                                      OutputStream out,
                                      InputStream is,
                                      boolean useCache)
          throws ModuleException{
	if (debug()) log.debug(this, "writing das for " + data + " to stream");
	try {
	    Spooler.spool(is, out);
	} catch (IOException ioe){
	    throw new ModuleException(this, "io error on das write", ioe);
	} finally {
	    try {
		is.close();
	    } catch (IOException ioe) {}
	}
    }
}
