package gov.noaa.pmel.tmap.las.client.map;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
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
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.overlay.Polygon;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class SelectWidget extends Composite {
	private MapWidget mMap;
	private ReferenceMap refMap;
	private LatLngBounds dataBounds;
	private Button selectAll;
	private ToggleButton mSelect;
	private MapTool mapTool;
	
	private Icon mIcon;
	boolean mDraw = false;
	private HorizontalPanel controls;
	private LatLonWidget textWidget = null;
	double span;
	double delta;
    LatLng lastGoodPosition;
   
	public SelectWidget (ReferenceMap refMap) {
		this.refMap = refMap;

		mSelect = new ToggleButton("Select");
		mSelect.addStyleName("map-button");
		
		String moduleRelativeURL = GWT.getModuleBaseURL();
		String moduleName = GWT.getModuleName();
		moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.indexOf(moduleName)-1);
		moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.lastIndexOf("/")+1);
		String imageURL = moduleRelativeURL + "images/";
		
		mMap = refMap.getMapWidget();
		controls = new HorizontalPanel();
		controls.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		this.dataBounds = LatLngBounds.newInstance(LatLng.newInstance(0.0, 0.0), LatLng.newInstance(0.0, 0.0));
		
		// probably want to be able to initialize on different tool types.
		mapTool = new XYMapTool(dataBounds, dataBounds);
		mapTool.setVisible(false);
		
		for (Iterator markerIt = mapTool.getOverlays().iterator(); markerIt.hasNext();) {
			Overlay o = (Overlay) markerIt.next();
			mMap.addOverlay(o);
		}
		mapTool.setEditingEnabled(false);
		// different tool types above...
		mIcon = Icon.newInstance();
		mIcon.setIconSize(Size.newInstance(20, 20));
		mIcon.setIconAnchor(Point.newInstance(10, 10));
		mIcon.setImageURL(imageURL+"crosshairs.png");
		
		selectAll = new Button("Select All");
		selectAll.addStyleName("map-button");
		selectAll.addClickListener(selectAllListener);
		controls.add(mSelect);
		controls.add(selectAll);
		
		initWidget(controls);
	}
	
	ClickListener selectAllListener = new ClickListener() {

		public void onClick(Widget sender) {
			if ( refMap.getDataBounds() == null ) {
				Window.alert("Please select a data set and variable.");
			} else {
				if ( refMap.isModulo() ) {
					setSelectionBounds(refMap.getDataBounds(), false, false);
				} else {
				    setSelectionBounds(refMap.getDataBounds(), false, true);
				}
			}
		}

	};
	

	public boolean isDown() {
		return mSelect.isDown();
	}

	public void setEditingEnabled(boolean b) {
		mapTool.setEditingEnabled(b);	
	}

	public void setText() {	
		if ( textWidget != null ) {
			textWidget.setText(mapTool.getSelectionBounds());
		}
	}
	public void clearOverlays() {
		mapTool.clearOverlays();
	}
	public void setSelectionBounds(LatLngBounds selectionBounds, boolean recenter, boolean show) {
		mapTool.setSelectionBounds(selectionBounds, recenter, show);
	}
	public void initSelectionBounds(LatLngBounds dataBounds, LatLngBounds selectionBounds, boolean show) {
		mapTool.initSelectionBounds(dataBounds, selectionBounds, show);
	}
	public void setVisible(boolean visible) {
		controls.setVisible(visible);
	}

	public void setLatLngWidget(LatLonWidget textWidget) {
		this.textWidget = textWidget;
	}

	public String getXhiFormatted() {
		return textWidget.getXhiFormatted();
	}

	public String getXloFormatted() {
		return textWidget.getXloFormatted();
	}
	public String getYloFormatted() {
		return textWidget.getYloFormatted();
	}
	public String getYhiFormatted() {
		return textWidget.getYhiFormatted();
	}
    
	public double getXhi() {
		return textWidget.getXhi();
	}
	public double getXlo() {
		return textWidget.getXlo();
	}
	public double getYhi() {
		return textWidget.getYhi();
	}
	public double getYlo() {
		return textWidget.getYlo();
	}
	
	public void setToolType(String tool) {
		mapTool.clearOverlays();
		if ( tool.equals("xy") ) {
			mapTool = new XYMapTool(dataBounds, mapTool.getSelectionBounds());
			for (Iterator oIt = mapTool.getOverlays().iterator(); oIt.hasNext();) {
				Overlay o = (Overlay) oIt.next();
				refMap.getMapWidget().addOverlay(o);
			}
		} else if ( tool.contains("t") || tool.equals("z") ) {
			mapTool = new PTMapTool(dataBounds, mapTool.getSelectionBounds());
			for (Iterator oIt = mapTool.getOverlays().iterator(); oIt.hasNext();) {
				Overlay o = (Overlay) oIt.next();
				refMap.getMapWidget().addOverlay(o);
			}
		}
	}
	public LatLngBounds getSelectionBounds() {
		return mapTool.getSelectionBounds();
	}
	
	public class XYMapTool extends MapTool {
		LatLng[] polygonPoints;
		Polygon polygon;
		private Marker mDrawMarker;
		private MarkerOptions mOptions;
		/**
		 * Construct a marker tool with the default colors, weights and opacities.
		 * @param bounds
		 * @param type
		 */
		public XYMapTool(LatLngBounds dataBounds, LatLngBounds selectionBounds) {
            
			markers.clear();
			
			SelectWidget.this.dataBounds = dataBounds;
			this.selectionBounds = selectionBounds;

			mOptions = MarkerOptions.newInstance();
			mOptions.setIcon(mIcon);
			mOptions.setDraggable(true);
			mOptions.setDragCrossMove(true);
			mOptions.setAutoPan(false);
			mDrawMarker = new Marker(LatLng.newInstance(0.0, 0.0), mOptions);
			
			mDrawMarker.addMarkerMouseDownHandler(markerMouseDownHandler);
			mDrawMarker.addMarkerMouseUpHandler(markerMouseUpHandler);
			
			mMap.addOverlay(mDrawMarker);
			mDrawMarker.setVisible(false);
			
			String moduleRelativeURL = GWT.getModuleBaseURL();
			String moduleName = GWT.getModuleName();
			moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.indexOf(moduleName)-1);
			moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.lastIndexOf("/")+1);
			String imageURL = moduleRelativeURL + "images/";

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
			sw_options.setAutoPan(false);
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
			sw_nw_options.setAutoPan(false);
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
			nw_options.setAutoPan(false);
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
			nw_ne_options.setAutoPan(false);
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
			ne_options.setAutoPan(false);
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
			ne_se_options.setAutoPan(false);
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
			se_options.setAutoPan(false);
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
			sw_se_options.setAutoPan(false);
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
			center_options.setAutoPan(false);
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
			mSelect.addClickListener(selectListener);
			setText();

		}
		MarkerMouseDownHandler markerMouseDownHandler = new MarkerMouseDownHandler() {
			public void onMouseDown(MarkerMouseDownEvent event) {
				mDraw = true;
				LatLng click = mDrawMarker.getLatLng();
				mapTool.setClick(click);
				lastGoodPosition = click;
				LatLngBounds bounds = LatLngBounds.newInstance(click, click);
				setSelectionBounds(bounds, false, true);
				mMap.setDraggable(false);
			}
		};
		
		ClickListener selectListener = new ClickListener() {
			public void onClick(Widget sender) {
				if ( refMap.getDataBounds() == null ) {
					Window.alert("Please select a data set and variable.");
					mSelect.setDown(false);
				} else {
					if (mSelect.isDown()) {
						mMap.addMapMouseMoveHandler(mouseMove);
					} else {	          
						mDrawMarker.setVisible(false);
						mMap.removeMapMouseMoveHandler(mouseMove);		           
					}
				}
			}
		};
		MarkerMouseUpHandler markerMouseUpHandler = new MarkerMouseUpHandler() {

			public void onMouseUp(MarkerMouseUpEvent event) {
				if ( mMap.getZoomLevel() == refMap.getZoom() && refMap.isModulo() ) {
					mMap.setDraggable(false);
//					refMap.setCenter(dataBounds.getCenter());
				} else {
				    mMap.setDraggable(true);
				}
				mSelect.setDown(false);
				mDrawMarker.setVisible(false);
				mMap.removeMapMouseMoveHandler(mouseMove);
				mDraw = false;
				mapTool.setEditingEnabled(true);
			}

		};
		MapMouseMoveHandler mouseMove = new MapMouseMoveHandler() {
			public void onMouseMove(MapMouseMoveEvent event) {
				refMap.getMapWidget().setDraggable(false);
				LatLng position = event.getLatLng();
				if ( mDraw ) {
					LatLng update_position;
					if ( dataBounds.containsLatLng(position) ) {
						mDrawMarker.setVisible(true);
						mDrawMarker.setLatLng(position);
						update_position = position;
					} else {
						mDrawMarker.setVisible(false);
						mDrawMarker.setLatLng(lastGoodPosition);
						update_position = lastGoodPosition;
					}
					double posLon = update_position.getLongitude();
					double posLat = update_position.getLatitude();
					double clickLon = click.getLongitude();
					double clickLat = click.getLatitude();
					double elon = dataBounds.getNorthEast().getLongitude();
					double slat = dataBounds.getSouthWest().getLatitude();
					double nlat = dataBounds.getNorthEast().getLatitude();
					double wlon = dataBounds.getSouthWest().getLongitude();
					LatLngBounds dragBoxSouthWest = LatLngBounds.newInstance(dataBounds.getSouthWest(), click);
					LatLngBounds dragBoxNorthEast = LatLngBounds.newInstance(click, dataBounds.getNorthEast());
					LatLngBounds dragBoxSouthEast = LatLngBounds.newInstance(LatLng.newInstance(slat, clickLon), LatLng.newInstance(clickLat, elon));
					LatLngBounds dragBoxNorthWest = LatLngBounds.newInstance(LatLng.newInstance(clickLat, wlon), LatLng.newInstance(nlat, clickLon));
					LatLngBounds rectBounds;
					if ( dragBoxNorthEast.containsLatLng(update_position) ) {
						rectBounds = LatLngBounds.newInstance(click, update_position);
						setSelectionBounds(rectBounds, false, true);
						lastGoodPosition = update_position;
					} else if ( dragBoxSouthWest.containsLatLng(update_position) ) {
					    rectBounds = LatLngBounds.newInstance(update_position, click);
						setSelectionBounds(rectBounds, false, true);
						lastGoodPosition = update_position;
					} else if ( dragBoxSouthEast.containsLatLng(update_position) ) {
						rectBounds = LatLngBounds.newInstance(LatLng.newInstance(posLat, clickLon), LatLng.newInstance(clickLat, posLon));
					    setSelectionBounds(rectBounds, false, true);
					    lastGoodPosition = update_position;
					} else if ( dragBoxNorthWest.containsLatLng(update_position) ) {
						rectBounds = LatLngBounds.newInstance(LatLng.newInstance(clickLat, posLon), LatLng.newInstance(posLat, clickLon));
						setSelectionBounds(rectBounds, false, true);
						lastGoodPosition = update_position;
					} 
					if ( dataBounds.containsLatLng(position) ) {
						setText();
					}
				} else {
					if ( dataBounds.containsLatLng(position) ) {
						mDrawMarker.setVisible(true);
						mDrawMarker.setLatLng(position);
					} else {
						mDrawMarker.setVisible(false);
					}
				}
			}
		};
		public void clearOverlays() {
			if ( polygon != null ) {
				mMap.removeOverlay(polygon);
			}
			for (Iterator markerIt = markers.iterator(); markerIt.hasNext();) {
				Marker marker = (Marker) markerIt.next();
				mMap.removeOverlay(marker);
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

		/**
		 * @param click the click to set
		 */
		public void setClick(LatLng click) {
			this.click = click;
		}

		public LatLng[] getPolygonPoints() {
			return polygonPoints;
		}
		MarkerDragHandler markerDragHandler = new MarkerDragHandler() {
			public void onDrag(MarkerDragEvent event) {
				Marker marker = event.getSender();
				String title = marker.getTitle();
				LatLng markerLocation = marker.getLatLng();
				
				LatLngBounds containmentBounds = SelectWidget.this.dataBounds;

				LatLng sw = containmentBounds.getSouthWest();
				LatLng ne = containmentBounds.getNorthEast();
				if ( title.equals("sw") ) {
					// The south west marker's movements are bounded by the south west data bounds and the north east rectangle bounds.
					containmentBounds = LatLngBounds.newInstance(dataBounds.getSouthWest(), selectionBounds.getNorthEast());
					// If it's still in the containment, the new selection is this:
					sw = markerLocation;
					ne = selectionBounds.getNorthEast();
				} else if ( title.equals("sw_nw") )  {
					containmentBounds = LatLngBounds.newInstance(LatLng.newInstance(dataBounds.getSouthWest().getLatitude(), dataBounds.getSouthWest().getLongitude()), LatLng.newInstance(dataBounds.getNorthEast().getLatitude(), selectionBounds.getNorthEast().getLongitude()));
					sw = LatLng.newInstance(selectionBounds.getSouthWest().getLatitude(), markerLocation.getLongitude());
					ne = LatLng.newInstance(selectionBounds.getNorthEast().getLatitude(), selectionBounds.getNorthEast().getLongitude());
				} else if ( title.equals("nw") ) {
					LatLng swBound = LatLng.newInstance(selectionBounds.getSouthWest().getLatitude(), dataBounds.getSouthWest().getLongitude());
					LatLng neBound = LatLng.newInstance(dataBounds.getNorthEast().getLatitude(), selectionBounds.getNorthEast().getLongitude());
					containmentBounds = LatLngBounds.newInstance(swBound, neBound);
					// If accepted the new selection bounds are:
					sw = LatLng.newInstance(selectionBounds.getSouthWest().getLatitude(), markerLocation.getLongitude());
					ne = LatLng.newInstance(markerLocation.getLatitude(), selectionBounds.getNorthEast().getLongitude());
				} else if ( title.equals("nw_ne") ) { 
					LatLng swBound = LatLng.newInstance(selectionBounds.getSouthWest().getLatitude(), dataBounds.getSouthWest().getLongitude());
					containmentBounds = LatLngBounds.newInstance(swBound, dataBounds.getNorthEast());
					sw = selectionBounds.getSouthWest();
					ne = LatLng.newInstance(markerLocation.getLatitude(), selectionBounds.getNorthEast().getLongitude());
				} else if ( title.equals("ne") ) {
					containmentBounds = LatLngBounds.newInstance(selectionBounds.getSouthWest(), dataBounds.getNorthEast());
					sw = selectionBounds.getSouthWest();
					ne = markerLocation;
				} else if ( title.equals("ne_se") ) {
					LatLng swBound = LatLng.newInstance(dataBounds.getSouthWest().getLatitude(), selectionBounds.getSouthWest().getLongitude());
					containmentBounds = LatLngBounds.newInstance(swBound, dataBounds.getNorthEast());
					sw = selectionBounds.getSouthWest();
					ne = LatLng.newInstance(selectionBounds.getNorthEast().getLatitude(), markerLocation.getLongitude());
				} else if ( title.equals("se") ) {
					LatLng swBound = LatLng.newInstance(dataBounds.getSouthWest().getLatitude(), selectionBounds.getSouthWest().getLongitude());
					LatLng neBound = LatLng.newInstance(selectionBounds.getNorthEast().getLatitude(), dataBounds.getNorthEast().getLongitude());
					containmentBounds = LatLngBounds.newInstance(swBound, neBound);
					sw = LatLng.newInstance(markerLocation.getLatitude(), selectionBounds.getSouthWest().getLongitude());
					ne = LatLng.newInstance(selectionBounds.getNorthEast().getLatitude(), markerLocation.getLongitude());
				} else if ( title.equals("sw_se") ) {
					containmentBounds = LatLngBounds.newInstance(dataBounds.getSouthWest(), LatLng.newInstance(selectionBounds.getNorthEast().getLatitude(), dataBounds.getNorthEast().getLongitude()));
					sw = LatLng.newInstance(markerLocation.getLatitude(), selectionBounds.getSouthWest().getLongitude());
					ne = selectionBounds.getNorthEast();
				} else if ( title.equals("center") ) {
					LatLng span = selectionBounds.toSpan();
					double lat_span = span.getLatitude();
					double lon_span = span.getLongitude();
					LatLng swBound = LatLng.newInstance(dataBounds.getSouthWest().getLatitude()+lat_span/2.0, dataBounds.getSouthWest().getLongitude()+lon_span/2.0);
					LatLng neBound = LatLng.newInstance(dataBounds.getNorthEast().getLatitude()-lat_span/2.0, dataBounds.getNorthEast().getLongitude()-lon_span/2.0);
					containmentBounds = LatLngBounds.newInstance(swBound, neBound);
				}

				if ( title.equals("center") ) {
					if ( containmentBounds.containsLatLng(markerLocation) ) {
						LatLng span = selectionBounds.toSpan();
						double lat_span = span.getLatitude();
						double lon_span = span.getLongitude();
						double slat = markerLocation.getLatitude() - lat_span/2.0;
						double nlat = markerLocation.getLatitude() + lat_span/2.0;
						double wlon = markerLocation.getLongitude() - lon_span/2.0;
						double elon = markerLocation.getLongitude() + lon_span/2.0;
						LatLngBounds rectBounds = LatLngBounds.newInstance(LatLng.newInstance(slat, wlon), LatLng.newInstance(nlat, elon));
						setSelectionBounds(rectBounds, false, true);
						if ( mMap.getZoomLevel() == refMap.getZoom() ) {
							mMap.setCenter(dataBounds.getCenter());
						}
						lastGoodPosition = rectBounds.getCenter();
					} else {
						centerMarker.setLatLng(lastGoodPosition);
					}

				} else {
					if ( containmentBounds.containsLatLng(markerLocation)) {
						LatLngBounds rectBounds = LatLngBounds.newInstance(sw, ne);
						setSelectionBounds(rectBounds, false, true);
						
					} else {
						if ( title.equals("sw") ) {
							swMarker.setLatLng(selectionBounds.getSouthWest());
						} else if ( title.equals("sw_nw") )  {
							sw_nwMarker.setLatLng(LatLng.newInstance(selectionBounds.getCenter().getLatitude(), selectionBounds.getSouthWest().getLongitude()));
						} else if ( title.equals("nw") ) {
							nwMarker.setLatLng(LatLng.newInstance(selectionBounds.getNorthEast().getLatitude(), selectionBounds.getSouthWest().getLongitude()));
						} else if ( title.equals("nw_ne") ) { 
							nw_neMarker.setLatLng(LatLng.newInstance(selectionBounds.getNorthEast().getLatitude(), selectionBounds.getCenter().getLongitude()));
						} else if ( title.equals("ne") ) {
							neMarker.setLatLng(selectionBounds.getNorthEast());
						} else if ( title.equals("ne_se") ) {
							ne_seMarker.setLatLng(LatLng.newInstance(selectionBounds.getCenter().getLatitude(), selectionBounds.getNorthEast().getLongitude()));
						} else if ( title.equals("se") ) {
							seMarker.setLatLng(LatLng.newInstance(selectionBounds.getSouthWest().getLatitude(), selectionBounds.getNorthEast().getLongitude()));
						} else if ( title.equals("sw_se") ) {
							sw_seMarker.setLatLng(LatLng.newInstance(selectionBounds.getSouthWest().getLatitude(), selectionBounds.getCenter().getLongitude()));
						} 
					}
				}
			}
		};
        public void initSelectionBounds(LatLngBounds dataBounds, LatLngBounds selectionBounds, boolean show) {
        	SelectWidget.this.dataBounds = dataBounds;
        	setSelectionBounds(selectionBounds, false, show);
        }
		public void setSelectionBounds(LatLngBounds rectBounds, boolean recenter, boolean show) {
			this.selectionBounds = rectBounds;
			if ( recenter ) {
				refMap.setCenter(rectBounds.getCenter());
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
			if ( show ) {
			    mMap.addOverlay(polygon);
			}
			swMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), sw.getLongitude()));
			sw_nwMarker.setLatLng(LatLng.newInstance(rectBounds.getCenter().getLatitude(), sw.getLongitude()));
			nwMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), sw.getLongitude()));
			nw_neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), rectBounds.getCenter().getLongitude()));
			neMarker.setLatLng(LatLng.newInstance(ne.getLatitude(), ne.getLongitude()));
			ne_seMarker.setLatLng(LatLng.newInstance(rectBounds.getCenter().getLatitude(), ne.getLongitude()));
			seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), ne.getLongitude()));
			sw_seMarker.setLatLng(LatLng.newInstance(sw.getLatitude(), rectBounds.getCenter().getLongitude()));
			centerMarker.setLatLng(rectBounds.getCenter());
			if ( show ) {
				setEditingEnabled(true);
			} else {
			    setEditingEnabled(false);
			}
			setText();

		}
		@Override
		public ArrayList<Overlay> getOverlays() {
			ArrayList<Overlay> o = new ArrayList<Overlay>();
			o.add(polygon);
			o.addAll(markers);
			return o;
		}
	}
	public class PTMapTool extends MapTool {
		
		Marker centerMarker;
		public PTMapTool(LatLngBounds dataBounds, LatLngBounds selectionBounds) {
			
			SelectWidget.this.dataBounds = dataBounds;
			this.selectionBounds = selectionBounds;
			markers.clear();
			
			/* Used for getting the custom marker icons.  Not used for a point marker.
			String moduleRelativeURL = GWT.getModuleBaseURL();
			String moduleName = GWT.getModuleName();
			moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.indexOf(moduleName)-1);
			moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.lastIndexOf("/")+1);
			String imageURL = moduleRelativeURL + "images/";
			*/
			
			MarkerOptions center_options = MarkerOptions.newInstance();
			center_options.setDraggable(true);
			center_options.setDragCrossMove(true);
			center_options.setAutoPan(false);
			center_options.setBouncy(false);
			center_options.setTitle("center");
			centerMarker = new Marker(selectionBounds.getCenter(), center_options);
			centerMarker.addMarkerDragHandler(markerDragHandler);
			centerMarker.addMarkerDragEndHandler(markerDragEndHandler);
			markers.add(centerMarker);
			textWidget.setText(LatLngBounds.newInstance(selectionBounds.getCenter(), selectionBounds.getCenter()));
		}
		MarkerDragEndHandler markerDragEndHandler = new MarkerDragEndHandler() {
			public void onDragEnd(MarkerDragEndEvent event) {
				Marker marker = event.getSender();
				LatLng markerLocation = marker.getLatLng();
				LatLng center = selectionBounds.getCenter();
				LatLng extents = selectionBounds.toSpan();
				double lat = extents.getLatitude()/2.0;
				double lon = extents.getLongitude()/2.0;
				
				double slat = markerLocation.getLatitude() - lat;
				if ( slat < dataBounds.getSouthWest().getLatitude() ) {
					slat = dataBounds.getSouthWest().getLatitude();
				}
				double wlon = markerLocation.getLongitude() - lon;
				if ( !refMap.isModulo() ) {
					if ( wlon < dataBounds.getSouthWest().getLongitude() ) {
						wlon = dataBounds.getSouthWest().getLongitude();
					}
				}
				double nlat = markerLocation.getLatitude() + lat;
				if ( nlat > dataBounds.getNorthEast().getLatitude() ) {
					nlat = dataBounds.getNorthEast().getLatitude();
				}
				double elon = markerLocation.getLongitude() + lon;
				if ( !refMap.isModulo() ) {
					if ( elon > dataBounds.getNorthEast().getLongitude() ) {
						elon = dataBounds.getNorthEast().getLongitude();
					}
				}
				LatLng sw = LatLng.newInstance(slat, wlon);
				LatLng ne = LatLng.newInstance(nlat, elon);
				setSelectionBounds(LatLngBounds.newInstance(sw, ne), true, true);
			}	
		};
		MarkerDragHandler markerDragHandler = new MarkerDragHandler() {
			public void onDrag(MarkerDragEvent event) {
				Marker marker = event.getSender();
				LatLng markerLocation = marker.getLatLng();
				if ( dataBounds.containsLatLng(markerLocation) ) {
					textWidget.setText(LatLngBounds.newInstance(markerLocation, markerLocation));
					lastGoodPosition = markerLocation;
				} else {
					centerMarker.setLatLng(lastGoodPosition);
				}
			}	
		};
		@Override
		public void clearOverlays() {
			for (Iterator mkIt = markers.iterator(); mkIt.hasNext();) {
				Marker m = (Marker) mkIt.next();
				mMap.removeOverlay(m);
			}
		}
		@Override
		public ArrayList<Overlay> getOverlays() {
			return new ArrayList<Overlay>(markers);
		}
		@Override
		public void initSelectionBounds(LatLngBounds dataBounds, LatLngBounds selectionBounds, boolean show) {
			SelectWidget.this.dataBounds = dataBounds;
			this.selectionBounds = selectionBounds;
			setSelectionBounds(selectionBounds, false, show);
		}
		@Override
		public void setSelectionBounds(LatLngBounds rectBounds,	boolean recenter, boolean show) {
			this.selectionBounds = rectBounds;
			if ( recenter ) {
				refMap.setCenter(rectBounds.getCenter());
			}
			
			LatLng sw = rectBounds.getSouthWest();
			if ( sw.getLatitude() < -88.5 ) {
				sw = LatLng.newInstance(-88.5, sw.getLongitude());
			}
			LatLng ne = rectBounds.getNorthEast();
			if ( ne.getLatitude() > 88.5 ) {
				ne = LatLng.newInstance(88.5, ne.getLongitude());
			}
			//clearOverlays();
			
			centerMarker.setLatLng(rectBounds.getCenter());
			if ( show ) {
				setEditingEnabled(true);
			} else {
			    setEditingEnabled(false);
			}
		}
		@Override
		public void setVisible(boolean visible) {
			centerMarker.setVisible(visible);
		}
	}
	public class YMapTool {
		
	}
	public class XMapTool {
		
	}

}
