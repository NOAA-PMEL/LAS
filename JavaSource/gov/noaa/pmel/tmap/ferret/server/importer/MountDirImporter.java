package gov.noaa.pmel.tmap.ferret.server.importer;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;

import org.iges.util.FileResolver;
import org.iges.anagram.*;
import gov.noaa.pmel.tmap.ferret.server.FerretDataInfo;

import gov.noaa.pmel.tmap.ferret.server.FerretXMLReader;
/** A handler for importing data inside a file directory dynamically.
 *  That is, users can drop a data file into this directory and FDS will
 *  show it right away through catalog.
 *  The tag name to activate this module is "mountdir". 
 */
public class MountDirImporter
    extends Importer
 {
    public String getImporterName() {
       return "mountdir";
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
	    log.error(this, "skipping mountdir " + dir + 
			     "---not a directory");
	    return null;
	}

	String basePath = baseDir;
	if (tag.hasAttribute("name")) {
	    String name  = tag.getAttribute("name");
	    if (name.startsWith("/")) {
		name = name.substring(1);
	    }
	    if (name.endsWith("/")) {
		name = name.substring(0,name.length()-1);
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
	String format  = tag.getAttribute("format");

        String environment = tag.getAttribute("environment");

	// Check recurse
	boolean recurse = true;
	if (tag.hasAttribute("recurse") &&
	    tag.getAttribute("recurse").equals("false")) {
	    recurse = false;
	}

        String prefix = tag.getAttribute("prefix");
        String suffix = tag.getAttribute("suffix");        

	// Get documentation url if any
	String docURL = tag.getAttribute("doc");

	String userDAS = null;
	if (tag.hasAttribute("das")) {
	    File dasFile = FileResolver.resolve(server.getHome(), 
					   tag.getAttribute("das"));
            if(dasFile.exists()&&dasFile.isFile())
                userDAS = dasFile.getAbsolutePath();
	}

	List returnVal = new ArrayList();
        MountDirHandle mDir = null;
        try {
            mDir = new MountDirHandle(basePath,
                                      dir,
                                      prefix,
                                      suffix,
                                      docURL,
                                      userDAS,
                                      format,
                                      environment,
                                      recurse);
	
            log.info(this, "mounted directory " + basePath);
        }
        catch(AnagramException ae){}

        if(mDir != null){
            returnVal.add(mDir);
            returnVal.addAll(mDir.getEntries(true).values());        
        }

	return returnVal;
    }
 
}
