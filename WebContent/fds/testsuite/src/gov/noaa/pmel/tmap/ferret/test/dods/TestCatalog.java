package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;
import java.io.*;
import java.util.*;

// JAXP APIs
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.parsers.FactoryConfigurationError; 
import javax.xml.parsers.ParserConfigurationException;
 
import org.xml.sax.*; 

// DOM
import org.w3c.dom.*;

import gov.noaa.pmel.tmap.ferret.test.*;

public class TestCatalog 
      extends AbstractTestModule {

      public String getModuleID() {
          return "catalog";
      }

      public void test(String strURL) 
        throws Exception {
          FDSTest fdstest = FDSTest.getInstance();
          AbstractTestModule testThredds = fdstest.getDODS().getTestModule("thredds");
          testThredds.test(strURL);
          String threddsDodsURL = addExtensionToURL(strURL, "thredds");
          File threddsFile = fdstest.getStore().get(FDSUtils.shortenName(threddsDodsURL));

          Task currentTask = Task.currentTask();
          TaskStatus status = currentTask.getStatus();

          if(threddsFile.exists()){
              Document threddsXML = null;
              try{
                  InputStream is = new BufferedInputStream(new FileInputStream(threddsFile));
                  threddsXML = FDSUtils.XML2DOM(is);
              }
              catch(Exception e){
                  throw new Exception("Can not parse thredds catalog xml:" + e.getMessage());
              }
              Vector urls = getURLs(threddsXML);
              int timeout = getTimeout(strURL);
              Setting taskSetting = createSetting(urls, timeout);

              Task subTask = new Task(taskSetting, currentTask);
              currentTask.getSubTasks().put(subTask.getFullName(), subTask);
              currentTask.getSubTasksLeft().add(subTask.getFullName());     

              subTask.start();
              try {
                   currentTask.sleep(Task.DEFAULT_TIMEOUT);
              }
              catch(Exception e){}
              synchronized(currentTask){
                  if(currentTask.getSubTasksLeft().contains(subTask.getFullName())){
                      currentTask.getSubTasksLeft().remove(subTask.getFullName());
                      subTask.endTask();
                  }
              }

              status.passLevel(TaskStatus.MAX_LEVEL);
          }
          else{
              throw new Exception("Thredds catalog file " 
                                  + threddsFile.getAbsolutePath() + "does not exist.");
          }

      }

      protected Vector getURLs(Document threddsXML){
          Element catalogTag = threddsXML.getDocumentElement();
          if(!(catalogTag.getAttribute("version").equals("0.6")))
              return null;

          //find the root dataset tag
          Node rootDataset = null;
          for(rootDataset = catalogTag.getFirstChild();
              rootDataset != null;
              rootDataset = rootDataset.getNextSibling()) {
              if(rootDataset.getNodeType()==Node.ELEMENT_NODE
                 && rootDataset.getNodeName().equals("dataset"))
                  break;
          }
          if(rootDataset==null)
             return null;

          //create servie name and base url mappings
          Map serviceBaseURLMap = new HashMap();
          for(Node service = rootDataset.getFirstChild();
              service != null;
              service = service.getNextSibling()){
              if(service.getNodeType()==Node.ELEMENT_NODE
                 && service.getNodeName().equals("service")){
                  Element serviceTag = (Element)service;
                  String name = serviceTag.getAttribute("name");
                  String serviceType = serviceTag.getAttribute("serviceType");
                  String base = serviceTag.getAttribute("base");
                  if(!name.equals("")&&serviceType.equals("DODS")){
                      serviceBaseURLMap.put(name, base);
                  }
              }
          }

          //get a list of DODS URL's
          Vector returnVal = new Vector();
          NodeList nodeList = ((Element)rootDataset).getElementsByTagName("dataset");
          for(int i = 0; i<nodeList.getLength(); i++){
              Element currentTag = (Element)nodeList.item(i);
              String urlPath = currentTag.getAttribute("urlPath");
              String serviceName = currentTag.getAttribute("serviceName");
              if(!urlPath.equals("")&&!serviceName.equals("")){
                  String base = (String)serviceBaseURLMap.get(serviceName);
                  if(base!=null){
                      returnVal.add(base + urlPath);
                  }
              }    
          }
          return returnVal;
      }

      protected Setting createSetting(Vector urls, int timeout)
           throws Exception {
           DocumentBuilder builder = 
		DocumentBuilderFactory.newInstance().newDocumentBuilder();
           Document taskXML = builder.newDocument();
           Element rootTaskTag = taskXML.createElement("task");
           rootTaskTag.setAttribute("name", "catalog_test");
           taskXML.appendChild(rootTaskTag);
           
           for(int i=0;i<urls.size();i++){
               Element current = taskXML.createElement("task");
               String strURL = (String)urls.get(i);
               String allURL = addExtensionToURL(strURL, "all");
               current.setAttribute("url", allURL);
               current.setAttribute("timeout", ""+timeout);
               rootTaskTag.appendChild(current);
           }
           return new Setting(taskXML);
      }

      protected int getTimeout(String strURL){
           int queMarkPos = FDSUtils.lastIndexOf('?', strURL);
           if(queMarkPos<0)
               return default_timeout;
           String catalogQuery = strURL.substring(queMarkPos+1);
           if(catalogQuery.startsWith("timeout=")){
               String numMilliSecs = catalogQuery.substring("timeout=".length());
               try {
	           return Integer.valueOf(numMilliSecs).intValue();
	       } catch (NumberFormatException nfe) {
	           return default_timeout;
	       }
           }
           return default_timeout;
      }

      protected int default_timeout = 60000;
}

