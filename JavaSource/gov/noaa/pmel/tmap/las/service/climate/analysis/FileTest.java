package gov.noaa.pmel.tmap.las.service.climate.analysis;

import gov.noaa.pmel.tmap.las.util.FileListing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

public class FileTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			List<File> files = FileListing.getFileListing(new File("/tmp"));
			for (Iterator fileIt = files.iterator(); fileIt.hasNext();) {
				File file = (File) fileIt.next();
				System.out.println(file.getAbsolutePath());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
