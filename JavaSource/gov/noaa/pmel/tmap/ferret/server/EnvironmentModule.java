package gov.noaa.pmel.tmap.ferret.server;

import java.lang.*;
import java.io.*;
import java.util.*;

import org.iges.anagram.*;

/** This module tries to detect the environment variables. If
 *  suceeded, server can change environment variable value at runtime.
 */
public class EnvironmentModule
     extends AbstractModule {

    public String getModuleID() {
	return "environment";
    }

    public EnvironmentModule(FerretTool tool) {
        this.tool = tool;
        isAvailable = false;
    }

    /** Detects and configures the default runtime environment.
     */

    public void configure(Setting setting) {
        String cmmd[] = new String[1];
        cmmd[0] = "printenv";
        Map parameters = new HashMap();
        Task task = new Task(cmmd, null,null,10000);

        try {
           task.run();
        }
        catch(AnagramException ae){
           isAvailable = false;
           return;
        }

        String output = task.getOutput();

	BufferedReader in = new BufferedReader(new StringReader(output));
        String var, value;

        try {
           while(in.ready()) {
               String line = in.readLine();
               if(line == null) 
                   break;

               int equalPos = line.indexOf("=");
               if(equalPos >= 0) {
                  var = line.substring(0,equalPos);
                  value = line.substring(equalPos+1);
               }
               else{
                  var = line;
                  value = "";
               }
               parameters.put(var, value);
           }
        }
        catch(IOException ioe){
            isAvailable = false;
            return;
        }
        defaultRuntimeEnv = new RuntimeEnvironment(parameters);
        isAvailable = true;
        String envString = setting.getAttribute("variables", "");
        if(!envString.equals("")) {
             defaultRuntimeEnv.setEnv(envString);
        }
    }

    /**Is the runtime environment service availalbe
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /** Returns a copy of default runtime environment.
     *  If this module is not in service, null is returned.
     */
    public RuntimeEnvironment  getRuntimeEnvironment(){

        if(!isAvailable())
            return null;

        RuntimeEnvironment returnVal=null;

        try { 
            returnVal = (RuntimeEnvironment) defaultRuntimeEnv.clone();
        }
        catch(CloneNotSupportedException cnse){}

        return returnVal;
    }

    protected FerretTool tool;
    protected RuntimeEnvironment defaultRuntimeEnv;
    protected boolean isAvailable;

}
