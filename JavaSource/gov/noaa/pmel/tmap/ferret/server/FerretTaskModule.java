package gov.noaa.pmel.tmap.ferret.server;

import java.io.*;
import org.iges.util.*;
import org.iges.anagram.*;

/** A task module to invokes Ferret as an external process.
 *  Modified from org.iges.grads.server.GradsTaskModule class.
 * 
 * @author Richard Roger, Yonghua Wei 
 */
public class FerretTaskModule 
    extends AbstractModule {

    /** Constructs a {@link FerretTaskModule} object
     * @param tool a reference to {@link FerretTool} module
     */
    public FerretTaskModule(FerretTool tool) {
	this.tool = tool;
    }

    public String getModuleID() {
	return "invoker";
    }
    
    public void configure(Setting setting) 
	throws ConfigException {

	String scriptDirName = setting.getAttribute("script_dir", "scripts");
	scriptDir = FileResolver.resolve(server.getHome(), scriptDirName);
	if (debug()) debug("script directory is " + 
			       scriptDir.getAbsolutePath());
	if (!scriptDir.exists()) {
	    throw new ConfigException(this, "script directory " + 
				      scriptDir.getAbsolutePath() + 
				      " not found");
	}

	timeLimit = setting.getNumAttribute("time_limit", 300);
	if (verbose()) verbose("default time limit set to " + 
			       timeLimit + " sec");

	findFerret(setting);
	verifyFerret();
	useNice = true;

    }

    /** Finds the Ferret binary according to provided setting
     * 
     * @param setting setting tag that indicates the location of Ferret binary
     * @throws ConfigException if the Ferret binary specified by setting does not exist
     */
    protected void findFerret(Setting setting) 
	throws ConfigException {

	String ferretBinaryString = setting.getAttribute("ferret_bin");

	if (debug()) debug("ferret_bin: " + ferretBinaryString);

	if (ferretBinaryString.equals("")) {
	    error ("must specify Ferret executable in using attribute \"ferret_bin\" " 
                   + "of tag \"fds/ferret/invoker\" in fds.xml file");
	    ferretBinary = null;
        }
        else if(ferretBinaryString.equals("ferret")){
            ferretBinary = "ferret";
        }
        else {
	    File ferretBinaryFile = FileResolver.resolve(server.getHome(), 
					                 ferretBinaryString);

            ferretBinary = ferretBinaryFile.getAbsolutePath();

	    if (!ferretBinaryFile.exists()) {
		error("The specified Ferret executable " + 
		      ferretBinaryFile.getAbsolutePath() + 
		      "does not exist");
		ferretBinary = null;
	    }
            else if(ferretBinaryFile.isDirectory()){
		error("The specified Ferret executable " + 
		      ferretBinaryFile.getAbsolutePath() + 
		      " is a directory");
		ferretBinary = null;
            }
	}
	
	if (ferretBinary == null) {
	    throw new ConfigException(this, "couldn't locate any Ferret " + 
				      "executables",
				      setting);
	} 

	info("using Ferret executable " + ferretBinary);
    }

    /** Verifies if Ferret server is working
     *
     * @throws ConfigException if Ferret server is not working correctly
     */
    protected void verifyFerret()
        throws ConfigException {
                                                                                
        verbose("checking Ferret executable");
	checkOutput();
    }

    /** Checks if the output of Ferret binary is correct
     *
     * @param binary the Ferret server binary
     * @throws ConfigExcepiton if the output of the Ferret binary is not correct
     */
    protected void checkOutput()
	throws ConfigException {

	Task task;
	try {
 
	    task = task("FDS_test", new String[0], null, null);

	    task.run();
	} catch (AnagramException ae) {
	    debug("task error: " + ae.getMessage());
	    throw new ConfigException(this, 
				      "Unable to run Ferret executable verification task.\n"
                                       + ferretBinary + " is not a Ferret executable version 5.8 or above."
                                       + "Please make attribute \"ferret_bin\" " 
                                       + "of tag \"fds/ferret/invoker\" in fds.xml file point to "
                                       + "a Ferret executable version 5.8 or above.");
	}
	String output = task.getOutput();

        if(debug()) log.debug(this, "The test output is:"+output);

	BufferedReader in = new BufferedReader(new StringReader(output));
	try {
	    String testStat = in.readLine();
	    debug("checkOutput task output: "+testStat);
	    if (testStat == null || !testStat.equals("<success>")) {
		throw new ConfigException(this, 
					  ferretBinary
					  + " does not appear to be a Ferret " 
					  + "executable. Please make attribute \"ferret_bin\" " 
                                          + "of tag \"fds/ferret/invoker\" in fds.xml file point to "
                                          + "a Ferret executable 5.8 or above.");
	    }
	} catch (IOException ioe) {
	    throw new ConfigException (this, "Unable to verify " + 
		  ferretBinary + " is a valid Ferret executable");
	}

	verbose("verified Ferret executable " + ferretBinary);
    }

    /** Generates a {@link Task} object for given task name and arguments
     *
     * @param taskName the jounal file name that will be run
     * @param args an array of arguments to pass to the jounal file
     * @param envStr a environment string that modifies the environment variables
     * @param workDir a path that specifies what working directory the task should be executed
     * @return a {@link Task} object for this task
     * @throws ModuleException if the specified journal file does not exist.
     */

    public Task task(String taskName, String[] args, String envStr, String workDir)
	throws ModuleException {

	File scriptFile = new File(scriptDir, taskName + ".jnl");
	if (!scriptFile.exists()) {
	    throw new ModuleException(this, "missing script for " + taskName);
	}

	StringBuffer argBuffer = 
	    new StringBuffer(taskName);

	for (int i = 0; i < args.length; i++) {
	    argBuffer.append(" ");
	    argBuffer.append(args[i]);
	}

	int offset = (useNice) ? 1 : 0;
	String[] cmd = new String[offset + 5];
	if (useNice) {
	    cmd[0] = "nice";
	}

	cmd[offset] = ferretBinary;

	cmd[offset + 1] = "-memsize";

        cmd[offset + 2] = "16";

	cmd[offset + 3] = "-script";
       
	cmd[offset + 4] = argBuffer.toString();
     
        String env[]=null;

        RuntimeEnvironment runTimeEnv = tool.getEnvModule().getRuntimeEnvironment();

        if(runTimeEnv!=null){
             runTimeEnv.setEnv(envStr);
             env= runTimeEnv.getEnv();
        }

        File workDirFile = null;
        if(workDir!=null){
            workDirFile = new File(workDir);
        }

	Task task =  new Task(cmd, 
                              env, 
                              workDirFile, 
                              timeLimit);

	if (debug()) log.debug(this, "command line for '" + taskName + 
			       "' task is:\n" + task.getCmd());
	return task;

    }

    /** Returns the default time limit (sec) for this task module
     */
    public long getTimeLimit() {
	return timeLimit;
    }

    /**A reference to {@link FerretTool} module
     */
    protected FerretTool tool;

    /** True if "nice" is prefixed to command 
     */
    protected boolean useNice;

    /** Script directory
     */
    protected File scriptDir;

    /** Default time limit in sec for this task module
     */
    protected long timeLimit;

    /** Ferret binary file
     */
    protected String ferretBinary;
}
