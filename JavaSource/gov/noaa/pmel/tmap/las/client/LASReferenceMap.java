package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.maps.client.MapType;
import com.google.gwt.maps.client.MapTypeOptions;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.Control;
import com.google.gwt.maps.client.control.ControlAnchor;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.MenuMapTypeControl;
import com.google.gwt.maps.client.control.SmallMapControl;
import com.google.gwt.maps.client.event.MapDragEndHandler;
import com.google.gwt.maps.client.event.MapDragHandler;
import com.google.gwt.maps.client.event.MapDragStartHandler;
import com.google.gwt.maps.client.event.MapZoomEndHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LASReferenceMap extends Composite {
	MaskOverlay maskOverlay = null;
	SelectControl selectControl;
	ResetControl resetControl;
	MapWidget mMap;
	LatLng mSouthWestCorner;
	LatLng mNorthEastCorner;
	LatLng mCenter;
	LatLngBounds dataBounds;
	int mZoom;
	int mWidth;   // Width in pixels 
	int mHeight;  // Height in pixels
	double current_margin_x;
	double current_margin_y;
	double margin_x;
	double margin_y;
	LatLng initial_map_center;
	double south_center;
	double north_center;
	double west_center;
	double east_center;
	boolean set_south_center = true;
	boolean set_north_center = true;
	boolean set_west_center = true;
	boolean set_east_center = true;
	String gridID = null;
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
    	
    	mMap.addMapZoomEndHandler(new MapZoomEndHandler() {

			public void onZoomEnd(MapZoomEndEvent event) {
				int zoom_factor = (event.getNewZoomLevel() - mZoom) + 1;
				double denominator = Double.valueOf(zoom_factor).doubleValue()*2.0;
				current_margin_x = margin_x/denominator;
				current_margin_y = margin_y/denominator;
				set_south_center = true;
				set_north_center = true;
				set_east_center = true;
				set_west_center = true;
				mMap.setDraggable(true);
				
				// If back at original zoom, center the map and turn off dragging.
				if ( event.getNewZoomLevel() == mZoom ) {
					mMap.setDraggable(false);
					//mMap.setCenter(initial_map_center);
				}
			}
    	});
    	mMap.addMapDragStartHandler(new MapDragStartHandler() {

			public void onDragStart(MapDragStartEvent event) {
				
			}
    		
    	});
    	mMap.addMapDragEndHandler(new MapDragEndHandler() {

			public void onDragEnd(MapDragEndEvent event) {
				
				if ( maskOverlay != null ) {
				    mMap.removeOverlay(maskOverlay);
				}
				LatLngBounds mapBounds = mMap.getBounds();
				if ( !dataBounds.containsBounds(mapBounds) ) {
					maskOverlay = new MaskOverlay(mapBounds, dataBounds, "#666666", 1, 0.75, "#666666", 0.75);
					mMap.addOverlay(maskOverlay);
				}
				
			}
    	});
    	
    	mMap.addMapDragHandler(new MapDragHandler(){
            /**
             * As the map drags, only allow it to move so that no more than one third of the visible data area becomes grey.
             * 
             * This is done by checking the distance of the data area from the corner of the map.
             */
			public void onDrag(MapDragEvent event) {
				
				LatLngBounds mapBounds = mMap.getBounds();
		        double nlat = mapBounds.getCenter().getLatitude();
		        double nlon = mapBounds.getCenter().getLongitude();
				LatLng sw = dataBounds.getSouthWest();
				double data_south = sw.getLatitude();
				double data_west = sw.getLongitude();
				while ( data_west <= 0.0 ) {
					data_west = data_west + 360.;
				}
				
				LatLng ne = dataBounds.getNorthEast();
				double data_north = ne.getLatitude();
				double data_east = ne.getLongitude();
				while ( data_east <= 0.0 ) {
					data_east = data_east + 360.;
				}
				
				LatLng map_sw = mapBounds.getSouthWest();
				double map_south = map_sw.getLatitude();
				double map_west = map_sw.getLongitude();
				while( map_west <= 0.0 ) {
					map_west = map_west + 360.;
				}
				
				LatLng map_ne = mapBounds.getNorthEast();
				double map_north = map_ne.getLatitude();
				double map_east = map_ne.getLongitude();
				while ( map_east <= 0.0 ) {
					map_east = map_east + 360.;
				}
				
				boolean recenter = false;;
				if ( data_south < map_south ) {
					
					// Top of the data area is below the map.  Limit how far it goes down.
					if ( data_north < map_north && (Math.abs(map_north - data_north) > current_margin_y) ) {
					    recenter = true;
					    if ( set_north_center ) {
					        north_center = nlat;
					        set_north_center = false;
					    }
					    nlat = north_center;
					}
				} else if ( data_north > map_north ) {
					
					// The bottom of the data area is above the map.  Limit how far it goes up.
					if ( data_south > map_south && (Math.abs(data_south - map_south) > current_margin_y) ) {
						recenter = true;
						if ( set_south_center ) {
							south_center = nlat;
							set_south_center = false;
						}
						nlat = south_center;
					}
				} else {
					// The entire data area is visible on the map.  Do nothing for now.
					
				}
				
				
				if ( data_east > map_east ) {
					
					// The western grey boundary is growing.  Limit its growth.
					if ( data_west > map_west && (Math.abs(data_west - map_west) > current_margin_x) ) {
						recenter = true;
						if ( set_west_center ) {
							west_center = nlon;
							set_west_center = false;
						}
						nlon = west_center;
					}
				} else if ( data_west < map_west ) {
					
					if ( data_east < map_east && (Math.abs(map_east - data_east) > current_margin_x ) ) {
						recenter = true;
						if ( set_east_center ) {
							east_center = nlon;
							set_east_center = false;
						}
						nlon = east_center;
					}
				} else {
					// Entire data area is visible.  Do nothing for now.
					
				}
				
				if ( recenter ) {
					LatLng center = LatLng.newInstance(nlat, nlon);
					mMap.setCenter(center);
				}
				
				
			}
    		
    	});
    	mWidth = width;
    	mHeight = height;
		mMap.setSize(String.valueOf(mWidth)+"px", String.valueOf(mHeight)+"px");
		selectControl = new SelectControl(new ControlPosition(ControlAnchor.TOP_RIGHT, 10 ,60));
		if ( mHeight > 350 ) {
		    mMap.addControl(new LargeMapControl());
		} else {
			 mMap.addControl(new SmallMapControl());
		}
		mMap.addControl(new MapTypeControl());
		resetControl = new ResetControl(new ControlPosition(ControlAnchor.TOP_RIGHT, 10 ,30), LatLng.newInstance(0.0, 0.0), 1);
		mMap.addControl(resetControl);
		resetControl.addClickListener(click);
		addControl(selectControl);
		initWidget(mMap);
    }
    public void addControl(Control control) {
    	mMap.addControl(control);
    }
	public void zoomToGrid(GridSerializable grid) {
		gridID = grid.getID();
		if (maskOverlay != null ) {
        	mMap.removeOverlay(maskOverlay);
        }
		selectControl.clearOverlays();
		selectControl.setGridID(grid.getID());
		/*
		 * if a rectangle has a line along a latitude that crosses the
		 * anti-meridian, you have to draw the rectangle in two pieces
		 */
		double grid_west = Double.valueOf(grid.getXAxis().getLo());
		double grid_east = Double.valueOf(grid.getXAxis().getHi());
		
		double grid_south = Double.valueOf(grid.getYAxis().getLo());
		double grid_north = Double.valueOf(grid.getYAxis().getHi());
		
		LatLng sw = LatLng.newInstance(grid_south, grid_west);
		LatLng ne = LatLng.newInstance(grid_north, grid_east);
		dataBounds = LatLngBounds.newInstance(sw, ne);
		selectControl.setDataBounds(dataBounds);
		int zoom = mMap.getBoundsZoomLevel(dataBounds);


		mMap.setZoomLevel(zoom);
		mMap.setCenter(dataBounds.getCenter());
		resetControl.setCenter(dataBounds.getCenter());
		resetControl.setZoom(zoom);
		resetControl.setGridID(grid.getID());

		MapType[] types = mMap.getMapTypes();
		for (int i = 0; i < types.length; i++) {
			MapTypeOptions options = new MapTypeOptions();
			options.setMinResolution(zoom);
			MapType mapType = new MapType(types[i].getTileLayers(), types[i].getProjection(), types[i].getName(false), options);
			mMap.removeMapType(types[i]);
			mMap.addMapType(mapType);
		}
		LatLngBounds mapBounds = mMap.getBounds();

		maskOverlay = new MaskOverlay(mapBounds, dataBounds, "#666666", 1, 0.75, "#666666", 0.75);
		mMap.addOverlay(maskOverlay);
		mMap.setDraggable(false);

		LatLng map_sw = mMap.getBounds().getSouthWest();
		double map_south = map_sw.getLatitude();
		double map_west = sw.getLongitude();
		while( map_west <= 0.0 ) {
			map_west = map_west + 360.;
		}
		LatLng map_ne = mMap.getBounds().getNorthEast();
		double map_north = map_ne.getLatitude();
		double map_east = map_ne.getLongitude();
		while ( map_east <= 0.0 ) {
			map_east = map_east + 360.;
		}
		
		margin_y = (map_north - map_south)/4.;
		margin_x = (map_east - map_west)/4.;
		
		initial_map_center = mMap.getCenter();
		
		// For some reason the map does not get set correctly for some data sets.
		// This hack forces it to reconsider...
		for ( int i = 0; i < 3; i++) {
		    reset();
		}
		
	}
	ClickListener click = new ClickListener() {
		public void onClick(Widget sender) {
			if ( gridID == null ) {
				Window.alert("Please select a data set and variable.");
			} else {
				for ( int i = 0; i < 3; i++) {
					reset();
				}
			}
		}
	};
	private void reset() {
		int zoom = mMap.getBoundsZoomLevel(dataBounds);
		if ( maskOverlay != null ) {
			mMap.removeOverlay(maskOverlay);
		}
		maskOverlay = new MaskOverlay(mMap.getBounds(), dataBounds, "#666666", 1, 0.75, "#666666", 0.75);
		mMap.addOverlay(maskOverlay);
		mMap.setDraggable(false);
		mMap.setZoomLevel(zoom);
		mMap.setCenter(dataBounds.getCenter());
		mMap.setDraggable(false);
	}
}
