package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;
import java.io.*;
import java.util.*;

import dods.dap.*;

import gov.noaa.pmel.tmap.ferret.test.*;

public class TestAll 
      extends AbstractTestModule {

      public String getModuleID() {
          return "all";
      }

      public void test(String strURL) 
        throws Exception {

          Task currentTask = Task.currentTask();
          TaskStatus status = currentTask.getStatus();

          try {
              FDSTest fdstest = FDSTest.getInstance();
              AbstractTestModule testDAS = fdstest.getDODS().getTestModule("das");
              testDAS.test(strURL);

              AbstractTestModule testDDS = fdstest.getDODS().getTestModule("dds");
              testDDS.test(strURL);

              String ddsURL = addExtensionToURL(strURL, "dds");
              File ddsFile = fdstest.getStore().get(FDSUtils.shortenName(ddsURL));

              status.passLevel(TaskStatus.MIN_LEVEL);

              if(ddsFile.exists()){
                  String dodsPath = stripConstraint(strURL);
                  String ce = createConstraint(ddsFile);
                  String dodsURL = dodsPath+"?"+ce;
                  AbstractTestModule testData = fdstest.getDODS().getTestModule("dods");
                  testData.test(dodsURL);
              }
           }
           catch(Exception e){
               status.passLevel(TaskStatus.MIN_LEVEL);
               throw e;
           }
      }

      protected String stripConstraint(String strURL) {
          int lastQueMarkPos = FDSUtils.lastIndexOf('?', strURL);
          if(lastQueMarkPos>=0){
              return strURL.substring(lastQueMarkPos+1);
          }
          return strURL;
      }

      protected String createConstraint(File ddsFile) 
          throws Exception {

          InputStream is = getInputStream(ddsFile);
          DDS dds = new DDS();
          try{
              dds.parse(is);
          }
          catch(Exception e){
              throw new Exception("dds load failed");
          }

          StringBuffer returnVal = new StringBuffer();
	  Enumeration varIt = dds.getVariables();
          boolean isFirst = true;
	  while (varIt.hasMoreElements()) {
	      BaseType var = (BaseType) varIt.nextElement();
	      if (var instanceof DArray) {
                  if(!isFirst){
		     returnVal.append(",");
                  }

	          DArray dvar = (DArray) var;
	          DArrayDimension dim = dvar.getDimension(0);
                  returnVal.append(dim.getName()+"["+(dim.getSize()-1)+":"+(dim.getSize()-1)+"]");

                  isFirst = false;
	      } else if (var instanceof DGrid) {
                  if(!isFirst){
		     returnVal.append(",");
                  }

 	          DGrid dvar = (DGrid) var;
                  DArray darr = (DArray) (dvar.getVar(0));
                  returnVal.append(darr.getName());
                  int numDim = darr.numDimensions(); 
	          for (int i=0; i < numDim; i+=1) {
	   	      DArrayDimension dim = darr.getDimension(i);
		      returnVal.append("["+(dim.getSize()-1)+":"+(dim.getSize()-1)+"]");
		  }
                  isFirst = false;
	      }   
          }
          return returnVal.toString();
      }

      protected InputStream  getInputStream(File file)
         throws Exception { 
	  try {
	      return new BufferedInputStream
		(new FileInputStream
		    (file));
	  } catch (Exception e) {
	      throw new Exception(file.getAbsolutePath() + 
				      " not found:" + e);
	  } 
      }
}

