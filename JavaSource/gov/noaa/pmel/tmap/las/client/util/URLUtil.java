package gov.noaa.pmel.tmap.las.client.util;

import com.google.gwt.core.client.GWT;

public class URLUtil {
	public static String getImageURL() {
		return getBaseURL() + "images/";
	}
	public static String getBaseURL() {
		String moduleRelativeURL = GWT.getModuleBaseURL();
		int first_path_slash = moduleRelativeURL.indexOf("/", moduleRelativeURL.indexOf("://")+4);
		int second_path_slash = moduleRelativeURL.indexOf("/", first_path_slash+1);
		String base =  moduleRelativeURL.substring(0, second_path_slash) + "/";
		return base;
	}
}
