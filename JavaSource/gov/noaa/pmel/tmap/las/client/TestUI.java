package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.Constants;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequestWrapper;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationPushButton;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsMenu;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.RegionSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.URLUtil;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;


public class TestUI extends BaseUI {
	
	String tInitialTime;
	String tInitialZ;

	String openid;

	// Keep track of the current operations
	OperationSerializable[] ops;

	// Sometimes  you need to keep the current map selection values.
	double[] cs = null;

	PopupPanel initializing = new PopupPanel();

	// This is the main UI Panel
	FlexTable uiPanel = new FlexTable();

	boolean changeDataset = false;

	OperationsMenu tOperationsMenu = new OperationsMenu();

	List<String> tTribs;
	int tTribIndex;


	public void onModuleLoad() {

		super.onModuleLoad();
		activateNativeHooks();
		activateNativeHooks();	

		openid = Util.getParameterString("openid");

		String spinImageURL = URLUtil.getImageURL()+"/mozilla_blu.gif";
		HTML output = new HTML("<img src=\""+spinImageURL+"\" alt=\"Spinner\"/> Initializing...");
		initializing.add(output);
		initializing.show();


		if ( xOperationID == null ) {
			xOperationID = "Plot_2D_XY_zoom";
		}
		if ( xOptionID == null ) {
			xOptionID = "Options_2D_image_contour_xy_7";
		}


		tOperationsMenu.addClickHandler(tExternalOperationClickHandler);
		uiPanel.setWidget(0, 0, tOperationsMenu);
		uiPanel.setWidget(1, 0, xMainPanel);

		// Set the required handlers...
		setOperationsClickHandler(operationsClickHandler);
		setDatasetSelectionHandler(datasetSelectionHandler);
		setDatasetOpenHandler(datasetOpenHandler);
		setOptionsOkHandler(optionsOkHandler);

		// Add the apply button handler
		xAxesWidget.addApplyHandler(settingsButtonApplyHandler);

		RootPanel.get("main").add(uiPanel);	
		
		String initial_auth = Util.getParameterString("auth_url");
		if ( initial_auth != null ) {
			doNextAuth(null, initial_auth);
		} else {
			startUI();
		}

		


	}

	//	public AsyncCallback authenticateCallback = new AsyncCallback() {
//		@Override
//		public void onFailure(Throwable caught) {
//			if ( xDSID != null && xVarID != null && xOperationID != null && xView != null && xOptionID != null) {
//				xOptionsButton.setOptions(xOptionID);
//				Util.getRPCService().getCategories(xDSID, initPanelFromParametersCallback);
//			} else {
//				Util.getRPCService().getPropertyGroup("product_server", initPanelFromDefaultsCallback);	
//			}
//		}
//
//		@Override
//		public void onSuccess(Object result) {
//			CategorySerializable[] cats = (CategorySerializable[]) result;
//			xAuthURLs = new String[cats.length-1];
//			int urlIndex = 0;
//			for ( int i = 0; i < cats.length; i++ ) {
//				String url = cats[i].getAttributes().get("remote_las");
//				if ( url != null ) {
//					xAuthURLs[urlIndex] = cats[i].getAttributes().get("remote_las");
//					urlIndex++;
//				}
//			}
//			
//			xSettingsHeader.setOpen(false);
//			startUI();
//		}
//	};
	private void doNextAuth(CategorySerializable cat, String url) {		
		    xSettingsHeader.setOpen(false);
			final PopupPanel authPanel = new PopupPanel(true);
			final VerticalPanel authInterior = new VerticalPanel();
			authPanel.add(authInterior);
			int h = Window.getClientHeight();
			if ( h > 300 ) {
				h = h - 100;
			} else {
				h = h - 10;
			}
			int w = Window.getClientWidth();
			if ( w > 300 ) {
				w = w - 100;
			} else {
				w = w - 10;
			}
			
			authPanel.setWidth(w+"px");
			authPanel.setHeight(h+"px");
			Label authLabel = new Label("Authenticating at remote LAS sites...       ");
			HorizontalPanel topBar = new HorizontalPanel();
			topBar.add(authLabel);
			final CategorySerializable rcat = cat;
			final String rurl = url;
			Button control = new Button("Close");
				control.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent arg0) {
						
						authPanel.hide();
						xSettingsHeader.setOpen(true);
						testImageLoad(rcat, rurl);
					}

				});
			
			topBar.add(control);
			authInterior.add(topBar);
			
			authPanel.show();
			
			if ( openid != null ) {
				url = url+"?openid="+openid;
			}
			Frame authFrame = new Frame(url);
			authFrame.setWidth(w-10+"px");
			authFrame.setHeight(h-10+"px");
			authInterior.add(authFrame);

	}
	private void testImageLoad(CategorySerializable cat, String url) {
	    final String auth_url = url;
		String image_url = url.replace("auth.do", "output/test.png");
		final String furl = url.replace("auth.do","");
		final CategorySerializable fcat = cat;
		Image image = new Image();
		image.addErrorHandler(new ErrorHandler() {

			@Override
			public void onError(ErrorEvent e) {
				
                Window.alert("Failed to authenticate at "+furl+". Plots from this server will not be visible.");
				
			}
			
		});
		image.addLoadHandler(new LoadHandler() {

			@Override
			public void onLoad(LoadEvent arg0) {
				if ( fcat != null ) {
				    fcat.setAttribute("authenticated", "true");
				} else {
					startUI();
					xDatasetButton.setAuthenticated(auth_url, "true");
				}
				
			}
			
		});
		image.setUrl(image_url);
		RootPanel.get("__esg_authenticateFrame").add(image);
	}
	private void startUI() {
		if ( xDSID != null && xVarID != null && xOperationID != null && xView != null && xOptionID != null) {
			xOptionsButton.setOptions(xOptionID);
			Util.getRPCService().getCategories(xDSID, initPanelFromParametersCallback);
		} else {
			Util.getRPCService().getPropertyGroup("product_server", initPanelFromDefaultsCallback);	
		}
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
					xDSID = value;
				} else if ( name.equals(Constants.DEFAULT_VARID) ) {
					xVarID = value;
				} else if ( name.equals(Constants.DEFAULT_OP) ) {
					xOperationID = value;
				} else if ( name.equals(Constants.DEFAULT_OPTION) ) {
					xOptionID = value; 
				} else if ( name.equals(Constants.DEFAULT_VIEW) ) {
					xView = value;
				} else if ( name.equals(Constants.DEFAULT_TIME) ) {
					tInitialTime = value;
				} else if ( name.equals(Constants.DEFAULT_Z) ) {
					tInitialZ = value;
				}
			}

			initializing.hide();
			if ( xDSID != null && xVarID != null && xOperationID != null && xView != null && xOptionID != null) {
				xOptionsButton.setOptions(xOptionID);
				Util.getRPCService().getCategories(xDSID, initPanelFromParametersCallback);
			} else {
				Window.alert("Initalization failed.");
			}
		}		
	};
	
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
						if ( vars[i].getID().equals(xVarID) ) {
							xVariable = vars[i];
							// View is null to get all operations
							Util.getRPCService().getConfig(null, xVariable.getDSID(), xVariable.getID(), getGridCallback);
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

		xPanels.get(0).setVariable(xVariable);
		xPanels.get(0).init(false, ops);

		if ( xView == null ) {
			xView = "xy";
		}
		GridSerializable ds_grid = xVariable.getGrid();
		double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
		double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

		double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
		double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

		double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));
		xAxesWidget.getRefMap().setTool(xView);
		xAxesWidget.getRefMap().setDataExtent(grid_south, grid_north, grid_west, grid_east, delta);
		xOperationsWidget.setOperations(xVariable.getIntervals(), xVariable.getDSID(), xVariable.getID(), xOperationID, xView);
		tOperationsMenu.setMenus(ops, xView);
		// Examine the variable axes and determine which are orthogonal to the view. 


		if ( xVariable.getGrid().getXAxis() != null && !xView.contains("x") ) {
			xOrtho.add("x");
		}
		if ( xVariable.getGrid().getYAxis() != null && !xView.contains("y") ) {
			xOrtho.add("y");
		}
		if ( xVariable.getGrid().getZAxis() != null && !xView.contains("z") ) {
			xOrtho.add("z");
		}
		if ( xVariable.getGrid().getTAxis() != null && !xView.contains("t") ) {
			xOrtho.add("t");
		}


		xAxesWidget.init(xVariable.getGrid());
		xAxesWidget.setFixedAxis(xView, xOrtho, null);		


		xPanels.get(0).addApplyHandler(panelApply);

		// Move the current state of the axes to the panel
		if ( xTlo != null && !xTlo.equals("") ) {
			xPanels.get(0).setAxisRangeValues("t", xTlo, xThi);
			xAxesWidget.getTAxis().setLo(xTlo);
			xAxesWidget.getTAxis().setHi(xThi);
		} else {
			if ( xVariable.getGrid().hasT() ) {
				xPanels.get(0).setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
		}
		if ( xZlo != null && !xZlo.equals("") ) {
			xPanels.get(0).setAxisRangeValues("z", xZlo, xZhi);
			xAxesWidget.getZAxis().setLo(xZlo);
			xAxesWidget.getZAxis().setHi(xZhi);
		} else {
			if ( xVariable.getGrid().hasZ() ) {
				xPanels.get(0).setAxisRangeValues("z", xAxesWidget.getZAxis().getHi(), xAxesWidget.getZAxis().getLo());
			}
		}


		// If these limits are not the same as the dataBounds, then set them.
		if ( xXlo != null && !xXlo.equals("") && xXhi != null && !xXhi.equals("") && 
				xYlo != null && !xYlo.equals("") && xYhi != null && !xYhi.equals("") ) {
			xAxesWidget.getRefMap().setCurrentSelection(Double.valueOf(xYlo), Double.valueOf(xYhi), Double.valueOf(xXlo), Double.valueOf(xXhi));
			xPanels.get(0).setMapTool(xView);
			xPanels.get(0).setLatLon(xYlo, xYhi, xXlo, xXhi);
		} else {
			double tmp_xlo = xAxesWidget.getRefMap().getXlo();
			double tmp_xhi = xAxesWidget.getRefMap().getXhi();
			double tmp_ylo = xAxesWidget.getRefMap().getYlo();
			double tmp_yhi = xAxesWidget.getRefMap().getYhi();
			xPanels.get(0).setMapTool(xView);
			xPanels.get(0).setLatLon(String.valueOf(tmp_ylo), String.valueOf(tmp_yhi), String.valueOf(tmp_xlo), String.valueOf(tmp_xhi));
		}


		xPanels.get(0).refreshPlot(null, false, false);

		resize();
	}
	public void applyChange() {
		if ( changeDataset ) {
			cs = xAxesWidget.getRefMap().getCurrentSelection();
			// This involves a jump across the wire, so the finishApply gets called in the callback from the getGrid.
			changeDataset();
		} else {
			// No jump required, just finish up now.
			String op_id = xOperationsWidget.getCurrentOperation().getID();
			String op_view = xOperationsWidget.getCurrentView();
			if ( !op_id.equals(xOperationID) && !op_view.equals(xView) ) {
				xOperationID = op_id;
				xView = op_view;
			}
			// The view may have changed if the operation changed before the apply.
			xAxesWidget.getRefMap().setTool(xView);
			if ( xVariable.getGrid().hasT() ) {
				if ( xView.contains("t") ) {
					xAxesWidget.getTAxis().setRange(true);
					xPanels.get(0).setPanelAxisRange("t", true);
				} else {
					xAxesWidget.getTAxis().setRange(false);
					xPanels.get(0).setPanelAxisRange("t", false);
				}				
				xPanels.get(0).setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			if ( xVariable.getGrid().hasZ() ) {
				if ( xView.contains("z") ) {
					xAxesWidget.getZAxis().setRange(true);
					xPanels.get(0).setPanelAxisRange("z", true);
				} else {
					xAxesWidget.getZAxis().setRange(false);
					xPanels.get(0).setPanelAxisRange("z", false);
				}
				xPanels.get(0).setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}

			double tmp_xlo = xAxesWidget.getRefMap().getXlo();
			double tmp_xhi = xAxesWidget.getRefMap().getXhi();

			double tmp_ylo = xAxesWidget.getRefMap().getYlo();
			double tmp_yhi = xAxesWidget.getRefMap().getYhi();

			xPanels.get(0).setLatLon(String.valueOf(tmp_ylo), String.valueOf(tmp_yhi), String.valueOf(tmp_xlo), String.valueOf(tmp_xhi));

			Map<String, String> temp_state = xOptionsButton.getState();
			xPanels.get(0).refreshPlot(temp_state, false, true);
		}
	}
	/**
	 * A little helper method to change data sets.
	 */
	public void changeDataset() {
		xVariable = xNewVariable;

		changeDataset = false;

		// Since we are changing data sets, go to the default plot and view.

		// TODO Maybe we can derive the default operations from the data set during the init(), but it would require an asynchronous request
		// to know the default operation for the new dataset and variable...
		if ( xNewVariable.getAttributes().get("grid_type").equals("regular") ) {
			xOperationID = "Plot_2D_XY_zoom";
		} else if ( xNewVariable.getAttributes().get("grid_type").equals("vector") ) {
			xOperationID = "Plot_vector";
		} else {
			xOperationID = "Insitu_extract_location_value_plot";
		}
		xView = "xy";

		// Get all the config info.  View is null to get all operations.

		Util.getRPCService().getConfig(null, xVariable.getDSID(), xVariable.getID(), getGridCallback);

	}
	ClickHandler optionsOkHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			applyChange();
		}

	};
	ClickHandler settingsButtonApplyHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {
			applyChange();
		}
	};
	ClickHandler panelApply = new ClickHandler() {
		@Override
		public void onClick(ClickEvent arg0) {
			xPanels.get(0).refreshPlot(xOptionsButton.getState(), false, true);
		}		
	};
	AsyncCallback<ConfigSerializable> getGridCallback = new AsyncCallback<ConfigSerializable>() {
		public void onSuccess(ConfigSerializable config) {

			GridSerializable grid = config.getGrid();
			RegionSerializable[] regions = config.getRegions();
			xAxesWidget.getRefMap().setRegions(regions);
			ops = config.getOperations();
			xVariable.setGrid(grid);
			//xAnalysisWidget.setAnalysisAxes(grid);
			if ( xPanels == null || xPanels.size() == 0 ) {
				TestUI.super.init(1, Constants.FRAME);
			}
			initPanel();

		}

		@Override
		public void onFailure(Throwable caught) {
			initializing.hide();
			Window.alert("Could not fetch grid.  "+caught.getLocalizedMessage());

		}

	};

	public SelectionHandler<TreeItem> datasetSelectionHandler = new SelectionHandler<TreeItem>() {

		@Override
		public void onSelection(SelectionEvent<TreeItem> event) {
			Object u = event.getSelectedItem().getUserObject();
			if ( u instanceof VariableSerializable ) {
				xVariable = (VariableSerializable) u;
				Util.getRPCService().getConfig(xView, xVariable.getDSID(), xVariable.getID(), getGridCallback);
			} else if ( u instanceof CategorySerializable ) {
				CategorySerializable cat = (CategorySerializable) u;
				String remote_url = cat.getAttributes().get("remote_las");
				if ( remote_url != null ) {
					String auth = cat.getAttributes().get("authenticated");
					if ( auth == null ) {
						doNextAuth(cat, remote_url);
				    } else if ( !auth.equals("true") ) {
				    	doNextAuth(cat, remote_url);
				    }
				}
			}
		}
	};
	public OpenHandler<TreeItem> datasetOpenHandler = new OpenHandler<TreeItem>() {

		@Override
		public void onOpen(OpenEvent<TreeItem> event) {
			Object u = event.getTarget().getUserObject();
			if ( u instanceof VariableSerializable ) {
				xVariable = (VariableSerializable) u;
				Util.getRPCService().getConfig(xView, xVariable.getDSID(), xVariable.getID(), getGridCallback);
			} else if ( u instanceof CategorySerializable ) {
				CategorySerializable cat = (CategorySerializable) u;
				String remote_url = cat.getAttributes().get("remote_las");
				if ( remote_url != null ) {
					String auth = cat.getAttributes().get("authenticated");
					if ( auth == null ) {
						doNextAuth(cat, remote_url);
				    } else if ( !auth.equals("true") ) {
				    	doNextAuth(cat, remote_url);
				    }
				}
			}
			
		}
		
	};
	public ChangeHandler onAxisChange = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent arg0) {
			xPanels.get(0).refreshPlot(null, false, true);	
		}
	};
	public ClickHandler operationsClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			xView = xOperationsWidget.getCurrentView();
			xOperationID = xOperationsWidget.getCurrentOperation().getID();
			xPanels.get(0).setOperation(xOperationID, xView);
			applyChange();	
		}
	};
	public ClickHandler tExternalOperationClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			OperationPushButton b = (OperationPushButton) event.getSource();
			OperationSerializable xOperationID = b.getOperation();
			LASRequestWrapper lasRequest = xPanels.get(0).getRequest();
			lasRequest.setOperation(xOperationID.getID(), "v7");
			String features = "toolbar=1,location=1,directories=1,status=1,menubar=1,scrollbars=1,resizable=1"; 
			Window.open(Util.getProductServer()+"?xml="+URL.encode(lasRequest.getXMLText()), xOperationID.getName(), features);
		}

	};
}
