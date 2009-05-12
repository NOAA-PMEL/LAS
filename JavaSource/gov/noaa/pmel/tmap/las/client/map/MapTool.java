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
/**
 * Abstract base class for all of the map tools (a lat/lon rectangle, a lat line, a lon line and a point).
 * @author rhs
 *
 */
public abstract class MapTool {
	LatLngBounds selectionBounds;
	LatLng click;
	String strokeColor = "#FF0000";
	int strokeWeight = 3;
	float strokeOpacity = 1.0f;
	String fillColor = "#FF0000";
	float fillOpacity = 0.0f;
	
	ArrayList<Marker> markers = new ArrayList<Marker>();
	/**
	 * Clear any overlays associated with this tool type from the map.
	 */
	public abstract void clearOverlays();
	/**
	 * Make any overlays associated with this tool type visible (or not visible) as per the boolean value.
	 * @param visible true to make the overlays visible, false to make them not visible
	 */
	public abstract void setVisible(boolean visible);
	/**
	 * Return the list of overlay objects used by this tool
	 * @return the overlays used by this tool
	 */
	public abstract ArrayList<Overlay> getOverlays();
	/**
	 * Get the draw marker (usually a black plus sign image) used by this tool
	 * @return the draw marker
	 */
	public abstract Marker getDrawMarker();
	/**
	 * Get the handler that handles mouse moves for this tool
	 * @return handler the MapMouseMoveHandler for this tool
	 */
	public abstract MapMouseMoveHandler getMouseMove();
	public abstract void initSelectionBounds(LatLngBounds dataBounds, LatLngBounds selectionBounds, boolean show);
	public abstract void setSelectionBounds(LatLngBounds rectBounds, boolean recenter, boolean show);
    /**
     * Set whether the grab handles for the tool are visible
     * @param b true to make the grab handles visible, false for not visible
     */
	public void setEditingEnabled(boolean b) {
		for (Iterator markerIt = markers.iterator(); markerIt.hasNext();) {
			Marker marker = (Marker) markerIt.next();
			marker.setVisible(b);
		}
	}
	/**
	 * Get the current selectionBounds for this tool.  Caution, the selectionBounds will likely be a rectangle even for tools that are
	 * degenerate.  You must understand the tool type so you can use the correct values (the extremes or the center).
	 * @return selectionBounds the current selection bounds (see caution above)
	 */
	public LatLngBounds getSelectionBounds() {
		return selectionBounds;
	}
	/**
	 * Get the markers (grab handles) associated with this tool
	 * @return the markers
	 */
	public ArrayList<Marker> getMarkers() {
		return markers;
	}


	/**
	 * Set the LatLng location of where the map was initially clicked when doing a select with this tool.
	 * @param click the location of the initial click
	 */
	public void setClick(LatLng click) {
		this.click = click;
	}
	/**
	 * Add the dragHandler to the marker collection (grab handles) for this tool.  Each tool must implement a MarkerDragHandler that can handle
	 * a drag on each of the grab handles for that tool
	 * @param markerDragHandler the MarkerDragHandler that handles all grab handles for this tool
	 */
	public void addDragHandlers(MarkerDragHandler markerDragHandler) {
		for (Iterator mkIt = markers.iterator(); mkIt.hasNext();) {
			Marker m = (Marker) mkIt.next();
			m.addMarkerDragHandler(markerDragHandler);
		}
	}
	/**
	 * Add a dragEndHandler to the marker collection (grab handles) for this tool
	 * @param markerDragHandler The MarkerDragEndHandler that handles the end of dragging for each grab handle
	 */
	public void addDragEndHandlers(MarkerDragEndHandler markerDragHandler) {
		for (Iterator mkIt = markers.iterator(); mkIt.hasNext();) {
			Marker m = (Marker) mkIt.next();
			m.addMarkerDragEndHandler(markerDragHandler);
		}
	}
	/**
	 * Add a MarkerMouseUpHandler for the marker collection (grab handles) for this tool
	 * @param markerMouseUpHandler the MakerMouseUpHandler that handles the mouse up event for each grab handle
	 */
	public void addMarkerMouseUpHandler(MarkerMouseUpHandler markerMouseUpHandler) {
		for (Iterator mkIt = markers.iterator(); mkIt.hasNext();) {
			Marker m = (Marker) mkIt.next();
			m.addMarkerMouseUpHandler(markerMouseUpHandler);
		}
	}
}
