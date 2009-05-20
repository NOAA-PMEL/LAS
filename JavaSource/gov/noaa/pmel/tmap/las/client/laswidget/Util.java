package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.core.client.GWT;

public class Util {
	public static String getImageURL() {
		String moduleRelativeURL = GWT.getModuleBaseURL();
		String moduleName = GWT.getModuleName();
		moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.indexOf(moduleName)-1);
		moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.lastIndexOf("/")+1);
		return moduleRelativeURL + "images/";
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
