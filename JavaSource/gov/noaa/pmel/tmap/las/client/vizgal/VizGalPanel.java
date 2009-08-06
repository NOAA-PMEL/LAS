package gov.noaa.pmel.tmap.las.client.vizgal;

import gov.noaa.pmel.tmap.las.client.OperationButton;
import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequestWrapper;
import gov.noaa.pmel.tmap.las.client.laswidget.TandZWidgets;
import gov.noaa.pmel.tmap.las.client.map.SettingsWidget;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
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

	/* A message window when the plot cannot be made... */
	PopupPanel messagePanel;
	Grid messageGrid;
	Button messageButton;
	HTML message;

	/* The base widget used to layout the other widgets.  A single column of three rows. */
	FlexTable grid;

	/* The top bar of widgets... */
	Grid top;

	/* A widget to return panel to the slide sorter control */
	Button revert = new Button("Cancel Panel Settings");

	/* The current data set and variable.  */
	Label datasetLabel;

	/* This is the request.  It is either built from scratch or passed in via the constructor.  */
	LASRequestWrapper lasRequest = new LASRequestWrapper();

	// Keep track of the values that are currently being used as the fixed axis and compare axis
	String compareAxis;
	String fixedAxis;
	String fixedAxisValue;

	/*
	 * A a button that pops up a panel for region selection, operation and (still to be implmented) plot options.
	 */
	SettingsWidget settingsButton;

	/*
	 * A widget that keeps track of the orthogonal axes for this panel.  When in using panel settings it's displayed on the bottom of the panel.
	 */
	TandZWidgets tandzWidgets = new TandZWidgets();

	/* Keep track of the view and operation.  These are passed in as parameters when the pane is created. */
	String op;
	String view;

	/* The product server base URL */

	String productServer;

	/* The current variable in this panel. */
	VariableSerializable var;

	/* The dateTimeWidget for this panel. */
	DateTimeWidget dateTimeWidget = new DateTimeWidget();

	/* The z-axis widget for this panel. */
	AxisWidget zAxisWidget = new AxisWidget();

	// Some widgets to show when a panel is being refreshed.
	PopupPanel spin;
	HTML spinImage;

	// The view will have at most two axes,  these hold the ranges for those axes.
	String xlo;
	String xhi;

	String ylo;
	String yhi;

	String ID;

	RPCServiceAsync rpcService;

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
	
	// The height in pixels of the panel header
	String panelHeader = "75px";

	/**
	 * Builds a VizGal panel with a default plot for the variable.  See {@code}VizGal(LASRequest) if you want more options on the initial plot.
	 */
	public VizGalPanel(String id, boolean comparePanel, String op, String view, String productServer, boolean single, RPCServiceAsync rpcService, boolean allowEditing) {
		this.ID = id;
		this.comparePanel = comparePanel;
		this.productServer = productServer;
		this.singlePanel = single;
		this.rpcService = rpcService;
		this.op = op;
		this.view = view;
		dateTimeWidget.addChangeListener(comparisonAxisChangeListener);
		zAxisWidget.addChangeListener(comparisonAxisChangeListener);

		spin = new PopupPanel();
		spinImage = new HTML("<img src=\"../JavaScript/components/mozilla_blu.gif\" alt=\"Spinner\"/>");
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
		settingsButton = new SettingsWidget(title, LatLng.newInstance(0.0, 0.0), 1, 256, 360, ID, op, rpcService, "button", allowEditing);
		settingsButton.addApplyClickListener(applyPanelClick);
		settingsButton.addCloseClickListener(closeClick);
		settingsButton.addDatasetTreeListener(datasetTreeListener);
		settingsButton.addOptionsOkClickListener(optionsOkListener);
		settingsButton.addOperationClickListener(operationsClickListener);
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
		plot.setHTML("<img src=\"../JavaScript/components/mozilla_blu.gif\" alt=\"Spinner\"/>");
		grid.setWidget(1, 0, plot);
		initWidget(grid);	


	}
	public void init(boolean usePanel) {
		min =  999999999.;
		max = -999999999.;
		datasetLabel.setText(var.getDSName()+": "+var.getName());
		GridSerializable ds_grid = var.getGrid();
		double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
		double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

		double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
		double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

		double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));

		LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(grid_south, grid_west), LatLng.newInstance(grid_north, grid_east));
		settingsButton.getRefMap().initDataBounds(bounds, delta, true);
		settingsButton.setOperations(rpcService, var.getIntervals(), var.getDSID(), var.getID(), op, view, null);
		tandzWidgets.removeAxes();
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
			dateTimeWidget.init(ds_grid.getTAxis(), false);
			dateTimeWidget.setVisible(true);
			tandzWidgets.getDateWidget().init(ds_grid.getTAxis(), false);
			tandzWidgets.addAxis("t");
			if ( compareAxis.equals("t") ) {
				grid.setWidget(2, 0, dateTimeWidget);
			}
		}
		if ( ds_grid.getZAxis() != null ) {
			zAxisWidget.init(ds_grid.getZAxis());
			zAxisWidget.setVisible(true);
			tandzWidgets.getZWidget().init(ds_grid.getZAxis());
			tandzWidgets.addAxis("z");
			if ( compareAxis.equals("z") ) {
				grid.setWidget(2, 0, zAxisWidget);
			}
		}
	}
	private void switchAxis() {
		String temp = compareAxis;
		compareAxis = fixedAxis;
		fixedAxis = temp;
		if ( compareAxis.equals("t") ) {
			String tlo = tandzWidgets.getDateWidget().getFerretDateLo();
			dateTimeWidget.setLo(tlo);
			grid.setWidget(2, 0, dateTimeWidget);
		} else {
			String zlo = tandzWidgets.getZWidget().getLo();
			zAxisWidget.setLo(zlo);
			grid.setWidget(2, 0, zAxisWidget);
		}
		if ( isUsePanelSettings() || singlePanel ) {
			grid.setWidget(2, 0, tandzWidgets);
		}
	}
	// The SliderSorter needs to pass in which axis it is controlling and the value it should have...
	public void refreshPlot(Map<String, String> options, boolean switchAxis, boolean popup) {
		if (switchAxis) {
			switchAxis();
		}
		if (popup) {
			spin.setPopupPosition(grid.getWidget(1,0).getAbsoluteLeft(), grid.getWidget(1,0).getAbsoluteTop());
			spin.show();
		}
		lasRequest = new LASRequestWrapper();
		lasRequest.removeRegion(0);
		lasRequest.removeVariables();
		lasRequest.removePropertyGroup("ferret");

		lasRequest.addVariable(var.getDSID(), var.getID());

		lasRequest.setOperation(op, "v7");
		lasRequest.setProperty("ferret", "view", view);
		lasRequest.setProperty("ferret", "size", ".8333");

		if ( var.getGrid().getTAxis() != null ) {
			if (isUsePanelSettings() || singlePanel) {
				lasRequest.setRange("t", tandzWidgets.getDateWidget().getFerretDateLo(), tandzWidgets.getDateWidget().getFerretDateHi(), 0);
			} else {
				lasRequest.setRange("t", dateTimeWidget.getFerretDateLo(), dateTimeWidget.getFerretDateHi(), 0);
			}
		}

		xlo = String.valueOf(settingsButton.getRefMap().getXlo());
		xhi = String.valueOf(settingsButton.getRefMap().getXhi());
		ylo = String.valueOf(settingsButton.getRefMap().getYlo());
		yhi = String.valueOf(settingsButton.getRefMap().getYhi());

		lasRequest.setRange("x", xlo, xhi, 0);
		lasRequest.setRange("y", ylo, yhi, 0);

		if ( var.getGrid().getZAxis() != null ) {
			if ( isUsePanelSettings() || singlePanel ) {
				lasRequest.setRange("z", tandzWidgets.getZWidget().getLo(), tandzWidgets.getZWidget().getHi(), 0);
			} else {
				lasRequest.setRange("z", zAxisWidget.getLo(), zAxisWidget.getHi(), 0);
			}
		}
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
		if ( fill_levels != null && !fill_levels.equals("") ) {
			lasRequest.addProperty("ferret", "fill_levels", fill_levels);
		}
		lasRequest.setProperty("product_server", "ui_timeout", "10");
		String url = productServer+"?xml="+URL.encode(lasRequest.getXMLText());
		RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
		try {
			sendRequest.sendRequest(null, lasRequestCallback);
		} catch (RequestException e) {
			HTML error = new HTML(e.toString());
			grid.setWidget(1, 0, error);
		}
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
									Button retry = new Button("Retry");
									retry.addClickListener(new ClickListener() {
										public void onClick(Widget sender) {
											// Put the selection widget back in place...
											grid.setWidget(1, 0, new HTML("Retrying..."));
											grid.setWidget(2, 0, getCompareWidget());
											// Just send the same request again to see if it works the second time.
											String url = productServer+"?xml="+URL.encode(lasRequest.getXMLText());
											RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
											try {
												sendRequest.sendRequest(null, lasRequestCallback);
											} catch (RequestException e) {
												HTML error = new HTML(e.toString());
												grid.setWidget(1, 0, error);
											}
										}

									});
									grid.setWidget(2, 0, retry);
								}
							}
						} else if ( result.getAttribute("type").equals("batch") ) {
							String elapsed_time = result.getAttribute("elapsed_time");
							HTML batch = new HTML(spinImage.getHTML()+"<br><br>Your request has been processing for "+elapsed_time+" seconds.<br>This panel will refresh automatically.<br><br>");
							grid.setWidget(1, 0, batch);
							lasRequest.setProperty("product_server", "ui_timeout", "3");
							String url = productServer+"?xml="+URL.encode(lasRequest.getXMLText());
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
	ChangeListener comparisonAxisChangeListener = new ChangeListener() {
		public void onChange(Widget sender) {
			if ( compareAxis.equals("t") ) {
				tandzWidgets.getDateWidget().setLo(dateTimeWidget.getFerretDateLo());
			} else {
				tandzWidgets.getZWidget().setLo(zAxisWidget.getLo());
			}
		}
	};
	public void setRegion(int i, String region) {
		settingsButton.getRefMap().setRegion(i, region);
	}
	TreeListener datasetTreeListener = new TreeListener() {

		public void onTreeItemSelected(TreeItem item) {
			Object v = item.getUserObject();
			if ( v instanceof VariableSerializable ) {
				nvar = (VariableSerializable) v;
				changeDataset = true;
			}
		}

		public void onTreeItemStateChanged(TreeItem item) {
			// TODO Auto-generated method stub

		}

	};
	public void setVariable(VariableSerializable v) {
		var = v;
	}
	public void setCompareAxis(String axis) {
		compareAxis = axis;
	}
	ClickListener applyPanelClick = new ClickListener() {
		public void onClick(Widget sender) {
			if (changeDataset) {			
				var = nvar;
				datasetLabel.setText(var.getDSName()+": "+var.getName());
				usePanel(true);
				changeDataset = false;
				init(true);
			}
			if (settingsButton.isUsePanelSettings()) {
				grid.setStyleName("panelSettingsColor");
				grid.setWidget(2, 0, tandzWidgets);
				setCompareAxisVisible(false);
			} else {
				grid.setStyleName("regularBackground");
				grid.setWidget(2, 0, getCompareWidget());
				setCompareAxisVisible(true);
			}	
		}
	};
	ClickListener closeClick = new ClickListener() {
		public void onClick(Widget sender) {
			setPanelMode();
		}
	};
	private void setPanelMode() {
		if (settingsButton.isUsePanelSettings() ) {
			grid.addStyleName("panelSettingsColor");
			grid.setWidget(2, 0, tandzWidgets);
			setCompareAxisVisible(false);
			if ( !singlePanel ) {
				top.setWidget(0, 2, revert);
			}
		} else {
			grid.setWidget(2, 0, getCompareWidget());
			if ( compareAxis.equals("t") ) {
				setAxisValue("t", dateTimeWidget.getFerretDateLo());
			} else if ( compareAxis.equals("z") ) {
				setAxisValue("z", zAxisWidget.getLo());
			}
			setCompareAxisVisible(true);
			grid.setStyleName("regularBackground");
			if ( !singlePanel ) {
				top.remove(revert);
			}
		}	
	}
	private Widget getCompareWidget() {
		if ( compareAxis.equals("t") ) {
			return dateTimeWidget;
		} else{
			return zAxisWidget;
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
	private void setCompareAxisVisible(boolean b) {
		Widget w = grid.getWidget(2, 0);
		if ( w instanceof DateTimeWidget ) {
			DateTimeWidget dt = (DateTimeWidget) w;
			dt.setVisible(b);
		} else if ( w instanceof AxisWidget ) {
			AxisWidget a = (AxisWidget) w;
			a.setVisible(b);
		}
	}
	public String getID() {
		return ID;
	}
	public void addCloseListener(ClickListener closeClick) {
		settingsButton.addCloseClickListener(closeClick);
	}
	public void addApplyListener(ClickListener applyClick) {
		settingsButton.addApplyClickListener(applyClick);
	}

	public String getThi() {
		return tandzWidgets.getDateWidget().getFerretDateHi();
	}
	public String getTlo() {
		return tandzWidgets.getDateWidget().getFerretDateLo();
	}

	public String getZhi() {
		return tandzWidgets.getZWidget().getHi();
	}
	public String getZlo() {
		return tandzWidgets.getZWidget().getLo();
	}

	public String getYhiFormatted() {
		return settingsButton.getRefMap().getYhiFormatted();
	}
	public String getYloFormatted() {
		return settingsButton.getRefMap().getYloFormatted();
	}
	public String getXhiFormatted() {
		return settingsButton.getRefMap().getXhiFormatted();
	}
	public String getXloFormatted() {
		return settingsButton.getRefMap().getXloFormatted();
	}

	public double getYhi() {
		return settingsButton.getRefMap().getYhi();
	}
	public double getYlo() {
		return settingsButton.getRefMap().getYlo();
	}
	public double getXhi() {
		return settingsButton.getRefMap().getXhi();
	}
	public double getXlo() {
		return settingsButton.getRefMap().getXlo();
	}

	public void setT(String tlo, String thi) {
		tandzWidgets.getDateWidget().setLo(tlo);
		// TODO also set the hi value
	}

	public void setZ(String zlo, String zhi) {
		tandzWidgets.getZWidget().setLo(zlo);
		// TODO also set hi if it's a range
	}

	public void setLatLon(String xlo, String xhi, String ylo, String yhi) {
		settingsButton.setLatLon(xlo, xhi, ylo, yhi);

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
	public void setPanelWidth(int width) {
		autoZoom = true;
		int max = (int) image_w;
		pwidth = Math.min(width,  max);
		setImageWidth();	
	}
	public void addRegionChangeListener(ChangeListener listener) {
		settingsButton.getRefMap().addRegionChangeListener(listener);
	}

	public void setParentAxisValue(String axis, String value) {
		if ( axis.equals("z") ) {
			zAxisWidget.setLo(value);
		} else {
			dateTimeWidget.setLo(value);
		}
	}
	public void setAxisValue(String axis, String value) {
		//TODO what if it's a range?
		if ( axis.equals("z") ) {
			tandzWidgets.getZWidget().setLo(value);
		} else {
			tandzWidgets.getDateWidget().setLo(value);
		}

	}
	public void setParentAxisRangeValues(String axis, String lo_value, String hi_value) {
		if ( axis.equals("z") ) {
			zAxisWidget.setLo(lo_value);
			zAxisWidget.setHi(hi_value);
		} else {
			dateTimeWidget.setLo(lo_value);
			dateTimeWidget.setHi(hi_value);
		}
	}
	public boolean isUsePanelSettings() {
		return settingsButton.isUsePanelSettings();
	}
	public void usePanel(boolean b) {
		settingsButton.setUsePanel(b);
		setPanelMode();
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


		if ( !view_in.equals(settingsButton.getOperationsWidget().getCurrentView()) ) {
			messagePanel.setPopupPosition(grid.getWidget(1, 0).getAbsoluteLeft()+15, grid.getWidget(1,0).getAbsoluteTop()+15);
			messagePanel.show();
			return;
		}
		spin.setPopupPosition(grid.getWidget(1,0).getAbsoluteLeft(), grid.getWidget(1,0).getAbsoluteTop());
		spin.show();
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
		if ( !xlo_in.equals("") && !xhi_in.equals("") ) {
			lasRequest.setRange("x", xlo_in, xhi_in, 0);
		}
		if ( !ylo_in.equals("") && !yhi_in.equals("") ) {
			lasRequest.setRange("y", ylo_in, yhi_in, 0);
		}
		if ( !zlo_in.equals("") && !zhi_in.equals("") ) {
			lasRequest.setRange("z", zlo_in, zhi_in, 0);
		}
		if ( !tlo_in.equals("") && !thi_in.equals("") ) {
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
				if ( var.getGrid().getXAxis() != null ) {
					lasRequest.setRange("x", String.valueOf(settingsButton.getRefMap().getXlo()), String.valueOf(settingsButton.getRefMap().getXhi()), 1);
				}
			}
			if ( !view.contains("y") ) {
				if ( var.getGrid().getYAxis() != null ) {
					lasRequest.setRange("y", String.valueOf(settingsButton.getRefMap().getYlo()), String.valueOf(settingsButton.getRefMap().getYhi()), 1);
				}
			}
			if ( !view.contains("z") ) {
				if ( var.getGrid().getZAxis() != null ) {
					lasRequest.setRange("z", tandzWidgets.getZWidget().getLo(), tandzWidgets.getZWidget().getHi(), 1);
				}
			}
			if ( !view.contains("t") ) {
				if ( var.getGrid().getTAxis() != null ) {
					lasRequest.setRange("t", tandzWidgets.getDateWidget().getFerretDateLo(), tandzWidgets.getDateWidget().getFerretDateHi(), 1);
				}
			}
		} else {
			if ( !view.contains("x") ) {
				if ( var.getGrid().getXAxis() != null ) {
					if ( compareAxis.equals("x") ) {
						//TODO get it from the local compare axis widget.
					} else {
						lasRequest.setRange("x", xlo_in, xhi_in, 1);
					}
				}
			}
			if ( !view.contains("y") ) {
				if ( var.getGrid().getYAxis() != null ) {
					if ( compareAxis.equals("y") ) {
						//TODO get it from the local compare axis widget.
					} else {
						lasRequest.setRange("y", ylo_in, yhi_in, 1);
					}
				}
			}
			if ( !view.contains("z") ) {
				if ( var.getGrid().getZAxis() != null ) {
					if ( compareAxis.equals("z") ) {
						// Use the panel's compare axis widget.
						lasRequest.setRange("z", zAxisWidget.getLo(), zAxisWidget.getHi(), 1);
					} else {
						//Use the fixed axis in the slide sorter (the passed in value) if it applies to this data set.
						if ( !zlo_in.equals("") && !zhi_in.equals("") ) {
							lasRequest.setRange("z", zlo_in, zhi_in, 1);
						}
					}				
				}
			}
			if ( !view.contains("t") ) {
				if ( var.getGrid().getTAxis() != null ) {
					if ( compareAxis.equals("t") ) {
						// Use the panel's compare axis widget.
						lasRequest.setRange("t", dateTimeWidget.getFerretDateLo(), dateTimeWidget.getFerretDateHi(), 1);
					} else {
						//Use the fixed axis in the slide sorter (the passed in value) if it applies to this data set.
						if ( !tlo_in.equals("") && !thi_in.equals("") ) {
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
		String url = productServer+"?xml="+URL.encode(lasRequest.getXMLText());
		RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
		try {
			sendRequest.sendRequest(null, lasRequestCallback);
		} catch (RequestException e) {
			HTML error = new HTML(e.toString());
			grid.setWidget(1, 0, error);
		}
	}
	public void addCompareAxisChangeListener(ChangeListener compareAxisChangeListener) {
		dateTimeWidget.addChangeListener(compareAxisChangeListener);
		zAxisWidget.addChangeListener(compareAxisChangeListener);
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
			setPanelMode();
		}
	};

	public void addRevertListener(ClickListener listener) {
		revert.addClickListener(listener);
	}
	public ClickListener optionsOkListener = new ClickListener() {
		public void onClick(Widget sender) {
			settingsButton.setUsePanel(true);
			setPanelMode();
		}
	};
	public ClickListener operationsClickListener = new ClickListener() {
		public void onClick(Widget sender) {
			if ( sender instanceof OperationButton ) {
				OperationButton o = (OperationButton) sender;
				view = settingsButton.getOperationsWidget().getCurrentView();
				op = settingsButton.getCurrentOp().getID();
				usePanel(true);
				if ( isUsePanelSettings() || singlePanel ) {
					if ( view.contains("t") ) {
						tandzWidgets.setRange("t", true);
					} else {
						tandzWidgets.setRange("t", false);
					}
					if ( view.contains("z") ) {
						tandzWidgets.setRange("z", true);
					} else {
						tandzWidgets.setRange("z", false);
					}
				} else {
					if ( compareAxis.equals("t") ) {
						if ( view.contains("t") ) {
							dateTimeWidget.setRange(true);
						} else {
							dateTimeWidget.setRange(false);
						}
					}
					if ( compareAxis.equals("z") ) {
						if ( view.contains("z") ) {
							zAxisWidget.setRange(true);
						} else {
							zAxisWidget.setRange(false);
						}
					}
				}
			}
		}	
	};

	public void setParentAxisRange(String type, boolean b) {
		if ( type.equals("t") ) {
			if ( isUsePanelSettings() || singlePanel ) {
				tandzWidgets.setRange("t", b);
			} else {
				dateTimeWidget.setRange(b);
			}
		}
		if ( type.equals("z") ) {
			if ( isUsePanelSettings() || singlePanel ) {
				tandzWidgets.setRange("z", b);
			} else {
				zAxisWidget.setRange(b);
			}
		}
	}
	public void setOperation(String id, String v) {
		settingsButton.setOperation(id, v);	
		op = settingsButton.getCurrentOp().getID();
		view = settingsButton.getCurrentOperationView();
		settingsButton.setToolType(view);
	}


	public void addZChangeListner(ChangeListener zChangeListener) {
		tandzWidgets.addZChangeListener(zChangeListener);
	}

	public void addTChangeListner(ChangeListener tChangeListener) {
		tandzWidgets.addTChangeListener(tChangeListener);
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
	}
	public void show() {
		top.setVisible(true);
		//grid.getCellFormatter().setHeight(0, 0, "90px");
	}
	public String getHistoryToken() {
		StringBuilder token = new StringBuilder();
		token.append("usePanelSettings="+settingsButton.isUsePanelSettings());
		if ( compareAxis.equals("t") ) {
			token.append(";compareAxis=t;compareAxisLo="+dateTimeWidget.getFerretDateLo()+";compareAxisHi="+dateTimeWidget.getFerretDateHi());
		} else if ( compareAxis.equals("z") ) {
			token.append(";compareAxis=z;compareAxisLo="+zAxisWidget.getLo()+";compareAxisHi="+zAxisWidget.getHi());
		}
		token.append(";dsid="+var.getDSID());
		token.append(";varid="+var.getID());
		return token.toString();
	}
	public String getSettingsWidgetHistoryToken() {
		return settingsButton.getHistoryToken();
	}
	public void setFromHistoryToken(Map<String, String> tokenMap, Map<String, String> optionsMap) {		
		// Do the panel stuff here.
		if (tokenMap.get("compareAxis").equals("t") ) {
			dateTimeWidget.setLo(tokenMap.get("compareAxisLo"));
			dateTimeWidget.setHi(tokenMap.get("compareAxisHi"));
		} else {
			zAxisWidget.setLo(tokenMap.get("compareAxisLo"));
			zAxisWidget.setHi(tokenMap.get("compareAxisHi"));
		}
		setLatLon(tokenMap.get("xlo"), tokenMap.get("xhi"), tokenMap.get("ylo"), tokenMap.get("yhi"));
		if (isUsePanelSettings()) {
		    settingsButton.setFromHistoryToken(tokenMap, optionsMap);
		}
		
	}
	
}
