package gov.noaa.pmel.tmap.las.service.climate.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.iosp.Task;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASClimateAnalysisBackendConfig;
import gov.noaa.pmel.tmap.las.jdom.LASFerretBackendConfig;
import gov.noaa.pmel.tmap.las.service.RuntimeEnvironment;
import gov.noaa.pmel.tmap.las.service.TemplateTool;
import gov.noaa.pmel.tmap.las.service.ferret.FerretTool;

public class ClimateAnalysisTool extends TemplateTool {
	LASClimateAnalysisBackendConfig lasClimateAnalysisBackendConfig;
    final Logger log = Logger.getLogger(ClimateAnalysisTool.class.getName());
	public ClimateAnalysisTool() throws LASException, IOException {
		super("climate/analysis", "ClimateAnalysisBackendConfig.xml");
		lasClimateAnalysisBackendConfig = new LASClimateAnalysisBackendConfig();

		try {
			JDOMUtils.XML2JDOM(getConfigFile(), lasClimateAnalysisBackendConfig);
		} catch (Exception e) {
			throw new LASException("Could not parse Ferret config file: " + e.toString());
		}
	}
	public LASBackendResponse run(LASBackendRequest lasBackendRequest) throws Exception {
		LASBackendResponse lasResponse = new LASBackendResponse();
		
		lasClimateAnalysisBackendConfig.setBaseDir(getResourcePath(lasClimateAnalysisBackendConfig.getBaseDir()));
        HashMap<String, String> envMap = lasClimateAnalysisBackendConfig.getEnvironment();
        RuntimeEnvironment runTimeEnv = new RuntimeEnvironment();
        runTimeEnv.setParameters(envMap);
        String scriptDriver = null;
        synchronized(this) {
            scriptDriver = "climate_analysis_operation"
                + "_" + System.currentTimeMillis();
        }
        String tempDir   = lasClimateAnalysisBackendConfig.getTempDir();
        if (tempDir.equals("") ) {
            tempDir = getResourcePath("resources/climate/analysis/temp");
        }
        
      
        File shFile = new File(tempDir + scriptDriver + ".sh");
        
        mergeCommandTemplate(lasBackendRequest, shFile, "climate_launch.vm" );
        String arg_string = readMergedTemplate(shFile);
        String interpreter = lasClimateAnalysisBackendConfig.getInterpreter();
        String binary = lasClimateAnalysisBackendConfig.getExecutable();
        String[] errorKeys = lasClimateAnalysisBackendConfig.getErrorKeys();
        String args[] = arg_string.split(" ");
		String[] cmd = new String[args.length+2];
		cmd[0] = interpreter;
		cmd[1] = binary;
		for (int i = 0; i < args.length; i++) {
			cmd[i+2] = args[i];
		}
		String[] env = runTimeEnv.getEnv();
        Task task = new Task(cmd, env, new File(tempDir), 100000l, errorKeys);
        try {
            log.debug("Running Climate Analysis task.");
            task.run();
        } catch (Exception e) {
        	lasResponse.setError("Climate Analysis did not run correctly. ", e);
            return lasResponse;
        }
        
        String output = task.getOutput();
        String stderr = task.getStderr();
        
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

                log.debug("Task finished.  Looking for output.");
		if ( !task.getHasError() ) {
			String outputdir = findOutput(output);
			if ( outputdir.equals("") ) {
				lasResponse.setError("Unable to find output directory", new Exception("Output parsed and output directory not found."));
				log.error("Output not found: "+output+"\n"+stderr);
			} else {
				log.debug("Output found: "+outputdir);
				List<File> files = (List<File>) FileUtils.listFiles(new File(outputdir), new String[]{"gz", "nc"}, true);
				log.debug("Found "+files.size()+" output files.");
				for (Iterator filesIt = files.iterator(); filesIt.hasNext();) {
					File file = (File) filesIt.next();
					// For each plot make a result
					lasResponse.makeResult(file.getAbsolutePath());
				}
			}
			return lasResponse;
		} else {
			String errorMessage = task.getErrorMessage();
            log.debug("Error message: "+errorMessage);
            log.debug("stderr: "+stderr);
            log.debug("stdout: "+output);
            String error_message = "An error occurred creating your product.";
            try {
                error_message = findMessage(stderr, lasClimateAnalysisBackendConfig);
            } catch (Exception e) {
            	
            		// Go on with a generic error message.
            	
            }
            lasResponse.setError("las_message", error_message);
            try {
            	lasResponse.addError("exception_message", stderr+"\n"+output);
            } catch (Exception e) {
            		lasResponse.addError("exception_message", "Check debug output file for details.");
            }
            return lasResponse;
		}
		
	}
	private String findOutput(String output) {
		BufferedReader in = new BufferedReader(new StringReader(output.toString()));
		String line;
		try {
            while ((line = in.readLine()) != null) {
            	if (line.contains("PLEASE FIND ANY OUTPUT PRODUCED IN - ")) {
            		return line.substring(line.indexOf("- ")+2, line.length());
            	}
            }
            return "";
		} catch (Exception e) {
			return "";
		}
	}
}
