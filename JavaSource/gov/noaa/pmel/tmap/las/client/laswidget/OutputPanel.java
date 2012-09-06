package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.AppConstants;
import gov.noaa.pmel.tmap.las.client.BaseUI.Mouse;
import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.activity.OutputControlPanelActivity;
import gov.noaa.pmel.tmap.las.client.event.CancelEvent;
import gov.noaa.pmel.tmap.las.client.event.FeatureModifiedEvent;
import gov.noaa.pmel.tmap.las.client.event.LASRequestEvent;
import gov.noaa.pmel.tmap.las.client.event.LASResponseEvent;
import gov.noaa.pmel.tmap.las.client.event.MapChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.OperationChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.StringValueChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.UpdateFinishedEvent;
import gov.noaa.pmel.tmap.las.client.event.VariableSelectionChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.WidgetSelectionChangeEvent;
import gov.noaa.pmel.tmap.las.client.map.MapSelectionChangeListener;
import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.AnalysisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.ui.IESafeImage;
import gov.noaa.pmel.tmap.las.client.util.URLUtil;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
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
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

/**
 * This is a GWT Composite object that knows how to initialize itself from a
 * dsid, varid, operation and view, how to build menus for the orthogonal axes
 * and how to refresh itself when those menus change.
 * 
 * @author rhs
 * 
 */
public class OutputPanel extends Composite implements HasName {
	private static final AppConstants CONSTANTS = GWT
			.create(AppConstants.class);

	/**
	 * Evaluate scripts in an HTML string. Will eval both <script
	 * src=""></script> and <script>javascript here</scripts>.
	 * 
	 * @param element
	 *            a new HTML(text).getElement()
	 */
	public static native void evalScripts(
			com.google.gwt.user.client.Element element)
	/*-{
	  var scripts = element.getElementsByTagName("script");
	
	  for (i=0; i < scripts.length; i++) {
	  // if src, eval it, otherwise eval the body
	  if (scripts[i].hasAttribute("src")) {
	  var src = scripts[i].getAttribute("src");
	  var script = $doc.createElement('script');
	  script.setAttribute("src", src);
	  $doc.getElementsByTagName('body')[0].appendChild(script);
	  } else {
	  $wnd.eval(scripts[i].innerHTML);
	  }
	  }
	}-*/;

	/*
	 * An object that when set causes the request to contain an analysis
	 * request. It should be null when no analysis is active.
	 */
	AnalysisSerializable analysis = null;
	AnalysisWidget analysisWidget;
	boolean autoZoom = true;

	// Have a cancel button ready to go for this panel.
	CancelButton cancelButton;
	CancelEvent.Handler cancelRequestHandler = new CancelEvent.Handler() {

		@Override
		public void onCancel(CancelEvent event) {
			if (event.getID().equals(ID)) {
				currentURL = currentURL + "&cancel=true";
				RequestBuilder sendRequest = new RequestBuilder(
						RequestBuilder.GET, currentURL);
				try {
					lasAnnotationsPanel.setTitle("Plot Annotations");
					lasAnnotationsPanel
							.setError("Fetching plot annotations...");
					updating = true;
					sendRequest.sendRequest(null, lasRequestCallback);
				} catch (RequestException e) {
					Window.alert("Unable to cancel request.");
				}
			}
		}
	};

	/**
	 * True if a new data set has been selected with this OutputPanel's data set
	 * button (in it's {@link OutputControlPanel}). This allows the data set
	 * change to be delayed until the "Update Plot" button is pressed. As a
	 * consequence, changeDataset is only set to true when the local data set
	 * button is used, not when the main data set button is used.
	 * 
	 * Accessed and modified even with in this class only by methods so the
	 * correct events get fired.
	 */
	private boolean changeDataset = false;

	private ClientFactory clientFactory = GWT.create(ClientFactory.class);

	// Keep track of the values that are currently being used as the fixed axis
	// and compare axis
	String compareAxis;

	// Switch for the first panel in a multi-panel gallery. Controls all other
	// panels.
	boolean comparePanel = false;

	// view
	String comparePanelState; // These are the values for the axes that are in

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
			// This is only for the history state...

			if (ngrid == null) {
				Window.alert("Still fetching grid for new variable.");
				return;
			}

			if (isChangeDataset()) {
				setVariable(nvar);
				setChangeDataset(false);

			} else {
				if (!nvar.getID().equals(var.getID())) {
					setVariable(nvar);
				}
			}

			boolean pushHistory = true;
			if (historyTokenMap != null) {
				setOrthogonalAxesValues();
				setExtraVariables();
				waitingForHistory = false;
				pushHistory = false;
			} else {
				// If not a history event, handle this ourselves.
				panelAxesWidgets.showOrthoAxes(view, ortho, null,
						isComparePanel());
			}
			if (!waitingForHistory) {
				if (!isComparePanel()) {
					getOutputControlPanel().getVariableControls()
							.getMultiVariableSelector().getVariableSelector()
							.getLatestListBox().setAddButtonVisible(false);
				}
				// Make the update happen automatically, do not force a panel to
				// update, push the history only if it's not a history event.
				eventBus.fireEvent(new WidgetSelectionChangeEvent(false, false,
						pushHistory));
			}
		}

	};

	String containerType = Constants.FRAME;

	/* Keep track of the URL that will give the "print" page... */
	String currentPrintURL = "";
	// The current Product URL being displayed in this frame.
	String currentURL = "";
	/*
	 * A a button that pops up a panel for region selection, operation and
	 * (still to be implemented) plot options.
	 */
	DatasetButton datasetButton;

	ClickHandler datasetCloseHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {

			panelAxesWidgets.restorePanels();

		}

	};

	/* The current data set and variable. */
	Label datasetLabel;

	ClickHandler datasetOpenHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {

			panelAxesWidgets.closePanels();

		}

	};
	SelectionHandler<TreeItem> datasetSelctionHandler = new SelectionHandler<TreeItem>() {
		@Override
		public void onSelection(SelectionEvent<TreeItem> event) {
			DatasetWidget datasetWidget = datasetButton.getDatasetWidget();
			boolean isFromMyDatasetWidget = event.getSource().equals(
					datasetWidget);
			if (isFromMyDatasetWidget) {
				TreeItem item = event.getSelectedItem();
				Object variableUserObject = item.getUserObject();
				if (variableUserObject instanceof VariableSerializable) {
					VariableSerializable variable = (VariableSerializable) variableUserObject;
					applyVariableChange(variable, true);
				}
			}
		}
	};

	// Keep track of the difference state in case we change data sets.
	boolean difference = false;
	VariableSerializable differenceFromVariable;

	boolean draw = false;
	int endx;
	int endy;

	private EventBus eventBus;
	private FeatureModifiedEvent.Handler featureModifiedHandler = new FeatureModifiedEvent.Handler() {

		@Override
		public void onFeatureModified(FeatureModifiedEvent event) {
			OLMapWidget m = (OLMapWidget) event.getSource();
			if (panelAxesWidgets.getRefMap().equals(m)) {
				// Event from this map, re-fire it as a map change.
				eventBus.fireEventFromSource(new MapChangeEvent(event.getYlo(),
						event.getYhi(), event.getXlo(), event.getXhi()),
						thisOutputPanel);
			}

		}
	};

	// Keep track of the auto contour levels from the gallery.
	String fill_levels;

	String fixedAxis;
	String fixedAxisValue;

	int fixedZoom;

	Canvas frontCanvas;

	// Canvas to allow drawing on the plot for zooming.
	Context2d frontCanvasContext;

	double globalMax = -99999999.;
	double globalMin = 99999999.;

	/* The base widget used to layout the panel. A single column of three rows. */
	FlexTable grid;
	public AsyncCallback<CategorySerializable[]> historyDatasetCallback = new AsyncCallback<CategorySerializable[]>() {

		@Override
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes VizGal." + caught.toString());
		}

		@Override
		public void onSuccess(CategorySerializable[] result) {
			CategorySerializable[] cats = result;
			if (cats.length > 1) {
				Window.alert("Error getting variables for this dataset.");
			} else {
				if (cats[0].isVariableChildren()) {
					Vector<VariableSerializable> vars = cats[0]
							.getDatasetSerializable()
							.getVariablesSerializableAsVector();
					nvar = cats[0].getVariable(historyTokenMap.get("varid"));
					getOutputControlPanel().getVariableControls()
							.getMultiVariableSelector()
							.setVariables(vars, vars.indexOf(nvar));
					if (nvar.getDSID().equals(var.getDSID())) {
						setChangeDataset(false);
					} else {
						setChangeDataset(true);
					}
					Util.getRPCService().getConfig(null,
							historyTokenMap.get("dsid"),
							historyTokenMap.get("varid"), configCallback);
				} else {
					Window.alert("No variables found in this category");
				}
			}
		}
	};;

	private Map<String, String> historyOptionsMap;
	// Keep track of the passed in history for use after each server call back
	// in the cascade of calls needed to set the state from the history.
	private Map<String, String> historyTokenMap = null;

	String ID;

	double image_h = 631.;

	double image_w = 998.;

	ErrorHandler imageErrorHandler = new ErrorHandler() {
		@Override
		public void onError(ErrorEvent event) {
			eventBus.fireEventFromSource(new StringValueChangeEvent(null), this);
		}
	};

	LoadHandler imageLoadHandler = new LoadHandler() {

		@Override
		public void onLoad(LoadEvent event) {
			frontCanvasContext.drawImage(
					ImageElement.as(plotImage.getElement()), 0, 0);
			// No need to call setPlotImageWidth as a controller
			// that listens to the new
			// StringValueChangeEvent(plotImage.getUrl()) will eventually
			// do so
			// setPlotImageWidth();
			frontCanvas.addMouseDownHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					if (!view.equals("t")) {
						outx = false;
						outy = false;
						startx = event.getX();
						starty = event.getY();
						if (startx > x_offset_from_left
								&& starty > y_offset_from_top
								&& startx < x_offset_from_left + x_plot_size
								&& starty < y_offset_from_top + y_plot_size) {

							draw = true;
							drawToScreen(scaledImage); // frontCanvasContext.drawImage(ImageElement.as(plotImage.getElement()),
							// 0, 0);
							double scaled_x_per_pixel = x_per_pixel
									/ imageScaleRatio;
							double scaled_y_per_pixel = y_per_pixel
									/ imageScaleRatio;
							world_startx = x_axis_lower_left
									+ (startx - x_offset_from_left
											* imageScaleRatio)
									* scaled_x_per_pixel;
							world_starty = y_axis_lower_left
									+ ((y_image_size * imageScaleRatio - starty) - y_offset_from_bottom
											* imageScaleRatio)
									* scaled_y_per_pixel;

							world_endx = world_startx;
							world_endy = world_starty;
						}
					} else {
						draw = false;
					}
				}
			});
			frontCanvas.addMouseMoveHandler(new MouseMoveHandler() {

				@Override
				public void onMouseMove(MouseMoveEvent event) {
					if (view.equals("t"))
						draw = false;
					int currentx = event.getX();
					int currenty = event.getY();
					// If you drag it out, we'll stop drawing.

					if (currentx < x_offset_from_left
							|| currenty < y_offset_from_top
							|| currentx > x_offset_from_left + x_plot_size
							|| currenty > y_offset_from_top + y_plot_size) {

						endx = currentx;
						endy = currenty;

						// Set the limits for one last drawing of the selection
						// rectangle.
						if (currentx < x_offset_from_left) {
							endx = x_offset_from_left;
							currentx = x_offset_from_left;
							outx = true;
						}
						if (currenty < y_offset_from_top) {
							endy = y_offset_from_top;
							currenty = y_offset_from_top;
							outy = true;
						}
						if (currentx > x_offset_from_left + x_plot_size) {
							endx = x_offset_from_left + x_plot_size;
							currentx = x_offset_from_left + x_plot_size;
							outx = true;
						}
						if (currenty > y_offset_from_top + y_plot_size) {
							endy = y_offset_from_top + y_plot_size;
							currenty = y_offset_from_top + y_plot_size;
							outy = true;
						}
					}
					if (draw) {
						double scaled_x_per_pixel = x_per_pixel
								/ imageScaleRatio;
						double scaled_y_per_pixel = y_per_pixel
								/ imageScaleRatio;
						world_endx = x_axis_lower_left
								+ (currentx - x_offset_from_left
										* imageScaleRatio) * scaled_x_per_pixel;
						world_endy = y_axis_lower_left
								+ ((y_image_size * imageScaleRatio - currenty) - y_offset_from_bottom
										* imageScaleRatio) * scaled_y_per_pixel;
						frontCanvasContext.setFillStyle(randomColor);
						drawToScreen(scaledImage); // frontCanvasContext.drawImage(ImageElement.as(plotImage.getElement()),
						// 0, 0);
						frontCanvasContext.fillRect(startx, starty, currentx
								- startx, currenty - starty);
						for (Iterator<Mouse> mouseIt = mouseMoves.iterator(); mouseIt
								.hasNext();) {
							Mouse mouse = mouseIt.next();
							double minx = Math.min(world_startx, world_endx);
							double maxx = Math.max(world_startx, world_endx);
							double miny = Math.min(world_starty, world_endy);
							double maxy = Math.max(world_starty, world_endy);
							if (axisVertical.equals("y")
									&& axisHorizontal.equals("x")) {
								mouse.updateMap(miny, maxy, minx, maxx);
							} else if (axisVertical.equals("x")
									&& axisHorizontal.equals("y")) {
								mouse.updateMap(minx, maxx, miny, maxy);
							} else if (axisVertical.equals("y")
									&& !axisHorizontal.equals("x")) {
								mouse.updateLat(miny, maxy);
							} else if (axisVertical.equals("x")
									&& !axisHorizontal.equals("y")) {
								mouse.updateLon(miny, maxy);
							} else if (!axisVertical.equals("y")
									&& axisHorizontal.equals("x")) {
								mouse.updateLon(minx, maxx);
							} else if (!axisVertical.equals("x")
									&& axisHorizontal.equals("y")) {
								mouse.updateLat(minx, maxx);
							}
						}
					}
					if (outx && outy) {
						draw = false;
					}
				}
			});
			frontCanvas.addMouseUpHandler(new MouseUpHandler() {

				@Override
				public void onMouseUp(MouseUpEvent arg0) {
					double minx = Math.min(world_startx, world_endx);
					double maxx = Math.max(world_startx, world_endx);
					double miny = Math.min(world_starty, world_endy);
					double maxy = Math.max(world_starty, world_endy);
					if (axisVertical.equals("z")) {
						for (Iterator<Mouse> mouseIt = mouseMoves.iterator(); mouseIt
								.hasNext();) {
							Mouse mouse = mouseIt.next();
							mouse.setZ(miny, maxy);
						}
					}
					if (axisHorizontal.equals("z")) {
						for (Iterator<Mouse> mouseIt = mouseMoves.iterator(); mouseIt
								.hasNext();) {
							Mouse mouse = mouseIt.next();
							mouse.setZ(minx, maxx);
						}
					}
					if (!view.equals("t")) {
						eventBus.fireEvent(new WidgetSelectionChangeEvent(false));
					}
				}
			});
			grid.setWidget(1, 0, frontCanvas);
			logger.info("imageLoadHandler firing StringValueChangeEvent");
			eventBus.fireEventFromSource(
					new StringValueChangeEvent(plotImage.getUrl()),
					thisOutputPanel);
		}

	};

	double imageScaleRatio; // Keep track of the factor by which the image has
							// been scaled.

	// The panel with the plot annotations hidden at the top of the frame...
	LASAnnotationsPanel lasAnnotationsPanel;

	/*
	 * This is the request. It is either built from scratch or passed in via the
	 * constructor.
	 */
	LASRequest lasRequest = new LASRequest();

	private RequestCallback lasRequestCallback = new RequestCallback() {
		@Override
		public void onError(Request request, Throwable exception) {
			currentURL = currentURL + "&error=true";
			logger.warning("enterring lasRequestCallback.onError request:"
					+ request + "\nexception:" + exception);
			spin.hide();
			updating = false;
			HTML error = new HTML(exception.toString());
			Widget size = grid.getWidget(1, 0);
			error.setSize(image_w * imageScaleRatio + "px", image_h
					* imageScaleRatio + "px");
			grid.setWidget(1, 0, error);
			if (pending) {
				pending = false;
			}
			logger.warning("exiting lasRequestCallback.onError");
		}

		@Override
		public void onResponseReceived(Request request, Response response) {
			logger.info("enterring lasRequestCallback.onResponseReceived request:"
					+ request + "\nresponse:" + response);
			eventBus.fireEventFromSource(new UpdateFinishedEvent(),
					OutputPanel.this);
			updating = false;
			String doc = response.getText();
			spin.hide();
			// Look at the doc. If it's not obviously XML, treat it as HTML.
			if (!doc.substring(0, 100).contains("<?xml")) {
				logger.info("!doc.substring(0, 100).contains(\"<?xml\")");
				currentURL = currentURL + "&error=true";
				evalScripts(new HTML(response.getText()).getElement());
				VerticalPanel p = new VerticalPanel();
				ScrollPanel sp = new ScrollPanel();
				HTML result = new HTML(doc);
				p.add(result);
				HTML again = new HTML(
						"<b>Push the Update Plots button when you're ready to make your next request.");
				p.add(again);
				sp.add(p);
				sp.setSize(image_w + "px", image_h + "px");
				grid.setWidget(1, 0, sp);
			} else {
				logger.info("doc.substring(0, 100).contains(\"<?xml\")");
				doc = doc.replaceAll("\n", "").trim();
				Document responseXML = XMLParser.parse(doc);
				NodeList results = responseXML.getElementsByTagName("result");
				String annourl = "";
				String imageurl = "";
				boolean image_ready = false;
				for (int n = 0; n < results.getLength(); n++) {
					if (results.item(n) instanceof Element) {
						logger.info("working on result n:" + n);
						Element result = (Element) results.item(n);
						String typeAttribute = result.getAttribute("type");
						if (typeAttribute == null)
							typeAttribute = "null";
						logger.info("result.getAttribute(\"type\"):"
								+ typeAttribute);
						if (typeAttribute.equals("image")) {
							// HTML image = new
							// HTML("<a target=\"_blank\" href=\""+result.getAttribute("url")+"\"><img width=\"100%\" src=\""+result.getAttribute("url")+"\"></a>");
							image_ready = true;
							imageurl = result.getAttribute("url");
						} else if (typeAttribute.equals("annotations")) {
							annourl = result.getAttribute("url");
							lasAnnotationsPanel.setAnnotationsHTMLURL(Util
									.getAnnotationService(annourl)
									+ "&catid="
									+ var.getDSID());
						} else if (typeAttribute.equals("map_scale")) {
							NodeList map_scale = result
									.getElementsByTagName("map_scale");
							for (int m = 0; m < map_scale.getLength(); m++) {
								if (map_scale.item(m) instanceof Element) {
									Element map = (Element) map_scale.item(m);
									NodeList children = map.getChildNodes();
									for (int l = 0; l < children.getLength(); l++) {
										if (children.item(l) instanceof Element) {
											Element child = (Element) children
													.item(l);
											if (child.getNodeName().equals(
													"x_image_size")) {
												x_image_size = getNumber(child
														.getFirstChild());
											} else if (child.getNodeName()
													.equals("y_image_size")) {
												y_image_size = getNumber(child
														.getFirstChild());
											} else if (child.getNodeName()
													.equals("x_plot_size")) {
												x_plot_size = getNumber(child
														.getFirstChild());
											} else if (child.getNodeName()
													.equals("y_plot_size")) {
												y_plot_size = getNumber(child
														.getFirstChild());
											} else if (child
													.getNodeName()
													.equals("x_offset_from_left")) {
												x_offset_from_left = getNumber(child
														.getFirstChild());
											} else if (child
													.getNodeName()
													.equals("y_offset_from_bottom")) {
												y_offset_from_bottom = getNumber(child
														.getFirstChild());
											} else if (child
													.getNodeName()
													.equals("x_offset_from_right")) {
												x_offset_from_right = getNumber(child
														.getFirstChild());
											} else if (child
													.getNodeName()
													.equals("y_offset_from_top")) {
												y_offset_from_top = getNumber(child
														.getFirstChild());
											} else if (child
													.getNodeName()
													.equals("x_axis_lower_left")) {
												x_axis_lower_left = getDouble(child
														.getFirstChild());
											} else if (child
													.getNodeName()
													.equals("y_axis_lower_left")) {
												y_axis_lower_left = getDouble(child
														.getFirstChild());
											} else if (child
													.getNodeName()
													.equals("x_axis_upper_right")) {
												x_axis_upper_right = getDouble(child
														.getFirstChild());
											} else if (child
													.getNodeName()
													.equals("y_axis_upper_right")) {
												y_axis_upper_right = getDouble(child
														.getFirstChild());
											} else if (child.getNodeName()
													.equals("data_min")) {
												globalMin = getDouble(child
														.getFirstChild());
											} else if (child.getNodeName()
													.equals("data_max")) {
												globalMax = getDouble(child
														.getFirstChild());
											} else if (child.getNodeName()
													.equals("axis_horizontal")) {
												axisHorizontal = getString(child
														.getFirstChild());
											} else if (child.getNodeName()
													.equals("axis_vertical")) {
												axisVertical = getString(child
														.getFirstChild());
											} else if (child
													.getNodeName()
													.equals("axis_vertical_positive")) {
												axisVerticalPositive = getString(child
														.getFirstChild());
											}
										}
									}
								}
							}
						} else if (typeAttribute.equals("error")) {
							/*
							 * Unfortunately, we cannot provide what you have
							 * asked for. A remote server was unable to deliver
							 * the data LAS needs to make your product.
							 * 
							 * 
							 * LAS was trying to access these servers:
							 * 
							 * http://iridl.ldeo.columbia.edu/SOURCES/.NOAA/.NCEP
							 * /.EMC/.CMB/.Pacific/.monthly/dods
							 * 
							 * Advanced users may see more technical
							 * information. Try your request again.
							 * 
							 * Email to the site administrator.
							 */

							if (result.getAttribute("ID").equals("las_message")) {
								logger.info("result.getAttribute(\"ID\").equals(\"las_message\"");
								Node text = result.getFirstChild();
								if (text instanceof Text) {
									Text t = (Text) text;
									HTML error = new HTML(t.getData()
											.toString().trim());
									error.setSize(image_w * imageScaleRatio
											+ "px", image_h * imageScaleRatio
											+ "px");
									grid.setWidget(1, 0, error);
								}
							}
						} else if (typeAttribute.equals("batch")) {
							String elapsed_time = result
									.getAttribute("elapsed_time");
							cancelButton.setTime(Integer.valueOf(elapsed_time));
							cancelButton.setSize(image_w * imageScaleRatio
									+ "px", image_h * imageScaleRatio + "px");
							grid.setWidget(1, 0, cancelButton);
							lasRequest.setProperty("product_server",
									"ui_timeout", "3");
							String url = Util.getProductServer() + "?xml="
									+ URL.encode(lasRequest.toString());
							if (currentURL.contains("cancel"))
								url = url + "&cancel=true";
							RequestBuilder sendRequest = new RequestBuilder(
									RequestBuilder.GET, url);
							try {
								updating = true;
								sendRequest.setHeader("Pragma", "no-cache");
								sendRequest.setHeader("cache-directive",
										"no-cache");
								// These are needed or IE will cache and make
								// infinite requests that always return 304
								sendRequest.setHeader("If-Modified-Since",
										new Date().toString());
								sendRequest.setHeader("max-age", "0");
								logger.info("Pragma:"
										+ sendRequest.getHeader("Pragma"));
								logger.info("cache-directive:"
										+ sendRequest
												.getHeader("cache-directive"));
								logger.info("max-age:"
										+ sendRequest.getHeader("max-age"));
								logger.info("If-Modified-Since:"
										+ sendRequest
												.getHeader("If-Modified-Since"));
								logger.info("calling sendRequest with url:"
										+ url);
								sendRequest.sendRequest(null,
										lasRequestCallback);
							} catch (RequestException e) {
								HTML error = new HTML(e.toString());
								error.setSize(image_w * imageScaleRatio + "px",
										image_h * imageScaleRatio + "px");
								grid.setWidget(1, 0, error);
							}
						}
					}
				}
				if (axisVerticalPositive != null
						&& axisVerticalPositive.equalsIgnoreCase("down")
						&& axisVertical.equals("z")) {
					double t = y_axis_lower_left;
					y_axis_lower_left = y_axis_upper_right;
					y_axis_upper_right = t;
				}
				if (image_ready) {
					image_w = x_image_size;
					image_h = y_image_size;

					// set the canvas with the image and get to drawin'

					if (!imageurl.equals("")) {
						currentPrintURL = Util.getAnnotationsFrag(annourl,
								imageurl);
						plotImage = new IESafeImage(imageurl);
						x_per_pixel = (x_axis_upper_right - x_axis_lower_left)
								/ Double.valueOf(x_plot_size);
						y_per_pixel = (y_axis_upper_right - y_axis_lower_left)
								/ Double.valueOf(y_plot_size);

						if (frontCanvas != null) {
							gridSetWidgetPlotImageAndHide();
							plotImage.addLoadHandler(imageLoadHandler);
							plotImage.addErrorHandler(imageErrorHandler);
							frontCanvas.addMouseUpHandler(new MouseUpHandler() {

								@Override
								public void onMouseUp(MouseUpEvent event) {
									// If we're still drawing when the mouse
									// goes up, record the position.
									if (draw) {
										endx = event.getX();
										endy = event.getY();
									}
									draw = false;
									outx = false;
									outy = false;
									for (Iterator<Mouse> mouseIt = mouseMoves
											.iterator(); mouseIt.hasNext();) {
										Mouse mouse = mouseIt.next();
										mouse.applyNeeded();
									}
								}
							});
							lasAnnotationsPanel.setPopupWidth(getPlotWidth());
						} else {
							// Browser cannot handle a canvas tag, so just put
							// up the image.
							// The old stuff if there is no canvas...
							currentPrintURL = Util.getAnnotationsFrag(annourl,
									imageurl);
							setImage(imageurl, Util.getAnnotationsService(
									annourl, imageurl));
							// No need to call setPlotImageWidth as a controller
							// that listens to the new
							// StringValueChangeEvent(imageurl) will eventually
							// do so
							// setPlotImageWidth();
						}
						logger.info("onResponseReceived firing StringValueChangeEvent");
						eventBus.fireEventFromSource(
								new StringValueChangeEvent(imageurl),
								thisOutputPanel);
					}
					world_startx = x_axis_lower_left;
					world_endx = x_axis_upper_right;
					world_starty = y_axis_lower_left;
					world_endy = y_axis_upper_right;
				}
			}
			if (pending) {
				pending = false;
				eventBus.fireEvent(new WidgetSelectionChangeEvent(false));
			}
			logger.info("exiting lasRequestCallback.onResponseReceived");
		}
	};

	private static final Logger logger = Logger.getLogger(OutputPanel.class
			.getName());

	private MapChangeEvent.Handler mapChangeHandler = new MapChangeEvent.Handler() {

		@Override
		public void onMapSelectionChange(MapChangeEvent event) {
			OutputPanel p = (OutputPanel) event.getSource();
			if (p.isComparePanel() && !isComparePanel()) {
				if (view.contains("x") && !view.contains("y")) {
					panelAxesWidgets.getRefMap().setCurrentSelection(
							panelAxesWidgets.getRefMap().getYlo(),
							panelAxesWidgets.getRefMap().getYhi(),
							event.getXlo(), event.getXhi());
				} else if (!view.contains("x") && view.contains("y")) {
					panelAxesWidgets.getRefMap().setCurrentSelection(
							event.getYlo(), event.getYhi(),
							panelAxesWidgets.getRefMap().getXlo(),
							panelAxesWidgets.getRefMap().getXhi());
				}
			}
		}
	};

	// public AsyncCallback<VariableSerializable> variableCallback = new
	// AsyncCallback<VariableSerializable>() {
	//
	// @Override
	// public void onFailure(Throwable caught) {
	//
	// Window.alert("Could not initialize panel from URL history.");
	//
	// }
	//
	// @Override
	// public void onSuccess(VariableSerializable result) {
	// nvar = result;
	// Util.getRPCService().getConfig(null, nvar.getDSID(), nvar.getID(),
	// configCallback);
	// }
	//
	// };
	public MapSelectionChangeListener mapListener = new MapSelectionChangeListener() {

		@Override
		public void onFeatureChanged() {

			eventBus.fireEventFromSource(new MapChangeEvent(panelAxesWidgets
					.getRefMap().getYlo(), panelAxesWidgets.getRefMap()
					.getYhi(), panelAxesWidgets.getRefMap().getXlo(),
					panelAxesWidgets.getRefMap().getXhi()), thisOutputPanel);
			eventBus.fireEventFromSource(new WidgetSelectionChangeEvent(false),
					thisOutputPanel);

		}

	};

	/* A message window when the plot cannot be made... */
	MessagePanel messagePanel;
	List<Mouse> mouseMoves = new ArrayList<Mouse>();
	// The new grid.
	GridSerializable ngrid;

	// The new variable.
	VariableSerializable nvar;
	OperationChangeEvent.Handler operationChangeHandler = new OperationChangeEvent.Handler() {

		@Override
		public void onOperationChange(OperationChangeEvent event) {
			int min = event.getMinVars();
			int max = event.getMaxVars();
			VariableSelector vs = outputControlPanel.getVariableControls()
					.getMultiVariableSelector().getVariableSelector();
			List<UserListBox> boxes = vs.getListBoxes();

			if (boxes.size() >= min && boxes.size() <= max)
				return;
			if (min == 1 && max == 1) {
				// Handle the most likely case first separately
				vs.removeExtraListBoxes(true);
			} else {
				if (boxes.size() < min) {
					Window.alert("This operation requires more variables.  Add variables until you have "
							+ min + " selected.");
				} else if (boxes.size() > max) {
					while (boxes.size() > max) {
						UserListBox b = boxes.get(boxes.size() - 1);
						vs.removeListBox(b);
					}
				}
			}
		}
	};
	String operationID;
	// Keep track of the current operations set
	OperationSerializable[] ops;
	/*
	 * Keep track of the optionID, view and operation. These are passed in as
	 * parameters when the pane is created.
	 */
	String optionID;
	// Keep track of the axes that are currently orthogonal to the plot.
	List<String> ortho;
	Frame output = new Frame();
	private OutputControlPanel outputControlPanel;
	boolean outx = false;
	boolean outy = false;
	/*
	 * The copy of the axes for this panel.
	 */
	AxesWidgetGroup panelAxesWidgets;
	// Set to true if there is a request for refresh that comes in while the
	// panel is updating...
	boolean pending = false;
	Image plotImage = null;
	// Keep track of the global values from the compare panel (upper left) so we
	// can revert
	String prePanelModeState;
	VariableSerializable prePanelModeVariable;
	// Some information to control the size of the image as the browser window
	// changes size.
	int pwidth;
	CssColor randomColor;

	ImageData scaledImage; // The scaled image data.
	boolean singlePanel;
	// Some widgets to show when a panel is being refreshed.
	PopupPanel spin;
	Image spinImage;
	// Drawing start position
	int startx = -1;
	int starty = -1;
	private final OutputPanel thisOutputPanel = this;

	// Set to true if there is an outstanding product request...
	boolean updating = false;

	/* The current variable in this panel. */
	VariableSerializable var;
	String view;
	// Keep track of the current vizGal state when you are in panel mode.
	String vizGalState; // These are the values for the axes that are in the

	// the compare panel (orthogonal to the view, but
	// for the comparison variable only)
	VariableSerializable vizGalVariable;
	boolean waitingForHistory = false;

	protected double world_endx;

	protected double world_endy;
	protected double world_startx;

	protected double world_starty;

	protected double x_axis_lower_left;
	protected double x_axis_upper_right;

	// Drawing parameters from the map scale response
	protected int x_image_size;

	protected int x_offset_from_left;

	// Listen for variable changes to this OutputPanel

	protected int x_offset_from_right;

	protected double x_per_pixel;

	protected int x_plot_size;

	protected double y_axis_lower_left;

	protected double y_axis_upper_right;

	protected int y_image_size;

	protected int y_offset_from_bottom;

	protected int y_offset_from_top;

	protected double y_per_pixel;

	protected int y_plot_size;

	protected String axisHorizontal;

	protected String axisVertical;

	protected String axisVerticalPositive;
	private LASResponseEvent.Handler lasResponseEventHandler = new LASResponseEvent.Handler() {
		@Override
		public void onResponse(LASResponseEvent event) {
			try {
				String thisOutputPanelName = getName();
				if ((event != null)
						&& (thisOutputPanelName != null)
						&& (event.getCallerObjectName()
								.equalsIgnoreCase(thisOutputPanelName))) {
					logger.info(thisOutputPanelName
							+ " has accepted LASResponseEvent event:" + event);
					boolean isResponseReceived = event.isResponseReceived();
					logger.info("event.isResponseReceived():"
							+ isResponseReceived);
					if (isResponseReceived) {
						if (event.getCallbackObjectName().equals(
								"lasRequestCallback")) {
							lasRequestCallback.onResponseReceived(
									event.getRequest(), event.getResponse());
						}
					} else {
						if (event.getCallbackObjectName().equals(
								"lasRequestCallback")) {
							lasRequestCallback.onError(event.getRequest(),
									event.getException());
						}
					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	};

	/**
	 * Builds a VizGal panel with a default plot for the variable. See {@code}
	 * VizGal(LASRequest) if you want more options on the initial plot.
	 */
	public OutputPanel(String id, boolean comparePanel, String op,
			String optionID, String view, boolean single,
			String container_type, String tile_server,
			boolean annotationsShowing) {
		logger.setLevel(Level.ALL);
		logger.info("OutputPanel constructor called with id:" + id);
		this.ID = id;
		this.comparePanel = comparePanel;
		this.singlePanel = single;
		this.operationID = op;
		this.optionID = optionID;
		this.view = view;
		this.containerType = container_type;
		cancelButton = new CancelButton(ID);
		eventBus = clientFactory.getEventBus();
		panelAxesWidgets = new AxesWidgetGroup(
		// "Coordinates Orthogonal to the Plot",
				"", "horizontal", "", "Apply To " + ID, tile_server, eventBus);
		spinImage = new Image(URLUtil.getImageURL() + "/mozilla_blu.gif");
		spinImage.setSize("18px", "18px");
		spin = new PopupPanel();
		spin.add(spinImage);
		spin.setSize("18px", "18px");

		// Creating plotImage this early causes error in Chrome
		// plotImage = new Image();
		// Also init the front canvas and context
		frontCanvas = Canvas.createIfSupported();
		frontCanvasContext = frontCanvas.getContext2d();

		messagePanel = new MessagePanel();

		grid = new FlexTable();
		grid.getCellFormatter().setHeight(0, 0, "");

		datasetLabel = new Label();

		// top = new FlexTable();

		String title = "Settings";

		OutputControlPanelActivity outputControlPanelPresenter = new OutputControlPanelActivity(
				clientFactory, ID);
		outputControlPanel = outputControlPanelPresenter.init(ID);

		datasetButton = outputControlPanel.getDatasetButton();
		eventBus.addHandler(SelectionEvent.getType(), datasetSelctionHandler);
		eventBus.addHandler(OperationChangeEvent.TYPE, operationChangeHandler);
		eventBus.addHandler(CancelEvent.TYPE, cancelRequestHandler);
		eventBus.addHandler(MapChangeEvent.TYPE, mapChangeHandler);
		eventBus.addHandler(FeatureModifiedEvent.TYPE, featureModifiedHandler);
		// TODO: move this logic into the AxesWidgetGroup (or its
		// presenter/activity if one is made)
		datasetButton.addOpenClickHandler(datasetOpenHandler);
		datasetButton.addCloseClickHandler(datasetCloseHandler);

		ChangeHandler variableChangeHandler = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				Object source = event.getSource();
				if (source instanceof UserListBox) {
					UserListBox variablesListBox = (UserListBox) source;
					// Only proceed if the source was from the same panel or
					// from the comparePanel
					if (variablesListBox != null) {
						String sourceName = variablesListBox.getName();
						if (sourceName != null) {
							boolean isFromThisOutputPanel = (ID != null)
									&& ID.equalsIgnoreCase(sourceName);
							boolean isFromComparePanel = (CONSTANTS
									.comparePanelName()
									.equalsIgnoreCase(sourceName));
							if (isFromThisOutputPanel || isFromComparePanel) {
								int selectedIndex = variablesListBox
										.getSelectedIndex();
								Object variableUserObject = variablesListBox
										.getUserObject(selectedIndex);
								if (variableUserObject instanceof VariableSerializable) {
									// Update this OutputPanel's
									// MultiVariableSelector only if the source
									// is not of this OutputPanel
									if (!isFromThisOutputPanel) {
										// Vector<VariableSerializable>
										// variables =
										// variablesListBox.getVariables();
										// // TODO: Make a higher level method
										// or
										// // use events
										// outputControlPanel.getVariableControls().getMultiVariableSelector().setVariables(variables,
										// selectedIndex);
									}
									if (isComparePanel()) {
										eventBus.fireEventFromSource(
												new VariableSelectionChangeEvent(),
												variablesListBox);
									} else {
										if (isFromThisOutputPanel) {
											// Update this OutputPanel's
											// variable
											VariableSerializable variable = (VariableSerializable) variableUserObject;
											applyVariableChange(variable, false);
											setChangeDataset(false);
											// Update OutputPanels if update
											// check box
											// is checked
											eventBus.fireEventFromSource(
													new WidgetSelectionChangeEvent(
															false),
													variablesListBox);
										}
									}
								}
							}
						}
					}
				}
			}
		};

		HandlerRegistration variableChangeHandlerReg = eventBus.addHandler(
				ChangeEvent.getType(), variableChangeHandler);

		// Make the comparePanel fire breadcrumb update events, on
		// non-comparePanels listen for them
		// outputControlPanel.getVariableControls().getVariableMetadataView().setOnComparePanel(comparePanel);
		if (comparePanel) {
			// Label gs = new Label("   ");
			// gs.setHeight("25px");
			// grid.setWidget(0, 0, gs);
			// grid.setWidget(0, 0, lasAnnotationsPanel);
			datasetButton.setVisible(false);
		} else {
			// grid.setWidget(0, 0, datasetButton);
			// grid.getCellFormatter().setWidth(0, 0, "1px");
			// grid.setWidget(0, 1, lasAnnotationsPanel);
		}

		HTML plot = new HTML();
		// plot.setHTML(spinImage.getHTML());
		plot.setHTML("<br>");
		lasAnnotationsPanel = new LASAnnotationsPanel();
		lasAnnotationsPanel.setVisible(annotationsShowing);
		grid.setWidget(0, 0, lasAnnotationsPanel);
		grid.setWidget(1, 0, plot);
		grid.setWidget(3, 0, outputControlPanel);
		initWidget(grid);
		logger.info("OutputPanel constructor exiting with id:" + id);
	}

	/**
	 * @param variable
	 */
	protected void applyVariableChange(VariableSerializable variable,
			boolean changeDataset) {
		nvar = variable;
		ngrid = null;
		setChangeDataset(changeDataset);
		if (nvar != null) {
			String nvarDSID = nvar.getDSID();
			String nvarID = nvar.getID();
			if (nvarDSID != null && nvarID != null && configCallback != null) {
				Util.getRPCService().getConfig(null, nvarDSID, nvarID,
						configCallback);
			}
		}
	}

	public void computeDifference(Map<String, String> options,
			boolean switchAxis, boolean force) {

		// If the passed in variable is a vector, then the panel variable must
		// also be a vector. Right?
		if (vizGalVariable.isVector() && !var.isVector()) {
			Widget plotWidget = grid.getWidget(1, 0);
			int w = plotWidget.getOffsetWidth();
			if (w > 100) {
				w = w - 10;
			}
			messagePanel.setWidth(w + "px");
			messagePanel
					.show(plotWidget.getAbsoluteLeft() + 15,
							plotWidget.getAbsoluteTop() + 15,
							"Could not make plot.  Variable in panel must also be a vector.");
			return;
		}

		// When called from vizGal the button is down
		difference = true;

		spin.hide();
		if (!hasViewAxes()) {
			spin.hide();
			HTML error = new HTML(
					"This data set does not have an axis in the current plot plane.  Choose a different plot.");
			error.setSize(image_w * imageScaleRatio + "px", image_h
					* imageScaleRatio + "px");
			grid.setWidget(1, 0, error);
			return;
		}
		lasRequest = new LASRequest();

		if (vizGalVariable.isVector()) {
			lasRequest.setOperation("Compare_Vectors", "v7");
		} else {
			lasRequest.setOperation("Compare_Plot", "v7");
		}
		lasRequest.setProperty("ferret", "view", view);
		lasRequest.setProperty("ferret", "size", ".8333");
		lasRequest.setProperty("ferret", "annotations", "file");
		// Add the variable in the upper left panel
		if (vizGalVariable.isVector()) {
			// Add the first component
			lasRequest.addVariable(vizGalVariable.getDSID(), vizGalVariable
					.getComponents().get(0), 0);
		} else {
			lasRequest.addVariable(vizGalVariable.getDSID(),
					vizGalVariable.getID(), 0);
		}

		Map<String, String> vgState = Util.getTokenMap(vizGalState);
		Map<String, String> cpState = Util.getTokenMap(comparePanelState);

		if (analysis != null) {
			if (analysis.isActive("t")) {
				analysis.getAxes().get("t").setLo(cpState.get("tlo"));
				analysis.getAxes().get("t").setHi(cpState.get("thi"));
			}
			if (analysis.isActive("z")) {
				analysis.getAxes().get("z").setLo(cpState.get("zlo"));
				analysis.getAxes().get("z").setHi(cpState.get("zhi"));
			}
			if (analysis.isActive("y")) {
				analysis.getAxes().get("y").setLo(cpState.get("ylo"));
				analysis.getAxes().get("y").setHi(cpState.get("yhi"));
			}
			if (analysis.isActive("x")) {
				analysis.getAxes().get("x").setLo(cpState.get("xlo"));
				analysis.getAxes().get("x").setHi(cpState.get("xhi"));
			}
			analysis.setLabel(var.getName());
			lasRequest.setAnalysis(analysis, 0);
		}

		// Set the region of the first variable according to the global state or
		// the compare panel state...

		if (view.contains("x")) {
			lasRequest.setRange("x", vgState.get("xlo"), vgState.get("xhi"), 0);
		} else {
			lasRequest.setRange("x", cpState.get("xlo"), cpState.get("xhi"), 0);
		}

		if (view.contains("y")) {
			lasRequest.setRange("y", vgState.get("ylo"), vgState.get("yhi"), 0);
		} else {
			lasRequest.setRange("y", cpState.get("ylo"), cpState.get("yhi"), 0);
		}
		if (vizGalVariable.getGrid().hasZ()) {
			if (view.contains("z")) {
				lasRequest.setRange("z", vgState.get("zlo"),
						vgState.get("zhi"), 0);
			} else {
				lasRequest.setRange("z", cpState.get("zlo"),
						cpState.get("zhi"), 0);
			}
		}
		if (vizGalVariable.getGrid().hasT()) {
			if (view.contains("t")) {
				lasRequest.setRange("t", vgState.get("tlo"),
						vgState.get("thi"), 0);
			} else {
				lasRequest.setRange("t", cpState.get("tlo"),
						cpState.get("thi"), 0);
			}
		}

		if (vizGalVariable.isVector()) {
			// Add the second component and its region to match the first
			// component.
			lasRequest.setProperty("ferret", "vector_name",
					vizGalVariable.getName());
			lasRequest
					.addVariable(var.getDSID(), var.getComponents().get(0), 1);
			if (view.contains("x")) {
				lasRequest.setRange("x", vgState.get("xlo"),
						vgState.get("xhi"), 1);
			} else {
				lasRequest.setRange("x", cpState.get("xlo"),
						cpState.get("xhi"), 1);
			}

			if (view.contains("y")) {
				lasRequest.setRange("y", vgState.get("ylo"),
						vgState.get("yhi"), 1);
			} else {
				lasRequest.setRange("y", cpState.get("ylo"),
						cpState.get("yhi"), 1);
			}
			if (var.getGrid().hasZ()) {
				if (view.contains("z")) {
					lasRequest.setRange("z", vgState.get("zlo"),
							vgState.get("zhi"), 1);
				} else {
					lasRequest.setRange("z", cpState.get("zlo"),
							cpState.get("zhi"), 1);
				}
			}
			if (var.getGrid().hasT()) {
				if (view.contains("t")) {
					lasRequest.setRange("t", vgState.get("tlo"),
							vgState.get("thi"), 1);
				} else {
					lasRequest.setRange("t", cpState.get("tlo"),
							cpState.get("thi"), 1);
				}
			}
		} else {

			// The local variable only set what is unique to this variable. I.e.
			// what's not in the view.

			lasRequest.addVariable(var.getDSID(), var.getID(), 1);

			if (analysis == null) {
				if (!view.contains("x")) {
					if (var.getGrid().hasX()) {
						lasRequest.setRange("x",
								String.valueOf(panelAxesWidgets.getRefMap()
										.getXlo()), String
										.valueOf(panelAxesWidgets.getRefMap()
												.getXhi()), 1);
					}
				}
				if (!view.contains("y")) {
					if (var.getGrid().hasY()) {
						lasRequest.setRange("y",
								String.valueOf(panelAxesWidgets.getRefMap()
										.getYlo()), String
										.valueOf(panelAxesWidgets.getRefMap()
												.getYhi()), 1);
					}
				}
				if (!view.contains("z")) {
					if (var.getGrid().hasZ()) {
						lasRequest.setRange("z", panelAxesWidgets.getZAxis()
								.getLo(), panelAxesWidgets.getZAxis().getHi(),
								1);
					}
				}
				if (!view.contains("t")) {
					if (var.getGrid().hasT()) {
						lasRequest.setRange("t", panelAxesWidgets.getTAxis()
								.getFerretDateLo(), panelAxesWidgets.getTAxis()
								.getFerretDateHi(), 1);
					}
				}
				// Add the analysis computation to the variable from this panel
			} else {
				if (analysis.isActive("t")) {
					analysis.getAxes()
							.get("t")
							.setLo(panelAxesWidgets.getTAxis()
									.getFerretDateLo());
					analysis.getAxes()
							.get("t")
							.setHi(panelAxesWidgets.getTAxis()
									.getFerretDateHi());
				} else {
					if (!view.contains("t")) {
						if (var.getGrid().hasT()) {
							lasRequest.setRange("t", panelAxesWidgets
									.getTAxis().getFerretDateLo(),
									panelAxesWidgets.getTAxis()
											.getFerretDateHi(), 1);
						}
					}
				}
				if (analysis.isActive("z")) {
					analysis.getAxes().get("z")
							.setLo(panelAxesWidgets.getZAxis().getLo());
					analysis.getAxes().get("z")
							.setHi(panelAxesWidgets.getZAxis().getHi());
				} else {
					if (!view.contains("z")) {
						if (var.getGrid().hasZ()) {
							lasRequest.setRange("z", panelAxesWidgets
									.getZAxis().getLo(), panelAxesWidgets
									.getZAxis().getHi(), 1);
						}
					}
				}
				if (analysis.isActive("y")) {
					analysis.getAxes()
							.get("y")
							.setLo(String.valueOf(panelAxesWidgets.getRefMap()
									.getYlo()));
					analysis.getAxes()
							.get("y")
							.setHi(String.valueOf(panelAxesWidgets.getRefMap()
									.getYhi()));
				} else {
					if (!view.contains("y")) {
						if (var.getGrid().hasY()) {
							lasRequest.setRange("y", String
									.valueOf(panelAxesWidgets.getRefMap()
											.getYlo()), String
									.valueOf(panelAxesWidgets.getRefMap()
											.getYhi()), 1);
						}
					}
				}
				if (analysis.isActive("x")) {
					analysis.getAxes()
							.get("x")
							.setLo(String.valueOf(panelAxesWidgets.getRefMap()
									.getXlo()));
					analysis.getAxes()
							.get("x")
							.setHi(String.valueOf(panelAxesWidgets.getRefMap()
									.getXhi()));
				} else {
					if (!view.contains("x")) {
						if (var.getGrid().hasX()) {
							lasRequest.setRange("x", String
									.valueOf(panelAxesWidgets.getRefMap()
											.getXlo()), String
									.valueOf(panelAxesWidgets.getRefMap()
											.getXhi()), 1);
						}
					}
				}
				analysis.setLabel(var.getName());
				lasRequest.setAnalysis(analysis, 1);
			}

		}

		// If the passed in variable is a vector, then the panel variable must
		// also be a vector. Right?
		if (vizGalVariable.isVector() && var.isVector()) {
			lasRequest.addVariable(vizGalVariable.getDSID(), vizGalVariable
					.getComponents().get(1), 0);
			if (var.getGrid().hasX()) {
				if (!view.contains("x")) {
					lasRequest.setRange("x", String.valueOf(panelAxesWidgets
							.getRefMap().getXlo()), String
							.valueOf(panelAxesWidgets.getRefMap().getXhi()), 2);
				}
			}

			if (var.getGrid().hasY()) {
				if (!view.contains("y")) {
					lasRequest.setRange("y", String.valueOf(panelAxesWidgets
							.getRefMap().getYlo()), String
							.valueOf(panelAxesWidgets.getRefMap().getYhi()), 2);
				}
			}
			if (var.getGrid().hasZ()) {
				if (!view.contains("z")) {
					lasRequest.setRange("z", panelAxesWidgets.getZAxis()
							.getLo(), panelAxesWidgets.getZAxis().getHi(), 2);
				}
			}
			if (var.getGrid().hasT()) {
				lasRequest.setRange("t", panelAxesWidgets.getTAxis()
						.getFerretDateLo(), panelAxesWidgets.getTAxis()
						.getFerretDateHi(), 2);
			}

			// And the other variable component. Same as above...
			lasRequest
					.addVariable(var.getDSID(), var.getComponents().get(1), 1);
			if (var.getGrid().hasX()) {
				if (!view.contains("x")) {
					lasRequest.setRange("x", String.valueOf(panelAxesWidgets
							.getRefMap().getXlo()), String
							.valueOf(panelAxesWidgets.getRefMap().getXhi()), 3);
				}
			}

			if (var.getGrid().hasY()) {
				if (!view.contains("y")) {
					lasRequest.setRange("y", String.valueOf(panelAxesWidgets
							.getRefMap().getYlo()), String
							.valueOf(panelAxesWidgets.getRefMap().getYhi()), 3);
				}
			}
			if (var.getGrid().hasZ()) {
				if (!view.contains("z")) {
					lasRequest.setRange("z", panelAxesWidgets.getZAxis()
							.getLo(), panelAxesWidgets.getZAxis().getHi(), 3);
				}
			}
			if (var.getGrid().hasT()) {
				if (!view.contains("t")) {
					lasRequest.setRange("t", panelAxesWidgets.getTAxis()
							.getFerretDateLo(), panelAxesWidgets.getTAxis()
							.getFerretDateHi(), 3);
				}
			}
		}
		lasRequest.setProperty("ferret", "image_format", "gif");
		if (containerType.equals(Constants.IMAGE)) {
			lasRequest.setProperty("las", "output_type", "xml");
		}

		// Use the global options.
		if (options != null) {
			for (Iterator<String> opIt = options.keySet().iterator(); opIt
					.hasNext();) {
				String key = opIt.next();
				String value = options.get(key);
				if (!value.toLowerCase().equals("default") && !value.equals("")) {
					lasRequest.setProperty("ferret", key, value);
				}
			}
		}

		lasRequest.setProperty("product_server", "ui_timeout", "20");
		String url = Util.getProductServer() + "?xml="
				+ URL.encode(lasRequest.toString());

		if (!url.equals(currentURL) || force) {

			if (updating) {
				pending = true;

				Widget plotWidget = grid.getWidget(1, 0);
				int w = plotWidget.getOffsetWidth();
				if (w > 100) {
					w = w - 10;
				}
				messagePanel.setWidth(w + "px");
				messagePanel.show(plotWidget.getAbsoluteLeft() + 15,
						plotWidget.getAbsoluteTop() + 350,
						"Cancel the current request before making another.");

				cancelButton.setTime(0);
				cancelButton.setSize(image_w * imageScaleRatio + "px", image_h
						* imageScaleRatio + "px");
				grid.setWidget(1, 0, cancelButton);

				return;
			}

			currentURL = url;
			Widget plotWidget = grid.getWidget(1, 0);
			spinSetPopupPositionCenter(plotWidget);
			spin.show();
			if (containerType.equals(Constants.IMAGE)) {

				RequestBuilder sendRequest = new RequestBuilder(
						RequestBuilder.GET, url);
				try {
					updating = true;
					sendRequest.sendRequest(null, lasRequestCallback);
				} catch (RequestException e) {
					spin.hide();
					HTML error = new HTML(e.toString());
					error.setSize(image_w * imageScaleRatio + "px", image_h
							* imageScaleRatio + "px");
					grid.setWidget(1, 0, error);
				}
			} else {
				output.setUrl(url);
				grid.setWidget(1, 0, output);
			}
		} else {
			setPlotImageWidth();
		}
	}

	/**
	 * @param size
	 * @return
	 * @throws NumberFormatException
	 */
	int convertPXtoInt(String size) throws NumberFormatException {
		String widthTrimmedLowerCase = size.trim().toLowerCase();
		widthTrimmedLowerCase = widthTrimmedLowerCase.substring(0,
				widthTrimmedLowerCase.length() - 2);
		widthTrimmedLowerCase = widthTrimmedLowerCase.trim();
		int widthInt = Integer.valueOf(widthTrimmedLowerCase);
		return widthInt;
	}

	private void drawToScreen(ImageData imageData) {
		if (frontCanvasContext != null) {
			logger.severe("CALLING frontCanvasContext.putImageData(imageData, 0, 0);");
			frontCanvasContext.putImageData(imageData, 0, 0);
		}
	}

	private double getDouble(Node firstChild) {
		if (firstChild instanceof Text) {
			Text content = (Text) firstChild;
			String value = content.getData().toString().trim();
			return Double.valueOf(value).doubleValue();
		} else {
			return -999.;
		}
	}

	private String getString(Node firstChild) {
		if (firstChild instanceof Text) {
			Text content = (Text) firstChild;
			String value = content.getData().toString().trim();
			return value;
		} else {
			return "";
		}
	}

	private String getElementValue(String element, Document doc) {
		NodeList results_y = doc.getElementsByTagName(element);
		String value = null;
		for (int n = 0; n < results_y.getLength(); n++) {
			if (results_y.item(n) instanceof Element) {
				Element result = (Element) results_y.item(n);
				Node text = result.getFirstChild();
				if (text instanceof Text) {
					Text t = (Text) text;
					value = t.getData();
				}
			}
		}
		return value;
	}

	public String getHistoryToken() {
		StringBuilder token = new StringBuilder();
		boolean wantZ;
		if (analysis == null) {
			wantZ = var.getGrid().hasZ() && !view.contains("z");
		} else {
			wantZ = var.getGrid().hasZ() && !view.contains("z")
					&& !analysis.isActive("z");
		}
		if (wantZ) {
			token.append(";zlo=" + panelAxesWidgets.getZAxis().getLo()
					+ ";zhi=" + panelAxesWidgets.getZAxis().getHi());
		}

		boolean wantT;
		if (analysis == null) {
			wantT = var.getGrid().hasT() && !view.contains("t");
		} else {
			wantT = var.getGrid().hasT() && !view.contains("t")
					&& !analysis.isActive("t");
		}
		if (wantT) {
			token.append(";tlo="
					+ panelAxesWidgets.getTAxis().getFerretDateLo() + ";thi="
					+ panelAxesWidgets.getTAxis().getFerretDateHi());
		}
		token.append(";dsid=" + var.getDSID());
		token.append(";varid=" + var.getID());
		// Add additional variables to the mix.
		List<UserListBox> boxes = outputControlPanel.getVariableControls()
				.getMultiVariableSelector().getVariableSelector()
				.getListBoxes();

		for (int i = 1; i < boxes.size(); i++) {
			UserListBox box = boxes.get(i);
			int index = box.getSelectedIndex();
			if (index >= 0) {
				VariableSerializable v = box.getUserObject(index);
				token.append(";avarid" + i + "=" + v.getID());
			}
		}
		if (boxes.size() > 0) {
			int avarcount = boxes.size() - 1;
			token.append(";avarcount=" + avarcount);
		}
		token.append(getSettingsWidgetHistoryToken());
		return token.toString();
	}

	public String getID() {
		return ID;
	}

	public double getMax() {
		return globalMax;
	}

	public double getMin() {
		return globalMin;
	}

	@Override
	public String getName() {
		return getID();
	}

	private int getNumber(Node firstChild) {
		if (firstChild instanceof Text) {
			Text content = (Text) firstChild;
			String value = content.getData().toString().trim();
			return Double.valueOf(value).intValue();
		} else {
			return -999;
		}
	}

	/**
	 * @return the outputControlPanel
	 */
	public OutputControlPanel getOutputControlPanel() {
		return outputControlPanel;
	}

	String getPlotWidth() {
		logger.fine("enterring getPlotWidth()");
		int antipadding = 0;// 100;
		String w = CONSTANTS.DEFAULT_ANNOTATION_PANEL_WIDTH();
		if (plotImage != null) {
			if (imageScaleRatio == 0.0)
				imageScaleRatio = 1.0;
			int wt = (int) ((plotImage.getWidth() - antipadding) * imageScaleRatio);
			w = wt + "px";
		}
		logger.fine("exiting getPlotWidth(), retuning w:" + w);
		return w;
	}

	public String getPrintURL() {
		return currentPrintURL;
	}

	public LASRequest getRequest() {
		LASRequest lasRequest = new LASRequest();

		if (var.isVector()) {
			// Add the first component
			lasRequest
					.addVariable(var.getDSID(), var.getComponents().get(0), 0);
			lasRequest.setProperty("ferret", "vector_name", var.getName());
		} else {
			lasRequest.addVariable(var.getDSID(), var.getID(), 0);
		}

		// Add the extra variable if they are set.
		List<UserListBox> boxes = outputControlPanel.getVariableControls()
				.getMultiVariableSelector().getVariableSelector()
				.getListBoxes();
		for (int i = 1; i < boxes.size(); i++) {
			UserListBox box = boxes.get(i);
			int index = box.getSelectedIndex();
			if (index >= 0) {
				VariableSerializable v = box.getUserObject(index);
				lasRequest.addVariable(v.getDSID(), v.getID(), 0);
			}
		}

		lasRequest.setOperation(operationID, "v7");

		Map<String, String> vgState = Util.getTokenMap(vizGalState);

		// If the axis is in the view, the state comes from the global vizGal
		// state which are store in the member variables.
		// Otherwise it gets its axis value from the local widget.
		String local_xlo = null;
		String local_xhi = null;
		String local_ylo = null;
		String local_yhi = null;
		String local_zlo = null;
		String local_zhi = null;
		String local_tlo = null;
		String local_thi = null;

		if (view.contains("x") || isComparePanel()) {
			local_xlo = vgState.get("xlo");
			local_xhi = vgState.get("xhi");
		} else {
			local_xlo = String.valueOf(panelAxesWidgets.getRefMap().getXlo());
			local_xhi = String.valueOf(panelAxesWidgets.getRefMap().getXhi());
		}

		if (view.contains("y") || isComparePanel()) {
			local_ylo = vgState.get("ylo");
			local_yhi = vgState.get("yhi");
		} else {
			local_ylo = String.valueOf(panelAxesWidgets.getRefMap().getYlo());
			local_yhi = String.valueOf(panelAxesWidgets.getRefMap().getYhi());
		}

		if (view.contains("z")) {
			local_zlo = vgState.get("zlo");
			local_zhi = vgState.get("zhi");
		} else {
			if (var.getGrid().hasZ()) {
				local_zlo = panelAxesWidgets.getZAxis().getLo();
				local_zhi = panelAxesWidgets.getZAxis().getHi();
			}
		}

		if (view.contains("t")) {
			local_tlo = vgState.get("tlo");
			local_thi = vgState.get("thi");
		} else {
			if (var.getGrid().hasT()) {
				local_tlo = panelAxesWidgets.getTAxis().getFerretDateLo();
				local_thi = panelAxesWidgets.getTAxis().getFerretDateHi();
			}
		}

		if (analysis != null) {

			if (!analysis.isActive("x")) {
				lasRequest.setRange("x", local_xlo, local_xhi, 0);
			}

			if (!analysis.isActive("y")) {
				lasRequest.setRange("y", local_ylo, local_yhi, 0);
			}

			if (var.getGrid().getZAxis() != null) {
				if (!analysis.isActive("z")) {
					lasRequest.setRange("z", local_zlo, local_zhi, 0);
				}
			}
			if (var.getGrid().getTAxis() != null) {
				if (!analysis.isActive("t")) {
					lasRequest.setRange("t", local_tlo, local_thi, 0);
				}
			}
		} else {
			lasRequest.setRange("x", local_xlo, local_xhi, 0);
			lasRequest.setRange("y", local_ylo, local_yhi, 0);
			if (var.getGrid().hasZ()) {
				lasRequest.setRange("z", local_zlo, local_zhi, 0);
			}
			if (var.getGrid().hasT()) {
				lasRequest.setRange("t", local_tlo, local_thi, 0);
			}
		}

		if (var.isVector()) {
			// Add the second component...
			lasRequest
					.addVariable(var.getDSID(), var.getComponents().get(1), 1);

			lasRequest.setRange("x", local_xlo, local_xhi, 1);
			lasRequest.setRange("y", local_ylo, local_yhi, 1);

			if (var.getGrid().getZAxis() != null) {
				lasRequest.setRange("z", local_zlo, local_zhi, 1);
			}

			if (var.getGrid().getTAxis() != null) {
				lasRequest.setRange("t", local_tlo, local_thi, 1);
			}

		}

		lasRequest.setProperty("ferret", "view", view);

		lasRequest.setProperty("ferret", "size", ".8333");
		lasRequest.setProperty("ferret", "image_format", "gif");
		lasRequest.setProperty("ferret", "annotations", "file");
		if (containerType.equals(Constants.IMAGE)) {
			lasRequest.setProperty("las", "output_type", "xml");
		}
		return lasRequest;
	}

	public String getSettingsWidgetHistoryToken() {
		StringBuffer t = new StringBuffer();
		if (!view.contains("x")) {
			t.append(";xlo=" + panelAxesWidgets.getRefMap().getXlo() + ";xhi="
					+ panelAxesWidgets.getRefMap().getXhi());
		}
		if (!view.contains("y")) {
			t.append(";ylo=" + panelAxesWidgets.getRefMap().getYlo() + ";yhi="
					+ panelAxesWidgets.getRefMap().getYhi());
		}
		return t.toString();
	}

	public String getThi() {
		return panelAxesWidgets.getTAxis().getFerretDateHi();
	}

	public String getTlo() {
		return panelAxesWidgets.getTAxis().getFerretDateLo();
	}

	public String getURL() {
		return currentURL;
	}

	public VariableSerializable getVariable() {
		return var;
	}

	public double getXhi() {
		return panelAxesWidgets.getRefMap().getXhi();
	}

	public String getXhiFormatted() {
		return panelAxesWidgets.getRefMap().getXhiFormatted();
	}

	public double getXlo() {
		return panelAxesWidgets.getRefMap().getXlo();
	}

	public String getXloFormatted() {
		return panelAxesWidgets.getRefMap().getXloFormatted();
	}

	public double getYhi() {
		return panelAxesWidgets.getRefMap().getYhi();
	}

	public String getYhiFormatted() {
		return panelAxesWidgets.getRefMap().getYhiFormatted();
	}

	public double getYlo() {
		return panelAxesWidgets.getRefMap().getYlo();
	}

	public String getYloFormatted() {
		return panelAxesWidgets.getRefMap().getYloFormatted();
	}

	public String getZhi() {
		return panelAxesWidgets.getZAxis().getHi();
	}

	public String getZlo() {
		return panelAxesWidgets.getZAxis().getLo();
	}

	/**
	 * Sets the plotImage to the grid (in a cell that is not used for anything)
	 * and makes it invisible so plotImage's imageElement will be valid. Usually
	 * done just before scaling and drawing plotImage.
	 */
	public void gridSetWidgetPlotImageAndHide() {
		grid.setWidget(2, 0, plotImage);
		plotImage.setVisible(false);
	}

	private boolean hasViewAxes() {
		if (view.contains("x")) {
			if (!var.getGrid().hasX()) {
				return false;
			}
		}
		if (view.contains("y")) {
			if (!var.getGrid().hasY()) {
				return false;
			}
		}
		if (view.contains("z")) {
			if (!var.getGrid().hasZ()) {
				return false;
			}
		}
		if (view.contains("t")) {
			if (!var.getGrid().hasT()) {
				return false;
			}
		}
		return true;
	}

	public void hide() {
		grid.setVisible(false);
		grid.getCellFormatter().setHeight(0, 0, "1px");
		panelAxesWidgets.setOpen(false);
	}

	public void init(boolean usePanel, OperationSerializable[] ops) {
		if (eventBus == null) {
			eventBus = clientFactory.getEventBus();
		}
		eventBus.addHandler(LASResponseEvent.TYPE, lasResponseEventHandler);
		panelAxesWidgets.getRefMap().setMapListener(mapListener);
		this.ops = ops;
		globalMin = 999999999.;
		globalMax = -999999999.;
		datasetLabel.setText(var.getDSName() + ": " + var.getName());
		GridSerializable ds_grid = var.getGrid();
		ngrid = ds_grid;
		ortho = Util.setOrthoAxes(view, ds_grid);
		double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
		double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

		double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
		double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

		double delta = Math.abs(Double.valueOf(ds_grid.getXAxis()
				.getArangeSerializable().getStep()));
		panelAxesWidgets.getRefMap().setDataExtent(grid_south, grid_north,
				grid_west, grid_east, delta);

		if (ds_grid.getTAxis() != null) {
			compareAxis = "t";
			if (ds_grid.getZAxis() != null) {
				fixedAxis = "z";
			}
		} else if (ds_grid.getZAxis() != null) {
			compareAxis = "z";
			fixedAxis = "none";
		}
		if (ds_grid.getTAxis() != null) {
			panelAxesWidgets.getTAxis().init(ds_grid.getTAxis(), false);

		}
		if (ds_grid.getZAxis() != null) {
			panelAxesWidgets.getZAxis().init(ds_grid.getZAxis());
		}

		if (!singlePanel) {
			outputControlPanel.setWidget(1, 0, panelAxesWidgets);
		} else {
			// In singlePanel, the panelAxesWidgets controls will be on the left
			// navbar.
		}
		// These two lines were removed since frontCanvas and
		// frontCanvasContext are now initialized in the constructor.
		// frontCanvas = Canvas.createIfSupported();
		// frontCanvasContext = frontCanvas.getContext2d();

		int rndRedColor = 244;
		int rndGreenColor = 154;
		int rndBlueColor = 0;
		double rndAlpha = .45;

		randomColor = CssColor.make("rgba(" + rndRedColor + ", "
				+ rndGreenColor + "," + rndBlueColor + ", " + rndAlpha + ")");

		// setPlotImageWidth now sets the plotImage to the grid before any
		// scaling so plotImage's imageElement will be valid
		setPlotImageWidth();
	}

	/**
	 * True if a new data set has been selected with this OutputPanel's data set
	 * button (in it's {@link OutputControlPanel}). This allows the data set
	 * change to be delayed until the "Update Plot" button is pressed. As a
	 * consequence, changeDataset is only set to true when the local data set
	 * button is used, not when the main data set button is used.
	 * 
	 * @return the changeDataset
	 */
	public boolean isChangeDataset() {
		return changeDataset;
	}

	public boolean isComparePanel() {
		return comparePanel;
	}

	public boolean isWaitingForRPC() {
		return waitingForHistory;
	}

	/**
	 * Send a request to the LAS server to create a new plot, you must have
	 * already pushed the operation and axes values from the main left-hand
	 * controls to the panel and you can pass in the current "global" plot
	 * options.
	 * 
	 * @param options
	 * @param switchAxis
	 * @param popup
	 */
	public void refreshPlot(Map<String, String> options, boolean switchAxis,
			boolean popup, boolean force) {

		if ((!var.isVector() && vizGalVariable.isVector())) {
			Widget plotWidget = grid.getWidget(1, 0);
			int w = plotWidget.getOffsetWidth();
			if (w > 100) {
				w = w - 10;
			}
			messagePanel.setWidth(w + "px");
			messagePanel
					.show(plotWidget.getAbsoluteLeft() + 15,
							plotWidget.getAbsoluteTop() + 15,
							"Could not make plot.  Variable in panel must also be a vector.");
			return;
		}

		if (var.isVector() && !vizGalVariable.isVector()) {
			Widget plotWidget = grid.getWidget(1, 0);
			int w = plotWidget.getOffsetWidth();
			if (w > 100) {
				w = w - 10;
			}
			messagePanel.setWidth(w + "px");
			messagePanel
					.show(plotWidget.getAbsoluteLeft() + 15,
							plotWidget.getAbsoluteTop() + 15,
							"Could not make plot.  Variable in panel must not be a vector.");
			return;
		}

		// When called from vizGal this means the button is off...

		difference = false;

		messagePanel.hide();

		if (!hasViewAxes()) {
			spin.hide();
			HTML error = new HTML(
					"This data set does not have an axis in the current plot plane.  Choose a different plot.");
			error.setSize(image_w * imageScaleRatio + "px", image_h
					* imageScaleRatio + "px");
			grid.setWidget(1, 0, error);
			return;
		}

		lasRequest = getRequest();

		// Analysis comes from the state of the nav section of the main UI
		Map<String, String> vgState = Util.getTokenMap(vizGalState);

		if (analysis != null) {
			if (analysis.isActive("t")) {
				analysis.getAxes().get("t").setLo(vgState.get("tlo"));
				analysis.getAxes().get("t").setHi(vgState.get("thi"));
			}
			if (analysis.isActive("z")) {
				analysis.getAxes().get("z").setLo(vgState.get("zlo"));
				analysis.getAxes().get("z").setHi(vgState.get("zhi"));
			}
			if (analysis.isActive("y")) {
				analysis.getAxes().get("y").setLo(vgState.get("ylo"));
				analysis.getAxes().get("y").setHi(vgState.get("yhi"));
			}
			if (analysis.isActive("x")) {
				analysis.getAxes().get("x").setLo(vgState.get("xlo"));
				analysis.getAxes().get("x").setHi(vgState.get("xhi"));
			}
			lasRequest.setAnalysis(analysis, 0);
		}
		if (options != null) {
			for (Iterator<String> opIt = options.keySet().iterator(); opIt
					.hasNext();) {
				String key = opIt.next();
				String value = options.get(key);
				if (!value.toLowerCase().equals("default") && !value.equals("")) {
					lasRequest.setProperty("ferret", key, value);
				}
			}
		}

		// Now force in the auto contour settings if it exists.
		if (fill_levels != null && !fill_levels.equals("")
				&& !fill_levels.equals(Constants.NO_MIN_MAX)) {
			lasRequest.setProperty("ferret", "fill_levels", fill_levels);
		}
		lasRequest.setProperty("product_server", "ui_timeout", "10");
		Widget plotWidget = grid.getWidget(1, 0);
		if (var.getGrid().hasT()) {
			if (view.contains("t")
					&& lasRequest.getRangeLo("t", 0).equals(
							lasRequest.getRangeHi("t", 0))) {
				int w = plotWidget.getOffsetWidth();
				if (w > 100) {
					w = w - 10;
				}
				messagePanel.setWidth(w + "px");
				messagePanel
						.show(plotWidget.getAbsoluteLeft() + 15,
								plotWidget.getAbsoluteTop() + 15,
								"Set the range selectors for the Date/Time to different values and click the update plots button.");
				return;
			}

		}
		if (var.getGrid().hasZ()) {
			if (view.contains("z")
					&& lasRequest.getRangeLo("z", 0).equals(
							lasRequest.getRangeHi("z", 0))) {
				int w = plotWidget.getOffsetWidth();
				if (w > 100) {
					w = w - 10;
				}
				messagePanel.setWidth(w + "px");
				messagePanel
						.show(plotWidget.getAbsoluteLeft() + 15,
								plotWidget.getAbsoluteTop() + 15,
								"Set the range selectors for the Z-Level to different values and click the Apply button.");
				return;
			}
		}

		if (lasRequest == null) {
			return;
		}

		String url = Util.getProductServer() + "?xml="
				+ URL.encode(lasRequest.toString());

		if (!url.equals(currentURL) || force) {
			if (updating) {
				pending = true;

				Widget pw = grid.getWidget(1, 0);
				int w = pw.getOffsetWidth();
				if (w > 100) {
					w = w - 10;
				}
				messagePanel.setWidth(w + "px");
				messagePanel.show(pw.getAbsoluteLeft() + 15,
						pw.getAbsoluteTop() + 350,
						"Cancel the current request before making another.");

				cancelButton.setTime(0);
				cancelButton.setSize(image_w * imageScaleRatio + "px", image_h
						* imageScaleRatio + "px");
				grid.setWidget(1, 0, cancelButton);

				return;
			}
			currentURL = url;
			if (containerType.equals(Constants.IMAGE)) {
				if (popup) {
					spinSetPopupPositionCenter(plotWidget);
					spin.show();
				}

				// RequestBuilder sendRequest = new
				// RequestBuilder(RequestBuilder.GET, url);
				try {
					lasAnnotationsPanel.setTitle("Plot Annotations");
					lasAnnotationsPanel
							.setError("Fetching plot annotations...");
					updating = true;
					historyTokenMap = null;
					// sendRequest.sendRequest(null, lasRequestCallback);
					// logger.info("Just called sendRequest.sendRequest(null, lasRequestCallback);");
					// } catch ( RequestException e ) {
					LASRequestEvent lasRequestEvent = new LASRequestEvent(url,
							RequestBuilder.GET, "lasRequestCallback");
					Logger.getLogger("refreshPlot").info(
							this.getName() + " is firing lasRequestEvent:"
									+ lasRequestEvent + " with url:" + url);
					eventBus.fireEventFromSource(lasRequestEvent, this);
				} catch (Exception e) {
					spin.hide();
					HTML error = new HTML(e.toString());
					error.setSize(image_w * imageScaleRatio + "px", image_h
							* imageScaleRatio + "px");
					grid.setWidget(1, 0, error);
				}
			} else {
				grid.setWidget(1, 0, spinImage);
				output.setUrl(url);
				grid.setWidget(1, 0, output);
			}
		} else {
			// No need to call setPlotImageWidth as a controller
			// that listens to the new
			// StringValueChangeEvent(url) will eventually do so
			// setPlotImageWidth();
			logger.info("refreshPlot firing StringValueChangeEvent with currentURL = url:"
					+ url);
			eventBus.fireEvent(new StringValueChangeEvent(url));
		}
	}

	private void scale(Image img, double scaleRatio) {
		scaledImage = scaleImage(img, scaleRatio);
		drawToScreen(scaledImage);
	}

	private ImageData scaleImage(Image image, double scaleToRatio) {
		logger.info("enterring scaleImage with scaleToRatio:" + scaleToRatio);
		Canvas canvasTmp = Canvas.createIfSupported();
		Context2d context = canvasTmp.getContext2d();

		int imageHeight = image.getHeight();
		if (imageHeight <= 0) {
			logger.warning("imageHeight:" + imageHeight);
		}
		double ch = (imageHeight * scaleToRatio);
		int imageWidth = image.getWidth();
		if (imageWidth <= 0) {
			logger.warning("imageWidth:" + imageWidth);
		}
		double cw = (imageWidth * scaleToRatio);

		canvasTmp.setCoordinateSpaceHeight((int) ch);
		canvasTmp.setCoordinateSpaceWidth((int) cw);

		// TODO: make a temp imageElement?
		ImageElement imageElement = ImageElement.as(image.getElement());

		// s = source
		// d = destination
		double sx = 0;
		double sy = 0;
		int imageElementWidth = imageElement.getWidth();
		if (imageElementWidth <= 0) {
			logger.warning("imageElementWidth:" + imageElementWidth);
			imageElementWidth = imageWidth;
			logger.info("imageElementWidth:" + imageElementWidth);
		}
		double sw = imageElementWidth;
		int imageElementHeight = imageElement.getHeight();
		if (imageElementHeight <= 0) {
			logger.warning("imageElementHeight:" + imageElementHeight);
			imageElementHeight = imageHeight;
			logger.info("imageElementHeight:" + imageElementHeight);
		}
		double sh = imageElementHeight;

		double dx = 0;
		double dy = 0;
		double dw = imageElementWidth;
		double dh = imageElementHeight;

		// tell it to scale image
		context.scale(scaleToRatio, scaleToRatio);

		// draw image to canvas
		context.drawImage(imageElement, sx, sy, sw, sh, dx, dy, dw, dh);

		// get image data
		double w = dw * scaleToRatio;
		double h = dh * scaleToRatio;
		ImageData imageData = null;
		try {
			imageData = context.getImageData(0, 0, w, h);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			if (image == null) {
				logger.warning("image:null");
			} else {
				logger.warning("image:" + image.toString());
			}
			logger.warning("scaleToRatio:" + scaleToRatio);
			logger.warning("ch:" + ch);
			logger.warning("cw:" + cw);
			logger.warning("dw:" + dw);
			logger.warning("w:" + w);
			logger.warning("dh:" + dh);
			logger.warning("h:" + h);
			if (imageData == null) {
				logger.warning("imageData:null");
			} else {
				logger.warning("imageData:" + imageData.toString());
			}
			e.printStackTrace();
		}

		frontCanvas.setCoordinateSpaceHeight((int) h + 10);
		frontCanvas.setCoordinateSpaceWidth((int) w + 10);
		if (imageData != null)
			logger.info("scaleImage exiting returning imageData:" + imageData);
		else
			logger.severe("scaleImage exiting returning imageData:null");
		return imageData;
	}

	public void setAnalysis(AnalysisSerializable analysisSerializable) {
		this.analysis = analysisSerializable;
	}

	void setAnalysisAxes(String value) {

		// Eliminate the transformed axis from the acceptable intervals for the
		// current variable.
		GridSerializable ds_grid = var.getGrid();
		String intervals = ds_grid.getIntervals().replace(value, "");
		// Eliminate the transformed axis from the view.
		String newView = view.replace(value, "");
		// If the view goes blank, find the next best view.
		if (newView.equals("")) {
			if (intervals.contains("xy")) {
				view = "xy";
			} else if (intervals.contains("t") && ds_grid.hasT()) {
				view = "t";
			} else if (intervals.contains("z") && ds_grid.hasZ()) {
				view = "z";
			} else if (intervals.contains("x")) {
				view = "x";
			} else if (intervals.contains("y")) {
				view = "y";
			}
		} else {
			view = newView;
		}

		// Get set the new operations that apply to the remaining views.
		operationID = ops[0].getID();
		// xOperationsWidget.setOperations(intervals, ops[0].getID(), view,
		// ops);
		this.setOperation(operationID, view);
		// tOperationsMenu.setMenus(ops, view);
		// setOperationsClickHandler(xVizGalOperationsClickHandler);

		// Set the default operation.
		// operationID = xOperationsWidget.setZero(view);
		ortho = Util.setOrthoAxes(view, ds_grid);

		// for ( Iterator panIt = xPanels.iterator(); panIt.hasNext(); ) {
		// OutputPanel panel = (OutputPanel) panIt.next();
		// panel.setOperation(operationID, view);
		// panel.showOrthoAxes(view, xOrtho);
		// }

		String aAxis = null;
		StringBuilder ab = new StringBuilder();
		if (analysis != null) {
			if (analysis.isActive("x"))
				ab.append("x");
			if (analysis.isActive("y"))
				ab.append("y");
			if (analysis.isActive("z"))
				ab.append("z");
			if (analysis.isActive("t"))
				ab.append("t");
			if (ab.length() > 0) {
				aAxis = ab.toString();
			}
		}

		panelAxesWidgets.showOrthoAxes(view, ortho, aAxis, isComparePanel());
		String mapTool = "";
		String panelMapTool = "";
		if (view.contains("x")) {
			mapTool = "x";
		} else {
			if (value.contains("x")) {
				panelMapTool = "x";
			}
		}
		if (view.contains("y")) {
			mapTool = mapTool + "y";
		} else {
			if (value.contains("y")) {
				panelMapTool = panelMapTool + "y";
			}
		}
		if (mapTool.equals(""))
			mapTool = "pt";
		if (panelMapTool.equals(""))
			panelMapTool = "pt";
		// xAxesWidget.getRefMap().setTool(mapTool);
		panelAxesWidgets.getRefMap().setTool(mapTool);
		// for ( Iterator panIt = xPanels.iterator(); panIt.hasNext(); ) {
		// OutputPanel panel = (OutputPanel) panIt.next();
		// panel.setMapTool(panelMapTool);
		// if ( value.contains("z") ) {
		// panel.setRange("z", true);
		// } else {
		// panel.setRange("z", false);
		// }
		// if ( value.contains("t") ) {
		// panel.setRange("t", true);
		// } else {
		// panel.setRange("t", false);
		// }
		// }
		if (value.contains("z")) {
			this.setRange("z", true);
		} else {
			this.setRange("z", false);
		}
		if (value.contains("t")) {
			this.setRange("t", true);
		} else {
			this.setRange("t", false);
		}
	}

	// This is for mock up user interface and are not used with "real" UI's.
	public void setAnnotationsHTMLURL(String url) {
		lasAnnotationsPanel.setAnnotationsHTMLURL(url);
		lasAnnotationsPanel.setPopupWidth(getPlotWidth());
	}

	public void setAnnotationsOpen(boolean open) {
		lasAnnotationsPanel.setPopupWidth(getPlotWidth());
		lasAnnotationsPanel.setVisible(open);
	}

	public void setAxisRangeValues(String axis, String lo_value, String hi_value) {
		if (axis.equals("z")) {
			panelAxesWidgets.getZAxis().setLo(lo_value);
			panelAxesWidgets.getZAxis().setHi(hi_value);
		} else if (axis.equals("t")) {
			panelAxesWidgets.getTAxis().setLo(lo_value);
			panelAxesWidgets.getTAxis().setHi(hi_value);
		}
	}

	/**
	 * True if a new data set has been selected with this OutputPanel's data set
	 * button (in it's {@link OutputControlPanel}). This allows the data set
	 * change to be delayed until the "Update Plot" button is pressed. As a
	 * consequence, changeDataset is only set to true when the local data set
	 * button is used, not when the main data set button is used.
	 * 
	 * @param changeDataset
	 *            the changeDataset to set
	 */
	void setChangeDataset(boolean changeDataset) {
		this.changeDataset = changeDataset;
	}

	private void setCompareAxisEnabled(boolean b) {
		Widget w = grid.getWidget(2, 0);
		if (w instanceof DateTimeWidget) {
			DateTimeWidget dt = (DateTimeWidget) w;
			dt.setEnabled(b);
		} else if (w instanceof AxisWidget) {
			AxisWidget a = (AxisWidget) w;
			a.setEnabled(b);
		}
	}

	public void setDataExtent(double[] data) {
		panelAxesWidgets.getRefMap().setDataExtent(data[0], data[1], data[2],
				data[3]);
	}

	public void setDataExtent(String xlo, String xhi, String ylo, String yhi) {
		panelAxesWidgets.getRefMap().setDataExtent(Double.valueOf(ylo),
				Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
	}

	private void setExtraVariables() {
		// If the history includes additional variables, add the selectors and
		// set them.
		if (historyTokenMap.keySet().contains("avarcount")) {
			UserListBox source = getOutputControlPanel().getVariableControls()
					.getMultiVariableSelector().getVariableSelector()
					.getListBoxes().get(0);
			getOutputControlPanel().getVariableControls()
					.getMultiVariableSelector().getVariableSelector()
					.removeExtraListBoxes(false);
			int c = Integer.valueOf(historyTokenMap.get("avarcount"));
			for (int i = 0; i < c; i++) {
				int avari = i + 1;
				String avid = historyTokenMap.get("avarid" + avari);
				VariableSelector vs = getOutputControlPanel()
						.getVariableControls().getMultiVariableSelector()
						.getVariableSelector();
				source.setAddButtonVisible(false);
				source.setRemoveButtonVisible(false);
				UserListBox box = vs.addUserListBox(source, vs);
				Vector<VariableSerializable> variables = box.getVariables();
				int v = 0;
				int index = 0;
				for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
					VariableSerializable variableSerializable = (VariableSerializable) varIt
							.next();
					if (variableSerializable.getID().equals(avid)) {
						index = v;
					}
					v++;
				}
				box.setSelectedIndex(index);
				source = box;
			}
		}
	}

	public void setFillLevels(String fill_levels) {
		this.fill_levels = fill_levels;
	}

	public void setFromHistoryToken(Map<String, String> tokenMap,
			Map<String, String> optionsMap) {

		historyTokenMap = tokenMap;
		historyOptionsMap = optionsMap;

		String tokvarid = tokenMap.get("varid");
		String tokdsid = tokenMap.get("dsid");
		if (!tokvarid.equals(var.getID()) || !tokdsid.equals(var.getDSID())) {
			waitingForHistory = true;
			Util.getRPCService().getCategories(tokdsid, historyDatasetCallback);
		} else {
			setExtraVariables();
			setOrthogonalAxesValues();
		}
	}

	public void setHeight(int height) {
		super.setHeight(height + "px");
		this.setPanelHeight(height - lasAnnotationsPanel.getOffsetHeight());
	}

	/**
	 * @see com.google.gwt.user.client.ui.UIObject#setHeight(java.lang.String)
	 */
	@Override
	public void setHeight(String height) {
		super.setHeight(height);
		int heightInt = convertPXtoInt(height);
		this.setPanelHeight(heightInt - lasAnnotationsPanel.getOffsetHeight());
	}

	public void setID(String id) {
		this.ID = id;
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
		logger.severe("CALLING grid.setWidget(1, 0, image);");
		grid.setWidget(1, 0, image);
	}

	public void setImageSize(int percent) {
		fixedZoom = percent;
		double factor = percent / 100.;
		Widget p = grid.getWidget(1, 0);
		int w = (int) (Double.valueOf(image_w).doubleValue() * factor);
		int h = (int) (Double.valueOf(image_h).doubleValue() * factor);
		p.setWidth(w + "px");
		p.setHeight(h + "px");
		autoZoom = false;
	}

	public void setLat(String ylo, String yhi) {
		panelAxesWidgets.getRefMap().setCurrentSelection(Double.valueOf(ylo),
				Double.valueOf(yhi), panelAxesWidgets.getRefMap().getXlo(),
				panelAxesWidgets.getRefMap().getXhi());
	}

	public void setLatLon(double[] selection) {
		panelAxesWidgets.getRefMap().setCurrentSelection(selection[0],
				selection[1], selection[2], selection[3]);
	}

	public void setLatLon(String ylo, String yhi, String xlo, String xhi) {
		panelAxesWidgets.getRefMap().setCurrentSelection(Double.valueOf(ylo),
				Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
	}

	public void setLon(String xlo, String xhi) {
		panelAxesWidgets.getRefMap().setCurrentSelection(
				panelAxesWidgets.getRefMap().getYlo(),
				panelAxesWidgets.getRefMap().getYhi(), Double.valueOf(xlo),
				Double.valueOf(xhi));
	}

	public void setMapSelectionChangeLister(MapSelectionChangeListener handler) {
		panelAxesWidgets.getRefMap().setMapListener(handler);
	}

	public void setMapTool(String view) {
		if (isComparePanel()) {
			panelAxesWidgets.getRefMap().setTool(view);
		} else {
			if (view.contains("x") && !view.contains("y")) {
				panelAxesWidgets.getRefMap().setTool("px");
				panelAxesWidgets
						.setTitle("Select a latitude for this panel by clicking on the map.");
			} else if (!view.contains("x") && view.contains("y")) {
				panelAxesWidgets.getRefMap().setTool("px");
				panelAxesWidgets
						.setTitle("Select a longitude for this panel by clicking on the map.");
			} else {
				panelAxesWidgets.getRefMap().setTool("pt");
				panelAxesWidgets
						.setTitle("Select a latitude/longitude point for this panel by clicking on the map.");
			}
		}
	}

	public void setMouseMoves(List<Mouse> mouse) {
		mouseMoves = mouse;
	}

	@Override
	public void setName(String name) {
		setID(name);
	}

	public void setOperation(String id, String v) {
		operationID = id;
		view = v;
		// In any panel a map selection will always be a point orthogonal to the
		// view.
		if (v.contains("y") && !v.contains("x")) {
			panelAxesWidgets.getRefMap().setTool("py");
		} else if (!v.contains("y") && v.contains("x")) {
			panelAxesWidgets.getRefMap().setTool("px");
		} else {
			panelAxesWidgets.getRefMap().setTool("pt");
		}
	}

	public void setOperationOnly(String id, String v) {
		// The map might be being used for the analysis, so only set the
		// operation...
		operationID = id;
		view = v;
	}

	private void setOrthogonalAxesValues() {
		// If the variable is different, this has to be called after the RPC.
		if (!view.contains("t")) {
			if (historyTokenMap.get("tlo") != null
					&& historyTokenMap.get("thi") != null) {
				panelAxesWidgets.getTAxis().setLo(historyTokenMap.get("tlo"));
				panelAxesWidgets.getTAxis().setHi(historyTokenMap.get("thi"));
			}
		}
		if (!view.contains("z")) {
			if (historyTokenMap.get("zlo") != null
					&& historyTokenMap.get("zhi") != null) {
				panelAxesWidgets.getZAxis().setLo(historyTokenMap.get("zlo"));
				panelAxesWidgets.getZAxis().setHi(historyTokenMap.get("zhi"));
			}
		}
		if (!view.contains("x") && !view.contains("y")) {
			setLatLon(historyTokenMap.get("ylo"), historyTokenMap.get("yhi"),
					historyTokenMap.get("xlo"), historyTokenMap.get("xhi"));
		} else if (!view.contains("x") && view.contains("y")) {
			setLatLon(String.valueOf(panelAxesWidgets.getRefMap().getYlo()),
					String.valueOf(panelAxesWidgets.getRefMap().getYhi()),
					historyTokenMap.get("xlo"), historyTokenMap.get("xhi"));
		} else if (view.contains("x") && !view.contains("y")) {
			setLatLon(historyTokenMap.get("ylo"), historyTokenMap.get("yhi"),
					String.valueOf(panelAxesWidgets.getRefMap().getXlo()),
					String.valueOf(panelAxesWidgets.getRefMap().getXhi()));
		}
	}

	public void setPanelAxisRange(String type, boolean b) {
		if (type.equals("t")) {
			panelAxesWidgets.setRange("t", b);
		}
		if (type.equals("z")) {
			panelAxesWidgets.setRange("z", b);
		}
	}

	public void setPanelColor(String style) {
		grid.setStyleName(style);
	}

	void setPanelHeight(int height) {
		autoZoom = true;
		pwidth = (int) ((image_w / image_h) * Double.valueOf(height));
		logger.info("setPanelHeight(int " + height + ") Just set pwidth:"
				+ pwidth);
		setPlotImageWidth();
	}

	void setPanelWidth(int width) {
		autoZoom = true;
		int max = (int) image_w;
		pwidth = Math.min(width, max);
		logger.info("setPanelWidth(int " + width + ") has just set pwidth:"
				+ pwidth);
		setPlotImageWidth();
	}

	public void setPlotImageWidth() {
		Widget plotWidget = grid.getWidget(1, 0);
		// Piggy back setting the annotations width onto this method.
		if (autoZoom) {
			imageScaleRatio = 1.;
			if (pwidth < image_w) {
				// If the panel is less than the image, shrink the image.
				double h = ((Double.valueOf(image_h) / Double.valueOf(image_w)) * Double
						.valueOf(pwidth));
				imageScaleRatio = h / Double.valueOf(image_h);
			}
			if (plotImage != null) {
				// set the plotImage to the grid before scaling so plotImage's
				// imageElement will be valid
				gridSetWidgetPlotImageAndHide();
				scale(plotImage, imageScaleRatio);
			}
		} else {
			setImageSize(fixedZoom);
		}
		if (spin.isVisible()) {
			spinSetPopupPositionCenter(plotWidget);
		}
		lasAnnotationsPanel.setPopupWidth(getPlotWidth());
	}

	public void setRange(String axis, boolean b) {
		if (axis.equals("z")) {
			panelAxesWidgets.getZAxis().setRange(b);
		} else if (axis.equals("t")) {
			panelAxesWidgets.getTAxis().setRange(b);
		}
	}

	public void setRanges(String xView, List<String> xOrtho) {
		for (int i = 0; i < xView.length(); i++) {
			String type = xView.substring(i, i + 1);
			panelAxesWidgets.setRange(type, true);
		}
	}

	public void setOrthoRanges(String xView, List<String> xOrtho) {
		boolean range = false;
		if (var != null && var.getGrid() != null && var.isScattered()) {
			range = true;
		}
		for (Iterator<String> oIt = xOrtho.iterator(); oIt.hasNext();) {
			String type = oIt.next();
			panelAxesWidgets.setRange(type, range);
		}
	}

	public void setT(String tlo, String thi) {
		panelAxesWidgets.getTAxis().setLo(tlo);
		// TODO also set the hi value
	}

	public void setURL(String url) {
		currentURL = url;
	}

	public void setVariable(VariableSerializable v) {
		GridSerializable oldgrid = null;
		if (var != null) {
			oldgrid = var.getGrid();
		}
		var = v;
		datasetLabel.setText(var.getDSName() + ": " + var.getName());
		getOutputControlPanel().getVariableControls().setVariable(var);
		UserListBox box = getOutputControlPanel().getVariableControls()
				.getMultiVariableSelector().getVariableSelector()
				.getLatestListBox();
		int index = 0;
		for (int vi = 0; vi < box.getVariables().size(); vi++) {
			VariableSerializable vs = (VariableSerializable) box.getVariables()
					.get(vi);
			if (vs.getID().equals(var.getID())) {
				index = vi;
			}
		}
		box.setSelectedIndex(index);
		GridSerializable ds_grid = var.getGrid();
		if (oldgrid == null
				|| (oldgrid != null && !ds_grid.getID().equals(oldgrid.getID()))) {
			panelAxesWidgets.init(ds_grid);
		}
	}

	public void setVizGalState(VariableSerializable variable,
			String historyToken, String comparePanelState) {
		this.vizGalVariable = variable;
		this.vizGalState = historyToken;
		this.comparePanelState = comparePanelState;
	}

	public void setWidth(int width) {
		logger.info("setWidth(int width) called with width:" + width);
		this.setPanelWidth(width);
		int plotImageWidth = convertPXtoInt(getPlotWidth());
		logger.info("New plotImageWidth:" + plotImageWidth);
		super.setWidth(width + "px");
		if (plotImageWidth != width) {
			logger.warning("Couldn't set the exact width of this plotImage to width:"
					+ width);
		}
		if (plotImage != null) {
			logger.info("plotImage == " + plotImage.toString());
		} else {
			logger.severe("plotImage == null");
		}
		logger.info("ID:" + this.getID());
	}

	/**
	 * @see com.google.gwt.user.client.ui.UIObject#setWidth(java.lang.String)
	 */
	@Override
	public void setWidth(String width) {
		logger.info("setWidth(String width) with width:" + width);
		int widthInt = convertPXtoInt(width);
		setWidth(widthInt);
	}

	public void setZ(String zlo, String zhi) {
		panelAxesWidgets.getZAxis().setLo(zlo);
		// TODO also set hi if it's a range
	}

	public void show() {
		grid.setVisible(true);
		panelAxesWidgets.setOpen(true);
	}

	public void showOrthoAxes(String view, List<String> ortho, String analysis) {
		panelAxesWidgets.showOrthoAxes(view, ortho, analysis, isComparePanel());
	}

	/**
	 * Position spin at the center of the plotWidget
	 * 
	 * @param plotWidget
	 */
	void spinSetPopupPositionCenter(Widget plotWidget) {
		int absoluteLeft = plotWidget.getAbsoluteLeft();
		int offsetWidth = plotWidget.getOffsetWidth();
		int absoluteTop = plotWidget.getAbsoluteTop();
		int offsetHeight = plotWidget.getOffsetHeight();
		int left = absoluteLeft + (offsetWidth / 2)
				- (spin.getOffsetWidth() / 2);
		int top = absoluteTop + (offsetHeight / 2)
				- (spin.getOffsetHeight() / 2);
		spin.setPopupPosition(left, top);
	}

	void turnOffAnalysis() {

		this.setAnalysis(null);
		operationID = ops[0].getID();

		view = "xy";
		ortho = Util.setOrthoAxes(view, var.getGrid());
		panelAxesWidgets.getRefMap().setTool(view);
		GridSerializable grid = var.getGrid();
		this.setOperation(operationID, view);
		if (grid.hasZ()) {
			this.setRange("z", false);
		}
		if (grid.hasT()) {
			this.setRange("t", false);
		}
		String aAxis = null;
		StringBuilder ab = new StringBuilder();
		if (analysis != null) {
			if (analysis.isActive("x"))
				ab.append("x");
			if (analysis.isActive("y"))
				ab.append("y");
			if (analysis.isActive("z"))
				ab.append("z");
			if (analysis.isActive("t"))
				ab.append("t");
			if (ab.length() > 0) {
				aAxis = ab.toString();
			}
		}
		this.showOrthoAxes(view, ortho, aAxis);
		panelAxesWidgets.getRefMap().resizeMap();
	}

}
