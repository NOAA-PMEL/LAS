package gov.noaa.pmel.tmap.ferret.server.dodsservice;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import dods.dap.*;
import dods.dap.Server.*;
import dods.dap.parser.*;

import org.iges.util.*;
import org.iges.anagram.*;

import gov.noaa.pmel.tmap.ferret.server.*;
import gov.noaa.pmel.tmap.ferret.server.dodstype.*;

/** Provides XML for given dataset.<p>
 *
 *  @author Yonghua Wei
 */

public class XMLGenerator 
    extends AbstractGenerator {

    public String getModuleID() {
	return "xml";
    }

    /** Constructs a FerretDODSModule class instance
     * 
     * @param tool reference to {@link FerretTool} module
     */
    public XMLGenerator(FerretTool tool) {
	this.tool = tool;
    }

    /** Gets a XML Document object for all the metadata of this dataset handle
     *
     * @param data the data handle of the input dataset
     * @param privilege The privilege assciated with this request
     * @param useCache if this request uses cached file
     */
    public Document getXML (DataHandle data,
                            Privilege privilege,
                            boolean useCache) 
	throws ModuleException 
    {
         return (Document)getMeta(data, null, privilege, useCache);
    }

    protected Object getMetaFromInput(DataHandle data,
                                      String ce, 
                                      Privilege privilege,
                                      InputStream is)
          throws ModuleException {
	try {
	    return FerretXMLReader.XML2DOM(is);
	}
	catch (Exception e) {
	    info ("exception: "+e.getMessage());
	    throw new ModuleException (this, "unable to generate DOM:"+e.getMessage());
	}
   }

   protected void writeMetaFromInput(DataHandle data,
                                     String ce,
                                     Privilege privilege,
                                     OutputStream out,
                                     InputStream is,
                                     boolean useCache)
          throws ModuleException {
       throw new ModuleException(this, "writeMetaFromInput is not implemented.");
  }
}
