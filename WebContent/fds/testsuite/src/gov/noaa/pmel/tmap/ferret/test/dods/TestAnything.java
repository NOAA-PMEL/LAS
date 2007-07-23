package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;

public class TestAnything 
      extends AbstractTestHTTP {

     public TestAnything() {
         exactCompare = false; 
     }

     public String getModuleID() {
          return moduleID;
     }

     public void setModuleID(String moduleID) {
          this.moduleID = moduleID;
     }

     String moduleID="";
}

