package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;

public class Util {
	public static HashMap<String, String> getTokenMap(String token) {
		String[] tokens = token.split(";");
		HashMap<String, String> tokenMap = new HashMap<String, String>();
		for( int i=0; i < tokens.length; i++ ) {
			String[] parts = tokens[i].split("=");
			String name = parts[0];
			String value = parts[1];
			if ( !value.contains("ferret_") ) {
			    tokenMap.put(name, value);
			}
		}
		return tokenMap;
	}
	public static HashMap<String, String> getOptionsMap(String token) {
		String[] tokens = token.split(";");
		HashMap<String, String> optionsMap = new HashMap<String, String>();
		for( int i=0; i < tokens.length; i++ ) {
			String[] parts = tokens[i].split("=");
			String name = parts[0];
			String value = parts[1];
			if ( name.contains("ferret_") ) {
			    optionsMap.put(name.substring(7, name.length()), value);
			}
		}
		return optionsMap;
	}
	public static String getImageURL() {
		return getBaseURL() + "/images/";
	}
	public static String getBaseURL() {
		String moduleRelativeURL = GWT.getModuleBaseURL();
		int first_path_slash = moduleRelativeURL.indexOf("/", moduleRelativeURL.indexOf("://")+4);
		int second_path_slash = moduleRelativeURL.indexOf("/", first_path_slash+1);
		return moduleRelativeURL.substring(0, second_path_slash);
	}
	public static String format_two(int i) {
		// Really an error for i<10 and i>99, but these are 1<days<31 and 0<hours<23.
		if ( i < 10 ) {
			return "0"+i;
		} else {
			return String.valueOf(i);
		}
	}
	public static String format_four (int i) {
		// Really an error for i<100 and i>9999, but these are years which start at 0001 or at worst 0000.
		if ( i < 10 ) {
			return "000"+i;
		} else if ( i >= 10 && i < 100 ) {
			return "00"+i;
		} else if ( i >= 100 && i < 1000 ) {
			return "0"+i;
		} else {
			return String.valueOf(i);
		}
	}
}
