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
}
