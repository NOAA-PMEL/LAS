package gov.noaa.pmel.tmap.ferret.server.dodsservice;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import org.w3c.dom.*;

import dods.dap.*;
import dods.dap.Server.*;
import dods.dap.parser.*;

import org.iges.util.*;
import org.iges.anagram.*;

import gov.noaa.pmel.tmap.ferret.server.*;
import gov.noaa.pmel.tmap.ferret.server.dodstype.*;

/** Provides DODS metadata and subsets for Ferret data objects.<p>
 *
 *  @author Yonghua Wei, Richard Roger
 */
public class BINGenerator 
    extends AbstractModule {

    public String getModuleID() {
	return "bin";
    }

    /** Constructs a FerretDODSModule class instance
     * 
     * @param tool reference to {@link FerretTool} module
     */
    public BINGenerator(FerretTool tool) {
	this.tool = tool;
        generating = new HashSet();
        counter = 0;
    }

    public void configure(Setting setting) 
        throws ConfigException {
	this.defaultSubsetSize = 
	    setting.getNumAttribute("subset_size", 0);

	int bufferSize = (int)setting.getNumAttribute("buffer_size", 16384);
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
	info("writeBinaryData for "+data.getCompleteName());

        FerretDataInfo info = (FerretDataInfo) data.getToolInfo();
        File outputFile = null;
	File jnlFile = null;
        Document dom = null;
        ServerDDS dds = null;
        int dataSize = 0;
        String query = ""+data+"?"+ce;

        try {
	    long outputLimit = 
	            privilege.getNumAttribute("output_limit", 20);

	    dom = tool.getDODS().getXML(data, privilege, useCache);
	    dds = tool.getDODS().getDDS(data, ce, privilege, useCache);

            dataSize = tool.getDODS().getDataSize(dds,ce);
            if(outputLimit>0 && dataSize > outputLimit*1000000/4){
                throw new Exception("Output exceeds output limit "+outputLimit+" Mbytes" );
	    }
	} catch (Exception e) {
	    info ("exception: "+e.getMessage());
	    throw new ModuleException(this, "Unable to output dods: "+e.getMessage());
	}

        if(useCache){

            tool.getCacher().cacheDataset(data, privilege, dataSize);

            outputFile = tool.getCacher().getSubset(query, data.getCreateTime());

            if(outputFile==null){
	        synchronized (generating) {
	            while (generating.contains(query)) {
	    	       if (debug()) log.debug(this, "waiting for binary query to complete");
		       try {
		           generating.wait(0);
		       } catch (InterruptedException ie) {}
	            }

	            generating.add(query);
	        }

                try {
                   outputFile = tool.getCacher().getSubset(query, data.getCreateTime());
                   if(outputFile==null){
                       log.info(this, "evaluating query: " + query);
                       String outputName;
                       synchronized(this) {
                           outputName = info.getDODSName()
                                        + "_"+System.currentTimeMillis()+"_"+counter++;
                       }

   	               outputFile = server.getStore().get(this, outputName+".subset");
                       jnlFile = server.getStore().get(this, outputName+".jnl");
                       createJnlFile(dds, ce, dom, data, outputFile, jnlFile, useCache);

                       String envStr = info.getEnvironment();
                       String workDir = info.getWorkDir();

	               Task subset = tool.getTask().task("FDS_go",
                                                          new String[]{
                                                             jnlFile.getAbsolutePath()
                                                          }, 
                                                          envStr,
                                                          workDir);

                       long timeout = privilege.getNumAttribute("time_limit", -1);
                       if(timeout==-1)
	                  subset.run();
                       else
                          subset.run(timeout);

                       sendBinaryData(dds, ce, outputFile, out);
                       tool.getCacher().addSubset(query, outputFile);
                       return;
                   } 
	        } catch (Exception e) {
	           info ("exception: "+e.getMessage());
	           throw new ModuleException(this, "Unable to create binary data: "+e.getMessage());
	        } finally{
                     if(jnlFile!=null)
                         jnlFile.delete();
	             synchronized (generating) {
		         generating.remove(query);
		         generating.notifyAll();
	             }
	        }

            }

            sendBinaryData(dds, ce, outputFile, out);

        }
        else{
            try {
                log.info(this, "evaluating query: " + query);
                String outputName;
                synchronized(this) {
                   outputName = info.getDODSName()
                                + "_"+System.currentTimeMillis()+"_"+counter++;
                }

   	        outputFile = server.getStore().get(this, outputName+".subset");
                jnlFile = server.getStore().get(this, outputName+".jnl");
                createJnlFile(dds, ce, dom, data, outputFile, jnlFile, useCache);

                String envStr = info.getEnvironment();
                String workDir = info.getWorkDir();

	        Task subset = tool.getTask().task("FDS_go",
                                                   new String[]{
                                                        jnlFile.getAbsolutePath()
                                                   }, 
                                                   envStr,
                                                   workDir);

                long timeout = privilege.getNumAttribute("time_limit", -1);
                if(timeout==-1)
	            subset.run();
                else
                    subset.run(timeout);

                sendBinaryData(dds, ce, outputFile, out);
                outputFile.delete();
                return;

	    } catch (Exception e) {
	        info ("exception: "+e.getMessage());
	        throw new ModuleException(this, "Unable to create binary data: "+e.getMessage());
	    } finally{
                if(jnlFile!=null)
                    jnlFile.delete();
	    }
        }
    }

    private void sendBinaryData(ServerDDS dds, 
                                  String ce, 
                                  File outputFile,
                                  OutputStream out)
        throws ModuleException {
        try{
            PrintStream ddsOut = new PrintStream(out);
            if(ce!=null)
	       dds.printConstrained(ddsOut);
            else
               dds.print(ddsOut);

	    ddsOut.println("Data:");
	    ddsOut.flush();
            Spooler.spool (new FileInputStream(outputFile) , out);
	} catch (Exception e) {
	    info ("exception: "+e.getMessage());
	    throw new ModuleException(this, "Unable to send binary data: "+e.getMessage());
        }
    }
 
    private void createJnlFile(ServerDDS dds,
                                 String ce,
                                 Document dom, 
                                 DataHandle data,
                                 File outputFile,
                                 File jnlFile,
                                 boolean useCache)
        throws Exception {
	PrintWriter jnlWriter = new PrintWriter(new FileOutputStream(jnlFile));
        FerretDataInfo info = (FerretDataInfo)data.getToolInfo();

        jnlWriter.println(info.getFerretCommand(useCache) + " \"" + info.getTargetName(useCache) + "\"");

	Enumeration varIt = dds.getVariables();
	while (varIt.hasMoreElements()) {
	   BaseType var = (BaseType) varIt.nextElement();
	   if (var instanceof SDArray) {
	       SDArray sdvar = (SDArray) var;
	       if ( sdvar.isProject()||ce==null ) {
	      	    DArrayDimension dim = sdvar.getDimension(0);
		    doBinaryAxis (dim, data, outputFile, jnlWriter);
	       }
	   } else if (var instanceof SDGrid) {
 	       SDGrid sdvar = (SDGrid) var;
	       if ( sdvar.isProject()||ce==null ) {
                   SDArray sdarr = (SDArray) (sdvar.getVar(0));
                   doBinaryGrid (sdarr, dom, data, outputFile, jnlWriter, useCache);
                   int numDim = sdarr.numDimensions(); 
	           for (int i=0; i < numDim; i+=1) {
	   	       DArrayDimension dim = sdarr.getDimension(i);
		       doBinaryAxis (dim, data, outputFile, jnlWriter);
		   }
	       }
	   }
       }

       jnlWriter.flush();
       jnlWriter.close();
    }

    private void doBinaryAxis (DArrayDimension dim, 
                                 DataHandle data, 
                                 File outputFile, 
                                 PrintWriter jnlWriter) 
	throws ModuleException, AnagramException
    {
	String range = ""  + (dim.getStart() + 1) + // Ferret starts @ 1
	               ":" + (dim.getStop()  + 1);  // DODS   starts @ 0
        if(dim.getStride()!=1)
             range = range + ":" + dim.getStride();

        String dimName = FerretXMLReader.decodeName(dim.getName());

        if(dimName.startsWith("(")&&dimName.endsWith(")")){
            jnlWriter.println("show var/xml");
            jnlWriter.println("show axis/i=" + range + "/dods/append/file=\""
                               + outputFile.getAbsolutePath() + "\" " + dimName);
        } 
        else {        
            jnlWriter.println("show axis/i=" + range + "/dods/append/file=\""
                               + outputFile.getAbsolutePath() + "\" " + dimName);
        }
    }

    // Outputs grid data to temporary DODS file.
    private void doBinaryGrid (SDArray sdarr, 
                                 Document dom, 
                                 DataHandle data, 
			         File outputFile, 
                                 PrintWriter jnlWriter,
                                 boolean useCache) 
	throws ModuleException, AnagramException
    {
	 FerretDataInfo info = (FerretDataInfo) data.getToolInfo();
         String ranges = getRanges(sdarr, dom, info, useCache);
         jnlWriter.println("list/form=dods/append/file=\""
                           + outputFile.getAbsolutePath() + "\" "
                           + sdarr.getName() +"[" + ranges + "]");
    }

    // Generates the Ferret range specification for an array.
    private String getRanges(SDArray sdarr, 
                               Document dom, 
                               FerretDataInfo info, 
                               boolean useCache)
         throws ModuleException {
         try {
	     String ranges = "";
	     int i, numDim = sdarr.numDimensions();
             int dsetIndex;
             if(!info.getFerretCommand(useCache).equals("go"))
                dsetIndex = 1;
             else
                dsetIndex = FerretXMLReader.whichDset(dom, sdarr.getName());
             if(dsetIndex>0) {
                 ranges = "d="+dsetIndex;
             }
	     for (i=0; i < numDim; i+=1) {
	         DArrayDimension dim = sdarr.getDimension(i);
                 if(i!=0 || dsetIndex>0)
                     ranges = ranges + ",";
	         ranges = ranges +
	              FerretXMLReader.whichDim(dom, sdarr.getName(), dim.getName()) +
		      "=" + (dim.getStart()+1) + 
		      ":" + (dim.getStop()+1);
                 if(dim.getStride()!=1) {
                     ranges = ranges + 
		             ":" + dim.getStride();
                 }
	     }
             return ranges;
         }
         catch (Exception e) {
             throw new ModuleException(this, "failed to get range:" + e);
         }
    }

    /** Default subset size limit
     */
    protected long defaultSubsetSize;
    /** Reference to {@link FerretTool} module
     */
    protected FerretTool tool;

    private HashSet generating;

    private long counter;
}
