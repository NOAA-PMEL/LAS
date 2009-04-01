package gov.noaa.pmel.tmap.las.client.map;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.maps.client.event.MapMouseMoveHandler;
import com.google.gwt.maps.client.event.MarkerDragEndHandler;
import com.google.gwt.maps.client.event.MarkerDragHandler;
import com.google.gwt.maps.client.event.MarkerMouseUpHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Overlay;

public abstract class MapTool {
	LatLngBounds selectionBounds;
	LatLng click;
	String strokeColor = "#FF0000";
	int strokeWeight = 3;
	float strokeOpacity = 1.0f;
	String fillColor = "#FF0000";
	float fillOpacity = 0.0f;
	
	ArrayList<Marker> markers = new ArrayList<Marker>();
	
	public abstract void clearOverlays();
	public LatLngBounds getSelectionBounds() {
		return selectionBounds;
	}

	public abstract void setVisible(boolean visible);
	/**
	 * @return the polygon
	 */
	public abstract ArrayList<Overlay> getOverlays();
	
	public abstract Marker getDrawMarker();
	
	public abstract MapMouseMoveHandler getMouseMove();

	public void setEditingEnabled(boolean b) {
		for (Iterator markerIt = markers.iterator(); markerIt.hasNext();) {
			Marker marker = (Marker) markerIt.next();
			marker.setVisible(b);
		}
	}
	/**
	 * @return the markers
	 */
	public ArrayList<Marker> getMarkers() {
		return markers;
	}


	/**
	 * @param click the click to set
	 */
	public void setClick(LatLng click) {
		this.click = click;
	}
	public void addDragHandlers(MarkerDragHandler markerDragHandler) {
		for (Iterator mkIt = markers.iterator(); mkIt.hasNext();) {
			Marker m = (Marker) mkIt.next();
			m.addMarkerDragHandler(markerDragHandler);
		}
	}
	public void addDragEndHandlers(MarkerDragEndHandler markerDragHandler) {
		for (Iterator mkIt = markers.iterator(); mkIt.hasNext();) {
			Marker m = (Marker) mkIt.next();
			m.addMarkerDragEndHandler(markerDragHandler);
		}
	}
	public void addMarkerMouseUpHandler(MarkerMouseUpHandler markerMouseUpHandler) {
		for (Iterator mkIt = markers.iterator(); mkIt.hasNext();) {
			Marker m = (Marker) mkIt.next();
			m.addMarkerMouseUpHandler(markerMouseUpHandler);
		}
	}

	public abstract void initSelectionBounds(LatLngBounds dataBounds, LatLngBounds selectionBounds, boolean show);
	public abstract void setSelectionBounds(LatLngBounds rectBounds, boolean recenter, boolean show);

}
