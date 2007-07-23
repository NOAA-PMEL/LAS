package gov.noaa.pmel.tmap.ferret.server.importer;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;

import org.iges.util.FileResolver;
import org.iges.anagram.*;

import gov.noaa.pmel.tmap.ferret.server.FerretXMLReader;
/** A handler for importing data from a file directory.
 * The tag name to activate this module is "datadir".
 */
public class DataDirImporter
    extends Importer
 {

    public String getImporterName() {
       return "datadir";
    }

    public List getHandlesFromTag(Element tag, String baseDir)
    {
	// Get dir name for entry
	if (!tag.hasAttribute("dir")) {
	    log.error(this, "skipping " + tag.getTagName()
			     + "---no directory provided:"
                             + " need a \"dir\" attribute for tag " + tag.getTagName() );
	    return null;
	}
	File dir = FileResolver.resolve(server.getHome(), 
					    tag.getAttribute("dir"));
	if (!dir.isDirectory()) {
	    log.error(this, "skipping datadir " + dir + 
			     "---not a directory");
	    return null;
	}

	String basePath = baseDir;
	if (tag.hasAttribute("name")) {
	    String name  = tag.getAttribute("name");
	    if (name.startsWith("/")) {
		name = name.substring(1);
	    }
	    if (!name.endsWith("/")) {
		name += "/";
	    }
	    basePath += name;
	} 
        else{
	    log.error(this, "skipping mountdir " 
			     + "---no name provided"
                             + " need a \"name\" attribute for tag " + tag.getTagName());
            return null;
        }

	// Get format for entry 
	String format = tag.getAttribute("format");

        String environment = tag.getAttribute("environment");

	// Check recurse
	boolean recurse = true;
	if (tag.hasAttribute("recurse") &&
	    tag.getAttribute("recurse").equals("false")) {
	    recurse = false;
	}

	// Set prefix and suffix
	String prefix = tag.getAttribute("prefix");
	String suffix = tag.getAttribute("suffix");

	// Get documentation url if any
	String docURL = tag.getAttribute("doc");

	String userDAS = null;
	if (tag.hasAttribute("das")) {
	    File dasFile = FileResolver.resolve(server.getHome(), 
					   tag.getAttribute("das"));
            if(dasFile.exists() && dasFile.isFile())
               userDAS = dasFile.getAbsolutePath();
	}

	return loadDir(dir, 
                       basePath, 
                       prefix, 
                       suffix, 
                       docURL,
		       userDAS, 
                       format, 
                       environment, 
                       recurse);
    }

    /**Gets a list of data handles from a file directory
     *
     * @param dir the file directory
     * @param basePath the current base directory
     * @param prefix the required prefix for file name
     * @param suffix the required suffix for file name
     * @param userDAS the user provided DAS
     * @param docURL the documentation URL for this directory
     * @param format the format of imported data, "use" or "jnl"
     * @param recurse if subdirectory need to be import as well
     * @return a list of data handles in this directory 
     */
    private List loadDir(File dir, 
			 String basePath,
			 String prefix,
			 String suffix, 
			 String docURL,
			 String userDAS,
			 String format,
                         String environment,
			 boolean recurse) {
	if (verbose()) log.verbose(this, "searching directory " + 
				   dir.getAbsolutePath());
	List returnVal = new ArrayList();

        try {
            returnVal.add(new DirHandle(basePath.substring(0,basePath.length()-1)));
        } 
        catch(AnagramException ae){}

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
	            File dasFile = FileResolver.resolve(server.getHome(), 
				  	       tag.getAttribute("das"));
                    if(dasFile.exists()&&dasFile.isFile())
                       userDAS = dasFile.getAbsolutePath();
 	        }
            }
        }
        catch(Exception e){}

	File[] contents = dir.listFiles();
	for (int i = 0; i < contents.length; i++) {
            File file = contents[i];
	    if (recurse && file.isDirectory()) {
		String subPath = basePath + contents[i].getName() + "/";
		List subDirEntries = 
		    loadDir(file, 
                            subPath, 
                            prefix, 
			    suffix, 
                            docURL, 
                            userDAS, 
                            format, 
                            environment,
                            recurse);
		returnVal.addAll(subDirEntries);
	    }

	    if (file.isFile() && 
		file.getName().startsWith(prefix) && 
		file.getName().endsWith(suffix)) {
		
	    // remove prefix and suffix from name
	    String name = file.getName().substring(prefix.length());
	    name = name.substring(0, name.length() - suffix.length());
            name = basePath + name;

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
	               File dasFile = FileResolver.resolve(server.getHome(), 
					           tag.getAttribute("das"));
                       if(dasFile.exists() && dasFile.isFile())
                           userDAS = dasFile.getAbsolutePath();
 	           }
                }
            }
            catch(Exception e){}
		try {
		    returnVal.add(createHandle(name, 
					       contents[i].getAbsolutePath(),
					       docURL,
                                               userDAS,
					       format,
                                               variables,
                                               environment,
                                               null));
		} catch (AnagramException ae) {
		    log.error(this, "can't import " + name + "; " + 
			      ae.getMessage());
		}			
	    }
	}
	return returnVal;
    }

 
}
