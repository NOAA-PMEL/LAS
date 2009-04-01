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
	double xlo;
	double xhi;
	double ylo;
	double yhi;
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
		ylo = swPolyCorner.getLatitude();
		String slat_f;
		if ( ylo <= 0.0 ) {
			slat_f = latFormat.format(Math.abs(ylo))+" S";
		} else {
			slat_f = latFormat.format(ylo)+" N";
		}
		southLat.setText(slat_f);
		
		yhi = nePolyCorner.getLatitude();
		String nlat_f;
		if ( yhi <= 0.0 ) {
			nlat_f = latFormat.format(Math.abs(yhi))+" S";
		} else {
			nlat_f = latFormat.format(yhi)+" N";
		}
		northLat.setText(nlat_f);
		xlo = swPolyCorner.getLongitude();
		xhi = nePolyCorner.getLongitude();
		String wlon_f;
		String elon_f;
		if ( xlo < 0.0 ) {
			wlon_f = lonFormat.format(Math.abs(xlo))+" W";
		} else {
			wlon_f = lonFormat.format(xlo)+" E";
		}
		if ( xhi < 0.0 ) {
			elon_f = lonFormat.format(Math.abs(xhi))+" W";
		} else {
			elon_f = lonFormat.format(xhi)+" E";
		}


		westLon.setText(wlon_f);
		eastLon.setText(elon_f);
		
	}
	public String getXhiFormatted() {
		return eastLon.getText();
	}
	public String getXloFormatted() {
		return westLon.getText();
	}
	public String getYloFormatted() {
		return southLat.getText();
	}
	public String getYhiFormatted() {
		return northLat.getText();
	}
	public double getXhi() {
		return xhi;
	}
	public double getXlo() {
		return xlo;
	}
	public double getYhi() {
		return yhi;
	}
	public double getYlo() {
		return ylo;
	}
}
