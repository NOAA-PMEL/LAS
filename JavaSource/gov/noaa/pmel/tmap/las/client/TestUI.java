package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.OperationsMenu;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.vizgal.VizGalPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;


public class TestUI extends LASEntryPoint {
	/*
	private MapWidget mapWidget;
	private Map map;
	private WMS wmsLayer;
	private Markers markers;
	private Popup popup;
	public void onModuleLoad() {
		super.onModuleLoad();
		MapOptions mapOptions = new MapOptions();
		mapOptions.setControls(new JObjectArray(new JSObject[] {}));
		mapOptions.setNumZoomLevels(16);
		mapOptions.setProjection("EPSG:4326");

		// let's create map widget and map objects
		mapWidget = new MapWidget("350px", "350px", mapOptions);
		map = mapWidget.getMap();
		markers = new Markers("marker layer");
		
		WMSParams wmsParams = new WMSParams();
		wmsParams.setFormat("image/png");
		wmsParams.setLayers("tiger-ny");
		wmsParams.setStyles("");
		wmsParams.setMaxExtent(new Bounds(-74.047185, 40.679648, -73.907005, 40.882078));

		wmsLayer = new WMS("WMS Layer", "http://localhost:8080/geoserver/wms", wmsParams);

		// let's add layers and controls to map
		map.addLayers(new Layer[] {wmsLayer, markers});

		map.addControl(new PanZoomBar(RootPanel.get("nav").getElement()));
		map.addControl(new MousePosition(RootPanel.get("position").getElement()));
		map.addControl(new Scale(RootPanel.get("scale").getElement()));
		map.addControl(new MouseToolbar());
		map.addControl(new LayerSwitcher());
		
		LonLat center = new LonLat(-73.99, 40.73);
		map.setCenter(center, 13);

		// add marker
		Size size = new Size(10,17);
		Pixel offset = new Pixel(-5, -17);
		Icon icon = new Icon("img/marker.png", size, offset);
		Marker marker = new Marker(center, icon);
		markers.addMarker(marker);
		marker.getEvents().register("mouseover", marker, new EventHandler() {
			public void onHandle(JSObject source, JSObject[] param) {
				Marker marker = Marker.narrowToMarker(source);
				if (popup != null) {
					map.removePopup(popup);
				}

				popup = new AnchoredBubble("marker-info",
						marker.getLonLat(),
						new Size(120,80),
						"<p>You moved near " + marker.getLonLat().lon() + " : " + marker.getLonLat().lat() + "</p>" ,
						new Icon("", new Size(0,0), new Pixel(0,0)),
						true);
			map.addPopup(popup);

			}
		});

		// register mouse out event
		marker.getEvents().register("mouseout", marker, new EventHandler() {
			public void onHandle(JSObject source, JSObject[] param) {
				Marker marker = Marker.narrowToMarker(source);
				if (popup != null) {
					map.removePopup(popup);
				}
			}
		});

		DockPanel dockPanel = new DockPanel();
		dockPanel.add(mapWidget, DockPanel.CENTER);
		dockPanel.setBorderWidth(1);
		RootPanel.get("map").add(dockPanel);

		
	}
	*/
	
	HTML output;
	VizGalPanel panel;
	Grid layout = new Grid(2, 1);
	OperationsMenu operationsMenu;
	String dsid;
	String vid;
	String op;
	String optionID;
	String view;
	int rightPad = 15;
	VariableSerializable var;
	ArrayList<String> ortho = new ArrayList<String>();
	String compareAxis;
	public void onModuleLoad() {
		super.onModuleLoad();
		Map<String, List<String>> parameters = Window.Location.getParameterMap();
		dsid = parameters.get("dsid").get(0);
		vid = parameters.get("vid").get(0);
		//TODO If the operation is null, get the default operation (the map or plot; left nav) for this view.
		op = parameters.get("opid").get(0);
		optionID = parameters.get("optionid").get(0);
		view = parameters.get("view").get(0);
		if ( dsid != null && vid != null & op != null && view != null) {
			// If the proper information was sent to the widget, pull down the variable definition
			// and initialize the slide sorter with this Ajax call.
			rpcService.getCategories(dsid, initPanelCallback);
		}
		operationsMenu = new OperationsMenu();
		RootPanel.get("menu").add(operationsMenu);
		
		Window.addWindowResizeListener(windowResizeListener);
	}
	public AsyncCallback initPanelCallback = new AsyncCallback() {
		public void onSuccess(Object result) {
			CategorySerializable[] cats = (CategorySerializable[]) result;
			if ( cats != null && cats.length > 1 ) {
				Window.alert("Multiple categories found.");
			} else {
				if ( cats[0].isVariableChildren() ) {
					DatasetSerializable ds = cats[0].getDatasetSerializable();
					VariableSerializable[] vars = ds.getVariablesSerializable();

					for (int i=0; i < vars.length; i++ ) {
						if ( vars[i].getID().equals(vid) ) {
							var = vars[i];
					        initPanel();
						}
					}
				}
			}
		}
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes VizGal."+caught.toString());
		}
	};
	private void initPanel() {
		if ( view.equals("xy") ) {
			// If the plot view is XY set up the map for selecting the region in all panels.
			// TODO Still need this for other views but with parameters to set the map selector tool.
			GridSerializable ds_grid = var.getGrid();
			double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
			double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

			double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
			double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

			double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));

			LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(grid_south, grid_west), LatLng.newInstance(grid_north, grid_east));
		}
		// Examine the variable axes and determine which are orthogonal to the view. 

		if ( var.getGrid().getXAxis() != null && !view.contains("x") ) {
			ortho.add("x");
		}
		if ( var.getGrid().getYAxis() != null && !view.contains("y") ) {
			ortho.add("y");
		}
		if ( var.getGrid().getZAxis() != null && !view.contains("z") ) {
			ortho.add("z");
		}
		if ( var.getGrid().getTAxis() != null && !view.contains("t") ) {
			ortho.add("t");
		}
		if ( ortho.contains("t") ) {
			compareAxis = "t";
		}  else if ( ortho.contains("z") ) {
			compareAxis = "z";
		} else if ( ortho.contains("y") ) {
			compareAxis = "y";
		} else if ( ortho.contains("x") ) {
			compareAxis = "x";
		}
		if ( ortho.size() == 0 ) {
			Window.alert("There are no axes orthogonal to the view on which the data can be compared.");
		} else if ( ortho.size() > 2 ) { 
			Window.alert("There are "+ortho.size()+" orthogonal axes.  The SlideSorterOld only allows 2.");
		} else {
			ortho.clear();
			int width = Window.getClientWidth();
			int pwidth = (width-rightPad);
			panel = new VizGalPanel("LAS", false, op, optionID, view, productServer, true, rpcService);
			panel.setVariable(var);
			panel.init(true);
			panel.addCompareAxisChangeListener(onAxisChange);
			RootPanel.get("panel").add(panel);
			panel.setPanelWidth(pwidth);
			panel.addApplyListener(panelApply);
			panel.refreshPlot(null, false, false);
		}
	}
	ClickListener panelApply = new ClickListener() {
		public void onClick(Widget sender) {
			panel.refreshPlot(null, false, true);
		}		
	};
	public WindowResizeListener windowResizeListener = new WindowResizeListener() {
		public void onWindowResized(int width, int height) {
			int pwidth = (width-rightPad);
			if (panel != null ) {
	            panel.setPanelWidth(pwidth);
			}
		}
	};
	public TreeListener datasetTreeListener = new TreeListener() {
		public void onTreeItemSelected(TreeItem item) {
			Object u = item.getUserObject();
			if ( u instanceof VariableSerializable ) {
				var = (VariableSerializable) u;
				initPanel();
			}
		}

		public void onTreeItemStateChanged(TreeItem item) {
			// TODO Auto-generated method stub

		}

	};
	public ChangeListener onAxisChange = new ChangeListener() {
		public void onChange(Widget sender) {
			panel.refreshPlot(null, false, true);	
		}
	};
	
}
