package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;

public class TestInfo 
      extends AbstractTestHTTP {

     public TestInfo(){
         exactCompare = false; 
     }

     public String getModuleID() {
          return "info";
     }
     
}

