package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.Control.CustomControl;
import com.google.gwt.maps.client.event.MapMouseMoveHandler;
import com.google.gwt.maps.client.event.MarkerMouseDownHandler;
import com.google.gwt.maps.client.event.MarkerMouseUpHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polygon;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class SelectControl extends CustomControl {
	private MapWidget mMap;
	private TextBox mTextBox;
	private ToggleButton mSelect;
	private Polygon mSelectPoly;
	private LatLng[] mPolyPoints;
	private Marker mDrawMarker;
	private MarkerOptions mOptions;
	boolean mDraw = false;
	public SelectControl (ControlPosition position) {
		super(position);

	}
	@Override
	protected Widget initialize(final MapWidget map) {
		mMap = map;
		
		final Grid grid = new Grid(1,2);
		mSelect = new ToggleButton("Select");
		mTextBox = new TextBox();
		grid.setWidget(0, 0, mSelect);
		grid.setWidget(0,1,mTextBox);
		mTextBox.setVisible(false);
		mTextBox.setWidth("400px");
		mSelect.addClickListener(new ClickListener() {
		      public void onClick(Widget sender) {
		        if (mSelect.isDown()) {
		        	mMap.clearOverlays();
		        	mMap.setDraggable(false);
		        	mMap.addMapMouseMoveHandler(mouseMove);
		        	mTextBox.setVisible(true);
		        	mMap.addOverlay(mDrawMarker);
		        	mDrawMarker.setVisible(true);
		        	
		        } else {	          
		        	mDrawMarker.setVisible(false);
		            mMap.removeMapMouseMoveHandler(mouseMove);
		            mTextBox.setVisible(false);
		        }
		      }
		    });
		mPolyPoints = new LatLng[5];
		mPolyPoints[0] = LatLng.newInstance(0.0, 0.0);
		mPolyPoints[1] = LatLng.newInstance(0.0, 0.0);
		mPolyPoints[2] = LatLng.newInstance(0.0, 0.0);
		mPolyPoints[3] = LatLng.newInstance(0.0, 0.0);
		mPolyPoints[4] = LatLng.newInstance(0.0, 0.0);
		mSelectPoly = new Polygon(mPolyPoints,"#FF0000", 3, 1., "#FF0000", 0.0);
		Icon icon = Icon.newInstance();
		icon.setIconSize(Size.newInstance(20, 20));
		icon.setIconAnchor(Point.newInstance(10, 10));
		icon.setImageURL("http://localhost:8880/baker/images/crosshairs.png");
		mOptions = MarkerOptions.newInstance();
        mOptions.setIcon(icon);
		mOptions.setDraggable(true);
		mDrawMarker = new Marker(LatLng.newInstance(0.0, 0.0), mOptions);
		mMap.addOverlay(mDrawMarker);
		mDrawMarker.setVisible(false);
		mMap.addOverlay(mSelectPoly);
		mSelectPoly.setVisible(false);
		mDrawMarker.addMarkerMouseDownHandler(new MarkerMouseDownHandler() {
			public void onMouseDown(MarkerMouseDownEvent event) {
				mDraw = true;
				LatLng click = mDrawMarker.getLatLng();
				mPolyPoints[0] = click;
				mPolyPoints[1] = click;
				mPolyPoints[2] = click;
				mPolyPoints[3] = click;
				mPolyPoints[4] = click;
				mSelectPoly = new Polygon(mPolyPoints,"#FF0000", 3, 1., "#FF0000", 0.0);
				mMap.addOverlay(mSelectPoly);
                mSelectPoly.setVisible(true);	
			}
		});
		mDrawMarker.addMarkerMouseUpHandler(new MarkerMouseUpHandler() {

			public void onMouseUp(MarkerMouseUpEvent event) {
				mSelect.setDown(false);
				mDrawMarker.setVisible(false);
	            mMap.removeMapMouseMoveHandler(mouseMove);
	            mTextBox.setVisible(false);
	            mDraw = false;
	            mMap.setDraggable(true);
			}
			
		});
		
		return grid;
	}
	MapMouseMoveHandler mouseMove = new MapMouseMoveHandler() {

		public void onMouseMove(MapMouseMoveEvent event) {
			mDrawMarker.setVisible(true);
			LatLng position = event.getLatLng();
			mDrawMarker.setLatLng(position);
			mTextBox.setText("Lat: " + position.getLatitude()+" Lon: "+ position.getLongitude() );
			if ( mDraw ) {
				mPolyPoints[1] = LatLng.newInstance(mPolyPoints[0].getLatitude(), position.getLongitude());
				mPolyPoints[2] = position;
				mPolyPoints[3] = LatLng.newInstance(position.getLatitude(), mPolyPoints[0].getLongitude());
				mMap.removeOverlay(mSelectPoly);
				mSelectPoly = new Polygon(mPolyPoints,"#FF0000", 3, 1., "#FF0000", 0.0);
				mMap.addOverlay(mSelectPoly);
				mSelectPoly.setVisible(true);	
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
}
