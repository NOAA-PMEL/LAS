package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.BaseUI.Mouse;
import gov.noaa.pmel.tmap.las.client.map.MapSelectionChangeListener;
import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.AnalysisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.URLUtil;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;
/**
 * This is a GWT Composite object that knows how to initialize itself from a dsid, varid, operation and view, how to build menus for the orthogonal axes and how to refresh itself when those menus change.
 * @author rhs
 *
 */
public class OutputPanel extends Composite {
	/* An object that when set causes the request to contain an analysis request.  It should be null when no analysis is active.  */
	AnalysisSerializable analysis = null;
	
	String containerType = Constants.FRAME;
	
	/* Keep track of the URL that will give the "print" page... */
	String currentPrintURL = "";
	
	Frame output = new Frame();
	
	/* A message window when the plot cannot be made... */
	MessagePanel messagePanel;

	/* The base widget used to layout the panel.  A single column of three rows. */
	FlexTable grid;
	
	/* The top bar of widgets... */
	FlexTable top;

	/* A widget to return panel to the slide sorter control */
	PushButton revert = new PushButton("Revert");

	/* The current data set and variable.  */
	Label datasetLabel;

	/* This is the request.  It is either built from scratch or passed in via the constructor.  */
	LASRequest lasRequest = new LASRequest();

	// Keep track of the values that are currently being used as the fixed axis and compare axis
	String compareAxis;
	String fixedAxis;
	String fixedAxisValue;

	/*
	 * A a button that pops up a panel for region selection, operation and (still to be implemented) plot options.
	 */
	DatasetButton datasetButton;

	/*
	 * The copy of the axes for this panel.
	 */
	AxesWidgetGroup panelAxesWidgets;
     
	/* Keep track of the optionID, view and operation.  These are passed in as parameters when the pane is created. */
	String optionID;
	String operationID;
	
	// Keep track of the global values from the compare panel (upper left) so we can revert
	String prePanelModeState;
	VariableSerializable prePanelModeVariable;
	
	// Keep track of the current vizGal state when you are in panel mode.
	String vizGalState;       // These are the values for the axes that are in the view
	String comparePanelState; // These are the values for the axes that are in the compare panel (orthogonal to the view, but for the comparison variable only)
	VariableSerializable vizGalVariable;

	// Keep track of the axes that are currently orthogonal to the plot.
	List<String> ortho;
	String view;

	/* The current variable in this panel. */
	VariableSerializable var;

	// Some widgets to show when a panel is being refreshed.
	PopupPanel spin;
	Image spinImage;

	String ID;

	// Some information to control the size of the image as the browser window changes size.
	int pwidth;

	double image_h = 631.;
	double image_w = 998.;

	int fixedZoom;
	boolean autoZoom = true;;

	double min = 99999999.;
	double max = -99999999.;

	// Switch for "single panel mode".  If only one panel being used, do not display the "Revert" button.
	boolean singlePanel;

	// Switch for the first panel in a multi-panel gallery.  Controls all other panels.
	boolean comparePanel = false;

	// Keep track of the auto contour levels from the gallery.
	String fill_levels;

	// True if a new data set has been selected.  This allows the dataset change to be delayed until the "Apply" button is pressed.
	boolean changeDataset = false;

	// The new variable.
	VariableSerializable nvar;
	
	// The new grid.
	GridSerializable ngrid;
	
	// The height in pixels of the panel header
	String panelHeader = "75px";

	// The current Product URL being displayed in this frame.
	String currentURL = "";
	
	// The panel with the plot annotations hidden at the top of the frame...
	LASAnnotationsButtonPanel lasAnnotationsPanel = new LASAnnotationsButtonPanel();
	
	// Keep track of whether the retry is visible and remove it when the results come back.
	boolean retryShowing = false;
	
	// Keep track of the current operations set
	OperationSerializable[] ops;
	
	// Canvas to allow drawing on the plot for zooming.
	Context2d frontCanvasContext;
	Canvas frontCanvas;
	CssColor randomColor;
	
	// Drawing parameters from the map scale response
	protected int x_image_size;
	protected int y_image_size;
	protected int x_plot_size;
	protected int y_plot_size;
	protected int x_offset_from_left;
	protected int y_offset_from_bottom;
	protected int x_offset_from_right;
	protected int y_offset_from_top;
	protected double x_axis_lower_left;
	protected double y_axis_lower_left;
	protected double x_axis_upper_right;
	protected double y_axis_upper_right;
	protected double world_startx;
	protected double world_starty;
	protected double world_endx;
	protected double world_endy;
	protected double x_per_pixel;
	protected double y_per_pixel;
	
	// Drawing start position
	int startx = -1;
	int starty = -1;
	int endx;
	int endy;
	boolean draw = false;
	
	List<Mouse> mouseMoves = new ArrayList<Mouse>();
	
	Image plotImage = null;
	ImageData scaledImage; // The scaled image data.
	double imageScaleRatio; // Keep track of the factor by which the image has been scaled.
	
	// Keep track of the difference state in case we change data sets.
	boolean difference = false;
	VariableSerializable differenceFromVariable;
	
	/**
	 * Builds a VizGal panel with a default plot for the variable.  See {@code}VizGal(LASRequest) if you want more options on the initial plot.
	 */
	public OutputPanel(String id, boolean comparePanel, String op, String optionID, String view, boolean single, String container_type, String tile_server) {
		this.ID = id;
		this.comparePanel = comparePanel;
		this.singlePanel = single;
		this.operationID = op;
		this.optionID = optionID;
		this.view = view;
		this.containerType = container_type;
		panelAxesWidgets = new AxesWidgetGroup("Coordinates Orthogonal to the Plot", "horizontal", "", "Apply To "+ID, tile_server);
		spinImage = new Image(URLUtil.getImageURL()+"/mozilla_blu.gif");
		spinImage.setSize("18px", "18px");
		spin = new PopupPanel();
		
		
		
		spin.add(spinImage);
		spin.setSize("18px", "18px");

		messagePanel = new MessagePanel();
		

		grid = new FlexTable();
		//grid.getCellFormatter().setHeight(0, 0, panelHeader);
		datasetLabel = new Label();

		top = new FlexTable();

		String title = "Settings";
		datasetButton = new DatasetButton();
		datasetButton.addSelectionHandler(datasetSelctionHandler);
		datasetButton.addOpenClickHandler(datasetOpenHandler);
		datasetButton.addCloseClickHandler(datasetCloseHandler);
		revert.addStyleDependentName("SMALLER");
		revert.addClickHandler(revertHandler);
		revert.setTitle("Cancel Panel Settings for "+ID);

		
		if ( comparePanel ) {
			Label gs = new Label("   ");
			gs.setHeight("25px");
			top.setWidget(0, 0, gs);
		} else {
			top.setWidget(0, 0, datasetButton);
		} 
		top.setWidget(0, 1, lasAnnotationsPanel);		
		
		grid.setWidget(0, 0, top);
		HTML plot = new HTML();
		//plot.setHTML(spinImage.getHTML());
		plot.setHTML("<br>");
		grid.setWidget(1, 0, plot);
		initWidget(grid);	
	}
	public void init(boolean usePanel, OperationSerializable[] ops) {
		this.ops = ops;
		min =  999999999.;
		max = -999999999.;
	    datasetLabel.setText(var.getDSName()+": "+var.getName());
		GridSerializable ds_grid = var.getGrid();
		ngrid = ds_grid;
		ortho = Util.setOrthoAxes(view, ds_grid);
		double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
		double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

		double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
		double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

		double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));
		panelAxesWidgets.getRefMap().setDataExtent(grid_south, grid_north, grid_west, grid_east, delta);
		
		if ( ds_grid.getTAxis() != null ) {
			compareAxis = "t";
			if ( ds_grid.getZAxis() != null ) {
				fixedAxis = "z";
			}
		} else if ( ds_grid.getZAxis() != null ) {
			compareAxis = "z";
			fixedAxis = "none";
		}
		if ( ds_grid.getTAxis() != null ) {
			panelAxesWidgets.getTAxis().init(ds_grid.getTAxis(), false);
			
		}
		if ( ds_grid.getZAxis() != null ) {
			panelAxesWidgets.getZAxis().init(ds_grid.getZAxis());
		}
		
		if ( !singlePanel ) {
			// In singlePanel, the controls will be on the left navbar.
			grid.setWidget(3, 0, panelAxesWidgets);
		} 
		frontCanvas = Canvas.createIfSupported();
    	frontCanvasContext = frontCanvas.getContext2d();

    	int rndRedColor = 244;
    	int rndGreenColor = 154;
    	int rndBlueColor = 0;
    	double rndAlpha = .45;

    	randomColor = CssColor.make("rgba(" + rndRedColor + ", " + rndGreenColor + "," + rndBlueColor + ", " + rndAlpha + ")");
    	
    	setImageWidth();
    	grid.setWidget(2, 0, plotImage);
        	
	}
	public void setMapTool(String view) {
		panelAxesWidgets.getRefMap().setTool(view);
	}
	/**
	 * Send a request to the LAS server to create a new plot, you must have already pushed the operation and axes values from the main left-hand
	 * controls to the panel and you can pass in the current "global" plot options.
	 * @param options
	 * @param switchAxis
	 * @param popup
	 */
	public void refreshPlot(Map<String, String> options, boolean switchAxis, boolean popup) {
		
		// When called from vizGal this means the button is off...
		
		difference = false;
		
		messagePanel.hide();
		
        lasRequest = new LASRequest();
		
		if ( var.isVector() ) {
			// Add the first component
			lasRequest.addVariable(var.getDSID(), var.getComponents().get(0), 0);
			lasRequest.setProperty("ferret", "vector_name", var.getName());
		} else {
				lasRequest.addVariable(var.getDSID(), var.getID(), 0);
		}

		lasRequest.setOperation(operationID, "v7");
		
		Map<String, String> vgState = Util.getTokenMap(vizGalState);
		
		// If the axis is in the view, the state comes from the global vizGal state which are store in the member variables.
		// Otherwise it gets its axis value from the local widget.
		String local_xlo = null;
		String local_xhi = null;
		String local_ylo = null;
		String local_yhi = null;
		String local_zlo = null;
		String local_zhi = null;
		String local_tlo = null;
		String local_thi = null;
		
		if ( view.contains("x") ) {
			local_xlo = vgState.get("xlo");
			local_xhi = vgState.get("xhi");
		} else {
			local_xlo = String.valueOf(panelAxesWidgets.getRefMap().getXlo());
			local_xhi = String.valueOf(panelAxesWidgets.getRefMap().getXhi());
		}
		
		if ( view.contains("y") ) {
			local_ylo = vgState.get("ylo");
			local_yhi = vgState.get("yhi");
		} else {
			local_ylo = String.valueOf(panelAxesWidgets.getRefMap().getYlo());
			local_yhi = String.valueOf(panelAxesWidgets.getRefMap().getYhi());
		}
        
		if ( view.contains("z") ) {
			local_zlo = vgState.get("zlo");
			local_zhi = vgState.get("zhi");
		} else {
			if ( var.getGrid().hasZ() ) {
				local_zlo = panelAxesWidgets.getZAxis().getLo();
				local_zhi = panelAxesWidgets.getZAxis().getHi();
			}
		}
		
		if ( view.contains("t") ) {
			local_tlo = vgState.get("tlo");
			local_thi = vgState.get("thi");
		} else {
			if ( var.getGrid().hasT() ) {
				local_tlo = panelAxesWidgets.getTAxis().getFerretDateLo();
				local_thi = panelAxesWidgets.getTAxis().getFerretDateHi();
			}
		}

		if ( analysis != null ) {
			
			if ( !analysis.isActive("x") ) {
				lasRequest.setRange("x", local_xlo, local_xhi, 0);
			}

			if (!analysis.isActive("y") ) {
				lasRequest.setRange("y", local_ylo, local_yhi, 0);
			}

			if ( var.getGrid().getZAxis() != null ) {
				if ( !analysis.isActive("z") ) {
					lasRequest.setRange("z", local_zlo, local_zhi, 0);
				}
			}
			if ( var.getGrid().getTAxis() != null ) {
				if ( !analysis.isActive("t") ) {
				    lasRequest.setRange("t", local_tlo, local_thi, 0);
				}
			}
		} else {
			lasRequest.setRange("x", local_xlo, local_xhi, 0);
			lasRequest.setRange("y", local_ylo, local_yhi, 0);
			if ( var.getGrid().hasZ() ) {
				lasRequest.setRange("z", local_zlo, local_zhi, 0);
			} 
			if ( var.getGrid().hasT() ) {
				lasRequest.setRange("t", local_tlo, local_thi, 0);
			}
		}
		
		if ( var.isVector() ) {
			// Add the second component...
			lasRequest.addVariable(var.getDSID(), var.getComponents().get(1), 1);
		
			lasRequest.setRange("x", local_xlo, local_xhi, 1);
			lasRequest.setRange("y", local_ylo, local_yhi, 1);

			if ( var.getGrid().getZAxis() != null ) {
				lasRequest.setRange("z", local_zlo, local_zhi, 1);
			}
			
			if ( var.getGrid().getTAxis() != null ) {
				lasRequest.setRange("t", local_tlo, local_thi, 1);
			}

		}
		if ( analysis != null ) {
			
		  lasRequest.setAnalysis(analysis, 0);
			
		}
		lasRequest.setProperty("ferret", "view", view);
        
		lasRequest.setProperty("ferret", "size", ".8333");
		lasRequest.setProperty("ferret", "image_format", "gif");
		lasRequest.setProperty("ferret", "annotations", "file");
		if ( containerType.equals(Constants.IMAGE) ) {
			lasRequest.setProperty("las", "output_type", "xml");
		}

		if ( options != null ) {
			for (Iterator opIt = options.keySet().iterator(); opIt.hasNext();) {
				String key = (String) opIt.next();
				String value = options.get(key);
				if ( !value.toLowerCase().equals("default") && !value.equals("") ) {
					lasRequest.setProperty("ferret", key, value);
				}
			}
		}			
		
		// Now force in the auto contour settings if it exists.
		if ( fill_levels != null && !fill_levels.equals("") && !fill_levels.equals(Constants.NO_MIN_MAX) ) {
			lasRequest.setProperty("ferret", "fill_levels", fill_levels);
		}
		lasRequest.setProperty("product_server", "ui_timeout", "10");	
        if ( var.getGrid().hasT() ) {		
        	if ( view.contains("t") && local_tlo.equals(local_thi) ) {
        		messagePanel.show(grid.getWidget(1, 0).getAbsoluteLeft()+15, grid.getWidget(1,0).getAbsoluteTop()+15, "Set plot range selectors to different values and click the Apply button.");
        		return;
        	}
        	
		}
        if ( var.getGrid().hasZ() ) {
        	if ( view.contains("z") && local_zlo.equals(local_zhi) ) {
        		messagePanel.show(grid.getWidget(1, 0).getAbsoluteLeft()+15, grid.getWidget(1,0).getAbsoluteTop()+15, "Set plot range selectors to different values and click the Apply button.");
        		return;
        	}
        }
        
        if ( lasRequest == null ) {
        	return;
        }
        
		String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.toString());
		
		if ( !url.equals(currentURL) ) {
			currentURL = url;
			if ( containerType.equals(Constants.IMAGE) ) {
				if (popup) {
					spin.setPopupPosition(grid.getWidget(1,0).getAbsoluteLeft(), grid.getWidget(1,0).getAbsoluteTop());
					spin.show();
				}

				RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
				try {
					lasAnnotationsPanel.setTitle("Plot Annotations");
					lasAnnotationsPanel.setError("Fetching plot annotations...");
					sendRequest.sendRequest(null, lasRequestCallback);
				} catch (RequestException e) {
					spin.hide();
					HTML error = new HTML(e.toString());
					grid.setWidget(1, 0, error);
				}
			} else {
				grid.setWidget(1, 0, spinImage);
				output.setUrl(url);
				grid.setWidget(1, 0, output);
			}
		}
	}
	public void computeDifference(Map<String, String> options, boolean switchAxis) {

		// When called from vizGal the button is down
		difference = true;

		spin.hide();

		lasRequest = new LASRequest();

		if ( vizGalVariable.isVector() ) {
			lasRequest.setOperation("Compare_Vectors", "v7");
		} else {
			lasRequest.setOperation("Compare_Plot", "v7");
		}
		lasRequest.setProperty("ferret", "view", view);
		lasRequest.setProperty("ferret", "size", ".8333");
		lasRequest.setProperty("ferret", "annotations", "file");
		// Add the variable in the upper left panel
		if ( vizGalVariable.isVector() ) {
			// Add the first component
			lasRequest.addVariable(vizGalVariable.getDSID(), vizGalVariable.getComponents().get(0), 0);
		} else {
			lasRequest.addVariable(vizGalVariable.getDSID(), vizGalVariable.getID(), 0);
		}


		Map<String, String> vgState = Util.getTokenMap(vizGalState);
		Map<String, String> cpState = Util.getTokenMap(comparePanelState);

		// Set the region of the first variable according to the global state or the compare panel state...


		if ( view.contains("x") ) {
			lasRequest.setRange("x", vgState.get("xlo"), vgState.get("xhi"), 0);
		} else {
			lasRequest.setRange("x", cpState.get("xlo"), cpState.get("xhi"), 0);
		}

		if ( view.contains("y") ) {
			lasRequest.setRange("y", vgState.get("ylo"), vgState.get("yhi"), 0);
		} else {
			lasRequest.setRange("y", cpState.get("ylo"), cpState.get("yhi"), 0);
		}
		if ( vizGalVariable.getGrid().hasZ() ) {
			if ( view.contains("z") ) {
				lasRequest.setRange("z", vgState.get("zlo"), vgState.get("zhi"), 0);
			} else {
				lasRequest.setRange("z", cpState.get("zlo"), cpState.get("zhi"), 0);
			}
		}
		if ( vizGalVariable.getGrid().hasT() ) {
			if ( view.contains("t") ) {
				lasRequest.setRange("t", vgState.get("tlo"), vgState.get("thi"), 0);
			} else {
				lasRequest.setRange("t", cpState.get("tlo"), cpState.get("thi"), 0);
			}
		}

		if ( vizGalVariable.isVector() ) {
			// Add the second component and its region to match the first component.
			lasRequest.setProperty("ferret", "vector_name", vizGalVariable.getName());
			lasRequest.addVariable(vizGalVariable.getDSID(), vizGalVariable.getComponents().get(1), 1);
			if ( view.contains("x") ) {
				lasRequest.setRange("x", vgState.get("xlo"), vgState.get("xhi"), 1);
			} else {
				lasRequest.setRange("x", cpState.get("xlo"), cpState.get("xhi"), 1);
			}

			if ( view.contains("y") ) {
				lasRequest.setRange("y", vgState.get("ylo"), vgState.get("yhi"), 1);
			} else {
				lasRequest.setRange("y", cpState.get("ylo"), cpState.get("yhi"), 1);
			}
			if ( var.getGrid().hasZ() ) {
				if ( view.contains("z") ) {
					lasRequest.setRange("z", vgState.get("zlo"), vgState.get("zhi"), 1);
				} else {
					lasRequest.setRange("z", cpState.get("zlo"), cpState.get("zhi"), 1);
				}
			}
			if ( var.getGrid().hasT() ) {
				if ( view.contains("t") ) {
					lasRequest.setRange("t", vgState.get("tlo"), vgState.get("thi"), 1);
				} else {
					lasRequest.setRange("t", cpState.get("tlo"), cpState.get("thi"), 1);
				}
			}
		} else {

			// The local variable only set what is unique to this variable.  I.e. what's not in the view.

			lasRequest.addVariable(var.getDSID(), var.getID(), 1);

			if ( !view.contains("x") ) {
				if ( var.getGrid().hasX() ) {
					lasRequest.setRange("x", String.valueOf(panelAxesWidgets.getRefMap().getXlo()), String.valueOf(panelAxesWidgets.getRefMap().getXhi()), 1);
				}
			}
			if ( !view.contains("y") ) {
				if ( var.getGrid().hasY() ) {
					lasRequest.setRange("y", String.valueOf(panelAxesWidgets.getRefMap().getYlo()), String.valueOf(panelAxesWidgets.getRefMap().getYhi()), 1);
				}
			}
			if ( !view.contains("z") ) {
				if ( var.getGrid().hasZ() ) {
					lasRequest.setRange("z", panelAxesWidgets.getZAxis().getLo(), panelAxesWidgets.getZAxis().getHi(), 1);
				}
			}
			if ( !view.contains("t") ) {
				if ( var.getGrid().hasT() ) {
					lasRequest.setRange("t", panelAxesWidgets.getTAxis().getFerretDateLo(), panelAxesWidgets.getTAxis().getFerretDateHi(), 1);
				}
			}
		}

		// If the passed in variable is a vector, then the panel variable must also be a vector.  Right?
		if ( vizGalVariable.isVector() && !var.isVector() ) {
			messagePanel.show(grid.getWidget(1, 0).getAbsoluteLeft()+15, grid.getWidget(1,0).getAbsoluteTop()+15, "Could not make plot.  Variable in panel must also be a vector.");
			return;
		} else if ( vizGalVariable.isVector() && var.isVector() ) {
			lasRequest.addVariable(var.getDSID(), var.getComponents().get(0), 0);
			if ( var.getGrid().hasX() ) {
				if ( !view.contains("x") ) {
					lasRequest.setRange("x", String.valueOf(panelAxesWidgets.getRefMap().getXlo()), String.valueOf(panelAxesWidgets.getRefMap().getXhi()), 2);
				}
			}

			if ( var.getGrid().hasY() ) {
				if ( !view.contains("y") ) {
					lasRequest.setRange("y", String.valueOf(panelAxesWidgets.getRefMap().getYlo()), String.valueOf(panelAxesWidgets.getRefMap().getYhi()), 2);
				}
			}
			if ( var.getGrid().hasZ() ) {
				if ( !view.contains("z") ) {	
					lasRequest.setRange("z", panelAxesWidgets.getZAxis().getLo(), panelAxesWidgets.getZAxis().getHi(), 2);
				}				
			}
			if ( var.getGrid().hasT() ) {
				lasRequest.setRange("t", panelAxesWidgets.getTAxis().getFerretDateLo(), panelAxesWidgets.getTAxis().getFerretDateHi(), 2);
			}	


			//  And the other variable component.  Same as above...
			lasRequest.addVariable(var.getDSID(), var.getComponents().get(1), 1);
			if ( var.getGrid().hasX() ) {
				if ( !view.contains("x") ) {
					lasRequest.setRange("x", String.valueOf(panelAxesWidgets.getRefMap().getXlo()), String.valueOf(panelAxesWidgets.getRefMap().getXhi()), 3);
				}
			}

			if ( var.getGrid().hasY() ) {
				if ( !view.contains("y") ) {
					lasRequest.setRange("y", String.valueOf(panelAxesWidgets.getRefMap().getYlo()), String.valueOf(panelAxesWidgets.getRefMap().getYhi()), 3);
				}
			}
			if ( var.getGrid().hasZ() ) {
				if ( !view.contains("z") ) {	
					lasRequest.setRange("z", panelAxesWidgets.getZAxis().getLo(), panelAxesWidgets.getZAxis().getHi(), 3);
				}				
			}
			if ( var.getGrid().hasT() ) {
				if ( !view.contains("t") ) {
					lasRequest.setRange("t", panelAxesWidgets.getTAxis().getFerretDateLo(), panelAxesWidgets.getTAxis().getFerretDateHi(), 3);
				}	
			}	
		}
		lasRequest.setProperty("ferret", "image_format", "gif");
		if ( containerType.equals(Constants.IMAGE) ) {
			lasRequest.setProperty("las", "output_type", "xml");
		}

		// Use the global options.
		if ( options != null ) {
			for (Iterator opIt = options.keySet().iterator(); opIt.hasNext();) {
				String key = (String) opIt.next();
				String value = options.get(key);
				if ( !value.toLowerCase().equals("default") && !value.equals("") ) {
					lasRequest.setProperty("ferret", key, value);
				}
			}
		}

		lasRequest.setProperty("product_server", "ui_timeout", "20");
		String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.toString());

		if ( !url.equals(currentURL) ) {
			currentURL = url;
			spin.show();
			if ( containerType.equals(Constants.IMAGE) ) {

				RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
				try {
					sendRequest.sendRequest(null, lasRequestCallback);
				} catch (RequestException e) {
					spin.hide();
					HTML error = new HTML(e.toString());
					grid.setWidget(1, 0, error);

				}
			} else {
				output.setUrl(url);
				grid.setWidget(1, 0, output);
			}
		}
	}

	public boolean isComparePanel() {
		return comparePanel;
	}
	private RequestCallback mapScaleCallBack = new RequestCallback() {

		public void onError(Request request, Throwable exception) {
			// Don't care.  Just use information we have.
		}

		public void onResponseReceived(Request request, Response response) {
			// Get the image scale information.
			String doc = response.getText();
			if ( !doc.contains("<?xml") ) {
				// Don't care.  Just don't crash.
			} else {
				doc = doc.replaceAll("\n", "");
				Document responseXML = XMLParser.parse(doc);
				String w = getElementValue("x_image_size", responseXML);
				if ( w != null ) {
					image_w = Double.valueOf(w);
				}

				String h = getElementValue("y_image_size", responseXML);
				if ( h != null ) {
					image_h = Double.valueOf(h);
				}

				setImageWidth();

				
				// Also set the min and max for the data in this panel.
				String mn = getElementValue("data_min", responseXML);
				if ( mn != null ) {
					min = Double.valueOf(mn);
				}

				String mx = getElementValue("data_max", responseXML);
				if ( mx != null ) {
					max = Double.valueOf(mx);
				}
                
			}	
		}		
	};
	private String getElementValue(String element, Document doc) {
		NodeList results_y = doc.getElementsByTagName(element);
		String value = null;
		for(int n=0; n<results_y.getLength();n++) {
			if ( results_y.item(n) instanceof Element ) {
				Element result = (Element) results_y.item(n);
				Node text = result.getFirstChild();
				if ( text instanceof Text ) {
					Text t = (Text) text;
					value = t.getData();
				}
			}
		}
		return value;
	}
	private RequestCallback lasRequestCallback = new RequestCallback() {
		public void onError(Request request, Throwable exception) {
			spin.hide();
			HTML error = new HTML(exception.toString());
			Widget size = grid.getWidget(1, 0);
			error.setSize(image_w*imageScaleRatio+"px", image_h*imageScaleRatio+"px");
			grid.setWidget(1, 0, error);
		}

		public void onResponseReceived(Request request, Response response) {
			String doc = response.getText();
			if ( retryShowing ) {
				grid.removeCell(2, 1);
				retryShowing = false;
			}
			// Look at the doc.  If it's not obviously XML, treat it as HTML.
			if ( !doc.substring(0, 100).contains("<?xml") ) {
				HTML result = new HTML(doc, true);
				result.setSize(image_w+"px", image_h+"px");
				grid.setWidget(1, 0, result);
			} else {
				doc = doc.replaceAll("\n", "").trim();
				Document responseXML = XMLParser.parse(doc);
				NodeList results = responseXML.getElementsByTagName("result");
				String annourl = "";
				String imageurl = "";
				boolean image_ready = false;
				for(int n=0; n<results.getLength();n++) {
					if ( results.item(n) instanceof Element ) {
						Element result = (Element) results.item(n);
						if ( result.getAttribute("type").equals("image") ) {
							//HTML image = new HTML("<a target=\"_blank\" href=\""+result.getAttribute("url")+"\"><img width=\"100%\" src=\""+result.getAttribute("url")+"\"></a>");
							image_ready = true;
							imageurl = result.getAttribute("url");
						} else if ( result.getAttribute("type").equals("annotations") ) {
							annourl = result.getAttribute("url");
							lasAnnotationsPanel.setAnnotationsHTMLURL(Util.getAnnotationService(annourl)+"&catid="+var.getDSID());
						} else if ( result.getAttribute("type").equals("map_scale") ) {
							NodeList map_scale = result.getElementsByTagName("map_scale");
							for ( int m = 0; m < map_scale.getLength(); m++ ) {
								if ( map_scale.item(m) instanceof Element ) {
									Element map = (Element) map_scale.item(m);
									NodeList children = map.getChildNodes();
									for ( int l = 0; l < children.getLength(); l++ ) {
										if ( children.item(l) instanceof Element ) {
											Element child = (Element) children.item(l);
											if ( child.getNodeName().equals("x_image_size") ) {
												x_image_size = getNumber(child.getFirstChild());
											} else if ( child.getNodeName().equals("y_image_size") ) {
												y_image_size = getNumber(child.getFirstChild());
											} else if ( child.getNodeName().equals("x_plot_size") ) {
												x_plot_size = getNumber(child.getFirstChild());
											} else if ( child.getNodeName().equals("y_plot_size") ) {
												y_plot_size = getNumber(child.getFirstChild());
											} else if ( child.getNodeName().equals("x_offset_from_left") ) {
												x_offset_from_left = getNumber(child.getFirstChild());
											} else if ( child.getNodeName().equals("y_offset_from_bottom") ) {
												y_offset_from_bottom = getNumber(child.getFirstChild());
											} else if ( child.getNodeName().equals("x_offset_from_right") ) {
												x_offset_from_right = getNumber(child.getFirstChild());
											} else if ( child.getNodeName().equals("y_offset_from_top") ) {
												y_offset_from_top = getNumber(child.getFirstChild());
											} else if ( child.getNodeName().equals("x_axis_lower_left") ) {
												x_axis_lower_left = getDouble(child.getFirstChild());
											} else if ( child.getNodeName().equals("y_axis_lower_left") ) {
												y_axis_lower_left = getDouble(child.getFirstChild());
											} else if ( child.getNodeName().equals("x_axis_upper_right") ) {
												x_axis_upper_right = getDouble(child.getFirstChild());
											} else if ( child.getNodeName().equals("y_axis_upper_right") ) {
												y_axis_upper_right = getDouble(child.getFirstChild());
											} else if ( child.getNodeName().equals("data_min") ) {
												min = getDouble(child.getFirstChild());
											} else if ( child.getNodeName().equals("data_max") ) {
												max = getDouble(child.getFirstChild());
											}
										}
									}
								}
							}
						} else if ( result.getAttribute("type").equals("error") ) {
							if ( result.getAttribute("ID").equals("las_message") ) {
								Node text = result.getFirstChild();
								if ( text instanceof Text ) {
									spin.hide();
									Text t = (Text) text;
									HTML error = new HTML(t.getData().toString().trim());
									error.setSize(image_w*imageScaleRatio+"px", image_h*imageScaleRatio+"px");
									grid.setWidget(1, 0, error);
//									retryShowing = true;
//									PushButton retry = new PushButton("Retry");
//									retry.addStyleDependentName("SMALLER");
//									retry.addClickHandler(new ClickHandler() {
//										
//										@Override
//										public void onClick(ClickEvent event) {
//											// Just send the same request again to see if it works the second time.
//											String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.toString());
//											RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
//											try {
//												sendRequest.sendRequest(null, lasRequestCallback);
//											} catch (RequestException e) {
//												HTML error = new HTML(e.toString());
//												error.setSize(image_w*imageScaleRatio+"px", image_h*imageScaleRatio+"px");
//												grid.setWidget(1, 0, error);
//											}
//										}
//
//									});
//									grid.setWidget(2, 1, retry);
								}
							}
							image_w = x_image_size;
							image_h = y_image_size;
						} else if ( result.getAttribute("type").equals("batch") ) {
							String elapsed_time = result.getAttribute("elapsed_time");
							HTML batch = new HTML("<br><br>Your request has been processing for "+elapsed_time+" seconds.<br>This panel will refresh automatically.<br><br>");
							batch.setSize(image_w*imageScaleRatio+"px", image_h*imageScaleRatio+"px");
							grid.setWidget(1, 0, batch);
							lasRequest.setProperty("product_server", "ui_timeout", "3");
							String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.toString());
							RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
							try {
								sendRequest.sendRequest(null, lasRequestCallback);
							} catch (RequestException e) {
								HTML error = new HTML(e.toString());
								error.setSize(image_w*imageScaleRatio+"px", image_h*imageScaleRatio+"px");
								grid.setWidget(1, 0, error);
							}
						}
					}
				}
				if ( image_ready ) {
					
				    
				    // set the canvas with the image and get to drawin'
				    
				    if ( !imageurl.equals("") ) {
						plotImage = new Image(imageurl);
						x_per_pixel = (x_axis_upper_right - x_axis_lower_left)/Double.valueOf(x_plot_size);
						y_per_pixel = (y_axis_upper_right - y_axis_lower_left)/Double.valueOf(y_plot_size);
	                    
						if ( frontCanvas != null ) {
							grid.setWidget(2, 0 , plotImage);
							plotImage.setVisible(false);
							plotImage.addLoadHandler(imageLoadHandler);
							frontCanvas.addMouseUpHandler(new MouseUpHandler() {
								
								@Override
								public void onMouseUp(MouseUpEvent event) {
									// If we're still drawing when the mouse goes up, record the position.
									if ( draw ) {
										endx = event.getX();
										endy = event.getY();
									}
									draw = false;
									for (Iterator mouseIt = mouseMoves.iterator(); mouseIt.hasNext();) {
										Mouse mouse = (Mouse) mouseIt.next();
										mouse.applyNeeded();
									}
								}
							});
						} else {
							// Browser cannot handle a canvas tag, so just put up the image.
							// The old stuff if there is no canvas...
						    currentPrintURL = Util.getAnnotationsFrag(annourl, imageurl);
						    setImage(imageurl, Util.getAnnotationsService(annourl, imageurl));
						    setImageWidth();
						    spin.hide();
							
							
						}
					}
					world_startx = x_axis_lower_left;
	                world_endx = x_axis_upper_right;
	                world_starty = y_axis_lower_left;
	                world_endy = y_axis_upper_right;
					spin.hide();
				}
			}
		}
	};
	
	SelectionHandler<TreeItem> datasetSelctionHandler = new SelectionHandler<TreeItem>() {

		@Override
		public void onSelection(SelectionEvent<TreeItem> event) {
			TreeItem item = (TreeItem) event.getSelectedItem();
			Object v = item.getUserObject();
			if ( v instanceof VariableSerializable ) {
				nvar = (VariableSerializable) v;
				ngrid = null;
				changeDataset = true;
				// Get all operations by using a null view.
				Util.getRPCService().getConfig(null, nvar.getDSID(), nvar.getID(), configCallback);
			}
		}
		
	};
	
	public AsyncCallback<ConfigSerializable> configCallback = new AsyncCallback<ConfigSerializable>() {

		@Override
		public void onFailure(Throwable caught) {
			Window.alert("Could not fetch grid for new variable.");
		}

		@Override
		public void onSuccess(ConfigSerializable config) {
			ngrid = config.getGrid();
			nvar.setGrid(ngrid);
			ops = config.getOperations();
			ortho = Util.setOrthoAxes(view, ngrid);
			applyChanges();
		}
		
	};
	public void setVariable(VariableSerializable v) {
		var = v;
		datasetLabel.setText(var.getDSName()+": "+var.getName());
		panelAxesWidgets.init(var.getGrid());
	}
	public void showOrthoAxes(String view, List<String> ortho) {
		panelAxesWidgets.showOrthoAxes(view, ortho);
	}
	private void applyChanges() {

		if ( ngrid == null ) {
			Window.alert("Still fetching grid for new variable.");
			return;
		}


		if (changeDataset) {
			setVariable(nvar);			
			changeDataset = false;
			init(true, ops);
			if ( var.isVector() ) {
				setOperation(Constants.DEFAULT_VECTOR_OP, "xy");
				panelAxesWidgets.getRefMap().setTool("xy");
			} else {
				setOperation(Constants.DEFAULT_SCALER_OP, "xy");
				panelAxesWidgets.getRefMap().setTool("xy");
			}
		}



		panelAxesWidgets.showOrthoAxes(view, ortho);
		// When called locally, decide which to use base of value set from previous calls to refreshPlot or computeDifference.
		// TODO make this into one method.  :-)
		if ( difference ) {
			computeDifference(null, false);
		} else {
			refreshPlot(null, false, true);

		}
	}
	
	private void setCompareAxisEnabled(boolean b) {
		Widget w = grid.getWidget(2, 0);
		if ( w instanceof DateTimeWidget ) {
			DateTimeWidget dt = (DateTimeWidget) w;
			dt.setEnabled(b);
		} else if ( w instanceof AxisWidget ) {
			AxisWidget a = (AxisWidget) w;
			a.setEnabled(b);
		}
	}
	public String getID() {
		return ID;
	}
	public String getThi() {
		return panelAxesWidgets.getTAxis().getFerretDateHi();
	}
	public String getTlo() {
		return panelAxesWidgets.getTAxis().getFerretDateLo();
	}

	public String getZhi() {
		return panelAxesWidgets.getZAxis().getHi();
	}
	public String getZlo() {
		return panelAxesWidgets.getZAxis().getLo();
	}

	public String getYhiFormatted() {
		return panelAxesWidgets.getRefMap().getYhiFormatted();
	}
	public String getYloFormatted() {
		return panelAxesWidgets.getRefMap().getYloFormatted();
	}
	public String getXhiFormatted() {
		return panelAxesWidgets.getRefMap().getXhiFormatted();
	}
	public String getXloFormatted() {
		return panelAxesWidgets.getRefMap().getXloFormatted();
	}

	public double getYhi() {
		return panelAxesWidgets.getRefMap().getYhi();
	}
	public double getYlo() {
		return panelAxesWidgets.getRefMap().getYlo();
	}
	public double getXhi() {
		return panelAxesWidgets.getRefMap().getXhi();
	}
	public double getXlo() {
		return panelAxesWidgets.getRefMap().getXlo();
	}

	public void setT(String tlo, String thi) {
		panelAxesWidgets.getTAxis().setLo(tlo);
		// TODO also set the hi value
	}

	public void setZ(String zlo, String zhi) {
		panelAxesWidgets.getZAxis().setLo(zlo);
		// TODO also set hi if it's a range
	}
	public void setDataExtent(double[] data) {
		panelAxesWidgets.getRefMap().setDataExtent(data[0], data[1], data[2], data[3]);
	}
	public void setLatLon(double[] selection) {
		panelAxesWidgets.getRefMap().setCurrentSelection(selection[0], selection[1], selection[2], selection[3]);
	}
    public void setDataExtent(String xlo, String xhi, String ylo, String yhi) {
    	panelAxesWidgets.getRefMap().setDataExtent(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
    }
    
	public void setLatLon( String ylo, String yhi, String xlo, String xhi ) {
		panelAxesWidgets.getRefMap().setCurrentSelection(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
	}
	public void setLat(String ylo, String yhi) {
		panelAxesWidgets.getRefMap().setCurrentSelection(Double.valueOf(ylo), Double.valueOf(yhi), panelAxesWidgets.getRefMap().getXlo(), panelAxesWidgets.getRefMap().getXhi());
	}
	public void setLon(String xlo, String xhi) {
		panelAxesWidgets.getRefMap().setCurrentSelection(panelAxesWidgets.getRefMap().getYlo(), panelAxesWidgets.getRefMap().getYhi(), Double.valueOf(xlo), Double.valueOf(xhi));
	}
	public void setImageSize(int percent) {
		fixedZoom = percent;
		double factor = percent/100.;
		Widget p = grid.getWidget(1, 0);
		int w = (int)(Double.valueOf(image_w).doubleValue()*factor);
		int h = (int)(Double.valueOf(image_h).doubleValue()*factor);
		p.setWidth(w+"px");
		p.setHeight(h+"px");
		autoZoom = false;
	}
	public void setImageWidth() {
		Widget w = grid.getWidget(1, 0);
		// Piggy back setting the annotations width onto this method.
		if ( autoZoom ) {
			imageScaleRatio = 1.;
			if ( pwidth < image_w ) {
				// If the panel is less than the image, shrink the image.
				int h = (int) ((image_h/image_w)*Double.valueOf(pwidth));
				imageScaleRatio = h/image_h;
			}
			if (plotImage != null ) scale(plotImage, imageScaleRatio);
		} else {
			setImageSize(fixedZoom);
		}
		if ( spin.isVisible() ) {
			spin.setPopupPosition(w.getAbsoluteLeft(), w.getAbsoluteTop());
		}
		
	}
	public void setPanelHeight(int height) {
		autoZoom = true;
		pwidth = (int) ((image_w/image_h)*Double.valueOf(height));
		setImageWidth();
	}
	public void setPanelWidth(int width) {
		autoZoom = true;
		int max = (int) image_w;
		pwidth = Math.min(width,  max);
		setImageWidth();	
	}

	public void setAxisRangeValues(String axis, String lo_value, String hi_value) {
		if ( axis.equals("z") ) {
			panelAxesWidgets.getZAxis().setLo(lo_value);
			panelAxesWidgets.getZAxis().setHi(hi_value);
		} else if ( axis.equals("t") ) {
			panelAxesWidgets.getTAxis().setLo(lo_value);
			panelAxesWidgets.getTAxis().setHi(hi_value);
		}
	}
	
	public VariableSerializable getVariable() {
		return var;
	}
	public void addCompareAxisChangeListener(ChangeHandler compareAxisChangeHandler) {
		panelAxesWidgets.getTAxis().addChangeHandler(compareAxisChangeHandler);
		panelAxesWidgets.getZAxis().addChangeHandler(compareAxisChangeHandler);
	}
	public double getMin() {
		return min;
	}
	public double getMax() {
		return max;
	}
    public ClickHandler revertHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			applyChanges();
		}
    	
    };
	public void addRevertHandler(ClickHandler handler) {
		revert.addClickHandler(handler);
	}
	public void setPanelAxisRange(String type, boolean b) {
		if ( type.equals("t") ) {
			panelAxesWidgets.setRange("t", b);
		}
		if ( type.equals("z") ) {
			panelAxesWidgets.setRange("z", b);
		}
	}
	public void setOperation(String id, String v) {
		operationID = id;
		view = v;
		panelAxesWidgets.getRefMap().setTool(view);
	}
    public void setOperationOnly(String id, String v) {
    	// The map might be being used for the analysis, so only set the operation...
    	operationID = id;
    	view = v;
    }
	public void setFillLevels(String fill_levels) {
		this.fill_levels = fill_levels;
	}

	public void setPanelColor(String style) {
		grid.setStyleName(style);
	}
	public void hide() {
		top.setVisible(false);
		grid.getCellFormatter().setHeight(0, 0, "1px");
		panelAxesWidgets.setOpen(false);
	}
	public void show() {
		top.setVisible(true);
	    panelAxesWidgets.setOpen(true);	
	}
	public String getHistoryToken() {
		StringBuilder token = new StringBuilder();
		if ( var.getGrid().hasZ() ) {
			token.append(";zlo="+panelAxesWidgets.getZAxis().getLo()+";zhi="+panelAxesWidgets.getZAxis().getHi());
		}
		if ( var.getGrid().hasT() ) {
		   token.append(";tlo="+panelAxesWidgets.getTAxis().getFerretDateLo()+";thi="+panelAxesWidgets.getTAxis().getFerretDateHi());
		}
		token.append(";dsid="+var.getDSID());
		token.append(";varid="+var.getID());
		token.append(getSettingsWidgetHistoryToken());
		return token.toString();
	}
	public String getSettingsWidgetHistoryToken() {
		
		return 
		       ";xlo="+panelAxesWidgets.getRefMap().getXlo()+
		       ";xhi="+panelAxesWidgets.getRefMap().getXhi()+
		       ";ylo="+panelAxesWidgets.getRefMap().getYlo()+
		       ";yhi="+panelAxesWidgets.getRefMap().getYhi();
		
	}
	public void setFromHistoryToken(Map<String, String> tokenMap, Map<String, String> optionsMap) {		
		// Do the panel stuff here.
		if ( tokenMap.get("tlo") != null && tokenMap.get("thi") != null ) {
			panelAxesWidgets.getTAxis().setLo(tokenMap.get("tlo"));
			panelAxesWidgets.getTAxis().setHi(tokenMap.get("thi"));
		} 
        if ( tokenMap.get("zlo") != null && tokenMap.get("zhi") != null ) {
			panelAxesWidgets.getZAxis().setLo(tokenMap.get("zlo"));
			panelAxesWidgets.getZAxis().setHi(tokenMap.get("zhi"));
		}
		setLatLon(tokenMap.get("ylo"), tokenMap.get("yhi"), tokenMap.get("xlo"), tokenMap.get("xhi"));
		
	}
	public void setPanelModeFromHistoryToken(Map<String, String> tokenMap, Map<String, String> optionsMap, boolean change) {
		String pmvarid = tokenMap.get("varid");
		String pmdsid = tokenMap.get("dsid");
		changeDataset = change;
		Util.getRPCService().getVariable(pmdsid, pmvarid, variableCallback);
	}
	public AsyncCallback<VariableSerializable> variableCallback = new AsyncCallback<VariableSerializable>() {

		@Override
		public void onFailure(Throwable caught) {
			
			Window.alert("Could not initialize panel from URL history.");
			
		}

		@Override
		public void onSuccess(VariableSerializable result) {
			nvar = result;
			Util.getRPCService().getConfig(null, nvar.getDSID(), nvar.getID(), configCallback);			
		}
		
	};
	public MapSelectionChangeListener mapListener = new MapSelectionChangeListener() {

		@Override
		public void onFeatureChanged() {
			
			refreshPlot(null, false, true);
			
		}
		
	};
	ClickHandler datasetOpenHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {
			
			panelAxesWidgets.closePanels();
			
		}
		
	};
	ClickHandler datasetCloseHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {
			
			panelAxesWidgets.restorePanels();
			
		}
		
		
	};

	public String getURL() {
		return currentURL;
	}
	public void setURL(String url) {
		currentURL = url;
	}
	public void setVizGalState(VariableSerializable variable, String historyToken, String comparePanelState) {
		this.vizGalVariable = variable;
		this.vizGalState = historyToken;	
		this.comparePanelState = comparePanelState;
	}
	public void setImage(String image_url, String link_url) {
		final String url = link_url;
		Image image = new Image(image_url);
		image.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				Window.open(url, "plot", "");
				
			}

		});
		image.setTitle("  Click to Enlarge.  Images will size with browser.");
		grid.setWidget(1, 0 , image);
	}
	public String getPrintURL() {
		return currentPrintURL;
	}
	
	public void setAnnotationsOpen(boolean open) {
		lasAnnotationsPanel.setOpen(open);
	}
	public void addAnnotationsClickHandler(ClickHandler clickHandler) {
		lasAnnotationsPanel.addClickHandler(clickHandler);
	}
	public void setAnnotationsButtonDown(boolean down) {
		lasAnnotationsPanel.setButtonDown(down);
	}
	// This is for mock up user interface and are not used with "real" UI's.
	public void setAnnotationsHTMLURL(String url) {
		lasAnnotationsPanel.setAnnotationsHTMLURL(url);
	}
	public void addTChangeHandler(ChangeHandler handler) {
		panelAxesWidgets.addTChangeHandler(handler);
	}
	public void addZChangeHandler(ChangeHandler handler) {
		panelAxesWidgets.addZChangeHandler(handler);
	}
	public void setMapSelectionChangeLister(MapSelectionChangeListener handler) {
		panelAxesWidgets.getRefMap().setMapListener(handler);
	}
	public void setAnalysis(AnalysisSerializable analysisSerializable) {
		this.analysis = analysisSerializable;
	}
	private int getNumber(Node firstChild) {
		if ( firstChild instanceof Text ) {
			Text content = (Text) firstChild;
			String value = content.getData().toString().trim();
			return Double.valueOf(value).intValue();
		} else {
			return -999;
		}
	}
	private double getDouble(Node firstChild) {
		if ( firstChild instanceof Text ) {
			Text content = (Text) firstChild;
			String value = content.getData().toString().trim();
			return Double.valueOf(value).doubleValue();
		} else {
			return -999.;
		}
	}
	public void setMouseMoves(List<Mouse> mouse) {
		mouseMoves = mouse;
	}
	public void setRanges(String xView, List<String> xOrtho) {
		for (int i = 0; i < xView.length(); i++) {
			String type = xView.substring(i, i+1);
			panelAxesWidgets.setRange(type, true);
		}
		for (Iterator<String> oIt = xOrtho.iterator(); oIt.hasNext(); ) {
			String type = (String) oIt.next();
			panelAxesWidgets.setRange(type, false);
		}
	}
	LoadHandler imageLoadHandler =  new LoadHandler() {

		@Override
		public void onLoad(LoadEvent event) {
			int wt = (int)((plotImage.getWidth() - 100)*imageScaleRatio);
			String w = wt + "px";
            lasAnnotationsPanel.setPopupWidth(w);
			frontCanvasContext.drawImage(ImageElement.as(plotImage.getElement()), 0, 0);
			setImageWidth();
			frontCanvas.addMouseDownHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					
					startx = event.getX();
					starty = event.getY();
					if ( startx > x_offset_from_left && 
						 starty > y_offset_from_top &&
						 startx < x_offset_from_left + x_plot_size && 
						 starty < y_offset_from_top + y_plot_size      ) {
						
						draw = true;
						drawToScreen(scaledImage); //frontCanvasContext.drawImage(ImageElement.as(plotImage.getElement()), 0, 0);
						double scaled_x_per_pixel = x_per_pixel/imageScaleRatio;
						double scaled_y_per_pixel = y_per_pixel/imageScaleRatio;
						world_startx = x_axis_lower_left + (startx - x_offset_from_left*imageScaleRatio)*scaled_x_per_pixel;
						world_starty = y_axis_lower_left + ((y_image_size*imageScaleRatio-starty)-y_offset_from_bottom*imageScaleRatio)*scaled_y_per_pixel;
						
						world_endx = world_startx;
						world_endy = world_starty;										
					}
				}
			});
			frontCanvas.addMouseMoveHandler(new MouseMoveHandler() {

				@Override
				public void onMouseMove(MouseMoveEvent event) {
					int currentx = event.getX();
					int currenty = event.getY();
					// If you drag it out, we'll stop drawing.
					if ( currentx < x_offset_from_left || 
					     currenty < y_offset_from_top ||
						 currentx > x_offset_from_left + x_plot_size || 
					     currenty > y_offset_from_top + y_plot_size      ) {
						
						draw = false;
						endx = currentx;
						endy = currenty;
					}
					if ( draw ) {
						double scaled_x_per_pixel = x_per_pixel/imageScaleRatio;
						double scaled_y_per_pixel = y_per_pixel/imageScaleRatio;
						world_endx = x_axis_lower_left + (currentx - x_offset_from_left*imageScaleRatio)*scaled_x_per_pixel;
						world_endy = y_axis_lower_left + ((y_image_size*imageScaleRatio-currenty)-y_offset_from_bottom*imageScaleRatio)*scaled_y_per_pixel;
						frontCanvasContext.setFillStyle(randomColor);
						drawToScreen(scaledImage); //frontCanvasContext.drawImage(ImageElement.as(plotImage.getElement()), 0, 0);
						frontCanvasContext.fillRect(startx, starty, currentx - startx, currenty-starty);
						for (Iterator mouseIt = mouseMoves.iterator(); mouseIt.hasNext();) {
							Mouse mouse = (Mouse) mouseIt.next();
							mouse.updateMap(world_starty, world_endy, world_startx, world_endx);
						}
					}
				}
			});
			grid.setWidget(1, 0 , frontCanvas);
		}

	};
	private void scale(Image img, double scaleRatio) {
	    scaledImage = scaleImage(img, scaleRatio);
	    drawToScreen(scaledImage);
	}

	private ImageData scaleImage(Image image, double scaleToRatio) {
	    
	    Canvas canvasTmp = Canvas.createIfSupported();
	    Context2d context = canvasTmp.getContext2d();

	    double ch = (image.getHeight() * scaleToRatio);
	    double cw = (image.getWidth() * scaleToRatio);

	    canvasTmp.setCoordinateSpaceHeight((int) ch);
	    canvasTmp.setCoordinateSpaceWidth((int) cw);
	    
	    ImageElement imageElement = ImageElement.as(image.getElement());
	   
	    // s = source
	    // d = destination 
	    double sx = 0;
	    double sy = 0;
	    double sw = imageElement.getWidth();
	    double sh = imageElement.getHeight();
	    
	    double dx = 0;
	    double dy = 0;
	    double dw = imageElement.getWidth();
	    double dh = imageElement.getHeight();
	    
	    // tell it to scale image
	    context.scale(scaleToRatio, scaleToRatio);
	    
	    // draw image to canvas
	    context.drawImage(imageElement, sx, sy, sw, sh, dx, dy, dw, dh);
	    
	    // get image data
	    double w = dw * scaleToRatio;
	    double h = dh * scaleToRatio;
	    ImageData imageData = context.getImageData(0, 0, w, h);
	    
	    frontCanvas.setCoordinateSpaceHeight((int)h+10);
	    frontCanvas.setCoordinateSpaceWidth((int)w+10);	    
	    return imageData;
	}

	private void drawToScreen(ImageData imageData) {
	    if ( frontCanvasContext != null ) frontCanvasContext.putImageData(imageData, 0, 0);
	}
}
