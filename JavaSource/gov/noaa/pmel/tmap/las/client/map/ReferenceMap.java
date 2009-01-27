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
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

public class ReferenceMap extends Composite {
	DockPanel panel;
	HorizontalPanel topControls;
	HorizontalPanel bottomControls;
	MaskOverlay maskOverlay = null;
	DataBoundsOverlay dataBoundsOverlay = null;
	SelectWidget selectWidget;
	LatLonWidget textWidget;
	ResetWidget resetWidget;
	RotateWidget rotateWidget;
	IconifyControl iconifyControl;
	SmallMapControl smallMapControl = null;
	LargeMapControl largeMapControl = null;
	MapTypeControl mapTypeControl;
	MapWidget mMap;
	RegionWidget regionWidget;
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
        panel = new DockPanel();
        topControls = new HorizontalPanel();
        topControls.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
        bottomControls = new HorizontalPanel();
        bottomControls.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
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
		selectWidget = new SelectWidget(this);
		textWidget = new LatLonWidget();
		selectWidget.setLatLngWidget(textWidget);
		if ( mHeight >9999 ) {
			largeMapControl = new LargeMapControl();
			mMap.addControl(largeMapControl);
		} else {
			smallMapControl = new SmallMapControl();
			mMap.addControl(smallMapControl);
		}
		mMap.setCurrentMapType(MapType.getHybridMap());
		regionWidget = new RegionWidget(this);
		resetWidget = new ResetWidget(this.mMap);
		rotateWidget = new RotateWidget();
		rotateWidget.addClickListener(rotate);
		
		rotateWidget.setVisible(false);
		mMap.addMapDragEndHandler(new MapDragEndHandler() {
			public void onDragEnd(MapDragEndEvent event) {
				if ( selectWidget.getSelectionBounds().containsLatLng(mMap.getCenter()) ) {
				    selectWidget.setSelectionCenter(mMap.getCenter());
				}
			}		
		});			
		iconifyControl = new IconifyControl(new ControlPosition(ControlAnchor.TOP_RIGHT, 10, 30), this);
		addControl(iconifyControl);
		topControls.add(regionWidget);
		topControls.add(selectWidget);
		topControls.add(resetWidget);
		topControls.add(rotateWidget);
		bottomControls.add(textWidget);
		panel.add(topControls, DockPanel.NORTH);
		panel.add(bottomControls, DockPanel.SOUTH);
		panel.add(mMap, DockPanel.CENTER);
		initWidget(panel);
    }
    public void addControl(Control control) {
    	mMap.addControl(control);
    }
    public void setDataBounds(LatLngBounds dataBounds, double delta, boolean selection) {
    	modulo = false;
    	resetWidget.setDataBounds(dataBounds);
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
    		selectWidget.initSelectionBounds(dataBounds, dataBounds, dataBounds.getCenter());
    		selectWidget.setEditingEnabled(true);
    		int zoom = mMap.getBoundsZoomLevel(dataBounds);
    		mMap.setZoomLevel(zoom);
    		mMap.setCenter(dataBounds.getCenter());
    		resetWidget.setSelectionBounds(dataBounds);  	  		
    	}
    	if ( modulo ) {
			rotateWidget.setVisible(true);
		} else {
			rotateWidget.setVisible(false);
		}
    }
	
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
		
	}
	public boolean isModulo() {
		return modulo;
	}
	public void setSelectionBounds(LatLngBounds bounds, boolean recenter) {
		selectWidget.setSelectionBounds(bounds, recenter);
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
		if ( selectWidget != null ) {
			selectWidget.setVisible(false);
		}
		if ( resetWidget != null ) {
			resetWidget.setVisible(false);
		}
		if ( rotateWidget != null ) {
			rotateWidget.setVisible(false);
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
		if ( selectWidget != null ) {
			selectWidget.setVisible(true);
		}
		if ( resetWidget != null ) {
			resetWidget.setVisible(true);
		}
		if ( rotateWidget != null ) {
			rotateWidget.setVisible(true);
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
		double xwest = selectWidget.getSelectionBounds().getSouthWest().getLongitude();
		if ( xwest < 0.0 ) {
			xwest = xwest + 360.;
		}
		return String.valueOf(xwest);
	}
	public String getXhi() {
		double xwest = selectWidget.getSelectionBounds().getSouthWest().getLongitude();
		double xeast = selectWidget.getSelectionBounds().getNorthEast().getLongitude();
		if ( xeast <= 0.0 || xeast < xwest ) {
			xeast = xeast + 360.;
		}
		return String.valueOf(xeast);
	}
	public String getYlo() {
		return String.valueOf(selectWidget.getSelectionBounds().getSouthWest().getLatitude());
	}
	public String getYhi() {
		return String.valueOf(selectWidget.getSelectionBounds().getNorthEast().getLatitude());
	}
	public RegionWidget getRegionWidget() {
		return regionWidget;
	}
}
