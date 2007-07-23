package gov.noaa.pmel.tmap.ferret.server.importer;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;

import org.iges.util.FileResolver;
import org.iges.anagram.*;

import gov.noaa.pmel.tmap.ferret.server.FerretXMLReader;
/** A handler for importing a single dataset.
 *  The tag name to activate this module is "dataset".
 */
public class DatasetImporter
    extends Importer
 {

    public String getImporterName() {
       return "dataset";
    }

    public List getHandlesFromTag(Element tag, String baseDir)
    {

	if (!tag.hasAttribute("name")) {
	    return null;
	}
              
        List returnVal = new ArrayList();;
	String name  = tag.getAttribute("name");

	// Get filename for entry
	if (!tag.hasAttribute("source")) {
	    log.error(this, "skipping dataset " + name + 
			     "---no file or url provided");
	    return null;
	}
	String source = tag.getAttribute("source");

	// Get documentation url if any
	String docURL = tag.getAttribute("doc");
	
	// Get format for entry 
	String format = tag.getAttribute("format");

	debug("parseDatasetTag format = "+format);

        String variables = null;
        if (tag.hasAttribute("variables")) {
            variables = tag.getAttribute("variables");
	}

	String userDAS = null;
	if (tag.hasAttribute("das")) {
	    File dasFile = FileResolver.resolve(server.getHome(), 
					   tag.getAttribute("das"));
            if(dasFile.exists() && dasFile.isFile())
                userDAS = dasFile.getAbsolutePath();
	}
	
	if (name.equals("")) {
	    // name defaults to filename - extension
	    name = source.substring(source.lastIndexOf("/") + 1);
	    if (name.lastIndexOf(".") > 0) {
		name = name.substring(0, name.lastIndexOf(".") - 1);
	    }
	}

	// might need to check for goofy names starting with '.'

	if (name.startsWith("/")) {
	    name = baseDir + name.substring(1);
	} else {
	    name = baseDir + name;
	}

        String environment = null;
        if(tag.hasAttribute("environment")){
            environment = tag.getAttribute("environment");
        }


	try {
	    returnVal.add(createHandle(name, 
                                       source, 
                                       docURL,
                                       userDAS,
                                       format, 
                                       variables,
                                       environment,
                                       null));
	} catch (AnagramException ae) {
	    log.error(this, "can't import " + name + "; " + 
		      ae.getMessage());
	    return null;
	}			

        return returnVal;
    }
 
}
