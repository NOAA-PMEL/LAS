package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MapButton extends Composite {
	Button mapButton;
	PopupPanel mapPanel;
	VerticalPanel mapInteriorPanel;
	Button closeButton;
	ReferenceMap refMap;
	
	public MapButton (LatLng center, int zoom, int xpix, int ypix) {
		mapButton = new Button ("Select Region");
		mapButton.addClickListener(mapButtonClick);

		mapPanel = new PopupPanel(false);
		mapInteriorPanel = new VerticalPanel();

		closeButton = new Button("Close and Plot");
		closeButton.addClickListener(closeClick);

		mapInteriorPanel.add(closeButton);

		refMap = new ReferenceMap(center, zoom, xpix, ypix);
		mapInteriorPanel.add(refMap);

		mapPanel.add(mapInteriorPanel);
		initWidget(mapButton);
	}
	public void addClickListener(ClickListener listener) {
		closeButton.addClickListener(listener);
	}
	ClickListener mapButtonClick = new ClickListener() {
		public void onClick(Widget sender) {
			mapPanel.setPopupPosition(mapButton.getAbsoluteLeft(), mapButton.getAbsoluteTop());
			mapPanel.show();			
		}		
	};
	ClickListener closeClick = new ClickListener() {
		public void onClick(Widget sender) {
			mapPanel.hide();	
		}
	};
	public void addCloseClickListener(ClickListener close) {
		closeButton.addClickListener(close);
	}
	public ReferenceMap getRefMap() {
		return refMap;
	}
}
