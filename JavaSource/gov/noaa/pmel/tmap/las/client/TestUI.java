package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.OperationsMenu;
import gov.noaa.pmel.tmap.las.client.laswidget.Util;
import gov.noaa.pmel.tmap.las.client.laswidget.Constants;
import gov.noaa.pmel.tmap.las.client.map.SettingsWidget;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.vizgal.VizGalPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;


public class TestUI extends LASEntryPoint {
	HTML output;
	VizGalPanel panel;
	Grid layout = new Grid(2, 1);
	OperationsMenu operationsMenu;
	String dsid;
	String vid;
	String op;
	String option;
	String view;
	String initial_time = null;
	String initial_z = null;
	int rightPad = 15;
	VariableSerializable var;
	ArrayList<String> ortho = new ArrayList<String>();
	String compareAxis;
	SettingsWidget settingsControls;
	DockPanel dockPanel = new DockPanel();
	PopupPanel initializing = new PopupPanel();
	public void onModuleLoad() {
		super.onModuleLoad();
		String spinImageURL = Util.getImageURL()+"/mozilla_blu.gif";
		output = new HTML("<img src=\""+spinImageURL+"\" alt=\"Spinner\"/> Initializing...");
	    initializing.add(output);
	    initializing.show();
		Map<String, List<String>> parameters = Window.Location.getParameterMap();
		dsid = getParameterString("dsid");
		vid = getParameterString("vid");
		op = getParameterString("opid");
		option = getParameterString("optionid");
		view = getParameterString("view");
		
		
		if ( dsid != null && vid != null && op != null && view != null && option != null) {
			rpcService.getCategories(dsid, initPanelFromParametersCallback);
		} else {
			rpcService.getPropertyGroup("product_server", initPanelFromDefaultsCallback);	
		}

		if ( op == null ) {
			op = "Plot_2D_XY";
		}
		if ( option == null ) {
			option = "Options_2D_image_contour_xy_7";
		}
		settingsControls = new SettingsWidget("Settings", "LAS", op, option, rpcService, "panel");
		settingsControls.addDatasetTreeListener(datasetTreeListener);
		settingsControls.addApplyClickListener(panelApply);
		operationsMenu = new OperationsMenu();
		dockPanel.add(operationsMenu, DockPanel.NORTH);
		dockPanel.add(settingsControls, DockPanel.WEST);
		RootPanel.get("main").add(dockPanel);	
		Window.addWindowResizeListener(windowResizeListener);
	}
	public AsyncCallback initPanelFromDefaultsCallback = new AsyncCallback() {

		@Override
		public void onFailure(Throwable caught) {
			// Ok with me.  User will just have to select a data set.
			initializing.hide();
		}

		@Override
		public void onSuccess(Object result) {
			HashMap<String, String> product_server = (HashMap<String, String>) result;
			for (Iterator nameIt = product_server.keySet().iterator(); nameIt.hasNext();) {
				String name = (String) nameIt.next();
				String value = product_server.get(name);
				if ( name.equals(Constants.DEFAULT_DSID) ) {
					dsid = value;
				} else if ( name.equals(Constants.DEFAULT_VARID) ) {
					vid = value;
				} else if ( name.equals(Constants.DEFAULT_OP) ) {
					op = value;
				} else if ( name.equals(Constants.DEFAULT_OPTION) ) {
					option = value; 
				} else if ( name.equals(Constants.DEFAULT_VIEW) ) {
					view = value;
				} else if ( name.equals(Constants.DEFAULT_TIME) ) {
					initial_time = value;
				} else if ( name.equals(Constants.DEFAULT_Z) ) {
					initial_z = value;
				}
			}
			if ( dsid != null && vid != null && op != null && view != null && option != null) {
				rpcService.getCategories(dsid, initPanelFromParametersCallback);
			}
		}
		
	};
	// TODO you're going to have to fix this to work, but for now...
	public AsyncCallback initPanelFromParametersCallback = new AsyncCallback() {
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
							rpcService.getGrid(var.getDSID(), var.getID(), getGridCallback);
						}
					}
				}
			}
		}
		public void onFailure(Throwable caught) {
			initializing.hide();
			Window.alert("Failed to initalizes VizGal."+caught.toString());
		}
	};
	private void initPanel() {
		initializing.hide();
		if ( view.equals("xy") ) {
			// If the plot view is XY set up the map for selecting the region in all panels.
			// TODO Still need this for other views but with parameters to set the map selector tool.
			GridSerializable ds_grid = var.getGrid();
			double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
			double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

			double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
			double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

			double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));

			settingsControls.setToolType(view);
			settingsControls.getRefMap().setDataExtent(grid_south, grid_north, grid_west, grid_east, delta);
			settingsControls.setOperations(rpcService, var.getIntervals(), var.getDSID(), var.getID(), op, view, null);
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
			panel = new VizGalPanel("LAS", true, op, option, view, productServer, true, rpcService);
			panel.setVariable(var);
			panel.init(false);
			panel.addCompareAxisChangeListener(onAxisChange);
			panel.setPanelWidth(pwidth);
			panel.addApplyListener(panelApply);
			panel.refreshPlot(null, false, false);
			dockPanel.add(panel, DockPanel.CENTER);
		}
	}
	ClickListener panelApply = new ClickListener() {
		public void onClick(Widget sender) {
			panel.setLatLon(String.valueOf(settingsControls.getRefMap().getXlo()), String.valueOf(settingsControls.getRefMap().getXhi()), String.valueOf(settingsControls.getRefMap().getYlo()), String.valueOf(settingsControls.getRefMap().getYhi()));
			panel.refreshPlot(settingsControls.getOptions(), false, true);
		}		
	};
	AsyncCallback getGridCallback = new AsyncCallback() {
		public void onSuccess(Object result) {
            
			GridSerializable grid = (GridSerializable) result;
			var.setGrid(grid);
			initPanel();
			
		}

		@Override
		public void onFailure(Throwable caught) {
			initializing.hide();
			Window.alert("Could not fetch grid.  "+caught.getLocalizedMessage());
			
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
				rpcService.getGrid(var.getDSID(), var.getID(), getGridCallback);
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
