/**
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development. 
 */
package gov.noaa.pmel.tmap.las.service.fregrid;

import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASFregridBackendConfig;
import gov.noaa.pmel.tmap.las.service.RuntimeEnvironment;
import gov.noaa.pmel.tmap.las.service.Task;
import gov.noaa.pmel.tmap.las.service.TemplateTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.log4j.Logger;


/**
 * @author rhs
 *
 */
public class FregridTool extends TemplateTool {
	final Logger log = Logger.getLogger(FregridTool.class.getName());
	LASFregridBackendConfig lasFregridBackendConfig;
	/**
	 * @throws IOException 
	 * @throws LASException 
	 * 
	 */
	public FregridTool() throws LASException, IOException {
		super("fregrid", "FregridBackendConfig.xml");
		lasFregridBackendConfig = new LASFregridBackendConfig();
		try {
			JDOMUtils.XML2JDOM(getConfigFile(), lasFregridBackendConfig);
		} catch (Exception e) {
			throw new LASException("Could not parse Fregrid config file: " + e.toString());
		}
	}

	/**
	 * @param serviceName
	 * @param configFileName
	 * @throws LASException
	 * @throws IOException
	 */
	public FregridTool(String serviceName, String configFileName)
	throws LASException, IOException {
		super(serviceName, configFileName);
		// TODO Auto-generated constructor stub
	}
	public LASBackendResponse run(LASBackendRequest lasBackendRequest) throws Exception {
		log.debug("Running the FregridTool.");

		LASBackendResponse lasBackendResponse = new LASBackendResponse();
		// TODO check to see if the request contains a custom fregrid environment

		// Set up the runtime environment.
		lasFregridBackendConfig.setBaseDir(getResourcePath(lasFregridBackendConfig.getBaseDir()));
		HashMap<String, String> envMap = lasFregridBackendConfig.getEnvironment();
		RuntimeEnvironment runTimeEnv = new RuntimeEnvironment();
		// Set any environment variables that were included in the config.
		if ( envMap != null) {
			runTimeEnv.setParameters(envMap);
		}



		if ( lasBackendRequest.isCanceled() ) {
			lasBackendResponse.setError("Job canceled");
			return lasBackendResponse;
		}

		log.debug("Setting up the the Fregrid command line file.");

		String journalName = null;
		synchronized(this) {
			journalName = "fregrid_operation"
				+ "_" + System.currentTimeMillis();
		}

		if ( lasBackendRequest.isCanceled() ) {
			lasBackendResponse.setError("Job canceled");
			return lasBackendResponse;
		}

		String tempDir   = lasFregridBackendConfig.getTempDir();
		if ( tempDir == "" ) {
			tempDir = getResourcePath("resources/fregrid/temp");
		}

		if ( lasBackendRequest.isCanceled() ) {
			lasBackendResponse.setError("Job canceled");
			return lasBackendResponse;
		}

		File fregridFile = new File(tempDir + journalName + ".jnl");

		if ( lasBackendRequest.isCanceled() ) {
			lasBackendResponse.setError("Job canceled");
			return lasBackendResponse;
		}

		log.debug("Creating Fregrid journal file.");

		mergeCommandTemplate(lasBackendRequest, fregridFile, "launch.vm" );

		log.debug("Finished creating Fregrid journal file.");

		String args_string = readMergedTemplate(fregridFile);

		log.debug("Creating Fregrid task.");

		Task fregridTask;
		long timeLimit = lasBackendRequest.getProductTimeout();

		if ( lasBackendRequest.isCanceled() ) {
			lasBackendResponse.setError("Job canceled");
			return lasBackendResponse;
		}

		try {
			fregridTask = task(runTimeEnv, args_string, lasBackendRequest.getCancelFile(), timeLimit);
		} catch (Exception e) {
			lasBackendResponse.setError("Could not create Fregrid task. ", e);
			return lasBackendResponse;
		}

		log.debug("Fregrid task created.");

		if ( lasBackendRequest.isCanceled() ) {
			lasBackendResponse.setError("Job canceled");
			return lasBackendResponse;
		}

		log.debug("Running Fregrid task.");

		try {
			fregridTask.run();
		} catch (Exception e) {
			lasBackendResponse.setError("Fregrid did not run correctly. ", e);
			return lasBackendResponse;
		}

		log.debug("Fregrid Task finished.");

		if ( lasBackendRequest.isCanceled() ) {
			lasBackendResponse.setError("Job canceled");
			return lasBackendResponse;
		}

		log.debug("Checking for errors.");
		String output = fregridTask.getOutput();
		String stderr = fregridTask.getStderr();

		// Make a debug file so we can see what happened.
		String debug_filename = lasBackendRequest.getResultAsFileByType("debug");
		if ( debug_filename != null && !debug_filename.equals("") ) {
			PrintWriter debugWriter=null;
			File debug = new File(debug_filename);
			debugWriter = new PrintWriter(new FileOutputStream(debug));
			debugWriter.println(stderr);
			debugWriter.println(output);
			debugWriter.flush();
			debugWriter.close();

		}

		// Build the response as if it worked.  If it did not, the response will get modified.
		lasBackendResponse.addResponseFromRequest(lasBackendRequest);

		if ( fregridTask.getHasError() ) {

			// Error was generated.  Make error page instead.

			String errorMessage = fregridTask.getErrorMessage();
			log.debug("Error message: "+errorMessage);
			log.debug("stderr: "+stderr);
			log.debug("stdout: "+output);
			String error_message = "An error occurred creating your product.";
			try {
				error_message = findMessage(stderr, lasFregridBackendConfig);
			} catch (Exception e) {

				// Go on with a generic error message.

			}
			lasBackendResponse.setError("las_message", error_message);
			try {
				lasBackendResponse.addError("exception_message", stderr+"\n"+output);
			} catch (Exception e) {
				lasBackendResponse.addError("exception_message", "Check debug output file for details.");
			}
			return lasBackendResponse;
		}



		if ( lasBackendRequest.isCanceled() ) {
			lasBackendResponse.setError("Job canceled");
			return lasBackendResponse;
		}

		log.debug("Finished running the FregridTool.");

		return lasBackendResponse;
	}
	public Task task(RuntimeEnvironment runTimeEnv, String arg_string, File cancel, long timeLimit) throws Exception {

		String[] errors = lasFregridBackendConfig.getErrorKeys();

		String tempDir   = lasFregridBackendConfig.getTempDir();
		if ( tempDir == "" ) {
			tempDir = getResourcePath("resources/fregrid/temp");
		}

		boolean useNice = lasFregridBackendConfig.getUseNice();
		String binary = lasFregridBackendConfig.getExecutable();
		
		String args[] = arg_string.split(" ");
		
		int offset = (useNice) ? 1 : 0;
                // Offset + the binary iteself + number of args in the template
		String[] cmd = new String[offset + 1 + args.length];
		

		if (useNice) {
			cmd[0] = "nice";
		}
        
		cmd[offset] = binary;
		offset++;
		for (int i = 0; i < args.length; i++) {
			cmd[offset + i] = args[i];
		}
		
		String env[] = runTimeEnv.getEnv();

		File workDirFile = null;
		if (tempDir != null) {
			workDirFile = new File(tempDir);
		}


		Task task = new Task(cmd, env, workDirFile, cancel, timeLimit, errors);

		System.out.println("command line for task is:\n"
				+ task.getCmd());
		return task;

	}
}
