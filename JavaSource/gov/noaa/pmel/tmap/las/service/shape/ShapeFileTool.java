package gov.noaa.pmel.tmap.las.service.shape;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;


import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridCoordSys;
import ucar.nc2.dt.grid.GridDataset;

import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.jdom.LASFerretBackendConfig;
import gov.noaa.pmel.tmap.las.jdom.LASShapeFileBackendConfig;
import gov.noaa.pmel.tmap.las.service.RuntimeEnvironment;
import gov.noaa.pmel.tmap.las.service.Task;
import gov.noaa.pmel.tmap.las.service.TemplateTool;
import gov.noaa.pmel.tmap.las.service.ferret.FerretTool;

public class ShapeFileTool extends TemplateTool {
	final Logger log = Logger.getLogger(TemplateTool.class.getName());
	LASShapeFileBackendConfig lasShapeFileBackendConfig;
	public ShapeFileTool() throws LASException, IOException {

		super("shape", "ShapeFileBackendConfig.xml");


		lasShapeFileBackendConfig = new LASShapeFileBackendConfig();

		try {
			JDOMUtils.XML2JDOM(getConfigFile(), lasShapeFileBackendConfig);
		} catch (Exception e) {
			throw new LASException("Could not parse Ferret config file: " + e.toString());
		}
	}

	public ShapeFileTool(String serviceName, String configFileName)
	throws LASException, IOException {
		super(serviceName, configFileName);
	}

	public LASBackendResponse run(LASBackendRequest lasBackendRequest) throws Exception {
		LASBackendResponse lasBackendResponse = new LASBackendResponse();
		RuntimeEnvironment runTimeEnv = new RuntimeEnvironment();
		if ( lasBackendRequest.isCanceled() ) {
			lasBackendResponse.setError("Job canceled");
			return lasBackendResponse;
		}
		File shapeDir = new File(lasBackendRequest.getResultAsFile("dir"));
		if ( !shapeDir.exists() ) {
			shapeDir.mkdir();
		}
		HashMap symbols = lasBackendRequest.getFerretSymbols();
		String url = (String) symbols.get("data_0_url");
		String var = ((String) symbols.get("data_0_var")).toUpperCase();
		File cancel = lasBackendRequest.getCancelFile();
		long timeLimit = lasBackendRequest.getProductTimeout();
		String dir = lasBackendRequest.getResultAsFile("dir");
		Task shapeTask = task(runTimeEnv, cancel, timeLimit, url, var, dir);
		shapeTask.run();
		String output = shapeTask.getOutput();
		String stderr = shapeTask.getStderr();
		String dbf = lasBackendRequest.getResultAsFile("dbf");
		String shp = lasBackendRequest.getResultAsFile("shp");
		String shx = lasBackendRequest.getResultAsFile("shx");
		String zip = lasBackendRequest.getResultAsFile("zip");

		if ( shapeTask.getHasError() ) {
			String errorMessage = shapeTask.getErrorMessage();
			log.debug("Error message: "+errorMessage);
			log.debug("stderr: "+stderr);
			log.debug("stdout: "+output);
			String error_message = "An error occurred creating your product.";

			lasBackendResponse.setError("las_message", error_message);
			try {
				lasBackendResponse.addError("exception_message", stderr+"\n"+output);
			} catch (Exception e) {
				lasBackendResponse.addError("exception_message", "Check debug output file for details.");
			}
			return lasBackendResponse;
		} else {
			// Make a zip of the three output files. 
			// These are the files to include in the ZIP file
			String[] filenames = new String[]{var+".dbf", var+".shp", var+".shx"};

			// Create a buffer for reading the files
			byte[] buf = new byte[1024];

			try {
				// Create the ZIP file

				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));

				// Compress the files
				for (int i=0; i<filenames.length; i++) {
					FileInputStream in = new FileInputStream(dir+File.separator+filenames[i]);

					// Add ZIP entry to output stream. 
					String basename = (new File(dir)).getName();
					out.putNextEntry(new ZipEntry(basename+File.separator+filenames[i]));

					// Transfer bytes from the file to the ZIP file
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}

					// Complete the entry
					out.closeEntry();
					in.close();
				}

				// Complete the ZIP file
				out.close();

				// Move the files to their output names.
				String outdir = dir.substring(0, dir.indexOf("output/")+6);
				File odir = new File(outdir);
				for (int i=0; i<filenames.length; i++) {
					File f = new File(dir+File.separator+filenames[i]);
					String mvfile = null;
					if ( filenames[i].contains("dbf") ) {
						mvfile = dbf;
					} else if ( filenames[i].contains("shp") ) {
						mvfile = shp;
					} else if ( filenames[i].contains("shx") ) {
						mvfile = shx;
					}
					boolean move = false;
					if ( mvfile != null) {
						move = f.renameTo(new File(mvfile));
					}
					if ( !move ) {
						throw new LASException("Unable to move output files.");
					}
				}
				// Remove the now empty output directory...
				File otdir = new File(dir);
				otdir.delete();
			} catch (IOException e) {
				throw new LASException(e.getMessage());
			}          
		}
		lasBackendResponse.addResponseFromRequest(lasBackendRequest);
		return lasBackendResponse;
	}
	public Task task(RuntimeEnvironment runTimeEnv, File cancel, long timeLimit, String url, String var, String dir) throws Exception {

		String[] errors = { "Segmentation fault", "No such", "NetCDF: Variable not found",  "unrecognized option"};

		String tempDir   = lasShapeFileBackendConfig.getTempDir();
		if ( tempDir == "" ) {
			tempDir = getResourcePath("resources/shape/temp");
		}
		boolean useNice = lasShapeFileBackendConfig.getUseNice();
		String interpreter = lasShapeFileBackendConfig.getInterpreter();
		String nc2shape = lasShapeFileBackendConfig.getExecutable();
		int offset = (useNice) ? 1 : 0;
		if ( (interpreter != null && !interpreter.equals("")) ) {
			offset = offset + 1;
		}
		String[] cmd;
		if ( (interpreter != null && !interpreter.equals("")) ) {
			cmd = new String[offset + 1];
		}
		else {
			cmd = new String[offset + 6];
		}

		if (useNice) {
			cmd[0] = "nice";
		}

		if ( (interpreter != null && !interpreter.equals("")) && useNice ) {
			cmd[1] = interpreter;
		} else if ( (interpreter != null && !interpreter.equals("")) && !useNice) {
			cmd[0] = interpreter;
		}

		String x = null;
		String y = null;
		NetcdfDataset ncd = null;
		try {
			ncd = NetcdfDataset.openDataset(url);
			GridDataset gridDs = new GridDataset(ncd);
			List<GridDatatype> grids = gridDs.getGrids();
			for (Iterator gridIt = grids.iterator(); gridIt.hasNext();) {
				GridDatatype grid = (GridDatatype) gridIt.next();
				if ( grid.getName().equals(var)) {
					GridCoordSys gcs = (GridCoordSys) grid.getCoordinateSystem();
					x = gcs.getXHorizAxis().getName();
					y = gcs.getYHorizAxis().getName();				
				}
			}
		} catch (IOException ioe) {
			throw new LASException("Could not read the subset netCDF file.");
		} finally { 
			if (null != ncd) try {
				ncd.close();
			} catch (IOException ioe) {
				throw new LASException("Could not close the subset netCDF file.");
			}
		}

		if ( x == null || y == null ) {
			throw new LASException("Could not read the coordinate axes names from the subset file.");
		}

		if ( (interpreter != null && !interpreter.equals("")) ) {
			// We're ignoring this for now...
		} else {
			cmd[offset] = nc2shape;

			cmd[offset + 1] = "--var="+var;

			cmd[offset + 2] = "--lon="+x;

			cmd[offset + 3] = "--lat="+y;

			cmd[offset + 4] = url;

			cmd[offset + 5] = dir;

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
