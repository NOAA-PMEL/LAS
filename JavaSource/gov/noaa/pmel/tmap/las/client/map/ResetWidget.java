package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
/**
 * A widget that will reset the map to the center of the valid data region and set the selection to the entire valid data region
 * @author rhs
 *
 */
public class ResetWidget extends Composite {
    LatLng mCenter;
    int mZoom;
    Button resetButton;
    private ReferenceMap refMap;
    /**
     * Construct a resetWidget for use with a particular reference map.  You have to set the data bounds of the reference map before users
     * can use the reset widget.
     * @param map the map that will be reset using this widget
     */
	public ResetWidget (ReferenceMap map) {
		this.refMap = map;
		this.resetButton = new Button("Reset");
		this.resetButton.addStyleName("map-button");
		this.resetButton.addClickListener(click);
		initWidget(resetButton);
	}
	/**
	 * Handle a click on the reset button.
	 */
	ClickListener click = new ClickListener() {
		public void onClick(Widget sender) {
			if ( refMap.getDataBounds() == null ) {
				Window.alert("Please set the data bounds for this map instance.");
			} else {
			    reset();
			}
			
		}
	};
	
	/**
	 * Set the center which will be used when the reset button is pressed 
	 * @param center the cener to set
	 */
	public void setCenter(LatLng center) {
		mCenter = center;
	}
	/**
	 * Set the zoom level that will be used when the reset button is pressed
	 * @param zoom the zoom to set
	 */
	public void setZoom(int zoom) {
		mZoom = zoom;
	}
	/**
	 * Add an external click listener to listen for clicks on the reset button
	 * @param listener
	 */
    public void addClickListener(ClickListener listener) {
    	resetButton.addClickListener(listener);
    }
    /**
     * Use the bounds to compute the center and zoom level to use when the reset button is pressed
     * @param dataBounds
     */
	public void setSelectionBounds(LatLngBounds dataBounds) {
		
		setCenter(dataBounds.getCenter());
		setZoom(refMap.getBoundsZoomLevel(dataBounds));
		
	}
	/**
	 * Turn the reset button on and off
	 */
	public void setVisible(boolean visible) {
		resetButton.setVisible(visible);
	}
	/**
	 * Reset the map to be centered on the current data bounds with the entire valid data region selected
	 */
	public void reset() {
		refMap.setDataBounds(refMap.getDataBounds(), refMap.getDelta(), true);
	}
}
