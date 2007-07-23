package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;

public class TestDir 
      extends AbstractTestHTTP {

     public TestDir(){
         exactCompare = false; 
     }

     public String getModuleID() {
          return "dir";
     }
     
}

