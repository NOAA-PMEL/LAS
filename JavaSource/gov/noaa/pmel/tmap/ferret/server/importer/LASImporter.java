package gov.noaa.pmel.tmap.ferret.server.importer;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;

import org.iges.util.FileResolver;
import org.iges.anagram.*;

import gov.noaa.pmel.tmap.ferret.server.*;

/** A handler for importing data from a LAS xml configuration file.
 * The tagname to activate this module is "lasdata".
 */
public class LASImporter
    extends Importer
 {

    public String getImporterName() {
       return "lasdata";
    }

    public List getHandlesFromTag(Element tag, String baseDir)
    {

	if (!tag.hasAttribute("name")
            ||!tag.hasAttribute("las_root")) {
	    return null;
	}

	String subDir  = tag.getAttribute("name");
	if (subDir.startsWith("/")) {
	    subDir = subDir.substring(1);
	}

	if (subDir.endsWith("/")) {
	    subDir = subDir.substring(0, subDir.length() - 1);
	}

        String lasRoot = tag.getAttribute("las_root");
        if(lasRoot.endsWith("/")){
            lasRoot = lasRoot.substring(0, lasRoot.length() - 1);
        }
        File lasRootFile = FileResolver.resolve(server.getHome(), lasRoot);
        if(!lasRootFile.exists())
            return null;
        lasRoot = lasRootFile.getAbsolutePath();

        String environment = getLASEnvironment(tag, lasRoot);

        String xmlFileName = lasRoot+"/server/las.xml";            
        String mapDir = baseDir+subDir+"/";
        File xmlFile = FileResolver.resolve(server.getHome(), xmlFileName);

        List returnVal = new ArrayList();;
        Document lasxml = null;

        try {
            returnVal.add(new DirHandle(baseDir+subDir));
        }
        catch(AnagramException ae){
            return null;
        }

	try {
            lasxml = FerretXMLReader.XML2DOM(xmlFile);
                log.info(this,"las xml configuration file:"+xmlFileName+" is loaded!");

            NodeList children = lasxml.getDocumentElement().getChildNodes();
	    for (int i = 0; i < children.getLength(); i++) {
	        if (!(children.item(i) instanceof Element)) continue;

	        Element current = (Element)children.item(i);
	        if (current.getTagName().equals("datasets")) {

                    NodeList datasets = current.getChildNodes();
                    for(int j = 0; j < datasets.getLength(); j++) {
                       if (!(datasets.item(j) instanceof Element)) continue;

                       Element dataset = (Element)datasets.item(j);
                       String datasetName = dataset.getTagName();
                       String datasetSource = dataset.getAttribute("url");
                       if(!datasetSource.equals("")) {
                           try{
                               URI dsetURI = new URI(datasetSource);
                               if (dsetURI.getScheme()==null||dsetURI.getScheme().equals("file")) {
                                  datasetSource = dsetURI.getSchemeSpecificPart();
                                  if(!datasetSource.startsWith("/")
                                     &&datasetSource.indexOf("/")>0){
                                       datasetSource = lasRoot + "/server/"+datasetSource;
                                  }
		               }
                           }
                           catch(URISyntaxException urise){
                                log.error(this, datasetSource+" is not a correct URI.");
                                continue;
                           }
		       }

                       String dsetDoc = dataset.getAttribute("doc");

                       NodeList nodeList = dataset.getChildNodes();
                       boolean isDataBase = false;
                       Map dsetFerretProps = new LinkedHashMap();

                       for(int k = 0; k < nodeList.getLength();k++) {
                           if(!(nodeList.item(k) instanceof Element)) continue;

                           Element node = (Element)nodeList.item(k);
                           if(node.getTagName().equals("properties")){
                               NodeList properties = node.getChildNodes();
                               for(int l = 0; l< properties.getLength();l++){
                                   if(!(properties.item(l) instanceof Element)) continue;

                                   Element property = (Element)properties.item(l);
                                   if(property.getTagName().equals("database_access")){
                                      isDataBase = true;
                                      break;
                                   }
                                   else if(property.getTagName().equals("ferret")){                                     
				       NodeList ferretProps = property.getChildNodes();
                                       for(int m = 0; m<ferretProps.getLength(); m++){
					  if(!(ferretProps.item(m) instanceof Element)) continue;

                                          Element ferretProp = (Element)ferretProps.item(m);
                                          NodeList propValues = ferretProp.getChildNodes();
                                          for(int n =0; n<propValues.getLength(); n++){ 
					      Node propValue=propValues.item(n);
                                              if(propValue.getNodeType()==Node.TEXT_NODE 
                                                 && !propValue.getNodeName().equals("")){
				                  dsetFerretProps.put(ferretProp.getTagName(),
                                                                      propValue.getNodeValue().toString());
                                                  break;
                                              }
					  }
				       }
				   }
                               }
                           }
                           if(isDataBase) break;
                      }
                      if(isDataBase) continue;

                      try {
                           returnVal.add(new DirHandle(mapDir+datasetName));
                      }
                      catch(AnagramException ae){}

                      for(int k = 0; k < nodeList.getLength();k++) {
                           if(!(nodeList.item(k) instanceof Element)) continue;

                           Element node = (Element)nodeList.item(k);
                           if(node.getTagName().equals("variables")){
                               NodeList variables = node.getChildNodes();
                               for(int l = 0; l< variables.getLength(); l++){
                                   if(!(variables.item(l) instanceof Element)) continue;
  
                                   Element variable = (Element)variables.item(l);
                                   String dodsName = variable.getTagName();
                                   String variableName = "";
                                   String variableSource = variable.getAttribute("url");
                                   if(!variableSource.equals("")) {
                                      if(variableSource.lastIndexOf("#")>=0){
                                         variableName = variableSource.substring(variableSource.lastIndexOf("#")+1);
                                         variableSource = variableSource.substring(0, variableSource.lastIndexOf("#"));
                                      }

                                      try {
                                          URI varURI = new URI(variableSource);
                                          if (varURI.getScheme()==null||varURI.getScheme().equals("file")) {
                                              variableSource = varURI.getSchemeSpecificPart();
                                              if(!variableSource.startsWith("/")
                                                 &&variableSource.indexOf("/")>0){
                                                  variableSource = lasRoot + "/server/"+variableSource;
                                              }
		                          }
                                      }
                                      catch(URISyntaxException urise){
                                          log.error(this, variableSource+" is not a correct URI.");
                                          continue;
                                      }
				   }

                                   if(variableSource.equals(""))
                                       variableSource = datasetSource;
                                   if(variableSource.equals(""))
                                       continue;

                                   if(variableName.equals(""))
                                       variableName = dodsName;
                                 
                                   String varDoc = variable.getAttribute("doc");
                                   if(varDoc.equals(""))
				       varDoc = dsetDoc;

                                   Map varFerretProps = new LinkedHashMap();
                                  
                                   NodeList varNodeList = variable.getChildNodes();
                                   for(int m = 0; m < varNodeList.getLength();m++) {
                                      if(!(varNodeList.item(m) instanceof Element)) continue;

                                      Element varNode = (Element)varNodeList.item(m);
                                      if(varNode.getTagName().equals("properties")){
                                          NodeList properties = varNode.getChildNodes();
                                          for(int n = 0; n< properties.getLength();n++){
                                              if(!(properties.item(n) instanceof Element)) continue;

                                              Element property = (Element)properties.item(n);
                                              if(property.getTagName().equals("ferret")){
			           	          NodeList ferretProps = property.getChildNodes();
                                                  for(int o = 0; o<ferretProps.getLength(); o++){
					              if(!(ferretProps.item(o) instanceof Element)) continue;

                                                      Element ferretProp = (Element)ferretProps.item(o);
                                                      NodeList propValues = ferretProp.getChildNodes();
                                                      for(int p =0; p<propValues.getLength(); p++){ 
					                  Node propValue=propValues.item(p);
                                                          if(propValue.getNodeType()==Node.TEXT_NODE 
                                                             && !propValue.getNodeName().equals("")){
				                              varFerretProps.put(ferretProp.getTagName(),
                                                                                  propValue.getNodeValue().toString());
                                                              break;
                                                          }
					              }
				                  }
				              }
                                          }
                                      }
				   }

                                   if(!varFerretProps.containsKey("init_script"))
				       varFerretProps = dsetFerretProps;

                                   if(varFerretProps.containsKey("init_script")){
                                       File initFile = server.getStore().get(this, mapDir+datasetName+"/"+dodsName+".jnl");
                                       try {
                                           writeInit(initFile, varFerretProps, variableSource, variableName);
                                       }
                                       catch(Exception e){
                                           continue;
                                       }
                                       returnVal.add(createHandle(mapDir+datasetName+"/"+dodsName,
                                                                  initFile.getAbsolutePath(),
                                                                  varDoc,
                                                                  null,
                                                                  "jnl",
                                                                  variableName,
                                                                  environment,
                                                                  lasRoot + "/server"));
                                   }
                                   else{
                                       returnVal.add(createHandle(mapDir+datasetName+"/"+dodsName,
                                                                  variableSource,
                                                                  varDoc,
                                                                  null,
                                                                  null,
                                                                  variableName,
                                                                  environment,
                                                                  lasRoot + "/server"));
                                   }
                               }
                           }
                       }
                    } 
                }
	    }

	} catch (Exception e) {
	    log.error(this, "has problem loading " + xmlFile.getAbsolutePath() + ";" +e);
            return null;
	} 

        return returnVal;
    }
 
    protected void writeInit(File initFile, 
                             Map ferretProps, 
                             String source,
                             String variable)
        throws AnagramException {

        if(ferretProps.containsKey("init_script")) {
	    FileWriter output;

	    try {
	        output = new FileWriter(initFile);
	    } catch (IOException ioe) {
	        throw new AnagramException("writing init wrapper journal:" 
                                           + ioe.getMessage());
	    }

	    PrintWriter initOut = new PrintWriter(output);

            boolean hasError = false;
            String errMessage = null;
            try {
                Iterator eIt = ferretProps.entrySet().iterator();

                while(eIt.hasNext()){
                    Map.Entry current = (Map.Entry)eIt.next();
                    String propName = (String)current.getKey();
                    if(!propName.equals("init_script")){
                        String propValue = (String)current.getValue();
                        initOut.println("define symbol " + propName + "=" + propValue);
                    }
                }

                String init_script = (String)ferretProps.get("init_script");

                initOut.println("go \""+ init_script+"\" \""
                                       + source
                                       + "\" 1 1 " 
                                       + variable);
                initOut.flush();
            } catch (Exception e) {
                errMessage = e.getMessage();
	        info ("error writing init wrapper journal: "+errMessage);
                hasError = true;
	    }
            finally{
                initOut.close();
            }

            if(hasError){
	        throw new AnagramException("error writing init wrapper journal:"
                                           + errMessage);
            }
        }
    }

    protected String getLASEnvironment(Element tag, String lasRoot){

        String returnVal = "";
        RuntimeEnvironment runTimeEnv = ((FerretTool)server.getTool()).getEnvModule().getRuntimeEnvironment();
        if(runTimeEnv!=null){

            returnVal = "FER_GO="
                          + lasRoot + "/server/jnls "
                         // + lasRoot + "/server/jnls/insitu "
                         // + lasRoot + "/server/jnls/section "
                          + lasRoot + "/server/custom "
                          + "$FER_GO;"
                          + "FER_DATA="
                          + lasRoot + "/server/data "
                          + "$FER_DATA;"
                          + "FER_DESCR="
                          + lasRoot + "/server/des "
                          + "$FER_DESCR;"
                          + "DODS_CONF="
                          + lasRoot + "/server/dods/.dodsrc";
           if(tag.hasAttribute("environment")){
              returnVal = returnVal + ";" + tag.getAttribute("environment");
           }
        }
        return returnVal;
    } 
}
