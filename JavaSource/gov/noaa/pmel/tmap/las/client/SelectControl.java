package gov.noaa.pmel.tmap.las.client;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.Control.CustomControl;
import com.google.gwt.maps.client.event.MapMouseMoveHandler;
import com.google.gwt.maps.client.event.MarkerDragEndHandler;
import com.google.gwt.maps.client.event.MarkerDragHandler;
import com.google.gwt.maps.client.event.MarkerMouseDownHandler;
import com.google.gwt.maps.client.event.MarkerMouseUpHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polygon;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class SelectControl extends CustomControl {
	private MapWidget mMap;
	private Button selectAll;
	private ToggleButton mSelect;
	private MapTool mSelection;
	private Marker mDrawMarker;
	private MarkerOptions mOptions;
	private Icon mIcon;
	boolean mDraw = false;
	private LatLng dataSW;
	private LatLng dataNE;
	private LatLng rectSW;
	private LatLng rectNE;
	private Grid controls;
	private String gridID = null;
	private boolean modulo;
	TextBox southLat;
	TextBox northLat;
	TextBox westLon;
	TextBox eastLon;
	Label lonLabel;
	Label latLabel;
	NumberFormat latFormat;
	NumberFormat lonFormat;

	double span;
	double delta;
	
	private static final String boxWidth = "45px";
	public SelectControl (ControlPosition position) {
		super(position);

	}
	@Override
	protected Widget initialize(final MapWidget map) {
		String moduleRelativeURL = GWT.getModuleBaseURL();
        String moduleName = GWT.getModuleName();
        moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.indexOf(moduleName)-1);
        moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.lastIndexOf("/")+1);
        String imageURL = moduleRelativeURL + "images/";
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
    	
    	latFormat = NumberFormat.getFormat("###.##");
    	lonFormat = NumberFormat.getFormat("####.##");
		mMap = map;
		controls = new Grid(8,1);
		LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0));
		mSelection = new MapTool(mMap, LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0),"xy", false);
		mIcon = Icon.newInstance();
		mIcon.setIconSize(Size.newInstance(20, 20));
		mIcon.setIconAnchor(Point.newInstance(10, 10));
		mIcon.setImageURL(imageURL+"crosshairs.png");
		mOptions = MarkerOptions.newInstance();
        mOptions.setIcon(mIcon);
		mOptions.setDraggable(true);
		mOptions.setDragCrossMove(true);
		mDrawMarker = new Marker(LatLng.newInstance(0.0, 0.0), mOptions);
		mSelection.setVisible(false);
		mDrawMarker.addMarkerMouseDownHandler(new MarkerMouseDownHandler() {
			public void onMouseDown(MarkerMouseDownEvent event) {
				mDraw = true;
				LatLng click = mDrawMarker.getLatLng();
				LatLngBounds bounds = LatLngBounds.newInstance(click, click);
				mSelection.setEditingEnabled(false);
				mMap.removeOverlay(mSelection.getPolygon());
				for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
					Marker marker = (Marker) markerIt.next();
					mMap.removeOverlay(marker);
				}
				mSelection = new MapTool(mMap, dataSW, dataNE, rectSW, rectNE, "xy", modulo);	
				mSelection.setClick(click);
				mMap.addOverlay(mSelection.getPolygon());
				setText();
				for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
					Marker marker = (Marker) markerIt.next();
					mMap.addOverlay(marker);
				}
				mSelection.setEditingEnabled(true);
                mSelection.setVisible(true);	
			}
		});
		mDrawMarker.addMarkerMouseUpHandler(new MarkerMouseUpHandler() {

			public void onMouseUp(MarkerMouseUpEvent event) {
				mSelect.setDown(false);
				mDrawMarker.setVisible(false);
	            mMap.removeMapMouseMoveHandler(mouseMove);
	            mDraw = false;
	            mMap.setDraggable(false); // was true
	            mSelection.setEditingEnabled(true);
			}
			
		});
		mSelect = new ToggleButton("Select");
		mSelect.addStyleName("map-button");
		mSelect.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				if ( gridID == null ) {
					Window.alert("Please select a data set and variable.");
					mSelect.setDown(false);
				} else {
					if (mSelect.isDown()) {
						mSelection.setEditingEnabled(false);
						mMap.removeOverlay(mSelection.getPolygon());
						mMap.setDraggable(false);
						mMap.addMapMouseMoveHandler(mouseMove);
						mDrawMarker.setVisible(true);
					} else {	          
						mDrawMarker.setVisible(false);
						//mMap.removeMapMouseMoveHandler(mouseMove);		           
					}
				}
			}
		});
		mMap.addOverlay(mDrawMarker);
		mDrawMarker.setVisible(false);
		mMap.addOverlay(mSelection.getPolygon());
		setText();
		for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
			Marker marker = (Marker) markerIt.next();
			mMap.addOverlay(marker);
		}
		mSelection.setEditingEnabled(false);
		selectAll = new Button("Select All");
		selectAll.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				if ( gridID == null ) {
					Window.alert("Please select a data set and variable.");
				} else {
					mSelection.setEditingEnabled(false);
					mMap.removeOverlay(mSelection.getPolygon());
					mSelection = new MapTool(mMap, dataSW, dataNE, dataSW, dataNE, "xy", modulo);
					mMap.addOverlay(mSelection.getPolygon());
					mMap.setCenter(mSelection.getCenterMarker().getLatLng());
					mMap.setZoomLevel(mMap.getBoundsZoomLevel(mSelection.getPolygon().getBounds()));
					setText();
					for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
						Marker marker = (Marker) markerIt.next();
						mMap.addOverlay(marker);
					}
					mSelection.setEditingEnabled(true);
					mSelection.setVisible(true);
				}
			}
			
		});
		controls.setWidget(0, 0, mSelect);
		controls.setWidget(1, 0, selectAll);
    	controls.setWidget(2, 0, latLabel);
    	controls.setWidget(3, 0, northLat);
    	controls.setWidget(4, 0, southLat);
    	controls.setWidget(5, 0, lonLabel);
    	controls.setWidget(6, 0, westLon);
    	controls.setWidget(7, 0, eastLon);
		return controls;
	}
	MapMouseMoveHandler mouseMove = new MapMouseMoveHandler() {

		public void onMouseMove(MapMouseMoveEvent event) {
			LatLng position = event.getLatLng();
			LatLngBounds dataBounds = LatLngBounds.newInstance(dataSW, dataNE);
			if ( dataBounds.containsLatLng(position)) {
				mDrawMarker.setVisible(true);
				mDrawMarker.setLatLng(position);
				if ( mDraw ) {
					mSelection.setEditingEnabled(false);
					mMap.removeOverlay(mSelection.getPolygon());
					mSelection.update(position);
					mMap.addOverlay(mSelection.getPolygon());
					setText();
					mSelection.setEditingEnabled(true);
					mSelection.setVisible(true);	
				}
			} else {
				mDrawMarker.setVisible(false);
			}
		}

	};
	@Override
	public boolean isSelectable() {
		// TODO Auto-generated method stub
		return false;
	}
    public boolean isDown() {
    	return mSelect.isDown();
    }
	
	/**
	 * @param dataBounds the dataBounds to set
	 */
	public void setDataBounds(LatLng sw, LatLng ne, boolean modulo, double delta) {
		
		mSelection.setEditingEnabled(false);
		mMap.removeOverlay(mSelection.getPolygon());
		dataSW = sw;
		dataNE = ne;
		rectSW = sw;
		rectNE = ne;
		this.modulo = modulo;
		mSelection = new MapTool(mMap, sw, ne, sw, ne, "xy", modulo);
		mMap.addOverlay(mSelection.getPolygon());
		setText();
		for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
			Marker marker = (Marker) markerIt.next();
			mMap.addOverlay(marker);
		}
		mSelection.setEditingEnabled(true);
		mSelection.setVisible(true);	
		LatLngBounds dataBounds = LatLngBounds.newInstance(dataSW, dataNE);
		span = dataBounds.toSpan().getLongitude();
		this.delta = delta;
	}
	public void setText() {	
		LatLng swPolyCorner = mSelection.getSouthWest();
		LatLng nePolyCorner = mSelection.getNorthEast();
		southLat.setText(latFormat.format(swPolyCorner.getLatitude()));
		northLat.setText(latFormat.format(nePolyCorner.getLatitude()));
		double wlon = swPolyCorner.getLongitude();
		double elon = nePolyCorner.getLongitude();
		while ( wlon < 0.0 ) {
			wlon = wlon + 360.;
		}
		while ( elon < 0.0 ) {
			elon = elon + 360;
		}
		
		
		String wlon_f = lonFormat.format(wlon);
		String elon_f = lonFormat.format(elon);
        
		westLon.setText(wlon_f);
		eastLon.setText(elon_f);
	}
	public void clearOverlays() {
		if ( mSelection != null ) {
			mMap.removeOverlay(mSelection.getPolygon());
			for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
				Marker marker = (Marker) markerIt.next();
				mMap.removeOverlay(marker);
			}	
		}
		
	}
	public void setGridID(String id) {
		gridID = id;		
	}
	public class MapTool {
		String type;
		LatLng[] polygonPoints;
		Polygon polygon;
		LatLng dataSW;
		LatLng dataNE;
		LatLng rectSW;
		LatLng rectNE;
		LatLng click;
		String strokeColor = "#FF0000";
		int strokeWeight = 3;
		float strokeOpacity = 1.0f;
		String fillColor = "#FF0000";
		float fillOpacity = 0.0f;
		Marker swMarker;
		Marker sw_nwMarker;
		Marker nwMarker;
		Marker nw_neMarker;
		Marker neMarker;
		Marker ne_seMarker;
		Marker seMarker;
		Marker sw_seMarker;
		Marker centerMarker;
		ArrayList<Marker> markers;
		MapWidget mMap;
		boolean modulo;
		/**
		 * Construct a marker tool with the default colors, weights and opacities.
		 * @param bounds
		 * @param type
		 */
		public MapTool(MapWidget map, LatLng sw_in, LatLng ne_in, LatLng rectSW, LatLng rectNE, String type, boolean modulo) {
			
			this.mMap = map;
			this.dataSW = sw_in;
			this.dataNE = ne_in;
		    this.rectSW = rectSW;
		    this.rectNE = rectNE;
			this.type = type;
			this.markers = new ArrayList<Marker>();
			this.modulo = modulo;
			
			String moduleRelativeURL = GWT.getModuleBaseURL();
	        String moduleName = GWT.getModuleName();
	        moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.indexOf(moduleName)-1);
	        moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.lastIndexOf("/")+1);
	        String imageURL = moduleRelativeURL + "images/";
			
			if ( type.equals("xy") ) {
             
				LatLng center = LatLngBounds.newInstance(rectSW, rectNE).getCenter();
				// Build the 8 LatLng instances, starting with the SW corner and enforce the constraint that the
				// East longitude always be greater than the West longitude and that the instances not be scaled.
				double center_lon = center.getLongitude();
				if ( center_lon < 0 ) {
					center_lon = center_lon + 360;
				}
				// SW corner...
				LatLng sw = sw_in;
				// West side...
				LatLng sw_nw = LatLng.newInstance(center.getLatitude(), rectSW.getLongitude(), true);
				// NW corner...
				LatLng nw = LatLng.newInstance(rectNE.getLatitude(), rectSW.getLongitude(), true);
				// North side...
				LatLng nw_ne = LatLng.newInstance(rectNE.getLatitude(), center_lon, true);
				// NE corner...
				LatLng ne = ne_in;
				double east_lon = ne.getLongitude();
				
				ne = LatLng.newInstance(ne.getLatitude(), east_lon, true);
				// East side...
				LatLng ne_se = LatLng.newInstance(center.getLatitude(), east_lon, true);
				// SE corner...
				LatLng se = LatLng.newInstance(rectSW.getLatitude(), east_lon, true);
				// South side...
				LatLng sw_se = LatLng.newInstance(rectSW.getLatitude(), center_lon, true);
				
				Icon sw_icon = Icon.newInstance();
				sw_icon.setIconSize(Size.newInstance(12, 12));
				sw_icon.setIconAnchor(Point.newInstance(5, 5)); 
				sw_icon.setImageURL(imageURL+"edit_square.png");
				MarkerOptions sw_options = MarkerOptions.newInstance();
		        sw_options.setIcon(sw_icon);
				sw_options.setDraggable(true);
				sw_options.setDragCrossMove(true);
				sw_options.setBouncy(false);
				sw_options.setTitle("sw");
				swMarker = new Marker(rectSW, sw_options);
				markers.add(swMarker);
				
				Icon sw_nw_icon = Icon.newInstance();
				sw_nw_icon.setIconSize(Size.newInstance(12, 12));
				sw_nw_icon.setIconAnchor(Point.newInstance(5, 5));
				sw_nw_icon.setImageURL(imageURL+"edit_square.png");
				MarkerOptions sw_nw_options = MarkerOptions.newInstance();
		        sw_nw_options.setIcon(sw_nw_icon);
		        sw_nw_options.setDraggable(true);
		        sw_nw_options.setDragCrossMove(true);
		        sw_nw_options.setBouncy(false);
		        sw_nw_options.setTitle("sw_nw");
				sw_nwMarker = new Marker(sw_nw, sw_nw_options);
				markers.add(sw_nwMarker);
				
				Icon nw_icon = Icon.newInstance();
				nw_icon.setIconSize(Size.newInstance(12, 12));
				nw_icon.setIconAnchor(Point.newInstance(5, 5));
				nw_icon.setImageURL(imageURL+"edit_square.png");
				MarkerOptions nw_options = MarkerOptions.newInstance();
				nw_options.setIcon(nw_icon);
				nw_options.setDraggable(true);
				nw_options.setDragCrossMove(true);
				nw_options.setBouncy(false);
				nw_options.setTitle("nw");
				nwMarker = new Marker(nw, nw_options);
				markers.add(nwMarker);
				
				Icon nw_ne_icon = Icon.newInstance();
				nw_ne_icon.setIconSize(Size.newInstance(12, 12));
				nw_ne_icon.setIconAnchor(Point.newInstance(5, 5));
				nw_ne_icon.setImageURL(imageURL+"edit_square.png");
				MarkerOptions nw_ne_options = MarkerOptions.newInstance();
				nw_ne_options.setIcon(nw_ne_icon);
				nw_ne_options.setDraggable(true);
				nw_ne_options.setDragCrossMove(true);
				nw_ne_options.setBouncy(false);
				nw_ne_options.setTitle("nw_ne");
				nw_neMarker = new Marker(nw_ne, nw_ne_options);
				markers.add(nw_neMarker);
				
				Icon ne_icon = Icon.newInstance();
				ne_icon.setIconSize(Size.newInstance(12, 12));
				ne_icon.setIconAnchor(Point.newInstance(5, 5));
				ne_icon.setImageURL(imageURL+"edit_square.png");
				MarkerOptions ne_options = MarkerOptions.newInstance();
				ne_options.setIcon(ne_icon);
				ne_options.setDraggable(true);
				ne_options.setDragCrossMove(true);
				ne_options.setBouncy(false);
				ne_options.setTitle("ne");
				neMarker = new Marker(ne, ne_options);
				markers.add(neMarker);
				
				Icon ne_se_icon = Icon.newInstance();
				ne_se_icon.setIconSize(Size.newInstance(12, 12));
				ne_se_icon.setIconAnchor(Point.newInstance(5, 5));
				ne_se_icon.setImageURL(imageURL+"edit_square.png");
				MarkerOptions ne_se_options = MarkerOptions.newInstance();
				ne_se_options.setIcon(ne_se_icon);
				ne_se_options.setDraggable(true);
				ne_se_options.setDragCrossMove(true);
				ne_se_options.setBouncy(false);
				ne_se_options.setTitle("ne_se");
				ne_seMarker = new Marker(ne_se, ne_se_options);
				markers.add(ne_seMarker);
				
				Icon se_icon = Icon.newInstance();
				se_icon.setIconSize(Size.newInstance(12, 12));
				se_icon.setIconAnchor(Point.newInstance(5, 5));
				se_icon.setImageURL(imageURL+"edit_square.png");
				MarkerOptions se_options = MarkerOptions.newInstance();
				se_options.setIcon(se_icon);
				se_options.setDraggable(true);
				se_options.setDragCrossMove(true);
				se_options.setBouncy(false);
				se_options.setTitle("se");
				seMarker = new Marker(se, se_options);
				markers.add(seMarker);
				
				Icon sw_se_icon = Icon.newInstance();
				sw_se_icon.setIconSize(Size.newInstance(12, 12));
				sw_se_icon.setIconAnchor(Point.newInstance(5, 5));
				sw_se_icon.setImageURL(imageURL+"edit_square.png");
				MarkerOptions sw_se_options = MarkerOptions.newInstance();
				sw_se_options.setIcon(sw_se_icon);
				sw_se_options.setDraggable(true);
				sw_se_options.setDragCrossMove(true);
				sw_se_options.setBouncy(false);
				sw_se_options.setTitle("sw_se");
				sw_seMarker = new Marker(sw_se, sw_se_options);
				markers.add(sw_seMarker);
				
				Icon center_icon = Icon.newInstance();
				center_icon.setIconSize(Size.newInstance(12, 12));
				center_icon.setIconAnchor(Point.newInstance(5, 5));
				center_icon.setImageURL(imageURL+"edit_square.png");
				MarkerOptions center_options = MarkerOptions.newInstance();
				center_options.setIcon(center_icon);
				center_options.setDraggable(true);
				center_options.setDragCrossMove(true);
				center_options.setBouncy(false);
				center_options.setTitle("center");
				centerMarker = new Marker(center, center_options);
				markers.add(centerMarker);
				
				
				polygonPoints = new LatLng[9];
				polygonPoints[0] = sw;
				polygonPoints[1] = sw_nw;
				polygonPoints[2] = nw;
				polygonPoints[3] = nw_ne;
				polygonPoints[4] = ne;
				polygonPoints[5] = ne_se;
				polygonPoints[6] = se;
				polygonPoints[7] = sw_se;
				polygonPoints[8] = sw;
				polygon = new Polygon(polygonPoints, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity);	
			    addDragHandlers(markerDragHandler);
			}
			
		}

		public void setVisible(boolean visible) {
			polygon.setVisible(visible);		
		}
		/**
		 * @return the polygon
		 */
		public Polygon getPolygon() {
			return polygon;
		}
		public void setEditingEnabled(boolean b) {
			for (Iterator markerIt = markers.iterator(); markerIt.hasNext();) {
				Marker marker = (Marker) markerIt.next();
				marker.setVisible(b);
			}
		}
		/**
		 * @return the markers
		 */
		public ArrayList<Marker> getMarkers() {
			return markers;
		}
		
		public void update(LatLng position) {
			// Figure out where the drag marker is relative to where the click was made.
			double posLon = position.getLongitude();
			double posLat = position.getLatitude();
			double clickLon = click.getLongitude();
			double clickLat = click.getLatitude();
			
			while ( posLon <= 360. ) {
				posLon = posLon + 360.;
			}
			while ( clickLon <= 360. ) {
				clickLon = clickLon + 360.;
			}
						
			double west_lon;
			double east_lon;
			double north_lat;
			double south_lat;
			if ( posLat > clickLat && posLon > clickLon ) {
				
				east_lon = posLon;
				north_lat = posLat;
				
				west_lon = clickLon;
				south_lat = clickLat;
				
			} else if ( posLat <= clickLat && posLon <= clickLon ) {
				
				east_lon = clickLon;
				north_lat = clickLat;
				
				west_lon = posLon;
				south_lat = posLat;
				
			} else if ( posLat > clickLat && posLon <= clickLon ) {
				
				east_lon = clickLon;
				north_lat = posLat;
				
				west_lon = posLon;
				south_lat = clickLat;
				
			} else {
				
				east_lon = posLon;
				north_lat = clickLat;
				
				west_lon = clickLon;
				south_lat = posLat;

			}
			
			if ( west_lon > east_lon ) {
				east_lon = east_lon + 360.;
			}
			LatLng sw = LatLng.newInstance(south_lat, west_lon);
			LatLng ne = LatLng.newInstance(north_lat, east_lon);
			LatLngBounds rectBounds = LatLngBounds.newInstance(sw, ne);
			polygonPoints[0] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
			polygonPoints[1] = LatLng.newInstance(rectBounds.getCenter().getLatitude(), sw.getLongitude());
			polygonPoints[2] = LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
			polygonPoints[3] = LatLng.newInstance(ne.getLatitude(), rectBounds.getCenter().getLongitude());
			polygonPoints[4] = LatLng.newInstance(ne.getLatitude(), ne.getLongitude());
			polygonPoints[5] = LatLng.newInstance(rectBounds.getCenter().getLatitude(), ne.getLongitude());
			polygonPoints[6] = LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
			polygonPoints[7] = LatLng.newInstance(sw.getLatitude(), rectBounds.getCenter().getLongitude());
			polygonPoints[8] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
			polygon = new Polygon(polygonPoints, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity);
			LatLngBounds bounds = polygon.getBounds();
			LatLng center = bounds.getCenter();
			double center_lon = center.getLongitude();
			if ( center_lon < 0.0 ) {
				center_lon = center_lon + 360.;
			}
			swMarker.setLatLng(sw);
			sw_nwMarker.setLatLng(LatLng.newInstance(center.getLatitude(), sw.getLongitude()));
			nwMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), sw.getLongitude()));
			nw_neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), center_lon));
			neMarker.setLatLng(ne);
			ne_seMarker.setLatLng(LatLng.newInstance(center.getLatitude(), ne.getLongitude()));
			seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), ne.getLongitude()));
			sw_seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), center_lon));
			centerMarker.setLatLng(center);
		}
		/**
		 * @param click the click to set
		 */
		public void setClick(LatLng click) {
			this.click = click;
		}
		
		public LatLng getSouthWest() {
			return polygonPoints[0];
		}
		public LatLng getNorthEast() {
			return polygonPoints[4];
		}

		public LatLng[] getPolygonPoints() {
			return polygonPoints;
		}

		/**
		 * @return the swMarker
		 */
		public Marker getSwMarker() {
			return swMarker;
		}

		/**
		 * @return the sw_nwMarker
		 */
		public Marker getSw_nwMarker() {
			return sw_nwMarker;
		}

		/**
		 * @return the nwMarker
		 */
		public Marker getNwMarker() {
			return nwMarker;
		}

		/**
		 * @return the nw_neMarker
		 */
		public Marker getNw_neMarker() {
			return nw_neMarker;
		}

		/**
		 * @return the neMarker
		 */
		public Marker getNeMarker() {
			return neMarker;
		}

		/**
		 * @return the ne_seMarker
		 */
		public Marker getNe_seMarker() {
			return ne_seMarker;
		}

		/**
		 * @return the sw_seMarker
		 */
		public Marker getSw_seMarker() {
			return sw_seMarker;
		}

		/**
		 * @return the centerMarker
		 */
		public Marker getCenterMarker() {
			return centerMarker;
		}

		/**
		 * @return the seMarker
		 */
		public Marker getSeMarker() {
			return seMarker;
		}

		public void addDragHandlers(MarkerDragHandler markerDragHandler) {
			swMarker.addMarkerDragHandler(markerDragHandler);
			sw_nwMarker.addMarkerDragHandler(markerDragHandler);
			nwMarker.addMarkerDragHandler(markerDragHandler);
			nw_neMarker.addMarkerDragHandler(markerDragHandler);
			neMarker.addMarkerDragHandler(markerDragHandler);
			ne_seMarker.addMarkerDragHandler(markerDragHandler);
			seMarker.addMarkerDragHandler(markerDragHandler);
			sw_seMarker.addMarkerDragHandler(markerDragHandler);
			centerMarker.addMarkerDragHandler(markerDragHandler);
		}
		public void addDragEndHandlers(MarkerDragEndHandler markerDragHandler) {
			swMarker.addMarkerDragEndHandler(markerDragHandler);
			sw_nwMarker.addMarkerDragEndHandler(markerDragHandler);
			nwMarker.addMarkerDragEndHandler(markerDragHandler);
			nw_neMarker.addMarkerDragEndHandler(markerDragHandler);
			neMarker.addMarkerDragEndHandler(markerDragHandler);
			ne_seMarker.addMarkerDragEndHandler(markerDragHandler);
			seMarker.addMarkerDragEndHandler(markerDragHandler);
			sw_seMarker.addMarkerDragEndHandler(markerDragHandler);
			centerMarker.addMarkerDragEndHandler(markerDragHandler);
		}
		public void addMarkerMouseUpHandler(MarkerMouseUpHandler markerMouseUpHandler) {
			swMarker.addMarkerMouseUpHandler(markerMouseUpHandler);
			sw_nwMarker.addMarkerMouseUpHandler(markerMouseUpHandler);
			nwMarker.addMarkerMouseUpHandler(markerMouseUpHandler);
			nw_neMarker.addMarkerMouseUpHandler(markerMouseUpHandler);
			neMarker.addMarkerMouseUpHandler(markerMouseUpHandler);
			ne_seMarker.addMarkerMouseUpHandler(markerMouseUpHandler);
			seMarker.addMarkerMouseUpHandler(markerMouseUpHandler);
			sw_seMarker.addMarkerMouseUpHandler(markerMouseUpHandler);
			centerMarker.addMarkerMouseUpHandler(markerMouseUpHandler);
		}
		
		MarkerDragHandler markerDragHandler = new MarkerDragHandler() {
			public void onDrag(MarkerDragEvent event) {
				LatLngBounds dataBounds = LatLngBounds.newInstance(dataSW, dataNE);
				mMap.setCenter(dataBounds.getCenter());
				Marker marker = event.getSender();
				String title = marker.getTitle();
				LatLng markerLocation = marker.getLatLng();
				double markerLon = markerLocation.getLongitude();
				double markerLat = markerLocation.getLatitude();
				
				while ( markerLon < 0.0 ) {
					markerLon = markerLon + 360.;
				}
				
				double westBoundsLon;
				double eastBoundsLon;
				double southBoundsLat;
				double northBoundsLat;
				
				double south_lat = polygonPoints[0].getLatitude();
				double west_lon = polygonPoints[0].getLongitude();
				while ( west_lon < 0.0 ) {
					west_lon = west_lon + 360.;
				}
	           
	            double north_lat = polygonPoints[4].getLatitude();
	            double east_lon = polygonPoints[4].getLongitude();
	            while ( east_lon < 0.0 ) {
	            	east_lon = east_lon + 360.;
	            }
	            
	            LatLng center = mMap.getCenter();
        		double center_lon = center.getLongitude();

        		if ( title.equals("sw") ) {
        			// The south west marker's movements are bounded by the south west data bounds and the north east rectangle bounds.

        			southBoundsLat = dataSW.getLatitude();
        			northBoundsLat = north_lat;

        			westBoundsLon = dataSW.getLongitude();
        			eastBoundsLon = east_lon;

        			while ( westBoundsLon < 0.0 ) {
        				westBoundsLon = westBoundsLon + 360.;
        			}
        			while (eastBoundsLon < 0.0 ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        			}
        			double dataEast = dataNE.getLongitude();
        			while ( dataEast < 0.0 ) {
        				dataEast = dataEast + 360.;
        			}
        			if ( modulo && eastBoundsLon < westBoundsLon ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        				dataEast = dataEast + 360.;
        			}
        			
        			// Compute whether or not the east selection boundary has been moved.
        		
        			double delta = Math.abs(dataEast - eastBoundsLon);
        			// If it's not global or if boundary has moved (delta > "zero") then
        			// freeze the movement on the east side.
        			
        			if ( !modulo || (modulo && (delta > .1)) ) {
        				if ( markerLon >= eastBoundsLon ) {
        					markerLon = eastBoundsLon;
        				}
        				if ( markerLon <= westBoundsLon ) {
        					markerLon = westBoundsLon;
        				}
        				if (markerLat < southBoundsLat ) {
        					markerLat = southBoundsLat;
        				}
        				if ( markerLat >= northBoundsLat ) {
        					markerLat = northBoundsLat;
        				}
        			}
        			south_lat = markerLat;
        			west_lon = markerLon;
        		} else if ( title.equals("sw_nw") )  {
        			// The west line marker's movements are bounded by it's current latitude and the west data bounds and the east
        			// rectangle bounds.

        			markerLat = polygon.getBounds().getCenter().getLatitude();

        			westBoundsLon = dataSW.getLongitude();
        			eastBoundsLon = east_lon;

        			while ( westBoundsLon < 0.0 ) {
        				westBoundsLon = westBoundsLon + 360.;
        			}
        			while (eastBoundsLon < 0.0 ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        			}
        			// Compute whether or not the east selection boundary has been moved.
        			double dataEast = dataNE.getLongitude();
        			while ( dataEast < 0.0 ) {
        				dataEast = dataEast + 360.;
        			}
        			if ( modulo && eastBoundsLon < westBoundsLon ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        				dataEast = dataEast + 360.;
        			}
     
        			double delta = Math.abs(dataEast - eastBoundsLon);
        			// If it's not global or if boundary has moved (delta > "zero") then
        			// freeze the movement on the east side.

        			if ( !modulo || (modulo && (delta > .1)) ) {
        				if ( markerLon >= eastBoundsLon ) {
        					markerLon = eastBoundsLon;
        				}
        				if ( markerLon <= westBoundsLon ) {
        					markerLon = westBoundsLon;
        				}
        			}
        			west_lon = markerLon;
        		} else if ( title.equals("nw") ) {
        			// The north west corner is bounded by the west and north data bounds and the south east rectangle corner.

        			northBoundsLat = dataNE.getLatitude();
        			southBoundsLat = south_lat;

        			westBoundsLon = dataSW.getLongitude();
        			eastBoundsLon = east_lon;

        			while ( westBoundsLon < 0.0 ) {
        				westBoundsLon = westBoundsLon + 360.;
        			}
        			while (eastBoundsLon < 0.0 ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        			}
        			// Compute whether or not the east selection boundary has been moved.
        			double dataEast = dataNE.getLongitude();
        			while ( dataEast < 0.0 ) {
        				dataEast = dataEast + 360.;
        				
        			}
        			if ( modulo && eastBoundsLon < westBoundsLon ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        				dataEast = dataEast + 360.;
        			}
        			
        			double delta = Math.abs(dataEast - eastBoundsLon);
        			// If it's not global or if boundary has moved (delta > "zero") then
        			// freeze the movement on the east side.

        			if ( !modulo || (modulo && (delta > .1)) ) {
        				if ( markerLon >= eastBoundsLon ) {
        					markerLon = eastBoundsLon;
        				}
        				if ( markerLon <= westBoundsLon ) {
        					markerLon = westBoundsLon;
        				}
        				if (markerLat <= southBoundsLat ) {
        					markerLat = southBoundsLat;
        				}
        				if ( markerLat >= northBoundsLat ) {
        					markerLat = northBoundsLat;
        				}
        			}
        			west_lon = markerLon;
        			north_lat = markerLat;
        		} else if ( title.equals("nw_ne") ) { 
        			southBoundsLat = south_lat;
        			northBoundsLat = dataNE.getLatitude();
        			markerLon = polygon.getBounds().getCenter().getLongitude();
        			if (markerLat <= southBoundsLat ) {
        				markerLat = southBoundsLat;
        			}
        			if ( markerLat >= northBoundsLat  ) {
        				markerLat = northBoundsLat;
        			}
        			north_lat = markerLat;
        		} else if ( title.equals("ne") ) {
        			northBoundsLat = dataNE.getLatitude();
        			southBoundsLat = south_lat;

        			westBoundsLon = west_lon;
        			eastBoundsLon = dataNE.getLongitude();

        			while ( westBoundsLon < 0.0 ) {
        				westBoundsLon = westBoundsLon + 360.;
        			}
        			while (eastBoundsLon < 0.0 ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        			}
        			if ( modulo && eastBoundsLon < westBoundsLon ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        			}

        			// Compute whether or not the west selection boundary has been moved.
        			double dataWest = dataSW.getLongitude();
        			while ( dataWest < 0.0 ) {
        				dataWest = dataWest + 360.;
        			}

        			double delta = Math.abs(dataWest - westBoundsLon);
        			// If it's not global or if boundary has moved (delta > "zero") then
        			// freeze the movement on the west side.

        			if ( !modulo || (modulo && (delta > .1)) ) {

        				if ( markerLon >= eastBoundsLon ) {
        					markerLon = eastBoundsLon;
        				}
        				if ( markerLon <= westBoundsLon ) {
        					markerLon = westBoundsLon;
        				}
        				if (markerLat <= southBoundsLat ) {
        					markerLat = southBoundsLat;
        				}
        				if ( markerLat >= northBoundsLat ) {
        					markerLat = northBoundsLat;
        				}
        			}
        			east_lon = markerLon;
        			north_lat = markerLat;
        		} else if ( title.equals("ne_se") ) {
        			markerLat = polygon.getBounds().getCenter().getLatitude();

        			westBoundsLon = west_lon;
        			eastBoundsLon = dataNE.getLongitude();

        			while ( westBoundsLon < 0.0 ) {
        				westBoundsLon = westBoundsLon + 360.;
        			}
        			while (eastBoundsLon < 0.0 ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        			}
        			
        			if ( modulo && eastBoundsLon < westBoundsLon ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        			}

        			// Compute whether or not the west selection boundary has been moved.
        			double dataWest = dataSW.getLongitude();
        			while ( dataWest < 0.0 ) {
        				dataWest = dataWest + 360.;
        			}

        			double delta = Math.abs(dataWest - westBoundsLon);
        			// If it's not global or if boundary has moved (delta > "zero") then
        			// freeze the movement on the west side.

        			if ( !modulo || (modulo && (delta > .1)) ) {
        				if ( markerLon >= eastBoundsLon ) {
        					markerLon = eastBoundsLon;
        				}
        				if ( markerLon <= westBoundsLon ) {
        					markerLon = westBoundsLon;
        				}
        			}
        			east_lon = markerLon;
        		} else if ( title.equals("se") ) {
        			northBoundsLat = north_lat;
        			southBoundsLat = dataSW.getLatitude();

        			westBoundsLon = west_lon;
        			eastBoundsLon = dataNE.getLongitude();

        			while ( westBoundsLon < 0.0 ) {
        				westBoundsLon = westBoundsLon + 360.;
        			}
        			while (eastBoundsLon < 0.0 ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        			}
        			
        			if ( modulo && eastBoundsLon < westBoundsLon ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        			}

        			// Compute whether or not the west selection boundary has been moved.
        			double dataWest = dataSW.getLongitude();
        			while ( dataWest < 0.0 ) {
        				dataWest = dataWest + 360.;
        			}

        			double delta = Math.abs(dataWest - westBoundsLon);
        			// If it's not global or if boundary has moved (delta > "zero") then
        			// freeze the movement on the west side.

        			if ( !modulo || (modulo && (delta > .1)) ) {
        				if ( markerLon >= eastBoundsLon ) {
            				markerLon = eastBoundsLon;
            			}
        				if ( markerLon <= westBoundsLon ) {
        					markerLon = westBoundsLon;
        				}
        				if (markerLat <= southBoundsLat ) {
        					markerLat = southBoundsLat;
        				}
        				if ( markerLat >= northBoundsLat ) {
        					markerLat = northBoundsLat;
        				}
        			}
        			east_lon = markerLon;
        			south_lat = markerLat;
        		} else if ( title.equals("sw_se") ) {
        			southBoundsLat = dataSW.getLatitude();
        			northBoundsLat = north_lat;
        			markerLon = polygon.getBounds().getCenter().getLongitude();
        			if (markerLat <= southBoundsLat ) {
        				markerLat = southBoundsLat;
        			}
        			if ( markerLat >= northBoundsLat ) {
        				markerLat = northBoundsLat;
        			}
        			south_lat = markerLat;
        		} else if ( title.equals("center") ) {

        			southBoundsLat = dataSW.getLatitude() + polygon.getBounds().toSpan().getLatitude()/2.;
        			northBoundsLat = dataNE.getLatitude() - polygon.getBounds().toSpan().getLatitude()/2.;

        			westBoundsLon = dataSW.getLongitude() + polygon.getBounds().toSpan().getLongitude()/2.;
        			eastBoundsLon = dataNE.getLongitude() - polygon.getBounds().toSpan().getLongitude()/2.;

        			while ( westBoundsLon < 0.0 ) {
        				westBoundsLon = westBoundsLon + 360.;
        			}
        			while (eastBoundsLon < 0.0 ) {
        				eastBoundsLon = eastBoundsLon + 360.;
        			}
        			if ( markerLon >= eastBoundsLon ) {
        				markerLon = eastBoundsLon;
        			}
        			if ( markerLon <= westBoundsLon ) {
        				markerLon = westBoundsLon;
        			}

        			if ( markerLat >= northBoundsLat ) {
        				markerLat = northBoundsLat;
        			}
        			if ( markerLat <= southBoundsLat ) {
        				markerLat = southBoundsLat;
        			}
        			south_lat = markerLat - polygon.getBounds().toSpan().getLatitude()/2.;
        			west_lon = markerLon - polygon.getBounds().toSpan().getLongitude()/2.0;
        			north_lat = markerLat + polygon.getBounds().toSpan().getLatitude()/2.;
        			east_lon = markerLon + polygon.getBounds().toSpan().getLongitude()/2.0;
        		}
                while ( west_lon < 0.0 ) {
                	west_lon = west_lon + 360.;
                }	
                while ( east_lon < 0.0 ) {
                	east_lon = east_lon + 360.;
                }
                
                LatLng sw = LatLng.newInstance(south_lat, west_lon, true);
                LatLng ne = LatLng.newInstance(north_lat, east_lon, true);
	            mMap.removeOverlay(polygon);
				LatLngBounds rectBounds = LatLngBounds.newInstance(sw, ne);
				polygonPoints[0] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
				polygonPoints[1] = LatLng.newInstance(rectBounds.getCenter().getLatitude(), sw.getLongitude());
				polygonPoints[2] = LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
				polygonPoints[3] = LatLng.newInstance(ne.getLatitude(), rectBounds.getCenter().getLongitude());
				polygonPoints[4] = LatLng.newInstance(ne.getLatitude(), ne.getLongitude());
				polygonPoints[5] = LatLng.newInstance(rectBounds.getCenter().getLatitude(), ne.getLongitude());
				polygonPoints[6] = LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
				polygonPoints[7] = LatLng.newInstance(sw.getLatitude(), rectBounds.getCenter().getLongitude());
				polygonPoints[8] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
				polygon = new Polygon(polygonPoints, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity);
				mMap.addOverlay(polygon);
				
				swMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), sw.getLongitude()));
				sw_nwMarker.setLatLng(LatLng.newInstance(rectBounds.getCenter().getLatitude(), sw.getLongitude()));
				nwMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), sw.getLongitude()));
				nw_neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), rectBounds.getCenter().getLongitude()));
				neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), ne.getLongitude()));
				ne_seMarker.setLatLng(LatLng.newInstance(rectBounds.getCenter().getLatitude(), ne.getLongitude()));
				seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), ne.getLongitude()));
				sw_seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), rectBounds.getCenter().getLongitude()));
				centerMarker.setLatLng(rectBounds.getCenter());
				setText();
				mMap.setCenter(dataBounds.getCenter());
			}
		};
		public void setSelectionBounds(LatLngBounds rectBounds) {
			LatLng sw = rectBounds.getSouthWest();
			LatLng ne = rectBounds.getNorthEast();
			mMap.removeOverlay(polygon);
			polygonPoints[0] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
			polygonPoints[1] = LatLng.newInstance(rectBounds.getCenter().getLatitude(), sw.getLongitude());
			polygonPoints[2] = LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
			polygonPoints[3] = LatLng.newInstance(ne.getLatitude(), rectBounds.getCenter().getLongitude());
			polygonPoints[4] = LatLng.newInstance(ne.getLatitude(), ne.getLongitude());
			polygonPoints[5] = LatLng.newInstance(rectBounds.getCenter().getLatitude(), ne.getLongitude());
			polygonPoints[6] = LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
			polygonPoints[7] = LatLng.newInstance(sw.getLatitude(), rectBounds.getCenter().getLongitude());
			polygonPoints[8] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
			polygon = new Polygon(polygonPoints, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity);
			mMap.addOverlay(polygon);
			
			swMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), sw.getLongitude()));
			sw_nwMarker.setLatLng(LatLng.newInstance(rectBounds.getCenter().getLatitude(), sw.getLongitude()));
			nwMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), sw.getLongitude()));
			nw_neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), rectBounds.getCenter().getLongitude()));
			neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), ne.getLongitude()));
			ne_seMarker.setLatLng(LatLng.newInstance(rectBounds.getCenter().getLatitude(), ne.getLongitude()));
			seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), ne.getLongitude()));
			sw_seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), rectBounds.getCenter().getLongitude()));
			centerMarker.setLatLng(rectBounds.getCenter());
			setText();
			
		}
	}
	public LatLngBounds getSelectionBounds() {
		return LatLngBounds.newInstance(mSelection.getSouthWest(), mSelection.getNorthEast());
	}
	public void setSelectionBounds(LatLngBounds rectBounds) {
		mSelection.setSelectionBounds(rectBounds);	
	}
	public LatLngBounds getDataBounds() {
		return LatLngBounds.newInstance(dataSW, dataNE);
	}
}
