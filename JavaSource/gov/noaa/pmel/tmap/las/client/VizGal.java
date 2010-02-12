package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.SettingsWidget;
import gov.noaa.pmel.tmap.las.client.serializable.AxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.TimeAxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Constants;
import gov.noaa.pmel.tmap.las.client.util.Util;
import gov.noaa.pmel.tmap.las.client.vizgal.VizGalPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

/**
 * A UI widget with one or more panels containing an LAS product with widgets to interact with the specifications of the products.
 * @author rhs
 *
 */
public class VizGal implements EntryPoint {

	/*
	 * This is a hack for right now, but we are going to define two default operations.
	 */
	String default_regular_op = "Plot_2D_XY";
	String default_scattered_op = "Insitu_extract_location_value_plot";

	/*
	 * These are the four pieces of information required
	 * to initialize a SlideSorterOld
	 */
	String dsid;
	String vid;
	String view;
	String op;
	String optionID;
	/*
	 * These are optional parameters that can be used to set the xyzt ranges for the initial plot in the panels.
	 */
	String xlo;
	String xhi;
	String ylo;
	String yhi;
	String zlo;
	String zhi;
	String tlo;
	String thi;

	/*
	 * Keep track of the axes orthogonal to the view.
	 */
	List<String> ortho;

	/*
	 * Keep track of which axis is in the plot panels.
	 */
	String compareAxis;

	/*
	 * Keep track of which axis is selected in the header.
	 */
	String fixedAxis;

	/*
	 * A header row with some widgets.
	 */
	Grid header;

	/*
	 * The slide sorter grid.
	 */
	Grid slides;

	/*
	 * The container for the slides and controls.
	 */
	Grid mainPanel;
	CellFormatter cellFormatter;

	/*
	 * Button to make slide sorter compute differences
	 */
	ToggleButton differenceButton;

	/*
	 * The panels in this slide sorter.
	 */
	List<VizGalPanel> panels = new ArrayList<VizGalPanel>();

	/*
	 * Padding on the right side of the browser frame...
	 */
	int rightPad = 35;
	/*
	 * Fixed size of the left control panel
	 */
	int controlsWidth = 260;

	/*
	 * A DateTimeWidget to globally control the time.
	 */
	DateTimeWidget dateWidget;

	/*
	 * RadioButton to select the dateWidget as the comparison axis.
	 */
	RadioButton dateButton;

	/*
	 * Panel to hold the date widget and the radio button.
	 */
	HorizontalPanel datePanel = new HorizontalPanel();

	/*
	 * An AxisWidget to globally control the z-axis.
	 */
	AxisWidget xyzWidget;

	/*
	 * A Radio Button to select the Z axis as the comparison axis.
	 */
	RadioButton xyzButton;

	/*
	 * Panel to hold the z widget and the radio button.
	 */
	HorizontalPanel xyzPanel = new HorizontalPanel();

	/*
	 * A settings panel for the entire Slide Sorter.
	 */
	SettingsWidget settingsControls;

	/*
	 * The currently selected variable.
	 */
	VariableSerializable var;
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

	/*
	 * Grid for the Global ZT selector.
	 */
	FlexTable ztGrid;

	/*
	 * Label for compare axis selector
	 */
	Label varyAxis;

	/*
	 * Keep track of the new variable and change state to be able apply data set changes only after apply button is pressed.
	 */
	VariableSerializable nvar;
	boolean changeDataset = false;
	

	/*
	 * Button for showing and hiding the panel headers.
	 */
	ToggleButton showHide;
	Image minus;
	Image plus;
	boolean panelHeaderHidden;

	/*
	 * Image size control.
	 */
	ListBox imageSize;
	Label imageSizeLabel = new Label("Image zoom: ");
	int pwidth = 0;
	
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
     
     
	public void onModuleLoad() {
		
		ortho = new ArrayList<String>(); 

		dsid = Util.getParameterString("dsid");
		vid = Util.getParameterString("vid");
		//TODO If the operation is null, get the default operation (the map or plot; left nav) for this view.
		op = Util.getParameterString("opid");
		optionID = Util.getParameterString("optionid");
		view = Util.getParameterString("view");

		// This may have come from a running LAS and it might want to set up the xyzt ranges for the plots in the panels.
		xlo = Util.getParameterString("xlo");
		xhi = Util.getParameterString("xhi");
		ylo = Util.getParameterString("ylo");
		yhi = Util.getParameterString("yhi");
		zlo = Util.getParameterString("zlo");
		zhi = Util.getParameterString("zhi");
		tlo = Util.getParameterString("tlo");
		thi = Util.getParameterString("thi");
		
		initialHistory = getAnchor();
		
		// Hard-coded 4 panel display.
		// TODO make the gallery size changeable somehow...
		slides = new Grid(2,2);

		// A strip across the top with some of the gallery controls...
		header = new Grid(1, 10);

		// Controls which axis is the compare axis and which is the fixed axis
		ztGrid = new FlexTable();
		varyAxis = new Label("Select Axis to Vary in Panels");
		varyAxis.addStyleName("las-align-center");
		ztGrid.setWidget(0, 0, varyAxis);
		ztGrid.getFlexCellFormatter().setColSpan(0, 0, 2);
		ztGrid.addStyleName("LSS_middle");
		header.setWidget(0, 2, ztGrid);

		// Control whether the headers are hidden.
		panelHeaderHidden = false;
		plus = new Image(GWT.getModuleBaseURL()+"../images/plus_on.png");
		minus = new Image(GWT.getModuleBaseURL()+"../images/minus_on.png");
		showHide = new ToggleButton(plus, minus, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Handle the change by passing in the current state.
				handlePanelShowHide();				
			}	
		});
		header.setWidget(0, 0, showHide);
		
		showHide.setTitle("Show/Hide Panel Headers");
        showHide.setStylePrimaryName("OL_MAP-ToggleButton");
		// Button to turn on and off difference mode.
		differenceButton = new ToggleButton("Difference Mode");
		differenceButton.setTitle("Toggle Difference Mode");
		differenceButton.addClickListener(differencesClick);
		header.setWidget(0, 1, differenceButton);

		// This is a control panel that will control the settings for all the panels and it appears vertically on the left side.
		// The last false is to disallow editing with the grab handles on the map.
		settingsControls = new SettingsWidget("Gallery Settings", "Slide Sorter", op, optionID, "panel");
		settingsControls.setTitle("Settings for all panels.");
		settingsControls.addDatasetTreeListener(datasetTreeListener);
		settingsControls.addOptionsOkClickListener(optionsOkListener);
		settingsControls.addOperationClickListener(operationsClickListener);
		//settingsControls.setToolType(view);

		// Sets the contour levels for all plots based on the global min/max of the data (as returned in the map scale file).
		autoContourButton = new ToggleButton("Auto Set Color Fill Levels for Gallery");
		autoContourButton.setTitle("Set consistent contour levels for all panels.");
		autoContourButton.addClickListener(autoContour);
		header.setWidget(0, 4, autoContourButton);
		autoContourTextBox = new TextBox();
		header.setWidget(0, 5, autoContourTextBox);

		/*
		 * Control widget that sets the sizing of the image in the panel.
		 * If set to auto the panel resizes with the browser up to the size of the actual image.
		 * If set to 90% to 30% the image is set to a fixed size as a percentage of the actual image
		 * If set to 100% the image is fixed at full size.
		 * 
		 * Any size transformation to get to less than 100% is done by the browser.
		 */
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

		imageSize.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				String value = imageSize.getValue(imageSize.getSelectedIndex());
				if ( !value.equals("auto") ) {
					for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
						VizGalPanel panel = (VizGalPanel) panelIt.next();
						panel.setImageSize(Integer.valueOf(value).intValue());
					}
				} else {
					if ( pwidth == 0 ) {
						int win = Window.getClientWidth();
						if ( panelHeaderHidden ) {
							pwidth = (win - rightPad)/2;
						} else {
							pwidth = (win-(rightPad+controlsWidth))/2;
						}
						if ( pwidth <= 0 ) {
							pwidth = 400;
						}
					}
					for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
						VizGalPanel panel = (VizGalPanel) panelIt.next();
						panel.setPanelWidth(pwidth);
					}
				}
			}		
		});
		header.setWidget(0, 6, imageSizeLabel);
		header.setWidget(0, 7, imageSize);

		// Initialize the gallery with an asynchronous call to the server to get variable needed.
		if ( dsid != null && vid != null & op != null && view != null) {
			// If the proper information was sent to the widget, pull down the variable definition
			// and initialize the slide sorter with this Ajax call.
			Util.getRPCService().getVariable(dsid, vid, requestGrid);
		}


		mainPanel = new Grid(1, 2);
		mainPanel.setWidget(0,0,settingsControls);
		mainPanel.setWidget(0,1, slides);
		cellFormatter = mainPanel.getCellFormatter();
		// Float the controls and the panels to the top of their respective cells
		cellFormatter.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
		cellFormatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
		cellFormatter.setWidth(0, 0 ,controlsWidth+"px");

		RootPanel.get("header").add(header);
		RootPanel.get("slides").add(mainPanel);
		
		Window.addWindowResizeListener(windowResizeListener);
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				String historyToken = event.getValue();
				popHistory(historyToken);
			}
		});
	}
	
	public void handlePanelShowHide() {
		if ( panelHeaderHidden ) {
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.show();
			}
			cellFormatter.setVisible(0, 0, true);
			pwidth = pwidth - controlsWidth/2;
			resize();
		} else {
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.hide();
			}
			cellFormatter.setVisible(0, 0, false);
			pwidth = pwidth + controlsWidth/2;
			resize();
		}
		panelHeaderHidden = !panelHeaderHidden;
		pushHistory();
	}
	
	private String getAnchor() {
		String url = Window.Location.getHref();
		if ( url.contains("#") ) {
			return url.substring(url.indexOf("#")+1, url.length());
		} else {
			return "";
		}
		
	}
	TreeListener datasetTreeListener = new TreeListener() {

		public void onTreeItemSelected(TreeItem item) {
			Object v = item.getUserObject();
			if ( v instanceof VariableSerializable ) {
				nvar = (VariableSerializable) v;
				changeDataset = true;
				/*
				if ( nvar.getAttributes().get("grid_type").equals("regular") ) {
					changeDataset = true;
				} else {
					nvar = var;
					Window.alert("visGal cannot work with scattered data at this time.");
				}
				 */
			}
		}

		public void onTreeItemStateChanged(TreeItem item) {
			// TODO Auto-generated method stub

		}

	};
	AsyncCallback requestGrid = new AsyncCallback() {
		public void onSuccess(Object result) {
			var = (VariableSerializable) result;
			initial_var = var;
			Util.getRPCService().getGrid(dsid, vid, initVisGal);
		}


		@Override
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes VizGal."+caught.toString());
		}
	};

	AsyncCallback getGridForChangeDatasetCallback = new AsyncCallback() {
		public void onSuccess(Object result) {

			GridSerializable grid = (GridSerializable) result;
			var.setGrid(grid);
			// Figure out the compare and fixed axis
			init();
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.setPanelColor("regularBackground");
				panel.setOperation(op, view);
				panel.setVariable(var);
				// Send in the ortho axis to allow these to be built and displayed in the panel

				panel.init(false);
				if ( fixedAxis.equals("t") ) {
					panel.setParentAxisValue("t", dateWidget.getFerretDateLo());
				} else if ( fixedAxis.equals("z") ) {
					panel.setParentAxisValue("z", xyzWidget.getLo());
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
					var = cats[0].getVariable(vid);
					initial_var = var;
					Util.getRPCService().getGrid(dsid, vid, initVizGalForHistory);
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
	AsyncCallback initVisGal = new AsyncCallback() {
		public void onSuccess(Object result) {
			GridSerializable grid = (GridSerializable) result;
			var.setGrid(grid);
			initPanels();
		}
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes VizGal."+caught.toString());
		}
	};
	AsyncCallback initVizGalForHistory = new AsyncCallback() {
		public void onSuccess(Object result) {
			GridSerializable grid = (GridSerializable) result;
			var.setGrid(grid);
			initPanels();
			applyHistory(historyTokens);
		}
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes VizGal."+caught.toString());
		}
	};
	public WindowResizeListener windowResizeListener = new WindowResizeListener() {
		public void onWindowResized(int width, int height) {
			if ( panelHeaderHidden ) {
				pwidth = (width - rightPad)/2;
			} else {
				pwidth = (width-(rightPad+controlsWidth))/2;
			}
			resize();
		}
	};
	public void resize() {
		if ( imageSize.getValue(imageSize.getSelectedIndex()).equals("auto") ) {
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.setPanelWidth(pwidth);
			}
		}
	}
	private void initPanels() {

		ortho.clear();
		datePanel.clear();
		xyzPanel.clear();
		panels.clear();

		settingsControls.addApplyClickListener(settingsButtonApplyListener);
		
		// FYI Column 3 of the header used to contain the gallery settings button.
		int win = Window.getClientWidth();
		if ( panelHeaderHidden ) {
			pwidth = (win - rightPad)/2;
		} else {
			pwidth = (win-(rightPad+controlsWidth))/2;
		}
		if ( pwidth <= 0 ) {
			pwidth = 400;
		}
		VizGalPanel sp1 = new VizGalPanel("Panel 0", true, op, optionID, view, false);
		sp1.addRevertListener(panelApplyButtonClick);
		sp1.addApplyListener(panelApplyButtonClick);
		slides.setWidget(0, 0, sp1);
		sp1.setPanelWidth(pwidth);
		sp1.addCompareAxisChangeListener(panelAxisMenuChange);
		sp1.addZChangeListner(panelAxisMenuChange);
		sp1.addTChangeListner(panelAxisMenuChange);
		panels.add(sp1);

		VizGalPanel sp2 = new VizGalPanel("Panel 1", false, op, optionID, view, false);
		sp2.addRevertListener(panelApplyButtonClick);
		sp2.addApplyListener(panelApplyButtonClick);
		//sp2.addRegionChangeListener(regionChange);
		slides.setWidget(0, 1, sp2);
		sp2.setPanelWidth(pwidth);
		sp2.addCompareAxisChangeListener(panelAxisMenuChange);
		sp2.addZChangeListner(panelAxisMenuChange);
		sp2.addTChangeListner(panelAxisMenuChange);		
		panels.add(sp2);

		VizGalPanel sp3 = new VizGalPanel("Panel 2", false, op, optionID, view, false);
		sp3.addRevertListener(panelApplyButtonClick);
		sp3.addApplyListener(panelApplyButtonClick);
		//sp2.addRegionChangeListener(regionChange);
		slides.setWidget(1, 0, sp3);
		sp3.setPanelWidth(pwidth);
		sp3.addCompareAxisChangeListener(panelAxisMenuChange);
		sp3.addZChangeListner(panelAxisMenuChange);
		sp3.addTChangeListner(panelAxisMenuChange);		
		panels.add(sp3);

		VizGalPanel sp4 = new VizGalPanel("Panel 3", false, op, optionID, view, false);
		sp4.addRevertListener(panelApplyButtonClick);
		sp4.addApplyListener(panelApplyButtonClick);
		//sp2.addRegionChangeListener(regionChange);
		slides.setWidget(1, 1, sp4);
		sp4.setPanelWidth(pwidth);
		sp4.addCompareAxisChangeListener(panelAxisMenuChange);
		sp4.addZChangeListner(panelAxisMenuChange);
		sp4.addTChangeListner(panelAxisMenuChange);		
		panels.add(sp4);

		sp1.setVariable(var);
		sp1.init(false);
		sp2.setVariable(var);
		sp2.init(false);
		sp3.setVariable(var);
		sp3.init(false);
		sp4.setVariable(var);
		sp4.init(false);

		init();

		if ( compareAxis.equals("t") && tlo != null && !tlo.equals("") ) {
			// If "t" is the compare axis, then set it to the passed in values in the panels
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				if (dateWidget.isRange() ) {
					if ( thi != null && !thi.equals("") ) {
						panel.setParentAxisRange("t", true);
						panel.setParentAxisRangeValues("t", tlo, thi);
					}
				} else {
					panel.setParentAxisValue("t", tlo);
				}
			}
			// and in the currently hidden fixed axis widget
			if ( dateWidget.isRange() ) {
				dateWidget.setLo(tlo);
				dateWidget.setHi(thi);
			} else {
				dateWidget.setLo(tlo);
			}


			// And if z exists, it will be the fixed axis so it also needs to be set.
			if ( fixedAxis.equals("z") ) {
				// Set the z axis in the gallery
				if ( xyzWidget.isRange() ) {
					if ( zlo != null && !zlo.equals("") && zhi != null && !zhi.equals("") ) {
						xyzWidget.setLo(zlo);
						xyzWidget.setHi(zhi);
						// Pass the settings down to the panels
						for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
							VizGalPanel panel = (VizGalPanel) panelIt.next();
							panel.setParentAxisRange("z", true);
							panel.setParentAxisRangeValues("z", zlo, zhi);
						}
					}
				} else {
					if ( zlo != null && !zlo.equals("") ) {
						xyzWidget.setLo(zlo);
						for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
							VizGalPanel panel = (VizGalPanel) panelIt.next();
							panel.setParentAxisValue("z", zlo);
						}
					}
				}

			}

		} else if ( compareAxis.equals("z") && zlo != null && !zlo.equals("") ) {
			// Same if z is the compare axis.
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				if ( xyzWidget.isRange() ) {
					panel.setParentAxisRangeValues("z", zlo, zhi);
				} else {
					panel.setParentAxisValue("z", zlo);
				}
			}
			if ( xyzWidget.isRange() ) {
				xyzWidget.setLo(zlo);
				xyzWidget.setHi(zhi);
			} else {
				xyzWidget.setLo(zlo);
			}

			if ( fixedAxis.equals("t") ) {
				if ( dateWidget.isRange() ) {
					if ( tlo != null && !tlo.equals("") && thi != null && !thi.equals("") ) {
						dateWidget.setLo(tlo);
						dateWidget.setHi(thi);
						for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
							VizGalPanel panel = (VizGalPanel) panelIt.next();
							panel.setParentAxisRangeValues("t", tlo, thi);
						}
					}
				} else {
					if ( tlo != null && !tlo.equals("") ) {
						dateWidget.setLo(tlo);	
					}
					for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
						VizGalPanel panel = (VizGalPanel) panelIt.next();
						panel.setParentAxisValue("t", tlo);
					}
				}
			}
		}
		// If these limits are not the same as the dataBounds, then set them.
		if ( xlo != null && !xlo.equals("") && xhi != null && !xhi.equals("") && 
			 ylo != null && !ylo.equals("") && yhi != null && !yhi.equals("") ) {
			settingsControls.setLatLon(xlo, xhi, ylo, yhi);
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.setLatLon(xlo, xhi, ylo, yhi);
			}
		}
		
		// Apply the initial history here...  Then refresh.  :-)
		if (initialHistory != null && !initialHistory.equals("") ) {
			String[] settings = initialHistory.split("token");
			HashMap<String, String> tokenMap = Util.getTokenMap(settings[0]);
			applyHistory(tokenMap);


			for (int t = 0; t < panels.size(); t++) {
				HashMap<String, String> panelTokenMap = Util.getTokenMap(settings[t+1]);
				HashMap<String, String> optionsMap = Util.getOptionsMap(settings[t+1]);
				if ( t == 0 ) {
					settingsControls.setFromHistoryToken(panelTokenMap, optionsMap);
				}
				panels.get(t).setFromHistoryToken(panelTokenMap, optionsMap);
			}

		}
		boolean diff = !view.contains(compareAxis);	
		if ( !diff ) {
			differenceButton.setDown(false);
		}
		differenceButton.setEnabled(diff);
		refresh(false, false);
	}
	public boolean init() {

		// This will load up the operations panels and select the radio button for the op id that is passed in...
		// Initially the default operation is passed in in the query string.  For subsequent initializations
		// the value gets set to the default operation for the data type.
		settingsControls.setOperations(var.getIntervals(), var.getDSID(), var.getID(), op, view, null);
		GridSerializable ds_grid = var.getGrid();
		double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
		double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

		double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
		double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

		double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));
		settingsControls.setToolType(view);
		settingsControls.getRefMap().setDataExtent(grid_south, grid_north, grid_west, grid_east, delta);
		
		ortho.clear();
		if ( datePanel != null ) {
			datePanel.clear();
			ztGrid.remove(datePanel);
		}
		if ( xyzPanel != null ) {
			xyzPanel.clear();
			ztGrid.remove(xyzPanel);
		}
		if ( var.getGrid().getZAxis() != null ) {
			ortho.add("z");
		}
		if ( var.getGrid().getTAxis() != null  ) {
			ortho.add("t");
		}
		if ( ortho.size() == 0 ) {
			Window.alert("There are no axes orthogonal to the view on which the data can be compared.");
			return false;
		} else {
			// Build a widget for each orthogonal axis.  There should be a max of 2.

			int pos = 0;
			// Figure out which axis vary in each frame.  Take them in order of t, z, x, y...
			if ( ortho.contains("t") ) {
				compareAxis = "t";
			}  else if ( ortho.contains("z") ) {
				compareAxis = "z";
			}
			for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
				String type = (String) orthoIt.next();
				if ( type.equals("t") ) {
					// TODO  For now assuming t is a comparison axis and that is is active.
					TimeAxisSerializable axis = (TimeAxisSerializable) var.getGrid().getAxis(type);

					dateWidget = new DateTimeWidget(axis, false);
					dateWidget.addChangeListener(fixedAxisMenuChange);
					if (view.contains("t")) dateWidget.setRange(true);
					if ( compareAxis.equals("t") ) {
						dateWidget.setEnabled(false);
					} else {
						dateWidget.setEnabled(true);
						fixedAxis = "t";
					}
					dateButton = new RadioButton("compare", " ");
					dateButton.addClickListener(compareAxisChangeListener);
					if ( compareAxis.equals("t") ) {
						dateButton.setChecked(true);
					}
					datePanel.add(dateButton);
					datePanel.add(dateWidget);
					ztGrid.setWidget(1, pos, datePanel);
				} else {
					AxisSerializable axis = var.getGrid().getAxis(type);

					xyzButton = new RadioButton("compare", " ");
					xyzButton.addClickListener(compareAxisChangeListener);
					xyzWidget = new AxisWidget(axis);
					xyzWidget.addChangeListener(fixedAxisMenuChange);
					if ( view.contains("z") ) xyzWidget.setRange(true);
					if ( compareAxis.equals(type) ) {
						xyzButton.setChecked(true);
						xyzWidget.setEnabled(false);
					} else {
						fixedAxis = type;
						xyzWidget.setEnabled(true);
					}
					xyzPanel.add(xyzButton);
					xyzPanel.add(xyzWidget);
					ztGrid.setWidget(1, pos, xyzPanel);
				}
				pos++;
			}
			if ( ortho.size() == 1 ) {
				fixedAxis = "none";
			}
			return true;
		}
	}
	public ClickListener compareAxisChangeListener = new ClickListener() {
		public void onClick(Widget sender) {
			if ( sender instanceof RadioButton) {
				String temp = compareAxis;
				compareAxis = fixedAxis;
				fixedAxis = temp;
				boolean fixed_axis_range = false;
				String fixedAxisLoValue = "";
				String fixedAxisHiValue = "";
				if ( compareAxis.equals("t") ) {
					dateWidget.setRange(false);
					dateWidget.setEnabled(false);
					xyzWidget.setEnabled(true);
					xyzWidget.setRange(view.contains("z"));
					fixedAxisLoValue = xyzWidget.getLo();
					fixedAxisHiValue = xyzWidget.getHi();
					fixed_axis_range = xyzWidget.isRange();
				}  else {
					dateWidget.setEnabled(true);
					dateWidget.setRange(view.contains("t"));
					xyzWidget.setRange(false);
					xyzWidget.setEnabled(false);
					fixedAxisLoValue = dateWidget.getFerretDateLo();
					fixedAxisHiValue = dateWidget.getFerretDateHi();
					fixed_axis_range = dateWidget.isRange();
				}
				// Set the value of the fixed axis in all the panels under slide sorter control.
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
					VizGalPanel panel = (VizGalPanel) panelIt.next();
					if ( !panel.isUsePanelSettings() ) {
						if ( fixed_axis_range ) {
							panel.setParentAxisRangeValues(fixedAxis, fixedAxisLoValue, fixedAxisHiValue);
						} else {
							panel.setParentAxisValue(fixedAxis, fixedAxisLoValue);
						}
					}
				}
				refresh(true, true);
			}		
			boolean diff = !view.contains(compareAxis);	
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
			VizGalPanel comparePanel = null;
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				if ( panel.getID().contains("Panel 0") ) {
					comparePanel = panel;
					panel.refreshPlot(settingsControls.getOptions(), switchAxis, true);	
				}
			}
			if ( comparePanel != null ) {
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
					VizGalPanel panel = (VizGalPanel) panelIt.next();
					if ( !panel.getID().equals(comparePanel.getID()) ) {
						String xlo = "";
						String xhi = "";
						String ylo = "";
						String yhi = "";
						String zlo = "";
						String zhi = "";
						String tlo = "";
						String thi = "";
						// Always get values from the map.  They may get replaced with the panel settings.
						xlo = String.valueOf(settingsControls.getRefMap().getXlo());
						xhi = String.valueOf(settingsControls.getRefMap().getXhi());

						ylo = String.valueOf(settingsControls.getRefMap().getYlo());
						yhi = String.valueOf(settingsControls.getRefMap().getYhi());

						if ( fixedAxis.equals("t") ) {
							tlo = dateWidget.getFerretDateLo();
							thi = dateWidget.getFerretDateHi();
						} else {
							if ( comparePanel.getVariable().getGrid().getAxis("t") != null ) {
								tlo = comparePanel.getTlo();
								thi = comparePanel.getThi();
							}
						}
						if ( fixedAxis.equals("z") ) {
							zlo = xyzWidget.getLo();
							zhi = xyzWidget.getHi();
						} else {
							if ( comparePanel.getVariable().getGrid().getAxis("z") != null ) {
								zlo = comparePanel.getZlo();
								zhi = comparePanel.getZhi();
							}
						}
						panel.computeDifference(settingsControls.getOptions(), switchAxis, comparePanel.getVariable(), settingsControls.getCurrentOperationView(), xlo, xhi, ylo, yhi, zlo, zhi, tlo, thi);
					}
				}
			}
		} else {
			// Get the current state of the options...
			Map<String, String> temp_state = new HashMap<String, String>(settingsControls.getOptions());
			if ( autoContourButton.isDown() ) {
				// If the auto button is down, it wins...
				autoScale();
			} else {
				// If it's not down, the current options value will be used.
				autoContourTextBox.setText("");
			}
			for (int panelIndexCounter = 0; panelIndexCounter < panels.size(); panelIndexCounter++) {
				VizGalPanel panel = (VizGalPanel) panels.get(panelIndexCounter);
				panel.setFillLevels(autoContourTextBox.getText());
				panel.refreshPlot(temp_state, switchAxis, true);
			}
		}
		if ( history ) {
			pushHistory();
		}
	}
	/*
	 * A region change in the panel is now only going to be applied to the panel.
	 * 
	 * Need some controls to prevent the global settings from overriding the panel setting after a panel it settings changed.
	 * 
	 * 
	ChangeListener regionChange = new ChangeListener() {

		public void onChange(Widget sender) {
		    RegionWidget r = (RegionWidget) sender;
			Widget parent = sender;
			String title="";
			while ( parent != null ) {
				parent = parent.getParent();
				if ( parent instanceof DialogBox ) {
					DialogBox d = (DialogBox) parent;
					title = d.getText();
				}
			}
			List<String> applyAll = new ArrayList<String>();
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
	    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
	    		if ( title.contains(panel.getID())) {
	    			applyAll = panel.getApplyAll();	
	    		}
			}
			if ( applyAll.contains("x") || applyAll.contains("y") ) {
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
		    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
		    		if (!title.contains(panel.getID())) {
		    			//TODO this assumes the regions are the same in each panel.  Bad assumption.
		    			panel.setRegion(r.getSelectedIndex(), r.getValue(r.getSelectedIndex()));
		    		}
				}
    		}	
		}		
	};
	 */
	/**
	 * A little helper method to change data sets.
	 */
	public void changeDataset() {
		var = nvar;
		changeDataset = false;
		differenceButton.setEnabled(true);
		differenceButton.setDown(false);


		// Since we are changing data sets, go to the default plot and view.

		// TODO Maybe we can derive the default operations from the data set during the init(), but it would require an asynchronous request
		// to know the default operation for the new dataset and variable...
		if ( nvar.getAttributes().get("grid_type").equals("regular") ) {
			op = "XY_zoomable_image";
		} else {
			op = "Insitu_extract_location_value_plot";
		}
		view = "xy";

		// Go get the grid if you don't have it already...
		Util.getRPCService().getGrid(var.getDSID(), var.getID(), getGridForChangeDatasetCallback);
	}

	/**
	 * A helper methods that moves the current state of the map widget to the panels.
	 */
	private void setMapRanges(double[] cs) {
		for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
			VizGalPanel panel = (VizGalPanel) panelIt.next();
			
			if (!panel.isUsePanelSettings()) {
				if ( cs == null ) {
				    panel.setLatLon(String.valueOf(settingsControls.getRefMap().getXlo()), String.valueOf(settingsControls.getRefMap().getXhi()), String.valueOf(settingsControls.getRefMap().getYlo()), String.valueOf(settingsControls.getRefMap().getYhi()));
				} else {
					//cs contains n, s, e w
					panel.setLatLon(String.valueOf(cs[3]), String.valueOf(cs[2]), String.valueOf(cs[1]), String.valueOf(cs[0]));
				}
			}
		}
	}
	ClickListener settingsButtonApplyListener = new ClickListener() {
		public void onClick(Widget sender) {
			
			if ( changeDataset ) {
				cs = settingsControls.getRefMap().getCurrentSelection();
				// This involves a jump across the wire, so the finishApply gets called in the callback from the getGrid.
				changeDataset();
			} else {
				// No jump required, just finish up now.
				finishApply();
			}
		}
	};
	public void finishApply() {
			// Check to see if the operation changed.  If so, change the tool.
			String op_id = settingsControls.getCurrentOp().getID();
			String op_view = settingsControls.getCurrentOperationView();
			if ( !op_id.equals(op) && !op_view.equals(view) ) {
				op = op_id;
				view = op_view;
			}
			settingsControls.setToolType(view);
			// Update the plot based on the new settings first set the "gallery map" then by moving the map settings
			// to all the panels that are under slide sorter control, the refresh (which handles the options).
			if ( cs != null ) {
			    settingsControls.setLatLon(String.valueOf(cs[3]), String.valueOf(cs[2]), String.valueOf(cs[1]), String.valueOf(cs[0]));
			}
			setMapRanges(cs);
			refresh(false, true);
	}
	ClickListener panelApplyButtonClick = new ClickListener() {
		public void onClick(Widget sender) {
			String title = sender.getTitle();
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				if (title.contains(panel.getID())) {
					// See if the panel settings are not being used on this panel, if not
					// reset the fixed axis, the lat/lon region and the operation to the slide sorter value.
					if ( !panel.isUsePanelSettings() ) {
						VariableSerializable v = panel.getVariable();
						if ( !v.getID().equals(var.getID()) || !v.getDSID().equals(var.getDSID() ) ) {
							panel.setVariable(var);
							panel.init(false);
						} 
						if ( fixedAxis.equals("t") ) {
							panel.setParentAxisValue("t", dateWidget.getFerretDateLo());
						} else if ( fixedAxis.equals("z") ) {
							panel.setParentAxisValue("z", xyzWidget.getLo());
						}
						panel.setLatLon(String.valueOf(settingsControls.getRefMap().getXlo()), String.valueOf(settingsControls.getRefMap().getXhi()), String.valueOf(settingsControls.getRefMap().getYlo()), String.valueOf(settingsControls.getRefMap().getYhi()));
						panel.setOperation(op, view);
					}
					if ( !panel.getCurrentOperationView().equals(settingsControls.getCurrentOperationView()) ) {
						differenceButton.setDown(false);
						differenceButton.setEnabled(false);
					} else {
						differenceButton.setEnabled(true);
					}
				}
			}
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
				lo_value = dateWidget.getFerretDateLo();
				hi_value = dateWidget.getFerretDateHi();
				range = dateWidget.isRange();
			} else if ( fixedAxis.equals("z") ) {
				lo_value = xyzWidget.getLo();
				hi_value = xyzWidget.getHi();
				range = xyzWidget.isRange();
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
					dateWidget.setLo(lo);
					dateWidget.setHi(hi);
				} else {
					dateWidget.setLo(lo);
				}	
			} else if ( axis.equals("z") ) {
				if ( range ) {
					xyzWidget.setLo(lo);
					xyzWidget.setHi(hi);
				} else {
					xyzWidget.setLo(lo);
				}
			}
		}
		for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
			VizGalPanel panel = (VizGalPanel) panelIt.next();
			if ( !panel.isUsePanelSettings() ) {
				if ( range ) {
					panel.setParentAxisRangeValues(axis, lo, hi);
				} else {
					panel.setParentAxisValue(axis, lo);
				}
			}
		}
	}
	ClickListener optionsOkListener = new ClickListener() {
		public void onClick(Widget sender) {
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
		VizGalPanel panel = panels.get(0);

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
		String fill_levels = "(" + uminr + "," + umaxr + "," + dint + ")";

		// These are pretty close to zero.  I think the min/max did not come back from the server, so stop
		if ( (uminr + .00001 < .0001 && umaxr + .00001 < .0001) || globalMax < -9999999. && globalMin > 9999999. ) {
			autoContourTextBox.setText(Constants.NO_MIN_MAX);
			autoContourButton.setDown(false);
		} else {
			autoContourTextBox.setText(fill_levels);
		}
	}
	public ClickListener operationsClickListener = new ClickListener() {
		public void onClick(Widget sender) {
			if ( sender instanceof OperationButton ) {
				setupMenusForOperationChange();
			}
		}
	};

	private void setupMenusForOperationChange() {
		view = settingsControls.getOperationsWidget().getCurrentView();
		op = settingsControls.getCurrentOp().getID();
		// Turn off the difference button when the compare axis is a range.			
		boolean diff = !view.contains(compareAxis);
		if ( !diff ) {
			differenceButton.setDown(false);
		}
		differenceButton.setEnabled(diff);
		if ( view.length() !=  2 ) {
			autoContourTextBox.setText("");
			autoContourButton.setDown(false);
			autoContourButton.setEnabled(false);
		} else {
			autoContourButton.setEnabled(true);
		}
		// Set the current fixed menu to a range if necessary.
		if ( view.contains(fixedAxis) ) {
			if ( fixedAxis.equals("t") ) {
				dateWidget.setRange(true);
			} else if ( fixedAxis.equals("z") ) {
				xyzWidget.setRange(true);
			}
		} else {
			if ( fixedAxis.equals("t") ) {
				dateWidget.setRange(false);
			} else if ( fixedAxis.equals("z") ) {
				xyzWidget.setRange(false);
			}
		}

		// Set the orthogonal axes to a range in each panel.
		for (Iterator panelsIt = panels.iterator(); panelsIt.hasNext();) {
			VizGalPanel panel = (VizGalPanel) panelsIt.next();
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

	}	
	private void pushHistory() {
		// First token collection is the gallery settings (mostly in the header of the UI)
		StringBuilder historyToken = new StringBuilder();
		historyToken.append("panelHeaderHidden="+panelHeaderHidden);
		historyToken.append(";differences="+differenceButton.isDown());
		historyToken.append(";compareAxis="+compareAxis);
		historyToken.append(";fixedAxis="+fixedAxis);
		if ( fixedAxis.equals("t") ) {
			historyToken.append(";fixedAxisLo="+dateWidget.getFerretDateLo());
			historyToken.append(";fixedAxisHi="+dateWidget.getFerretDateHi());
		} else if ( fixedAxis.equals("z") ) {
			historyToken.append(";fixedAxisLo="+xyzWidget.getLo());
			historyToken.append(";fixedAxisHi="+xyzWidget.getHi());
		}
		historyToken.append(";autoContour="+autoContourButton.isDown());
		if ( panels.get(0).getMin() < 99999999. ) {
			historyToken.append(";globalMin="+panels.get(0).getMin());
		}
		if ( panels.get(0).getMax() > -99999999. ) {
			historyToken.append(";globalMax="+panels.get(0).getMax());
		}
		historyToken.append(";dsid="+var.getDSID());
		historyToken.append(";varid="+var.getID());
		
		historyToken.append(";imageSize="+imageSize.getValue(imageSize.getSelectedIndex()));
		
		// Build the tokens for the panels.
		
		// The 0 panel is controlled by the SettingsControl in the gallery.
		
		// The next N tokens are the states of the individual panels.
		historyToken.append("token"+panels.get(0).getHistoryToken()+settingsControls.getHistoryToken());
		
		for (int i = 1; i < panels.size(); i++) {
			VizGalPanel panel = panels.get(i);
			historyToken.append("token"+panel.getHistoryToken()+panel.getSettingsWidgetHistoryToken());
		}
       
		History.newItem(historyToken.toString(), false);
	}
	private void popHistory(String historyToken) {
		if ( historyToken.equals("") ) {
			var = initial_var;
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
			for (int t = 1; t < panels.size(); t++) {

				VizGalPanel panel = panels.get(t);
				galleryOnly = galleryOnly && !panel.isUsePanelSettings();
			}



			// Process everything that applies to the gallery


			HashMap<String, String> tokenMap = Util.getTokenMap(settings[0]);
			/*
			 *  This history event uses the same data set and variable as the current state, so
			 *  we can just set apply the gallery settings.
			 */

			boolean switch_axis = false;
			if ( (tokenMap.containsKey("dsid") && tokenMap.get("dsid").equals(var.getDSID())) && 
				 (tokenMap.containsKey("varid") && tokenMap.get("varid").equals(var.getID())) )  {
				switch_axis = applyHistory(tokenMap);
				/*
				 * Apply the settings to the panels (the compare axis and some plot options).
				 * The first token string (index 0) is all the junk in the header so we start 
				 * counting the tokens with t+1.
				 * 
				 * The rest are the tokens for the individual panels.  We must apply the first 
				 * (index 0) to the gallery ControlSettings widget then the panel.
				 */
				
				for (int t = 0; t < panels.size(); t++) {
					HashMap<String, String> panelTokenMap = Util.getTokenMap(settings[t+1]);
					HashMap<String, String> optionsMap = Util.getOptionsMap(settings[t+1]);
					if ( t == 0 ) {
						settingsControls.setFromHistoryToken(panelTokenMap, optionsMap);
					}
					panels.get(t).setFromHistoryToken(panelTokenMap, optionsMap);
				}
			} else {
				// If the variable happens to be the initial variable use it.
				if ( (tokenMap.containsKey("dsid") && tokenMap.get("dsid").equals(initial_var.getDSID())) && 
					 (tokenMap.containsKey("varid") && tokenMap.get("varid").equals(initial_var.getID())) )  {
					initPanels();
					switch_axis = applyHistory(tokenMap);
				} else {
					// Otherwise, save the history tokens, call back to the server for the variable and then apply the history in the callback.
					historyTokens = tokenMap;
					Util.getRPCService().getGrid(tokenMap.get("dsid"), tokenMap.get("vid"), requestGridForHistory);
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
			if ( new_panelHeaderHidden != panelHeaderHidden ) {
				// If the new state should be different, handle it.  Otherwise ignore it.
				handlePanelShowHide();
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
				// You have to manually set the correct radio button since there was not button push to change to this state.
			}
			if ( new_fixedAxis.equals("t") ) {
				xyzButton.setValue(true);
				xyzWidget.setEnabled(false);
				dateButton.setValue(false);
				dateWidget.setEnabled(true);
				dateWidget.setLo(tokenMap.get("fixedAxisLo"));
				dateWidget.setHi(tokenMap.get("fixedAxisHi"));
			} else if ( new_fixedAxis.equals("z") ) {
				dateButton.setValue(true);
				dateWidget.setEnabled(false);
				xyzButton.setValue(false);
				xyzWidget.setEnabled(true);
				xyzWidget.setLo(tokenMap.get("fixedAxisLo"));
				xyzWidget.setHi(tokenMap.get("fixedAxisHi"));
			}
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

		int count = imageSize.getItemCount();
		for (int i = 0; i < count; i++ ) {
			String item_value = imageSize.getValue(i);
			if ( item_value.equals(tokenMap.get("imageSize")) ) {
				imageSize.setSelectedIndex(i);
			}
		}
		
		return switch_axis;
	}
}
