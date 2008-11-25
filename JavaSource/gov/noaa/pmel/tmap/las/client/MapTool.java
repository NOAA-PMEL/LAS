package gov.noaa.pmel.tmap.las.client;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MarkerDragHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polygon;

public class MapTool {
	String type;
	LatLng[] polygonPoints;
	Polygon polygon;
	LatLngBounds dataBounds;
	LatLngBounds currentBounds;
	LatLng click;
	String strokeColor = "#FF0000";
	int strokeWeight = 3;
	float strokeOpacity = 1.0f;
	String fillColor = "#FF0000";
	float fillOpacity = 0.0f;
	Marker swMarker;
	Marker sw_nwMarker;
	Marker nwMarker;
	Marker nw_neMarker;
	Marker neMarker;
	Marker ne_seMarker;
	Marker seMarker;
	Marker sw_seMarker;
	Marker centerMarker;
	ArrayList<Marker> markers;
	MapWidget mMap;
	/**
	 * Construct a marker tool with the default colors, weights and opacities.
	 * @param bounds
	 * @param type
	 */
	public MapTool(MapWidget map, LatLngBounds dBounds, LatLngBounds selectedBounds, String type) {
		
		this.mMap = map;
		this.dataBounds = dBounds;
		this.currentBounds = selectedBounds;
		this.type = type;
		this.markers = new ArrayList<Marker>();
		
		String moduleRelativeURL = GWT.getModuleBaseURL();
        String moduleName = GWT.getModuleName();
        moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.indexOf(moduleName)-1);
        moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.lastIndexOf("/")+1);
        String imageURL = moduleRelativeURL + "images/";
		
		if ( type.equals("xy") ) {
			polygonPoints = new LatLng[5];
			polygonPoints[0] = LatLng.newInstance(currentBounds.getSouthWest().getLatitude(), currentBounds.getSouthWest().getLongitude());
			polygonPoints[1] = LatLng.newInstance(currentBounds.getNorthEast().getLatitude(), currentBounds.getSouthWest().getLongitude());
			polygonPoints[2] = LatLng.newInstance(currentBounds.getNorthEast().getLatitude(), currentBounds.getNorthEast().getLongitude());
			polygonPoints[3] = LatLng.newInstance(currentBounds.getSouthWest().getLatitude(), currentBounds.getNorthEast().getLongitude());
			polygonPoints[4] = LatLng.newInstance(currentBounds.getSouthWest().getLatitude(), currentBounds.getSouthWest().getLongitude());
			polygon = new Polygon(polygonPoints, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity);
			
			LatLng center = selectedBounds.getCenter();
			
			Icon sw_icon = Icon.newInstance();
			sw_icon.setIconSize(Size.newInstance(12, 12));
			sw_icon.setIconAnchor(Point.newInstance(5, 5));
			sw_icon.setImageURL(imageURL+"edit_square.png");
			MarkerOptions sw_options = MarkerOptions.newInstance();
	        sw_options.setIcon(sw_icon);
			sw_options.setDraggable(true);
			sw_options.setDragCrossMove(true);
			sw_options.setBouncy(false);
			sw_options.setTitle("sw");
			swMarker = new Marker(selectedBounds.getSouthWest(), sw_options);
			swMarker.addMarkerDragHandler(markerDragHandler);
			markers.add(swMarker);
			
			Icon sw_nw_icon = Icon.newInstance();
			sw_nw_icon.setIconSize(Size.newInstance(12, 12));
			sw_nw_icon.setIconAnchor(Point.newInstance(5, 5));
			sw_nw_icon.setImageURL(imageURL+"edit_square.png");
			MarkerOptions sw_nw_options = MarkerOptions.newInstance();
	        sw_nw_options.setIcon(sw_nw_icon);
	        sw_nw_options.setDraggable(true);
	        sw_nw_options.setDragCrossMove(true);
	        sw_nw_options.setBouncy(false);
	        sw_nw_options.setTitle("sw_nw");
			sw_nwMarker = new Marker(LatLng.newInstance(center.getLatitude(), currentBounds.getSouthWest().getLongitude()), sw_nw_options);
			sw_nwMarker.addMarkerDragHandler(markerDragHandler);
			markers.add(sw_nwMarker);
			
			Icon nw_icon = Icon.newInstance();
			nw_icon.setIconSize(Size.newInstance(12, 12));
			nw_icon.setIconAnchor(Point.newInstance(5, 5));
			nw_icon.setImageURL(imageURL+"edit_square.png");
			MarkerOptions nw_options = MarkerOptions.newInstance();
			nw_options.setIcon(nw_icon);
			nw_options.setDraggable(true);
			nw_options.setDragCrossMove(true);
			nw_options.setBouncy(false);
			nw_options.setTitle("nw");
			nwMarker = new Marker(LatLng.newInstance(currentBounds.getNorthEast().getLatitude(), currentBounds.getSouthWest().getLongitude()), nw_options);
			nwMarker.addMarkerDragHandler(markerDragHandler);
			markers.add(nwMarker);
			
			Icon nw_ne_icon = Icon.newInstance();
			nw_ne_icon.setIconSize(Size.newInstance(12, 12));
			nw_ne_icon.setIconAnchor(Point.newInstance(5, 5));
			nw_ne_icon.setImageURL(imageURL+"edit_square.png");
			MarkerOptions nw_ne_options = MarkerOptions.newInstance();
			nw_ne_options.setIcon(nw_ne_icon);
			nw_ne_options.setDraggable(true);
			nw_ne_options.setDragCrossMove(true);
			nw_ne_options.setBouncy(false);
			nw_ne_options.setTitle("nw_ne");
			nw_neMarker = new Marker(LatLng.newInstance(currentBounds.getNorthEast().getLatitude(), center.getLongitude()), nw_ne_options);
			nw_neMarker.addMarkerDragHandler(markerDragHandler);
			markers.add(nw_neMarker);
			
			Icon ne_icon = Icon.newInstance();
			ne_icon.setIconSize(Size.newInstance(12, 12));
			ne_icon.setIconAnchor(Point.newInstance(5, 5));
			ne_icon.setImageURL(imageURL+"edit_square.png");
			MarkerOptions ne_options = MarkerOptions.newInstance();
			ne_options.setIcon(ne_icon);
			ne_options.setDraggable(true);
			ne_options.setDragCrossMove(true);
			ne_options.setBouncy(false);
			ne_options.setTitle("ne");
			neMarker = new Marker(currentBounds.getNorthEast(), ne_options);
			neMarker.addMarkerDragHandler(markerDragHandler);
			markers.add(neMarker);
			
			Icon ne_se_icon = Icon.newInstance();
			ne_se_icon.setIconSize(Size.newInstance(12, 12));
			ne_se_icon.setIconAnchor(Point.newInstance(5, 5));
			ne_se_icon.setImageURL(imageURL+"edit_square.png");
			MarkerOptions ne_se_options = MarkerOptions.newInstance();
			ne_se_options.setIcon(ne_se_icon);
			ne_se_options.setDraggable(true);
			ne_se_options.setDragCrossMove(true);
			ne_se_options.setBouncy(false);
			ne_se_options.setTitle("ne_se");
			ne_seMarker = new Marker(LatLng.newInstance(center.getLatitude(), currentBounds.getNorthEast().getLongitude()), ne_se_options);
			ne_seMarker.addMarkerDragHandler(markerDragHandler);
			markers.add(ne_seMarker);
			
			Icon se_icon = Icon.newInstance();
			se_icon.setIconSize(Size.newInstance(12, 12));
			se_icon.setIconAnchor(Point.newInstance(5, 5));
			se_icon.setImageURL(imageURL+"edit_square.png");
			MarkerOptions se_options = MarkerOptions.newInstance();
			se_options.setIcon(se_icon);
			se_options.setDraggable(true);
			se_options.setDragCrossMove(true);
			se_options.setBouncy(false);
			se_options.setTitle("se");
			seMarker = new Marker(LatLng.newInstance(currentBounds.getSouthWest().getLatitude(), currentBounds.getNorthEast().getLongitude()), se_options);
			seMarker.addMarkerDragHandler(markerDragHandler);
			markers.add(seMarker);
			
			Icon sw_se_icon = Icon.newInstance();
			sw_se_icon.setIconSize(Size.newInstance(12, 12));
			sw_se_icon.setIconAnchor(Point.newInstance(5, 5));
			sw_se_icon.setImageURL(imageURL+"edit_square.png");
			MarkerOptions sw_se_options = MarkerOptions.newInstance();
			sw_se_options.setIcon(sw_se_icon);
			sw_se_options.setDraggable(true);
			sw_se_options.setDragCrossMove(true);
			sw_se_options.setBouncy(false);
			sw_se_options.setTitle("sw_se");
			sw_seMarker = new Marker(LatLng.newInstance(currentBounds.getSouthWest().getLatitude(), center.getLongitude()), sw_se_options);
			sw_seMarker.addMarkerDragHandler(markerDragHandler);
			markers.add(sw_seMarker);
			
			Icon center_icon = Icon.newInstance();
			center_icon.setIconSize(Size.newInstance(12, 12));
			center_icon.setIconAnchor(Point.newInstance(5, 5));
			center_icon.setImageURL(imageURL+"edit_square.png");
			MarkerOptions center_options = MarkerOptions.newInstance();
			center_options.setIcon(center_icon);
			center_options.setDraggable(true);
			center_options.setDragCrossMove(true);
			center_options.setBouncy(false);
			center_options.setTitle("center");
			centerMarker = new Marker(currentBounds.getCenter(), center_options);
			centerMarker.addMarkerDragHandler(markerDragHandler);
			markers.add(centerMarker);
		}
		
	}
	MarkerDragHandler markerDragHandler = new MarkerDragHandler() {
		public void onDrag(MarkerDragEvent event) {
			Marker marker = event.getSender();
			String title = marker.getTitle();
			LatLng markerLocation = marker.getLatLng();
			double markerLon = markerLocation.getLongitude();
			double markerLat = markerLocation.getLatitude();
			while ( markerLon < 0.0 ) {
				markerLon = markerLon + 360.;
			}
			double westBoundsLon;
			double eastBoundsLon;
			double southBoundsLat;
			double northBoundsLat;
			LatLng sw = polygon.getBounds().getSouthWest();
            LatLng ne = polygon.getBounds().getNorthEast();
			if ( title.equals("sw") ) {
				// The south west marker's movements are bounded by the south west data bounds and the north east rectangle bounds.
				westBoundsLon = dataBounds.getSouthWest().getLongitude();
				eastBoundsLon = ne.getLongitude();
				southBoundsLat = dataBounds.getSouthWest().getLatitude();
				northBoundsLat = ne.getLatitude();
				while ( westBoundsLon < 0.0 ) {
					westBoundsLon = westBoundsLon + 360.;
				}
				while (eastBoundsLon < 0.0 ) {
					eastBoundsLon = eastBoundsLon + 360.;
				}
				if ( markerLon >= eastBoundsLon ) {
					markerLon = eastBoundsLon;
				}
				if ( markerLon <= westBoundsLon ) {
					markerLon = westBoundsLon;
				}
				if (markerLat <= southBoundsLat ) {
					markerLat = southBoundsLat;
				}
				if ( markerLat >= northBoundsLat ) {
					markerLat = northBoundsLat;
				}
				sw = LatLng.newInstance(markerLat, markerLon);
			} else if ( title.equals("sw_nw") )  {
				// The west line marker's movements abound bounded by it's current latitude and the west data bounds and the east
				// rectangle bounds.
				markerLat = polygon.getBounds().getCenter().getLatitude();
				westBoundsLon = dataBounds.getSouthWest().getLongitude();
				eastBoundsLon = ne.getLongitude();
				while ( westBoundsLon < 0.0 ) {
					westBoundsLon = westBoundsLon + 360.;
				}
				while (eastBoundsLon < 0.0 ) {
					eastBoundsLon = eastBoundsLon + 360.;
				}
				if ( markerLon >= eastBoundsLon ) {
					markerLon = eastBoundsLon;
				}
				if ( markerLon <= westBoundsLon ) {
					markerLon = westBoundsLon;
				}
				sw = LatLng.newInstance(sw.getLatitude(), markerLon);
			} else if ( title.equals("nw") ) {
				// The north west corner is bounded by the west and north data bounds and the south east rectangle corner.
				northBoundsLat = dataBounds.getNorthEast().getLatitude();
				southBoundsLat = sw.getLatitude();
				westBoundsLon = dataBounds.getSouthWest().getLongitude();
				eastBoundsLon = ne.getLongitude();
				while ( westBoundsLon < 0.0 ) {
					westBoundsLon = westBoundsLon + 360.;
				}
				while (eastBoundsLon < 0.0 ) {
					eastBoundsLon = eastBoundsLon + 360.;
				}
				if ( markerLon >= eastBoundsLon ) {
					markerLon = eastBoundsLon;
				}
				if ( markerLon <= westBoundsLon ) {
					markerLon = westBoundsLon;
				}
				if (markerLat <= southBoundsLat ) {
					markerLat = southBoundsLat;
				}
				if ( markerLat >= northBoundsLat ) {
					markerLat = northBoundsLat;
				}
				sw = LatLng.newInstance(sw.getLatitude(), markerLon);
				ne = LatLng.newInstance(markerLat, ne.getLongitude());
		    } else if ( title.equals("nw_ne") ) { 
				southBoundsLat = sw.getLatitude();
				northBoundsLat = dataBounds.getNorthEast().getLatitude();
				markerLon = polygon.getBounds().getCenter().getLongitude();
				if (markerLat <= southBoundsLat ) {
					markerLat = southBoundsLat;
				}
				if ( markerLat >= northBoundsLat ) {
					markerLat = northBoundsLat;
				}
				ne = LatLng.newInstance(markerLat, ne.getLongitude());
			} else if ( title.equals("ne") ) {
				northBoundsLat = dataBounds.getNorthEast().getLatitude();
				southBoundsLat = sw.getLatitude();
				westBoundsLon = sw.getLongitude();
				eastBoundsLon = dataBounds.getNorthEast().getLongitude();
				while ( westBoundsLon < 0.0 ) {
					westBoundsLon = westBoundsLon + 360.;
				}
				while (eastBoundsLon < 0.0 ) {
					eastBoundsLon = eastBoundsLon + 360.;
				}
				if ( markerLon >= eastBoundsLon ) {
					markerLon = eastBoundsLon;
				}
				if ( markerLon <= westBoundsLon ) {
					markerLon = westBoundsLon;
				}
				if (markerLat <= southBoundsLat ) {
					markerLat = southBoundsLat;
				}
				if ( markerLat >= northBoundsLat ) {
					markerLat = northBoundsLat;
				}
				ne = LatLng.newInstance(markerLat, markerLon);
			} else if ( title.equals("ne_se") ) {
				markerLat = polygon.getBounds().getCenter().getLatitude();
				westBoundsLon = sw.getLongitude();
				eastBoundsLon = dataBounds.getNorthEast().getLongitude();
				while ( westBoundsLon < 0.0 ) {
					westBoundsLon = westBoundsLon + 360.;
				}
				while (eastBoundsLon < 0.0 ) {
					eastBoundsLon = eastBoundsLon + 360.;
				}
				if ( markerLon >= eastBoundsLon ) {
					markerLon = eastBoundsLon;
				}
				if ( markerLon <= westBoundsLon ) {
					markerLon = westBoundsLon;
				}
				ne = LatLng.newInstance(ne.getLatitude(), markerLon);
			} else if ( title.equals("se") ) {
				northBoundsLat = ne.getLatitude();
				southBoundsLat = dataBounds.getSouthWest().getLatitude();
				westBoundsLon = sw.getLongitude();
				eastBoundsLon = dataBounds.getNorthEast().getLongitude();
				while ( westBoundsLon < 0.0 ) {
					westBoundsLon = westBoundsLon + 360.;
				}
				while (eastBoundsLon < 0.0 ) {
					eastBoundsLon = eastBoundsLon + 360.;
				}
				if ( markerLon >= eastBoundsLon ) {
					markerLon = eastBoundsLon;
				}
				if ( markerLon <= westBoundsLon ) {
					markerLon = westBoundsLon;
				}
				if (markerLat <= southBoundsLat ) {
					markerLat = southBoundsLat;
				}
				if ( markerLat >= northBoundsLat ) {
					markerLat = northBoundsLat;
				}
				ne = LatLng.newInstance(ne.getLatitude(), markerLon);
				sw = LatLng.newInstance(markerLat, sw.getLongitude());
			} else if ( title.equals("sw_se") ) {
				southBoundsLat = dataBounds.getSouthWest().getLatitude();
				northBoundsLat = ne.getLatitude();
				markerLon = polygon.getBounds().getCenter().getLongitude();
				if (markerLat <= southBoundsLat ) {
					markerLat = southBoundsLat;
				}
				if ( markerLat >= northBoundsLat ) {
					markerLat = northBoundsLat;
				}
				sw = LatLng.newInstance(markerLat, sw.getLongitude());
			} else if ( title.equals("center") ) {
				westBoundsLon = dataBounds.getSouthWest().getLongitude() + polygon.getBounds().toSpan().getLongitude()/2.;
				eastBoundsLon = dataBounds.getNorthEast().getLongitude() - polygon.getBounds().toSpan().getLongitude()/2.;
				southBoundsLat = dataBounds.getSouthWest().getLatitude() + polygon.getBounds().toSpan().getLatitude()/2.;
				northBoundsLat = dataBounds.getNorthEast().getLatitude() - polygon.getBounds().toSpan().getLatitude()/2.;
				
				while ( westBoundsLon < 0.0 ) {
					westBoundsLon = westBoundsLon + 360.;
				}
				while (eastBoundsLon < 0.0 ) {
					eastBoundsLon = eastBoundsLon + 360.;
				}
				if ( markerLat >= northBoundsLat ) {
					markerLat = northBoundsLat;
				}
				if ( markerLat <= southBoundsLat ) {
					markerLat = southBoundsLat;
				}
				if ( markerLon >= eastBoundsLon ) {
					markerLon = eastBoundsLon;
				}
				if ( markerLon <= westBoundsLon ) {
					markerLon = westBoundsLon;
				}
				sw = LatLng.newInstance(markerLat - polygon.getBounds().toSpan().getLatitude()/2., markerLon - polygon.getBounds().toSpan().getLongitude()/2.0);
				ne = LatLng.newInstance(markerLat + polygon.getBounds().toSpan().getLatitude()/2., markerLon + polygon.getBounds().toSpan().getLongitude()/2.0);
			}

			mMap.removeOverlay(polygon);
			polygonPoints[0] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
			polygonPoints[1] = LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
			polygonPoints[2] = LatLng.newInstance(ne.getLatitude(), ne.getLongitude());
			polygonPoints[3] = LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
			polygonPoints[4] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
			polygon = new Polygon(polygonPoints, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity);
			mMap.addOverlay(polygon);
			LatLngBounds rectBounds = polygon.getBounds();
			swMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), sw.getLongitude()));
			sw_nwMarker.setLatLng(LatLng.newInstance(rectBounds.getCenter().getLatitude(), sw.getLongitude()));
			nwMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), sw.getLongitude()));
			nw_neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), rectBounds.getCenter().getLongitude()));
			neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), ne.getLongitude()));
			ne_seMarker.setLatLng(LatLng.newInstance(rectBounds.getCenter().getLatitude(), ne.getLongitude()));
			seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), ne.getLongitude()));
			sw_seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), rectBounds.getCenter().getLongitude()));
			centerMarker.setLatLng(rectBounds.getCenter());
		}
	};
	public void setVisible(boolean visible) {
		polygon.setVisible(visible);		
	}
	/**
	 * @return the polygon
	 */
	public Polygon getPolygon() {
		return polygon;
	}
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
	
	public void update(LatLng position) {
		// Figure out where the drag marker is relative to where the click was made.
		double posLon = position.getLongitude();
		double posLat = position.getLatitude();
		double clickLon = click.getLongitude();
		double clickLat = click.getLatitude();
		while ( posLon <= 360. ) {
			posLon = posLon + 360.;
		}
		while ( clickLon <= 360. ) {
			clickLon = clickLon + 360.;
		}
		LatLng sw;
		LatLng ne;
		if ( posLat > clickLat && posLon > clickLon ) {
			ne = position;
			sw = click;
		} else if ( posLat <= clickLat && posLon <= clickLon ) {
			ne = click;
			sw = position;
		} else if ( posLat > clickLat && posLon <= clickLon ) {
			ne = LatLng.newInstance(posLat, clickLon);
			sw = LatLng.newInstance(clickLat, posLon);
		} else {
			ne = LatLng.newInstance(clickLat, posLon);
			sw = LatLng.newInstance(posLat, clickLon);
		}
		
		polygonPoints[0] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
		polygonPoints[1] = LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
		polygonPoints[2] = LatLng.newInstance(ne.getLatitude(), ne.getLongitude());
		polygonPoints[3] = LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
		polygonPoints[4] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
		polygon = new Polygon(polygonPoints, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity);
		LatLngBounds bounds = polygon.getBounds();
		LatLng center = bounds.getCenter();
		swMarker.setLatLng(sw);
		sw_nwMarker.setLatLng(LatLng.newInstance(center.getLatitude(), sw.getLongitude()));
		nwMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), sw.getLongitude()));
		nw_neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), center.getLongitude()));
		neMarker.setLatLng(ne);
		ne_seMarker.setLatLng(LatLng.newInstance(center.getLatitude(), ne.getLongitude()));
		seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), ne.getLongitude()));
		sw_seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), center.getLongitude()));
		centerMarker.setLatLng(center);
	}
	/**
	 * @param click the click to set
	 */
	public void setClick(LatLng click) {
		this.click = click;
	}
}
