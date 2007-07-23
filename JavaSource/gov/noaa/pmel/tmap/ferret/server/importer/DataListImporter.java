package gov.noaa.pmel.tmap.ferret.server.importer;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;

import org.iges.util.FileResolver;
import org.iges.anagram.*;

import gov.noaa.pmel.tmap.ferret.server.FerretXMLReader;
/** A handler for importing data from a file that lists 
 * the data that need to be imported.
 * The tag name to activate this module is "datalist".
 */
public class DataListImporter
    extends Importer
 {
    /** Returns the name of this importer. Used to 
     * map tag name to importer.
     */
    public String getImporterName() {
       return "datalist";
    }

    public List getHandlesFromTag(Element tag, String baseDir)
    {
	// Get filename for entry
	if (!tag.hasAttribute("file")) {
	    log.error(this, "skipping datalist"
			      + "---no filename provided:"
                              + " need a \"file\" attribute for tag " + tag.getTagName());
	    return null;
	}
	File listFile = FileResolver.resolve(server.getHome(), 
						 tag.getAttribute("file"));

        if(!listFile.exists()){
            log.error(this, "skipping datalist" + 
			     "---file does not exists");
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

	if (verbose()) log.verbose(this, "reading list file " + 
				   listFile.getAbsolutePath());

	// Get documentation url if any
	String docURL = tag.getAttribute("doc");

	String userDAS = null;
	if (tag.hasAttribute("das")) {
	    File dasFile = FileResolver.resolve(server.getHome(), 
					   tag.getAttribute("das"));
            if(dasFile.exists()&&dasFile.isFile())
                userDAS = dasFile.getAbsolutePath();
	}
	
	// Get format for datasets
	String format = tag.getAttribute("format");

        String environment = tag.getAttribute("environment");

	// Get format of list
	boolean readNames = false;
	if (tag.getAttribute("list_format").equals("name")) {
	    readNames = true;
	}

	try {
	    List returnVal = new ArrayList();

            try {
                returnVal.add(new DirHandle(basePath.substring(0,basePath.length()-1)));
            } 
            catch(AnagramException ae){}

	    BufferedReader listReader = 
		new BufferedReader
		    (new FileReader
			(listFile));
	    int lineNo = 0;
	    while (true) {
		String currentLine = listReader.readLine();
		if (currentLine == null) {
		    break;
		}
		lineNo++;
		currentLine = currentLine.trim();
		if (currentLine.startsWith("*") ||
		    currentLine.equals("")) {
		    continue;
		}
		StringTokenizer tokens = new StringTokenizer(currentLine);
		String name = null;
		String file = null;
		try {
		    if (readNames) {
			name = basePath + tokens.nextToken();
			file = tokens.nextToken();
		    } else {
			file = tokens.nextToken();
			name = basePath + file;
		    }
		    try {
			returnVal.add(createHandle(name, 
                                                   file, 
						   docURL,
                                                   userDAS,
						   format,
                                                   null,
                                                   environment,
                                                   null));
		    } catch (AnagramException ae) {
			log.error(this, "can't import " + name + "; " + 
				  ae.getMessage());
		    }			
		} catch (NoSuchElementException nsee) {
		    log.error(this, "line " + lineNo + 
			      " of  datalist " + 
			      listFile.getAbsolutePath() +
			      " skipped due to bad formatting");
		}
	    }
	    return returnVal;

	} catch (IOException ioe) {
	    log.error(this, "skipping datalist" + listFile.getAbsolutePath() +
			     " due to problem while reading. message: " +
			     ioe.getMessage());
	    return null;
	}	    
    }
 
}
