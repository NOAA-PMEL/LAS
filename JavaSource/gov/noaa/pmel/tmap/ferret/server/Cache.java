package gov.noaa.pmel.tmap.ferret.server;

import java.lang.*;
import java.io.*;
import java.util.*;


/** This class manages cached file in a queue. The last accessed file is moved
 * to the head of the queue. If the queue size reached its limit, the least recently
 * accessed file will be removed from the queue and deleted. This class synchronizes
 * access. The class has supporting function for serializing and deserializing cache
 * information to a local file.<p> 
 * @author Yonghua Wei
 */

public class Cache 
    extends LinkedHashMap implements Serializable{
    public Cache(int cacheSize){
        super(cacheSize, (float)0.75, true);
        this.cacheSize = cacheSize;
        this.enabled = true;
    }

    /** Test if this cache object is enabled */
    public synchronized boolean isEnabled(){
        return enabled;
    }

    /** Enable or disable this cache object according to 
     *  the input parameter <p>
     * @param enabled specified if this cache object should be enabled or disabled.
     */
    public synchronized void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    /** Add a file to the cache if the cache object is enabled.<p>
     *  @param query a string that is the key to the file. The file
     *         can be retrieved using this string.
     *  @param file the File object that need to be stored in cache.
     */
    public synchronized void addFile(String query, File file){
        if(enabled){
           File oldFile = (File)get(query);
           if(oldFile!=null
              &&!oldFile.getAbsolutePath().equals(file.getAbsolutePath()))
              oldFile.delete();
           put(query, file);
        }
        else{
           file.delete();
        }
    }

    /**Get the file identified by a string if the cache object is enabled.<p>
     *
     * @param query a string that is the key to the file. The file
     *         can be retrieved using this string.
     * @param time only a file newer than this time should be returned.
     *         If this parameter is equal to Cache.DELETE_CACHE, cache
     *         file will be deleted and return null. If this parameter is equal to
     *         Cache.GET_CACHE, cache file will always be returned if it
     *         exists.
     * @return a File object that points to the cache file in local 
     *         file system if cache file exists, otherwise return null.
     */ 
    public synchronized File getFile(String query, long time){
        File file = null;

        if(enabled){
            file = (File)get(query);
            if(file!=null){
                if(time > 0 && file.exists() 
                   && file.lastModified() >= time){
                   return file;
                }
                else{
                   file.delete();
                   remove(query);
                }
            }
        }

        return null;
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
        if(size() > cacheSize){
           File eldestFile = (File)eldest.getValue();
           if(eldestFile!=null)
                eldestFile.delete();
           return true;
        }
        else{
           return false;
        }
    }

    /** Delete all the files in this cache object */
    public synchronized void clean(){
        Iterator it = values().iterator();
        while(it.hasNext()){
           File current = (File)it.next();
           if(current!=null)
              current.delete();
        }
        clear();
    }

    /** Serialize this cache object to a local file<p>
     * @param cacheFile the local file to store this cache object
     */
    public void saveCacheToStore(File cacheFile) 
        throws Exception {
	try {
	    ObjectOutputStream entryStream =
		new ObjectOutputStream
		    (new FileOutputStream
			(cacheFile));
	    entryStream.writeObject(this);
	    entryStream.close();
	} catch (IOException ioe) {
	    throw new Exception("saving to persistence mechanism failed; " +
		                 "cache will not persist after reboot; " +
                                 "message: " + ioe);
	}
    }

    /** Load a local file and populate this cache object<p>
     * @param cacheFile the local file to load
     */
    public void loadCacheFromStore(File cacheFile) 
        throws Exception {

	if (!cacheFile.exists()) {
	    return;
	}
	    
	try {
	    ObjectInputStream entryStream = 
		new ObjectInputStream
		    (new FileInputStream
			(cacheFile));
	    Map restoredCache = (Map)entryStream.readObject();
	    Iterator it = restoredCache.entrySet().iterator();
	    while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String query = (String)entry.getKey();
                File file = (File)entry.getValue();
                if(file!=null&&file.exists()){
		    addFile(query, file);
                }
	    }	    

	    entryStream.close();	    
	} catch (Exception e) {
	    throw new Exception("cache could not be reloaded from " + 
		                 cacheFile.getAbsolutePath() + 
                                 "; message: " + e);
	} 
    }

    public static long DELETE_CACHE = 0;

    public static long GET_CACHE = 1;

    /** the maxmimum size of the this cache object */
    protected int cacheSize;

    /** true if the cache object is enabled, false if not */
    protected boolean enabled;
}
