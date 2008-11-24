package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.Control.CustomControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

public class ResetControl extends CustomControl {
    LatLng mCenter;
    int mZoom;
    Button resetButton;
	public ResetControl (ControlPosition position, LatLng center, int zoom) {
		super(position);
		mCenter = center;
		mZoom = zoom;
	}
	
	@Override
	protected Widget initialize(final MapWidget map) {
		resetButton = new Button("Reset");
		resetButton.addStyleName("map-button");
		/*
		resetButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				map.setCenter(mCenter);
				map.setZoomLevel(mZoom);
				
			}
		});
		*/
		return resetButton;
	}
    
	@Override
	public boolean isSelectable() {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * @return the mCenter
	 */
	public LatLng getCenter() {
		return mCenter;
	}
	/**
	 * @return the mZoom
	 */
	public int getZoom() {
		return mZoom;
	}
	/**
	 * @param center the mCenter to set
	 */
	public void setCenter(LatLng center) {
		mCenter = center;
	}
	/**
	 * @param zoom the mZoom to set
	 */
	public void setZoom(int zoom) {
		mZoom = zoom;
	}
    public void addClickListener(ClickListener listener) {
    	resetButton.addClickListener(listener);
    }
}
