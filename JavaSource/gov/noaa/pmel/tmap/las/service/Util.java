package gov.noaa.pmel.tmap.las.service;

import gov.noaa.pmel.tmap.las.util.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Util {
	
	public static String getTitle(String file) {
		String[] parts = file.split("/");
		return parts[8]+parts[13];
	}
	public static String getSubTitle(String file) {
		String parts[] = file.split("/");
		return parts[14]+" "+parts[12]+" "+parts[15];
	}
    public static Map<String, Map<String, String>> getResults(List<Result> results) {
    	Map<String, Map<String, String>> r = new HashMap<String, Map<String, String>>();
    	for (Iterator resultsIt = results.iterator(); resultsIt.hasNext();) {
			Result result = (Result) resultsIt.next();
			String file = result.getFile();
			String[] parts = file.split("/");
			String region = parts[16];
			Map<String, String> plots = r.get(region);
			if ( plots == null ) {
				plots = new HashMap<String, String>();
				plots.put(parts[17], result.getURL());
				r.put(parts[16], plots);
			} else {
				plots.put(parts[17], result.getURL());
			}
		}
    	return r;
    }
}
