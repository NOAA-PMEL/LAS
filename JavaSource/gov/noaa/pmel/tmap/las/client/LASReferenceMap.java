package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.Control;
import com.google.gwt.maps.client.control.ControlAnchor;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.event.MapMouseOverHandler;
import com.google.gwt.maps.client.event.MarkerDragStartHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;

public class LASReferenceMap extends Composite {
	SelectControl selectControl;
	MapWidget mMap;
	LatLng mSouthWestCorner;
	LatLng mNorthEastCorner;
	LatLng mCenter;
	int mZoom;
	int mWidth;   // Width in pixels 
	int mHeight;  // Height in pixels 
	/**
	 * Construct an LASReference map centered and zoomed with the width and height specified in pixels.
	 * @param center
	 * @param zoom
	 * @param width
	 * @param height
	 */
    public LASReferenceMap (LatLng center, int zoom, int width, int height) {
    	
    	mCenter = center;
    	mZoom = zoom;
    	mMap = new MapWidget(mCenter, mZoom);
    	mWidth = width;
    	mHeight = height;
		mMap.setSize(String.valueOf(mWidth)+"px", String.valueOf(mHeight)+"px");
		selectControl = new SelectControl(new ControlPosition(ControlAnchor.TOP_LEFT, 10 ,275));
		addControl(selectControl);
		initWidget(mMap);
    }
    public void addControl(Control control) {
    	mMap.addControl(control);
    }
}
