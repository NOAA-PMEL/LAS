package gov.noaa.pmel.tmap.las.client;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.Control.CustomControl;
import com.google.gwt.maps.client.event.MapMouseMoveHandler;
import com.google.gwt.maps.client.event.MarkerMouseDownHandler;
import com.google.gwt.maps.client.event.MarkerMouseUpHandler;
import com.google.gwt.maps.client.event.PolygonEndLineHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.PolyEditingOptions;
import com.google.gwt.maps.client.overlay.Polygon;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class SelectControl extends CustomControl {
	private MapWidget mMap;
	private Button selectAll;
	private ToggleButton mSelect;
	private MapTool mSelection;
	private Marker mDrawMarker;
	private MarkerOptions mOptions;
	private Icon mIcon;
	boolean mDraw = false;
	private LatLngBounds dataBounds;
	private Grid buttons;
	private String gridID = null;
	public SelectControl (ControlPosition position) {
		super(position);

	}
	@Override
	protected Widget initialize(final MapWidget map) {
		String moduleRelativeURL = GWT.getModuleBaseURL();
        String moduleName = GWT.getModuleName();
        moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.indexOf(moduleName)-1);
        moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.lastIndexOf("/")+1);
        String imageURL = moduleRelativeURL + "images/";
		mMap = map;
		buttons = new Grid(2,1);
		LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0));
		mSelection = new MapTool(mMap, dataBounds, bounds,"xy");
		mIcon = Icon.newInstance();
		mIcon.setIconSize(Size.newInstance(20, 20));
		mIcon.setIconAnchor(Point.newInstance(10, 10));
		mIcon.setImageURL(imageURL+"crosshairs.png");
		mOptions = MarkerOptions.newInstance();
        mOptions.setIcon(mIcon);
		mOptions.setDraggable(true);
		mOptions.setDragCrossMove(true);
		mDrawMarker = new Marker(LatLng.newInstance(0.0, 0.0), mOptions);
		mSelection.setVisible(false);
		mDrawMarker.addMarkerMouseDownHandler(new MarkerMouseDownHandler() {
			public void onMouseDown(MarkerMouseDownEvent event) {
				mDraw = true;
				LatLng click = mDrawMarker.getLatLng();
				LatLngBounds bounds = LatLngBounds.newInstance(click, click);
				mSelection.setEditingEnabled(false);
				mMap.removeOverlay(mSelection.getPolygon());
				for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
					Marker marker = (Marker) markerIt.next();
					mMap.removeOverlay(marker);
				}
				mSelection = new MapTool(mMap, dataBounds, bounds, "xy");	
				mSelection.setClick(click);
				mMap.addOverlay(mSelection.getPolygon());
				for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
					Marker marker = (Marker) markerIt.next();
					mMap.addOverlay(marker);
				}
				mSelection.setEditingEnabled(true);
                mSelection.setVisible(true);	
			}
		});
		mDrawMarker.addMarkerMouseUpHandler(new MarkerMouseUpHandler() {

			public void onMouseUp(MarkerMouseUpEvent event) {
				mSelect.setDown(false);
				mDrawMarker.setVisible(false);
	            mMap.removeMapMouseMoveHandler(mouseMove);
	            mDraw = false;
	            mMap.setDraggable(true);
	            mSelection.setEditingEnabled(true);
			}
			
		});
		mSelect = new ToggleButton("Select");
		mSelect.addStyleName("map-button");
		mSelect.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				if ( gridID == null ) {
					Window.alert("Please select a data set and variable.");
					mSelect.setDown(false);
				} else {
					if (mSelect.isDown()) {
						mSelection.setEditingEnabled(false);
						mMap.removeOverlay(mSelection.getPolygon());
						mMap.setDraggable(false);
						mMap.addMapMouseMoveHandler(mouseMove);
						mDrawMarker.setVisible(true);
					} else {	          
						mDrawMarker.setVisible(false);
						//mMap.removeMapMouseMoveHandler(mouseMove);		           
					}
				}
			}
		});
		mMap.addOverlay(mDrawMarker);
		mDrawMarker.setVisible(false);
		mMap.addOverlay(mSelection.getPolygon());
		for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
			Marker marker = (Marker) markerIt.next();
			mMap.addOverlay(marker);
		}
		mSelection.setEditingEnabled(false);
		selectAll = new Button("Select All");
		selectAll.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				if ( gridID == null ) {
					Window.alert("Please select a data set and variable.");
				} else {
					mSelection.setEditingEnabled(false);
					mMap.removeOverlay(mSelection.getPolygon());
					mSelection = new MapTool(mMap, dataBounds, dataBounds, "xy");
					mMap.addOverlay(mSelection.getPolygon());
					for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
						Marker marker = (Marker) markerIt.next();
						mMap.addOverlay(marker);
					}
					mSelection.setEditingEnabled(true);
					mSelection.setVisible(true);
				}
			}
			
		});
		buttons.setWidget(0, 0, mSelect);
		buttons.setWidget(1, 0, selectAll);
		return buttons;
	}
	MapMouseMoveHandler mouseMove = new MapMouseMoveHandler() {

		public void onMouseMove(MapMouseMoveEvent event) {
			LatLng position = event.getLatLng();
			if ( dataBounds.containsLatLng(position)) {
				mDrawMarker.setVisible(true);
				mDrawMarker.setLatLng(position);
				if ( mDraw ) {
					mSelection.setEditingEnabled(false);
					mMap.removeOverlay(mSelection.getPolygon());
					mSelection.update(position);
					mMap.addOverlay(mSelection.getPolygon());
					mSelection.setEditingEnabled(true);
					mSelection.setVisible(true);	
				}
			} else {
				mDrawMarker.setVisible(false);
			}
		}

	};
	@Override
	public boolean isSelectable() {
		// TODO Auto-generated method stub
		return false;
	}
    public boolean isDown() {
    	return mSelect.isDown();
    }
	/**
	 * @return the dataBounds
	 */
	public LatLngBounds getDataBounds() {
		return dataBounds;
	}
	/**
	 * @param dataBounds the dataBounds to set
	 */
	public void setDataBounds(LatLngBounds dBounds) {
		mSelection.setEditingEnabled(false);
		mMap.removeOverlay(mSelection.getPolygon());
		this.dataBounds = dBounds;
		mSelection = new MapTool(mMap, dataBounds, dataBounds, "xy");
		mMap.addOverlay(mSelection.getPolygon());
		for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
			Marker marker = (Marker) markerIt.next();
			mMap.addOverlay(marker);
		}
		mSelection.setEditingEnabled(true);
		mSelection.setVisible(true);
	}
	public void clearOverlays() {
		if ( mSelection != null ) {
			mMap.removeOverlay(mSelection.getPolygon());
			for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
				Marker marker = (Marker) markerIt.next();
				mMap.removeOverlay(marker);
			}	
		}
		
	}
	public void setGridID(String id) {
		gridID = id;		
	}
}
