package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ResetWidget extends Composite {
    LatLng mCenter;
    int mZoom;
    Button resetButton;
    private LatLngBounds dataBounds;
	private ReferenceMap refMap;
	public ResetWidget (ReferenceMap map) {
		this.refMap = map;
		this.resetButton = new Button("Reset");
		this.resetButton.addStyleName("map-button");
		this.resetButton.addClickListener(click);
		initWidget(resetButton);
	}
	ClickListener click = new ClickListener() {
		public void onClick(Widget sender) {
			if ( dataBounds == null ) {
				Window.alert("Please select a variable.");
			} else {
			    reset();
			}
			
		}
	};
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
	public void setSelectionBounds(LatLngBounds dataBounds) {
		
		setCenter(dataBounds.getCenter());
		setZoom(refMap.getBoundsZoomLevel(dataBounds));
		
	}
	public void setVisible(boolean visible) {
		resetButton.setVisible(visible);
	}
	public void setDataBounds(LatLngBounds dataBounds) {
		this.dataBounds = dataBounds;
		
	}
	public LatLngBounds getDataBounds() {
		return this.dataBounds;
	}
	public void reset() {
		refMap.setDataBounds(dataBounds, refMap.getDelta(), true);
	}
}
