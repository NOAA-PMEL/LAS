package gov.noaa.pmel.tmap.las.client.map;

import gov.noaa.pmel.tmap.las.client.laswidget.Util;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
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
	Image rose;
	FlexTable panel;
	
	private static final String boxWidth = "70px";
	/**
	 * Constructs an empty widget.
	 */
	public LatLonWidget() {
		panel = new FlexTable();
		
		rose = new Image(Util.getImageURL()+"rose.png");
		rose.addStyleName("LSS_middle");

		southLat = new TextBox();
		northLat = new TextBox();

		southLat.setWidth(boxWidth);
		northLat.setWidth(boxWidth);

		eastLon = new TextBox();
		westLon = new TextBox();

		eastLon.setWidth(boxWidth);
		westLon.setWidth(boxWidth);
        
		panel.setWidget(0, 1, northLat);		
		panel.setWidget(1, 0, westLon);
		panel.setWidget(1, 1, rose);
		panel.setWidget(1, 2, eastLon);
		panel.setWidget(2, 1, southLat);
//		panel.getFlexCellFormatter().setColSpan(2, 0, 2);
		
		initWidget(panel);
	}
	/**
	 * Sets the values of the N and S latitude and the E and W longitude, use a degenerate bounds when the tool type is a line or point.
	 * @param selectionBounds
	 */
	public void setText(double nLat, double sLat, double eLon, double wLon) {
		ylo = sLat;
		String slat_f = GeoUtil.compassLat(ylo);
		southLat.setText(slat_f);
		yhi = nLat;
		String nlat_f = GeoUtil.compassLat(yhi);
		northLat.setText(nlat_f);
		xlo = wLon;
		xhi = eLon;
		String wlon_f = GeoUtil.compassLon(GeoUtil.normalizeLon(xlo));
		String elon_f = GeoUtil.compassLon(GeoUtil.normalizeLon(xhi));
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
