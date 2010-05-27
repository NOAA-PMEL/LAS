package gov.noaa.pmel.tmap.las.client.vizgal;

import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.laswidget.AxesWidgetGroup;
import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequestWrapper;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationRadioButton;
import gov.noaa.pmel.tmap.las.client.laswidget.SettingsWidget;
import gov.noaa.pmel.tmap.las.client.map.MapSelectionChangeListener;
import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Constants;
import gov.noaa.pmel.tmap.las.client.util.URLUtil;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
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
public class VizGalPanel extends Composite {

    /* TODO When changing a panel to "panel mode" the panel needs to save the state of the panelAxesWidgets including the titles,
     * the view, what's ortho and the axis states (hi, lo and range boolean) and restore that state when the panel is reverted.
     */
	
	/* A message window when the plot cannot be made... */
	PopupPanel messagePanel;
	Grid messageGrid;
	Button messageButton;
	HTML message;

	/* The base widget used to layout the panel.  A single column of three rows. */
	FlexTable grid;
	
	/* The top bar of widgets... */
	Grid top;

	/* A widget to return panel to the slide sorter control */
	Button revert = new Button("Revert");

	/* The current data set and variable.  */
	Label datasetLabel;

	/* This is the request.  It is either built from scratch or passed in via the constructor.  */
	LASRequestWrapper lasRequest = new LASRequestWrapper();

	// Keep track of the values that are currently being used as the fixed axis and compare axis
	String compareAxis;
	String fixedAxis;
	String fixedAxisValue;

	/*
	 * A a button that pops up a panel for region selection, operation and (still to be implemented) plot options.
	 */
	SettingsWidget settingsButton;

	/*
	 * The copy of the axes for this panel.
	 */
	AxesWidgetGroup panelAxesWidgets;
     
	/* Keep track of the optionID, view and operation.  These are passed in as parameters when the pane is created. */
	String optionID;
	String operationID;
	String view;

	/* The current variable in this panel. */
	VariableSerializable var;

	// Some widgets to show when a panel is being refreshed.
	PopupPanel spin;
	HTML spinImage;
	HTML rangeMessage;

	// The view will have at most two axes,  these hold the ranges for those axes.
	String xlo;
	String xhi;

	String ylo;
	String yhi;

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
	
	// Keep track of the axes that are currently orthogonal to the plot.
	List<String> ortho;
	
	// Keep track of whether the retry is visible and remove it when the results come back.
	boolean retryShowing = false;
	
	/**
	 * Builds a VizGal panel with a default plot for the variable.  See {@code}VizGal(LASRequest) if you want more options on the initial plot.
	 */
	public VizGalPanel(String id, boolean comparePanel, String op, String optionID, String view, boolean single) {
		this.ID = id;
		this.comparePanel = comparePanel;
		this.singlePanel = single;
		this.operationID = op;
		this.view = view;
		panelAxesWidgets = new AxesWidgetGroup("Plot Axis", "Comparison Axis", "horizontal", "", "Apply To "+ID);
		String spinImageURL = URLUtil.getImageURL()+"/mozilla_blu.gif";
		spinImage = new HTML("<img src=\""+spinImageURL+"\" alt=\"Spinner\"/>");
		
		rangeMessage = new HTML("Set plot range selectors to a different values and clike the Apply button.");

		spin = new PopupPanel();
		
		spin.add(spinImage);

		messagePanel = new PopupPanel();
		messageGrid = new Grid(1, 2);
		messageButton = new Button("Close");
		messageButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				messagePanel.hide();
			}

		});

		message = new HTML("Could not make plot.  Axes set to ranges do not match the upper left panel.");
		messageGrid.setWidget(0, 0, message);
		messageGrid.setWidget(0, 1, messageButton);

		messagePanel.add(messageGrid);

		grid = new FlexTable();
		grid.setStyleName("regularBackground");
		//grid.getCellFormatter().setHeight(0, 0, panelHeader);
		datasetLabel = new Label();

		top = new Grid(1,3);

		String title = "Settings";
		settingsButton = new SettingsWidget(title, ID, operationID, optionID);
	
		settingsButton.addDatasetTreeListener(datasetTreeListener);
		settingsButton.addOpenHandler(datasetOpenHandler);
		settingsButton.addCloseHandler(datasetCloseHandler);
		settingsButton.addOptionsOkClickListener(optionsOkListener);
		settingsButton.addOperationClickHandler(operationsClickHandler);
		revert.addClickListener(revertListener);
		revert.setTitle("Cancel Panel Settings for "+ID);

		top.setWidget(0, 0, datasetLabel);
		//top.getCellFormatter().setWordWrap(0, 0, false);
		top.getColumnFormatter().setWidth(0, "85%");
		top.getCellFormatter().setHeight(0, 0, "30px");
		top.getColumnFormatter().addStyleName(1, "las-align-right");
		
		if ( comparePanel ) {
			Label gs = new Label("(See left)");
			top.setWidget(0, 1, gs);
		} else {
			top.setWidget(0, 1, settingsButton);
		} 

		grid.setWidget(0, 0, top);
		HTML plot = new HTML();
		plot.setHTML(spinImage.getHTML());
		grid.setWidget(1, 0, plot);
		initWidget(grid);	


	}
	public void init(boolean usePanel) {
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
		
		settingsButton.setOperations(var.getIntervals(), var.getDSID(), var.getID(), operationID, view);
		
		settingsButton.setUsePanel(usePanel);
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
		panelAxesWidgets.setCompareAxis(view, ortho, compareAxis);
		if ( !singlePanel ) {
			// In singlePanel, the controls will be on the left navbar.
			grid.setWidget(2, 0, panelAxesWidgets);
		} 
	}
	public void setMapTool(String view) {
		panelAxesWidgets.getRefMap().setTool(view);
	}
	private void setLocalAxesWidgets() {
		/*
		 * Not sure when this is called and why:
		if ( view.contains("t") ) {
			compareAxisAndMap.clear();
			if ( compareAxis.equals("t") ) {
				compareAxisAndMap.add(dateTimeWidget);
				OLMapWidget map = settingsButton.getDisplayRefMap();
				map.setMapListener(mapListener);
				compareAxisAndMap.add(map);
				grid.setWidget(2, 0, compareAxisAndMap);
			}
			if ( compareAxis.equals("z") ) {
				compareAxisAndMap.add(zAxisWidget);
				OLMapWidget map = settingsButton.getDisplayRefMap();
				map.setMapListener(mapListener);
				compareAxisAndMap.add(map);
				grid.setWidget(2, 0, compareAxisAndMap);
			}		
		} else {
			if ( compareAxis.equals("t") ) {
				grid.setWidget(2, 0, dateTimeWidget);
			}
			if ( compareAxis.equals("z") ) {
				grid.setWidget(2, 0, zAxisWidget);
			}
		}
		*/
	}
	private void switchAxis() {
		// Already done by the ComparisonAxisSelector...
//		String temp = compareAxis;
//		compareAxis = fixedAxis;
//		fixedAxis = temp;
//		panelAxesWidgets.setCompareAxis(view, ortho, compareAxis);
	}
	// The SliderSorter needs to pass in which axis it is controlling and the value it should have...
	public void refreshPlot(Map<String, String> options, boolean switchAxis, boolean popup) {
		if (switchAxis) {
			switchAxis();
		}
		
		spin.hide();
		
        lasRequest = getRequest();
        lasRequest.setProperty("ferret", "view", view);
		lasRequest.setProperty("ferret", "size", ".8333");
		lasRequest.addProperty("ferret", "image_format", "gif");
		lasRequest.addProperty("las", "output_type", "xml");
		if ( settingsButton.isUsePanelSettings() || singlePanel ) {
			// Use panel options if they exist.
			Map<String, String> panelOptions = settingsButton.getOptions();
			for (Iterator opIt = panelOptions.keySet().iterator(); opIt.hasNext();) {
				String key = (String) opIt.next();
				String value = panelOptions.get(key);
				if ( !value.toLowerCase().equals("default") && !value.equals("") ) {
					lasRequest.addProperty("ferret", key, value);
				}
			}
		} else {
			// Use Gallery Settings if they have been set.
			if ( options != null ) {
				for (Iterator opIt = options.keySet().iterator(); opIt.hasNext();) {
					String key = (String) opIt.next();
					String value = options.get(key);
					if ( !value.toLowerCase().equals("default") && !value.equals("") ) {
						lasRequest.addProperty("ferret", key, value);
					}
				}
			}			
		}
		// Now force in the auto contour settings if it exists.
		if ( fill_levels != null && !fill_levels.equals("") && !fill_levels.equals(Constants.NO_MIN_MAX) ) {
			lasRequest.addProperty("ferret", "fill_levels", fill_levels);
		}
		lasRequest.setProperty("product_server", "ui_timeout", "10");	
        if ( var.getGrid().getTAxis() != null ) {		
        	if ( view.contains("t") && panelAxesWidgets.getTAxis().getFerretDateLo().equals(panelAxesWidgets.getTAxis().getFerretDateHi()) ) {
        		spin.setWidget(rangeMessage);
        		spin.show();
        		return;
        	}
        	if ( view.contains("z") && panelAxesWidgets.getZAxis().getLo().equals(panelAxesWidgets.getZAxis().getHi()) ) {
        		spin.setWidget(rangeMessage);
        		spin.show();
        		return;
        	}
		}
        
        if ( lasRequest == null ) {
        	return;
        }
        
		String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.getXMLText());
		
		if ( !url.equals(currentURL) ) {
			currentURL = url;
			if (popup) {
				spin.setWidget(spinImage);
				spin.setPopupPosition(grid.getWidget(1,0).getAbsoluteLeft(), grid.getWidget(1,0).getAbsoluteTop());
				spin.show();
			}
			RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
			try {
				sendRequest.sendRequest(null, lasRequestCallback);
			} catch (RequestException e) {
				HTML error = new HTML(e.toString());
				grid.setWidget(1, 0, error);
			}
		}
	}
	public boolean isComparePanel() {
		return comparePanel;
	}
	public LASRequestWrapper getRequest() {
		LASRequestWrapper lasRequest = new LASRequestWrapper();
		lasRequest.removeRegion(0);
		lasRequest.removeVariables();
		lasRequest.removePropertyGroup("ferret");

		lasRequest.addVariable(var.getDSID(), var.getID());
		lasRequest.setOperation(operationID, "v7");
		

		if ( var.getGrid().getTAxis() != null ) {
			lasRequest.setRange("t", panelAxesWidgets.getTAxis().getFerretDateLo(), panelAxesWidgets.getTAxis().getFerretDateHi(), 0);
		}

		xlo = String.valueOf(panelAxesWidgets.getRefMap().getXlo());
		xhi = String.valueOf(panelAxesWidgets.getRefMap().getXhi());
		ylo = String.valueOf(panelAxesWidgets.getRefMap().getYlo());
		yhi = String.valueOf(panelAxesWidgets.getRefMap().getYhi());

		lasRequest.setRange("x", xlo, xhi, 0);
		lasRequest.setRange("y", ylo, yhi, 0);

		if ( var.getGrid().getZAxis() != null ) {
			lasRequest.setRange("z", panelAxesWidgets.getZAxis().getLo(), panelAxesWidgets.getZAxis().getHi(), 0);
		}
		return lasRequest;
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
			grid.setWidget(1, 0, error);
		}

		public void onResponseReceived(Request request, Response response) {
			String doc = response.getText();
			if ( retryShowing ) {
				grid.removeCell(2, 1);
				retryShowing = false;
			}
			// Look at the doc.  If it's not obviously XML, treat it as HTML.
			if ( !doc.contains("<?xml") ) {
				HTML result = new HTML(doc);
				grid.setWidget(1, 0, result);
			} else {
				doc = doc.replaceAll("\n", "").trim();
				Document responseXML = XMLParser.parse(doc);
				NodeList results = responseXML.getElementsByTagName("result");
				for(int n=0; n<results.getLength();n++) {
					if ( results.item(n) instanceof Element ) {
						Element result = (Element) results.item(n);
						if ( result.getAttribute("type").equals("image") ) {
							//HTML image = new HTML("<a target=\"_blank\" href=\""+result.getAttribute("url")+"\"><img width=\"100%\" src=\""+result.getAttribute("url")+"\"></a>");
							final String url = result.getAttribute("url");
							Image image = new Image(result.getAttribute("url"));
							image.addClickListener(new ClickListener() {

								public void onClick(Widget sender) {
									Window.open(url, "plot", "");

								}

							});
							image.setTitle("  Click to Enlarge.  Images will size with browser.");
							grid.setWidget(1, 0 , image);
							setImageWidth();
//							if ( autoZoom ) {
//							image.setWidth(pwidth+"px");
//							int h = (int) ((image_h/image_w)*Double.valueOf(pwidth));
//							image.setHeight(h+"px");
//							} else {
//							setImageSize(fixedZoom);
//							}

						} else if ( result.getAttribute("type").equals("map_scale") )  {
							final String ms_url = result.getAttribute("url");
							RequestBuilder mapScaleRequest = new RequestBuilder(RequestBuilder.GET, ms_url);
							try {
								mapScaleRequest.sendRequest(null, mapScaleCallBack);
							} catch (RequestException e) {
								// Don't care.  Just go with the information we have.
							}
						} else if ( result.getAttribute("type").equals("error") ) {
							if ( result.getAttribute("ID").equals("las_message") ) {
								Node text = result.getFirstChild();
								if ( text instanceof Text ) {
									Text t = (Text) text;
									HTML error = new HTML(t.getData().toString().trim());
									grid.setWidget(1, 0, error);
									retryShowing = true;
									Button retry = new Button("Retry");
									retry.addClickListener(new ClickListener() {
										public void onClick(Widget sender) {
											
											// Just send the same request again to see if it works the second time.
											String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.getXMLText());
											RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
											try {
												sendRequest.sendRequest(null, lasRequestCallback);
											} catch (RequestException e) {
												HTML error = new HTML(e.toString());
												grid.setWidget(1, 0, error);
											}
										}

									});
									grid.setWidget(2, 1, retry);
								}
							}
						} else if ( result.getAttribute("type").equals("batch") ) {
							String elapsed_time = result.getAttribute("elapsed_time");
							HTML batch = new HTML(spinImage.getHTML()+"<br><br>Your request has been processing for "+elapsed_time+" seconds.<br>This panel will refresh automatically.<br><br>");
							grid.setWidget(1, 0, batch);
							lasRequest.setProperty("product_server", "ui_timeout", "3");
							String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.getXMLText());
							RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
							try {
								sendRequest.sendRequest(null, lasRequestCallback);
							} catch (RequestException e) {
								HTML error = new HTML(e.toString());
								grid.setWidget(1, 0, error);
							}
						}
					}
				}
			}
			spin.hide();
		}
	};
	

	TreeListener datasetTreeListener = new TreeListener() {

		public void onTreeItemSelected(TreeItem item) {
			Object v = item.getUserObject();
			if ( v instanceof VariableSerializable ) {
				nvar = (VariableSerializable) v;
				ngrid = null;
				changeDataset = true;
				Util.getRPCService().getGrid(nvar.getDSID(), nvar.getID(), gridCallback);
			}
		}

		public void onTreeItemStateChanged(TreeItem item) {
			// TODO Auto-generated method stub

		}

	};
	public AsyncCallback gridCallback = new AsyncCallback() {

		@Override
		public void onFailure(Throwable caught) {
			Window.alert("Could not fetch grid for new variable.");
		}

		@Override
		public void onSuccess(Object result) {
			ngrid = (GridSerializable) result;
			nvar.setGrid(ngrid);
			applyChanges();
		}
		
	};
	public void setVariable(VariableSerializable v) {
		var = v;
		panelAxesWidgets.init(var.getGrid());
	}
	public void setCompareAxis(String view, List<String> ortho, String compareAxis) {
		this.compareAxis = compareAxis;
		panelAxesWidgets.setCompareAxis(view, ortho, compareAxis);
	}
	private void applyChanges() {
		
		if ( ngrid == null ) {
			Window.alert("Still fetching grid for new variable.");
			return;
		}
		if (changeDataset) {			
			var = nvar;
			datasetLabel.setText(var.getDSName()+": "+var.getName());
			settingsButton.setUsePanel(true);
			changeDataset = false;
			init(true);
		}
		
		if (settingsButton.isUsePanelSettings()) {
			grid.setStyleName("panelSettingsColor");
			if ( !singlePanel ) {
				top.setWidget(0, 2, revert);
			}
			showAllAxes();
		} else {
			grid.setStyleName("regularBackground");
			if ( !singlePanel ) {
				top.remove(revert);
			}
			panelAxesWidgets.setCompareAxis(view, ortho, compareAxis);
		}
		refreshPlot(settingsButton.getOptions(), false, true);
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
	
	public void addApplyHandler(ClickHandler applyClick) {
		//settingsButton.addApplyClickListener(applyClick);
		panelAxesWidgets.addApplyHandler(applyClick);
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
		if ( autoZoom ) {
			if ( pwidth < image_w ) {
				// If the panel is less than the image, shrink the image.
				int h = (int) ((image_h/image_w)*Double.valueOf(pwidth));
				w.setWidth(pwidth+"px");
				w.setHeight(h+"px");
			} else {
				// Just use the exact image size.
				w.setWidth(image_w+"px");
				w.setHeight(image_h+"px");
			}
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
		} else {
			panelAxesWidgets.getTAxis().setLo(lo_value);
			panelAxesWidgets.getTAxis().setHi(hi_value);
		}
	}
	public boolean isUsePanelSettings() {
		return settingsButton.isUsePanelSettings();
	}
	
	public VariableSerializable getVariable() {
		return var;
	}
	public void computeDifference(Map<String, String> options, boolean switchAxis, VariableSerializable variable, String view_in, 
			String xlo_in, String xhi_in, String ylo_in, String yhi_in, String zlo_in,
			String zhi_in, String tlo_in, String thi_in) {

		if (switchAxis) {
			switchAxis();
		}
		
        spin.hide();

		if ( !view_in.equals(settingsButton.getOperationsWidget().getCurrentView()) ) {
			messagePanel.setPopupPosition(grid.getWidget(1, 0).getAbsoluteLeft()+15, grid.getWidget(1,0).getAbsoluteTop()+15);
			messagePanel.show();
			return;
		}
		
		lasRequest = new LASRequestWrapper();
		lasRequest.removeRegion(0);
		lasRequest.removeVariables();
		lasRequest.removePropertyGroup("ferret");
		lasRequest.setOperation("Compare_Plot", "v7");
		lasRequest.setProperty("ferret", "view", view);
		lasRequest.setProperty("ferret", "size", ".8333");
		// Add the variable in the upper left panel
		lasRequest.addVariable(variable.getDSID(), variable.getID());

		// From the current slide sorter is this comment:
		//
		// Need to add a dummy variable here because the addRegion/AddVariable
		// methods of LASRequest.js will not always add at the end of the <args>
		// node list when there are duplicate <link ...> or <region ...> nodes.
		// Using 'DUMMY' guarantees that addRegion will place the new region at
		// the end.  Then we just replace 'DUMMY'.
		// All this is necessary because order is important in comparison 
		// requests.
		//
		// mimic this action here....

		// For the first variable set the region according to the values passed in...
		if ( xlo_in != null && xhi_in != null && !xlo_in.equals("") && !xhi_in.equals("") ) {
			lasRequest.setRange("x", xlo_in, xhi_in, 0);
		}
		if ( ylo_in != null && yhi_in != null && !ylo_in.equals("") && !yhi_in.equals("") ) {
			lasRequest.setRange("y", ylo_in, yhi_in, 0);
		}
		if ( zlo_in != null && zhi_in != null && !zlo_in.equals("") && !zhi_in.equals("") ) {
			lasRequest.setRange("z", zlo_in, zhi_in, 0);
		}
		if ( tlo_in != null && thi_in != null && !tlo_in.equals("") && !thi_in.equals("") ) {
			lasRequest.setRange("t", tlo_in, thi_in, 0);
		}
		lasRequest.addVariable("dummy", "dummy");

		lasRequest.addRegion();
		lasRequest.replaceVariable(var.getDSID(), var.getID(), 1);

		// For the second variable set all the axes that are not in the view, 
		// either from the fixed in the slide sorter and the comparison axis in the panel
		// or from the panel settings.
		if ( isUsePanelSettings() || singlePanel ) {
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
		} else {
			if ( !view.contains("x") ) {
				if ( var.getGrid().hasX() ) {
					if ( compareAxis.contains("x") ) {
						lasRequest.setRange("x", String.valueOf(panelAxesWidgets.getRefMap().getXlo()), String.valueOf(panelAxesWidgets.getRefMap().getXhi()), 1);
					} else {
						lasRequest.setRange("x", xlo_in, xhi_in, 1);
					}
				}
			}
			if ( !view.contains("y") ) {
				if ( var.getGrid().hasY() ) {
					if ( compareAxis.contains("y") ) {
						lasRequest.setRange("y", String.valueOf(panelAxesWidgets.getRefMap().getYlo()), String.valueOf(panelAxesWidgets.getRefMap().getYhi()), 1);
					} else {
						lasRequest.setRange("y", ylo_in, yhi_in, 1);
					}
				}
			}
			if ( !view.contains("z") ) {
				if ( var.getGrid().hasZ() ) {
					if ( compareAxis.equals("z") ) {
						// Use the panel's compare axis widget.
						lasRequest.setRange("z", panelAxesWidgets.getZAxis().getLo(), panelAxesWidgets.getZAxis().getHi(), 1);
					} else {
						//Use the fixed axis in the slide sorter (the passed in value) if it applies to this data set.
						if ( zlo_in != null && zhi_in != null && !zlo_in.equals("") && !zhi_in.equals("") ) {
							lasRequest.setRange("z", zlo_in, zhi_in, 1);
						}
					}				
				}
			}
			if ( !view.contains("t") ) {
				if ( var.getGrid().getTAxis() != null ) {
					if ( compareAxis.equals("t") ) {
						// Use the panel's compare axis widget.
						lasRequest.setRange("t", panelAxesWidgets.getTAxis().getFerretDateLo(), panelAxesWidgets.getTAxis().getFerretDateHi(), 1);
					} else {
						//Use the fixed axis in the slide sorter (the passed in value) if it applies to this data set.
						if ( tlo_in != null && thi_in != null && !tlo_in.equals("") && !thi_in.equals("") ) {
							lasRequest.setRange("t", tlo_in, thi_in, 1);
						}
					}	
				}

			}		
		}
		lasRequest.addProperty("ferret", "image_format", "gif");
		lasRequest.addProperty("las", "output_type", "xml");
		if ( settingsButton.isUsePanelSettings() || singlePanel ) {
			// Use the panel options.
			Map<String, String> panelOptions = settingsButton.getOptions();
			if ( panelOptions != null ) {
				for (Iterator opIt = panelOptions.keySet().iterator(); opIt.hasNext();) {
					String key = (String) opIt.next();
					String value = panelOptions.get(key);
					if ( !value.toLowerCase().equals("default") && !value.equals("") ) {
						lasRequest.addProperty("ferret", key, value);
					}
				}
			}
		} else {
			// Use the global options.
			if ( options != null ) {
				for (Iterator opIt = options.keySet().iterator(); opIt.hasNext();) {
					String key = (String) opIt.next();
					String value = options.get(key);
					if ( !value.toLowerCase().equals("default") && !value.equals("") ) {
						lasRequest.addProperty("ferret", key, value);
					}
				}
			}
		}
		lasRequest.setProperty("product_server", "ui_timeout", "20");
		String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.getXMLText());
		
		if ( !url.equals(currentURL) ) {
			currentURL = url;
			spin.setPopupPosition(grid.getWidget(1, 0).getAbsoluteLeft(), grid.getWidget(1, 0).getAbsoluteTop());
			spin.show();
			RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET,
					url);
			try {
				sendRequest.sendRequest(null, lasRequestCallback);
			} catch (RequestException e) {
				HTML error = new HTML(e.toString());
				grid.setWidget(1, 0, error);
			}
		}
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
	public ClickListener revertListener = new ClickListener() {
		public void onClick(Widget sender) {
			settingsButton.setUsePanel(false);
			applyChanges();
		}
	};

	public void addRevertHandler(ClickHandler handler) {
		revert.addClickHandler(handler);
	}
	public ClickListener optionsOkListener = new ClickListener() {
		public void onClick(Widget sender) {
			settingsButton.setUsePanel(true);
			applyChanges();
		}
	};
	public ClickHandler operationsClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			if ( event.getSource() instanceof OperationRadioButton ) {
				OperationRadioButton o = (OperationRadioButton) event.getSource();
				view = settingsButton.getOperationsWidget().getCurrentView();
				operationID = settingsButton.getCurrentOp().getID();
				settingsButton.setUsePanel(true);
				if ( isUsePanelSettings() || singlePanel ) {
					if ( view.contains("t") ) {
						panelAxesWidgets.setRange("t", true);
					} else {
						panelAxesWidgets.setRange("t", false);
					}
					if ( view.contains("z") ) {
						panelAxesWidgets.setRange("z", true);
					} else {
						panelAxesWidgets.setRange("z", false);
					}
					
					// In "panel" mode so show all the axes.
					showAllAxes();
					 

					applyChanges();
				} 
			}			
		}	
	};
    private void showAllAxes() {
    	/*
         * If you don't remove the map, all the features that have
         * been rendered to it while it was hidden will appear on
         * the map and will be zombies (you can't clear them
         * and you can't select them).  Remove the map and reinitializing
         * it works around this problem.
         */
    	

       
        double[] data = panelAxesWidgets.getRefMap().getDataExtent();

        double xlo = panelAxesWidgets.getRefMap().getXlo();
        double xhi = panelAxesWidgets.getRefMap().getXhi();
        double ylo = panelAxesWidgets.getRefMap().getYlo();
        double yhi = panelAxesWidgets.getRefMap().getYhi();

        double delta = panelAxesWidgets.getRefMap().getDelta();
        int zoom = panelAxesWidgets.getRefMap().getZoom();
        double[] center = panelAxesWidgets.getRefMap().getCenterLatLon();
        
		panelAxesWidgets.showAll(view, Util.setOrthoAxes(view, var.getGrid()));
		
		panelAxesWidgets.getRefMap().setTool(view);
		panelAxesWidgets.getRefMap().setCenter(center[0], center[1], zoom);
		panelAxesWidgets.getRefMap().setDataExtentOnly(data[0], data[1], data[2], data[3], delta);
		panelAxesWidgets.getRefMap().setCurrentSelection(ylo, yhi, xlo, xhi);
    }
	public void setParentAxisRange(String type, boolean b) {
		if ( type.equals("t") ) {
			panelAxesWidgets.setRange("t", b);
		}
		if ( type.equals("z") ) {
			panelAxesWidgets.setRange("z", b);
		}
		setLocalAxesWidgets();
	}
	public void setOperation(String id, String v) {
		settingsButton.setOperation(id, v);	
		operationID = id;
		view = v;
		panelAxesWidgets.getRefMap().setTool(view);
	}

	public void addSettingsButtonListener(ClickListener clickListener) {
		settingsButton.addClickListener(clickListener);
	}
	public void setFillLevels(String fill_levels) {
		this.fill_levels = fill_levels;
	}
	public String getCurrentOperationView() {
		return settingsButton.getCurrentOperationView();
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
		token.append("usePanelSettings="+settingsButton.isUsePanelSettings());
		if ( compareAxis.equals("t") ) {
			token.append(";compareAxis=t;compareAxisLo="+panelAxesWidgets.getTAxis().getFerretDateLo()+";compareAxisHi="+panelAxesWidgets.getTAxis().getFerretDateHi());
			
		} else if ( compareAxis.equals("z") ) {
			token.append(";compareAxis=z;compareAxisLo="+panelAxesWidgets.getZAxis().getLo()+";compareAxisHi="+panelAxesWidgets.getZAxis().getHi());
		}
		token.append(";dsid="+var.getDSID());
		token.append(";varid="+var.getID());
		return token.toString();
	}
	public String getSettingsWidgetHistoryToken() {
		
		return settingsButton.getHistoryToken()+
		       ";xlo="+panelAxesWidgets.getRefMap().getXlo()+
		       ";xhi="+panelAxesWidgets.getRefMap().getXhi()+
		       ";ylo="+panelAxesWidgets.getRefMap().getYlo()+
		       ";yhi="+panelAxesWidgets.getRefMap().getYhi();
		
	}
	public void setFromHistoryToken(Map<String, String> tokenMap, Map<String, String> optionsMap) {		
		// Do the panel stuff here.
		if (tokenMap.get("compareAxis").equals("t") ) {
			panelAxesWidgets.getTAxis().setLo(tokenMap.get("compareAxisLo"));
			panelAxesWidgets.getTAxis().setHi(tokenMap.get("compareAxisHi"));
		} else {
			panelAxesWidgets.getZAxis().setLo(tokenMap.get("compareAxisLo"));
			panelAxesWidgets.getZAxis().setHi(tokenMap.get("compareAxisHi"));
		}
		setLatLon(tokenMap.get("ylo"), tokenMap.get("yhi"), tokenMap.get("xlo"), tokenMap.get("xhi"));
		if (isUsePanelSettings()) {
		    settingsButton.setFromHistoryToken(tokenMap, optionsMap);
		}
		
	}
	
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
}
