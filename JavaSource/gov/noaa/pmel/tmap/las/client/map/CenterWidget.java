package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

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
    	
    	lonText = new TextBox();
    	lonText.setText("Enter lon");
    	lonText.addChangeListener(textChangeListener);
    	
    	zeroButton = new Button("Center at 0");
    	zeroButton.addStyleName("las-centerButton");
    	zeroButton.addClickListener(buttonListener);
    	oneEightyButton = new Button("Center at 180");
    	oneEightyButton.addStyleName("las-centerButton");
    	oneEightyButton.addClickListener(buttonListener);
    	dataButton = new Button("Center on data");
    	dataButton.addStyleName("las-centerButton");
    	dataButton.addClickListener(buttonListener);
    	/*
    	userButton = new Button("Enter a longitude for center");
    	*/
    	layout.setWidget(0, 0, zeroButton);
        layout.setWidget(1, 0, oneEightyButton);
        layout.setWidget(2, 0, dataButton);
        /*
        layout.setWidget(3, 0, userButton);
        layout.setWidget(3, 1, lonText);
    	*/
        popPanel.add(layout);
    	
    	initWidget(setCenter);
    	
    	
    }
    
    ClickListener setCenterButtonListener = new ClickListener() {

		public void onClick(Widget sender) {
			popPanel.setPopupPosition(setCenter.getAbsoluteLeft(), setCenter.getAbsoluteTop());
			popPanel.show();
		}
    	
    };
    
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
				refMap.setDataBounds(refMap.getResetWidget().getDataBounds(), refMap.getDelta(), true);
			}
			popPanel.hide();
		}
    	
    };
    ChangeListener textChangeListener = new ChangeListener() {
		public void onChange(Widget sender) {
			TextBox t = (TextBox) sender;
			double lon = Double.valueOf(t.getText());
			//TODO clean input, then convert to lon.
			refMap.setCenter(LatLng.newInstance(data.getLatitude(), lon));
			popPanel.hide();
		}	
    };
    ChangeListener menuChangeListener = new ChangeListener() {
		public void onChange(Widget sender) {
			ListBox l = (ListBox) sender;
			String value = l.getValue(l.getSelectedIndex());
			if ( value.equals("0.0") ) {
				refMap.setCenter(LatLng.newInstance(data.getLatitude(), 0.0));
				LatLngBounds dBounds = refMap.getDataBounds();
				double nlat = dBounds.getNorthEast().getLatitude();
				double slat = dBounds.getSouthWest().getLatitude();
				double wlon = -180;
				double elon = 180;
				LatLng sw = LatLng.newInstance(slat, wlon);
				LatLng ne = LatLng.newInstance(nlat, elon);
				refMap.setDataBounds(LatLngBounds.newInstance(sw, ne), refMap.getDelta(), false);
			} else if ( value.equals("180.0") ) {
				refMap.setCenter(LatLng.newInstance(data.getLatitude(), 180.0));
			} else if ( value.equals("data")) {
				refMap.setCenter(data);
			} else {
//				lonEntry.setPopupPosition(centers.getAbsoluteLeft(), centers.getAbsoluteLeft());
//				lonEntry.show();
			}
		}   
    };
    public void setUser(double lon) {
    	this.user = LatLng.newInstance(data.getLatitude(), lon);
    }
    public void setData(LatLng data) {
    	this.data = data;
    	this.zero = LatLng.newInstance(data.getLatitude(), 0.0);
    	this.one_eighty = LatLng.newInstance(data.getLatitude(), 180.);
    }
}
