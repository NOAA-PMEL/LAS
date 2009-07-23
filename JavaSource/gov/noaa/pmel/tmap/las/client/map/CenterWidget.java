package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
public class CenterWidget extends ListBox {

	LatLngBounds data;	
	ReferenceMap refMap;
	
	/**
	 * Constructs a widget, the value for centering on the data must be supplied via a @see #setData(LatLng) call. 
	 * @param map
	 */
    public CenterWidget(ReferenceMap map) {
 
        this.refMap = map;
        this.data = map.getDataBounds();
        addItem("Set center", "Set center");
        addItem("Center at 0", "at 0");
        addItem("Center at 180", "at 180");
        addItem("Center on the data", "at data");
        addChangeHandler(changeHandler);
    }
   
    /**
     * Listens for changes on the centering buttons and rotates the map accordingly.
     */
    ChangeHandler changeHandler = new ChangeHandler() {
    	@Override
		public void onChange(ChangeEvent event) {
			String text = getValue(getSelectedIndex());
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
			} else if ( text.contains("data") ) {
				refMap.setDataBounds(data, refMap.getDelta(), true);
			}
			setSelectedIndex(0);
		}
    };
    /**
     * Sets the location of the center of the current data range to allow centering on that location (handy if different from 0 or 180).
     * @param data
     */
    public void setData(LatLngBounds data) {
    	this.data = data;
    }
}
