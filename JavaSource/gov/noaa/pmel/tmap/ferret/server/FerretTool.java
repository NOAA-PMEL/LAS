package gov.noaa.pmel.tmap.ferret.server;

import java.lang.*;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
// DODS
import dods.dap.*;
import dods.dap.Server.*;
//DOM
import org.w3c.dom.*;
// Anagram
import org.iges.anagram.*;
import org.iges.util.*;

/** This class is the main interface between Ferret server and Anagram. 
 *  It passes the requests from Anagram to various modules on Ferret server side.
 *
 * @author Yonghua Wei, Richard Roger
 */

public class FerretTool extends Tool {

    //Module methods 
    public String getModuleID() {
	return "ferret";
    }

    public void init(Server server, Module parent) {
	super.init(server, parent);

        if(verbose()) log.verbose(this, "creating environment module");
        env = new EnvironmentModule(this);
        env.init(server, this);

        if (verbose()) log.verbose(this, "creating invoker module");
        task = new FerretTaskModule(this);
        task.init(server, this);

 	if (verbose()) log.verbose(this, "creating dods module");
	dods = new FerretDODSModule(this);
	dods.init(server, this);

	if (verbose()) log.verbose(this, "creating analyzer module");
	analyzer = new FerretAnalysisModule(this);
	analyzer.init(server, this);

	if (verbose()) log.verbose(this, "creating updater module");
	updater = new FerretUpdateModule(this);
	updater.init(server, this);

        if(verbose()) log.verbose(this, "creating cacher module");
        cacher = new FerretCacheModule(this);
        cacher.init(server, this);

        if (verbose()) log.verbose(this, "creating importer module");
        importer = new FerretImportModule(this);
        importer.init(server, this);
   }

    public void configure(Setting setting)
	throws ConfigException {
        configModule(env, setting);
	configModule(task, setting);
	configModule(dods, setting);
	configModule(analyzer, setting);
	configModule(updater, setting);
        configModule(cacher, setting);
	configModule(importer, setting);
    }

    
    // Tool methods
    /** Returns the reference to {@link EnvironmentModule} module
     */
    public EnvironmentModule getEnvModule() {
        return env;
    }

    /** Returns the reference to {@link FerretTaskModule} module
     */
    public FerretTaskModule getTask() {
        return task;
    }

   /** Returns the reference to {@link FerretDODSModule} module
    */
    public FerretDODSModule getDODS() {
        return dods;
    }

   /** Returns the reference to {@link FerretCacheModule} module
    */
    public FerretCacheModule getCacher(){
        return cacher;
    }


    public TempDataHandle doAnalysis(java.lang.String name, 
                                     HttpServletRequest request, 
				     Privilege privilege)
	throws ModuleException {
        return analyzer.doAnalysis(name, request, privilege);
    }

    public boolean allowAnalysis(String name,
				 Privilege privilege)
        throws ModuleException{
        return analyzer.allowAnalysis(name, privilege);
    }
  

    public List doImport(Setting setting) {
	return importer.doImport(setting);
    }

    public boolean doUpdate(DataHandle data) 
	throws ModuleException {
	return updater.doUpdate(data);
    }

    public TempDataHandle doUpload(java.lang.String name, 
				   java.io.InputStream input,
				   long size, Privilege privilege)
	throws ModuleException {
	info("doUpload");
	throw new ModuleException(this, "Not implemented");
    }

    public DAS getDAS(DataHandle data,
                      Privilege privilege,
                      boolean useCache)
	throws ModuleException
    {
	return dods.getDAS(data, privilege, useCache);
    }

    public ServerDDS getDDS(DataHandle data, 
                            java.lang.String ce,
                            Privilege privilege,
                            boolean useCache)
	throws ModuleException 
    {
	return dods.getDDS(data, ce, privilege, useCache);
    }

    public void writeASCIIData(DataHandle data, java.lang.String ce,
			       Privilege privilege,
                               HttpServletRequest request, 
                               java.io.OutputStream out,
                               boolean useCache)
	throws ModuleException
    {
	dods.writeASCIIData(data, ce, privilege, request, out, useCache);
    }

    public void writeBinaryData(DataHandle data, 
                                java.lang.String ce,
				Privilege privilege, 
                                java.io.OutputStream out,
                                boolean useCache)
	throws ModuleException
    {
	dods.writeBinaryData(data, ce, privilege, out, useCache);
    }


    public void writeDDS(DataHandle data, 
                         java.lang.String ce,
                         Privilege privilege,
			 java.io.OutputStream out,
                         boolean useCache)
	throws ModuleException {
	dods.writeDDS(data, ce, privilege, out, useCache);
    }

    public void writeTHREDDSTag(DataHandle data,
                                Privilege privilege,
                                OutputStream out,
                                boolean useCache)
	throws ModuleException {
	dods.writeTHREDDSTag(data, privilege, out, useCache);
    }

    public void writeWebInfo(DataHandle data,
                             Privilege privilege,
                             java.io.OutputStream out,
                             boolean useCache)
	throws ModuleException 
    {
	dods.writeWebInfo(data, privilege, out, useCache);
    }

    /**Returns a XML document for information about a specified dataset 
     *
     * @param data The data object to be accessed
     * @return a org.w3c.dom.Document object containing metadata information for this dataset
     * @throws ModuleException if the request fails for any reason
     */
    public Document getXML (DataHandle data,
                            Privilege privilege,
                            boolean useCache) 
	  throws ModuleException {
        return dods.getXML(data, privilege, useCache);
    }
    
    public void destroy(){
        cacher.destroy();
    }
    
    /** A reference to {@link FerretAnalysisModule} module
     */
    protected FerretAnalysisModule analyzer;

    /** A reference to {@link FerretImportModule} module
     */
    protected FerretImportModule importer;

    /** A reference to {@link FerretDODSModule} module
     */
    protected FerretDODSModule dods; 

    /** A reference to {@link FerretTaskModule} module
     */
    protected FerretTaskModule task;

    /** A reference to {@link FerretUpdateModule} module
     */
    protected FerretUpdateModule updater;

    /**A reference to {@link FerretCacheModule} module
     */
    protected FerretCacheModule cacher;

    /**A reference to {@link EnvironmentModule} module
     */
    protected EnvironmentModule env;
 }

