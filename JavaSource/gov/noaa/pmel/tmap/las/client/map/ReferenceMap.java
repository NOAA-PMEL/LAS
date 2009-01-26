package gov.noaa.pmel.tmap.las.client.map;

import gov.noaa.pmel.tmap.las.client.GridSerializable;

import com.google.gwt.maps.client.MapType;
import com.google.gwt.maps.client.MapTypeOptions;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.Control;
import com.google.gwt.maps.client.control.ControlAnchor;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
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

public class ReferenceMap extends Composite {
	MaskOverlay maskOverlay = null;
	DataBoundsOverlay dataBoundsOverlay = null;
	SelectControl selectControl;
	ResetControl resetControl;
	RotateControl rotateControl;
	IconifyControl iconifyControl;
	SmallMapControl smallMapControl = null;
	LargeMapControl largeMapControl = null;
	MapTypeControl mapTypeControl;
	MapWidget mMap;
	
	LatLng mCenter;
	LatLngBounds dataBounds = LatLngBounds.newInstance(LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0));
	
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
	double delta;
	boolean haveData = false;
	boolean modulo = false;
	
	String gridID = null;
	
	
	/**
	 * Construct an LASReference map centered and zoomed with the width and height specified in pixels.
	 * @param center
	 * @param zoom
	 * @param width
	 * @param height
	 */
	public ReferenceMap (LatLng center, int zoom, int width, int height) {

		mCenter = center;
		mZoom = zoom;
		mMap = new MapWidget(mCenter, mZoom);    	
		mMap.addMapZoomEndHandler(new MapZoomEndHandler() {

			public void onZoomEnd(MapZoomEndEvent event) {
				int zoom_factor = (event.getNewZoomLevel() - mZoom) + 1;
				double denominator = Double.valueOf(zoom_factor).doubleValue()*2.0;
				current_margin_x = margin_x/denominator;
				current_margin_y = margin_y/denominator;
			}
		});
		mWidth = width;
		mHeight = height;
		mMap.setSize(String.valueOf(mWidth)+"px", String.valueOf(mHeight)+"px");
		selectControl = new SelectControl(new ControlPosition(ControlAnchor.TOP_RIGHT, 10 ,60), this);
		if ( mHeight > 350 ) {
			largeMapControl = new LargeMapControl();
			mMap.addControl(largeMapControl);
		} else {
			smallMapControl = new SmallMapControl();
			mMap.addControl(smallMapControl);
		}
		mapTypeControl = new MapTypeControl();
		mMap.addControl(mapTypeControl);
		resetControl = new ResetControl(new ControlPosition(ControlAnchor.TOP_RIGHT, 35 ,30), LatLng.newInstance(0.0, 0.0), 1);
		mMap.addControl(resetControl);
		resetControl.addClickListener(click);
		rotateControl = new RotateControl(new ControlPosition(ControlAnchor.TOP_RIGHT, 10 ,290));
		mMap.addControl(rotateControl);
		rotateControl.setVisible(false);
		mMap.addMapDragEndHandler(new MapDragEndHandler() {
			public void onDragEnd(MapDragEndEvent event) {
				if ( selectControl.getSelectionBounds().containsLatLng(mMap.getCenter()) ) {
				    selectControl.setSelectionCenter(mMap.getCenter());
				}
			}		
		});		
		addControl(selectControl);		
		iconifyControl = new IconifyControl(new ControlPosition(ControlAnchor.TOP_RIGHT, 10, 30), this);
		addControl(iconifyControl);
		initWidget(mMap);
    }
    public void addControl(Control control) {
    	mMap.addControl(control);
    }
    public void setDataBounds(LatLngBounds dataBounds, double delta, boolean selection) {
    	modulo = false;
    	this.dataBounds = dataBounds;
    	double lon_span = dataBounds.toSpan().getLongitude();
		if ( dataBounds.isFullLongitude() || lon_span + 2.*delta >= 360.0 ) {
			modulo = true;
		}
    	this.delta = delta;
    	// Set data selection rectangle to the data bounds
    	if ( selection ) {
    		if ( dataBoundsOverlay != null ) {
    			mMap.removeOverlay(dataBoundsOverlay.getPolygon());
    		}
    		dataBoundsOverlay = new DataBoundsOverlay(dataBounds);
    		mMap.addOverlay(dataBoundsOverlay.getPolygon());
    		selectControl.initSelectionBounds(dataBounds, dataBounds, dataBounds.getCenter());
    		selectControl.setEditingEnabled(true);
    		int zoom = mMap.getBoundsZoomLevel(dataBounds);
    		mMap.setZoomLevel(zoom);
    		mMap.setCenter(dataBounds.getCenter());
    		resetControl.setSelectionBounds(dataBounds);  	  		
    	}
    	if ( modulo ) {
			rotateControl.setVisible(true);
		} else {
			rotateControl.setVisible(false);
		}
    }
	
	ClickListener click = new ClickListener() {
		public void onClick(Widget sender) {
			if ( dataBounds == null ) {
				Window.alert("Please select a data set and variable.");
			} else {
				for ( int i = 0; i < 3; i++) {
					reset();
				}
			}
		}
	};
	ClickListener rotate = new ClickListener() {

		public void onClick(Widget sender) {
			if ( sender.getTitle().equals("Rotate west") ) {
				rotateWest();				
			} else if ( sender.getTitle().equals("Rotate east") ) {
				rotateEast();
			}	
		}
	};
	public void rotateWest() {
		LatLng c_ne = dataBounds.getNorthEast();
		double east = c_ne.getLongitude() - 90.;
		LatLng c_sw = dataBounds.getSouthWest();
		double west = c_sw.getLongitude() - 90.;
		dataBounds = LatLngBounds.newInstance(LatLng.newInstance(c_sw.getLatitude(), west), LatLng.newInstance(c_ne.getLatitude(), east));
		setDataBounds(dataBounds, delta, true);
	}
	public void rotateEast() {
		LatLng c_ne = dataBounds.getNorthEast();
		double east = c_ne.getLongitude() + 90.;
		LatLng c_sw = dataBounds.getSouthWest();
		double west = c_sw.getLongitude() + 90.;
		dataBounds = LatLngBounds.newInstance(LatLng.newInstance(c_sw.getLatitude(), west), LatLng.newInstance(c_ne.getLatitude(), east));
		setDataBounds(dataBounds, delta, true);
	}
	private void reset() {
		int zoom = mMap.getBoundsZoomLevel(dataBounds);
		mMap.setZoomLevel(zoom);
		mMap.setCenter(dataBounds.getCenter());
	}
	public boolean isModulo() {
		return modulo;
	}
	public void setSelectionBounds(LatLngBounds bounds, boolean recenter) {
		selectControl.setSelectionBounds(bounds, recenter);
	}
	
	public LatLngBounds getDataBounds() {
		return dataBounds;
	}
	public int getBoundsZoomLevel(LatLngBounds region) {
		return mMap.getBoundsZoomLevel(region);
	}
	public void setZoom(int zoom) {
		mMap.setZoomLevel(zoom);
	}
	public MapWidget getMapWidget() {
		return mMap;
	}
	public void setCenter(LatLng center) {
		mMap.setCenter(center);
	}
	public void hideControls() {
		if ( selectControl != null ) {
			selectControl.setVisible(false);
		}
		if ( resetControl != null ) {
			resetControl.setVisible(false);
		}
		if ( rotateControl != null ) {
			rotateControl.setVisible(false);
		}
		if ( largeMapControl != null ) {
			mMap.removeControl(largeMapControl);
		}
		if ( smallMapControl != null ) {
			mMap.removeControl(smallMapControl);
		}		
		if ( mapTypeControl != null ) {
			mMap.removeControl(mapTypeControl);
		}
	}
	public void showControls() {
		if ( selectControl != null ) {
			selectControl.setVisible(true);
		}
		if ( resetControl != null ) {
			resetControl.setVisible(true);
		}
		if ( rotateControl != null ) {
			rotateControl.setVisible(true);
		}
		if ( largeMapControl != null ) {
			mMap.addControl(largeMapControl);
		}
		if ( smallMapControl != null ) {
			mMap.addControl(smallMapControl);
		}		
		if ( mapTypeControl != null ) {
			mMap.addControl(mapTypeControl);
		}
	}
	public void resetSize() {
		mMap.setSize(String.valueOf(mWidth)+"px", String.valueOf(mHeight)+"px");		
	}
	public String getXlo() {
		double xwest = selectControl.getSelectionBounds().getSouthWest().getLongitude();
		if ( xwest < 0.0 ) {
			xwest = xwest + 360.;
		}
		return String.valueOf(xwest);
	}
	public String getXhi() {
		double xwest = selectControl.getSelectionBounds().getSouthWest().getLongitude();
		double xeast = selectControl.getSelectionBounds().getNorthEast().getLongitude();
		if ( xeast <= 0.0 || xeast < xwest ) {
			xeast = xeast + 360.;
		}
		return String.valueOf(xeast);
	}
	public String getYlo() {
		return String.valueOf(selectControl.getSelectionBounds().getSouthWest().getLatitude());
	}
	public String getYhi() {
		return String.valueOf(selectControl.getSelectionBounds().getNorthEast().getLatitude());
	}
	
}
