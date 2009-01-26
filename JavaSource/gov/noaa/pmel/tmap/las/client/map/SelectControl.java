package gov.noaa.pmel.tmap.las.client.map;

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
	private ReferenceMap refMap;
	private LatLngBounds dataBounds;
	private Button selectAll;
	private ToggleButton mSelect;
	private MapTool mSelection;
	private Marker mDrawMarker;
	private MarkerOptions mOptions;
	private Icon mIcon;
	boolean mDraw = false;
	private Grid controls;
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

	private static final String boxWidth = "70px";
	public SelectControl (ControlPosition position, ReferenceMap refMap) {
		super(position);
		this.refMap = refMap;
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
		this.dataBounds = LatLngBounds.newInstance(LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0));
		mSelection = new MapTool(dataBounds, dataBounds, "xy", false);
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
				mSelection = new MapTool(dataBounds, bounds, "xy", modulo);	
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
				mMap.setDraggable(true);
				mSelection.setEditingEnabled(true);
			}

		});
		mSelect = new ToggleButton("Select");
		mSelect.addStyleName("map-button");
		mSelect.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				if ( refMap.getDataBounds() == null ) {
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
				if ( refMap.getDataBounds() == null ) {
					Window.alert("Please select a data set and variable.");
				} else {
					setSelectionBounds(refMap.getDataBounds(), false);
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
			LatLngBounds dataBounds = refMap.getDataBounds();
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

	public void setEditingEnabled(boolean b) {
		mSelection.setEditingEnabled(b);	
	}

	public void setText() {	
		LatLng swPolyCorner = mSelection.getSelectionBounds().getSouthWest();
		LatLng nePolyCorner = mSelection.getSelectionBounds().getNorthEast();
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
	public void clearOverlays() {
		if ( mSelection != null ) {
			mMap.removeOverlay(mSelection.getPolygon());
			for (Iterator markerIt = mSelection.getMarkers().iterator(); markerIt.hasNext();) {
				Marker marker = (Marker) markerIt.next();
				mMap.removeOverlay(marker);
			}	
		}

	}
	public void setSelectionBounds(LatLngBounds selectionBounds, boolean recenter) {
		mSelection.setSelectionBounds(selectionBounds, recenter);
	}
	public void initSelectionBounds(LatLngBounds dataBounds, LatLngBounds selectionBounds, LatLng center) {
		mSelection.initSelectionBounds(dataBounds, selectionBounds, center);
	}
	public LatLngBounds getSelectionBounds() {
		return mSelection.getSelectionBounds();
	}

	public class MapTool {
		
		private LatLngBounds selectionBounds;
		private LatLng selectionCenter;
		String type;
		LatLng[] polygonPoints;
		Polygon polygon;
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
		boolean modulo;
		/**
		 * Construct a marker tool with the default colors, weights and opacities.
		 * @param bounds
		 * @param type
		 */
		public MapTool(LatLngBounds dataBounds, LatLngBounds selectionBounds, String type, boolean modulo) {
            
			this.type = type;
			this.markers = new ArrayList<Marker>();
			this.modulo = modulo;
			SelectControl.this.dataBounds = dataBounds;
			this.selectionBounds = selectionBounds;
			this.selectionCenter = dataBounds.getCenter();
			
			String moduleRelativeURL = GWT.getModuleBaseURL();
			String moduleName = GWT.getModuleName();
			moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.indexOf(moduleName)-1);
			moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.lastIndexOf("/")+1);
			String imageURL = moduleRelativeURL + "images/";

			if ( type.equals("xy") ) {
				// Set the selection to the data bounds initially.
				LatLng sw = selectionBounds.getSouthWest();
				LatLng ne = selectionBounds.getNorthEast();
				LatLng center = selectionBounds.getCenter();
				LatLng sw_nw = LatLng.newInstance(center.getLatitude(), sw.getLongitude());
				LatLng nw = LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
				LatLng nw_ne = LatLng.newInstance(ne.getLatitude(), center.getLongitude());
				LatLng ne_se = LatLng.newInstance(center.getLatitude(), ne.getLongitude());
				LatLng se = LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
				LatLng sw_se = LatLng.newInstance(sw.getLatitude(), center.getLongitude());

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
				swMarker = new Marker(sw, sw_options);
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

		public LatLngBounds getSelectionBounds() {
			return selectionBounds;
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
			setSelectionBounds(rectBounds, false);
		}
		/**
		 * @param click the click to set
		 */
		public void setClick(LatLng click) {
			this.click = click;
		}

		public LatLng[] getPolygonPoints() {
			return polygonPoints;
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
                
				mMap.setCenter(selectionCenter);
				Marker marker = event.getSender();
				String title = marker.getTitle();
				LatLng markerLocation = marker.getLatLng();
				LatLngBounds containmentBounds = SelectControl.this.dataBounds;

				LatLng sw = containmentBounds.getSouthWest();
				LatLng ne = containmentBounds.getNorthEast();
				if ( title.equals("sw") ) {
					// The south west marker's movements are bounded by the south west data bounds and the north east rectangle bounds.
					containmentBounds = LatLngBounds.newInstance(dataBounds.getSouthWest(), mSelection.getSelectionBounds().getNorthEast());
					// If it's still in the containment, the new selection is this:
				    sw = markerLocation;
				    ne = mSelection.getSelectionBounds().getNorthEast();
				} else if ( title.equals("sw_nw") )  {
					containmentBounds = LatLngBounds.newInstance(LatLng.newInstance(dataBounds.getSouthWest().getLatitude(), dataBounds.getSouthWest().getLongitude()), LatLng.newInstance(dataBounds.getNorthEast().getLatitude(), mSelection.getSelectionBounds().getNorthEast().getLongitude()));
					sw = LatLng.newInstance(mSelection.getSelectionBounds().getSouthWest().getLatitude(), markerLocation.getLongitude());
					ne = LatLng.newInstance(mSelection.getSelectionBounds().getNorthEast().getLatitude(), mSelection.getSelectionBounds().getNorthEast().getLongitude());
				} else if ( title.equals("nw") ) {
					LatLng swBound = LatLng.newInstance(mSelection.getSelectionBounds().getSouthWest().getLatitude(), dataBounds.getSouthWest().getLongitude());
				    LatLng neBound = LatLng.newInstance(dataBounds.getNorthEast().getLatitude(), mSelection.getSelectionBounds().getNorthEast().getLongitude());
				    containmentBounds = LatLngBounds.newInstance(swBound, neBound);
				    // If accepted the new selection bounds are:
				    sw = LatLng.newInstance(mSelection.getSelectionBounds().getSouthWest().getLatitude(), markerLocation.getLongitude());
				    ne = LatLng.newInstance(markerLocation.getLatitude(), mSelection.getSelectionBounds().getNorthEast().getLongitude());
				} else if ( title.equals("nw_ne") ) { 
					LatLng swBound = LatLng.newInstance(mSelection.getSelectionBounds().getSouthWest().getLatitude(), dataBounds.getSouthWest().getLongitude());
					containmentBounds = LatLngBounds.newInstance(swBound, dataBounds.getNorthEast());
					sw = mSelection.getSelectionBounds().getSouthWest();
					ne = LatLng.newInstance(markerLocation.getLatitude(), mSelection.getSelectionBounds().getNorthEast().getLongitude());
				} else if ( title.equals("ne") ) {
					containmentBounds = LatLngBounds.newInstance(mSelection.getSelectionBounds().getSouthWest(), dataBounds.getNorthEast());
					sw = mSelection.getSelectionBounds().getSouthWest();
					ne = markerLocation;
				} else if ( title.equals("ne_se") ) {
					LatLng swBound = LatLng.newInstance(dataBounds.getSouthWest().getLatitude(), mSelection.getSelectionBounds().getSouthWest().getLongitude());
                    containmentBounds = LatLngBounds.newInstance(swBound, dataBounds.getNorthEast());
                    sw = mSelection.getSelectionBounds().getSouthWest();
                    ne = LatLng.newInstance(mSelection.getSelectionBounds().getNorthEast().getLatitude(), markerLocation.getLongitude());
				} else if ( title.equals("se") ) {
					LatLng swBound = LatLng.newInstance(dataBounds.getSouthWest().getLatitude(), mSelection.getSelectionBounds().getSouthWest().getLongitude());
					LatLng neBound = LatLng.newInstance(mSelection.getSelectionBounds().getNorthEast().getLatitude(), dataBounds.getNorthEast().getLongitude());
				    containmentBounds = LatLngBounds.newInstance(swBound, neBound);
				    sw = LatLng.newInstance(markerLocation.getLatitude(), mSelection.getSelectionBounds().getSouthWest().getLongitude());
				    ne = LatLng.newInstance(mSelection.getSelectionBounds().getNorthEast().getLatitude(), markerLocation.getLongitude());
				} else if ( title.equals("sw_se") ) {
					containmentBounds = LatLngBounds.newInstance(dataBounds.getSouthWest(), LatLng.newInstance(mSelection.getSelectionBounds().getNorthEast().getLatitude(), dataBounds.getNorthEast().getLongitude()));
				    sw = LatLng.newInstance(markerLocation.getLatitude(), mSelection.getSelectionBounds().getSouthWest().getLongitude());
					ne = mSelection.getSelectionBounds().getNorthEast();
				} else if ( title.equals("center") ) {
					LatLng span = mSelection.getSelectionBounds().toSpan();
					double lat_span = span.getLatitude();
					double lon_span = span.getLongitude();
					LatLng swBound = LatLng.newInstance(dataBounds.getSouthWest().getLatitude()+lat_span/2.0, dataBounds.getSouthWest().getLongitude()+lon_span/2.0);
				    LatLng neBound = LatLng.newInstance(dataBounds.getNorthEast().getLatitude()-lat_span/2.0, dataBounds.getNorthEast().getLongitude()-lon_span/2.0);
				    containmentBounds = LatLngBounds.newInstance(swBound, neBound);
				}

				if ( title.equals("center") ) {
					if ( containmentBounds.containsLatLng(markerLocation) ) {
						LatLng span = mSelection.getSelectionBounds().toSpan();
						double lat_span = span.getLatitude();
						double lon_span = span.getLongitude();
						double slat = markerLocation.getLatitude() - lat_span/2.0;
						double nlat = markerLocation.getLatitude() + lat_span/2.0;
						double wlon = markerLocation.getLongitude() - lon_span/2.0;
						double elon = markerLocation.getLongitude() + lon_span/2.0;
						LatLngBounds rectBounds = LatLngBounds.newInstance(LatLng.newInstance(slat, wlon), LatLng.newInstance(nlat, elon));
						setSelectionBounds(rectBounds, false);
						mMap.setCenter(selectionCenter);
					} else {
						centerMarker.setLatLng(mSelection.getSelectionBounds().getCenter());
					}
					
				} else {
					if ( containmentBounds.containsLatLng(markerLocation)) {
						/*
						if ( title.equals("sw_nw") ) {
							sw_nwMarker.setLatLng(LatLng.newInstance(mSelection.getSelectionBounds().getCenter().getLatitude(), markerLocation.getLongitude()));
						} else if ( title.equals("nw_ne") ) {
							nw_neMarker.setLatLng(LatLng.newInstance(markerLocation.getLatitude(), mSelection.getSelectionBounds().getCenter().getLongitude()));
						} else if ( title.equals("ne_se") ) {
							ne_seMarker.setLatLng(LatLng.newInstance(mSelection.getSelectionBounds().getCenter().getLatitude(), markerLocation.getLongitude()));
						}
						*/
						LatLngBounds rectBounds = LatLngBounds.newInstance(sw, ne);
						setSelectionBounds(rectBounds, false);
						mMap.setCenter(selectionCenter);
					} else {
						if ( title.equals("sw") ) {
							swMarker.setLatLng(mSelection.getSelectionBounds().getSouthWest());
						} else if ( title.equals("sw_nw") )  {
							sw_nwMarker.setLatLng(LatLng.newInstance(mSelection.getSelectionBounds().getCenter().getLatitude(), mSelection.getSelectionBounds().getSouthWest().getLongitude()));
						} else if ( title.equals("nw") ) {
							nwMarker.setLatLng(LatLng.newInstance(mSelection.getSelectionBounds().getNorthEast().getLatitude(), mSelection.getSelectionBounds().getSouthWest().getLongitude()));
						} else if ( title.equals("nw_ne") ) { 
							nw_neMarker.setLatLng(LatLng.newInstance(mSelection.getSelectionBounds().getNorthEast().getLatitude(), mSelection.getSelectionBounds().getCenter().getLongitude()));
						} else if ( title.equals("ne") ) {
							neMarker.setLatLng(mSelection.getSelectionBounds().getNorthEast());
						} else if ( title.equals("ne_se") ) {
							ne_seMarker.setLatLng(LatLng.newInstance(mSelection.getSelectionBounds().getCenter().getLatitude(), mSelection.getSelectionBounds().getNorthEast().getLongitude()));
						} else if ( title.equals("se") ) {
							seMarker.setLatLng(LatLng.newInstance(mSelection.getSelectionBounds().getSouthWest().getLatitude(), mSelection.getSelectionBounds().getNorthEast().getLongitude()));
						} else if ( title.equals("sw_se") ) {
                            sw_seMarker.setLatLng(LatLng.newInstance(mSelection.getSelectionBounds().getSouthWest().getLatitude(), mSelection.getSelectionBounds().getCenter().getLongitude()));
						} else if ( title.equals("center") ) {

						}
					}
				}
			}
		};
        public void initSelectionBounds(LatLngBounds dataBounds, LatLngBounds selectionBounds, LatLng center) {
        	SelectControl.this.dataBounds = dataBounds;
        	setSelectionBounds(selectionBounds, false);
        	this.selectionCenter = center;
        }
		public void setSelectionBounds(LatLngBounds rectBounds, boolean recenter) {
			this.selectionBounds = rectBounds;
			if ( recenter ) {
				this.selectionCenter = rectBounds.getCenter();
			}
			
			LatLng sw = rectBounds.getSouthWest();
			if ( sw.getLatitude() < -88.5 ) {
				sw = LatLng.newInstance(-88.5, sw.getLongitude());
			}
			LatLng ne = rectBounds.getNorthEast();
			if ( ne.getLatitude() > 88.5 ) {
				ne = LatLng.newInstance(88.5, ne.getLongitude());
			}
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

	public void setSelectionCenter(LatLng center) {
		mSelection.selectionCenter = center;
	}
	public void setVisible(boolean visible) {
		controls.setVisible(visible);
	};		
}
