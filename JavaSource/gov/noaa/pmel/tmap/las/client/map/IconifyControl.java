package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.Control.CustomControl;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

public class IconifyControl extends CustomControl {
	Button iconify;
	MapWidget mMap;
	ReferenceMap refMap;
    public IconifyControl (ControlPosition position, ReferenceMap refMap) {
    	super(position);
    	this.refMap = refMap;
    }
	@Override
	protected Widget initialize(MapWidget map) {
		mMap = map;
		iconify = new Button("-");
		iconify.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				if ( iconify.getText().equals("-") ) {
					mMap.setSize("240px", "120px");
					int zoom = mMap.getBoundsZoomLevel(refMap.selectWidget.getSelectionBounds());
					mMap.setZoomLevel(zoom);
					refMap.hideControls();
					iconify.setText("+");
				} else {
				    refMap.resetSize();
					int zoom = mMap.getBoundsZoomLevel(refMap.selectWidget.getSelectionBounds());
					mMap.setZoomLevel(zoom);
					refMap.showControls();
					iconify.setText("-");
				}
			}

		});
		return iconify;
	}

	@Override
	public boolean isSelectable() {
		// TODO Auto-generated method stub
		return false;
	}

}
