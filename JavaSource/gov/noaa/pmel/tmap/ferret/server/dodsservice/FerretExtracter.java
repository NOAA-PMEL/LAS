package gov.noaa.pmel.tmap.ferret.server.dodsservice;

import java.io.*;
import java.util.*;
import java.net.*;
import java.security.*;

import org.iges.anagram.*;
import org.iges.anagram.filter.AnalysisFilter;

import gov.noaa.pmel.tmap.ferret.server.*;

/** Shared interface for modules that extract and cache metadata for
 *  Ferret datasets.<p>
 *  
 *  Modified from org.iges.grads.server.GradsExtracter class.<p>
 *
 *  @author Yonghua Wei
 */
public abstract class FerretExtracter 
    extends AbstractModule {

    public FerretExtracter(FerretTool tool){
        this.tool = tool;
        this.counter = 0;
    }

    public void configure(Setting setting) {
        this.store = server.getStore();
    }

    /** Extracts XML, DDS, DAS and INFO cached files for the specified data handle.
     *
     *  @param data the data handle of the input dataset
     *  @param privilege The privilege assciated with this request
     *  @param useCache if this request uses cached file
     */
    public Map extract(DataHandle data, 
                       Privilege privilege,
                       boolean useCache) 
	throws ModuleException {

        Map returnVal = new HashMap();
	FerretDataInfo ferretInfo = (FerretDataInfo)data.getToolInfo();

        String outputName = null;
        synchronized(this){
            outputName = ferretInfo.getDODSName()
                         + "_"+System.currentTimeMillis()+"_"+counter++;
        }

	try {

 	    File xmlFile = extractXML(data, privilege, outputName, useCache);
            returnVal.put(""+data+".xml", xmlFile);

	    File ddsFile = extractDDS(data, outputName);
            returnVal.put(""+data+".dds", ddsFile);


	    File dasFile = extractDAS(data, outputName);
            returnVal.put(""+data+".das", dasFile);


	    File infoFile = extractWebSummary(data, outputName);
            returnVal.put(""+data+".info", infoFile);


	// Disabled for the time being until THREDDS 1.0 is ready
	//  File threddsFile = extractTHREDDSTag();
        //  returnVal.put(""+data+".thredds", threddsFile);

	    //extractSubsetInfo(data, outputName);

            return returnVal;

	} catch (AnagramException ae) {
	    throw new ModuleException(this, "meta data extraction failed", ae);
	}
    }

    /** Takes the parsed metadata and writes to a XML file */
    protected abstract File extractXML(DataHandle data, 
                                       Privilege privilege,
                                       String outputName, 
                                       boolean useCache)
	throws AnagramException;

    /** Takes the parsed metadata and writes a DDS  */
    protected abstract File extractDDS(DataHandle data, String outputName)
	throws AnagramException;

    /** Takes the parsed metadata and writes a DAS file.  */
    protected abstract File extractDAS(DataHandle data, String outputName)
	throws AnagramException;
    
    /** Takes the parsed metadata and writes a ".info" page  */
    protected abstract File extractWebSummary(DataHandle data, String outputName)
	throws AnagramException;

    /** Caches any precalculated values for subsetting */
    protected abstract File extractSubsetInfo(DataHandle data, String outputName)
	throws AnagramException;

    /** Gets a string for history record */
    protected String getHistoryString(DataHandle data) {
	return new Date(data.getCreateTime()) + " : imported by " 
                        + server.getImplName() + " " + server.getImplVersion();
    }

    /** A reference to the <code>FerretTool</code> module */
    protected FerretTool tool;

    protected Store store;

    protected long counter;
}
