package gov.noaa.pmel.tmap.ferret.server.dodsservice;

import java.io.*;
import java.text.*;
import java.util.*;

import org.w3c.dom.*;

import dods.dap.*;
import dods.dap.Server.*;

import org.iges.util.Range;
import org.iges.anagram.*;

import gov.noaa.pmel.tmap.ferret.server.*;
import gov.noaa.pmel.tmap.ferret.server.dodstype.*;

/** Extracts and caches metadata for a gridded Ferret dataset.<p>
 *
 *  Modified from org.iges.grads.server.GradsGridExtracter class 
 *
 *  @author Yonghua Wei
 */
public class FerretGridExtracter
    extends FerretExtracter {

    public FerretGridExtracter(FerretTool tool){
        super(tool);
    }

    public String getModuleID() {
	return "extract";
    }

    protected File extractSubsetInfo(DataHandle data, String outputName) 
	throws AnagramException {
	throw new AnagramException("extractSubsetInfo not implememted yet");
    }
     

    protected File extractXML (DataHandle data, 
                               Privilege privilege,
                               String outputName, 
                               boolean useCache) 
	throws AnagramException  {
	info("writing XML for "+data.getCompleteName());
        FerretDataInfo ferretInfo = (FerretDataInfo)data.getToolInfo();
        File xmlFile = store.get(this, outputName+".xml");
        boolean hasError = false;
        String errMessage = null;

	FileWriter output;
	try {
	    output = new FileWriter(xmlFile);
	} catch (IOException ioe) {
	    throw new AnagramException("error writing XML for " + 
				       data.getCompleteName()+":"+ioe.getMessage());
	}

	PrintWriter xmlOut = new PrintWriter(output);
	String initCmd = ferretInfo.getFerretCommand(useCache);
        String srcName = "\""+ferretInfo.getTargetName(useCache)+"\"";
	try {
            String envStr = ferretInfo.getEnvironment();
            String workDir = ferretInfo.getWorkDir();
	    Task genXML = tool.getTask().task("FDS_genXML",
                                               new String[]{
					          "\""+initCmd+"\"",
                                                  srcName },
                                               envStr,
                                               workDir
					     );

            long timeout = privilege.getNumAttribute("time_limit", -1);
            if(timeout==-1)
	       genXML.run();
            else
               genXML.run(timeout);

	    info("generate XML completed");

            String xmlStr = genXML.getOutput();

            xmlOut.print(removeWarningTag(xmlStr));
            xmlOut.flush();
	}
	catch (Exception e) {
            errMessage = e.getMessage();
	    info ("exception: unable to generate XML:"+errMessage);
            hasError = true;
	}
        finally{
            xmlOut.close();
        }

        if(hasError){
            xmlFile.delete();
	    throw new ModuleException (this, "unable to generate XML:"+errMessage);
        }

        return xmlFile;

    }

    protected String removeWarningTag(String inputXML)
        throws Exception {
        int start = inputXML.indexOf("<ferret_warnings>");
        int end = inputXML.indexOf("</ferret_warnings>");
        if(start<0||end<0)
            throw new Exception("xml missing warnings tag");

        String returnVal = inputXML.substring(0, start) 
                           + inputXML.substring(end+"</ferret_warnings>".length());
        return returnVal;
    }

    protected File extractDDS(DataHandle data, String outputName) 
	throws AnagramException {
        FerretDataInfo ferretInfo = (FerretDataInfo)data.getToolInfo();
	File ddsFile = store.get(this, outputName+".dds");
        File xmlFile = store.get(this, outputName+".xml");
        boolean hasError = false;
        String errMessage = null;

	if (debug()) log.debug(this, "writing dds to " + ddsFile.getAbsolutePath());

	OutputStream output;
	try {
	    output = new FileOutputStream(ddsFile);
	} catch (Exception e) {
	    throw new AnagramException("error writing dds for " + 
				       data.getCompleteName());
	}
	
	ServerDDS dds = new ServerDDS(data.getDataSetName(),new FerretTypeFactory());
	try {
            Node xml = FerretXMLReader.XML2DOM(new FileInputStream(xmlFile));
            String variables = ferretInfo.getVariables();
	    FerretXMLReader.DOM2DDS(xml, dds, variables);
	    dds.print(output);
	    output.flush();
	}
	catch (Exception e) {
            errMessage = e.getMessage();
	    info ("exception: unable to generate DDS:"+errMessage);
            hasError = true;
	}
	finally{
            try {
               output.close();
	    } catch(IOException ioe){}
        }
 
        if(hasError){
            xmlFile.delete();
            ddsFile.delete();
	    throw new AnagramException("unable to generate DDS: "+errMessage);
        }

        return ddsFile;
    }

    protected File extractDAS(DataHandle data, String outputName) 
	throws AnagramException {
        FerretDataInfo ferretInfo = (FerretDataInfo)data.getToolInfo();
	File dasFile = store.get(this, outputName+".das");
        File xmlFile = store.get(this, outputName+".xml");
        boolean hasError = false;
        String errMessage = null;

	if (debug()) log.debug(this, "writing das to " + dasFile.getAbsolutePath());

	OutputStream output;
	try {
	    output = new FileOutputStream(dasFile);
	} catch (Exception e) {
	    throw new AnagramException("error writing das for " + 
				       data.getCompleteName()+":"+e.getMessage());
	}

        DAS das = new DAS();
	try {
            Node xml = FerretXMLReader.XML2DOM(new FileInputStream(xmlFile));
            String variables = ferretInfo.getVariables();
	    FerretXMLReader.DOM2DAS(xml, das, variables);
	    das.print(output);
	    output.flush();
	} catch (Exception e) {
            errMessage = e.getMessage();
	    info ("exception: unable to generate DAS: "+errMessage);
            hasError = true;
	}
        finally{
            try {
               output.close();
	    } catch(IOException ioe){}
        }

        if(hasError){
            xmlFile.delete();
            dasFile.delete();
	    throw new ModuleException (this, "unable to generate DAS:"+errMessage);
        }

        return dasFile;
    }
    

    protected File extractWebSummary(DataHandle data, String outputName)
	throws AnagramException {

        FerretDataInfo ferretInfo = (FerretDataInfo)data.getToolInfo();
        File infoFile = store.get(this, outputName+".info");
	if (debug()) log.debug(this, "writing web info to " + infoFile.getAbsolutePath());

	FileWriter output;
	try {
	    output = new FileWriter(infoFile);
	} catch (IOException ioe) {
	    throw new AnagramException("error writing web info for " + 
				       data.getCompleteName()+":"+ioe.getMessage());
	}

	PrintWriter info = new PrintWriter(output);

	info.print("<table>\n");
	info.print("   <tbody>\n");
	info.print("     <tr>\n");
	info.print("       <td valign=\"Bottom\" colspan=\"2\">\n");
	info.print("<b>Description:</b><br>\n");
	info.print("       </td>\n");
	info.print("       <td valign=\"Bottom\" colspan=\"2\">\n");
	info.print(data.getDescription());
	info.print("<br>\n");
	info.print("       </td>\n");
	info.print("     </tr>\n");
	info.print("     <tr>\n");
	info.print("       <td valign=\"Bottom\" colspan=\"2\">\n");
	info.print("<b>Documentation:</b>\n");
	info.print("       </td>\n");
	info.print("       <td valign=\"Bottom\" colspan=\"2\">");
        String docURL = ferretInfo.getDocURL();
	if (docURL!=null
            &&!docURL.equals("")
            &&(docURL.startsWith("http:")||docURL.startsWith("/"))) {
	    info.print("<a href=\"\n");
	    info.print(docURL);
	    info.print("\">\n");
	    info.print(docURL);
	} else {
	    info.print("none provided");
	}
	info.print("</a>\n");
	info.print("       <br>\n");
	info.print("       </td>\n");
	info.print("     </tr>\n");

	info.print("     <tr>\n");

	info.print("   \n");
	info.print("  </tbody> \n");
	info.print("</table>\n");


	info.close();

        return infoFile;
    }

    /** Takes the parsed metadata and writes an XML fragment
     * for the THREDDS catalog  */
/*
    public File extractTHREDDSTag(DataHandle data, String outputName)
	throws AnagramException {

        File threddsFile = store.get(this, outputName+".info");
	if (debug()) log.debug(this, "writing thredds tag to " 
                                     + threddsFile.getAbsolutePath());

	FileWriter output;
	try {
	    output = new FileWriter(threddsFile);
	} catch (IOException ioe) {
	    throw new AnagramException("error writing thredds tag for " + 
				       data.getCompleteName()+":"+ioe.getMessage());
	}

	PrintWriter thredds = new PrintWriter(output);

	thredds.print("<dataset name=\"" + data.getDescription() + "\"\n");
	thredds.print("         urlPath=\"" + data.getCompleteName() + "\"\n");
	thredds.print("         dataType=\"Grid\" serviceName=\"" + 
		      server.getModuleName() + "\">\n");
	thredds.print("  <geospatialCoverage>\n");
	thredds.print("    <northlimit>" +  this.maxValues.get("lat") 
		      + "</northlimit>\n");
	thredds.print("    <southlimit>" +  this.minValues.get("lat") 
		      + "</southlimit>\n");
	thredds.print("    <eastlimit>" +  this.maxValues.get("lon") 
		      + "</eastlimit>\n");
	thredds.print("    <westlimit>" +  this.minValues.get("lon") 
		      + "</westlimit>\n");
	thredds.print("  </geospatialCoverage>\n");
	thredds.print("  <timeCoverage>\n");
	thredds.print("    <start>" +  
		      Range.parseGradsFormat((String)this.minValues.get("time")) + 
		      "</start>\n");
	thredds.print("    <end>" +  
		      Range.parseGradsFormat((String)this.maxValues.get("time")) + 
		      "</end>\n");
	thredds.print("  </timeCoverage>\n");
	thredds.print("</dataset>\n");

	thredds.close();

        return threddsFile;
    }
*/

}

