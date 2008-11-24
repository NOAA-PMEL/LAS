package gov.noaa.pmel.tmap.las.client;

import java.util.ArrayList;
import java.util.Iterator;

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
	Marker se_swMarker;
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
			sw_icon.setImageURL("http://localhost:8880/baker/images/edit_square.png");
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
			sw_nw_icon.setImageURL("http://localhost:8880/baker/images/edit_square.png");
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
			nw_icon.setImageURL("http://localhost:8880/baker/images/edit_square.png");
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
			nw_ne_icon.setImageURL("http://localhost:8880/baker/images/edit_square.png");
			MarkerOptions nw_ne_options = MarkerOptions.newInstance();
			nw_ne_options.setIcon(nw_ne_icon);
			nw_ne_options.setDraggable(true);
			nw_ne_options.setDragCrossMove(true);
			nw_ne_options.setBouncy(false);
			nw_ne_options.setTitle("nw_ne");
			nw_neMarker = new Marker(LatLng.newInstance(currentBounds.getNorthEast().getLatitude(), center.getLongitude()), nw_ne_options);
			markers.add(nw_neMarker);
			
			Icon ne_icon = Icon.newInstance();
			ne_icon.setIconSize(Size.newInstance(12, 12));
			ne_icon.setIconAnchor(Point.newInstance(5, 5));
			ne_icon.setImageURL("http://localhost:8880/baker/images/edit_square.png");
			MarkerOptions ne_options = MarkerOptions.newInstance();
			ne_options.setIcon(ne_icon);
			ne_options.setDraggable(true);
			ne_options.setDragCrossMove(true);
			ne_options.setBouncy(false);
			ne_options.setTitle("ne");
			neMarker = new Marker(currentBounds.getNorthEast(), ne_options);
			markers.add(neMarker);
			
			Icon ne_se_icon = Icon.newInstance();
			ne_se_icon.setIconSize(Size.newInstance(12, 12));
			ne_se_icon.setIconAnchor(Point.newInstance(5, 5));
			ne_se_icon.setImageURL("http://localhost:8880/baker/images/edit_square.png");
			MarkerOptions ne_se_options = MarkerOptions.newInstance();
			ne_se_options.setIcon(ne_se_icon);
			ne_se_options.setDraggable(true);
			ne_se_options.setDragCrossMove(true);
			ne_se_options.setBouncy(false);
			ne_se_options.setTitle("ne_se");
			ne_seMarker = new Marker(LatLng.newInstance(center.getLatitude(), currentBounds.getNorthEast().getLongitude()), ne_se_options);
			markers.add(ne_seMarker);
			
			Icon se_icon = Icon.newInstance();
			se_icon.setIconSize(Size.newInstance(12, 12));
			se_icon.setIconAnchor(Point.newInstance(5, 5));
			se_icon.setImageURL("http://localhost:8880/baker/images/edit_square.png");
			MarkerOptions se_options = MarkerOptions.newInstance();
			se_options.setIcon(se_icon);
			se_options.setDraggable(true);
			se_options.setDragCrossMove(true);
			se_options.setBouncy(false);
			se_options.setTitle("se");
			seMarker = new Marker(LatLng.newInstance(currentBounds.getSouthWest().getLatitude(), currentBounds.getNorthEast().getLongitude()), se_options);
			markers.add(seMarker);
			
			Icon se_sw_icon = Icon.newInstance();
			se_sw_icon.setIconSize(Size.newInstance(12, 12));
			se_sw_icon.setIconAnchor(Point.newInstance(5, 5));
			se_sw_icon.setImageURL("http://localhost:8880/baker/images/edit_square.png");
			MarkerOptions se_sw_options = MarkerOptions.newInstance();
			se_sw_options.setIcon(se_sw_icon);
			se_sw_options.setDraggable(true);
			se_sw_options.setDragCrossMove(true);
			se_sw_options.setBouncy(false);
			se_sw_options.setTitle("se_sw");
			se_swMarker = new Marker(LatLng.newInstance(currentBounds.getSouthWest().getLatitude(), center.getLongitude()), se_sw_options);
			markers.add(se_swMarker);
			
			Icon center_icon = Icon.newInstance();
			center_icon.setIconSize(Size.newInstance(12, 12));
			center_icon.setIconAnchor(Point.newInstance(5, 5));
			center_icon.setImageURL("http://localhost:8880/baker/images/edit_square.png");
			MarkerOptions center_options = MarkerOptions.newInstance();
			center_options.setIcon(center_icon);
			center_options.setDraggable(true);
			center_options.setDragCrossMove(true);
			center_options.setBouncy(false);
			center_options.setTitle("center");
			centerMarker = new Marker(currentBounds.getCenter(), center_options);
			markers.add(centerMarker);
		}
		
	}
	MarkerDragHandler markerDragHandler = new MarkerDragHandler() {
		public void onDrag(MarkerDragEvent event) {
			Marker marker = event.getSender();
			LatLng markerLocation = marker.getLatLng();
			double markerLon = markerLocation.getLongitude();
			while ( markerLon < 0.0 ) {
				markerLon = markerLon + 360.;
			}
			double westBoundsLon = dataBounds.getSouthWest().getLongitude();
			while ( westBoundsLon < 0.0 ) {
				westBoundsLon = westBoundsLon + 360.;
			}
			double eastBoundsLon = dataBounds.getNorthEast().getLongitude();
			while (eastBoundsLon < 0.0 ) {
				eastBoundsLon = eastBoundsLon + 360.;
			}
			double resetLon = markerLon;
			boolean reset = false;
			double markerLat = markerLocation.getLatitude();
			double southBoundsLat = dataBounds.getSouthWest().getLatitude();
			double northBoundsLat = dataBounds.getNorthEast().getLatitude();
			double resetLat = markerLat;
			LatLng sw = null;
			LatLng ne = null;
             String title = marker.getTitle();
			
             if ( title.equals("sw") ) {
            	 if ( !dataBounds.containsLatLng(markerLocation) )  {
            		 if ( markerLon <= westBoundsLon ) {
            			 reset = true;
            			 resetLon = westBoundsLon;
            		 }
            		 if ( markerLat <= southBoundsLat ) {
            			 reset = true;
            			 resetLat = southBoundsLat;
            		 }
            		 if ( markerLon > eastBoundsLon ) {
            			 reset = true;
            			 resetLon = eastBoundsLon;
            		 }
            		 if ( markerLat > northBoundsLat ) {
            			 reset = true;
            			 resetLat = northBoundsLat;
            		 }
            		 if (reset) {
            			 marker.setLatLng(LatLng.newInstance(resetLat, resetLon));
            		 }
            	 }

            	 // This was the SW corner.  Is it still?
            	 double old_ne_lat = polygonPoints[2].getLatitude();
            	 double old_ne_lon = polygonPoints[2].getLongitude();
            	 while ( old_ne_lon <= 0. ) {
            		 old_ne_lon = old_ne_lon + 360.;
            	 }
            	 if ( resetLat > old_ne_lat && resetLon > old_ne_lon ) {
            		 ne = LatLng.newInstance(resetLat, resetLon);
            		 sw = LatLng.newInstance(old_ne_lat, old_ne_lon);
            	 } else if ( resetLat <= old_ne_lat && resetLon <= old_ne_lon ) {
            		 ne = LatLng.newInstance(old_ne_lat, old_ne_lon);
            		 sw = LatLng.newInstance(resetLat, resetLon);
            	 } else if ( resetLat > old_ne_lat && resetLon <= old_ne_lon ) {
            		 ne = LatLng.newInstance(resetLat, old_ne_lon);
            		 sw = LatLng.newInstance(old_ne_lat, resetLon);
            	 } else {
            		 ne = LatLng.newInstance(old_ne_lat, resetLon);
            		 sw = LatLng.newInstance(resetLat, old_ne_lon);
            	 }

             } else if (title.equals("sw_nw") ) {
            	 reset = true;
            	 resetLat = polygon.getBounds().getCenter().getLatitude();
            	 if ( markerLon <= westBoundsLon ) {
        			 reset = true;
        			 resetLon = westBoundsLon;
        		 }
            	 if ( markerLon > eastBoundsLon ) {
        			 reset = true;
        			 resetLon = eastBoundsLon;
        		 }
            	 if ( reset ) {
            		 marker.setLatLng(LatLng.newInstance(resetLat, resetLon));
            	 }
            	 
            	 double old_ne_lon = polygonPoints[2].getLongitude();
            	 while ( old_ne_lon <= 0. ) {
            		 old_ne_lon = old_ne_lon + 360.;
            	 }
            	 
            	 double old_sw_lon = polygonPoints[0].getLongitude();
            	 while ( old_sw_lon <= 0.0 ) {
            		 old_sw_lon = old_sw_lon + 360.;
            	 }
            	 if ( resetLon <= old_ne_lon ) {
            		 ne = polygonPoints[2];
            		 sw = LatLng.newInstance(polygonPoints[0].getLatitude(), resetLon);
            	 } else {
            		 ne = LatLng.newInstance(polygonPoints[1].getLatitude(), resetLon);
            		 sw = polygonPoints[3];
            	 }
             } else if ( title.equals("nw") ) {
            	 if ( !dataBounds.containsLatLng(markerLocation) )  {
            		 if ( markerLon <= westBoundsLon ) {
            			 reset = true;
            			 resetLon = westBoundsLon;
            		 }
            		 if ( markerLat <= southBoundsLat ) {
            			 reset = true;
            			 resetLat = southBoundsLat;
            		 }
            		 if ( markerLon > eastBoundsLon ) {
            			 reset = true;
            			 resetLon = eastBoundsLon;
            		 }
            		 if ( markerLat > northBoundsLat ) {
            			 reset = true;
            			 resetLat = northBoundsLat;
            		 }
            		 if (reset) {
            			 marker.setLatLng(LatLng.newInstance(resetLat, resetLon));
            		 }
            	 }
            	 LatLng old_sw = polygonPoints[0];
            	 LatLng old_ne = polygonPoints[2];
            	 
            	 double old_se_lat = polygonPoints[3].getLatitude();
            	 double old_se_lon = polygonPoints[3].getLongitude();
            	 while ( old_se_lon <= 0. ) {
            		 old_se_lon = old_se_lon + 360.;
            	 }
            	 if ( resetLat <= old_se_lat && resetLon > old_se_lon ) {
            		 ne = old_sw;
            		 sw = LatLng.newInstance(resetLat, old_ne.getLongitude());
            	 } else if ( resetLat <= old_se_lat && resetLon <= old_se_lon ) {
            		 ne = LatLng.newInstance(old_se_lat, old_ne.getLongitude());
            		 sw = LatLng.newInstance(resetLat, resetLon);
            	 } else if ( resetLat > old_se_lat && resetLon <= old_se_lon ) {
            		 ne = LatLng.newInstance(resetLat, old_ne.getLongitude());
            		 sw = LatLng.newInstance(old_sw.getLatitude(), resetLon);
            	 } else {
            		 ne = LatLng.newInstance(resetLat, resetLon);
            		 sw = LatLng.newInstance(old_se_lat, old_se_lon);
            	 }
            	 
             } else if ( title.equals("nw_ne") ) {
            	 reset = true;
            	 resetLon = polygon.getBounds().getCenter().getLongitude();
            	 if ( markerLat <= southBoundsLat ) {
        			 reset = true;
        			 resetLat = southBoundsLat;
        		 }
            	 if ( markerLat > northBoundsLat ) {
        			 reset = true;
        			 resetLat = northBoundsLat;
        		 }
            	 if ( reset ) {
            		 marker.setLatLng(LatLng.newInstance(resetLat, resetLon));
            	 }
            	 
            	 double old_ne_lat = polygonPoints[2].getLatitude();
            	 double old_sw_lat = polygonPoints[0].getLatitude();
            	 
            	 // TODO FIX THIS.
            	 
            	 if ( resetLat <= old_sw_lat ) {
            		 //ne
            		 //sw = LatLng.newInstance(polygonPoints[0].getLatitude(), resetLon);
            	 } else {
            		 //ne = LatLng.newInstance(polygonPoints[1].getLatitude(), resetLon);
            		 //sw = polygonPoints[3];
            	 }
             }
			
			mMap.removeOverlay(polygon);
			polygonPoints[0] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
			polygonPoints[1] = LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
			polygonPoints[2] = LatLng.newInstance(ne.getLatitude(), ne.getLongitude());
			polygonPoints[3] = LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
			polygonPoints[4] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
			polygon = new Polygon(polygonPoints, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity);
			mMap.addOverlay(polygon);
			LatLngBounds dataBounds = polygon.getBounds();
			swMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), sw.getLongitude()));
			sw_nwMarker.setLatLng(LatLng.newInstance(dataBounds.getCenter().getLatitude(), sw.getLongitude()));
			nwMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), sw.getLongitude()));
			nw_neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), dataBounds.getCenter().getLongitude()));
			neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), ne.getLongitude()));
			ne_seMarker.setLatLng(LatLng.newInstance(dataBounds.getCenter().getLatitude(), ne.getLongitude()));
			seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), ne.getLongitude()));
			se_swMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), dataBounds.getCenter().getLongitude()));
			centerMarker.setLatLng(dataBounds.getCenter());
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
		se_swMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), center.getLongitude()));
		centerMarker.setLatLng(center);
	}
	/**
	 * @param click the click to set
	 */
	public void setClick(LatLng click) {
		this.click = click;
	}
}
