package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
/**
 * A widget that displays the N and S latitude and E and W longitude of the current selection.
 * @author rhs
 *
 */
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
	/**
	 * Constructs an empty widget.
	 */
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
	/**
	 * Sets the values of the N and S latitude and the E and W longitude, use a degenerate bounds when the tool type is a line or point.
	 * @param selectionBounds
	 */
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
	/**
	 * Get the East longitude value as a String formatted to 2 decimal places.
	 * @return xhi The current East longitude as a String formatted to 2 decimal places.
	 */
	public String getXhiFormatted() {
		return eastLon.getText();
	}
	/**
	 * Get the West longitude value as a String formatted to 2 decimal places.
	 * @return xlo The current West longitude as a String formatted to 2 decimal places.
	 */
	public String getXloFormatted() {
		return westLon.getText();
	}
	/**
	 * Get the South latitude value as a String formatted to 2 decimal places.
	 * @return ylo The current South latitude as a String formatted to 2 decimal places.
	 */
	public String getYloFormatted() {
		return southLat.getText();
	}
	/**
	 * Get the North latitude value as a String formatted to 2 decimal places.
	 * @return yhi The North latitude as a String formatted to 2 decimal places.
	 */
	public String getYhiFormatted() {
		return northLat.getText();
	}
	/**
	 * Get the current east longitude.
	 * @return xhi the current east longitude
	 */
	public double getXhi() {
		return xhi;
	}
	/**
	 * Get the current west longitude
	 * @return xlo the current west longitude
	 */
	public double getXlo() {
		return xlo;
	}
	/**
	 * Get the current north latitude
	 * @return yhi the current north latitude
	 */
	public double getYhi() {
		return yhi;
	}
	/**
	 * Get the current south latitude
	 * @return ylo the current south latitude
	 */
	public double getYlo() {
		return ylo;
	}
	/**
	 * Adds a listener to the south latitude TextBox.  Supplied by the ReferenceMap so it can update all the other widgets when the value changes.
	 * @param southChangeListener the listener for the south latitude TextBox
	 */
	public void addSouthChangeListener(ChangeListener southChangeListener) {
		southLat.addChangeListener(southChangeListener);
	}
	/**
	 * Adds a listener to the north latitude TextBox.  Supplied by the ReferenceMap so it can update all the other widgets when the value changes.
	 * @param northChangeListener the listener for the north latitude TextBox
	 */
	public void addNorthChangeListener(ChangeListener northChangeListener) {
		northLat.addChangeListener(northChangeListener);
	}
	/**
	 * Adds a listener to the east longitude TextBox.  Supplied by the ReferenceMap so it can update all the other widgets when the value changes.
	 * @param eastChangeListener the listener for the east longitude TextBox
	 */
	public void addEastChangeListener(ChangeListener eastChangeListener) {
		eastLon.addChangeListener(eastChangeListener);
	}
	/**
	 * Adds a listener to the west longitude TextBox.  Supplied by the ReferenceMap so it can update all the other widgets when the value changes.
	 * @param westChangeListener the listener for the west longitude TextBox
	 */
	public void addWestChangeListener(ChangeListener westChangeListener) {
		westLon.addChangeListener(westChangeListener);
	}
}
