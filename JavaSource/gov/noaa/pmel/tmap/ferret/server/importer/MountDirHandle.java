package gov.noaa.pmel.tmap.ferret.server.importer;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.iges.anagram.*;
import org.iges.util.*;
import gov.noaa.pmel.tmap.ferret.server.*;

/** Represents a sub-directory of the server catalog. 
 * The content inside this directory is synched with the content 
 * in its corresponding file directory.
 */
public class MountDirHandle
    extends DirHandle {

    /** Constructor of the <code>MountDirHandle</code>
     *
     * @param completeName the name of this directory in DODS catalog
     * @param dir the file directory handle in the file system
     * @param prefix the prefix for file names
     * @param suffix the suffix for file names
     * @param docURL the documentation URL for this directory
     * @param format the format of data inside this directory, "use" or "jnl"
     * @param recurse if the subdirectory should be considered.
     */
    public MountDirHandle(String completeName,
                          File dir,
                          String prefix,
                          String suffix,
                          String docURL, 
                          String userDAS,
                          String format,
                          String environment, 
                          boolean recurse)
	throws AnagramException {
	super(completeName);
        this.dir = dir;
        this.prefix = prefix;
        this.suffix = suffix;
        this.docURL = docURL;
        this.userDAS = userDAS;
        this.format = format;
        this.environment = environment;
        this.recurse = recurse;
    }

    /** Returns true if this directory contains a handle that
     *  matches the name given
     *  @param completeName the name of the given handle
     */     
    public boolean contains(String completeName) {

        Handle handle = getHandle(completeName);

        if(handle == null)
           return false;
        else
           return true;
    }

    
    /** Gets a <code>Handle</code> object using its name
     *  @param completeName the complete path name for the specified object
     */
    public Handle get(String completeName) {

        Handle newHandle = getHandle(completeName);

        getSynch().lockExclusive();

        Handle oldHandle = (Handle)entries.get(completeName);

        if(newHandle==null) {
           if(oldHandle != null) {
              entries.remove(completeName);
           }
           getSynch().releaseExclusive();
           return null;
        }

        if(oldHandle==null) {
           entries.put(completeName, newHandle);
           getSynch().releaseExclusive();
           return newHandle;
        }
        else if(!newHandle.equals(oldHandle)) {
           entries.put(completeName, newHandle);

           getSynch().releaseExclusive();
           return newHandle;
        }
        
        getSynch().releaseExclusive();
        return oldHandle;
    }

    /** Tests if the directory is empty. It always returns true
     * so the mounted directory is not deleted even if it is really empty.
     */
    public boolean isEmpty() {
        return false;
    }

    /** Returns a Map containing all entries in this directory.
     *  The keys are the names of the entries, and the values are the
     *  Handle objects associated with those names.
     *  @param recurse If true, the Map will also contain all entries in all
     *  sub-directories. In this case, handles for the subdirectories 
     *  themselves will be omitted.
     */
    public Map getEntries(boolean recurse) {
	if (recurse&&this.recurse) {
	    SortedMap recursedEntries = new TreeMap();
	    Iterator it = getEntries(false).values().iterator();
	    while (it.hasNext()) {
		Handle next = (Handle)it.next();
                recursedEntries.put(next.getCompleteName(), next);
		if (next instanceof DirHandle) {
		    recursedEntries.putAll(((DirHandle)next).getEntries(true));
		} 
	    }
	    return recursedEntries;
	} else {
            getSynch().lockExclusive();

            SortedMap localEntries= new TreeMap();
            File[] contents = dir.listFiles();
            if(contents!=null){
               for (int i = 0; i < contents.length; i++) {
                  Handle handle = createHandle(contents[i]);
                  if(handle!=null)
                      localEntries.put(handle.getCompleteName(), handle);
               }
            }

            Iterator it = entries.values().iterator();
            while (it.hasNext()) {
                Handle next = (Handle)it.next();
                if(localEntries.get(next.getCompleteName())==null){

                    it.remove();
                }
            }

            Iterator it2 = localEntries.values().iterator();
            while(it2.hasNext()) {
                Handle next = (Handle)it2.next();
                Handle oldHandle = (Handle)entries.get(next.getCompleteName());
                if(oldHandle ==null) {
                    entries.put(next.getCompleteName(), next);
                }
                else if(!oldHandle.equals(next)) {
                    entries.put(next.getCompleteName(), next);
                }
            }
            localEntries= new TreeMap();
            localEntries.putAll(entries);

            getSynch().releaseExclusive();

	    return localEntries;
	}
    }

    public String getAbsolutePath(){
        return dir.getAbsolutePath(); 
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public String getDocURL() {
        return this.docURL;
    }
 
    public String getUserDAS() {
        return this.userDAS;
    }

    public String getFormat() {
        return this.format;
    }

    public boolean isRecurse() {
        return this.recurse;
    }

    public boolean equals(Object o) {
        if(o==null)
           return false;
        if(!o.getClass().getName().equals(this.getClass().getName()))
           return false;
        MountDirHandle o1 = (MountDirHandle)o;
        if(isEqual(o1.getCompleteName(), getCompleteName()) &&
           isEqual(o1.getAbsolutePath(), getAbsolutePath()) &&
           isEqual(o1.getPrefix(), getPrefix()) &&
           isEqual(o1.getSuffix(), getSuffix()) &&
           isEqual(o1.getDocURL(), getDocURL()) &&
           isEqual(o1.getUserDAS(), getUserDAS()) &&
           isEqual(o1.getFormat(), getFormat()) &&
           ((o1.isRecurse()&&isRecurse())||(!o1.isRecurse()&&!isRecurse())))
           return true;
        else
           return false;
    }

    /** Creates a <code>Handle</code> object from a file
     *  @param file the input file 
     */
    protected Handle createHandle(File file) {
        Handle handle=null;
        String prefix = this.prefix;
        String suffix = this.suffix;
        String docURL = this.docURL;
        String userDAS = this.userDAS;
        String format = this.format;
        String environment = this.environment;
        boolean recurse = this.recurse;
        File dirConfig = new File(dir.getAbsolutePath()+"/dir.xml");
        try {
            if(dirConfig.exists()){
                Document dom = FerretXMLReader.XML2DOM(dirConfig);
                Element tag = (Element)dom.getFirstChild();

   	        // Get format for entry
	        if (tag.hasAttribute("format")) {
	            format = tag.getAttribute("format");
	        }

                //Get prefix and suffix
                if(tag.hasAttribute("prefix"))
                     prefix = tag.getAttribute("prefix");

                if(tag.hasAttribute("suffix"))
                     suffix = tag.getAttribute("suffix");

                if(tag.hasAttribute("environment")){
                     if(environment!=null){
                         environment = environment + ";" + tag.getAttribute("environment");
                     }
                     else{
                         environment = tag.getAttribute("environment");
                     }
                }

	        // Check recurse
	        if (tag.hasAttribute("recurse")){
	            if(tag.getAttribute("recurse").equals("false"))
	               recurse = false;
                    else
                       recurse = true;
	        }

	        // Get documentation url if any
	        if (tag.hasAttribute("doc")) {
	            docURL = tag.getAttribute("doc");
	        }

	        if (tag.hasAttribute("das")) {
	            File dasFile = FileResolver.resolve(Server.getServer().getHome(), 
					                tag.getAttribute("das"));
                    if(dasFile.exists()&&dasFile.isFile())
                        userDAS = dasFile.getAbsolutePath();
 	        }
            }
        }
        catch(Exception e){}

	if (this.recurse && file.isDirectory()) {
	    String subPath = this.completeName + "/" + file.getName();
            File subDir = new File(dir.getAbsolutePath()+"/" + file.getName());

            try {
                handle = new MountDirHandle(subPath,
                                            subDir,
                                            prefix,
                                            suffix,
                                            docURL,
                                            userDAS,
                                            format,
                                            environment,
                                            recurse);
            }
            catch(AnagramException ae){}
	}
        else if(file.isFile() && 
	        file.getName().startsWith(prefix) && 
	        file.getName().endsWith(suffix)) {
		
	    // remove prefix and suffix from name
	    String name = file.getName().substring(prefix.length());
	    name = name.substring(0, name.length() - suffix.length());
            name = getCompleteName() +"/"+name;

            String variables = null;
            File fileConfig = new File(file.getAbsolutePath()+".xml");
            try {
                if(fileConfig.exists()){
                   Document dom = FerretXMLReader.XML2DOM(fileConfig);
                   Element tag = (Element)dom.getFirstChild();

	           // Get documentation url if any
	           if (tag.hasAttribute("doc")) {
	               docURL = tag.getAttribute("doc");
	           }
	
          	   // Get format for entry
	           if (tag.hasAttribute("format")) {
	               format = tag.getAttribute("format");
	           }

                   if (tag.hasAttribute("variables")) {
                       variables = tag.getAttribute("variables");
	           }

                   if(tag.hasAttribute("environment")){
                       if(environment!=null){
                           environment = environment + ";" + tag.getAttribute("environment");
                       }
                       else{
                           environment = tag.getAttribute("environment");
                       }
                   }

	           if (tag.hasAttribute("das")) {
	               File dasFile = FileResolver.resolve(Server.getServer().getHome(), 
		   			   tag.getAttribute("das"));
                       if(dasFile.exists()&&dasFile.isFile())
                           userDAS = dasFile.getAbsolutePath();
 	           }
                }
            }
            catch(Exception e){}

            try {
    
	        FerretDataInfo info = new FerretDataInfo(name, 
                                                         file.getAbsolutePath(),
                                                         variables,
                                                         format, 
                                                         name, 
                                                         docURL,
                                                         userDAS,
                                                         environment,
                                                         null);

	        handle = new DataHandle(name, 
	 			        info.getTitle(), 
					info,
					System.currentTimeMillis());
            }
            catch(AnagramException ae) {}
        }
        return handle;
    }

    protected Handle getHandle(String completeName) {

        String subPath = getChildPath(completeName);
        if(subPath==null)
           return null;
        String prefix = this.prefix;
        String suffix = this.suffix;
        File dirConfig = new File(dir.getAbsolutePath()+"/dir.xml");
        try {
            if(dirConfig.exists()){
                Document dom = FerretXMLReader.XML2DOM(dirConfig);
                Element tag = (Element)dom.getFirstChild();

                //Get prefix and suffix
                if(tag.hasAttribute("prefix"))
                     prefix = tag.getAttribute("prefix");

                if(tag.hasAttribute("suffix"))
                     suffix = tag.getAttribute("suffix");
            }
        }
        catch(Exception e){}
        
        if(!completeName.equals(getCompleteName()+subPath))
           return null;
        String absolutePath =  this.dir.getAbsolutePath()+subPath;
        File handleFile = new File(absolutePath);

	if(handleFile.exists() && handleFile.isDirectory()){
           return createHandle(handleFile);
        }
        else {
           subPath = "/"+prefix+subPath.substring(1)+suffix;
           absolutePath =  this.dir.getAbsolutePath()+subPath;
           handleFile = new File(absolutePath);
           if(handleFile.exists() && handleFile.isFile())
              return createHandle(handleFile);
        }

        return null;
    }

    /** Returns the subdirectory name if the current directory is given 
     *  @param completePath the absolute path of the current dirctory
     */
    protected String getChildPath(String completePath) {
	int subPathStart = this.getCompleteName().length();
        if(subPathStart>=completePath.length())
           return null;
	int subPathEnd = completePath.indexOf('/', subPathStart + 1);
	if (subPathEnd < 0) { 
	    subPathEnd = completePath.length();
	}
	return completePath.substring(subPathStart, subPathEnd);
    }

    /** The file directory from which data is imported*/
    protected File dir;

    /** The prefix for the file names in this directory to be imported*/
    protected String prefix;

    /** The suffix for the file names in this directory to be imported*/
    protected String suffix;

    /** The documentaiton URL for this directory */
    protected String docURL;

    protected String userDAS;

    /** The format of the data inside this directory */
    protected String format;

    protected String environment;

    /** If subdirectory should be considered */
    protected boolean recurse;
}
