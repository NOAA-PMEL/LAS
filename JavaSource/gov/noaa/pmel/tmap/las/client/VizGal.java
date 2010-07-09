package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.AxesWidgetGroup;
import gov.noaa.pmel.tmap.las.client.laswidget.ComparisonAxisSelector;
import gov.noaa.pmel.tmap.las.client.laswidget.DatasetButton;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationRadioButton;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.OptionsButton;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Constants;
import gov.noaa.pmel.tmap.las.client.util.Util;
import gov.noaa.pmel.tmap.las.client.vizgal.VizGalPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.com.freller.tool.client.Print;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

/**
 * A UI widget with one or more panels containing an LAS product with widgets to interact with the specifications of the products.
 * @author rhs
 *
 */
public class VizGal implements EntryPoint {
    
	/*
	 * These are the four pieces of information required
	 * to initialize a SlideSorterOld
	 */
	String dsid;
	String vid;
	String view;
	String operationID;
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
	 * The slide sorter grid.
	 */
	Grid slides;

	/*
	 * The container for the slides and controls.
	 */
	FlexTable mainPanel;
	FlexCellFormatter cellFormatter;

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
	int rightPad = 45;
	/*
	 * Fixed size of the left control panel
	 */
	int controlsWidth = 290;
	String settingsWidth = String.valueOf(controlsWidth) + "px";

	
	// Try making the settings controls out of a collection of independent widgets managed by vizGal.
	
	
	DatasetButton datasetButton;
	OptionsButton optionsButton; 
	FlexTable buttonLayout;
	FlexCellFormatter buttonFormatter;
		
    AxesWidgetGroup axesWidget;
    ComparisonAxisSelector comparisonAxesSelector;
    OperationsWidget operationsWidget;
   
    FlexTable settingsControls;
    
    
    DisclosurePanel settingsHeader = new DisclosurePanel("Settings");
    DisclosurePanel hideControls = new DisclosurePanel("");
    
    boolean operationsPanelIsOpen = true;

    boolean panelHeaderHidden = false;
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
	 * Keep track of the new variable and change state to be able apply data set changes only after apply button is pressed.
	 */
	VariableSerializable nvar;
	boolean changeDataset = false;
	

	/*
	 * Image size control.
	 */
	ListBox imageSize;
	Label imageSizeLabel = new Label("Image zoom: ");
	int pwidth = 0;
	
	/*
	 * Make an HTML only popup page that can be printed.
	 */
	PushButton printerFriendlyButton;
	
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
     
     
	public void onModuleLoad() {
		
		ortho = new ArrayList<String>(); 

		dsid = Util.getParameterString("dsid");
		vid = Util.getParameterString("vid");
		//TODO If the operation is null, get the default operation (the map or plot; left nav) for this view.
		operationID = Util.getParameterString("opid");
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

		
		// Button to turn on and off difference mode.
		differenceButton = new ToggleButton("Difference Mode");
		differenceButton.ensureDebugId("differenceButton");
		differenceButton.setTitle("Toggle Difference Mode");
		differenceButton.addClickListener(differencesClick);

		// This is a control panel that will control the settings for all the panels and it appears vertically on the left side.
		
	    buttonLayout = new FlexTable();
	    buttonFormatter = buttonLayout.getFlexCellFormatter();
	    datasetButton = new DatasetButton();
	    datasetButton.ensureDebugId("datasetButton");
	    datasetButton.addTreeListener(datasetTreeListener);
	    // This is all to get around the fact that the OpenLayers map is always in front.
	    datasetButton.addOpenClickHandler(datasetOpenHandler);
	    datasetButton.addCloseClickHandler(datasetCloseHandler);
	    optionsButton = new OptionsButton(optionID, 0);
	    optionsButton.ensureDebugId("optionsButton");
	    optionsButton.addOkClickListener(optionsOkListener);
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
	   
		buttonLayout.setWidget(0, 0, hideControls);
		buttonLayout.setWidget(0, 1, datasetButton);
		buttonLayout.setWidget(0, 2, optionsButton);
		buttonLayout.setWidget(0, 3, differenceButton);
		
		axesWidget = new AxesWidgetGroup("Plot Axes", "Comparison Axes", "vertical", settingsWidth, "ApplyTo_gallery");
		axesWidget.addApplyHandler(settingsButtonApplyHandler);
		// Comparison Axes Selector
		comparisonAxesSelector = new ComparisonAxisSelector(settingsWidth);
		comparisonAxesSelector.addAxesChangeHandler(compareAxisChangeHandler);
		
		operationsWidget = new OperationsWidget("Operations");
		operationsWidget.addClickHandler(operationsClickHandler);
        settingsHeader.setOpen(true);		
		settingsHeader.addCloseHandler(new CloseHandler<DisclosurePanel> () {

			@Override
			public void onClose(CloseEvent<DisclosurePanel> event) {
				operationsWidget.setOpen(false);
				comparisonAxesSelector.setOpen(false);
				axesWidget.setOpen(false);
			}
			
		});
		settingsHeader.addOpenHandler(new OpenHandler<DisclosurePanel>() {

			@Override
			public void onOpen(OpenEvent<DisclosurePanel> arg0) {
				operationsWidget.setOpen(true);
				comparisonAxesSelector.setOpen(true);
				axesWidget.setOpen(true);
			}
			
		});
		
		// Sets the contour levels for all plots based on the global min/max of the data (as returned in the map scale file).
		autoContourButton = new ToggleButton("Auto Colors");
		autoContourButton.ensureDebugId("autoContourButton");
		autoContourButton.setTitle("Set consistent contour levels for all panels.");
		autoContourButton.addClickListener(autoContour);
		
		buttonLayout.setWidget(0, 4, autoContourButton);
		autoContourTextBox = new TextBox();
		autoContourTextBox.ensureDebugId("autoContourTextBox");
		buttonLayout.setWidget(0, 5, autoContourTextBox);

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
		
		
		buttonLayout.setWidget(0, 6, imageSizeLabel);
		buttonLayout.setWidget(0, 7, imageSize);
		
		printerFriendlyButton = new PushButton("Print...");
	    printerFriendlyButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent print) {
				
				printerFriendly();
				
			}
	    	
	    });
	    
	    buttonLayout.setWidget(0, 8, printerFriendlyButton);
		
		// Initialize the gallery with an asynchronous call to the server to get variable needed.
		if ( dsid != null && vid != null & operationID != null && view != null) {
			// If the proper information was sent to the widget, pull down the variable definition
			// and initialize the slide sorter with this Ajax call.
			Util.getRPCService().getVariable(dsid, vid, requestGrid);
		}
        
		settingsControls = new FlexTable();
		settingsControls.setWidget(0, 0, settingsHeader);
		
		settingsControls.setWidget(1, 0, axesWidget);
		settingsControls.setWidget(2, 0, comparisonAxesSelector);
		settingsControls.setWidget(3, 0, operationsWidget);
		
		mainPanel = new FlexTable();
		
		cellFormatter = mainPanel.getFlexCellFormatter();
		cellFormatter.setColSpan(0, 0, 2);
		cellFormatter.setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
		cellFormatter.setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
		
		mainPanel.setWidget(0, 0, buttonLayout);
		mainPanel.setWidget(1, 0, settingsControls);
		mainPanel.setWidget(1, 1, slides);
		

		
		RootPanel.get("vizGal").add(mainPanel);
		RootPanel.get("PLOT_LINK").setVisible(false);
		
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
			cellFormatter.setVisible(1, 0, true);
			pwidth = pwidth - controlsWidth/2;
			resize();
		} else {
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.hide();
			}
			cellFormatter.setVisible(1, 0, false);
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
				changeDataset();
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
			// Null view to get all operations.
			Util.getRPCService().getConfig(null, dsid, vid, initVizGal);
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
			operationsWidget.setOperations(var.getIntervals(), operationID, view, ops);
			optionsButton.setOptions(operationsWidget.getCurrentOperation().getOptionsID());
			var.setGrid(grid);
			// Figure out the compare and fixed axis
			init();
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.setPanelColor("regularBackground");
				panel.setVariable(var);
				panel.init(false, ops);
				panel.setOperation(operationID, view);
				
				
				if ( fixedAxis.equals("t") ) {
					panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
				} else if ( fixedAxis.equals("z") ) {
					panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
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
	AsyncCallback<ConfigSerializable> initVizGal = new AsyncCallback<ConfigSerializable>() {
		public void onSuccess(ConfigSerializable config) {
			
			GridSerializable grid = config.getGrid();
			ops = config.getOperations();
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
		panels.clear();
		
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
		VizGalPanel sp1 = new VizGalPanel("Panel 0", true, operationID, optionID, view, false);
		sp1.addRevertHandler(panelApplyButtonClick);
		sp1.addApplyHandler(panelApplyButtonClick);
		slides.setWidget(0, 0, sp1);
		sp1.setPanelWidth(pwidth);
		panels.add(sp1);

		VizGalPanel sp2 = new VizGalPanel("Panel 1", false, operationID, optionID, view, false);
		sp2.addRevertHandler(panelApplyButtonClick);
		sp2.addApplyHandler(panelApplyButtonClick);
		slides.setWidget(0, 1, sp2);
		sp2.setPanelWidth(pwidth);		
		panels.add(sp2);

		VizGalPanel sp3 = new VizGalPanel("Panel 2", false, operationID, optionID, view, false);
		sp3.addRevertHandler(panelApplyButtonClick);
		sp3.addApplyHandler(panelApplyButtonClick);
		slides.setWidget(1, 0, sp3);
		sp3.setPanelWidth(pwidth);		
		panels.add(sp3);

		VizGalPanel sp4 = new VizGalPanel("Panel 3", false, operationID, optionID, view, false);
		sp4.addRevertHandler(panelApplyButtonClick);
		sp4.addApplyHandler(panelApplyButtonClick);
		slides.setWidget(1, 1, sp4);
		sp4.setPanelWidth(pwidth);		
		panels.add(sp4);

		sp1.setVariable(var);
		sp1.init(false, ops);
		sp2.setVariable(var);
		sp2.init(false, ops);
		sp3.setVariable(var);
		sp3.init(false, ops);
		sp4.setVariable(var);
		sp4.init(false, ops);

		init();
		
		if ( tlo != null && !tlo.equals("") ) {
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.setAxisRangeValues("t", tlo, thi);
			}
			axesWidget.getTAxis().setLo(tlo);
			axesWidget.getTAxis().setHi(thi);
		}
		if ( zlo != null && !zlo.equals("") ) {
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.setAxisRangeValues("z", zlo, zhi);
			}
			axesWidget.getZAxis().setLo(zlo);
			axesWidget.getZAxis().setHi(zhi);
		}

//		if ( ortho.contains("t") && tlo != null && !tlo.equals("") ) {
//			// If "t" is the orthogonal to the plot then set it to the passed in values in the panels
//			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
//				VizGalPanel panel = (VizGalPanel) panelIt.next();
//				panel.setAxisRangeValues("t", tlo, thi);
//			}
//			// and in the currently hidden fixed axis widget
//			if ( axesWidget.getTAxis().isRange() ) {
//				axesWidget.getTAxis().setLo(tlo);
//				axesWidget.getTAxis().setHi(thi);
//			} else {
//			    axesWidget.getTAxis().setLo(tlo);
//			}
//
//
//			// And if z exists, it will be the fixed axis so it also needs to be set.
//			if ( fixedAxis.equals("z") ) {
//				axesWidget.getZAxis().setLo(zlo);
//				axesWidget.getZAxis().setHi(zhi);			
//				// Pass the settings down to the panels
//				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
//					VizGalPanel panel = (VizGalPanel) panelIt.next();
//					panel.setParentAxisRange("z", true);
//					panel.setAxisRangeValues("z", zlo, zhi);
//				}
//			}
//		} else if ( ortho.contains("z") && zlo != null && !zlo.equals("") ) {
//			// Same if z is orthogonal to the plot.
//			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
//				VizGalPanel panel = (VizGalPanel) panelIt.next();
//				panel.setAxisRangeValues("z", zlo, zhi);
//			}
//			if ( axesWidget.getZAxis().isRange() ) {
//				axesWidget.getZAxis().setLo(zlo);
//				axesWidget.getZAxis().setHi(zhi);
//			} else {
//				axesWidget.getZAxis().setLo(zlo);
//			}
//
//			if ( fixedAxis.equals("t") ) {
//				axesWidget.getTAxis().setLo(tlo);
//				axesWidget.getTAxis().setHi(thi);
//				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
//					VizGalPanel panel = (VizGalPanel) panelIt.next();
//					panel.setAxisRangeValues("t", tlo, thi);
//				}	
//			}
//		}
		// If these limits are not the same as the dataBounds, then set them.
		if ( xlo != null && !xlo.equals("") && xhi != null && !xhi.equals("") && 
			 ylo != null && !ylo.equals("") && yhi != null && !yhi.equals("") ) {
			axesWidget.getRefMap().setCurrentSelection(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.setMapTool(view);
				panel.setLatLon(ylo, yhi, xlo, xhi);
			}
		} else {
			double tmp_xlo = axesWidget.getRefMap().getXlo();
			double tmp_xhi = axesWidget.getRefMap().getXhi();
			double tmp_ylo = axesWidget.getRefMap().getYlo();
			double tmp_yhi = axesWidget.getRefMap().getYhi();
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				panel.setMapTool(view);
				panel.setLatLon(String.valueOf(tmp_ylo), String.valueOf(tmp_yhi), String.valueOf(tmp_xlo), String.valueOf(tmp_xhi));
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
					
					operationsWidget.setOperation(tokenMap.get("operation_id"), panelTokenMap.get("view"));
					axesWidget.getRefMap().setCurrentSelection(Double.valueOf(panelTokenMap.get("ylo")),
							                                   Double.valueOf(panelTokenMap.get("yhi")), 
							                                   Double.valueOf(panelTokenMap.get("xlo")),
							                                   Double.valueOf(panelTokenMap.get("xhi")));
					if ( optionsMap.size() >= 1 ) {
						optionsButton.setState(optionsMap);
					}
					
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

		operationsWidget.setOperations(var.getIntervals(), operationID, view, ops);
		optionsButton.setOptions(operationsWidget.getCurrentOperation().getOptionsID());
		GridSerializable ds_grid = var.getGrid();
		double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
		double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

		double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
		double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

		double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));
		axesWidget.getRefMap().setTool(view);
		axesWidget.getRefMap().setDataExtent(grid_south, grid_north, grid_west, grid_east, delta);
		
		ortho = Util.setOrthoAxes(view, var.getGrid());
		
		if ( ortho.size() == 0 ) {
			Window.alert("There are no axes orthogonal to the view on which the data can be compared.");
			return false;
		} else {

			int pos = 0;
			// Figure out which axis vary in each frame.  Take them in order of t, z, y, x...
			if ( ortho.contains("t") ) {
				
				compareAxis = "t";
				
				if ( ortho.contains("z") ) {
					fixedAxis = "z";
				} else if ( ortho.contains("y") ) {
					fixedAxis = "y";
				} else if ( ortho.contains("x") ) {
					fixedAxis = "x";
				} else {
					fixedAxis = "";
				}
		
			}  else if ( ortho.contains("z") ) {
				
				compareAxis = "z";
				
				if ( ortho.contains("y") ) {
					fixedAxis = "y";
				} else if ( ortho.contains("x") ) {
					fixedAxis = "x";
				} else {
					fixedAxis = "";
				}
				
			} else if ( ortho.contains("y") ) {
                
				compareAxis = "y";
				
				if ( ortho.contains("x") ) {
					fixedAxis = "x";
				} else {
					fixedAxis = "";
				}
				
			} else if ( ortho.contains("x") ) {
				
				compareAxis = "x";
				fixedAxis = "";
				
			}
			
		    
			comparisonAxesSelector.setAxes(ortho);
			axesWidget.init(var.getGrid());
			axesWidget.setFixedAxis(view, ortho, compareAxis);
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
				axesWidget.setRange("t", false);
				fixedAxisLoValue = axesWidget.getZAxis().getLo();
				fixedAxisHiValue = axesWidget.getZAxis().getHi();
				fixed_axis_range = axesWidget.getZAxis().isRange();	
			}  else {
				axesWidget.setRange("z", false);
				fixedAxisLoValue = axesWidget.getTAxis().getFerretDateLo();
				fixedAxisHiValue = axesWidget.getTAxis().getFerretDateHi();
				fixed_axis_range = axesWidget.getTAxis().isRange();
			}
			axesWidget.setFixedAxis(view, ortho, compareAxis);
			// Set the value of the fixed axis in all the panels under slide sorter control.
			ortho = Util.setOrthoAxes(view, var.getGrid());
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				if ( !panel.isUsePanelSettings() ) {
					panel.setCompareAxis(view, ortho, compareAxis);
					panel.setAxisRangeValues(fixedAxis, fixedAxisLoValue, fixedAxisHiValue);
					
					// A time or zWidget is already initialized so the setCompareAxis displays
					// these widgets and they are ready to go.  A map on the other hand has to
					// be initialized after it gets placed in the display.
					double[] data = axesWidget.getRefMap().getDataExtent();
					double[] selection = axesWidget.getRefMap().getCurrentSelection();
					// Set the map tool.
					panel.setMapTool(view);
					// Set the data extents.
					panel.setDataExtent(data);
					// Set the current selection.
					panel.setLatLon(selection);
					
				}
			}
			refresh(true, true);

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
				if ( panel.isComparePanel() ) {
					comparePanel = panel;
					panel.refreshPlot(optionsButton.getState(), switchAxis, true);	
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
						// The values for the first variable come from either the Gallery or
						// the comparison panel.
						if ( compareAxis.contains("x") ) {
							xlo = String.valueOf(comparePanel.getXlo());
							xhi = String.valueOf(comparePanel.getXhi());
						} else {
							xlo = String.valueOf(axesWidget.getRefMap().getXlo());
							xhi = String.valueOf(axesWidget.getRefMap().getXhi());
						}
						if ( compareAxis.contains("y") ) {
							ylo = String.valueOf(comparePanel.getYlo());
							yhi = String.valueOf(comparePanel.getYhi());
						} else {
							ylo = String.valueOf(axesWidget.getRefMap().getYlo());
							yhi = String.valueOf(axesWidget.getRefMap().getYhi());
						}
						
						if ( var.getGrid().hasT() ) {
							if ( compareAxis.equals("t") ) {
							    tlo = comparePanel.getTlo();
							    thi = comparePanel.getThi();
							} else {
								tlo = axesWidget.getTAxis().getFerretDateLo();
								thi = axesWidget.getTAxis().getFerretDateHi();
							}
						} else {
							tlo = null;
							thi = null;
						}
						if ( var.getGrid().hasZ() ) {
							if ( compareAxis.equals("z") ) {
								zlo = comparePanel.getZlo();
								zhi = comparePanel.getZhi();
							} else {
								zlo = axesWidget.getZAxis().getLo();
								zhi = axesWidget.getZAxis().getHi();
							}
						} else {
							zlo = null;
							zhi = null;
						}
						panel.computeDifference(optionsButton.getState(), switchAxis, comparePanel.getVariable(), operationsWidget.getCurrentView(), xlo, xhi, ylo, yhi, zlo, zhi, tlo, thi);
					}
				}
			}
		} else {
			double plot_xlo = -9999.;
			double plot_xhi = -9999.;
			double plot_yhi = -9999.;
			double plot_ylo = -9999.;
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();
				if ( panel.isComparePanel() ) {
					plot_xlo = panel.getXlo();
					plot_xhi = panel.getXhi();
					plot_ylo = panel.getYlo();
					plot_yhi = panel.getYhi();
				}
			}
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				VizGalPanel panel = (VizGalPanel) panelIt.next();

				// See if the panel settings are being used on this panel, if not
				// reset the fixed axis and the view axis and the operation to the slide sorter value.
				if ( !panel.isUsePanelSettings() ) {
					VariableSerializable v = panel.getVariable();
					if ( !v.getID().equals(var.getID()) || !v.getDSID().equals(var.getDSID() ) ) {
						panel.setVariable(var);
						panel.init(false, ops);
					} 
					if ( view.equals("xy") && compareAxis.equals("z") ) {
						// set x, y, and t in the panel
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
								        String.valueOf(axesWidget.getRefMap().getYhi()), 
								        String.valueOf(axesWidget.getRefMap().getXlo()), 
								        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("xy") && compareAxis.equals("t") ) {
						// set x, y and z in the panel
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("x") && compareAxis.equals("t") ) {
						// The map and z are in the fixed axis panel,  take the map value for both x and y and set z. 
						// ( OR from above )
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("x") && compareAxis.equals("z") ) {
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("x") && compareAxis.equals("y") ) {
						// The map is in the panels.  z and t are in the fixed axes panel.
						// Take z and t from the global controls and x from the first panel and put back the y from each panel
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(panel.getYlo()), 
						        String.valueOf(panel.getYhi()), 
						        String.valueOf(plot_xlo), 
						        String.valueOf(plot_xhi));
					} else if ( view.equals("y") && compareAxis.equals("t") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("y") && compareAxis.equals("z") ) {
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("y") && compareAxis.equals("x") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(plot_ylo), 
						        String.valueOf(plot_yhi), 
						        String.valueOf(panel.getXlo()), 
						        String.valueOf(panel.getXhi()));
					} else if ( view.equals("z") && compareAxis.equals("t") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("z") && compareAxis.equals("xy") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
					} else if ( view.equals("t") && compareAxis.equals("z") ) {
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("t") && compareAxis.equals("xy") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
					} else if ( view.equals("xz") && compareAxis.equals("y") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(panel.getYlo()), 
						        String.valueOf(panel.getYhi()), 
						        String.valueOf(plot_xlo), 
						        String.valueOf(plot_xhi));
					} else if ( view.equals("xz") && compareAxis.equals("t") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("yz") && compareAxis.equals("x") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(plot_ylo), 
						        String.valueOf(plot_yhi), 
						        String.valueOf(panel.getXlo()), 
						        String.valueOf(panel.getXhi()));
					} else if ( view.equals("yz") && compareAxis.equals("t") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("xt") && compareAxis.equals("z") ) {
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("xt") && compareAxis.equals("y") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(panel.getYlo()), 
						        String.valueOf(panel.getYhi()), 
						        String.valueOf(plot_xlo), 
						        String.valueOf(plot_xhi));
					} else if ( view.equals("yt") && compareAxis.equals("x") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(plot_ylo), 
						        String.valueOf(plot_yhi), 
						        String.valueOf(panel.getXlo()), 
						        String.valueOf(panel.getXhi()));
					} else if ( view.equals("yt") && compareAxis.equals("z") ) {
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
						panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), 
						        String.valueOf(axesWidget.getRefMap().getYhi()), 
						        String.valueOf(axesWidget.getRefMap().getXlo()), 
						        String.valueOf(axesWidget.getRefMap().getXhi()));
					} else if ( view.equals("zt") ) {
						if ( var.getGrid().hasZ() ) {
							panel.setAxisRangeValues("z", axesWidget.getZAxis().getLo(), axesWidget.getZAxis().getHi());
						}
						if ( var.getGrid().hasT() ) {
							panel.setAxisRangeValues("t", axesWidget.getTAxis().getFerretDateLo(), axesWidget.getTAxis().getFerretDateHi());
						}
					}
					panel.setOperation(operationID, view);
				}
				if ( !panel.getCurrentOperationView().equals(operationsWidget.getCurrentView()) ) {
					differenceButton.setDown(false);
					differenceButton.setEnabled(false);
				} else {
					if ( var.isVector() ) {
						if ( !view.equals("xy") ) {
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
				Map<String, String> temp_state = new HashMap<String, String>(optionsButton.getState());
				if ( autoContourButton.isDown() ) {
					// If the auto button is down, it wins...
					autoScale();
				} else {
					// If it's not down, the current options value will be used.
					autoContourTextBox.setText("");
				}
				
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
		if ( var.isVector() ) {
			autoContourTextBox.setText("");
			autoContourButton.setDown(false);
			autoContourButton.setEnabled(false);
			if ( !view.equals("xy") ) {
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
		if ( nvar.getAttributes().get("grid_type").equals("regular") ) {
			operationID = "Plot_2D_XY_zoom";
		} else if ( nvar.getAttributes().get("grid_type").equals("vector") ) {
			operationID = "Plot_vector";
		} else {
			operationID = "Insitu_extract_location_value_plot";
		}
		view = "xy";

		// Get all the config info.  View is null to get all operations.
		Util.getRPCService().getConfig(null, var.getDSID(), var.getID(), getGridForChangeDatasetCallback);
		
	}

	/**
	 * A helper methods that moves the current state of the map widget to the panels.
	 */
	private void setMapRanges(double[] cs) {
		for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
			VizGalPanel panel = (VizGalPanel) panelIt.next();
			
			if (!panel.isUsePanelSettings()) {
				panel.setMapTool(view);
				if ( cs == null ) {
				    panel.setLatLon(String.valueOf(axesWidget.getRefMap().getYlo()), String.valueOf(axesWidget.getRefMap().getYhi()), String.valueOf(axesWidget.getRefMap().getXlo()), String.valueOf(axesWidget.getRefMap().getXhi()));
				} else {
					//cs contains s, n, w, e
					panel.setLatLon(String.valueOf(cs[0]), String.valueOf(cs[1]), String.valueOf(cs[2]), String.valueOf(cs[3]));
				}
			}
		}
	}
	public void applyChange() {
		if ( changeDataset ) {
			cs = axesWidget.getRefMap().getCurrentSelection();
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
		String op_id = operationsWidget.getCurrentOperation().getID();
		String op_view = operationsWidget.getCurrentView();
		if ( !op_id.equals(operationID) && !op_view.equals(view) ) {
			operationID = op_id;
			view = op_view;
		}
		// The view may have changed if the operation changed before the apply.
		axesWidget.getRefMap().setTool(view);
		
		refresh(false, true);
	}
	ClickHandler panelApplyButtonClick = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			refresh(false, true);
			Widget sender = (Widget) event.getSource();
			String title = sender.getTitle();
			
			
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
				lo_value = axesWidget.getTAxis().getFerretDateLo();
				hi_value = axesWidget.getTAxis().getFerretDateHi();
				range = axesWidget.getTAxis().isRange();
			} else if ( fixedAxis.equals("z") ) {
				lo_value = axesWidget.getZAxis().getLo();
				hi_value = axesWidget.getZAxis().getHi();
				range = axesWidget.getZAxis().isRange();
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
					axesWidget.getTAxis().setLo(lo);
					axesWidget.getTAxis().setHi(hi);
				} else {
					axesWidget.getTAxis().setLo(lo);
				}	
			} else if ( axis.equals("z") ) {
				if ( range ) {
					axesWidget.getZAxis().setLo(lo);
					axesWidget.getZAxis().setHi(hi);
				} else {
					axesWidget.getZAxis().setLo(lo);
				}
			}
		}
		for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
			VizGalPanel panel = (VizGalPanel) panelIt.next();
			if ( !panel.isUsePanelSettings() ) {
				panel.setAxisRangeValues(axis, lo, hi);
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
		String fill_levels = "(-inf)(" + uminr + "," + umaxr + "," + dint + ")(inf)";

		// These are pretty close to zero.  I think the min/max did not come back from the server, so stop
		if ( (uminr + .00001 < .0001 && umaxr + .00001 < .0001) || globalMax < -9999999. && globalMin > 9999999. ) {
			autoContourTextBox.setText(Constants.NO_MIN_MAX);
			autoContourButton.setDown(false);
		} else {
			autoContourTextBox.setText(fill_levels);
		}
	}
	public ClickHandler operationsClickHandler = new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			Widget sender = (Widget) event.getSource();
			if ( sender instanceof OperationRadioButton ) {
				setupMenusForOperationChange();
			}
		}
	};

	private void setupMenusForOperationChange() {
		view = operationsWidget.getCurrentView();
		operationID = operationsWidget.getCurrentOperation().getID();
		optionsButton.setOptions(operationsWidget.getCurrentOperation().getOptionsID());
		ortho = Util.setOrthoAxes(view, var.getGrid());
		
		if ( var.isVector() ) {
			if ( !view.equals("xy") ) {
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
		comparisonAxesSelector.setAxes(ortho);
		
		if ( view.length() !=  2 ) {
			autoContourTextBox.setText("");
			autoContourButton.setDown(false);
			autoContourButton.setEnabled(false);
		} else {
			if ( var.isVector() ) {
				autoContourTextBox.setText("");
				autoContourButton.setDown(false);
				autoContourButton.setEnabled(false);
			} else {
			    autoContourButton.setEnabled(true);
			}
		}
		compareAxis = ortho.get(0);
		if ( ortho.size() > 1 ) {
		    fixedAxis = ortho.get(1);
		} else {
			fixedAxis = "";
		}
		axesWidget.setFixedAxis(view, ortho, compareAxis);
        // This will not be right for the new paradigm.
		// Set the orthogonal axes to a range in each panel.
		for (Iterator panelsIt = panels.iterator(); panelsIt.hasNext();) {
			VizGalPanel panel = (VizGalPanel) panelsIt.next();
			panel.setOperation(operationID, view);
			
			panel.setCompareAxis(view, ortho, compareAxis);
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
		applyChange();
	}	
	
	private void pushHistory() {
		// First token collection is the gallery settings (mostly in the header of the UI)
		StringBuilder historyToken = new StringBuilder();
		historyToken.append("panelHeaderHidden="+panelHeaderHidden);
		historyToken.append(";differences="+differenceButton.isDown());
		historyToken.append(";compareAxis="+compareAxis);
		historyToken.append(";fixedAxis="+fixedAxis);
		if ( fixedAxis.equals("t") ) {
			historyToken.append(";fixedAxisLo="+axesWidget.getTAxis().getFerretDateLo());
			historyToken.append(";fixedAxisHi="+axesWidget.getTAxis().getFerretDateHi());
		} else if ( fixedAxis.equals("z") ) {
			historyToken.append(";fixedAxisLo="+axesWidget.getZAxis().getLo());
			historyToken.append(";fixedAxisHi="+axesWidget.getZAxis().getHi());
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
		historyToken.append("token"+panels.get(0).getHistoryToken()+getHistoryToken());
		
		for (int i = 1; i < panels.size(); i++) {
			VizGalPanel panel = panels.get(i);
			historyToken.append("token"+panel.getHistoryToken()+panel.getSettingsWidgetHistoryToken());
		}
       
		History.newItem(historyToken.toString(), false);
	}
	private String getHistoryToken() {
		StringBuilder token = new StringBuilder();
		token.append(";xlo="+axesWidget.getRefMap().getXlo());
		token.append(";xhi="+axesWidget.getRefMap().getXhi());
		token.append(";ylo="+axesWidget.getRefMap().getYlo());
		token.append(";yhi="+axesWidget.getRefMap().getYhi());
		if ( operationsWidget.getCurrentOperation() != null ) {
			token.append(";operation_id="+operationsWidget.getCurrentOperation().getID());
			token.append(";view="+operationsWidget.getCurrentView());
		}
		Map<String, String> options = optionsButton.getState();
		for (Iterator opIt = options.keySet().iterator(); opIt.hasNext();) {
			String name = (String) opIt.next();
			String value = options.get(name);
			if ( !value.equalsIgnoreCase("default") ) {
				token.append(";ferret_"+name+"="+value);
			}
		}		
		return token.toString();
	}
	public void setFromHistoryToken(Map<String, String> tokenMap, Map<String, String> optionsMap) {
		operationsWidget.setOperation(tokenMap.get("operation_id"), tokenMap.get("view"));
		//s, n, w, e
		axesWidget.getRefMap().setCurrentSelection(Double.valueOf(tokenMap.get("ylo")),
				                                   Double.valueOf(tokenMap.get("yhi")), 
				                                   Double.valueOf(tokenMap.get("xlo")), 
				                                   Double.valueOf(tokenMap.get("xhi")));
		if ( optionsMap.size() >= 1 ) {
			optionsButton.setState(optionsMap);
		}
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
						setFromHistoryToken(panelTokenMap, optionsMap);
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
				    comparisonAxesSelector.setValue(tokenMap.get("compareAxis"));
				}
			}
			
			if ( new_fixedAxis.equals("t") ) {
				axesWidget.getTAxis().setLo(tokenMap.get("fixedAxisLo"));
				axesWidget.getTAxis().setHi(tokenMap.get("fixedAxisHi"));
			} else if ( new_fixedAxis.equals("z") ) {
				axesWidget.getZAxis().setLo(tokenMap.get("fixedAxisLo"));
				axesWidget.getZAxis().setHi(tokenMap.get("fixedAxisHi"));
			}
			axesWidget.setFixedAxis(view, ortho, compareAxis);
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

		int count = imageSize.getItemCount();
		for (int i = 0; i < count; i++ ) {
			String item_value = imageSize.getValue(i);
			if ( item_value.equals(tokenMap.get("imageSize")) ) {
				imageSize.setSelectedIndex(i);
			}
		}
		
		return switch_axis;
	}
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
	
	private void printerFriendly() {

		final double image_h = 631.;
		final double image_w = 998.;
		final PopupPanel pop = new PopupPanel(false);
		final boolean panelHidden = panelHeaderHidden;
		final Image[] images = new Image[panels.size()];
		final ListBox size = new ListBox();
		size.addItem("100%", "1.0");
		size.addItem(" 90%", ".9");
		size.addItem(" 80%", ".8");
		size.addItem(" 70%", ".7");
		size.addItem(" 60%", ".6");
		size.addItem(" 50%", ".5");
		size.addItem(" 40%", ".4");
		size.addItem(" 30%", ".3");
		size.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
                double px = image_w * Double.valueOf(size.getValue(size.getSelectedIndex()));
				int pw = (int) px;
				for(int i = 0; i < images.length; i++ ) {
					images[i].setWidth(pw+"px");
				}

			}

		});
		Label imageSizeLabel= new Label("Image Size: ");
		if ( !panelHidden ) {
			handlePanelShowHide();
		}
		PushButton close = new PushButton("Close");
		close.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				pop.hide();
				if ( !panelHidden ) {
					handlePanelShowHide();
				}
				
			}
			
		});
		
		PushButton print = new PushButton("Print");
		print.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				Print.it(pop);
			}
			
		});
		
		FlexTable buttons = new FlexTable();
		buttons.setWidget(0, 0, close);
		buttons.setWidget(0, 1, print);
		buttons.setWidget(0, 2, imageSizeLabel);
		buttons.setWidget(0, 3, size);
		FlexTable plots = new FlexTable();
		FlexCellFormatter formatter =  plots.getFlexCellFormatter();
		formatter.setColSpan(0, 0, 2);
		plots.setWidget(0, 0, buttons);
		pop.setGlassEnabled(true);
		int rows = panels.size()/2;
		int cols = 2;
		int panel_index = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				String panel_url = ((VizGalPanel)panels.get(panel_index)).getURL();
				final Image image = new Image(panel_url+"&stream=true&stream_ID=plot_image");
				images[panel_index] = image;
				plots.setWidget(i+1, j, image);
				panel_index++;
			}
		}
		pop.add(plots);
		pop.show();
	}
}
