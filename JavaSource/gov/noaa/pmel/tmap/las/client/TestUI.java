package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.maps.client.control.ControlAnchor;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.RootPanel;


public class TestUI extends LASEntryPoint {
    
	public void onModuleLoad() {
		super.onModuleLoad();
		LASReferenceMap map = new LASReferenceMap(LatLng.newInstance(0.0, 0.0), 1, 720, 360);
		map.addControl(new LargeMapControl());
		map.addControl(new MapTypeControl());
		ResetControl reset = new ResetControl(new ControlPosition(ControlAnchor.BOTTOM_RIGHT, 10 ,20), LatLng.newInstance(0.0, 0.0), 1);
		map.addControl(reset);
		
		
        RootPanel.get("refmap").add(map);
	}

}
