package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.maps.client.Copyright;
import com.google.gwt.maps.client.CopyrightCollection;
import com.google.gwt.maps.client.MapType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.TileLayer;
import com.google.gwt.maps.client.control.Control;
import com.google.gwt.maps.client.control.ControlAnchor;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.SmallMapControl;
import com.google.gwt.maps.client.control.SmallZoomControl;
import com.google.gwt.maps.client.event.MapDragEndHandler;
import com.google.gwt.maps.client.event.MapMouseOutHandler;
import com.google.gwt.maps.client.event.MapZoomEndHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.GroundOverlay;
import com.google.gwt.maps.client.overlay.TileLayerOverlay;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ReferenceMap extends Composite {
	DockPanel panel;
	Grid topControls;
	HorizontalPanel bottomControls;
	MaskOverlay maskOverlay = null;
	DataBoundsOverlay dataBoundsOverlay = null;
	SelectWidget selectWidget;
	LatLonWidget textWidget;
	ResetWidget resetWidget;
//	RotateWidget rotateWidget;
	CenterWidget centerWidget;
//	IconifyControl iconifyControl;
	SmallZoomControl smallZoomControl;
	MapTypeControl mapTypeControl;
	MapWidget mMap;
	RegionWidget regionWidget;
	LatLng mCenter;
	LatLngBounds dataBounds = LatLngBounds.newInstance(LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0));
	LatLngBounds moduloBounds = LatLngBounds.newInstance(LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0));
	int mZoom;
	int mWidth;   // Width in pixels 
	int mHeight;  // Height in pixels
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
	
	LatLng modulo_center;
	
	String gridID = null;
	
	GroundOverlay topOverlay;
	GroundOverlay bottomOverlay;
	
	/**
	 * Construct an LASReference map centered and zoomed with the width and height specified in pixels.
	 * @param center
	 * @param zoom
	 * @param width
	 * @param height
	 */
	public ReferenceMap (LatLng center, int zoom, int width, int height) {
        panel = new DockPanel();
        topControls = new Grid(2,2);
        bottomControls = new HorizontalPanel();
        bottomControls.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		mCenter = center;
		mZoom = zoom;
		mMap = new MapWidget(mCenter, mZoom);    	
		mMap.addMapZoomEndHandler(new MapZoomEndHandler() {

			public void onZoomEnd(MapZoomEndEvent event) {
				int nzoom = event.getNewZoomLevel();
				if ( nzoom == mZoom ) {
					mMap.setDraggable(false);
				} else if ( nzoom > mZoom ){
					mMap.setDraggable(true);
				} else {
					mMap.setDraggable(false);
				}
				
			}
		});
		mWidth = width;
		mHeight = height;
		mMap.setSize(String.valueOf(mWidth)+"px", String.valueOf(mHeight)+"px");
		selectWidget = new SelectWidget(this);
		textWidget = new LatLonWidget();
		selectWidget.setLatLngWidget(textWidget);
		smallZoomControl = new SmallZoomControl();
		mMap.addControl(smallZoomControl);
		mMap.setCurrentMapType(MapType.getHybridMap());
		regionWidget = new RegionWidget(this);
		resetWidget = new ResetWidget(this);
//		rotateWidget = new RotateWidget();
//		rotateWidget.addClickListener(rotate);
//		rotateWidget.setVisible(false);
		centerWidget = new CenterWidget(this);
		centerWidget.setVisible(false);
		mMap.addMapDragEndHandler(new MapDragEndHandler() {
			public void onDragEnd(MapDragEndEvent event) {
				if ( selectWidget.getSelectionBounds().containsLatLng(mMap.getCenter()) ) {
				    selectWidget.setSelectionCenter(mMap.getCenter());
				}
			}		
		});			
		
//		iconifyControl = new IconifyControl(new ControlPosition(ControlAnchor.TOP_RIGHT, 10, 30), this);
//		addControl(iconifyControl);
		topControls.setWidget(0, 0, regionWidget);
		topControls.setWidget(0, 1, selectWidget);
		topControls.setWidget(1, 0, centerWidget);
		topControls.setWidget(1, 1, resetWidget);
//      topControls.add(rotateWidget);
		bottomControls.add(textWidget);
		panel.add(topControls, DockPanel.NORTH);
		panel.add(bottomControls, DockPanel.SOUTH);
		panel.add(mMap, DockPanel.CENTER);
		
		initWidget(panel);
    }
    public void addControl(Control control) {
    	mMap.addControl(control);
    }
    /**
     * Initializes the data bounds and sets the reset widget to return to these bounds when clicked.
     *
     * @param dataBounds
     * @param delta
     * @param selection
     */
    public void initDataBounds(LatLngBounds dataBounds, double delta, boolean selection) {
    	setDataBounds(dataBounds, delta, true);
		getResetWidget().setDataBounds(dataBounds);
		getResetWidget().setSelectionBounds(dataBounds);
		getRegionWidget().setSelectedIndex(0);
    }
    /**
     * Sets the data bounds, but leaves the reset values to their initial values.
     * @param dataBounds
     * @param delta
     * @param selection
     */
    public void setDataBounds(LatLngBounds dataBounds, double delta, boolean selection) {
    	modulo = false;
    	removeTopAndBottom();
    	centerWidget.setData(dataBounds.getCenter());
    	this.dataBounds = dataBounds;
    	double lon_span = dataBounds.toSpan().getLongitude();
		if ( dataBounds.isFullLongitude() || lon_span + 2.*delta >= 360.0 ) {
			modulo = true;
			mMap.setDraggable(false);
		}
    	this.delta = delta;
    	// Set data selection rectangle to the data bounds
    	if ( selection ) {
    		if ( dataBoundsOverlay != null ) {
    			mMap.removeOverlay(dataBoundsOverlay.getPolygon());
    		}
    		dataBoundsOverlay = new DataBoundsOverlay(dataBounds);
    		if ( !modulo ) {
    		   mMap.addOverlay(dataBoundsOverlay.getPolygon());
    		}
    		if ( modulo ) {
    		    selectWidget.initSelectionBounds(dataBounds, dataBounds, dataBounds.getCenter(), false);
    		} else {
    			selectWidget.initSelectionBounds(dataBounds, dataBounds, dataBounds.getCenter(), true);
    		}
//    		selectWidget.setEditingEnabled(true);
    		mZoom = mMap.getBoundsZoomLevel(dataBounds);
    		mMap.setZoomLevel(mZoom);
    		mMap.setCenter(dataBounds.getCenter());
    		if ( mZoom == 0 ) {
    			addTopAndBottom();
    		}
    	}
    	if ( modulo ) {
			centerWidget.setVisible(true);
		} else {
			centerWidget.setVisible(false);
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
	public void setSelectionBounds(LatLngBounds bounds, boolean recenter, boolean show) {
		selectWidget.setSelectionBounds(bounds, recenter, show);
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
//		if ( rotateWidget != null ) {
//			rotateWidget.setVisible(false);
//		}
		if ( centerWidget != null ) {
			centerWidget.setVisible(false);
		}
		if ( smallZoomControl != null ) {
			mMap.removeControl(smallZoomControl);
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
//		if ( rotateWidget != null ) {
//			rotateWidget.setVisible(true);
//		}
		if ( centerWidget != null ) {
			centerWidget.setVisible(true);
		}
		if ( smallZoomControl != null ) {
			mMap.addControl(smallZoomControl);
		}		
		if ( mapTypeControl != null ) {
			mMap.addControl(mapTypeControl);
		}
	}
	public void resetSize() {
		mMap.setSize(String.valueOf(mWidth)+"px", String.valueOf(mHeight)+"px");		
	}
	public String getXlo() {
		
		//return selectWidget.getXlo();
		
		double xwest = selectWidget.getSelectionBounds().getSouthWest().getLongitude();
		return String.valueOf(xwest);
		
	}
	public String getXhi() {
		
		double xwest = selectWidget.getSelectionBounds().getSouthWest().getLongitude();
		double xeast = selectWidget.getSelectionBounds().getNorthEast().getLongitude();
		if ( xeast < xwest ) {
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
	public int getZoom() {
		return mZoom;
	}
	public double getDelta() {
		return delta;
	}
	public ResetWidget getResetWidget() {
		return resetWidget;
	}
	public void addTopAndBottom() {
		double wlon = dataBounds.getSouthWest().getLongitude();
		double elon = dataBounds.getNorthEast().getLongitude();
		if ( elon < wlon ) {
			elon = elon + 360.;
		}
		
		LatLngBounds topBounds = LatLngBounds.newInstance(LatLng.newInstance(85., wlon), LatLng.newInstance(90., elon));
        topOverlay = new GroundOverlay("http://localhost:8880/baker/images/top.png", topBounds);
        mMap.addOverlay(topOverlay);
        LatLngBounds bottomBounds = LatLngBounds.newInstance(LatLng.newInstance(-90., dataBounds.getSouthWest().getLongitude()), LatLng.newInstance(-85., dataBounds.getNorthEast().getLongitude()));
        bottomOverlay = new GroundOverlay("http://localhost:8880/baker/images/top.png", bottomBounds);
        mMap.addOverlay(bottomOverlay);
	}
	public void removeTopAndBottom() {
		if ( topOverlay != null ) mMap.removeOverlay(topOverlay);
		if ( bottomOverlay != null ) mMap.removeOverlay(bottomOverlay);
	}
}
