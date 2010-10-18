package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.Constants;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationRadioButton;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;
import gov.noaa.pmel.tmap.las.client.laswidget.OutputPanel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

/**
 * A UI widget with one or more panels containing an LAS product with widgets to interact with the specifications of the products.
 * @author rhs
 *
 */
public class VizGal extends BaseUI {
	
	
	
	/*
	 * The main panel for this UI, has custom vizGal Buttons and the BaseUI main panel
	 */
	FlexTable vVizGalPanel = new FlexTable();


	/*
	 * Keep track of which axis is in the plot panels.
	 */
	String compareAxis;

	/*
	 * Keep track of which axis is selected in the header.
	 */
	String fixedAxis;

	/*
	 * Button to make slide sorter compute differences
	 */
	ToggleButton differenceButton;
	
	FlexTable buttonLayout = new FlexTable();
	FlexCellFormatter buttonFormatter;

	/*
	 * The initial variable for when we run off the top off the history stack.
	 */
	VariableSerializable initial_var;

	/*
	 * Global min and max for setting contour levels.
	 */
	double globalMin =  999999999.;
	double globalMax = -999999999.;

	/*
	 * Button to set the contour levels automatically.
	 */
	ToggleButton autoContourButton;
	TextBox autoContourTextBox;

	boolean changeDataset = false;
	
	/*
	 * Keep if a history token is attached to an initial URL,  keep it and apply it after the VizGal has initialized.
	 */
	
	String initialHistory;
	
	/*
	 * When the gallery must be initialized with a server call to apply history, this is the history that will
	 * be applied in the initVizGalForHistory callback when the call returns.
	 */
     Map<String, String> historyTokens;
     
     // Sometimes  you need to keep the current map selection values.
     double[] cs = null;
	
     // Keep track of the current operations
     OperationSerializable[] ops;
     
     // Everybody that sub-classes BaseUI must implement three handlers for options OK, operations clicks and data set selections.
   
	public void onModuleLoad() {
		super.onModuleLoad();
		
		
		addMenuButtons(buttonLayout);
		vVizGalPanel.setWidget(1, 0, xMainPanel);
		
		initialHistory = getAnchor();
		
		
		// Button to turn on and off difference mode.
		differenceButton = new ToggleButton("Difference Mode");
		differenceButton.ensureDebugId("differenceButton");
		differenceButton.setTitle("Toggle Difference Mode");
		differenceButton.addClickListener(differencesClick);
		
		buttonLayout.setWidget(0, 0, differenceButton);
		
		
		xAxesWidget.addApplyHandler(settingsButtonApplyHandler);
		// Comparison Axes Selector
		
		xComparisonAxesSelector.addAxesChangeHandler(compareAxisChangeHandler);
		
		// Sets the contour levels for all plots based on the global min/max of the data (as returned in the map scale file).
		autoContourButton = new ToggleButton("Auto Colors");
		autoContourButton.ensureDebugId("autoContourButton");
		autoContourButton.setTitle("Set consistent contour levels for all panels.");
		autoContourButton.addClickListener(autoContour);
		
		buttonLayout.setWidget(0, 1, autoContourButton);
		autoContourTextBox = new TextBox();
		autoContourTextBox.ensureDebugId("autoContourTextBox");
		buttonLayout.setWidget(0, 2, autoContourTextBox);

		
		RootPanel.get("vizGal").add(vVizGalPanel);
		//RootPanel.get("PLOT_LINK").setVisible(false);
		
		super.init(4, Constants.IMAGE);
		
		// Set the three required handlers
		setDatasetSelectionHandler(xVisGalDatasetSelectionHandler);
		setOperationsClickHandler(xVizGalOperationsClickHandler);
		setOptionsOkHandler(optionsOkHandler);
		addPanelApplyClickHandler(panelApplyButtonClick);
		addPanelRevertClickHandler(panelApplyButtonClick);
		// Initialize the gallery with an asynchronous call to the server to get variable needed.
		if ( xDSID != null && xVarID != null & xOperationID != null && xView != null) {
			// If the proper information was sent to the widget, pull down the variable definition
			// and initialize the slide sorter with this Ajax call.
			Util.getRPCService().getVariable(xDSID, xVarID, requestGrid);
		}
		History.addValueChangeHandler(historyHandler);
	}
	
	ValueChangeHandler<String> historyHandler = new ValueChangeHandler<String>() {

		@Override
		public void onValueChange(ValueChangeEvent<String> event) {
			
			String tokens = event.getValue();
			popHistory(tokens);
			
		}
		
	};

	
	private String getAnchor() {
		String url = Window.Location.getHref();
		if ( url.contains("#") ) {
			return url.substring(url.indexOf("#")+1, url.length());
		} else {
			return "";
		}
		
	}
	SelectionHandler<TreeItem> xVisGalDatasetSelectionHandler = new SelectionHandler<TreeItem>() {

		@Override
		public void onSelection(SelectionEvent<TreeItem> event) {
			TreeItem item = event.getSelectedItem();
			Object v = item.getUserObject();
			if ( v instanceof VariableSerializable ) {
				xNewVariable = (VariableSerializable) v;
				changeDataset = true;
				changeDataset();
			}
		}

	};
	
	AsyncCallback requestGrid = new AsyncCallback() {
		public void onSuccess(Object result) {
			xVariable = (VariableSerializable) result;
			initial_var = xVariable;
			// Null view to get all operations.
			Util.getRPCService().getConfig(null, xDSID, xVarID, initVizGal);
		}


		@Override
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes VizGal."+caught.toString());
		}
	};

	AsyncCallback<ConfigSerializable> getGridForChangeDatasetCallback = new AsyncCallback<ConfigSerializable>() {
		public void onSuccess(ConfigSerializable config) {

			GridSerializable grid = config.getGrid();
			ops = config.getOperations();
			xOperationsWidget.setOperations(xVariable.getIntervals(), xOperationID, xView, ops);
			xOptionsButton.setOptions(xOperationsWidget.getCurrentOperation().getOptionsID());
			xVariable.setGrid(grid);
			// Figure out the compare and fixed axis
			init();
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				panel.setPanelColor("regularBackground");
				panel.setVariable(xVariable);
				panel.init(false, ops);
				panel.setOperation(xOperationID, xView);
				
				
				if ( fixedAxis.equals("t") ) {
					panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
				} else if ( fixedAxis.equals("z") ) {
					panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
				}
			}
			
			// Now that we have the grid, finish applying the changes.
			finishApply();
			
		}

		@Override
		public void onFailure(Throwable caught) {
			Window.alert("Could not get grid for new variable."+caught.toString());
			
		}
	};
	AsyncCallback requestGridForHistory = new AsyncCallback() {
		public void onSuccess(Object result) {
			CategorySerializable[] cats = (CategorySerializable[]) result;
			if ( cats.length > 1 ) {
				Window.alert("Error getting variables for this dataset.");
			} else {
				if ( cats[0].isVariableChildren() ) {
					xVariable = cats[0].getVariable(xVarID);
					initial_var = xVariable;
					Util.getRPCService().getGrid(xDSID, xVarID, initVizGalForHistory);
				} else {
					Window.alert("No variables found in this category");
				}
			}
		}

		@Override
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes VizGal."+caught.toString());
		}
	};
	AsyncCallback<ConfigSerializable> initVizGal = new AsyncCallback<ConfigSerializable>() {
		public void onSuccess(ConfigSerializable config) {
			
			GridSerializable grid = config.getGrid();
			ops = config.getOperations();
			
			xVariable.setGrid(grid);
			if (xVariable.isVector() ) {
				autoContourTextBox.setText("");
				autoContourButton.setDown(false);
				autoContourButton.setEnabled(false);
				if ( !xView.equals("xy") ) {
					differenceButton.setDown(false);
					differenceButton.setEnabled(false);
				} else {
					differenceButton.setDown(false);
					differenceButton.setEnabled(true);
				}
			}
			initPanels();
		}
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes VizGal."+caught.toString());
		}
	};
	AsyncCallback initVizGalForHistory = new AsyncCallback() {
		public void onSuccess(Object result) {
			GridSerializable grid = (GridSerializable) result;
			xVariable.setGrid(grid);
			initPanels();
			applyHistory(historyTokens);
		}
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes VizGal."+caught.toString());
		}
	};
	
	
	private void initPanels() {

		xOrtho.clear();
		
		
		
		for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
			OutputPanel panel = (OutputPanel) panelIt.next();
			panel.setVariable(xVariable);
			panel.init(false, ops);
		}
		
		init();
		
		if ( xTlo != null && !xTlo.equals("") ) {
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				if ( xThi != null && !xThi.equals("") ) {
					panel.setAxisRangeValues("t", xTlo, xThi);
				} else {
					panel.setAxisRangeValues("t", xTlo, xTlo);
				}
			}			
			xAxesWidget.getTAxis().setLo(xTlo);
			if ( xThi != null && !xThi.equals("") ) {
				xAxesWidget.getTAxis().setHi(xThi);
			} else {
				xAxesWidget.getTAxis().setHi(xTlo);
			}
		}
		if ( xZlo != null && !xZlo.equals("") ) {
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				if ( xZhi != null && !xZhi.equals("") ) {
					panel.setAxisRangeValues("z", xZlo, xZhi);
				} else {
					panel.setAxisRangeValues("z", xZlo, xZlo);
				}
			}
			xAxesWidget.getZAxis().setLo(xZlo);
			if ( xZhi != null && !xZhi.equals("") ) {
			xAxesWidget.getZAxis().setHi(xZhi);
			} else {
				xAxesWidget.getZAxis().setHi(xZlo);
			}
		}


		// If these limits are not the same as the dataBounds, then set them.
		if ( xXlo != null && !xXlo.equals("") && xXhi != null && !xXhi.equals("") && 
			 xYlo != null && !xYlo.equals("") && xYhi != null && !xYhi.equals("") ) {
			xAxesWidget.getRefMap().setCurrentSelection(Double.valueOf(xYlo), Double.valueOf(xYhi), Double.valueOf(xXlo), Double.valueOf(xXhi));
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				panel.setMapTool(xView);
				panel.setLatLon(xYlo, xYhi, xXlo, xXhi);
			}
		} else {
			double tmp_xXlo = xAxesWidget.getRefMap().getXlo();
			double tmp_xhi = xAxesWidget.getRefMap().getXhi();
			double tmp_ylo = xAxesWidget.getRefMap().getYlo();
			double tmp_yhi = xAxesWidget.getRefMap().getYhi();
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				panel.setMapTool(xView);
				panel.setLatLon(String.valueOf(tmp_ylo), String.valueOf(tmp_yhi), String.valueOf(tmp_xXlo), String.valueOf(tmp_xhi));
			}
		}
		
		// Apply the initial history here...  Then refresh.  :-)
		if (initialHistory != null && !initialHistory.equals("") ) {
			String[] settings = initialHistory.split("token");
			HashMap<String, String> tokenMap = Util.getTokenMap(settings[0]);
			applyHistory(tokenMap);


			for (int t = 0; t < xPanels.size(); t++) {
				HashMap<String, String> panelTokenMap = Util.getTokenMap(settings[t+1]);
				HashMap<String, String> optionsMap = Util.getOptionsMap(settings[t+1]);
				if ( t == 0 ) {
					
					xOperationsWidget.setOperation(tokenMap.get("operation_id"), panelTokenMap.get("xView"));
					xAxesWidget.getRefMap().setCurrentSelection(Double.valueOf(panelTokenMap.get("ylo")),
							                                   Double.valueOf(panelTokenMap.get("yhi")), 
							                                   Double.valueOf(panelTokenMap.get("xlo")),
							                                   Double.valueOf(panelTokenMap.get("xhi")));
					if ( optionsMap.size() >= 1 ) {
						xOptionsButton.setState(optionsMap);
					}
					
				}
				xPanels.get(t).setFromHistoryToken(panelTokenMap, optionsMap);
			}

		}
		boolean diff = !xView.contains(compareAxis);	
		if ( !diff ) {
			differenceButton.setDown(false);
		}
		differenceButton.setEnabled(diff);
		refresh(false, false);
	}
	public boolean init() {

		xOperationsWidget.setOperations(xVariable.getIntervals(), xOperationID, xView, ops);
		xOptionsButton.setOptions(xOperationsWidget.getCurrentOperation().getOptionsID());
		GridSerializable ds_grid = xVariable.getGrid();
		double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
		double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

		double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
		double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

		double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));
		xAxesWidget.getRefMap().setTool(xView);
		xAxesWidget.getRefMap().setDataExtent(grid_south, grid_north, grid_west, grid_east, delta);
		
		xOrtho = Util.setOrthoAxes(xView, xVariable.getGrid());
		
		if ( xOrtho.size() == 0 ) {
			Window.alert("There are no axes orthogonal to the view on which the data can be compared.");
			return false;
		} else {

			int pos = 0;
			
			// Figure out which axis vary in each frame.  Take them in order of t, z, y, x...

			xComparisonAxesSelector.setAxes(xOrtho);
			compareAxis = xComparisonAxesSelector.getValue();
			if ( compareAxis.equals("t")) {
				if ( xOrtho.contains("z") ) {
					fixedAxis = "z";
				} else if ( xOrtho.contains("y") ) {
					fixedAxis = "y";
				} else if ( xOrtho.contains("x") ) {
					fixedAxis = "x";
				} else {
					fixedAxis = "";
				}
			} else if ( compareAxis.equals("z") ) {
				if ( xOrtho.contains("y") ) {
					fixedAxis = "y";
				} else if ( xOrtho.contains("x") ) {
					fixedAxis = "x";
				} else {
					fixedAxis = "";
				}
			} else if ( compareAxis.equals("xy") ) {
				if (xOrtho.contains("t") ) {
					fixedAxis = "t";
				} else if ( xOrtho.contains("z") ) {
					fixedAxis = "z";
				} else {
					fixedAxis = "";
				}
			}
			xAxesWidget.init(xVariable.getGrid());
			xAxesWidget.setFixedAxis(xView, xOrtho, compareAxis);
			
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				panel.setCompareAxis(xView, xOrtho, compareAxis);
			}
			return true;
		}
	}
	public ChangeHandler compareAxisChangeHandler = new ChangeHandler() {

		@Override
		public void onChange(ChangeEvent event) {
			ListBox axes = (ListBox) event.getSource();
			fixedAxis = compareAxis;
			compareAxis = axes.getValue(axes.getSelectedIndex());
			boolean fixed_axis_range = false;
			String fixedAxisLoValue = "";
			String fixedAxisHiValue = "";
			if ( compareAxis.equals("t") ) {
				xAxesWidget.setRange("t", false);
				if ( xVariable.getGrid().hasZ() ) {
					fixedAxisLoValue = xAxesWidget.getZAxis().getLo();
					fixedAxisHiValue = xAxesWidget.getZAxis().getHi();
					fixed_axis_range = xAxesWidget.getZAxis().isRange();
				}
			}  else {
				xAxesWidget.setRange("z", false);
				if ( xVariable.getGrid().hasT() ) {
					fixedAxisLoValue = xAxesWidget.getTAxis().getFerretDateLo();
					fixedAxisHiValue = xAxesWidget.getTAxis().getFerretDateHi();
					fixed_axis_range = xAxesWidget.getTAxis().isRange();
				}
			}
			xAxesWidget.setFixedAxis(xView, xOrtho, compareAxis);
			// Set the value of the fixed axis in all the panels under slide sorter control.
			xOrtho = Util.setOrthoAxes(xView, xVariable.getGrid());
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				if ( !panel.isUsePanelSettings() ) {
					panel.setCompareAxis(xView, xOrtho, compareAxis);
					panel.setAxisRangeValues(fixedAxis, fixedAxisLoValue, fixedAxisHiValue);
					
					// A time or zWidget is already initialized so the setCompareAxis displays
					// these widgets and they are ready to go.  A map on the other hand has to
					// be initialized after it gets placed in the display.
					double[] data = xAxesWidget.getRefMap().getDataExtent();
					double[] selection = xAxesWidget.getRefMap().getCurrentSelection();
					// Set the map tool.
					panel.setMapTool(xView);
					// Set the data extents.
					panel.setDataExtent(data);
					// Set the current selection.
					panel.setLatLon(selection);
					
				}
			}
			refresh(true, true);

			boolean diff = !xView.contains(compareAxis);	
			if ( !diff ) {
				differenceButton.setDown(false);
			}
			differenceButton.setEnabled(diff);
		}			
	};
	private void refresh(boolean switchAxis, boolean history) {

		if (autoContourTextBox.getText().equals(Constants.NO_MIN_MAX) ) {
			autoContourTextBox.setText("");
		}
		if ( differenceButton.isDown() ) {
			if ( autoContourButton.isDown() ) {
				autoContourButton.setDown(false);
				autoContourTextBox.setText("");
			}
			OutputPanel comparePanel = null;
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				if ( panel.isComparePanel() ) {
					comparePanel = panel;
					setPanelAxes(comparePanel, panel);
					panel.refreshPlot(xOptionsButton.getState(), switchAxis, true);	
				}
			}
			if ( comparePanel != null ) {
				for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
					OutputPanel panel = (OutputPanel) panelIt.next();
					panel.setVizGalState(comparePanel.getVariable(), comparePanel.getHistoryToken());
					if ( !panel.getID().equals(comparePanel.getID()) ) {
						String xXlo = "";
						String xhi = "";
						String ylo = "";
						String yhi = "";
						String zlo = "";
						String zhi = "";
						String tlo = "";
						String thi = "";
						// The values for the first variable come from either the Gallery or
						// the comparison panel.
						if ( compareAxis.contains("x") ) {
							xXlo = String.valueOf(comparePanel.getXlo());
							xhi = String.valueOf(comparePanel.getXhi());
						} else {
							xXlo = String.valueOf(xAxesWidget.getRefMap().getXlo());
							xhi = String.valueOf(xAxesWidget.getRefMap().getXhi());
						}
						if ( compareAxis.contains("y") ) {
							ylo = String.valueOf(comparePanel.getYlo());
							yhi = String.valueOf(comparePanel.getYhi());
						} else {
							ylo = String.valueOf(xAxesWidget.getRefMap().getYlo());
							yhi = String.valueOf(xAxesWidget.getRefMap().getYhi());
						}
						
						if ( xVariable.getGrid().hasT() ) {
							if ( compareAxis.equals("t") ) {
							    tlo = comparePanel.getTlo();
							    thi = comparePanel.getThi();
							} else {
								tlo = xAxesWidget.getTAxis().getFerretDateLo();
								thi = xAxesWidget.getTAxis().getFerretDateHi();
							}
						} else {
							tlo = null;
							thi = null;
						}
						if ( xVariable.getGrid().hasZ() ) {
							if ( compareAxis.equals("z") ) {
								zlo = comparePanel.getZlo();
								zhi = comparePanel.getZhi();
							} else {
								zlo = xAxesWidget.getZAxis().getLo();
								zhi = xAxesWidget.getZAxis().getHi();
							}
						} else {
							zlo = null;
							zhi = null;
						}
						panel.computeDifference(xOptionsButton.getState(), switchAxis, comparePanel.getVariable(), xOperationsWidget.getCurrentView(), xXlo, xhi, ylo, yhi, zlo, zhi, tlo, thi);
					}
				}
			}
		} else {
			OutputPanel comparePanel = null;
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				if ( panel.isComparePanel() ) {
					comparePanel = panel;					
				}
			}
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				panel.setVizGalState(comparePanel.getVariable(), comparePanel.getHistoryToken());
				// See if the panel settings are being used on this panel, if not
				// reset the fixed axis and the view axis and the operation to the slide sorter value.
				if ( !panel.isUsePanelSettings() ) {
					VariableSerializable v = panel.getVariable();
					if ( !v.getID().equals(xVariable.getID()) || !v.getDSID().equals(xVariable.getDSID() ) ) {
						panel.setVariable(xVariable);
						panel.init(false, ops);
					} 
					setPanelAxes(comparePanel, panel);
					panel.setOperation(xOperationID, xView);
				}
				if ( !panel.getCurrentOperationView().equals(xOperationsWidget.getCurrentView()) ) {
					differenceButton.setDown(false);
					differenceButton.setEnabled(false);
				} else {
					if ( xVariable.isVector() ) {
						if ( !xView.equals("xy") ) {
							differenceButton.setDown(false);
							differenceButton.setEnabled(false);
						} else {
						    differenceButton.setDown(false);
						    differenceButton.setEnabled(true);
						}
					} else {
				  	    differenceButton.setDown(false);
					    differenceButton.setEnabled(true);
					}
				}


				// Get the current state of the options...
				Map<String, String> ts = xOptionsButton.getState();
				if ( autoContourButton.isDown() ) {
					// If the auto button is down, it wins...
					autoScale();
				} else {
					// If it's not down, the current options value will be used.
					autoContourTextBox.setText("");
				}
				
				panel.setFillLevels(autoContourTextBox.getText());
				panel.refreshPlot(ts, switchAxis, true);
			}
		}
		if ( history ) {
			pushHistory();
		}
	}
	private void setPanelAxes(OutputPanel comparePanel, OutputPanel panel) {
		double plot_xXlo = -9999.;
		double plot_xhi = -9999.;
		double plot_yhi = -9999.;
		double plot_ylo = -9999.;
		plot_xXlo = comparePanel.getXlo();
		plot_xhi = comparePanel.getXhi();
		plot_ylo = comparePanel.getYlo();
		plot_yhi = comparePanel.getYhi();

		if ( xView.equals("xy") && compareAxis.equals("z") ) {
			// set x, y, and t in the panel
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("xy") && compareAxis.equals("t") ) {
			// set x, y and z in the panel
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("x") && compareAxis.equals("t") ) {
			// The map and z are in the fixed axis panel,  take the map value for both x and y and set z. 
			// ( OR from above )
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("x") && compareAxis.equals("z") ) {
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("x") && compareAxis.equals("y") ) {
			// The map is in the panels.  z and t are in the fixed axes panel.
			// Take z and t from the global controls and x from the first panel and put back the y from each panel
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(panel.getYlo()), 
					String.valueOf(panel.getYhi()), 
					String.valueOf(plot_xXlo), 
					String.valueOf(plot_xhi));
		} else if ( xView.equals("y") && compareAxis.equals("t") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("y") && compareAxis.equals("z") ) {
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("y") && compareAxis.equals("x") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(plot_ylo), 
					String.valueOf(plot_yhi), 
					String.valueOf(panel.getXlo()), 
					String.valueOf(panel.getXhi()));
		} else if ( xView.equals("z") && compareAxis.equals("t") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("z") && compareAxis.equals("xy") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
		} else if ( xView.equals("t") && compareAxis.equals("z") ) {
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("t") && compareAxis.equals("xy") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
		} else if ( xView.equals("xz") && compareAxis.equals("y") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(panel.getYlo()), 
					String.valueOf(panel.getYhi()), 
					String.valueOf(plot_xXlo), 
					String.valueOf(plot_xhi));
		} else if ( xView.equals("xz") && compareAxis.equals("t") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("yz") && compareAxis.equals("x") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(plot_ylo), 
					String.valueOf(plot_yhi), 
					String.valueOf(panel.getXlo()), 
					String.valueOf(panel.getXhi()));
		} else if ( xView.equals("yz") && compareAxis.equals("t") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("xt") && compareAxis.equals("z") ) {
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("xt") && compareAxis.equals("y") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(panel.getYlo()), 
					String.valueOf(panel.getYhi()), 
					String.valueOf(plot_xXlo), 
					String.valueOf(plot_xhi));
		} else if ( xView.equals("yt") && compareAxis.equals("x") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(plot_ylo), 
					String.valueOf(plot_yhi), 
					String.valueOf(panel.getXlo()), 
					String.valueOf(panel.getXhi()));
		} else if ( xView.equals("yt") && compareAxis.equals("z") ) {
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
			panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), 
					String.valueOf(xAxesWidget.getRefMap().getYhi()), 
					String.valueOf(xAxesWidget.getRefMap().getXlo()), 
					String.valueOf(xAxesWidget.getRefMap().getXhi()));
		} else if ( xView.equals("zt") ) {
			if ( xVariable.getGrid().hasZ() ) {
				panel.setAxisRangeValues("z", xAxesWidget.getZAxis().getLo(), xAxesWidget.getZAxis().getHi());
			}
			if ( xVariable.getGrid().hasT() ) {
				panel.setAxisRangeValues("t", xAxesWidget.getTAxis().getFerretDateLo(), xAxesWidget.getTAxis().getFerretDateHi());
			}
		}
	}
	/**
	 * A little helper method to change data sets.
	 */
	public void changeDataset() {
		xVariable = xNewVariable;
		if ( xVariable.isVector() ) {
			autoContourTextBox.setText("");
			autoContourButton.setDown(false);
			autoContourButton.setEnabled(false);
			if ( !xView.equals("xy") ) {
				differenceButton.setDown(false);
				differenceButton.setEnabled(false);
			} else {
				differenceButton.setDown(false);
				differenceButton.setEnabled(true);
			}
		} else {
			autoContourButton.setEnabled(true);
			differenceButton.setDown(false);
			differenceButton.setEnabled(true);
		}
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
		Util.getRPCService().getConfig(null, xVariable.getDSID(), xVariable.getID(), getGridForChangeDatasetCallback);
		
	}

	/**
	 * A helper methods that moves the current state of the map widget to the panels.
	 */
	private void setMapRanges(double[] cs) {
		for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
			OutputPanel panel = (OutputPanel) panelIt.next();
			
			if (!panel.isUsePanelSettings()) {
				panel.setMapTool(xView);
				if ( cs == null ) {
				    panel.setLatLon(String.valueOf(xAxesWidget.getRefMap().getYlo()), String.valueOf(xAxesWidget.getRefMap().getYhi()), String.valueOf(xAxesWidget.getRefMap().getXlo()), String.valueOf(xAxesWidget.getRefMap().getXhi()));
				} else {
					//cs contains s, n, w, e
					panel.setLatLon(String.valueOf(cs[0]), String.valueOf(cs[1]), String.valueOf(cs[2]), String.valueOf(cs[3]));
				}
			}
		}
	}
	public void applyChange() {
		if ( changeDataset ) {
			cs = xAxesWidget.getRefMap().getCurrentSelection();
			// This involves a jump across the wire, so the finishApply gets called in the callback from the getGrid.
			changeDataset();
		} else {
			// No jump required, just finish up now.
			finishApply();
		}
	}
	ClickHandler settingsButtonApplyHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {
			applyChange();
		}
	};
	public void finishApply() {
		// Check to see if the operation changed.  If so, change the tool.
		String op_id = xOperationsWidget.getCurrentOperation().getID();
		String op_view = xOperationsWidget.getCurrentView();
		if ( !op_id.equals(xOperationID) && !op_view.equals(xView) ) {
			xOperationID = op_id;
			xView = op_view;
		}
		// The view may have changed if the operation changed before the apply.
		xAxesWidget.getRefMap().setTool(xView);
		
		refresh(false, true);
	}
	ClickHandler panelApplyButtonClick = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			refresh(false, true);
		}
	};
	ClickListener differencesClick = new ClickListener() {
		public void onClick(Widget sender) {
			refresh(false, true);	
		}
	};
	public ChangeListener panelAxisMenuChange = new ChangeListener() {
		public void onChange(Widget sender) {
			refresh(false, true);
		}
	};
	public ChangeListener fixedAxisMenuChange = new ChangeListener() {
		public void onChange(Widget sender) {
			String lo_value = null;
			String hi_value = null;
			boolean range = false;
			if ( fixedAxis.equals("t") ) {
				lo_value = xAxesWidget.getTAxis().getFerretDateLo();
				hi_value = xAxesWidget.getTAxis().getFerretDateHi();
				range = xAxesWidget.getTAxis().isRange();
			} else if ( fixedAxis.equals("z") ) {
				lo_value = xAxesWidget.getZAxis().getLo();
				hi_value = xAxesWidget.getZAxis().getHi();
				range = xAxesWidget.getZAxis().isRange();
			}
			setParentAxis(fixedAxis, lo_value, hi_value, range, false);
			refresh(false, true);
		}   	
	};
	/**
	 * Helper method to set the values of the axes in the panels that correspond to the fixed axis in the gallery.
	 * @param axis - the axis to set, either z or t.
	 * @param lo - the lo value to set
	 * @param hi - the hi value to set (will be equal to lo if not a range, it doesn't get used if not a range)
	 * @param range - whether the widget is showing a range of values
	 * @param set_local - whether to set the value of the gallery settings widget (in response to a history event)
	 */
	private void setParentAxis(String axis, String lo, String hi, boolean range, boolean set_local) {
		if ( set_local ) {
			if ( axis.equals("t") ) {
				if ( range ) {
					xAxesWidget.getTAxis().setLo(lo);
					xAxesWidget.getTAxis().setHi(hi);
				} else {
					xAxesWidget.getTAxis().setLo(lo);
				}	
			} else if ( axis.equals("z") ) {
				if ( range ) {
					xAxesWidget.getZAxis().setLo(lo);
					xAxesWidget.getZAxis().setHi(hi);
				} else {
					xAxesWidget.getZAxis().setLo(lo);
				}
			}
		}
		for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
			OutputPanel panel = (OutputPanel) panelIt.next();
			if ( !panel.isUsePanelSettings() ) {
				panel.setAxisRangeValues(axis, lo, hi);
			}
		}
	}
	ClickHandler optionsOkHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent arg0) {
			refresh(false, true);
		}
		
	};
	ClickListener autoContour = new ClickListener() {
		public void onClick(Widget sender) {
			refresh(false, true);
		}	
	};
	private void autoScale() {

		// Use the values from the "compare panel" to set the auto contour levels.
		OutputPanel panel = xPanels.get(0);

		if ( panel.getMin() < globalMin ) {
			globalMin = panel.getMin();
		}
		if ( panel.getMax() > globalMax ) {
			globalMax = panel.getMax();
		}


		// Algorithm from range.F subroutine in Ferret source code

		double umin = globalMin;
		double umax = globalMax;
		int nints = 20;

		double temp = (umax - umin) / nints;
		if (temp <= 0.0000000001) {
			temp = umax;
		}

		double nt = Math.floor(Math.log(temp) / Math.log(10.));
		if (temp < 1.0) {
			nt = nt - 1;
		}
		double pow = Math.pow(10,nt);
		temp = temp / pow;

		double dint = 10.0 * pow;
		if (temp < Math.sqrt(2.0)) {
			dint = pow;
		} else {
			if (temp < Math.sqrt(10.0)) {
				dint = 2.0 * pow;
			} else {
				if (temp < Math.sqrt(50.0)) {
					dint = 5.0 * pow;
				}
			}
		}

		double fm = umin / dint;
		double m = Math.floor(fm);
		if (m < 0) {
			m = m - 1;
		}
		double uminr = Math.round(1000000 * dint * m) / 1000000;

		fm = umax / dint;
		m = Math.floor(fm);
		if (m > 0) {
			m = m + 1;
		}
		double umaxr = Math.round(1000000 * dint * m) / 1000000;

		// END OF FERRET ALGORITHM

		// Only use 4 significant digits

		// Modify the optionTextField and submit the request
		String fill_levels = "(-inf)(" + uminr + "," + umaxr + "," + dint + ")(inf)";

		// These are pretty close to zero.  I think the min/max did not come back from the server, so stop
		if ( (uminr + .00001 < .0001 && umaxr + .00001 < .0001) || globalMax < -9999999. && globalMin > 9999999. ) {
			autoContourTextBox.setText(Constants.NO_MIN_MAX);
			autoContourButton.setDown(false);
		} else {
			autoContourTextBox.setText(fill_levels);
		}
	}
	public ClickHandler xVizGalOperationsClickHandler = new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			Widget sender = (Widget) event.getSource();
			if ( sender instanceof OperationRadioButton ) {
				setupMenusForOperationChange();
			}
		}
	};

	private void setupMenusForOperationChange() {
		xView = xOperationsWidget.getCurrentView();
		xOperationID = xOperationsWidget.getCurrentOperation().getID();
		xOptionsButton.setOptions(xOperationsWidget.getCurrentOperation().getOptionsID());
		xOrtho = Util.setOrthoAxes(xView, xVariable.getGrid());
		
		if ( xVariable.isVector() ) {
			if ( !xView.equals("xy") ) {
				differenceButton.setDown(false);
				differenceButton.setEnabled(false);
			} else {
				differenceButton.setDown(false);
				differenceButton.setEnabled(true);
			}
		} else {
			differenceButton.setDown(false);
			differenceButton.setEnabled(true);
		}
		xComparisonAxesSelector.setAxes(xOrtho);
		
		if ( xView.length() !=  2 ) {
			autoContourTextBox.setText("");
			autoContourButton.setDown(false);
			autoContourButton.setEnabled(false);
		} else {
			if ( xVariable.isVector() ) {
				autoContourTextBox.setText("");
				autoContourButton.setDown(false);
				autoContourButton.setEnabled(false);
			} else {
			    autoContourButton.setEnabled(true);
			}
		}

		compareAxis = xComparisonAxesSelector.getValue();
		xAxesWidget.setFixedAxis(xView, xOrtho, compareAxis);
        // This will not be right for the new paradigm.
		// Set the orthogonal axes to a range in each panel.
		for (Iterator panelsIt = xPanels.iterator(); panelsIt.hasNext();) {
			OutputPanel panel = (OutputPanel) panelsIt.next();
			if ( !panel.isUsePanelSettings() ) {
				panel.setOperation(xOperationID, xView);

				panel.setCompareAxis(xView, xOrtho, compareAxis);
				if ( xView.contains("t") ) {
					panel.setPanelAxisRange("t", true);
				} else {
					panel.setPanelAxisRange("t", false);
				}
				if ( xView.contains("z") ) {
					panel.setPanelAxisRange("z", true);
				} else {
					panel.setPanelAxisRange("z", false);
				}
			}
		}
		applyChange();
	}	
	
	private void pushHistory() {
		// First token collection is the gallery settings (mostly in the header of the UI)
		StringBuilder historyToken = new StringBuilder();
		historyToken.append("panelHeaderHidden="+xPanelHeaderHidden);
		historyToken.append(";differences="+differenceButton.isDown());
		historyToken.append(";compareAxis="+compareAxis);
		if ( fixedAxis != null && !fixedAxis.equals("none") && !fixedAxis.equals("") ){
			historyToken.append(";fixedAxis="+fixedAxis);
		}
		
		historyToken.append(";autoContour="+autoContourButton.isDown());
		if ( xPanels.get(0).getMin() < 99999999. ) {
			historyToken.append(";globalMin="+xPanels.get(0).getMin());
		}
		if ( xPanels.get(0).getMax() > -99999999. ) {
			historyToken.append(";globalMax="+xPanels.get(0).getMax());
		}
		historyToken.append(";xDSID="+xVariable.getDSID());
		historyToken.append(";varid="+xVariable.getID());
		
		historyToken.append(";imageSize="+xImageSize.getValue(xImageSize.getSelectedIndex()));
		
		// Build the tokens for the panels.
		
		// The 0 panel is controlled by the SettingsControl in the gallery.
		
		// The next N tokens are the states of the individual panels.
		historyToken.append("token"+xPanels.get(0).getHistoryToken()+getHistoryToken());
		
		for (int i = 1; i < xPanels.size(); i++) {
			OutputPanel panel = xPanels.get(i);
			historyToken.append("token"+panel.getHistoryToken()+panel.getSettingsWidgetHistoryToken());
		}
       
		History.newItem(historyToken.toString(), false);
	}
	private String getHistoryToken() {
		StringBuilder token = new StringBuilder();
		token.append(";xlo="+xAxesWidget.getRefMap().getXlo());
		token.append(";xhi="+xAxesWidget.getRefMap().getXhi());
		token.append(";ylo="+xAxesWidget.getRefMap().getYlo());
		token.append(";yhi="+xAxesWidget.getRefMap().getYhi());
		if ( xVariable.getGrid().hasT() ) {
			token.append(";tlo="+xAxesWidget.getTAxis().getFerretDateLo());
			token.append(";thi="+xAxesWidget.getTAxis().getFerretDateHi());
		}
		if ( xVariable.getGrid().hasZ() ) {
			token.append(";zlo="+xAxesWidget.getZAxis().getLo());
			token.append(";zhi="+xAxesWidget.getZAxis().getHi());
		}
		if ( xOperationsWidget.getCurrentOperation() != null ) {
			token.append(";operation_id="+xOperationsWidget.getCurrentOperation().getID());
			token.append(";view="+xOperationsWidget.getCurrentView());
		}
		Map<String, String> options = xOptionsButton.getState();
		for (Iterator<String> opIt = options.keySet().iterator(); opIt.hasNext();) {
			String name = opIt.next();
			String value = options.get(name);
			if ( !value.equalsIgnoreCase("default") ) {
				token.append(";ferret_"+name+"="+value);
			}
		}		
		return token.toString();
	}
	public void setFromHistoryToken(Map<String, String> tokenMap, Map<String, String> optionsMap) {
		xOperationsWidget.setOperation(tokenMap.get("operation_id"), tokenMap.get("view"));
		//s, n, w, e
		xAxesWidget.getRefMap().setCurrentSelection(Double.valueOf(tokenMap.get("ylo")),
				                                   Double.valueOf(tokenMap.get("yhi")), 
				                                   Double.valueOf(tokenMap.get("xlo")), 
				                                   Double.valueOf(tokenMap.get("xhi")));
		if ( optionsMap.size() >= 1 ) {
			xOptionsButton.setState(optionsMap);
		}
	}
	private void popHistory(String historyToken) {
		if ( historyToken.equals("") ) {
			xVariable = initial_var;
			initPanels();
		} else {
			// First split out the panel history
			String[] settings = historyToken.split("token");

			// Set the panels
			/*
			 * 
			 * Things that have to be reconciled:
			 * 		was the panel in "panel mode"
			 * 			if yes:	reinitialize the panel to the ds and var in the history token then set its state
			 * 			if no:	is the ds and var the same as the current ds and var
			 * 					if yes: set the state
			 * 					if no:	reinitialize with ds and var, then set the state
			 */

			// Check to see if the the variable is the same as before and that no panels are in usePanel mode.
			// This variable will be true if the settings only apply to the gallery state and not the variable
			// or panels in usePanelMode
			boolean galleryOnly = true;

			// Panel 0 is the gallery panel...
			for (int t = 1; t < xPanels.size(); t++) {

				OutputPanel panel = xPanels.get(t);
				galleryOnly = galleryOnly && !panel.isUsePanelSettings();
			}



			// Process everything that applies to the gallery


			HashMap<String, String> tokenMap = Util.getTokenMap(settings[0]);
			/*
			 *  This history event uses the same data set and variable as the current state, so
			 *  we can just set apply the gallery settings.
			 */

			boolean switch_axis = false;
			if ( (tokenMap.containsKey("xDSID") && tokenMap.get("xDSID").equals(xVariable.getDSID())) && 
				 (tokenMap.containsKey("varid") && tokenMap.get("varid").equals(xVariable.getID())) )  {
				switch_axis = applyHistory(tokenMap);
				/*
				 * Apply the settings to the panels (the compare axis and some plot options).
				 * The first token string (index 0) is all the junk in the header so we start 
				 * counting the tokens with t+1.
				 * 
				 * The rest are the tokens for the individual panels.  We must apply the first 
				 * (index 0) to the gallery ControlSettings widget then the panel.
				 */
				
				for (int t = 0; t < xPanels.size(); t++) {
					HashMap<String, String> panelTokenMap = Util.getTokenMap(settings[t+1]);
					HashMap<String, String> optionsMap = Util.getOptionsMap(settings[t+1]);
					if ( t == 0 ) {
						setFromHistoryToken(panelTokenMap, optionsMap);
					}
					xPanels.get(t).setFromHistoryToken(panelTokenMap, optionsMap);
				}
			} else {
				// If the variable happens to be the initial variable use it.
				if ( (tokenMap.containsKey("xDSID") && tokenMap.get("xDSID").equals(initial_var.getDSID())) && 
					 (tokenMap.containsKey("varid") && tokenMap.get("varid").equals(initial_var.getID())) )  {
					initPanels();
					switch_axis = applyHistory(tokenMap);
				} else {
					// Otherwise, save the history tokens, call back to the server for the variable and then apply the history in the callback.
					historyTokens = tokenMap;
					Util.getRPCService().getGrid(tokenMap.get("xDSID"), tokenMap.get("vid"), requestGridForHistory);
				}
			}
            // TODO what about the options for the gallery............................??
			refresh(switch_axis, false);
			// If necessary restore panels in usePanelSettings mode...
			/*
			for (int t = 1; t < panels.size(); t++) {
				HashMap<String, String> tokenMap = Util.getTokenMap(settings[t]);
				HashMap<String, String> optionsMap = Util.getOptionsMap(settings[t]);
				// If this history token has a panel that is using it's own settings, update witht the token.
				if ( tokenMap.get("usePanelSettings").equals("true") ) { 
					panel.setFromHistoryToken(settings[t]);
				} else {
					// If not force the ds and var from the gallery to the panel, then set from the compareAxis hi and lo from the token
				}
				t++;
			}
			 */
		}
	}
	private boolean applyHistory(Map<String, String> tokenMap) {
		boolean switch_axis = false;
		if ( tokenMap.containsKey("panelHeaderHidden") ) {
			boolean new_panelHeaderHidden = Boolean.valueOf(tokenMap.get("panelHeaderHidden")).booleanValue();
			if ( new_panelHeaderHidden != xPanelHeaderHidden ) {
				// If the new state should be different, handle it.  Otherwise ignore it.
				//handlePanelShowHide();
			}
		}
		if ( tokenMap.containsKey("differences") ) {
			boolean new_difference = Boolean.valueOf(tokenMap.get("differences")).booleanValue();
			if ( new_difference != differenceButton.isDown() ) {
				differenceButton.setDown(new_difference);
			}
		} 
		if ( tokenMap.containsKey("fixedAxis") && tokenMap.containsKey("fixedAxisLo") && tokenMap.containsKey("fixedAxisHi") ) {
			String new_fixedAxis = tokenMap.get("fixedAxis");
			if ( !new_fixedAxis.equals(fixedAxis) ) {
				switch_axis = true;
				if ( tokenMap.containsKey("compareAxis") && tokenMap.get("compareAxis") != null ) {
				    xComparisonAxesSelector.setValue(tokenMap.get("compareAxis"));
				}
			}
			
			if ( new_fixedAxis.equals("t") ) {
				xAxesWidget.getTAxis().setLo(tokenMap.get("tlo"));
				xAxesWidget.getTAxis().setHi(tokenMap.get("thi"));
			} else if ( new_fixedAxis.equals("z") ) {
				xAxesWidget.getZAxis().setLo(tokenMap.get("zlo"));
				xAxesWidget.getZAxis().setHi(tokenMap.get("zhi"));
			}
			xAxesWidget.setFixedAxis(xView, xOrtho, compareAxis);
			// TODO set compare axis in panels?
		}
		if ( tokenMap.containsKey("autoContour") ) {
			boolean new_autoContour = Boolean.valueOf(tokenMap.get("autoContour")).booleanValue();
			if ( new_autoContour != autoContourButton.isDown() ) {
				autoContourButton.setDown(new_autoContour);
			}
		}
		if ( tokenMap.containsKey("globalMin") ) {
			globalMin = Double.valueOf(tokenMap.get("globalMin")).doubleValue();
		}
		if ( tokenMap.containsKey("globalMax") ) {
			globalMax = Double.valueOf(tokenMap.get("globalMax")).doubleValue();
		}

		int count = xImageSize.getItemCount();
		for (int i = 0; i < count; i++ ) {
			String item_value = xImageSize.getValue(i);
			if ( item_value.equals(tokenMap.get("imageSize")) ) {
				xImageSize.setSelectedIndex(i);
			}
		}
		
		return switch_axis;
	}

	

}
