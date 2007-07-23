package gov.noaa.pmel.tmap.ferret.server.dodsservice;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import org.w3c.dom.*;

import dods.dap.*;
import dods.dap.Server.*;
import dods.dap.parser.*;
import dods.servers.ascii.*;

import org.iges.util.*;
import org.iges.anagram.*;

import gov.noaa.pmel.tmap.ferret.server.*;
import gov.noaa.pmel.tmap.ferret.server.dodstype.*;

/** Provides ASC service.<p>
 *
 *  @author Yonghua Wei
 */
public class ASCGenerator 
    extends AbstractModule {

    public String getModuleID() {
	return "asc";
    }

    /** Constructs a FerretDODSModule class instance
     * 
     * @param tool reference to {@link FerretTool} module
     */
    public ASCGenerator(FerretTool tool) {
	this.tool = tool;
    }

    public void configure(Setting setting) 
        throws ConfigException {
    }


    /** Writes a data subset to a stream in ASCII format.
     *
     * @param data the data handle of the input dataset
     * @param ce The constraint to apply to the ASCII request
     * @param privilege The privilege assciated with this ASCII request
     * @param request The HttpServletRequest object 
     * @param out the OutputStream ASCII data should be sent to
     * @param useCache if this request uses cached file
     */
    public void writeASCIIData(DataHandle data, 
			       String ce, 
			       Privilege privilege,
                               HttpServletRequest request,
			       OutputStream out, 
                               boolean useCache)
	throws ModuleException {
	info("writeASCIIData for "+data.getCompleteName());

	PrintWriter ascOut = new PrintWriter(out);
	DConnect url;
	DataDDS dataDDS;
	int suffixIndex = HttpUtils.getRequestURL(request).toString().lastIndexOf(".");
	String requestURL = HttpUtils.getRequestURL(request).substring(0,suffixIndex);

	if(ce==null){
	    ce="";
        } else {
            ce="?" + ce;
	}

	try{
           url = new DConnect(requestURL, true);
           dataDDS = url.getData(ce,null,new asciiFactory());        
	   dataDDS.print(ascOut);
    	   ascOut.println("---------------------------------------------");

           Enumeration varIt = dataDDS.getVariables();

           while(varIt.hasMoreElements()){
               BaseType bt = (BaseType)varIt.nextElement();

               ((toASCII)bt).toASCII(ascOut, true, null, true);
           }

           ascOut.flush();

	} 
        catch(Exception e){
	    throw new ModuleException (this, "Unable to generate ASCII output:" + e.getMessage());
	}
    }

    /** Reference to {@link FerretTool} module
     */
    protected FerretTool tool;
}
