package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.AnalysisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.AxesWidgetGroup;
import gov.noaa.pmel.tmap.las.client.laswidget.ComparisonAxisSelector;
import gov.noaa.pmel.tmap.las.client.laswidget.Constants;
import gov.noaa.pmel.tmap.las.client.laswidget.DatasetButton;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.OptionsButton;
import gov.noaa.pmel.tmap.las.client.laswidget.OutputPanel;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;
import gov.noaa.pmel.tmap.las.client.laswidget.OutputPanel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

public class BaseUI implements EntryPoint {
	/*
	 * Contains the visualization panels for this UI.  They are laid out in the xPanelTable
	 */
	List<OutputPanel> xPanels = new ArrayList<OutputPanel>();
	FlexTable xPanelTable = new FlexTable();

	String xDSID;
	String xVarID;
	String xView;
	String xOperationID;
	String xOptionID;
	/*
	 * These are optional parameters that can be used to set the xyzt ranges for the initial plot in the panels.
	 */
	String xXlo;
	String xXhi;
	String xYlo;
	String xYhi;
	String xZlo;
	String xZhi;
	String xTlo;
	String xThi;

	
	/*
	 * Keep track of the axes orthogonal to the view.
	 */
	List<String> xOrtho = new ArrayList<String>();
	
	
	/*
	 * Keep track of the current variable and the new variable when switching.
	 */
	VariableSerializable xVariable;
	VariableSerializable xNewVariable;

	/*
	 * Every Plot UI must have a data set and a plot options button
	 */
	DatasetButton xDatasetButton = new DatasetButton();
	/*
	 *  Keep track of the selection handler so it can be removed 
	 */
	HandlerRegistration xRegisterDatasetSelectionHandler;
	/*
	 * Every UI that wants to extent this base must register their own data set selection handler.
	 */
	SelectionHandler<TreeItem> xDatasetSelectionHandler;
	
	/*
	 *  Keep track of the open handler so it can be removed 
	 */
	HandlerRegistration xRegisterDatasetOpenHandler;
	
	
	
	OptionsButton xOptionsButton; 
	
	// The default handler when OK is clicked.  Everybody has to implement their own.
	HandlerRegistration xRegisterOptionsHandler;
	ClickHandler xOptionsClickHandler;
	
	//Same with the operations click handler...
	HandlerRegistration xRegisterOperationsHandler;
	ClickHandler xOperationsClickHandler = null;
	

	/*
	 * Whenever the dataset selector is open, everything else must be closed for neatness and issues with the
	 * OLMapWidget div always floating on top.  This allows us to restore the state of the operations panel.
	 */
	boolean xOperationsPanelIsOpen = true;

	// Number of panels, controls behavior of some listeners.
	int xPanelCount;

	// Main panel, contains the navigation controls,
	// the buttons across the top and the results panel.
	FlexTable xMainPanel = new FlexTable();
	FlexCellFormatter xMainPanelCellFormatter;

	// Left-side navigation widgets
	FlexTable xNavigationControls = new FlexTable();

	// Top level arrow to collapse everything.
	DisclosurePanel xHideControls = new DisclosurePanel(""); 

	// Disclosure panel to open and close all the left-hand controls
	DisclosurePanel xSettingsHeader = new DisclosurePanel("Settings");

	// The controls themselves
	AxesWidgetGroup xAxesWidget;
	ComparisonAxisSelector xComparisonAxesSelector;
	OperationsWidget xOperationsWidget = new OperationsWidget("Operations");

	// Analysis controls in the navigation panel
	AnalysisWidget xAnalysisWidget = new AnalysisWidget();
	CheckBox xApplyAnalysis = new CheckBox("Apply Analysis");

	// Controls for the panel sizes and to hide and show the headers.
	boolean xPanelHeaderHidden = false;
	int xPanelWidth;
	int xRightPad = 45;
	int xTopPad = 90;
	int xControlsWidth = 290;
	String xControlsWidthPx = xControlsWidth+"px";
	
	String xContainerType = Constants.FRAME;

	/*
	 * Control widget that sets the size of the image in the panel.
	 * If set to auto the panel resizes with the browser up to the size of the actual image.
	 * If set to 90% to 30% the image is set to a fixed size as a percentage of the actual image
	 * If set to 100% the image is fixed at full size.
	 * 
	 * Any size transformation to get to less than 100% is done by the browser.
	 */
	ListBox xImageSize = new ListBox();
	Label xImageSizeLabel = new Label("Image zoom: ");

	/*
	 * Button layout for the required button controls for any UI
	 */
	FlexTable xButtonLayout = new FlexTable();
	
	/*
	 * Make an HTML only popup page that can be printed.
	 */
	PushButton xPrinterFriendlyButton;
	

	/*
	 * (non-Javadoc)
	 * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
	 */

	@Override
	public void onModuleLoad() {

		xDSID = Util.getParameterString("dsid");
		xVarID = Util.getParameterString("vid");
		xOperationID = Util.getParameterString("opid");
		xOptionID = Util.getParameterString("optionid");
		xView = Util.getParameterString("view");

		xXlo = Util.getParameterString("xlo");
		xXhi = Util.getParameterString("xhi");
		xYlo = Util.getParameterString("ylo");
		xYhi = Util.getParameterString("yhi");
		xZlo = Util.getParameterString("zlo");
		xZhi = Util.getParameterString("zhi");
		xTlo = Util.getParameterString("tlo");
		xThi = Util.getParameterString("thi");

		xAxesWidget = new AxesWidgetGroup("Plot Axes", "Comparison Axes", "vertical", xControlsWidthPx, "ApplyTo_gallery");

		xComparisonAxesSelector = new ComparisonAxisSelector(xControlsWidthPx);

		xSettingsHeader.setOpen(true);		
		xSettingsHeader.addCloseHandler(new CloseHandler<DisclosurePanel> () {

			@Override
			public void onClose(CloseEvent<DisclosurePanel> event) {
				xOperationsWidget.setOpen(false);
				xComparisonAxesSelector.setOpen(false);
				xAxesWidget.setOpen(false);
			}

		});
		xSettingsHeader.addOpenHandler(new OpenHandler<DisclosurePanel>() {

			@Override
			public void onOpen(OpenEvent<DisclosurePanel> arg0) {
				xOperationsWidget.setOpen(true);
				xComparisonAxesSelector.setOpen(true);
				xAxesWidget.setOpen(true);
			}

		});
		xMainPanelCellFormatter = xMainPanel.getFlexCellFormatter();
		xMainPanelCellFormatter.setColSpan(0, 0, 2);
		xMainPanelCellFormatter.setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
		xMainPanelCellFormatter.setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);

		xNavigationControls.setWidget(0, 0, xSettingsHeader);		
		
		// This are bumped down one for the grow shrink experiment...
		xNavigationControls.setWidget(2, 0, xAxesWidget);
		xNavigationControls.setWidget(3, 0, xComparisonAxesSelector);
		xNavigationControls.setWidget(4, 0, xOperationsWidget);
		xNavigationControls.setWidget(5, 0, xApplyAnalysis);
		xNavigationControls.setWidget(6, 0, xAnalysisWidget);

		xHideControls.setOpen(true);
		xHideControls.addCloseHandler(new CloseHandler<DisclosurePanel>() {
			@Override
			public void onClose(CloseEvent<DisclosurePanel> arg0) {
				handlePanelShowHide();
			}
		});
		xHideControls.addOpenHandler(new OpenHandler<DisclosurePanel>() {
			@Override
			public void onOpen(OpenEvent<DisclosurePanel> arg0) {
				handlePanelShowHide();
			}
		});

		xDatasetButton.ensureDebugId("xDatasetButton");
		
		xDatasetSelectionHandler = new SelectionHandler<TreeItem>() {

			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				TreeItem item = (TreeItem) event.getSelectedItem();
				Object v = item.getUserObject();
				if ( v instanceof VariableSerializable ) {
					xNewVariable = (VariableSerializable) v;
					Window.alert("Your user selected "+xNewVariable.getName()+".  You must implement and register your own SelectionHandler.");
				}
			}

		};
		
		xRegisterDatasetSelectionHandler = xDatasetButton.addSelectionHandler(xDatasetSelectionHandler);
		// This is all to get around the fact that the OpenLayers map is always in front.
		xDatasetButton.addOpenClickHandler(xButtonOpenHandler);
		xDatasetButton.addCloseClickHandler(xButtonCloseHandler);
		xOptionsButton = new OptionsButton(0);
		xOptionsButton.addOpenClickHandler(xButtonOpenHandler);
		xOptionsButton.addCloseClickHandler(xButtonCloseHandler);
	    xOptionsButton.ensureDebugId("xOptionsButton");
	    
	    xOptionsClickHandler = new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Window.alert("Your user clicked OK on the options panel. You must implement and register your own handler.");
			}
	    	
	    };
	    
	    xRegisterOptionsHandler = xOptionsButton.addOkClickHandler(xOptionsClickHandler);
		
		xPrinterFriendlyButton = new PushButton("Print...");
		xPrinterFriendlyButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent print) {
				
				printerFriendly();
				
			}
	    	
	    });
	    
		xButtonLayout.setWidget(0, 0, xHideControls);
		xButtonLayout.setWidget(0, 1, xDatasetButton);
		xButtonLayout.setWidget(0, 2, xOptionsButton);
		xButtonLayout.setWidget(0, 3, xPrinterFriendlyButton);
		
		xMainPanel.setWidget(0, 0, xButtonLayout);
		xMainPanel.setWidget(1, 0, xNavigationControls);
		xMainPanel.setWidget(1, 1, xPanelTable);
		
		
		
		Window.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event) {
				if (xPanelCount > 1 ) {
					resize();
				} else {
					int pheight = (event.getHeight() - xTopPad);
					if (xPanels.get(0) != null ) {
						xPanels.get(0).setPanelHeight(pheight);
					}
				}
			}

		});

	}
	public void addMenuButtons(Widget buttons) {
		xButtonLayout.setWidget(0, 4, buttons);
	}
	ClickHandler xButtonOpenHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {
			
			xAxesWidget.closePanels();
			xOperationsPanelIsOpen = xOperationsWidget.isOpen();
			xOperationsWidget.setOpen(false);
			
		}
		
	};
	ClickHandler xButtonCloseHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {
			
			xAxesWidget.restorePanels();
			xOperationsWidget.setOpen(xOperationsPanelIsOpen);
		}
		
		
	};
	public void init(int numPanels, String container_type) {
		xContainerType = container_type;
		xPanelCount = numPanels;
		int col = 0;
		int row = 0;
		for(int i = 0; i < numPanels; i++ ) {
			String title = "Panel "+i;
			boolean compare_panel = false;
			if ( i == 0 ) {
				compare_panel = true;
			}

			boolean singlePanel = true;
			if ( numPanels > 1 ) {
				singlePanel = false;
			}

			OutputPanel panel = new OutputPanel(title, compare_panel, xOperationID, xOptionID, xView, singlePanel, xContainerType);

			xPanelTable.setWidget(row, col, panel);			
			xPanels.add(panel);

			// This will make a 2 column by n row display of the panels
			// If it's even
			if( i%2 == 0 ) {
				col++; // go to the next column
			}
			// If it's odd
			if ( i%2 != 0) {
				row++; // next row
				col--; // previous column
			}

			xPanelWidth = getPanelWidth(numPanels);
			panel.setPanelWidth(xPanelWidth);

		}
		if ( numPanels == 1 ) {
			xComparisonAxesSelector.setVisible(false);
			xAxesWidget.setOrthoTitle("Other Axes");
		} else {
			xComparisonAxesSelector.setVisible(true);
		}
		xImageSize.addItem("Auto", "auto");
		xImageSize.addItem("100%", "100");
		xImageSize.addItem(" 90%", "90");
		xImageSize.addItem(" 80%", "80");
		xImageSize.addItem(" 70%", "70");
		xImageSize.addItem(" 60%", "60");
		xImageSize.addItem(" 50%", "50");
		xImageSize.addItem(" 40%", "40");
		xImageSize.addItem(" 30%", "30");
	}
	public int getPanelWidth(int count) {
		int cols = count/2;
		if ( cols <= 0 ) {
			cols = 1;
		}
		int width;
		int win = Window.getClientWidth();
		if ( xPanelHeaderHidden ) {
			width = (win - xRightPad)/cols;
		} else {
			width = (win-(xRightPad+xControlsWidth))/cols;
		}
		if ( width <= 0 ) {
			width = 400;
		}
		return width;
	}
	public void setDatasetSelectionHandler(SelectionHandler<TreeItem> handler) {
		xRegisterDatasetSelectionHandler.removeHandler();
		xRegisterDatasetSelectionHandler = xDatasetButton.addSelectionHandler(handler);
	}
	public void setDatasetOpenHandler(OpenHandler<TreeItem> handler) {
		// There is no default open handler, so if it's null don't remove it.
		if ( xRegisterDatasetOpenHandler != null ) {
		    xRegisterDatasetOpenHandler.removeHandler();
		}
		xRegisterDatasetOpenHandler = xDatasetButton.addOpenHandler(handler);
	}
	public void setOptionsOkHandler(ClickHandler handler) {
		xRegisterOptionsHandler.removeHandler();
		xRegisterOptionsHandler = xOptionsButton.addOkClickHandler(handler);
	}
	public void setOperationsClickHandler(ClickHandler handler) {
		// This gets pushed on to every radio button in the operations widget.
		// Right now it seems to unwieldy to keep track of them all so I'll just check that I have one.
		xOperationsClickHandler = handler;
		xOperationsWidget.addClickHandler(handler);
	}
	public void addPanelApplyClickHandler(ClickHandler handler) {
		for (int i = 0; i < xPanelCount; i++ ) {
			xPanels.get(i).addApplyHandler(handler);
		}
		
	}
	public void handlePanelShowHide() {
		if ( xPanelHeaderHidden ) {
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				panel.show();
			}
			xMainPanelCellFormatter.setVisible(1, 0, true);
			resize();
		} else {
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				panel.hide();
			}
			xMainPanelCellFormatter.setVisible(1, 0, false);	
			resize();
		}
		xPanelHeaderHidden = !xPanelHeaderHidden;
		pushHistory();
	}
	public void resize() {
		xPanelWidth = getPanelWidth(xPanelCount);
		if ( xImageSize.getValue(xImageSize.getSelectedIndex()).equals("auto") ) {
			for (Iterator panelIt = xPanels.iterator(); panelIt.hasNext();) {
				OutputPanel panel = (OutputPanel) panelIt.next();
				panel.setPanelWidth(xPanelWidth);
			}
		}
	}
	private void pushHistory() {
		
	}
	private void printerFriendly() {

		final double image_h = 631.;
		final double image_w = 998.;
		final PopupPanel pop = new PopupPanel(false);
		final boolean panelHidden = xPanelHeaderHidden;
		final Image[] images = new Image[xPanels.size()];
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
		int rows = xPanels.size()/2;
		int cols = 2;
		int panel_index = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				String panel_url = ((OutputPanel)xPanels.get(panel_index)).getURL();
				final Image image = new Image(panel_url+"&stream=true&stream_ID=plot_image");
				images[panel_index] = image;
				plots.setWidget(i+1, j, image);
				panel_index++;
			}
		}
		pop.add(plots);
		pop.show();
	}
    private void setMapSelection(double s, double n, double w, double e) {
        xAxesWidget.getRefMap().setCurrentSelection(s, n, w, e);
    }
    private void setCurrentURL(String url) {
    	for (int i = 0; i < xPanelCount; i++ ) {
    		OutputPanel panel = xPanels.get(i);
    		if ( !panel.isUsePanelSettings() ) {
    		    xPanels.get(i).setURL(url);
    		}
    	}
    }
    public native void activateNativeHooks()/*-{
        var localObject = this;
        $wnd.updateMapSelection = function(slat, nlat, wlon, elon) {        
            localObject.@gov.noaa.pmel.tmap.las.client.TestUI::setMapSelection(DDDD)(slat, nlat, wlon, elon);
        }
        $wnd.updateCurrentURL = function(url) {
        	localObject.@gov.noaa.pmel.tmap.las.client.TestUI::setCurrentURL(Ljava/lang/String;)(url);
        }
    }-*/;
}
