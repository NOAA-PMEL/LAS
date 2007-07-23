package gov.noaa.pmel.tmap.ferret.test;

import java.io.*;

public class FileComparator
    extends AbstractModule {

    public String getModuleID() {
	return "comparator";
    }

    public boolean compare(String path, boolean exact) 
        throws Exception {

        FDSTest fdstest;
        try{
            fdstest = FDSTest.getInstance();
        }
        catch(Exception e){
            throw e;
        }

        String shortPath = FDSUtils.shortenName(path);
        File stdFile = new File(fdstest.getTestHome() + "/standard" + shortPath);
        File exmFile = fdstest.getStore().get(shortPath);

        TaskStatus status = Task.currentTask().getStatus();
        if(!stdFile.exists()){
            status.log("Standard file " + shortPath + " not exist.");
            return false;
        }

        if(!exmFile.exists()){
            status.log("Created file " + shortPath + " not exist.");
            return false;
        }

        long stdFileLen = stdFile.length();
        long exmFileLen = exmFile.length();
        if(stdFileLen!=exmFileLen){
            long diffLen = stdFileLen-exmFileLen;
            if(diffLen<0) diffLen = 0 - diffLen;
            if(exact||diffLen>60){
               status.log("File length of " + shortPath + " not right.");
               return false;
            }
        }

        try {
            FileInputStream stdIn = new FileInputStream(stdFile);
            FileInputStream exmIn = new FileInputStream(exmFile);

            byte[] stdBuff = new byte[1024];
            byte[] exmBuff = new byte[1024];

            int stdNum, exmNum, compNum = 0;

            do {
                stdNum = stdIn.read(stdBuff);
                exmNum = exmIn.read(exmBuff);

                for(int i=0; i<stdNum; i++){
                   compNum++;
                   if(stdBuff[i]!=exmBuff[i]){
                       if(exact||stdFileLen - compNum >60){
                          status.log("File content of " + shortPath + 
                                     " is not right.");
                          return false;
                       }
                   }
                }
            }
            while(stdNum>0);
            stdIn.close();
            exmIn.close();
            return true;
        }
        catch (Exception e){
            System.err.println(e.getMessage());
            return false;
        }
    }
}
