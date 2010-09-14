/**
 * This software was developed by Roland Schweitzer of Weathertop Consulting, LLC 
 * (http://www.weathertopconsulting.com/) as part of work performed for
 * NOAA Contracts AB113R-04-RP-0068 and AB113R-09-CN-0182.  
 * 
 * The NOAA licensing terms are explained below.
 * 
 * 
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development. 
 */
package gov.noaa.pmel.tmap.las.client.map;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class NativeMapWidget implements EntryPoint {
	OLMapWidget map = new OLMapWidget();
	@Override
	public void onModuleLoad() {
        map.activateNativeHooks();
		RootPanel.get("ol_map_widget").add(map);
		OLMapWidget.mapDone();
	}

}
