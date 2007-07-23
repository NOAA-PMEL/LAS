package gov.noaa.pmel.tmap.ferret.server;

import java.io.*;
import org.iges.util.*;
import org.iges.anagram.*;

/** A wrapper for invoking an external process. */
public class Task {

    /** Sets up an external process. */
    public Task(String[] cmd,
		String [] env,
		File workDir,
		long timeLimit) {
	this.cmd = cmd;
	this.env = env;
	this.workDir = workDir;
	this.timeLimit = timeLimit;
	this.cmdString = buildCmdString(cmd);
	this.output = new StringBuffer();
	this.stderr = new StringBuffer();
    }

    /** Executes the external process, returning when it is finished
     *  or when it exceeds the time limit specified in the constructor.
     * @throws AnagramException If the process fails, or if the 
     * output parser finds an error message in the output.
     */
    public void run() 
	throws AnagramException {

	try {
	    long startTime = System.currentTimeMillis();
	    
	    Process process = Runtime.getRuntime().exec(cmd, env, workDir);

	    if (process == null) {
		throw new AnagramException ("creation of child process " +
					    "failed for unknown reasons\n" +
					    "command: " + cmdString);
	    }

	    finish(process, startTime);

	} catch (IOException ioe) {
	    throw new AnagramException ("creation of child process failed\n" + 
					"command: " + cmdString, ioe);
	}
    }

    /** Executes the external process, returning when it is finished
     *  or when it exceeds the time limit specified.
     * @param timeLimit Overrides the time limit specified in the constructor.
     * @throws AnagramException If the process fails, or if there is an error 
     * message (a line beginning with "error: ") in the output.
     */
    public void run(long timeLimit) 
	throws AnagramException {

        long defaultTimeLimit = this.timeLimit;

        this.timeLimit = timeLimit;

        try {
	    run();
        } catch (AnagramException ae) {
            throw ae;
	} finally {
            this.timeLimit = defaultTimeLimit;
	}
    }


    /** Returns a printable string version of the external command.
     */
    public String getCmd() {
	return cmdString;
    }

    /** Returns a string version of the command's console output */
    public String getOutput() {
	return output.toString();
    }

    /** Builds a command string using an array of commands
     */
    protected String buildCmdString(String[] cmd) {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < cmd.length; i++) {
	    if (i > 0) {
		    buffer.append(" ");
	    }
	    buffer.append(cmd[i]);
	}
	return buffer.toString();
    }

    /** Monitors the running process, puts the process's standard output to 
     * <code>output</code> and errors output to <code>stderr</code>.
     * Also monitors the process' time limit and output limit, checks if
     * there is any error generated.<p>
     * 
     * @param process the running process
     * @param startTime the start time of the process
     * @throws AnagramException if anything goes wrong
     */
    protected void finish(Process process, long startTime) 
	throws AnagramException {
	
	BufferedReader outstream 
	    = new BufferedReader
		(new InputStreamReader
		    (process.getInputStream()));
	BufferedReader errstream 
	    = new BufferedReader
		(new InputStreamReader
		    (process.getErrorStream()));
	
	char[] buffer = new char[1024];
	
        File outputFile;
 
	// wait in 10ms increments for the script to complete
	while (true) {
	    try {
		process.exitValue(); 
		break;
	    } catch (IllegalThreadStateException itse) {
		try {
		    Thread.currentThread().sleep(10);
		} catch (InterruptedException ie) {}
		try {
			if (outstream.ready()) {
			    int charsRead = outstream.read(buffer);
			    output.append(buffer, 0, charsRead);
			}
		} catch (IOException ioe) {}
		try {
			if (errstream.ready()) {
			    int charsRead = errstream.read(buffer);
			    stderr.append(buffer, 0, charsRead);
			}
		} catch (IOException ioe) {}
		
		long endTime = System.currentTimeMillis();
		if (timeLimit>0 && endTime - startTime > timeLimit*1000) {
		    try {
			outstream.close();
			errstream.close();
		    } catch (IOException ioe) {}
		    process.destroy();
		    throw new AnagramException
			("process exceeded time limit of " +
			 timeLimit + " sec");
		}
	    }
	}

	try {
	    while (outstream.ready()) {
		int charsRead = outstream.read(buffer);
		output.append(buffer, 0, charsRead);
	    }
	} catch (IOException ioe) {}
	try {
	    while (errstream.ready()) {
		int charsRead = errstream.read(buffer);
		stderr.append(buffer, 0, charsRead);
	    }
	} catch (IOException ioe) {}

	checkErrors();
	
	try {
	    outstream.close();	
	} catch (IOException ioe) {}
	try {
	    errstream.close();	
	} catch (IOException ioe) {}
	
    }

    /** Checks if there is an error after the task is completed.
     *
     *  @throws AnagramException if there is any error
     */
    protected void checkErrors() 
	throws AnagramException {

	BufferedReader in = 
	    new BufferedReader
		(new StringReader
		    (stderr.toString()));
        findErrorsInStream(in);
        
	in = new BufferedReader
		(new StringReader
		    (output.toString()));
        findErrorsInStream(in);
    }

    /** Look for error information in an input stream. If
     * there is an indication of error, an AnagramException will
     * be thrown.
     */
    protected void findErrorsInStream(BufferedReader in)
         throws AnagramException {
	String line;
	int i;
        boolean hasError = false;
        StringBuffer msg = new StringBuffer();
	try {
	    while ((line = in.readLine()) != null) {
                if(!hasError){
		    for (i=0; i < ERROR_INDICATOR.length; i+=1) {
		        if (line.startsWith(ERROR_INDICATOR[i])) {
			   msg.append(line.substring(ERROR_INDICATOR[i].length()));
                           hasError = true;
                           break;
		        }
		    }
                }
                else{
                    for (i=0; i < ERROR_INDICATOR.length; i+=1) {
    	                if (line.startsWith(ERROR_INDICATOR[i])) {
		             msg.append(";"+line.substring(ERROR_INDICATOR[i].length()));
                             throw new AnagramException(msg.toString().replaceAll("\"","&quot;"));
		        }
		    }
                    String solidLine = FDSUtils.stripSpacesFrom(line);
                    if(solidLine.length()>0){
                            char firstChar = solidLine.charAt(0);
                            if((firstChar<='A'||firstChar>='Z')){
                                msg.append(" "+solidLine);
                            }
                    }
                    throw new AnagramException(msg.toString().replaceAll("\"","&quot;"));
                }
	    }

            if(hasError)
               throw new AnagramException(msg.toString().replaceAll("\"","&quot;"));
	} catch (IOException ioe) {
	    throw new AnagramException("Script output error scan failed: " +
				       ioe.getMessage());
	}
    }


    /** An array of strings that indicate there is an error in Ferret output*/
    protected static final String[] ERROR_INDICATOR = {
	" **ERROR", 
	" **TMAP ERR", 
	"STOP -script mode"
    };

    /** Standard output buffer */
    protected StringBuffer output;
    /** Standard error output buffer */
    protected StringBuffer stderr;
    /** Command string used to create this external process */
    protected String cmdString;
    /** An array of commmands used to create this external process*/
    protected String[] cmd;
    /** String array to describe the environment setting for this external process */
    protected String[] env;
    /** Work directory for this external process */
    protected File workDir;
    /** Default time limit in sec for this external process */
    protected long timeLimit;
}
