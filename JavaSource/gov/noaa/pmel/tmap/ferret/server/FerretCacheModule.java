package gov.noaa.pmel.tmap.ferret.server;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import dods.dap.Server.*;

import org.iges.util.*;
import org.iges.anagram.*;
import org.iges.anagram.filter.AnalysisFilter;

/** This module manages data caching which includes dataset caching, binary subset caching
 * and meta data caching.<p> 
 * @author Yonghua Wei
 */

public class FerretCacheModule 
    extends AbstractModule {

    public String getModuleID() {
	return "cacher";
    }

    public FerretCacheModule(FerretTool tool) {
	this.tool = tool;
        generating = new HashSet();
        counter = 0;
    }

    public void configure(Setting setting) {
        this.datasetCachingEnabled = setting.getAttribute("dataset", "false").equals("true");
        this.datasetSize = setting.getNumAttribute("dataset_size", 40);
        this.datasetQueueSize = (int)setting.getNumAttribute("dataset_queue_size", 10000);
        this.datasetCache = new Cache(datasetQueueSize);

        File cacheFile = server.getStore().get(this, DATASET_CACHE);
        if(cacheFile.exists()){
           try {
               datasetCache.loadCacheFromStore(cacheFile);
               log.info(this, "reloaded " + datasetCache.size() + 
         	              " dataset cache from " + 
		              cacheFile.getAbsolutePath());
           }
           catch(Exception e){
               log.error(this, e.getMessage());
           }
        }
        datasetCache.setEnabled(datasetCachingEnabled);

        this.subsetCachingEnabled = setting.getAttribute("subset", "true").equals("true");
        this.subsetQueueSize = (int)setting.getNumAttribute("subset_queue_size", 10000);
        this.subsetCache = new Cache(subsetQueueSize);

        cacheFile = server.getStore().get(this, SUBSET_CACHE);
        if(cacheFile.exists()){
           try {
               subsetCache.loadCacheFromStore(cacheFile);
               log.info(this, "reloaded " + subsetCache.size() + 
         	              "subset cache from " + 
		              cacheFile.getAbsolutePath());
           }
           catch(Exception e){
               log.error(this, e.getMessage());
           }
        }
        subsetCache.setEnabled(subsetCachingEnabled);

        this.metaCachingEnabled = setting.getAttribute("meta", "true").equals("true");
        this.metaQueueSize = (int)setting.getNumAttribute("meta_queue_size", 10000);
        this.metaCache = new Cache(metaQueueSize);

        cacheFile = server.getStore().get(this, META_CACHE);
        if(cacheFile.exists()){
           try {
               metaCache.loadCacheFromStore(cacheFile);
               log.info(this, "reloaded " + metaCache.size() + 
         	              "meta cache from " + 
		              cacheFile.getAbsolutePath());
           }
           catch(Exception e){
               log.error(this, e.getMessage());
           }
        }
        metaCache.setEnabled(metaCachingEnabled);

    }

    /**return a dataset specified by the DODS path from cache<p>
     *
     * @param query the DODS path that identifies the dataset.
     * @param time only dataset newer than this time should be returned.
     *         If this parameter is equal to Cache.DELETE_CACHE, cache
     *         file will be deleted and return null. If this parameter is equal to
     *         Cache.GET_CACHE, cache file will always be returned if it
     *         exists.
     * @return a File object that points to the cached dataset in local 
     *         file system if cache for the dataset exists, otherwise return null.
     */ 
     
    public File getDataset(String query, long time){
        return datasetCache.getFile(query, time);
    }

    /**add a dataset specified by the DODS path to cache<p>
     *
     * @param query the DODS path that identifies the dataset.
     * @param file a File object that points to the cached dataset in local 
     *         file system.
     */ 
    public void addDataset(String query, File file){
        datasetCache.addFile(query, file);
    }

    /**return a binary subset specified by the DODS query from cache<p>
     *
     * @param query the DODS query that identifies the subset.
     * @param time only dataset newer than this time should be returned.
     *         If this parameter is equal to Cache.DELETE_CACHE, cache
     *         file will be deleted and return null. If this parameter is equal to
     *         Cache.GET_CACHE, cache file will always be returned if it
     *         exists.
     * @return a File object that points to the cached subset in local 
     *         file system if cache for the subset exists, otherwise return null.
     */ 
    public File getSubset(String query, long time){
        return subsetCache.getFile(query, time);
    }

    /**add a subset specified by the DODS path to cache<p>
     *
     * @param query the DODS path that identifies the subset.
     * @param file a File object that points to the cached subset in local 
     *         file system.
     */ 
    public void addSubset(String query, File file){
        subsetCache.addFile(query, file);
    }

    /**return a meta data specified by the DODS query from cache<p>
     *
     * @param query the DODS query that identifies the metadata.
     * @param time only meta data newer than this time should be returned.
     *         If this parameter is equal to Cache.DELETE_CACHE, cache
     *         file will be deleted and return null. If this parameter is equal to
     *         Cache.GET_CACHE, cache file will always be returned if it
     *         exists.
     * @return a File object that points to the cached meta data in local 
     *         file system if cache for the meta data exists, otherwise return null.
     */ 
    public File getMeta(String query, long time){
        return metaCache.getFile(query, time);
    }

    /**add a meta data specified by the DODS path to cache<p>
     *
     * @param query the DODS path that identifies the meta data.
     * @param file a File object that points to the cached meta data in local 
     *         file system.
     */ 
    public void addMeta(String query, File file){
        metaCache.addFile(query, file);
    }

    /** This function is called when server is shutdown. This function will
     *  try to save cache information to the disk to be used when server 
     *  starts up next time.<p>
     */ 
    public void destroy(){
        File cacheFile = server.getStore().get(this, DATASET_CACHE);
        try {
            datasetCache.saveCacheToStore(cacheFile);
            log.info(this, "Dataset cache (" + datasetCache.size() 
                     + " entries) has been saved to "+cacheFile.getAbsolutePath());
        }
        catch(Exception e){
            log.error(this, e.getMessage());
        }

        cacheFile = server.getStore().get(this, SUBSET_CACHE);
        try {
            subsetCache.saveCacheToStore(cacheFile);
            log.info(this, "Subset cache (" + subsetCache.size() 
                     + " entries) has been saved to "+cacheFile.getAbsolutePath());
        }
        catch(Exception e){
            log.error(this, e.getMessage());
        }

        cacheFile = server.getStore().get(this, META_CACHE);
        try {
            metaCache.saveCacheToStore(cacheFile);
            log.info(this, "Meta Cache (" + metaCache.size() 
                     + " entries) has been saved to "+cacheFile.getAbsolutePath());
        }
        catch(Exception e){
            log.error(this, e.getMessage());
        }
    }

   /** Creates cached data from a <code>DataHandle</code> if the cache
    *  is not already there
    *  @param data the DataHandle that represents the dataset
    *  @throws ModuleException if anything goes wrong in the caching process
    */
    public void cacheDataset(DataHandle data,
                             Privilege privilege,
                             int requestSize) 
        throws ModuleException {

        if(!datasetCachingEnabled||requestSize<1000)
           return;

        FerretDataInfo ferretInfo = (FerretDataInfo)data.getToolInfo();

        if(needCache(ferretInfo)){

            ServerDDS dds = tool.getDODS().getDDS(data, null, privilege, true);
            int dataSize = tool.getDODS().getDataSize(dds, null);
            if(requestSize*10<dataSize||dataSize>datasetSize*1000000/4)
                return;

            String dodsName = ferretInfo.getDODSName();

	    synchronized (generating) {
	        while (generating.contains(dodsName)) {
	  	   if (debug()) log.debug(this, "waiting for data caching to complete");
		   try {
		       generating.wait(0);
		   } catch (InterruptedException ie) {}
	        }

	        generating.add(dodsName);
	    }

            try {
               if(needCache(ferretInfo)) {
                   String shortName = null;

                   synchronized(this){
                       shortName = dodsName + "_" + System.currentTimeMillis() + "_" + counter++;
                   }

                   File dataFile = server.getStore().get(this, shortName+".cdf", data.getCreateTime());

                   File jnlFile = null;
 	           try {
      	              info("doCache for "+data.getCompleteName());

                      String initCmmd = ferretInfo.getFerretCommand(true);
                      jnlFile = createJournalFile(data, privilege, shortName);
                      String envStr = ferretInfo.getEnvironment();
                      String workDir = ferretInfo.getWorkDir();
   	              Task task = tool.getTask().task("FDS_2go", 
		             		               new String[] {
                                                            "\""+initCmmd+"\"",
                                                            ferretInfo.getTargetName(true),
						            jnlFile.getAbsolutePath()},
                                                       envStr,
                                                       workDir);

                      long timeout = privilege.getNumAttribute("time_limit", -1);
                      if(timeout==-1)
	                 task.run();
                      else
                         task.run(timeout);
		
	              if(!dataFile.exists()) {
		         throw new ModuleException
		                  (this, "caching produced no output");
	              }

                      addDataset(dodsName, dataFile);

                   } catch (Exception e) {
	              dataFile.delete();
                      throw new ModuleException(this, e.getMessage());
	           }
                   finally {
                      if(jnlFile!=null)
                         jnlFile.delete();
                   }
               }
            }
            catch(Exception e){
                log.error(this, "Caching dataset failed for "+data.getCompleteName()+":"+e);
            }
            finally {
	        synchronized (generating) {
		   generating.remove(dodsName);
		   generating.notifyAll();
	        }
            }
        }
    }

    /** Creates a journal script for caching process
     * @param data the <code>DataHandle</code> object that represents the
     *        current dataset
     * @param shortName the file name for the cache
     * @return a <code>File</code> object of the journal script.
     */
    protected File createJournalFile(DataHandle data, 
                                     Privilege privilege,
                                     String shortName) 
        throws Exception {

        Document xml = tool.getXML(data, privilege, true);

        FerretDataInfo ferretInfo = (FerretDataInfo)data.getToolInfo();
        String variables = ferretInfo.getVariables();
        boolean varsSpecified = false;
        StringTokenizer vIt = null;
        if(variables!=null){
	    try{
                vIt = new StringTokenizer(variables, ",+ ", false);
                if(vIt.hasMoreTokens()){
                   varsSpecified = true;
                }
	    }
            catch(NullPointerException npe){}
	}

        File dataFile = server.getStore().get(this, shortName+".cdf");
        File jnlFile = server.getStore().get(this, shortName+".jnl");

	FileWriter jnlWriter;
	try {
	    jnlWriter = new FileWriter(jnlFile.getAbsolutePath());
	} catch (IOException ioe) {
	    throw new ModuleException(this,"error writing jnl for " + 
		 	          data.getCompleteName());
	}
	PrintWriter jnlOut = new PrintWriter(jnlWriter);

        jnlOut.print("list/format=CDF/file=\""+dataFile.getAbsolutePath()+"\" ");

        boolean isFirstVar =true;
        if(varsSpecified) {
	    while(vIt.hasMoreTokens()){
 
              if(!isFirstVar){
	         jnlOut.print(",");
	      }
                                 
              String var = vIt.nextToken();
              jnlOut.print(var);

              if(FerretXMLReader.hasDerivedAxes(xml, var)){
	          throw new ModuleException(this,"there are derived axes, no caching for " + 
		 	                    data.getCompleteName());
              }

              int dsetIndex = FerretXMLReader.whichDset(xml, var);
              if(dsetIndex>0)
	         jnlOut.print("[d="+dsetIndex+"]");
                       
              isFirstVar = false;
	   }
        }
        else {
           List varSet = FerretXMLReader.getVariables(xml);
           for(int i=0;i<varSet.size();i++){
              if(!isFirstVar){
	         jnlOut.print(",");
	      }
              String var = (String)varSet.get(i);
              jnlOut.print(var);

              if(FerretXMLReader.hasDerivedAxes(xml, var)){
	          throw new ModuleException(this,"there are derived axes, no caching for " + 
		 	                    data.getCompleteName());
              }
              int dsetIndex = FerretXMLReader.whichDset(xml, var);
              if(dsetIndex>0)
	         jnlOut.print("[d="+dsetIndex+"]");

              isFirstVar = false;
           }
        }

        jnlOut.print("\n");
        jnlOut.flush();
        jnlOut.close();
        return jnlFile;
    }

    /** A helper function to judge if caching is needed
     *  for a dataset
     * @param ferretInfo the FerretDataInfo object that describes a dataset.
     */
    protected boolean needCache(FerretDataInfo ferretInfo){
        String initCmmd = ferretInfo.getFerretCommand(true);
        String targetName = ferretInfo.getTargetName(true);
        return (initCmmd.equals("go")
                ||targetName.startsWith("http:")
                ||targetName.endsWith(".des"));
    }

    /** The {@link FerretTool} module */
    protected FerretTool tool;

    /** true if dataset caching enabled, false if not.  */
    protected boolean datasetCachingEnabled;

    /** the maximum number of slots available for dataset cache */
    protected int datasetQueueSize;

    /** the dataset Cache object  */ 
    protected Cache datasetCache;

    /** the file name to keep dataset cache information */
    protected final static String DATASET_CACHE = "dataset_cache.obj";

    /** the maxmum size of dataset that can be cached */
    protected long datasetSize;

    /** true if binary subset caching enabled, false if not.  */
    protected boolean subsetCachingEnabled;

    /** the maximum number of slots available for subset cache */
    protected int subsetQueueSize;

    /** the subset Cache object  */ 
    protected Cache subsetCache;

    /** the file name to keep subset cache information */
    protected final static String SUBSET_CACHE = "subset_cache.obj";

    /** true if meta data caching enabled, false if not.  */
    protected boolean metaCachingEnabled;

    /** the maximum number of slots available for meta data cache */
    protected int metaQueueSize;

    /** the meta data Cache object  */ 
    protected Cache metaCache;

    /** the file name to keep meta data cache information */
    protected final static String META_CACHE = "meta_cache.obj";

    private HashSet generating;
    private long counter;
}
