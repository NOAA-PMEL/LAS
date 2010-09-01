package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.AxesWidgetGroup;
import gov.noaa.pmel.tmap.las.client.laswidget.Constants;
import gov.noaa.pmel.tmap.las.client.laswidget.DatasetButton;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequestWrapper;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationPushButton;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsMenu;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.OptionsButton;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.URLUtil;
import gov.noaa.pmel.tmap.las.client.util.Util;
import gov.noaa.pmel.tmap.las.client.vizgal.VizGalPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;


public class TestUI implements EntryPoint {
	HTML output;
	VizGalPanel panel;
	Grid layout = new Grid(2, 1);
	
	String dsid;
	String vid;
	String op;
	String option;
	String view;
	String initial_time = null;
	String initial_z = null;
	int topPad = 120;
	VariableSerializable var;
	ArrayList<String> ortho = new ArrayList<String>();
	/*
	 * Padding on the right side of the browser frame...
	 */
	int rightPad = 45;
	/*
	 * Fixed size of the left control panel
	 */
	int controlsWidth = 290;
	String settingsWidth = String.valueOf(controlsWidth) + "px";
	// Left-hand nav
	FlexTable settingsControls;
	DisclosurePanel settingsHeader = new DisclosurePanel("Settings");
	AxesWidgetGroup axesWidget = new AxesWidgetGroup("Plot Axes", "Other Axes", "vertical", settingsWidth, "Axes");
	OperationsWidget operationsWidget = new OperationsWidget("operations");
	
	// Top nav
	FlexTable buttonControls = new FlexTable();
	DatasetButton datasetButton;
	OptionsButton optionsButton;
	OperationsMenu operationsMenu;
	
	DockPanel dockPanel = new DockPanel();
	PopupPanel initializing = new PopupPanel();
	
	OperationSerializable[] ops;
	
	String xlo;
	String xhi;
	String ylo;
	String yhi;
	String zlo;
	String zhi;
	String tlo;
	String thi;
	
	/*
	 * Keep track of the new variable and change state to be able apply data set changes only after apply button is pressed.
	 */
	VariableSerializable nvar;
	boolean changeDataset = false;
    // Sometimes  you need to keep the current map selection values.
    double[] cs = null;
    
    DisclosurePanel hideControls = new DisclosurePanel("");
    
    boolean operationsPanelIsOpen = true;
    boolean panelHeaderHidden = false;
    
    int pwidth = 0;
    /*
	 * Image size control.
	 */
	ListBox imageSize;
	Label imageSizeLabel = new Label("Image zoom: ");
	
	public void onModuleLoad() {
		
		String spinImageURL = URLUtil.getImageURL()+"/mozilla_blu.gif";
		output = new HTML("<img src=\""+spinImageURL+"\" alt=\"Spinner\"/> Initializing...");
	    initializing.add(output);
	    initializing.show();
		Map<String, List<String>> parameters = Window.Location.getParameterMap();
		dsid = Util.getParameterString("dsid");
		vid = Util.getParameterString("vid");
		op = Util.getParameterString("opid");
		option = Util.getParameterString("optionid");
		view = Util.getParameterString("view");
		xlo = Util.getParameterString("xlo");
		xhi = Util.getParameterString("xhi");
		ylo = Util.getParameterString("ylo");
		yhi = Util.getParameterString("yhi");
		zlo = Util.getParameterString("zlo");
		zhi = Util.getParameterString("zhi");
		tlo = Util.getParameterString("tlo");
		thi = Util.getParameterString("thi");
		
		if ( dsid != null && vid != null && op != null && view != null && option != null) {
			Util.getRPCService().getCategories(dsid, initPanelFromParametersCallback);
		} else {
			Util.getRPCService().getPropertyGroup("product_server", initPanelFromDefaultsCallback);	
		}

		if ( op == null ) {
			op = "Plot_2D_XY";
		}
		if ( option == null ) {
			option = "Options_2D_image_contour_xy_7";
		}
		settingsHeader.setOpen(true);		
		settingsHeader.addCloseHandler(new CloseHandler<DisclosurePanel> () {

			@Override
			public void onClose(CloseEvent<DisclosurePanel> event) {
				operationsWidget.setOpen(false);
				axesWidget.setOpen(false);
			}
			
		});
		settingsHeader.addOpenHandler(new OpenHandler<DisclosurePanel>() {

			@Override
			public void onOpen(OpenEvent<DisclosurePanel> arg0) {
				operationsWidget.setOpen(true);
				axesWidget.setOpen(true);
			}
			
		});
		axesWidget.addApplyHandler(settingsButtonApplyHandler);
		settingsControls = new FlexTable();
		settingsControls.setWidget(0, 0, settingsHeader);
		
		settingsControls.setWidget(1, 0, axesWidget);
		settingsControls.setWidget(2, 0, operationsWidget);
		operationsWidget.addClickHandler(operationsClickHandler);
		operationsMenu = new OperationsMenu();
		operationsMenu.addClickHandler(externalOperationClick);
		
		datasetButton = new DatasetButton();
		datasetButton.ensureDebugId("datasetButton");
		datasetButton.addTreeListener(datasetTreeListener);
		// This is all to get around the fact that the OpenLayers map is always in front.
		datasetButton.addOpenClickHandler(datasetOpenHandler);
		datasetButton.addCloseClickHandler(datasetCloseHandler);
		optionsButton = new OptionsButton(option, 0);
		optionsButton.ensureDebugId("optionsButton");
		optionsButton.addOpenClickHandler(datasetOpenHandler);
		optionsButton.addCloseClickHandler(datasetCloseHandler);
		// TODO need to refresh the plot  =---- > optionsButton.addOkClickListener(optionsOkListener);
		hideControls.setOpen(true);
		hideControls.addCloseHandler(new CloseHandler<DisclosurePanel>() {
			@Override
			public void onClose(CloseEvent<DisclosurePanel> arg0) {
				handlePanelShowHide();
			}
		});
		hideControls.addOpenHandler(new OpenHandler<DisclosurePanel>() {
			@Override
			public void onOpen(OpenEvent<DisclosurePanel> arg0) {
				handlePanelShowHide();
			}
		});
		imageSize = new ListBox();
		imageSize.addItem("Auto", "auto");
		imageSize.addItem("100%", "100");
		imageSize.addItem(" 90%", "90");
		imageSize.addItem(" 80%", "80");
		imageSize.addItem(" 70%", "70");
		imageSize.addItem(" 60%", "60");
		imageSize.addItem(" 50%", "50");
		imageSize.addItem(" 40%", "40");
		imageSize.addItem(" 30%", "30");

		imageSize.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent arg0) {
				String value = imageSize.getValue(imageSize.getSelectedIndex());
				if ( !value.equals("auto") ) {
					
						panel.setImageSize(Integer.valueOf(value).intValue());
					
				} else {
					if ( pwidth == 0 ) {
						int win = Window.getClientWidth();
						if ( panelHeaderHidden ) {
							pwidth = (win - rightPad);
						} else {
							pwidth = (win-(rightPad+controlsWidth));
						}
						if ( pwidth <= 0 ) {
							pwidth = 400;
						}
					}
					panel.setPanelWidth(pwidth);
				}
			}		
		});
		
		
		buttonControls.setWidget(0, 0, hideControls);
		buttonControls.setWidget(0, 1, datasetButton);
		buttonControls.setWidget(0, 2, optionsButton);
		buttonControls.setWidget(0, 3, operationsMenu);
		buttonControls.setWidget(0, 4, imageSizeLabel);
		buttonControls.setWidget(0, 5, imageSize);
		dockPanel.add(buttonControls, DockPanel.NORTH);
		dockPanel.add(settingsControls, DockPanel.WEST);
		
		RootPanel.get("main").add(dockPanel);	
		Window.addWindowResizeListener(windowResizeListener);
	}
	public void handlePanelShowHide() {
		if ( panelHeaderHidden ) {
			panel.show();
			dockPanel.add(settingsControls, DockPanel.WEST);
			pwidth = pwidth - controlsWidth/2;
			if ( imageSize.getValue(imageSize.getSelectedIndex()).equals("auto") ) {
				panel.setPanelWidth(pwidth);
			}
		} else {
			panel.hide();
			dockPanel.remove(settingsControls);
			panel.hide();
			pwidth = pwidth + controlsWidth/2;
			if ( imageSize.getValue(imageSize.getSelectedIndex()).equals("auto") ) {
				panel.setPanelWidth(pwidth);
			}
		}
		panelHeaderHidden = !panelHeaderHidden;
		// TODO get history working 
		//pushHistory();
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
				Util.getRPCService().getCategories(dsid, initPanelFromParametersCallback);
			} else {
				// set a default view.
				view = "xy";
			}
			initializing.hide();
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
							// View is null to get all operations
							Util.getRPCService().getConfig(null, var.getDSID(), var.getID(), getGridCallback);
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
		if ( view == null ) {
			view = "xy";
		}
		GridSerializable ds_grid = var.getGrid();
		double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
		double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

		double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
		double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

		double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));
		axesWidget.getRefMap().setTool(view);
		axesWidget.getRefMap().setDataExtent(grid_south, grid_north, grid_west, grid_east, delta);
		operationsWidget.setOperations(var.getIntervals(), var.getDSID(), var.getID(), op, view);
        operationsMenu.setMenus(ops, view);
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

		if ( ortho.size() == 0 ) {
			Window.alert("There are no axes orthogonal to the view on which the data can be compared.");
		} else if ( ortho.size() > 2 ) { 
			Window.alert("There are "+ortho.size()+" orthogonal axes.  The SlideSorterOld only allows 2.");
		} else {
			
			axesWidget.init(var.getGrid());
			axesWidget.setFixedAxis(view, ortho, null);		
			
			if ( panel != null ) {
				dockPanel.remove(panel);
			}
			panel = new VizGalPanel("LAS", true, op, option, view, true);
			panel.setVariable(var);
			panel.init(false, ops);

			panel.addApplyHandler(panelApply);
			
			// Move the current state of the axes to the panel
			if ( tlo != null && !tlo.equals("") ) {
				panel.setAxisRangeValues("t", tlo, thi);
				axesWidget.getTAxis().setLo(tlo);
				axesWidget.getTAxis().setHi(thi);
			} else {
				if ( var.getGrid().hasT() ) {
					panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
				}
			}
			if ( zlo != null && !zlo.equals("") ) {
				panel.setAxisRangeValues("z", zlo, zhi);
				axesWidget.getZAxis().setLo(zlo);
				axesWidget.getZAxis().setHi(zhi);
			} else {
				if ( var.getGrid().hasZ() ) {
					panel.setAxisRangeValues("z", axesWidget.getZAxis().getHi(), axesWidget.getZAxis().getLo());
				}
			}


			// If these limits are not the same as the dataBounds, then set them.
			if ( xlo != null && !xlo.equals("") && xhi != null && !xhi.equals("") && 
				 ylo != null && !ylo.equals("") && yhi != null && !yhi.equals("") ) {
				axesWidget.getRefMap().setCurrentSelection(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
				panel.setMapTool(view);
				panel.setLatLon(ylo, yhi, xlo, xhi);
			} else {
				double tmp_xlo = axesWidget.getRefMap().getXlo();
				double tmp_xhi = axesWidget.getRefMap().getXhi();
				double tmp_ylo = axesWidget.getRefMap().getYlo();
				double tmp_yhi = axesWidget.getRefMap().getYhi();
			    panel.setMapTool(view);
				panel.setLatLon(String.valueOf(tmp_ylo), String.valueOf(tmp_yhi), String.valueOf(tmp_xlo), String.valueOf(tmp_xhi));
			}
					
			panel.refreshPlot(null, false, false);
			int win = Window.getClientWidth();
			if ( panelHeaderHidden ) {
				pwidth = (win - rightPad);
			} else {
				pwidth = (win-(rightPad+controlsWidth));
			}
			if ( pwidth <= 0 ) {
				pwidth = 400;
			}
			panel.setPanelWidth(pwidth);
			dockPanel.add(panel, DockPanel.CENTER);
			
		}
	}
	public void applyChange() {
		if ( changeDataset ) {
			cs = axesWidget.getRefMap().getCurrentSelection();
			// This involves a jump across the wire, so the finishApply gets called in the callback from the getGrid.
			changeDataset();
		} else {
			// No jump required, just finish up now.
			String op_id = operationsWidget.getCurrentOperation().getID();
			String op_view = operationsWidget.getCurrentView();
			if ( !op_id.equals(op) && !op_view.equals(view) ) {
				op = op_id;
				view = op_view;
			}
			// The view may have changed if the operation changed before the apply.
			axesWidget.getRefMap().setTool(view);
			if ( var.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
			}
			if ( var.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", axesWidget.getZAxis().getHi(), axesWidget.getZAxis().getLo());
			}
			
			double tmp_xlo = axesWidget.getRefMap().getXlo();
			double tmp_xhi = axesWidget.getRefMap().getXhi();

			double tmp_ylo = axesWidget.getRefMap().getYlo();
			double tmp_yhi = axesWidget.getRefMap().getYhi();
			
			panel.setLatLon(String.valueOf(tmp_ylo), String.valueOf(tmp_yhi), String.valueOf(tmp_xlo), String.valueOf(tmp_xhi));
			// TODO GEt an options button for kripes sake Map<String, String> temp_state = new HashMap<String, String>(optionsButton.getState());
			panel.refreshPlot(null, false, true);
		}
	}
	/**
	 * A little helper method to change data sets.
	 */
	public void changeDataset() {
		var = nvar;
		
		changeDataset = false;

		// Since we are changing data sets, go to the default plot and view.

		// TODO Maybe we can derive the default operations from the data set during the init(), but it would require an asynchronous request
		// to know the default operation for the new dataset and variable...
		if ( nvar.getAttributes().get("grid_type").equals("regular") ) {
			op = "Plot_2D_XY_zoom";
		} else if ( nvar.getAttributes().get("grid_type").equals("vector") ) {
			op = "Plot_vector";
		} else {
			op = "Insitu_extract_location_value_plot";
		}
		view = "xy";

		// Get all the config info.  View is null to get all operations.
		//TODO a call back for getting the grid of the new variable...
		Util.getRPCService().getConfig(null, var.getDSID(), var.getID(), null);
		
	}
	ClickHandler settingsButtonApplyHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {
			applyChange();
		}
	};
	ClickHandler panelApply = new ClickHandler() {
		@Override
		public void onClick(ClickEvent arg0) {
//			panel.setLatLon(String.valueOf(settingsControls.getRefMap().getYlo()), String.valueOf(settingsControls.getRefMap().getYhi()), String.valueOf(settingsControls.getRefMap().getXlo()), String.valueOf(settingsControls.getRefMap().getXhi()));
			panel.refreshPlot(optionsButton.getState(), false, true);
		}		
	};
	AsyncCallback<ConfigSerializable> getGridCallback = new AsyncCallback<ConfigSerializable>() {
		public void onSuccess(ConfigSerializable config) {
            
			GridSerializable grid = config.getGrid();
			ops = config.getOperations();
			var.setGrid(grid);
			initPanel();
			
		}

		@Override
		public void onFailure(Throwable caught) {
			initializing.hide();
			Window.alert("Could not fetch grid.  "+caught.getLocalizedMessage());
			
		}
		
	};
	// In vizGal we monitor the window width since there are two across.  In this case we want the height to fit.
	public WindowResizeListener windowResizeListener = new WindowResizeListener() {
		public void onWindowResized(int width, int height) {
			int pheight = (height-topPad);
			if (panel != null ) {
	            panel.setPanelHeight(pheight);
			}
		}
	};
	public TreeListener datasetTreeListener = new TreeListener() {
		public void onTreeItemSelected(TreeItem item) {
			Object u = item.getUserObject();
			if ( u instanceof VariableSerializable ) {
				var = (VariableSerializable) u;
				Util.getRPCService().getConfig(view, var.getDSID(), var.getID(), getGridCallback);
			}
		}

		public void onTreeItemStateChanged(TreeItem item) {
			// TODO Auto-generated method stub

		}

	};
	public ChangeHandler onAxisChange = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent arg0) {
			panel.refreshPlot(null, false, true);	
		}
	};
	public ClickHandler operationsClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			view = operationsWidget.getCurrentView();
			op = operationsWidget.getCurrentOperation().getID();
			panel.setOperation(op, view);
			if ( view.contains("t") ) {
				panel.setParentAxisRange("t", true);
			} else {
				panel.setParentAxisRange("t", false);
			}
			if ( view.contains("z") ) {
				panel.setParentAxisRange("z", true);
			} else {
				panel.setParentAxisRange("z", false);
			}		
		}
	};
	public ClickHandler externalOperationClick = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			OperationPushButton b = (OperationPushButton) event.getSource();
			OperationSerializable op = b.getOperation();
			LASRequestWrapper lasRequest = panel.getRequest();
			lasRequest.setOperation(op.getID(), "v7");
			String features = "toolbar=1,location=1,directories=1,status=1,menubar=1,scrollbars=1,resizable=1"; 
			Window.open(Util.getProductServer()+"?xml="+URL.encode(lasRequest.getXMLText()), op.getName(), features);
		}
		
	};
	ClickHandler datasetOpenHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {
			
			axesWidget.closePanels();
			operationsPanelIsOpen = operationsWidget.isOpen();
			operationsWidget.setOpen(false);
			
		}
		
	};
	ClickHandler datasetCloseHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {
			
			axesWidget.restorePanels();
			operationsWidget.setOpen(operationsPanelIsOpen);
		}
		
		
	};
}
