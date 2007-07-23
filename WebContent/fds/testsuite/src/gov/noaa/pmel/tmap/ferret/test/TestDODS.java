package gov.noaa.pmel.tmap.ferret.test;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.net.*;

import gov.noaa.pmel.tmap.ferret.test.dods.*;

public class TestDODS 
    extends AbstractModule {

    public String getModuleID(){
        return "dods";
    }

    public TestDODS() {
        testModules = new HashMap();

        AbstractTestModule module = new TestDDS();
        testModules.put(module.getModuleID(), module);

        module = new TestDAS();
        testModules.put(module.getModuleID(), module);

        module = new TestInfo();
        testModules.put(module.getModuleID(), module);

        module = new TestData();
        testModules.put(module.getModuleID(), module);

        module = new TestAll();
        testModules.put(module.getModuleID(), module);

        module = new TestAsc();
        testModules.put(module.getModuleID(), module);

        testModules.put("ascii", testModules.get("asc"));

        module = new TestNone();
        testModules.put(module.getModuleID(), module);

        module = new TestVer();
        testModules.put(module.getModuleID(), module);

        module = new TestXML();
        testModules.put(module.getModuleID(), module);

        module = new TestDir();
        testModules.put(module.getModuleID(), module);

        module = new TestTHREDDS();
        testModules.put(module.getModuleID(), module);

        module = new TestCatalog();
        testModules.put(module.getModuleID(), module);
    }

    public AbstractTestModule getTestModule(String moduleID){
        return (AbstractTestModule)testModules.get(moduleID);
    }

    public void testURL(String strURL)
        throws Exception {
        strURL = FDSUtils.encodeURL(strURL);
        int questionPos = strURL.indexOf('?');
        String path, ce, extension;
        if(questionPos>=0) {
           path = strURL.substring(0, questionPos);
           ce = strURL.substring(questionPos);
        }
        else {
           path = strURL;
           ce = null;
        }

        int lastDot = path.lastIndexOf('.');
        if(lastDot>=0){        
            extension = path.substring(lastDot+1);
            path = path.substring(0,lastDot);
        }
        else{
            extension = null;
        }

        if(extension!=null){
            if(ce!=null)
               testService(path+ce, extension);
            else
               testService(path, extension);
        }
        else{
            testService(strURL, "");
        }
    }

    protected void testService(String strURL, String service) 
        throws Exception {
        AbstractTestModule testModule= (AbstractTestModule)testModules.get(service);
        if(testModule!=null){
            testModule.test(strURL);
        }
        else{
            TaskStatus status = Task.currentTask().getStatus();
            status.log("Service "+ service +" is not available. But it will be tested anyway.");
            TestAnything testAnything= new TestAnything();
            testAnything.setModuleID(service);
            testAnything.test(strURL);
        }
    }

    protected Map testModules;
}

