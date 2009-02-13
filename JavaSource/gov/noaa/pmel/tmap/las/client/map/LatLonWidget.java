package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class LatLonWidget extends Composite {
	TextBox southLat;
	TextBox northLat;
	TextBox westLon;
	TextBox eastLon;
	Label lonLabel;
	Label latLabel;
	Grid panel;
	NumberFormat latFormat;
	NumberFormat lonFormat;
	private static final String boxWidth = "70px";
	public LatLonWidget() {
		panel = new Grid(2,4);
		
		latLabel = new Label("Lat:");
		lonLabel = new Label("Lon:");

		southLat = new TextBox();
		northLat = new TextBox();

		southLat.setWidth(boxWidth);
		northLat.setWidth(boxWidth);

		eastLon = new TextBox();
		westLon = new TextBox();

		eastLon.setWidth(boxWidth);
		westLon.setWidth(boxWidth);
      
		panel.setWidget(0, 0, latLabel);
		panel.setWidget(0, 1, northLat);
		panel.setWidget(1, 1, southLat);
		panel.setWidget(0, 2, lonLabel);
		panel.setWidget(0, 3, eastLon);
		panel.setWidget(1, 3, westLon);
		latFormat = NumberFormat.getFormat("###.##");
		lonFormat = NumberFormat.getFormat("####.##");
		initWidget(panel);
	}
	public void setText(LatLngBounds selectionBounds) {
		LatLng swPolyCorner = selectionBounds.getSouthWest();
		LatLng nePolyCorner = selectionBounds.getNorthEast();
		double slat = swPolyCorner.getLatitude();
		String slat_f;
		if ( slat <= 0.0 ) {
			slat_f = latFormat.format(Math.abs(slat))+" S";
		} else {
			slat_f = latFormat.format(slat)+" N";
		}
		southLat.setText(slat_f);
		double nlat = nePolyCorner.getLatitude();
		String nlat_f;
		if ( nlat <= 0.0 ) {
			nlat_f = latFormat.format(Math.abs(nlat))+" S";
		} else {
			nlat_f = latFormat.format(nlat)+" N";
		}
		northLat.setText(nlat_f);
		double wlon = swPolyCorner.getLongitude();
		double elon = nePolyCorner.getLongitude();
		String wlon_f;
		String elon_f;
		if ( wlon < 0.0 ) {
			wlon = wlon + 180.;
			wlon_f = lonFormat.format(wlon)+" W";
		} else {
			wlon_f = lonFormat.format(wlon)+" E";
		}
		if ( elon < 0.0 ) {
			elon = elon + 180;
			elon_f = lonFormat.format(elon)+" W";
		} else {
			elon_f = lonFormat.format(elon)+" E";
		}


		westLon.setText(wlon_f);
		eastLon.setText(elon_f);
		
	}
	public String getXhi() {
		return eastLon.getText();
	}
	public String getXlo() {
		return westLon.getText();
	}
}
