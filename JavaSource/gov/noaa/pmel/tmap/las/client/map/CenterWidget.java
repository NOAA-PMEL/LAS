package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
/**
 * A widgets that "rotates" the maps so it is centered at 0, 180 or where ever the current data selection is centered.  It should only be
 * active when the longitude range spans the globe.  The definition of spans is that the east most longitude value is within "delta"
 * of the west most longitude.  The delta value is supplied to the reference map when the data bounds are set.
 * @author rhs
 *
 */
public class CenterWidget extends Composite {
	LatLng zero;
	LatLng one_eighty;
	LatLng data;
	LatLng user;
	Button zeroButton;
	Button oneEightyButton;
	Button dataButton;
	Button userButton;
	
	PopupPanel popPanel;
	Grid layout;
	TextBox lonText;
	
	Button setCenter;
	
	ReferenceMap refMap;
	
	/**
	 * Constructs a widget, the value for centering on the data must be supplied via a @see #setData(LatLng) call. 
	 * @param map
	 */
    public CenterWidget(ReferenceMap map) {
    	
    	this.data = LatLng.newInstance(0.0, 0.0);
    	this.zero = LatLng.newInstance(data.getLatitude(), 0.0);
    	this.one_eighty = LatLng.newInstance(data.getLatitude(), 180.);
    	this.user = data;
        this.refMap = map;
    	
        setCenter = new Button("Set Center");
        setCenter.addClickListener(setCenterButtonListener);
        
    	popPanel = new PopupPanel();
    	
    	layout = new Grid(3, 2);
    	
    	zeroButton = new Button("Center at 0");
    	zeroButton.addStyleName("las-centerButton");
    	zeroButton.addClickListener(buttonListener);
    	oneEightyButton = new Button("Center at 180");
    	oneEightyButton.addStyleName("las-centerButton");
    	oneEightyButton.addClickListener(buttonListener);
    	dataButton = new Button("Center on data");
    	dataButton.addStyleName("las-centerButton");
    	dataButton.addClickListener(buttonListener);
    	
    	layout.setWidget(0, 0, zeroButton);
        layout.setWidget(1, 0, oneEightyButton);
        layout.setWidget(2, 0, dataButton);
       
        popPanel.add(layout);
    	
    	initWidget(setCenter);
    	
    	
    }
    /**
     * Pops up the panel for the centering widget.
     */
    ClickListener setCenterButtonListener = new ClickListener() {

		public void onClick(Widget sender) {
			popPanel.setPopupPosition(setCenter.getAbsoluteLeft(), setCenter.getAbsoluteTop());
			popPanel.show();
		}
    	
    };
    /**
     * Listens for clicks on the centering buttons and rotates the map accordingly.
     */
    ClickListener buttonListener = new ClickListener() {

		public void onClick(Widget sender) {
			Button r = (Button) sender;
			String text = r.getText();
			if ( text.contains("at 0") ) {
				LatLngBounds dBounds = refMap.getDataBounds();
				double nlat = dBounds.getNorthEast().getLatitude();
				double slat = dBounds.getSouthWest().getLatitude();
				double wlon = -180;
				double elon = 179;
				LatLng sw = LatLng.newInstance(slat, wlon);
				LatLng ne = LatLng.newInstance(nlat, elon);
				LatLngBounds bounds = LatLngBounds.newInstance(sw, ne);
				refMap.setDataBounds(bounds, refMap.getDelta(), true);
			} else if ( text.contains("at 180") ) {
				LatLngBounds dBounds = refMap.getDataBounds();
				double nlat = dBounds.getNorthEast().getLatitude();
				double slat = dBounds.getSouthWest().getLatitude();
				double wlon = 0.;
				double elon = 359.;
				LatLng sw = LatLng.newInstance(slat, wlon);
				LatLng ne = LatLng.newInstance(nlat, elon);
				LatLngBounds bounds = LatLngBounds.newInstance(sw, ne);
				refMap.setDataBounds(bounds, refMap.getDelta(), true);
			} else if ( text.contains("on data") ) {
				refMap.setDataBounds(refMap.getDataBounds(), refMap.getDelta(), true);
			}
			popPanel.hide();
		}
    	
    };
    /**
     * Sets the location of the center of the current data range to allow centering on that location (handy if different from 0 or 180).
     * @param data
     */
    public void setData(LatLng data) {
    	this.data = data;
    }
}
