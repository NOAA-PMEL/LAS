package gov.noaa.pmel.tmap.las.client.map;

import gov.noaa.pmel.tmap.las.client.OperationsMenu;
import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsWidget;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SettingsButton extends Composite {
	Button settingsButton;
	PopupPanel settingsPanel;
	HorizontalPanel interiorPanel;
	VerticalPanel leftInteriorPanel;
	OperationsWidget operations;
	Button closeButton;
	ReferenceMap refMap;
	
	public SettingsButton (LatLng center, int zoom, int xpix, int ypix) {
		settingsButton = new Button ("Settings");
		settingsButton.addClickListener(settingsButtonClick);
		
		interiorPanel = new HorizontalPanel();

		settingsPanel = new PopupPanel(false);
		leftInteriorPanel = new VerticalPanel();

		closeButton = new Button("Close and Plot");
		closeButton.addClickListener(closeClick);

		leftInteriorPanel.add(closeButton);
	 
		operations = new OperationsWidget();
		leftInteriorPanel.add(operations);
		
		interiorPanel.add(leftInteriorPanel);
		
		refMap = new ReferenceMap(center, zoom, xpix, ypix);
		interiorPanel.add(refMap);

		settingsPanel.add(interiorPanel);
		
		initWidget(settingsButton);
	}
	public void addClickListener(ClickListener listener) {
		closeButton.addClickListener(listener);
	}
	ClickListener settingsButtonClick = new ClickListener() {
		public void onClick(Widget sender) {
			settingsPanel.setPopupPosition(settingsButton.getAbsoluteLeft(), settingsButton.getAbsoluteTop());
			settingsPanel.show();			
		}		
	};
	ClickListener closeClick = new ClickListener() {
		public void onClick(Widget sender) {
			settingsPanel.hide();	
		}
	};
	public void addCloseClickListener(ClickListener close) {
		closeButton.addClickListener(close);
	}
	public ReferenceMap getRefMap() {
		return refMap;
	}
	public void setOperations(RPCServiceAsync rpcService, String view, String dsID, String varID, OperationsMenu menu) {
		operations.setOperations(rpcService, view, dsID, varID, menu);
	}
}
