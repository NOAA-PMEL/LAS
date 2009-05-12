package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Polygon;
/**
 * Overlay which marks the location of the current valid data area on the map (only used when not global in longitude).
 * @author rhs
 *
 */
public class DataBoundsOverlay {
	LatLng[] polygonPoints;
	Polygon polygon;
	String strokeColor = "#FFFFFF";
	int strokeWeight = 4;
	float strokeOpacity = 1.0f;
	String fillColor = "#FFFFFF";
	float fillOpacity = 0.0f;
	double offset = .45;
	/**
	 * Constructs the overlay (a white polygon by default) to outline the current valid data area.
	 * @param dataBounds
	 */
	public DataBoundsOverlay (LatLngBounds dataBounds) {
		polygonPoints = new LatLng[9];
		LatLng sw = dataBounds.getSouthWest();
		if ( sw.getLatitude() < -88.05 ) {
			sw = LatLng.newInstance(-88.05, sw.getLongitude());
		}
		LatLng ne = dataBounds.getNorthEast();
		if ( ne.getLatitude() > 88.05 ) {
			ne = LatLng.newInstance(88.05, ne.getLongitude());
		}
		polygonPoints[0] = LatLng.newInstance(sw.getLatitude()-offset, sw.getLongitude()-offset);
		polygonPoints[1] = LatLng.newInstance(dataBounds.getCenter().getLatitude(), sw.getLongitude()-offset);
		polygonPoints[2] = LatLng.newInstance(ne.getLatitude()+offset, sw.getLongitude()-offset);
		polygonPoints[3] = LatLng.newInstance(ne.getLatitude()+offset, dataBounds.getCenter().getLongitude());
		polygonPoints[4] = LatLng.newInstance(ne.getLatitude()+offset, ne.getLongitude()+offset);
		polygonPoints[5] = LatLng.newInstance(dataBounds.getCenter().getLatitude(), ne.getLongitude()+offset);
		polygonPoints[6] = LatLng.newInstance(sw.getLatitude()-offset, ne.getLongitude()+offset);
		polygonPoints[7] = LatLng.newInstance(sw.getLatitude()-offset, dataBounds.getCenter().getLongitude());
		polygonPoints[8] = LatLng.newInstance(sw.getLatitude()-offset, sw.getLongitude()-offset);
		polygon = new Polygon(polygonPoints, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity);
	}
	/**
	 * Gets the current overlay polygon that outlines the valid data region.
	 * @return polygon the overlay the defines the valid data area 
	 */
	public Polygon getPolygon() {
		return polygon;
	}
}
