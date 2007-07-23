package gov.noaa.pmel.tmap.ferret.server;

import java.io.*;
import java.util.*;
import java.net.*;

import org.w3c.dom.*;

import org.iges.util.FileResolver;

import org.iges.anagram.*;
import gov.noaa.pmel.tmap.ferret.server.importer.*;

/** Creates data handles for Ferret datasets specified by 
 *  XML configuration tags in the configuration file "fds.xml". <p>
 *  
 *  Modified from org.iges.grads.server.GradsImportModule class.<p>
 *
 *  @author Yonghua Wei, Richard Roger
 */
public class FerretImportModule 
    extends AbstractModule {

    public String getModuleID() {
	return "importer";
    }

    public FerretImportModule(FerretTool tool) {
	this.tool = tool;
        importers = new HashMap();
    }

    public void configure(Setting setting) {
        Importer imp = new DatasetImporter();
        importers.put(imp.getImporterName() , imp);
        imp = new LASImporter();
        importers.put(imp.getImporterName(), imp);
        imp = new DataDirImporter();
        importers.put(imp.getImporterName(), imp);
        imp = new DataListImporter();
        importers.put(imp.getImporterName(), imp);
        imp = new MountDirImporter();
        importers.put(imp.getImporterName(), imp);

	Iterator it = importers.values().iterator();
	while (it.hasNext()) {
	    ((Module)it.next()).init(server, this);
	}
    }
 
    /** Returns a list of handles from a provided setting
     *
     * @param setting a setting tag
     * @return an array of handles
     */
    public List doImport(Setting setting) {
        //server.getStore().clearCacheFor(this);
	List handles = getHandlesFromXML(setting.getXML(), "/");
	return handles;
    }

    /** Returns a list of handles from a xml tag and the current baseDir.
     *  This is a recursive function.
     *  @param tag the xml tag that describes the data
     *  @param baseDir the current base directory 
     */
    protected List getHandlesFromXML(Element tag, String baseDir) {

	List returnVal = new ArrayList();
	
	NodeList children = tag.getChildNodes();
	// Run through each tag 
	for (int i = 0; i < children.getLength(); i++) {
	    if (!(children.item(i) instanceof Element)) {
		continue;
	    }
	    Element current = (Element)children.item(i);
	    
	    String tagName = current.getTagName();
	    
	    if (tagName.equals("mapdir")) {
		if (!current.hasAttribute("name")) {
		    continue;
		}
		String subDir  = current.getAttribute("name");
		if (subDir.startsWith("/")) {
		    subDir = subDir.substring(1);
		}
		if (subDir.endsWith("/")) {
		    subDir = subDir.substring(0, subDir.length() - 1);
		}

                try {
                    returnVal.add(new DirHandle(baseDir + subDir));
                }
                catch(AnagramException ae){}

		List handles = 
		    getHandlesFromXML(current, baseDir + subDir + "/");
		if (handles != null) {
		    returnVal.addAll(handles);
		}

	    } else {
                Importer importer = (Importer)importers.get(tagName);
                if(importer != null){
                    List handles = importer.getHandlesFromTag(current, baseDir);
                    if (handles != null) {
                        returnVal.addAll(handles);
		    }
                }
            }
	}

	return returnVal;
    }

    /** Reference to {@link FerretTool} module
     */
    protected FerretTool tool;

    /** A map for (importerName, importerModule) pairs
     */
    protected Map importers;
}
