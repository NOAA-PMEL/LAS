package gov.noaa.pmel.tmap.las.client.map;

import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MapButton extends Composite {
	Button mapButton;
	DialogBox mapDialog;
	VerticalPanel interiorPanel;
	HorizontalPanel buttonFlow;
	Button closeButton;
	Button applyButton;
	
	ReferenceMap refMap;
	Label dateTimeLabel = new Label("Select a date/time:");
	DateTimeWidget dateTimeWidget;
	Label zLabel = new Label("z");
	AxisWidget zAxisWidget;
	
	public MapButton (LatLng center, int zoom, int xpix, int ypix, String panelID) {
		mapButton = new Button ("Select Region");
		mapButton.addClickListener(settingsButtonClick);
		
		mapDialog = new DialogBox(false, false);
		mapDialog.setText("Select the Lat/Lon Region");
		
        interiorPanel = new VerticalPanel();
		closeButton = new Button("Close");
		closeButton.addClickListener(closeClick);
		
		applyButton = new Button("Apply");
		applyButton.setTitle("Apply Changes "+panelID);
		
		
		buttonFlow = new HorizontalPanel();
		buttonFlow.add(closeButton);
		buttonFlow.add(applyButton);
	 
		refMap = new ReferenceMap(center, zoom, xpix, ypix);
		interiorPanel.add(buttonFlow);
		interiorPanel.add(refMap);
		
		mapDialog.add(interiorPanel);
		
		initWidget(mapButton);	
	}
	public void addClickListener(ClickListener listener) {
		closeButton.addClickListener(listener);
	}
	ClickListener settingsButtonClick = new ClickListener() {
		public void onClick(Widget sender) {
			mapDialog.setPopupPosition(mapButton.getAbsoluteLeft(), mapButton.getAbsoluteTop());
			mapDialog.show();			
		}		
	};
	ClickListener closeClick = new ClickListener() {
		public void onClick(Widget sender) {
			mapDialog.hide();	
		}
	};
	public void addCloseClickListener(ClickListener close) {
		closeButton.addClickListener(close);
	}
	public void addApplyClickListener(ClickListener apply) {
		applyButton.addClickListener(apply);
	}
	public ReferenceMap getRefMap() {
		return refMap;
	}
	
	public void setLatLon(String xlo, String xhi, String ylo, String yhi) {
		refMap.setLatLon(xlo, xhi, ylo, yhi);
	}
}
