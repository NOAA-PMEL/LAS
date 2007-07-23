package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;
import java.io.*;
import java.util.*;

import dods.dap.*;
import gov.noaa.pmel.tmap.ferret.test.*;

public abstract class AbstractTestDODS 
    extends AbstractTestModule {

    public void test(String strURL) 
       throws Exception {
        if(strURL==null)
          return;

        String dodsPath = addExtensionToURL(strURL, getModuleID());

        boolean accept_deflate = true;
	DConnect url = null;
	
        FDSTest fdstest = FDSTest.getInstance();

        String outputFileName = FDSUtils.shortenName(dodsPath);
        File outputFile = fdstest.getStore().get(outputFileName);
        if(outputFile.exists()){
            outputFile.delete();
        }
        PrintStream ps = new PrintStream(new FileOutputStream(outputFile));

	url = new DConnect(strURL, accept_deflate);

        print(url, ps);

        ps.close();
 
        Task currentTask = Task.currentTask();
        currentTask.getStatus().passLevel(TaskStatus.SYNTAC_LEVEL);
        if(outputFile.exists())
           currentTask.getResultFiles().add(outputFileName);

        boolean passed =
            fdstest.getComparator().compare(dodsPath, true);
           
        if(passed) {
            currentTask.getStatus().passLevel(TaskStatus.MAX_LEVEL);
        }
    }

    protected abstract void print(DConnect url, PrintStream ps)
           throws Exception;

}

