package gov.noaa.pmel.tmap.las.client.map;

import gov.noaa.pmel.tmap.las.client.laswidget.Util;
import gov.noaa.pmel.tmap.las.client.openlayers.DrawSingleFeature;
import gov.noaa.pmel.tmap.las.client.openlayers.DrawSingleFeatureOptions;
import gov.noaa.pmel.tmap.las.client.openlayers.HorizontalPathHandler;
import gov.noaa.pmel.tmap.las.client.openlayers.VerticalPathHandler;
import gov.noaa.pmel.tmap.las.client.openlayers.DrawSingleFeature.FeatureAddedListener;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.StyleMap;
import org.gwtopenmaps.openlayers.client.control.ModifyFeature;
import org.gwtopenmaps.openlayers.client.control.ModifyFeatureOptions;
import org.gwtopenmaps.openlayers.client.control.ModifyFeature.OnModificationEndListener;
import org.gwtopenmaps.openlayers.client.control.ModifyFeature.OnModificationListener;
import org.gwtopenmaps.openlayers.client.control.ModifyFeature.OnModificationStartListener;
import org.gwtopenmaps.openlayers.client.event.MapMoveListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Geometry;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.handler.PointHandler;
import org.gwtopenmaps.openlayers.client.handler.RegularPolygonHandler;
import org.gwtopenmaps.openlayers.client.handler.RegularPolygonHandlerOptions;
import org.gwtopenmaps.openlayers.client.layer.Boxes;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;
import org.gwtopenmaps.openlayers.client.marker.Box;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class OLMapWidget extends Composite {
	private String tool = "";
	private DockPanel dockPanel;
	private MapOptions wmsMapOptions;
	private MapOptions wrapMapOptions;
	private MapOptions mapOptions;
	private Map map;
	private WMS wmsLayer;
	private Vector boxLayer;
	
	// Add a separate layer for the lines so that the they can have their own style.
	private Vector lineLayer;
	
	private Vector wrapLayer;
	
	private Bounds wmsExtent;
	private Bounds wrapExtent;
	private Bounds currentSelection = new Bounds(-180, -90, 180, 90);
	private Bounds dataBounds;
	
	private RegularPolygonHandler regularPolygonHandler;
	private DrawSingleFeatureOptions drawSingleFeatureOptionsForRectangle;
	private RegularPolygonHandlerOptions regularPolygonHandlerOptions;
	private DrawSingleFeature drawRectangle;
	
	private RegularPolygonHandlerOptions pathHandlerOptions;
	private DrawSingleFeatureOptions drawSingleFeatureOptionsForLines;
	
	private DrawSingleFeature drawXLine;
	private DrawSingleFeature drawYLine;
	private DrawSingleFeature drawPoint;
	
	private ModifyFeature modifyFeatureXY;
	private ModifyFeature modifyFeatureLine;
	
	private RegionWidget regionWidget = new RegionWidget();
    private LatLonWidget textWidget = new LatLonWidget();
	private Image helpButtonUp;
	private Image helpButtonDown;
	private PushButton helpButton;
	private Image resetButtonUp;
	private Image resetButtonDown;
	private PushButton resetButton;
	private Image drawButtonUp;
	private Image drawButtonDown;
	private ToggleButton drawButton;
	private Image panButtonUp;
	private Image panButtonDown;
	private ToggleButton panButton;
	private Image editButtonUp;
	private Image editButtonDown;
	private ToggleButton editButton;
	private Image settingsButtonUp;
	private Image settingsButtonDown;
	private PushButton settingsButton;
	private PopupPanel mapSettings;
	private VerticalPanel mapSettingsInterior;
	private CheckBox drawLock;
	private HorizontalPanel buttonPanel;
	private PopupPanel helpPanel;
	private VerticalPanel helpInterior;
	private Image closeUp;
	private Image closeDown;
	private PushButton helpClose;
	private PushButton settingsClose;
    private HTML help;
    private FlexTable topGrid;
  
    private boolean modulo = true;
	private boolean selectionMade = false;
	private boolean drawing = false;
	private boolean lockDraw = false;
	
    Boxes boxes = new Boxes("Valid Region");
	Box box = null;
	//public static final String WMS_URL = "http://strider.weathertopconsulting.com:8282/geoserver/wms?";
    public static final String WMS_URL = "http://labs.metacarta.com/wms/vmap0";
    
    double delta;
    
    boolean editing = false;
    
	public OLMapWidget() {
		regionWidget.setChangeListener(regionChangeListener);
		textWidget.addSouthChangeListener(southChangeListener);
		textWidget.addNorthChangeListener(northChangeListener);
		textWidget.addEastChangeListener(eastChangeListener);
		textWidget.addWestChangeListener(westChangeListener);
		
		closeUp = new Image(Util.getImageURL()+"close_off.png");
		closeDown = new Image(Util.getImageURL()+"close_on.png");
		
		mapSettings = new PopupPanel();
		mapSettingsInterior = new VerticalPanel();
		dockPanel = new DockPanel();
		helpPanel = new PopupPanel();
		helpInterior = new VerticalPanel();
	    buttonPanel = new HorizontalPanel();
		helpButtonUp = new Image(Util.getImageURL()+"info_off.png");
		helpButtonDown = new Image(Util.getImageURL()+"info_on.png");
		helpButton = new PushButton(helpButtonUp, helpButtonDown, new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				helpPanel.setPopupPosition(helpButton.getAbsoluteLeft()+256, helpButton.getAbsoluteTop()+20);
				helpPanel.show();			
			}
		});
		helpButton.setTitle("Help");
		helpButton.setStylePrimaryName("OL_MAP_image");
		
		helpClose = new PushButton(closeUp, closeDown, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				helpPanel.hide();
			}
			
		});
		helpClose.setTitle("Close");
		helpClose.setStylePrimaryName("OL_MAP_image_button_close");
		
		settingsClose = new PushButton(closeUp, closeDown, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mapSettings.hide();
			}
		});
		settingsClose.setTitle("Close");
		settingsClose.setStylePrimaryName("OL_MAP_image_button_close");
		
		resetButtonUp = new Image(Util.getImageURL()+"reset_off.png");
		resetButtonDown = new Image(Util.getImageURL()+"reset_on.png");
		resetButton = new PushButton(resetButtonUp, resetButtonDown, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				boxLayer.destroyFeatures();
				lineLayer.destroyFeatures();
				editing = false;
				setDataExtent(dataBounds.getLowerLeftY(), dataBounds.getUpperRightY(), dataBounds.getLowerLeftX(), dataBounds.getUpperRightX(), delta);
			    if ( !modulo ) {
			    	trimSelection(dataBounds);
			    }
			}
			
		});
		resetButton.setTitle("Reset Map");
		resetButton.setStylePrimaryName("OL_MAP_image_button");
		help = new HTML("<div style=\"font-family:verdana;font-size:9;\">" +
				"<ul><li>To select an area of the map, click the <img alt=\"draw\" src=\""+Util.getImageURL()+"draw_off.png"+"\"/> button then click and drag on the map.</li>" +
				"<li>When you finish drawing your selection, the selection button will deactivate.  I you want it to stay on click the <img alt=\"settings\" src=\""+Util.getImageURL()+"settings_off.png"+"\"/> button and check the box." +
				"<li>To modify a selection click the <img alt=\"edit\" src=\""+Util.getImageURL()+"edit_off.png"+"\"/> button and click the selected area to modify it.  (Click again when finished.)</li>" +
				"<li>To zoom, click the <img alt=\"zoom in\" src=\""+Util.getBaseURL()+"JavaScript/frameworks/OpenLayers/img/zoom-plus-mini.png\"/> and <img alt=\"zoom out\" src=\""+Util.getBaseURL()+"JavaScript/frameworks/OpenLayers/img/zoom-minus-mini.png\"/> buttons.</li>" +
				"<li>To pan the map, click the <img alt=\"pan\" src=\""+Util.getImageURL()+"pan_off.png\"/> button and click and drag on the map.</li>" +
				"<li>or click the <img alt=\"arrow \" src=\""+Util.getBaseURL()+"JavaScript/frameworks/OpenLayers/img/north-mini.png\"/> <img alt=\"arrow buttons\" src=\""+Util.getBaseURL()+"JavaScript/frameworks/OpenLayers/img/south-mini.png\"/> <img alt=\"arrow buttons\" src=\""+Util.getBaseURL()+"JavaScript/frameworks/OpenLayers/img/east-mini.png\"/> <img alt=\"arrow buttons\" src=\""+Util.getBaseURL()+"JavaScript/frameworks/OpenLayers/img/west-mini.png\"/> buttons.</li>" +
				"<li>To re-center and zoom out and keep your selection click on the <img alt=\"world\" src=\""+Util.getBaseURL()+"JavaScript/frameworks/OpenLayers/img/zoom-world-mini.png\"/> button.</li>" +
				"<li>To start over, click the <img alt=\"reset\" src=\""+Util.getImageURL()+"reset_off.png"+"\"/> button above the map.</li></ul>"+
		        "</div>");
		helpInterior.add(helpClose);
		helpInterior.add(help); 
		helpPanel.add(helpInterior);
		drawButtonUp = new Image(Util.getImageURL()+"draw_off.png");
		drawButtonDown = new Image(Util.getImageURL()+"draw_on.png");
		drawButton = new ToggleButton(drawButtonUp, drawButtonDown, drawButtonClickHandler);
		drawButton.setTitle("Draw Selection");
		drawButton.setStylePrimaryName("OL_MAP_image_button");
		panButtonUp = new Image(Util.getImageURL()+"pan_off.png");
		panButtonDown = new Image(Util.getImageURL()+"pan_on.png");
		panButton = new ToggleButton(panButtonUp, panButtonDown, panButtonClickHandler);
		panButton.setTitle("Pan Map");
		panButton.setStylePrimaryName("OL_MAP_image_button");
		panButton.setDown(true);
		editButtonUp = new Image(Util.getImageURL()+"edit_off.png");
		editButtonDown = new Image(Util.getImageURL()+"edit_on.png");
	    editButton = new ToggleButton(editButtonUp, editButtonDown, editButtonClickHandler);
		editButton.setTitle("Click on selection to edit.");
		editButton.setStylePrimaryName("OL_MAP_image_button");
		drawLock = new CheckBox("Keep map selection button active.");
		drawLock.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {

                 lockDraw = event.getValue();
				
			}
			
		});
		mapSettingsInterior.add(settingsClose);
		mapSettingsInterior.add(new Label("Check this box to keep the map region selector active until you click another button."));
		mapSettingsInterior.add(drawLock);
		mapSettings.add(mapSettingsInterior);
		settingsButtonUp = new Image(Util.getImageURL()+"settings_off.png");
		settingsButtonDown = new Image(Util.getImageURL()+"settings_on.png");
		settingsButton = new PushButton(settingsButtonUp, settingsButtonDown, settingsButtonClickHandler);
		settingsButton.setTitle("Map Widget Settings");
		settingsButton.setStylePrimaryName("OL_MAP_image_button");
		
		buttonPanel.add(helpButton);
		buttonPanel.add(resetButton);
		buttonPanel.add(editButton);
		buttonPanel.add(panButton);
		buttonPanel.add(drawButton);
		buttonPanel.add(settingsButton);
		
		topGrid = new FlexTable();
		topGrid.setWidget(0, 0, buttonPanel);
		topGrid.setWidget(1, 0, regionWidget);
		
		wmsExtent = new Bounds(-180, -90, 180, 90);
		wrapExtent  = new Bounds(-360, -90, 360, 90);
		dataBounds = wmsExtent;
		wmsMapOptions = new MapOptions();
		wmsMapOptions.setMaxExtent(wmsExtent);
		wmsMapOptions.setRestrictedExtent(wmsExtent);
		wrapMapOptions = new MapOptions();
		wrapMapOptions.setMaxExtent(wrapExtent);
		wrapMapOptions.setRestrictedExtent(wrapExtent);
		VectorOptions lineOptions = new VectorOptions();
		Style defaultStyle = new Style();
		defaultStyle.setStrokeWidth(4);
		Style selectedStyle = new Style();
		// Ripped from the OL source.  Bad I know, but ...
		// border: 2px solid blue;
	    // position: absolute;
	    // background-color: white;
	    // opacity: 0.50;
	    // font-size: 1px;
	    // filter: alpha(opacity=50);
        selectedStyle.setStrokeWidth(2);
        selectedStyle.setStrokeColor("blue");
        selectedStyle.setFillColor("blue");
        selectedStyle.setFillOpacity(.5);		
		StyleMap styles = new StyleMap(defaultStyle, selectedStyle, new Style());
		lineOptions.setStyleMap(styles);
		boxLayer = new Vector("Box Layer");
		lineLayer = new Vector("Line Layer", lineOptions);
		VectorOptions wrapLayerOptions = new VectorOptions();
		wrapLayerOptions.setIsBaseLayer(true);
		wrapLayer = new Vector("Wrap Layer", wrapLayerOptions);		
		MapWidget mapWidget = new MapWidget("256px","128px", wmsMapOptions);
		map = mapWidget.getMap();
	   
		//Add a WMS layer for a little background
		WMSParams wmsParams = new WMSParams();
		wmsParams.setFormat("image/png");
		wmsParams.setLayers("basic");
        WMSOptions wmsOptions = new WMSOptions();
        wmsOptions.setWrapDateLine(true);
        wmsOptions.setIsBaseLayer(false);
		wmsLayer = new WMS(
				"Basic WMS",
				WMS_URL,
				wmsParams,
				wmsOptions);
		
		map.addLayer(wrapLayer);
		map.addLayer(wmsLayer);
		map.addLayer(boxLayer);
		map.addLayer(lineLayer);
        
		drawSingleFeatureOptionsForRectangle = new DrawSingleFeatureOptions();
		regularPolygonHandlerOptions = new RegularPolygonHandlerOptions();
		regularPolygonHandlerOptions.setSides(4);
		regularPolygonHandlerOptions.setIrregular(true);
		//regularPolygonHandlerOptions.setKeyMask(Handler.MOD_CTRL);
		drawSingleFeatureOptionsForRectangle.setHandlerOptions(regularPolygonHandlerOptions);
				
		regularPolygonHandler = new RegularPolygonHandler();
		drawSingleFeatureOptionsForRectangle.onFeatureAdded(featureAddedListener);
		
		// The rectangle drawing control
		drawRectangle = new DrawSingleFeature(boxLayer, regularPolygonHandler, drawSingleFeatureOptionsForRectangle);
		
		pathHandlerOptions = new RegularPolygonHandlerOptions();
		//pathHandlerOptions.setKeyMask(Handler.MOD_CTRL);
		pathHandlerOptions.setIrregular(true);
		pathHandlerOptions.setSides(4);
		drawSingleFeatureOptionsForLines = new DrawSingleFeatureOptions();
		drawSingleFeatureOptionsForLines.onFeatureAdded(featureAddedListener);
		drawSingleFeatureOptionsForLines.setHandlerOptions(pathHandlerOptions);
		
		// The X-Line drawing control
		drawXLine = new DrawSingleFeature(lineLayer, new HorizontalPathHandler(), drawSingleFeatureOptionsForLines);
		
		// The Y-Line drawing control
		drawYLine = new DrawSingleFeature(lineLayer, new VerticalPathHandler(), drawSingleFeatureOptionsForLines);
		
		// The Point drawing control
		drawPoint = new DrawSingleFeature(boxLayer, new PointHandler(), drawSingleFeatureOptionsForLines);
		
		// Setup for modifying an XY shape...  Allows RESIZE, DRAG and RESHAPE...
		ModifyFeatureOptions modifyFeatureOptionsXY = new ModifyFeatureOptions();
		modifyFeatureOptionsXY.setDeleteCodes(new int[0]);
    	modifyFeatureOptionsXY.setMode(ModifyFeature.RESIZE|ModifyFeature.DRAG|ModifyFeature.RESHAPE);
		OnModificationListener onModification = new OnModificationListener() {
			@Override
			public void onModification(VectorFeature vectorFeature) {
				Geometry geo = Geometry.narrowToGeometry(vectorFeature.getGeometry().getJSObject());
				trimSelection(geo.getBounds());	
				selectionMade = true;
				featureModified();
			}
			
		};
		OnModificationStartListener onModificationStart = new OnModificationStartListener() {
			@Override
			public void onModificationStart(VectorFeature vectorFeature) {
				editing = true;
				drawing = false;
				drawButton.setDown(false);
				drawRectangle.deactivate();
				drawXLine.deactivate();
				drawYLine.deactivate();
				drawPoint.deactivate();
			}
		};
		OnModificationEndListener onModificationEnd = new OnModificationEndListener() {
			@Override
			public void onModificationEnd(VectorFeature vectorFeature) {
				editing = false;
			}
		};
		modifyFeatureOptionsXY.onModification(onModification);
		modifyFeatureOptionsXY.onModificationStart(onModificationStart);
		modifyFeatureOptionsXY.onModificationEnd(onModificationEnd);
		modifyFeatureXY = new ModifyFeature(boxLayer, modifyFeatureOptionsXY);
		
		// Setup for modifying line shape...  Allows RESIZE, DRAG...  RESHAPE is not allowed
		ModifyFeatureOptions modifyFeatureOptionsLine = new ModifyFeatureOptions();
		modifyFeatureOptionsLine.setDeleteCodes(new int[0]);
    	modifyFeatureOptionsLine.setMode(ModifyFeature.RESIZE|ModifyFeature.DRAG);
		modifyFeatureOptionsLine.onModification(onModification);
		modifyFeatureOptionsLine.onModificationStart(onModificationStart);
		modifyFeatureOptionsLine.onModificationEnd(onModificationEnd);
		modifyFeatureLine = new ModifyFeature(lineLayer, modifyFeatureOptionsLine);
			
		this.map.addControl(drawRectangle);
		this.map.addControl(drawXLine);
		this.map.addControl(drawYLine);
		this.map.addControl(drawPoint);
		this.map.addControl(modifyFeatureXY);
		this.map.addControl(modifyFeatureLine);
		
		drawRectangle.deactivate();
		drawXLine.deactivate();
		drawYLine.deactivate();
		drawPoint.deactivate();
		modifyFeatureXY.deactivate();
		modifyFeatureLine.deactivate();
		
		map.setCenter(new LonLat(0, 0), 0);
		map.setOptions(wrapMapOptions);		
		map.addMapMoveListener(mapMoveListener);
		dockPanel.add(topGrid, DockPanel.NORTH);
		dockPanel.add(mapWidget, DockPanel.CENTER);
		dockPanel.add(textWidget, DockPanel.SOUTH);
		initWidget(dockPanel);
	}
	public void setDataExtent(double slat, double nlat, double wlon, double elon, double delta) {
		this.delta = delta;
		dataBounds = new Bounds(wlon, slat, elon, nlat);
		double w = dataBounds.getWidth();
		double dt = Math.abs(360. - w);
		if ( dt <= 2.*delta ) {
			modulo = true;
			selectionMade = false;
		} else {
			modulo = false;
		}
		zoomMap();
		currentSelection = dataBounds;
		boxLayer.destroyFeatures();
		lineLayer.destroyFeatures();
		editing = false;
		// For now don't select the region at all.
//		if ( !modulo ) {
//		    boxLayer.addFeature(new VectorFeature(currentSelection.toGeometry()));
//		}
	}
	public void setDataExtent(double slat, double nlat, double wlon, double elon) {
		setDataExtent(slat, nlat, wlon, elon, delta);
	}
	private void zoomMap() {	
		int zoom = map.getZoomForExtent(dataBounds, false);
		if ( box != null ) {
		    boxes.removeMarker(box);
		}
		if ( !modulo ) {
			box = new Box(dataBounds);
			boxes.addMarker(box);
			map.addLayer(boxes);
		}
		mapOptions = new MapOptions();
		map.setOptions(mapOptions);		
		trimSelection(dataBounds);
		LonLat center = dataBounds.getCenterLonLat();
		// The selected region cannot not be centered exactly.
		// Back out the zoom and do the best you can.
		if ( center.lon() + 180. > 360. ) {
			zoom = 0;
			center = new LonLat(180., 0.);
		}
		map.setCenter(center, zoom);
	}
	private void zoomMapToSelection() {
		int zoom = map.getZoomForExtent(currentSelection, false);
		LonLat center = currentSelection.getCenterLonLat();
		if ( center.lon() + 180. > 360. ) {
			zoom = 0;
			center = new LonLat(180., 0.);
		}
		map.setCenter(center, zoom);
	}
	private void panMapToSelection() {
		int zoom = map.getZoom();
		LonLat center = currentSelection.getCenterLonLat();
		if ( center.lon() + 180. > 360. ) {
			zoom = 0;
			center = new LonLat(180., 0.);
		}
		map.setCenter(center, zoom);		
	}
	MapMoveListener mapMoveListener = new MapMoveListener() {

		@Override
		public void onMapMove(MapMoveEvent eventObject) {
			LonLat center = map.getCenter();
			if ( modulo && !selectionMade ) {
				setSelection(new Bounds(center.lon() - 180.0, center.lat() - 90., center.lon() + 180.0, center.lat() + 90.));
			}
			mapMoved();
		}	
	};
	FeatureAddedListener featureAddedListener = new FeatureAddedListener() {

		@Override
		public void onFeatureAdded(VectorFeature vectorFeature) {
			// How come there is no narrowToGeometry with a Geometry argument.
			Geometry geo = Geometry.narrowToGeometry(vectorFeature.getGeometry().getJSObject());
			trimSelection(geo.getBounds());
			selectionMade = true;
			
			if ( !lockDraw ) {
				drawing = false;
				panButton.setDown(true);
				drawButton.setDown(false);
				editButton.setDown(false);
				drawRectangle.deactivate();
				drawXLine.deactivate();
				drawYLine.deactivate();
				drawPoint.deactivate();
				modifyFeatureLine.deactivate();
				modifyFeatureXY.deactivate();
			}
            
			featureAdded();
		}
	};
	private void setSelection(Bounds bounds) {
		currentSelection = bounds;
		if ( tool.equals("xy") || tool.equals("t") || tool.equals("z") || tool.equals("zt") || tool.equals("pt") ) {
			textWidget.setText(currentSelection.getUpperRightY(), currentSelection.getLowerLeftY(), 
					currentSelection.getUpperRightX(), currentSelection.getLowerLeftX());
		} else if ( tool.equals("x") || tool.equals("xz") || tool.equals("xt") ) {
			LonLat c = currentSelection.getCenterLonLat();
			textWidget.setText(c.lat(), c.lat(), 
					currentSelection.getUpperRightX(), currentSelection.getLowerLeftX() );
		} else if ( tool.equals("y") || tool.equals("yz") || tool.equals("yt")) {
			LonLat c = currentSelection.getCenterLonLat();
			textWidget.setText(currentSelection.getUpperRightY(), currentSelection.getLowerLeftY(), 
					c.lon(), c.lon());
		}
	}
	private void trimSelection(Bounds bounds) {	
		editing = false;

		// Always trip the north and south bounds
		double s_data = dataBounds.getLowerLeftY();
		double n_data = dataBounds.getUpperRightY();
		double w_data = dataBounds.getLowerLeftX();
		double e_data = dataBounds.getUpperRightX();
		double w_selection = bounds.getLowerLeftX();
		double e_selection = bounds.getUpperRightX();
		double s_selection = bounds.getLowerLeftY();
		double n_selection = bounds.getUpperRightY();
		if ( tool.equals("xy") || tool.equals("x") || tool.equals("y") ) {
			if ( s_selection < s_data ) {
				s_selection = s_data;
			}
			if ( n_selection > n_data ) {
				n_selection = n_data;
			}
		}
		Bounds selectionBounds;
		if ( modulo || dataBounds.containsBounds(bounds, false, true) ) {
			lineLayer.destroyFeatures();
			boxLayer.destroyFeatures();
			selectionBounds = new Bounds(w_selection, s_selection, e_selection, n_selection);
		} else {
			// Only trip the east west if the data is not modulo
			if ( w_selection < w_data ) {
				w_selection = w_data;
			}
			if ( e_selection > e_data ) {
				e_selection = e_data;
			}
			selectionBounds = new Bounds(w_selection, s_selection, e_selection, n_selection);

		}
		LonLat center = selectionBounds.getCenterLonLat();
		if ( tool.equals("xy") ) {
			// Use it.
			boxLayer.addFeature(new VectorFeature(selectionBounds.toGeometry()));
			setSelection(selectionBounds);
		} else if ( tool.equals("x") || tool.equals("xz") || tool.equals("xt") ) {
			// Modify it then use it.
			selectionBounds = new Bounds(w_selection, center.lat(), e_selection, center.lat());
			lineLayer.addFeature(new VectorFeature(selectionBounds.toGeometry()));
			setSelection(selectionBounds);
		} else if (  tool.equals("y") || tool.equals("yz") || tool.equals("yt") ) {
			selectionBounds = new Bounds(center.lon(), s_selection, center.lon(), n_selection);
			lineLayer.addFeature(new VectorFeature(selectionBounds.toGeometry()));
			setSelection(selectionBounds);
		} else if ( tool.equals("t") || tool.equals("z") || tool.equals("zt") || tool.equals("pt") ) {
			Point p = new Point(selectionBounds.getCenterLonLat().lon(), selectionBounds.getCenterLonLat().lat());
			boxLayer.addFeature(new VectorFeature(p));
			setSelection(selectionBounds);
		}
	}
	public void setCurrentSelection(double slat, double nlat, double wlon, double elon) {
		editing = false;
		// Only set this if it is an actual sub-region of the data region of a global data set.
		boolean subRegion = true;
		if (modulo) {
			subRegion =  !((Math.abs(dataBounds.getLowerLeftY() - slat) < 1.5) && (Math.abs(dataBounds.getUpperRightY() - nlat ) < 1.5) &&
					       (Math.abs(dataBounds.getLowerLeftX() - wlon) < 1.5) && (Math.abs(dataBounds.getUpperRightX() - elon) < 1.5));
		}
		if ( subRegion ) {
			selectionMade = true;
			Bounds bounds = new Bounds(wlon, slat, elon, nlat);
			lineLayer.destroyFeatures();
			boxLayer.destroyFeatures();
			if ( tool.equals("t") || tool.equals("z") || tool.equals("zt") || tool.equals("pt") ) {	
				Point p = new Point(bounds.getCenterLonLat().lon(), bounds.getCenterLonLat().lat());
				boxLayer.addFeature(new VectorFeature(p));
			} else if ( tool.equals("x") || tool.equals("xz") || tool.equals("xt") ) {
				Bounds lineBounds = new Bounds(wlon, bounds.getCenterLonLat().lat(), elon, bounds.getCenterLonLat().lat());
				trimSelection(lineBounds);
//				lineLayer.addFeature(new VectorFeature(lineBounds.toGeometry()));
			} else if ( tool.equals("y") || tool.equals("yz") || tool.equals("yt") ) {
				Bounds lineBounds = new Bounds(bounds.getCenterLonLat().lon(), slat, bounds.getCenterLonLat().lon(), nlat);
				trimSelection(lineBounds);
//				lineLayer.addFeature(new VectorFeature(lineBounds.toGeometry()));
			} else {
				trimSelection(bounds);
				// XY box
				//boxLayer.addFeature(new VectorFeature(bounds.toGeometry()));
			}
		}
	}
	public void setTool(String tool) {
		if ( !this.tool.equals(tool) ) {
			editing = false;
			this.tool = tool;
			LonLat l = currentSelection.getCenterLonLat();
			
			double halfx = Math.min(Math.abs(dataBounds.getUpperRightX() - l.lon())/2.0, 
					Math.abs(l.lon() - dataBounds.getLowerLeftX())/2.0);
			double halfy = Math.min(Math.abs(dataBounds.getUpperRightY() - l.lat())/2.0,
					Math.abs(l.lat() - dataBounds.getLowerLeftY())/2.0);
			
			
			drawRectangle.deactivate();
			drawXLine.deactivate();
			drawYLine.deactivate();
			if ( !lockDraw ) {
				drawPoint.deactivate();
				drawing = false;
				drawButton.setDown(false);
			}
			// Draw the box from the center of the current geometry to half of the shortest distance to the edge of the data bounds.			
			
			Bounds b = new Bounds(l.lon()-halfx, l.lat()-halfy, l.lon()+halfx, l.lat()+halfy);
			trimSelection(b);

			if ( tool.equals("t") || tool.equals("z") || tool.equals("zt") || tool.equals("pt") ) {
				// Disable selecting for points
				editButton.setEnabled(false);
			} else {
				editButton.setEnabled(true);
			}
		}
	}
	public ClickHandler editButtonClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			drawing = false;
			panButton.setDown(false);
			editButton.setDown(true);
			drawButton.setDown(false);
			drawPoint.deactivate();
			drawRectangle.deactivate();
			drawXLine.deactivate();
			drawYLine.deactivate();
			if ( tool.equals("xy") ) {
				if ( boxLayer.getFeatures() == null ) {
					Window.alert("Make a selection on the map then select this button, to edit it.");
					panButton.setDown(true);
					drawButton.setDown(false);
					editButton.setDown(false);
				} else {
				    modifyFeatureXY.activate();
				    modifyFeatureLine.deactivate();
				}
			} else if ( tool.equals("x") || tool.equals("xz") || tool.equals("xt") ||
					tool.equals("y") || tool.equals("yz") || tool.equals("yt") ) { 	
				if ( lineLayer.getFeatures() == null ) {
					Window.alert("Make a selection on the map then select button, to edit it.");
					panButton.setDown(true);
					drawButton.setDown(false);
					editButton.setDown(false);
				} else {
					modifyFeatureXY.deactivate();
					modifyFeatureLine.activate();
				}

			} else if ( tool.equals("t") || tool.equals("z") || tool.equals("zt") || tool.equals("pt") ) {
				// A view of z to t is a point tool type

				modifyFeatureXY.deactivate();
				modifyFeatureLine.deactivate();

			} 	
		}

	};
	
	public ClickHandler settingsButtonClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			
			mapSettings.setPopupPosition(settingsButton.getAbsoluteLeft(), settingsButton.getAbsoluteTop()-25);
			mapSettings.show();
			
		}
		
	};
	public ClickHandler panButtonClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			drawing = false;
			panButton.setDown(true);
			drawButton.setDown(false);
			editButton.setDown(false);
			drawPoint.deactivate();
			drawRectangle.deactivate();
			drawXLine.deactivate();
			drawYLine.deactivate();
		    modifyFeatureXY.deactivate();
		    modifyFeatureLine.deactivate();
		}
		
	};
	public ClickHandler drawButtonClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			if ( !drawing ) {
				// Drawing is not active, so activate it.
				drawing = true;
				panButton.setDown(false);
				editButton.setDown(false);
				drawButton.setDown(true);
				if ( tool.equals("xy") ) {

					drawRectangle.activate();
					drawXLine.deactivate();
					drawYLine.deactivate();
					drawPoint.deactivate();

				} else if ( tool.equals("x") || tool.equals("xz") || tool.equals("xt") ) {

					drawRectangle.deactivate();
					drawXLine.activate();
					drawYLine.deactivate();
					drawPoint.deactivate();

				} else if ( tool.equals("y") || tool.equals("yz") || tool.equals("yt") ) {

					drawRectangle.deactivate();
					drawXLine.deactivate();
					drawYLine.activate();
					drawPoint.deactivate();

				} else if ( tool.equals("t") || tool.equals("z") || tool.equals("zt") || tool.equals("pt") ) {
					// A view of z to t is a point tool type

					drawRectangle.deactivate();
					drawXLine.deactivate();
					drawYLine.deactivate();
					drawPoint.activate();

				} 
			} else {
				// Turn off drawing to allow selections
				drawing = false;
				drawButton.setDown(false);
				panButton.setDown(true);
				editButton.setDown(false);
				if ( tool.equals("xy") ) {

					drawRectangle.deactivate();
					drawXLine.deactivate();
					drawYLine.deactivate();
					drawPoint.deactivate();

				} else if ( tool.equals("x") || tool.equals("xz") || tool.equals("xt") ) {

					drawRectangle.deactivate();
					drawXLine.deactivate();
					drawYLine.deactivate();
					drawPoint.deactivate();

				} else if ( tool.equals("y") || tool.equals("yz") || tool.equals("yt") ) {

					drawRectangle.deactivate();
					drawXLine.deactivate();
					drawYLine.deactivate();
					drawPoint.deactivate();

				} else if ( tool.equals("t") || tool.equals("z") || tool.equals("zt") || tool.equals("pt") ) {
					// A view of z to t is a point tool type

					drawRectangle.deactivate();
					drawXLine.deactivate();
					drawYLine.deactivate();
					drawPoint.deactivate();

				} 
			}
		}		
	};
	/**
	 * A listener that will handle change events when the user types text into the TextBox with the south latitude value.
	 */
	public ChangeListener southChangeListener = new ChangeListener() {

		public void onChange(Widget sender) {
			TextBox tb = (TextBox) sender;
			double ylo = textWidget.getYlo();
			double yhi = textWidget.getYhi();
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
			if ( ylo < dataBounds.getLowerLeftY() ) {
				ylo = dataBounds.getLowerLeftY();
			}
			if ( ylo > yhi ) {
				ylo = yhi;
			}
			
			setCurrentSelection(ylo, yhi, currentSelection.getLowerLeftX(), currentSelection.getUpperRightX());
			zoomMapToSelection();
		}
	};
	/**
	 * A listener that will handle change events when the user types text into the TextBox with the north latitude value.
	 */
	public ChangeListener northChangeListener = new ChangeListener() {

		public void onChange(Widget sender) {
			TextBox tb = (TextBox) sender;
			double ylo = textWidget.getYlo();
			double yhi = textWidget.getYhi();
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
			if ( yhi > dataBounds.getUpperRightY() ) {
				yhi = dataBounds.getUpperRightY();
			}

			if ( yhi < ylo ) {
				yhi = ylo;
			}
			
			setCurrentSelection(ylo, yhi, currentSelection.getLowerLeftX(), currentSelection.getUpperRightX());
			zoomMapToSelection();
		}
	};
	/**
	 * A listener that will handle change events when the user types text into the TextBox with the west longitude value.
	 */
	public ChangeListener westChangeListener = new ChangeListener() {

		public void onChange(Widget sender) {
			TextBox tb = (TextBox) sender;
			double xlo = textWidget.getXlo();
			double xhi = textWidget.getXhi();
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
			xlo = GeoUtil.normalizeLon(xlo);
			if ( !modulo ) {
				// This is not a global data set, make sure we're not west of the data bounds.
				if ( xlo < GeoUtil.normalizeLon(dataBounds.getLowerLeftX()) ) {
					xlo = dataBounds.getLowerLeftX();
				}
			} else {
				// It's a modulo data set, make the east and west wrap as appropriate.
				if ( xlo >= GeoUtil.normalizeLon(xhi) ) {
					
					xhi = GeoUtil.normalizeLon(xhi);
					while ( xlo >= xhi ) {
						xhi = xhi + 360.;
					}
				}
			}
			
//			if ( xlo > xhi ) {
//				xlo = xhi;
//			}
			
			setCurrentSelection(currentSelection.getLowerLeftY(), currentSelection.getUpperRightY(), xlo, xhi);
			zoomMapToSelection();
		}
	};
	/**
	 * A listener that will handle change events when the user types text into the TextBox with the east longitude value.
	 */
	public ChangeListener eastChangeListener = new ChangeListener() {
		
		public void onChange(Widget sender) {
			TextBox tb = (TextBox) sender;
			double xlo = textWidget.getXlo();
			double xhi = textWidget.getXhi();
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
			
			xhi = GeoUtil.normalizeLon(xhi);
			if ( !modulo ) {
				// This is not a global data set, make sure we're not east of the data bounds.
				if ( xhi > GeoUtil.normalizeLon(dataBounds.getUpperRightX()) ) {
					xhi = dataBounds.getUpperRightX();
				}
			} else {
				// It's a modulo data set, make the east and west wrap as appropriate.
				if ( xlo >= GeoUtil.normalizeLon(xhi) ) {
					while ( xlo >= xhi ) {
						xhi = xhi + 360.;
					}
				}
			}
			
			setCurrentSelection(currentSelection.getLowerLeftY(), currentSelection.getUpperRightY(), xlo, xhi);
			zoomMapToSelection();
		}
	};

	public double getXlo() {
		return textWidget.getXlo();
	}
	public double getXhi() {
		return textWidget.getXhi();
	}
	public double getYhi() {
		return textWidget.getYhi();
	}
	public double getYlo() {
		return textWidget.getYlo();
	}
	public String getYhiFormatted() {
		return textWidget.getYhiFormatted();
	}
	public String getYloFormatted() {
		return textWidget.getYloFormatted();
	}
	public String getXhiFormatted() {
		return textWidget.getXhiFormatted();
	}
	public String getXloFormatted() {
		return textWidget.getXloFormatted();
	}
	public ChangeListener regionChangeListener = new ChangeListener() {
		@Override
		public void onChange(Widget sender) {	
			
			int i = regionWidget.getSelectedIndex();
			String value = regionWidget.getItemText(i);
			double[] reg = regionWidget.getRegion(i, value);
			// s, n, w, e
			if ( reg != null ) {
				selectionMade = true;
				setCurrentSelection(reg[0], reg[1], reg[2], reg[3]);
				zoomMapToSelection();
			}	
			regionWidget.setSelectedIndex(0);
			// This call back is called by the drawing control when a feature is added by drawing.
			// We need to call it ourselves when the region widget fires.
			featureAdded();
		}
	};

	public void render() {
		map.render();
	}
	public double[] getDataExtent() {
		double[] d = new double[4];
		// n, s, e, w
		d[0] = dataBounds.getUpperRightY();
		d[1] = dataBounds.getLowerLeftY();
		d[2] = dataBounds.getUpperRightX();
		d[3] = dataBounds.getLowerLeftX();
		return d;
	}
	public double[] getCurrentSelection() {
		double[] cs = new double[4];
		// n, s, e, w
		cs[0] = currentSelection.getUpperRightY();
		cs[1] = currentSelection.getLowerLeftY();
		cs[2] = currentSelection.getUpperRightX();
		cs[3] = currentSelection.getLowerLeftX();
		return cs;
	}
	public double getDelta() {
		return delta;
	}
	public int getZoom() {
		return map.getZoom();
	}
	public double[] getCenterLatLon() {
		LonLat centerLonLat = map.getCenter();
		double[] center = new double[2];
		center[0] = centerLonLat.lat();
		center[1] = centerLonLat.lon();
		return center;
	}
	public void setCenter(double lat, double lon, int zoom) {
		LonLat c = new LonLat(lon, lat);
		map.setCenter(c, zoom);
	}
	public boolean isEditing() {
		return editing;
	}
	public static native void featureAdded() /*-{
        if (typeof $wnd.featureAddedCallback == 'function') {
            $wnd.featureAddedCallback();
        }
    }-*/;
    public static native void featureModified() /*-{
	    if(typeof $wnd.featureModifiedCallback == 'function') {
            $wnd.featureModifiedCallback();
	    }
    }-*/;
    public static native void mapMoved()/*-{
    	if (typeof $wnd.mapMovedCallback == 'function') {
    		$wnd.mapMovedCallback();
    	}
    }-*/;
	public native void activateNativeHooks()/*-{
		var localMap = this;
        $wnd.setMapCurrentSelection = function(slat, nlat, wlon, elon) {       	
	        localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::setCurrentSelection(DDDD)(slat, nlat, wlon, elon);
        } 
        $wnd.setMapTool = function(tool) {
        	localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::setTool(Ljava/lang/String;)(tool);
        }
        $wnd.setMapDataExtent = function(slat, nlat, wlon, elon, delta) {
        	localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::setDataExtent(DDDDD)(slat, nlat, wlon, elon, delta);
        }
        $wnd.getMapZoom = function() {
        	var zm = localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::getZoom()();
        	return zm;
        }
        $wnd.getMapXhi = function() {
        	var p = localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::getXhi()();
        	return p;
        }
        $wnd.getMapXlo = function() {
        	var p = localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::getXlo()();
        	return p;
        }
        $wnd.getMapYhi = function() {
        	var p = localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::getYhi()();
        	return p;
        }
        $wnd.getMapYlo = function() {
        	var p = localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::getYlo()();
        	return p;
        }
        $wnd.isFeatureEditing = function() {
        	var p = localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::isEditing()();
        	return p;
        }
        $wnd.zoomAndPanToSelection = function() {
        	localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::zoomMapToSelection()();
        }
         $wnd.panToSelection = function() {
        	localMap.@gov.noaa.pmel.tmap.las.client.map.OLMapWidget::panMapToSelection()();
        }
    }-*/;
}
