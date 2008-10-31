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
	public ResetControl (ControlPosition position, LatLng center, int zoom) {
		super(position);
		mCenter = center;
		mZoom = zoom;
	}
	@Override
	protected Widget initialize(final MapWidget map) {
		Button resetButton = new Button("Reset");
		resetButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				map.setCenter(mCenter);
				map.setZoomLevel(mZoom);
			}
		});
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
	public LatLng getMCenter() {
		return mCenter;
	}
	/**
	 * @return the mZoom
	 */
	public int getMZoom() {
		return mZoom;
	}
	/**
	 * @param center the mCenter to set
	 */
	public void setMCenter(LatLng center) {
		mCenter = center;
	}
	/**
	 * @param zoom the mZoom to set
	 */
	public void setMZoom(int zoom) {
		mZoom = zoom;
	}

}
