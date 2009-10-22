package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class NativeMapWidget implements EntryPoint {
	OLMapWidget map = new OLMapWidget();
	@Override
	public void onModuleLoad() {
        map.activateNativeHooks();
		RootPanel.get("ol_map_widget").add(map);
	}

}
