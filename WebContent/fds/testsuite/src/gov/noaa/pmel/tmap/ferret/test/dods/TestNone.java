package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;

public class TestNone 
      extends AbstractTestHTTP {

     public TestNone(){
         exactCompare = false; 
     }

     public String getModuleID() {
          return "";
     }
     
}

