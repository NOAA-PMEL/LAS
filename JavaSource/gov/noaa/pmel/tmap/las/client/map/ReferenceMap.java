package gov.noaa.pmel.tmap.las.client.map;

import org.apache.tools.ant.taskdefs.Sleep;

import gov.noaa.pmel.tmap.las.client.laswidget.Util;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.maps.client.MapType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.Control;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.SmallZoomControl;
import com.google.gwt.maps.client.event.MapZoomEndHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.GroundOverlay;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
/**
 * A Google MapWidget that allows selection of lat/lon  rectangles, lat and lon lines and points, displays
 * the current selection lat/lon, allows the selection to be restricted to a specific lat/lon bounds, centers the map
 * for continuous selection of any region, exposes a list of selectable named regions and allows the selection to be
 * edited by grab handles or text entry of new lat/lon coordinates.
 * 
 * For example:
	 <pre>
	     public class RefMap implements EntryPoint {
	         ReferenceMap refMap;
	         // Entry point to create and load a RefMap.
	         public void onModuleLoad() {
	             refMap = new ReferenceMap(LatLng.newInstance(0.0, 0.0), 0, 256, 360);
		         refMap.setDataBounds(LatLngBounds.newInstance(LatLng.newInstance(-89.5, -179.5), LatLng.newInstance(89.5, 179.5)), 1., true);
		         RootPanel.get("refMapContainer").add(refMap);
		     }
		 }
	 </pre>
 *
 * @author rhs
 *
 */
public class ReferenceMap extends Composite {
	DockPanel panel;
	Grid topControls;
	HorizontalPanel bottomControls;
	DataBoundsOverlay dataBoundsOverlay = null;
	SelectWidget selectWidget;
	LatLonWidget textWidget;
	ResetWidget resetWidget;
//	RotateWidget rotateWidget;
	CenterWidget centerWidget;
//	IconifyControl iconifyControl;
	SmallZoomControl smallZoomControl;
	MapTypeControl mapTypeControl;
	MapWidget mMap;
	RegionWidget regionWidget;
	LatLng mCenter;
	LatLngBounds dataBounds = LatLngBounds.newInstance(LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0));
	LatLngBounds moduloBounds = LatLngBounds.newInstance(LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0));
	int mZoom;
	int mWidth;   // Width in pixels 
	int mHeight;  // Height in pixels
	double margin_x;
	double margin_y;
	LatLng initial_map_center;
	double south_center;
	double north_center;
	double west_center;
	double east_center;
	double delta;
	boolean haveData = false;
	boolean modulo = false;
	
	LatLng modulo_center;
	
	String gridID = null;
	
	GroundOverlay topOverlay;
	GroundOverlay bottomOverlay;
	
	/**
	 * Construct an LASReference map centered and zoomed with the width and height specified in pixels.
	 * @param center the center of the map
	 * @param zoom the initial zoom level
	 * @param width the width in pixels
	 * @param height the height in pixels
	 
	 * 
	 * 
	 * 
	 */
	public ReferenceMap (LatLng center, int zoom, int width, int height, boolean allowEditing) {
        panel = new DockPanel();
        topControls = new Grid(2,2);
        bottomControls = new HorizontalPanel();
        bottomControls.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		mCenter = center;
		mZoom = zoom;
		mMap = new MapWidget(mCenter, mZoom);    	
		mMap.addMapZoomEndHandler(new MapZoomEndHandler() {

			public void onZoomEnd(MapZoomEndEvent event) {
				int nzoom = event.getNewZoomLevel();
				if ( nzoom == mZoom ) {
					mMap.setDraggable(false);
				} else if ( nzoom > mZoom ){
					mMap.setDraggable(true);
				} else {
					mMap.setDraggable(false);
				}
				
			}
		});
		mWidth = width;
		mHeight = height;
		mMap.setSize(String.valueOf(mWidth)+"px", String.valueOf(mHeight)+"px");
		selectWidget = new SelectWidget(this, allowEditing);
		textWidget = new LatLonWidget();
		textWidget.addSouthChangeListener(southChangeListener);
		textWidget.addNorthChangeListener(northChangeListener);
		textWidget.addEastChangeListener(eastChangeListener);
		textWidget.addWestChangeListener(westChangeListener);
		selectWidget.setLatLngWidget(textWidget);
		smallZoomControl = new SmallZoomControl();
		mMap.addControl(smallZoomControl);
		mMap.setCurrentMapType(MapType.getHybridMap());
		regionWidget = new RegionWidget(this);
		resetWidget = new ResetWidget(this);
		centerWidget = new CenterWidget(this);
		centerWidget.setVisible(false);
		topControls.setWidget(0, 0, regionWidget);
		topControls.setWidget(0, 1, selectWidget);
		topControls.setWidget(1, 0, centerWidget);
		topControls.setWidget(1, 1, resetWidget);
		bottomControls.add(textWidget);
		panel.add(topControls, DockPanel.NORTH);
		panel.add(bottomControls, DockPanel.SOUTH);
		panel.add(mMap, DockPanel.CENTER);
		
		initWidget(panel);
    }
	/**
	 * Add a map Control to the current map
	 * @param control the control to add
	 */
    public void addControl(Control control) {
    	mMap.addControl(control);
    }
    /**
     * Initializes the data bounds and sets the reset widget to return to these bounds when clicked.
     *
     * @param dataBounds
     * @param delta
     * @param selection
     */
    public void initDataBounds(LatLngBounds dataBounds, double delta, boolean selection) {
    	double east = dataBounds.getNorthEast().getLongitude();
    	double north = dataBounds.getNorthEast().getLatitude();
    	double west = dataBounds.getSouthWest().getLongitude();
    	double south = dataBounds.getSouthWest().getLatitude();
    	// Special kludge to get a full globe on a data set that repeats at 0 and 360.
    	if ( Math.abs(east - 0.0) < .0001 && Math.abs(west - 0.0) <= 0.0001 ) {
    		east = 359.;
    		dataBounds = LatLngBounds.newInstance(LatLng.newInstance(south, west), LatLng.newInstance(north, east));
    	}
    	setDataBounds(dataBounds, delta, true);
		getResetWidget().setSelectionBounds(dataBounds);
		getRegionWidget().setSelectedIndex(0);
		centerWidget.setData(dataBounds);
    }
    /**
     * Sets the data bounds, but leaves the reset values to their initial values.
     * @param dataBounds
     * @param delta
     * @param selection
     */
    public void setDataBounds(LatLngBounds dataBounds, double delta, boolean selection) {
    	modulo = false;
    	removeTopAndBottom();
    	this.dataBounds = dataBounds;
    	double lon_span = dataBounds.toSpan().getLongitude();
		if ( dataBounds.isFullLongitude() || lon_span + 2.*delta >= 360.0 ) {
			modulo = true;
			mMap.setDraggable(false);
		}
    	this.delta = delta;
    	// Set data selection rectangle to the data bounds
    	if ( selection ) {
    		if ( dataBoundsOverlay != null ) {
    			mMap.removeOverlay(dataBoundsOverlay.getPolygon());
    		}
    		dataBoundsOverlay = new DataBoundsOverlay(dataBounds);
    		if ( !modulo ) {
    		   mMap.addOverlay(dataBoundsOverlay.getPolygon());
    		}
    		if ( modulo ) {
    		    selectWidget.initSelectionBounds(dataBounds, dataBounds, false);
    		} else {
    			selectWidget.initSelectionBounds(dataBounds, dataBounds, true);
    		}
    		mZoom = mMap.getBoundsZoomLevel(dataBounds);
    		mMap.setZoomLevel(mZoom);
    		mMap.setCenter(dataBounds.getCenter());
    		if ( mZoom == 0 ) {
    			addTopAndBottom();
    		}
    	}
    	if ( modulo ) {
			centerWidget.setVisible(true);
		} else {
			centerWidget.setVisible(false);
		}
    }
	/**
	 * Does the map span the globe in longitude?
	 * @return true if the longitude spans the globe, false if it does not
	 */
	public boolean isModulo() {
		return modulo;
	}
	/**
	 * Sets the bounds of the current selection
	 * @param bounds the new selection bounds
	 * @param recenter whether to re-center the map at the center of the new selection
	 * @param show whether to show the outline of the new selection
	 */
	public void setSelectionBounds(LatLngBounds bounds, boolean recenter, boolean show) {
		selectWidget.setSelectionBounds(bounds, recenter, show);
	}
	/**
	 * Gets the current bounds of the valid data area
	 * @return the current LatLngBounds of the valid data area
	 */
	public LatLngBounds getDataBounds() {
		return dataBounds;
	}
	/**
	 * Get the deepest zoom level where the entire area of the specified bounds can still be seen @see {@link com.google.gwt.maps.client.MapWidget#getBoundsZoomLevel(LatLngBounds)}
	 * @param region the region to test
	 * @return the zoom level
	 */
	public int getBoundsZoomLevel(LatLngBounds region) {
		return mMap.getBoundsZoomLevel(region);
	}
	/**
	 * Sets the zoom level of the map @see {@link com.google.gwt.maps.client.MapWidget#setZoomLevel(int)}
	 * @param zoom
	 */
	public void setZoom(int zoom) {
		mMap.setZoomLevel(zoom);
	}
	/**
	 * Get the underlying {@link com.google.gwt.maps.client.MapWidget}
	 * @return the MapWidget
	 */
	public MapWidget getMapWidget() {
		return mMap;
	}
	/**
	 * Set the center of the map @see {@link com.google.gwt.maps.client.MapWidget#setCenter(LatLng)}
	 * @param center
	 */
	public void setCenter(LatLng center) {
		mMap.setCenter(center);
	}
	/**
	 * A convenience method to reach into the LatLonWidget and get the current west longitude.
	 * @return the west longitude formatted to 2 decimal places
	 */
	public String getXloFormatted() {
		
		return selectWidget.getXloFormatted();
		
	}
	/**
	 * A convenience method to reach into the LatLonWidget and get the current east longitude.
	 * @return the east longitude formatted to 2 decimal places
	 */
	public String getXhiFormatted() {
		
		return selectWidget.getXhiFormatted();
		
	}
	/**
	 * A convenience method to reach into the LatLonWidget and get the current north latitude
	 * @return the north latitude formatted to 2 decimal places
	 */
	public String getYloFormatted() {
		return selectWidget.getYloFormatted();
	}
	/**
	 * A convenience method to reach into the LatLonWidget and get the current south latitude
	 * @return the south latitude formatted to 2 decimal places
	 */
	public String getYhiFormatted() {
		return selectWidget.getYhiFormatted();
	}
	/**
	 * A convenience method to get the current west longitude
	 * @return xlo the current west longitude
	 */
	public double getXlo() {
		return selectWidget.getXlo();
	}
	/**
	 * A convenience method to get the current east longitude, guaranteed to be bigger than the current west longitude to disambiguate
	 * the selection for external clients
	 * @return xhi the current east longitude, xhi >= xlo guaranteed
	 */
	public double getXhi() {
		double xwest = selectWidget.getXlo();
		double xeast = selectWidget.getXhi();
		if ( xeast < xwest ) {
			xeast = xeast + 360.;
		}
		return xeast;
	}
	/**
	 * A convenience method to get the current north longitude
	 * @return yhi the current north longitude
	 */
	public double getYhi() {
		return selectWidget.getYhi();
	}
	/**
	 * A convenience method to get the current south longitude
	 * @return ylo the current south longitude
	 */
	public double getYlo() {
		return selectWidget.getYlo();
	}
	/**
	 * Gets the widget that controls the selection of the named regions
	 * @return regionWidget the widget that controls the selection of named regions
	 */
	public RegionWidget getRegionWidget() {
		return regionWidget;
	}
	/**
	 * Gets the current zoom level of the map
	 * @return zoom the zoom level
	 */
	public int getZoom() {
		return mZoom;
	}
	/**
	 * Gets the current delta, this is metadata our application supplies which we use to determine of a particular longitude range spans the globe.  You can use 1. everywhere.
	 * @return the spacing between lines of longitude in fractional degrees
	 */
	public double getDelta() {
		return delta;
	}
	/**
	 * Gets the widget that resets the map centered on the current data range with the full data range selected 
	 * @return the resetWidget for resetting the map centered on the current data range with the full data range selected
	 */
	public ResetWidget getResetWidget() {
		return resetWidget;
	}
	/**
	 * An attempt to overlay imagery above and below 85 degrees
	 */
	public void addTopAndBottom() {
		double wlon = dataBounds.getSouthWest().getLongitude();
		double elon = dataBounds.getNorthEast().getLongitude();
		if ( elon < wlon ) {
			elon = elon + 360.;
		}
		String imageURL = Util.getImageURL();
		LatLngBounds topBounds = LatLngBounds.newInstance(LatLng.newInstance(85., wlon), LatLng.newInstance(90., elon));
        topOverlay = new GroundOverlay(imageURL+"top.png", topBounds);
        mMap.addOverlay(topOverlay);
        LatLngBounds bottomBounds = LatLngBounds.newInstance(LatLng.newInstance(-90., dataBounds.getSouthWest().getLongitude()), LatLng.newInstance(-85., dataBounds.getNorthEast().getLongitude()));
        bottomOverlay = new GroundOverlay(imageURL+"top.png", bottomBounds);
        mMap.addOverlay(bottomOverlay);
	}
	public void removeTopAndBottom() {
		if ( topOverlay != null ) mMap.removeOverlay(topOverlay);
		if ( bottomOverlay != null ) mMap.removeOverlay(bottomOverlay);
	}
	/**
	 * Set the selection bounds from a indivdiual lat/lon values
	 * @param xlo the west longitude
	 * @param xhi the east longitude
	 * @param ylo the south latitude
	 * @param yhi the north latitude
	 */
	public void setLatLon(String xlo, String xhi, String ylo, String yhi) {
		LatLng sw = LatLng.newInstance(Double.valueOf(ylo), Double.valueOf(xlo));
		LatLng ne = LatLng.newInstance(Double.valueOf(yhi), Double.valueOf(xhi));
		// Special kludge for data that has a duplicate point at 0 and 360.
		double north = ne.getLatitude();
		double east = ne.getLongitude();
		double south = sw.getLatitude();
		double west = sw.getLongitude();
		LatLngBounds bounds;
		if ( Math.abs(east - 0.0) < .0001 && Math.abs(west - 0.0) <= 0.0001 ) {
			east = 359.;
			bounds = LatLngBounds.newInstance(LatLng.newInstance(south, west), LatLng.newInstance(north, east));
		} else {
			bounds = LatLngBounds.newInstance(sw, ne);
		}
		setSelectionBounds(bounds, true, true);
	}
	/**
	 * A convenience method to pass through a change listener for the region selection menu.  Convenient for
	 * applications that use the RefMap that need to react to changes in the region menu
	 * @param listener
	 */
	public void addRegionChangeListener(ChangeListener listener) {
		regionWidget.addChangeListener(listener);
	}
	/**
	 * A convenience method for setting the region selection to one of the named regions.  Used by our applications.
	 * @param i the index of the region in the list
	 * @param region the name of the region
	 */
	public void setRegion(int i, String region) {
		regionWidget.setRegion(i, region);	
	}
	
	/**
	 * Rotate the map center 90 degrees to the east.  Used for finding a center suitable for a named region
	 */
	public void rotateEast() {
		LatLng c_ne = dataBounds.getNorthEast();
		double east = c_ne.getLongitude() + 90.;
		LatLng c_sw = dataBounds.getSouthWest();
		double west = c_sw.getLongitude() + 90.;
		dataBounds = LatLngBounds.newInstance(LatLng.newInstance(c_sw.getLatitude(), west), LatLng.newInstance(c_ne.getLatitude(), east));
		setDataBounds(dataBounds, delta, true);
	}
	/**
	 * A convenience method to set the tool type based on the current data view (x, y, z, t, xy, xz, xt, ... , xyzt)
	 * @param view
	 */
	public void setToolType(String view) {
		selectWidget.setToolType(view);
	}
	/**
	 * A listener that will handle change events when the user types text into the TextBox with the south latitude value.
	 */
	public ChangeListener southChangeListener = new ChangeListener() {

		public void onChange(Widget sender) {
			TextBox tb = (TextBox) sender;
			double ylo = getYlo();
			double yhi = getYhi();
			String oentry = tb.getText();
			String entry = oentry.trim().toLowerCase();
			if ( entry.contains("s") && entry.contains("-") ) {
				Window.alert("Either use a minus sign or an \"S\" to denote southern latitudes, not both");
			} else if (entry.contains("n") && entry.contains("-")) {
				Window.alert("Either use a minus sign or an \"S\" to denote southern latitudes, not a minus sign and an \"N\"");
			} else if ( entry.contains("s") ) {
				entry = entry.substring(0, entry.indexOf("s"));
				try {
					ylo = Double.valueOf(entry.trim());
					ylo = -ylo;
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid latitude value.");
				}
			} else if (entry.contains("n") ) {
				entry = entry.substring(0, entry.indexOf("n"));
				try {
					ylo = Double.valueOf(entry.trim());
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid latitude value.");
				}
			} else {
				try {
					ylo = Double.valueOf(entry.trim());
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid latitude value.");
				}
			}
			if ( ylo < dataBounds.getSouthWest().getLatitude() ) {
				ylo = dataBounds.getSouthWest().getLatitude();
			}
			
			if ( selectWidget.getToolType().equals("xy") || selectWidget.getToolType().equals("y") ) {
				if ( ylo > yhi ) {
					ylo = yhi;
				}
				LatLng sw = LatLng.newInstance(ylo, selectWidget.getSelectionBounds().getSouthWest().getLongitude());
				LatLngBounds sBounds = LatLngBounds.newInstance(sw, selectWidget.getSelectionBounds().getNorthEast());
				if ( dataBounds.containsBounds(sBounds) ) {
				    selectWidget.setSelectionBounds(sBounds, false, true);
				} else {
					// put it back the way it was
					selectWidget.setSelectionBounds(selectWidget.getSelectionBounds(), false, true);
				}
			} else if ( selectWidget.getToolType().equals("x") || selectWidget.getToolType().equals("pt") ) {
				// The vertical location of the the horizontal selection line just moved.
				// Make the box as wide as the shortest distance from the the line to the data edge.
				double toTop = Math.abs(dataBounds.getNorthEast().getLatitude() - ylo);
				double toBottom = Math.abs(ylo -dataBounds.getSouthWest().getLatitude());
				double span = Math.min(toTop, toBottom);
				LatLng ne = LatLng.newInstance(ylo + span, selectWidget.getSelectionBounds().getNorthEast().getLongitude());
				LatLng sw = LatLng.newInstance(ylo - span, selectWidget.getSelectionBounds().getSouthWest().getLongitude());
				LatLngBounds sBounds = LatLngBounds.newInstance(sw, ne);
				if ( dataBounds.containsBounds(sBounds) ) {
					selectWidget.setSelectionBounds(sBounds, false, true);
				} else {
					// put it back the way it was
					selectWidget.setSelectionBounds(selectWidget.getSelectionBounds(), false, true);
				}
			}
		}
	};
	/**
	 * A listener that will handle change events when the user types text into the TextBox with the north latitude value.
	 */
	public ChangeListener northChangeListener = new ChangeListener() {

		public void onChange(Widget sender) {
			TextBox tb = (TextBox) sender;
			double ylo = getYlo();
			double yhi = getYhi();
			String oentry = tb.getText();
			String entry = oentry.trim().toLowerCase();
			if ( entry.contains("s") && entry.contains("-") ) {
				Window.alert("Either use a minus sign or an \"S\" to denote southern latitudes, not both");
			} else if (entry.contains("n") && entry.contains("-")) {
				Window.alert("Either use a minus sign or an \"S\" to denote southern latitudes, not a minus sign and an \"N\"");
			} else if ( entry.contains("s") ) {
				entry = entry.substring(0, entry.indexOf("s"));
				try {
					yhi = Double.valueOf(entry.trim());
					yhi = -yhi;
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid latitude value.");
				}
			} else if (entry.contains("n") ) {
				entry = entry.substring(0, entry.indexOf("n"));
				try {
					yhi = Double.valueOf(entry.trim());
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid latitude value.");
				}
			} else {
				try {
					yhi = Double.valueOf(entry.trim());
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid latitude value.");
				}
			}
			if ( yhi > dataBounds.getNorthEast().getLatitude() ) {
				yhi = dataBounds.getNorthEast().getLatitude();
			}
			if ( selectWidget.getToolType().equals("xy") || selectWidget.getToolType().equals("y") ) {
				if ( yhi < ylo ) {
					yhi = ylo;
				}
				LatLng ne = LatLng.newInstance(yhi, selectWidget.getSelectionBounds().getNorthEast().getLongitude());
				LatLngBounds sBounds = LatLngBounds.newInstance(selectWidget.getSelectionBounds().getSouthWest(), ne);
				selectWidget.setSelectionBounds(sBounds, false, true);
			} else if ( selectWidget.getToolType().equals("x") || selectWidget.getToolType().equals("pt") ) {
				double toTop = Math.abs(dataBounds.getNorthEast().getLatitude() - yhi);
				double toBottom = Math.abs(yhi -dataBounds.getSouthWest().getLatitude());
				double span = Math.min(toTop, toBottom);
				LatLng ne = LatLng.newInstance(yhi + span, selectWidget.getSelectionBounds().getNorthEast().getLongitude());
				LatLng sw = LatLng.newInstance(yhi - span, selectWidget.getSelectionBounds().getSouthWest().getLongitude());
				LatLngBounds sBounds = LatLngBounds.newInstance(sw, ne);
				selectWidget.setSelectionBounds(sBounds, false, true);
			}
		}
	};
	/**
	 * A listener that will handle change events when the user types text into the TextBox with the west longitude value.
	 */
	public ChangeListener westChangeListener = new ChangeListener() {

		public void onChange(Widget sender) {
			TextBox tb = (TextBox) sender;
			double xlo = getXlo();
			double xhi = getXhi();
			String oentry = tb.getText();
			String entry = oentry.trim().toLowerCase();
			if ( entry.contains("w") && entry.contains("-") ) {
				Window.alert("Either use a minus sign or an \"W\" to denote west longitudes, not both");
			} else if (entry.contains("e") && entry.contains("-")) {
				Window.alert("Either use a minus sign or an \"W\" to denote west longitudes, not a minus sign and an \"E\"");
			} else if ( entry.contains("w") ) {
				entry = entry.substring(0, entry.indexOf("w"));
				try {
					xlo = Double.valueOf(entry.trim());
					xlo = -xlo;
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid longitude value.");
				}
			} else if (entry.contains("e") ) {
				entry = entry.substring(0, entry.indexOf("e"));
				try {
					xlo = Double.valueOf(entry.trim());
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid longitude value.");
				}
			} else {
				try {
					xlo = Double.valueOf(entry.trim());
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid longitude value.");
				}
			}
			if ( xlo < dataBounds.getSouthWest().getLongitude() ) {
				xlo = dataBounds.getSouthWest().getLongitude();
			}
			if ( selectWidget.getToolType().equals("xy") || selectWidget.getToolType().equals("x") ) {
				if ( xlo > xhi ) {
					xlo = xhi;
				}
				LatLng sw = LatLng.newInstance(selectWidget.getSelectionBounds().getSouthWest().getLatitude(), xlo);
				LatLngBounds sBounds = LatLngBounds.newInstance(sw, selectWidget.getSelectionBounds().getNorthEast());
				selectWidget.setSelectionBounds(sBounds, false, true);
			} else if ( selectWidget.getToolType().equals("y") || selectWidget.getToolType().equals("pt") ) {
				double toEast = Math.abs(dataBounds.getNorthEast().getLongitude() - xlo);
				double toWest = Math.abs(xlo -dataBounds.getSouthWest().getLongitude());
				double span = Math.min(toEast, toWest);
				LatLng ne = LatLng.newInstance(selectWidget.getSelectionBounds().getNorthEast().getLatitude(), xlo + span);
				LatLng sw = LatLng.newInstance(selectWidget.getSelectionBounds().getSouthWest().getLatitude(), xlo - span);
				LatLngBounds sBounds = LatLngBounds.newInstance(sw, ne);
				selectWidget.setSelectionBounds(sBounds, false, true);
			}
		}
	};
	/**
	 * A listener that will handle change events when the user types text into the TextBox with the east longitude value.
	 */
	public ChangeListener eastChangeListener = new ChangeListener() {
		
		public void onChange(Widget sender) {
			TextBox tb = (TextBox) sender;
			double xlo = getXlo();
			double xhi = getXhi();
			String oentry = tb.getText();
			String entry = oentry.trim().toLowerCase();
			if ( entry.contains("w") && entry.contains("-") ) {
				Window.alert("Either use a minus sign or an \"W\" to denote west longitudes, not both");
			} else if (entry.contains("e") && entry.contains("-")) {
				Window.alert("Either use a minus sign or an \"W\" to denote west longitudes, not a minus sign and an \"E\"");
			} else if ( entry.contains("w") ) {
				entry = entry.substring(0, entry.indexOf("w"));
				try {
					xhi = Double.valueOf(entry.trim());
					xhi = -xhi;
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid longitude value.");
				}
			} else if (entry.contains("e") ) {
				entry = entry.substring(0, entry.indexOf("e"));
				try {
					xhi = Double.valueOf(entry.trim());
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid longitude value.");
				}
			} else {
				try {
					xhi = Double.valueOf(entry.trim());
				} catch (Exception e) {
					Window.alert(oentry+" is not a valid longitude value.");
				}
			}
			if ( xhi > dataBounds.getNorthEast().getLongitude() ) {
				xhi = dataBounds.getNorthEast().getLongitude();
			}
			if ( selectWidget.getToolType().equals("xy") || selectWidget.getToolType().equals("x") ) {
				if ( xhi < xlo ) {
					xhi = xhi + 360;
				}
				LatLng ne = LatLng.newInstance(selectWidget.getSelectionBounds().getNorthEast().getLatitude(), xhi);
				LatLngBounds sBounds = LatLngBounds.newInstance(selectWidget.getSelectionBounds().getSouthWest(), ne);
				selectWidget.setSelectionBounds(sBounds, false, true);
			} else if ( selectWidget.getToolType().equals("y") || selectWidget.getToolType().equals("pt") ) {
				double toEast = Math.abs(dataBounds.getNorthEast().getLongitude() - xhi);
				double toWest = Math.abs(xhi -dataBounds.getSouthWest().getLongitude());
				double span = Math.min(toEast, toWest);
				LatLng ne = LatLng.newInstance(selectWidget.getSelectionBounds().getNorthEast().getLatitude(), xhi + span);
				LatLng sw = LatLng.newInstance(selectWidget.getSelectionBounds().getSouthWest().getLatitude(), xhi - span);
				LatLngBounds sBounds = LatLngBounds.newInstance(sw, ne);
				selectWidget.setSelectionBounds(sBounds, false, true);
			}
			
		}
	};

	public void turnOffSelectButton() {
		selectWidget.turnOffSelectButton();	
	}
}
