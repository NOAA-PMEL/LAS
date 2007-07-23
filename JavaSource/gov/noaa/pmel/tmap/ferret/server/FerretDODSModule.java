package gov.noaa.pmel.tmap.ferret.server;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import org.w3c.dom.*;

import org.iges.util.*;

import dods.dap.*;
import dods.dap.Server.*;
import dods.dap.parser.*;
import dods.servers.ascii.*;

import org.iges.anagram.*;
import org.iges.anagram.filter.AnalysisFilter;
import gov.noaa.pmel.tmap.ferret.server.dodstype.*;
import gov.noaa.pmel.tmap.ferret.server.dodsservice.*;

/** Provides DODS metadata and subsets for Ferret data objects.
 *  This is the main back end for the FerretTool class. <p>
 *
 *  Modified from org.iges.grads.server.GradsDODSModule<p>
 *
 *  @author Yonghua Wei, Richard Roger
 */
public class FerretDODSModule 
    extends AbstractModule {

    public String getModuleID() {
	return "dods";
    }

    /** Constructs a FerretDODSModule class instance
     * 
     * @param tool reference to {@link FerretTool} module
     */
    public FerretDODSModule(FerretTool tool) {
	this.tool = tool;
    }

    public void init(Server server, Module parent) {
	super.init(server, parent);

        xmlGen = new XMLGenerator(tool);
        xmlGen.init(server, this);

        ddsGen = new DDSGenerator(tool);
        ddsGen.init(server, this);

        dasGen = new DASGenerator(tool);
        dasGen.init(server, this);

        infoGen = new InfoGenerator(tool);
        infoGen.init(server, this);

        binGen = new BINGenerator(tool);
        binGen.init(server, this);

        ascGen = new ASCGenerator(tool);
        ascGen.init(server, this);

        threddsGen = new THREDDSGenerator(tool);
        threddsGen.init(server, this);

        extract = new FerretGridExtracter(tool);
        extract.init(server, this);
    }

    public void configure(Setting setting) 
        throws ConfigException {

        configModule(xmlGen, setting);
        configModule(ddsGen, setting);
        configModule(dasGen, setting);
        configModule(infoGen, setting);
        configModule(binGen, setting);
        configModule(ascGen, setting);
        configModule(threddsGen, setting);
        configModule(extract, setting);
    }

    /** Creates a constrained DDS object for the given dataset (which
     *  is the Java-DODS encapsulation of a subset request).<p>
     *
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to the DDS
     * @param privilege privilege associated with this request
     * @param useCache if this request uses cached file
     */
    public ServerDDS getDDS(DataHandle data, 
			    String ce,
                            Privilege privilege,
                            boolean useCache)
	throws ModuleException {
	return ddsGen.getDDS(data, ce, privilege, useCache);
    }

    /** Creates a DAS object for the given dataset (which
     *  is the Java-DODS encapsulation of a subset request).<p>
     *
     * @param data the data handle of the input dataset
     * @param privilege privilege associated with this request
     * @param useCache if this request uses cached file
     */
    public DAS getDAS(DataHandle data,
                      Privilege privilege,
                      boolean useCache)
	throws ModuleException {
	return dasGen.getDAS(data, privilege, useCache);
    }

    /** Gets a XML Document object for all the metadata of this dataset handle
     *
     * @param data the data handle of the input dataset
     * @param privilege privilege associated with this request
     * @param useCache if this request uses cached file
     */
    public Document getXML (DataHandle data,
                            Privilege privilege,
                            boolean useCache) 
	throws ModuleException 
    {
        return xmlGen.getXML(data, privilege, useCache);
    }

    /** Writes a DAS object directly to an output stream. The DAS
     *  is cached and streamed directly from disk, avoiding the parsing
     *  overhead of getDAS(). 
     *  
     * @param data the data handle of the input dataset
     * @param privilege privilege associated with this request
     * @param out the OutputStream DAS object should be sent to
     * @param useCache if this request uses cached file
     */
    public void writeDAS(DataHandle data, 
                         Privilege privilege,
			 OutputStream out,
                         boolean useCache)
	throws ModuleException {
        dasGen.writeDAS(data, privilege, out, useCache);
    }

    /** Writes a DDS object directly to an output stream. 
     *
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to the DDS
     * @param privilege privilege associated with this request
     * @param out the OutputStream DDS object should be sent to
     * @param useCache if this request uses cached file
     */
    public void writeDDS(DataHandle data, 
			 String ce, 
                         Privilege privilege,
			 OutputStream out,
                         boolean useCache)
	throws ModuleException {
        ddsGen.writeDDS(data, ce, privilege, out, useCache);
    }

    /** Writes a THREDDS tag describing a dataset directly to an output stream. 
     *
     * @param data the data handle of the input dataset
     * @param privilege privilege associated with this request
     * @param out the OutputStream THREDDS tag should be sent to
     * @param useCache if this request uses cached file
     */
    public void writeTHREDDSTag(DataHandle data, 
                                Privilege privilege,
			        OutputStream out,
                                boolean useCache) 
	throws ModuleException {
        threddsGen.writeTHREDDSTag(data, privilege, out, useCache);
    }

    /** Writes an info page directly to an output stream. The info page
     *  is cached and streamed directly from disk.
     *
     * @param data the data handle of the input dataset
     * @param privilege privilege associated with this request
     * @param out the OutputStream INFO page should be sent to
     * @param useCache if this request uses cached file
     */
    public void writeWebInfo(DataHandle data, 
                             Privilege privilege,
			     OutputStream out,
                             boolean useCache) 
	throws ModuleException {
        infoGen.writeWebInfo(data, privilege, out, useCache);
    }

    /** Writes a data subset to a stream in binary format requested using DODS.
     *
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to the DODS request
     * @param privilege The privilege assciated with this DODS request
     * @param out the OutputStream DODS data should be sent to
     * @param useCache if this request uses cached file
     */
    public void writeBinaryData(DataHandle data, 
				String ce, 
				Privilege privilege,
				OutputStream out, 
                                boolean useCache)
	throws ModuleException {
        binGen.writeBinaryData(data, ce, privilege, out, useCache);
    }
 

    /** Writes a data subset to a stream in ASCII format.
     *
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to the ASCII request
     * @param privilege The privilege assciated with this ASCII request
     * @param request The HttpServletRequest object 
     * @param out the OutputStream ASCII data should be sent to
     * @param useCache if this request uses cached file
     */
    public void writeASCIIData(DataHandle data, 
			       String ce, 
			       Privilege privilege,
                               HttpServletRequest request,
			       OutputStream out,
                               boolean useCache)
	throws ModuleException {
        ascGen.writeASCIIData(data, ce, privilege, request, out, useCache);
    }

    /** Returns the size of the dataset specified by the DDS object.
     *
     *  @param dds the input ServerDDS object
     *  @return the size of the dataset specified by the DDS boject
     */
    public int getDataSize(ServerDDS dds, String ce) 
        throws ModuleException {
        int dataSetSize=0;
        try{
	    Enumeration varIt = dds.getVariables();
 	    while (varIt.hasMoreElements()) {
	        BaseType var = (BaseType) varIt.nextElement();
	        if (var instanceof SDArray) {
		    SDArray sdvar = (SDArray) var;
		    if ( sdvar.isProject()||ce==null ) {
		        DArrayDimension dim = sdvar.getDimension(0);
                        dataSetSize += 1+(dim.getStop()-dim.getStart())/dim.getStride();
		    }
	        } else if (var instanceof SDGrid) {
		    SDGrid sdvar = (SDGrid) var;
		    if ( sdvar.isProject()||ce==null ) {

                        int varSize =1;
		        SDArray sdarr = (SDArray) (sdvar.getVar(0));

		        DArrayDimension dim;
		        int i, numDim = sdarr.numDimensions();
		        for (i=0; i < numDim; i+=1) {
			   dim = sdarr.getDimension(i);
			   varSize = varSize * (1+(dim.getStop()-dim.getStart())/dim.getStride());
 		        }

                        dataSetSize += varSize;

       		        for (i=0; i < numDim; i+=1) {
			   dim = sdarr.getDimension(i);
                           dataSetSize += 1+(dim.getStop()-dim.getStart())/dim.getStride();
		        }
		   }
	       }
	   }
        }
        catch(Exception e) {
            throw new ModuleException(this, "DDS is wrong!"+e);
	}
        return dataSetSize;
    }

    public FerretExtracter getExtracter(){
        return extract;
    }

    /** Reference to {@link FerretTool} module
     */
    protected FerretTool tool;

    protected XMLGenerator xmlGen;

    protected DDSGenerator ddsGen;

    protected DASGenerator dasGen;

    protected InfoGenerator infoGen;

    protected BINGenerator binGen;

    protected ASCGenerator ascGen;

    protected THREDDSGenerator threddsGen;

    protected FerretExtracter extract;
}
