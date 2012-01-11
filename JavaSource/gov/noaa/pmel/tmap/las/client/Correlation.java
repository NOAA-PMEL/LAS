package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.CruiseIconWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.HelpPanel;
import gov.noaa.pmel.tmap.las.client.laswidget.LASAnnotationsPanel;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequest;
import gov.noaa.pmel.tmap.las.client.laswidget.VariableConstraintLayout;
import gov.noaa.pmel.tmap.las.client.laswidget.VariableConstraintWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.VariableListBox;
import gov.noaa.pmel.tmap.las.client.map.MapSelectionChangeListener;
import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.URLUtil;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ChangeEvent;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

public class Correlation implements EntryPoint {
	NumberFormat dFormat = NumberFormat.getFormat("########.##");
	CruiseIconWidget cruiseIcons = new CruiseIconWidget();
	FlexTable timePanel = new FlexTable();
	PushButton reset = new PushButton("Reset");
	DateTimeWidget timeConstraint = new DateTimeWidget();
	AxisWidget zAxisWidget = new AxisWidget();
	VerticalPanel spaceTimeConstraints = new VerticalPanel();
	HorizontalPanel spaceTimeTopRow = new HorizontalPanel();
	Map<String, VariableSerializable> xDatasetVariables = new HashMap<String, VariableSerializable>();
	ToggleButton annotationsButton;
	LASAnnotationsPanel lasAnnotationsPanel = new LASAnnotationsPanel();
	HorizontalPanel buttonPanel = new HorizontalPanel();
	HorizontalPanel topRow = new HorizontalPanel();
	HorizontalPanel output = new HorizontalPanel();
	HelpPanel help = new HelpPanel();
	HelpPanel spaceTimeHelp = new HelpPanel();
	PopupPanel spin;
	HTML spinImage;
	HorizontalPanel coloredBy = new HorizontalPanel();	
	VariableConstraintLayout constraintsLayout = new VariableConstraintLayout("Variable Constraints:", true);
	VariableConstraintWidget xVariableConstraint = new VariableConstraintWidget();
	VariableConstraintWidget yVariableConstraint = new VariableConstraintWidget();
	OLMapWidget mapConstraint = new OLMapWidget("128px", "256px");
	Label selection = new Label("Current selection:");
	Label horizontalLabel = new Label("Horizontal: ");
	Label verticalLabel = new Label("Vertical: ");
	FlexTable controlPanel = new FlexTable();
	VerticalPanel topPanel = new VerticalPanel();
	FlexTable outputPanel = new FlexTable();
    VariableListBox xVariables = new VariableListBox();
    VariableListBox yVariables = new VariableListBox();
    VariableListBox colorVariables = new VariableListBox();
    PushButton update = new PushButton("Update Plot");
    PushButton print = new PushButton("Print");
    CheckBox colorCheckBox = new CheckBox();
    LASRequest lasRequest;
    String dsid;
    String varid;
	String currentURL;
	String operationID;
	// Drawing start position
	int startx = -1;
	int starty = -1;
	int endx;
	int endy;
	boolean draw = false;
	Context2d frontCanvasContext;
	Canvas frontCanvas;
	CssColor randomColor;
	final static String operationType = "v7";
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
	protected String printURL;
	protected String xlo;
	protected String xhi;
	protected String ylo;
	protected String yhi;
	protected String tlo;
	protected String thi;
	protected String zlo;
	protected String zhi;
    @Override
	public void onModuleLoad() {
    	
    	mapConstraint.setMapListener(mapListener);
    	timeConstraint.addChangeHandler(needApply);
    	
    	timePanel.setWidget(0, 0, timeConstraint);
    	timePanel.setWidget(0, 1, reset);
    	
    	reset.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				timeConstraint.reinit();
				update.addStyleDependentName("APPLY-NEEDED");
			}
    		
    	});
    	reset.addStyleDependentName("SMALLER");
    	reset.setTitle("Reset time to the full range.");
    	String spinImageURL = URLUtil.getImageURL()+"/mozilla_blu.gif";
    	
    	annotationsButton = new ToggleButton(new Image(GWT.getModuleBaseURL()+"../images/i_off.png"), 
				new Image(GWT.getModuleBaseURL()+"../images/i_on.png"), new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						if ( annotationsButton.isDown() ) {	
							lasAnnotationsPanel.setOpen(true);
						} else {
							lasAnnotationsPanel.setOpen(false);
						}						
					}
			
		});
    	annotationsButton.setTitle("Plot Annotations");
		annotationsButton.setStylePrimaryName("OL_MAP-ToggleButton");
		annotationsButton.addStyleDependentName("WIDTH");
		spinImage = new HTML("<img src=\""+spinImageURL+"\" alt=\"Spinner\"/>");
		spin = new PopupPanel();
		spin.add(spinImage);
    	update.addStyleDependentName("SMALLER");
    	update.setWidth("80px");
    	
    	xVariableConstraint.addChangeHandler(constraintChange);
    	yVariableConstraint.addChangeHandler(constraintChange);
    	xVariableConstraint.addApplyHandler(applyHandler);
    	yVariableConstraint.addApplyHandler(applyHandler);
    	    	
    	constraintsLayout.setVisible(false);
    	print.addStyleDependentName("SMALLER");
    	print.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				printerFriendly();
			}
    		
    	});
    	print.setEnabled(false);
    	buttonPanel.add(update);	
		buttonPanel.add(print);
    	topRow.add(help);
    	topRow.add(new HTML("<b>&nbsp;&nbsp;Data Selection: </b>"));
    	controlPanel.setWidget(0, 0, topRow);
		controlPanel.getFlexCellFormatter().setColSpan(0, 0, 5);
    	controlPanel.setWidget(1, 0, yVariables);
		controlPanel.setWidget(1, 1, new Label(" as a function of "));
		controlPanel.setWidget(1, 2, xVariables);
		
		colorCheckBox.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				if ( colorCheckBox.getValue() ) {
					colorVariables.setEnabled(true);
				} else {
					colorVariables.setEnabled(false);
				}
				setVariables();
			}
			
		});
		constraintsLayout.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String id = constraintsLayout.getValue(constraintsLayout.getSelectedIndex());
				VariableSerializable v = xDatasetVariables.get(id);
				if ( v != null ) {
					VariableConstraintWidget c = new VariableConstraintWidget(true);
					c.addRemoveHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							removeHandlerHelper(event);
						}
					});
					c.setVariable(v);
					c.addApplyHandler(applyHandler);
					c.addChangeHandler(constraintChange);
					constraintsLayout.addWidget(c);
					setConstraints();
				}	
			}
			
		});
		coloredBy.add(new Label("Colored By "));
		coloredBy.add(colorCheckBox);
		controlPanel.setWidget(2, 1, coloredBy);
		controlPanel.setWidget(2, 2, colorVariables);
		topPanel.add(controlPanel);
		help.setPopupWidth("550px");
		help.setPopupHeight("550px");
		help.setHelpURL("../css/correlation_help.html");
		colorVariables.setEnabled(false);
		colorVariables.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				
				setVariables();

			}
			
		});
		update.addClickHandler(updateClick);
		String xml = Util.getParameterString("xml");
		if ( xml != null && !xml.equals("") ) {
			xml = URL.decode(xml);
			
			// Get rid of the entity values for > and <
			xml = xml.replace("&gt;", ">");
			xml = xml.replace("&lt;", "<");
			// Replace the op value with gt ge eq lt le as needed.
			xml = xml.replace("op=\">=\"", "op=\"ge\"");
			xml = xml.replace("op=\">\"", "op=\"gt\"");
			xml = xml.replace("op=\"=\"", "op=\"eq\"");
			xml = xml.replace("op=\"<=\"", "op=\"le\"");
			xml = xml.replace("op=\"<\"", "op=\"lt\"");
			lasRequest = new LASRequest(xml);
			dsid = lasRequest.getDataset(0);		
			varid = lasRequest.getVariable(0);
			xlo = lasRequest.getRangeLo("x", 0);
			xhi = lasRequest.getRangeHi("x", 0);
			ylo = lasRequest.getRangeLo("y", 0);
			yhi = lasRequest.getRangeHi("y", 0);
			tlo = lasRequest.getRangeLo("t", 0);
			thi = lasRequest.getRangeHi("t", 0);
			Util.getRPCService().getFullDataset(dsid, datasetCallback);
			// Up above we need to get the icons widget init url and use it here.  :-)
			cruiseIcons.init(lasRequest, varid);
		} else {
			
		}
		yVariables.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {

				VariableSerializable varY = yVariables.getVariable(yVariables.getSelectedIndex());
				if ( varY.getName().toLowerCase().equals("latitude") || varY.getName().toLowerCase().equals("longitude") || varY.getName().toLowerCase().equals("time") ) {
					yVariableConstraint.setVisible(false);
					yVariableConstraint.setApply(false);
					yVariableConstraint.setActive(false);
				} else {
					yVariableConstraint.setVisible(true);
					yVariableConstraint.setVariable(varY);
					yVariableConstraint.setActive(true);

					setVariables();
					resetConstraints("y");
					List<VariableConstraintWidget> remove = new ArrayList<VariableConstraintWidget>();
					for (Iterator vcwIt = constraintsLayout.getWidgets().iterator(); vcwIt.hasNext();) {
						VariableConstraintWidget vcw = (VariableConstraintWidget) vcwIt.next();
						if ( vcw.getVariable().getID().equals(varY.getID()) ) {
							String min = vcw.getMin();
							String max = vcw.getMax();
							if ( min != null && !min.equals("") ) {
								yVariableConstraint.setMin(min);
								yVariableConstraint.setApply(true);
							}
							if ( max != null && !max.equals("") ) {
								yVariableConstraint.setMax(max);
								yVariableConstraint.setApply(true);
							}
							remove.add(vcw);
						}
					}
					if ( remove.size() > 0 ) resetConstraints("y");
					for (Iterator vcwIt = remove.iterator(); vcwIt.hasNext();) {
						VariableConstraintWidget vcw = (VariableConstraintWidget) vcwIt.next();
						constraintsLayout.removeWidget(vcw);
					}
				}
		        
		        // Now the the constraint widgets are set, set the constraints in the request object.
		        setConstraints();
				
			}
		});
		xVariables.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				VariableSerializable varX = xVariables.getVariable(xVariables.getSelectedIndex());
				if ( varX.getName().toLowerCase().equals("latitude") || varX.getName().toLowerCase().equals("longitude") || varX.getName().toLowerCase().equals("time") ) {
					xVariableConstraint.setVisible(false);
					xVariableConstraint.setApply(false);
					xVariableConstraint.setActive(false);
				} else {
					xVariableConstraint.setVariable(varX);
					xVariableConstraint.setVisible(true);
					xVariableConstraint.setActive(true);
					setVariables();
					resetConstraints("x");
					List<VariableConstraintWidget> remove = new ArrayList<VariableConstraintWidget>();
					for (Iterator vcwIt = constraintsLayout.getWidgets().iterator(); vcwIt.hasNext();) {
						VariableConstraintWidget vcw = (VariableConstraintWidget) vcwIt.next();
						if ( vcw.getVariable().getID().equals(varX.getID())) {
							String min = vcw.getMin();
							String max = vcw.getMax();
							if ( min != null && !min.equals("") ) {
								xVariableConstraint.setMin(min);
								xVariableConstraint.setApply(true);
							}
							if ( max != null && !max.equals("") ) {
								xVariableConstraint.setMax(max);
								xVariableConstraint.setApply(true);
							}
							remove.add(vcw);
						}
					}
					if ( remove.size() > 0 ) resetConstraints("x");
					for (Iterator vcwIt = remove.iterator(); vcwIt.hasNext();) {
						VariableConstraintWidget vcw = (VariableConstraintWidget) vcwIt.next();
						constraintsLayout.removeWidget(vcw);
					}
				}
				// Now the the constraint widgets are set, set the constraints in the request object.
				setConstraints();
			}
		});
		outputPanel.setWidget(0, 0, lasAnnotationsPanel);
		output.add(annotationsButton);
		output.add(outputPanel);
		spaceTimeTopRow.add(spaceTimeHelp);
		spaceTimeTopRow.add(new HTML("<b>&nbsp;&nbsp;Latitude, Longitude and Time Constraints:</b>&nbsp;"));
		spaceTimeConstraints.add(spaceTimeTopRow);
		spaceTimeConstraints.add(mapConstraint);
		spaceTimeConstraints.add(timePanel);
		spaceTimeConstraints.add(zAxisWidget);
		RootPanel.get("icons").add(cruiseIcons);
		RootPanel.get("button_panel").add(buttonPanel);
		RootPanel.get("data_selection").add(topPanel);
		RootPanel.get("data_constraints").add(constraintsLayout);
		RootPanel.get("correlation").add(output);
		RootPanel.get("space_time_constraints").add(spaceTimeConstraints);
		lasAnnotationsPanel.setPopupLeft(outputPanel.getAbsoluteLeft());
		lasAnnotationsPanel.setPopupTop(outputPanel.getAbsoluteTop());
		lasAnnotationsPanel.setTitle("Plot Annotations");
		lasAnnotationsPanel.setError("Click \"Update plot\" to refresh the plot.&nbsp;");
		History.addValueChangeHandler(historyHandler);
	}
    protected MapSelectionChangeListener mapListener = new MapSelectionChangeListener() {
		
		@Override
		public void onFeatureChanged() {
            update.addStyleDependentName("APPLY-NEEDED");			
		}
	};
	public ChangeHandler needApply = new ChangeHandler() {

		@Override
		public void onChange(ChangeEvent event) {
			update.addStyleDependentName("APPLY-NEEDED");
		}
		
	};
    ClickHandler updateClick = new ClickHandler() {

    	@Override
    	public void onClick(ClickEvent event) {
    		updatePlot(true);		
    	}

    };
    private void printerFriendly() {
    	StringBuilder urlfrag = new StringBuilder(URLUtil.getBaseURL()+"getAnnotations.do?template=image_w_annotations.vm&");
    	urlfrag.append(printURL);
    	Window.open(urlfrag.toString(), "print", null);
    }
    private void updatePlot(boolean addHistory) {
    	setConstraints();
    	update.removeStyleDependentName("APPLY-NEEDED");
    	lasAnnotationsPanel.setTitle("Plot Annotations");
    	lasAnnotationsPanel.setError("Fetching plot annotations...");

    	spin.setPopupPosition(outputPanel.getAbsoluteLeft(), outputPanel.getAbsoluteTop());
    	spin.show();


    	//TODO get these from the map and the widget in the spaceTimeConstraint panel.
    	String tlo = timeConstraint.getFerretDateLo();
    	String thi = timeConstraint.getFerretDateHi();
    	String xlo = String.valueOf(mapConstraint.getXlo());
    	String xhi = String.valueOf(mapConstraint.getXhi());
    	String ylo = String.valueOf(mapConstraint.getYlo());
    	String yhi = String.valueOf(mapConstraint.getYhi());
    	String zlo = lasRequest.getRangeLo("z", 0);
    	String zhi = lasRequest.getRangeHi("z", 0);

    	String vix = xVariables.getVariable(xVariables.getSelectedIndex()).getID();
    	GridSerializable grid = xDatasetVariables.get(vix).getGrid();

        if ( tlo != null && thi != null ) {
    	   lasRequest.setRange("t", tlo, thi, 0);
        } else if ( thi != null ) {
        	lasRequest.setRange("t", thi, thi, 0);
        } else {
     	   lasRequest.setRange("t", timeConstraint.getFerretDateMin(), timeConstraint.getFerretDateMax(), 0);
        }


    	if ( xlo != null && xhi != null ) {
    		lasRequest.setRange("x", xlo, xhi, 0);
    	} else if ( xhi != null ) {
    		lasRequest.setRange("x", xhi, xhi, 0);
    	} else {
    		lasRequest.setRange("x", grid.getXAxis().getLo(), grid.getXAxis().getHi(), 0);
    	}


    	if ( ylo != null && yhi != null ) {
    		lasRequest.setRange("y", ylo, yhi, 0);
    	} else if ( yhi != null ) {
    		lasRequest.setRange("y", yhi, yhi, 0);
    	} else {
    		lasRequest.setRange("y", grid.getYAxis().getLo(), grid.getYAxis().getHi(), 0);
    	}


    	if ( zlo != null && zhi != null ) {
    		lasRequest.setRange("z", zlo, zhi, 0);
    	} else if ( zhi != null ) {
    		lasRequest.setRange("z", zhi, zhi, 0);
    	}

    	lasRequest.setProperty("product_server", "ui_timeout", "20");
    	lasRequest.setProperty("las", "output_type", "xml");
    	lasRequest.setOperation(operationID, operationType);
    	lasRequest.setProperty("ferret", "annotations", "file");

    	frontCanvas = Canvas.createIfSupported();
    	frontCanvasContext = frontCanvas.getContext2d();

    	int rndRedColor = 190;
    	int rndGreenColor = 40;
    	int rndBlueColor = 40;
    	double rndAlpha = .25;

    	randomColor = CssColor.make("rgba(" + rndRedColor + ", " + rndGreenColor + "," + rndBlueColor + ", " + rndAlpha + ")");

    	String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.toString());

    	currentURL = url;

    	if ( addHistory ) {
    		pushHistory(lasRequest.toString());
    	}

    	RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
    	try {
    		sendRequest.sendRequest(null, lasRequestCallback);
    	} catch (RequestException e) {
    		HTML error = new HTML(e.toString());
    		outputPanel.setWidget(1, 0, error);
    	}

    }
	RequestCallback lasRequestCallback = new RequestCallback() {

		@Override
		public void onError(Request request, Throwable exception) {
			
			spin.hide();
			Window.alert("Product request failed.");

		}

		@Override
		public void onResponseReceived(Request request, Response response) {
			spin.hide();
			print.setEnabled(true);
			String doc = response.getText();
			String imageurl = "";
			String annourl = "";
			// Look at the doc.  If it's not obviously XML, treat it as HTML.
			if ( !doc.substring(0, 100).contains("<?xml") ) {
				HTML result = new HTML(doc, true);
				outputPanel.setWidget(1, 0, result);
			} else {
				doc = doc.replaceAll("\n", "").trim();
				Document responseXML = XMLParser.parse(doc);
				NodeList results = responseXML.getElementsByTagName("result");

				for(int n=0; n<results.getLength();n++) {
					if ( results.item(n) instanceof Element ) {
						Element result = (Element) results.item(n);
						if ( result.getAttribute("type").equals("image") ) {
							imageurl = result.getAttribute("url");
						} else if ( result.getAttribute("type").equals("error") ) {
							if ( result.getAttribute("ID").equals("las_message") ) {
								Node text = result.getFirstChild();
								if ( text instanceof Text ) {
									Text t = (Text) text;
									HTML error = new HTML(t.getData().toString().trim());
									outputPanel.setWidget(1, 0, error);
								}
							}
						} else if ( result.getAttribute("type").equals("annotations") ) {
								annourl = result.getAttribute("url");
								lasAnnotationsPanel.setAnnotationsHTMLURL(Util.getAnnotationService(annourl));
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
											}
										}
									}
								}
							}
						}
					}
				}
				if ( !imageurl.equals("") ) {
					final Image image = new Image(imageurl);
					x_per_pixel = (x_axis_upper_right - x_axis_lower_left)/Double.valueOf(x_plot_size);
					y_per_pixel = (y_axis_upper_right - y_axis_lower_left)/Double.valueOf(y_plot_size);
                    
					if ( frontCanvas != null ) {
						outputPanel.setWidget(2, 0, image);
						image.setVisible(false);
						image.addLoadHandler(new LoadHandler() {

							@Override
							public void onLoad(LoadEvent event) {
								String w = image.getWidth() - 18 + "px";
			                    lasAnnotationsPanel.setPopupWidth(w);
			                    lasAnnotationsPanel.setPopupLeft(outputPanel.getAbsoluteLeft());
			            		lasAnnotationsPanel.setPopupTop(outputPanel.getAbsoluteTop());
								frontCanvas.setCoordinateSpaceHeight(image.getHeight());
								frontCanvas.setCoordinateSpaceWidth(image.getWidth());
								frontCanvasContext.drawImage(ImageElement.as(image.getElement()), 0, 0);
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
											frontCanvasContext.drawImage(ImageElement.as(image.getElement()), 0, 0);
											world_startx = x_axis_lower_left + (startx - x_offset_from_left)*x_per_pixel;
											world_starty = y_axis_lower_left + ((y_image_size-starty)-y_offset_from_bottom)*y_per_pixel;
											
											world_endx = world_startx;
											world_endy = world_starty;
											
											setTextValues();
											xVariableConstraint.setApply(true);
											yVariableConstraint.setApply(true);
											
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
											world_endx = x_axis_lower_left + (currentx - x_offset_from_left)*x_per_pixel;
											world_endy = y_axis_lower_left + ((y_image_size-currenty)-y_offset_from_bottom)*y_per_pixel;
											setTextValues();
											frontCanvasContext.setFillStyle(randomColor);
											frontCanvasContext.drawImage(ImageElement.as(image.getElement()), 0, 0);
											frontCanvasContext.fillRect(startx, starty, currentx - startx, currenty-starty);
										}
									}
								});
								outputPanel.setWidget(1, 0, frontCanvas);
							}

						});
						frontCanvas.addMouseUpHandler(new MouseUpHandler() {
							
							@Override
							public void onMouseUp(MouseUpEvent event) {
								// If we're still drawing when the mouse goes up, record the position.
								if ( draw ) {
									endx = event.getX();
									endy = event.getY();
								}
								draw = false;
								setConstraints();

							}
						});
					} else {
						// Browser cannot handle a canvas tag, so just put up the image.
						outputPanel.setWidget(1, 0, image);
						image.addLoadHandler(new LoadHandler() {

							@Override
							public void onLoad(LoadEvent event) {
								String w = image.getWidth() - 18 + "px";
								lasAnnotationsPanel.setPopupLeft(outputPanel.getAbsoluteLeft());
								lasAnnotationsPanel.setPopupTop(outputPanel.getAbsoluteTop());
			                    lasAnnotationsPanel.setPopupWidth(w);
							}
						});
						
					}
				}
				world_startx = x_axis_lower_left;
                world_endx = x_axis_upper_right;
                world_starty = y_axis_lower_left;
                world_endy = y_axis_upper_right;
                setTextValues();
                printURL = Util.getAnnotationsFrag(annourl, imageurl);
			}
		}
	};
	private void setTextValues() {

		constraintsLayout.setVisible(true);

		if ( world_startx <= world_endx ) {

			xVariableConstraint.setConstraint(dFormat.format(world_startx), dFormat.format(world_endx));

		} else {

			xVariableConstraint.setConstraint(dFormat.format(world_endx), dFormat.format(world_startx));	

		}

		if ( world_starty <= world_endy ) {
			
			yVariableConstraint.setConstraint(dFormat.format(world_starty), dFormat.format(world_endy));
			
		} else {
			
			yVariableConstraint.setConstraint(dFormat.format(world_endy), dFormat.format(world_starty));

		}
	}
    AsyncCallback<DatasetSerializable> datasetCallback = new AsyncCallback<DatasetSerializable>() {

		@Override
		public void onFailure(Throwable caught) {
			
			Window.alert("Could not get the variables list from the server.");
			
		}

		@Override
		public void onSuccess(DatasetSerializable result) {
			VariableSerializable variables[] = result.getVariablesSerializable();
			int index = -1;
			int time_index = -1;
			constraintsLayout.setHeader("Add a variable constraint...");
			for (int i = 0; i < variables.length; i++) {
				if ( !variables[i].getAttributes().get("grid_type").equals("vector") ) {
					xDatasetVariables.put(variables[i].getID(), variables[i]);
					xVariables.addItem(variables[i]);
					yVariables.addItem(variables[i]);
					colorVariables.addItem(variables[i]);
					if ( !variables[i].getName().toLowerCase().equals("latitude") && !variables[i].getName().toLowerCase().equals("longitude") && !variables[i].getName().toLowerCase().equals("time") ) {
				         constraintsLayout.addItem(variables[i]);
					}
					if ( variables[i].getID().equals(varid) ) {
						index = i;
					}
					if ( variables[i].getName().toLowerCase().contains("time") ) {
						time_index = i;
					}
				}
			}
			if ( index > 0 ) {
				yVariables.setSelectedIndex(index);
			}
			if ( time_index > 0 ) {
				xVariables.setSelectedIndex(time_index);
			}
			String grid_type = xVariables.getVariable(0).getAttributes().get("grid_type");
			if ( grid_type.equals("regular") ) {
				operationID = "prop_prop_plot";
			} else if ( grid_type.equals("trajectory") ) {
				operationID = "Trajectory_correlation_plot";
			}
			
			VariableSerializable varY = yVariables.getVariable(yVariables.getSelectedIndex());
			constraintsLayout.addWidgetForY(yVariableConstraint);
			if ( varY.getName().toLowerCase().equals("latitude") ) {
				yVariableConstraint.setVariable(varY);
				yVariableConstraint.setActive(false);
				yVariableConstraint.setVisible(false); 
			} else if ( varY.getName().toLowerCase().equals("longitude") ) {
				yVariableConstraint.setVariable(varY);
				yVariableConstraint.setActive(false);
				yVariableConstraint.setVisible(false); 
			} else if ( varY.getName().toLowerCase().equals("time") ) {
				yVariableConstraint.setVariable(varY);
				yVariableConstraint.setActive(false);
				yVariableConstraint.setVisible(false); 
			} else {
				yVariableConstraint.setVariable(varY);
				yVariableConstraint.setVisible(true); 	
			}

			VariableSerializable varX = xVariables.getVariable(xVariables.getSelectedIndex());
			constraintsLayout.addWidgetForX(xVariableConstraint);
			if ( varX.getName().toLowerCase().equals("latitude") ) {
				xVariableConstraint.setVariable(varX);
				xVariableConstraint.setActive(false);
				xVariableConstraint.setVisible(false);
			} else if ( varX.getName().toLowerCase().equals("longitude") ) {
				xVariableConstraint.setVariable(varX);
				xVariableConstraint.setActive(false);
				xVariableConstraint.setVisible(false);
			} else if ( varX.getName().toLowerCase().equals("time") ) {
				xVariableConstraint.setVariable(varX);
				xVariableConstraint.setActive(false);
				xVariableConstraint.setVisible(false);
			} else {
				xVariableConstraint.setVariable(varX);
				xVariableConstraint.setVisible(true);
			}
			
			
			GridSerializable grid = varX.getGrid();
			mapConstraint.setTool("xy");
			mapConstraint.setDataExtent(Double.valueOf(grid.getYAxis().getLo()), Double.valueOf(grid.getYAxis().getHi()), Double.valueOf(grid.getXAxis().getLo()), Double.valueOf(grid.getXAxis().getHi()));
			if  ( xlo != null && xhi != null && ylo != null && yhi != null ) {
				mapConstraint.setCurrentSelection(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
			}
			if ( grid.hasT() ) {
				timeConstraint.init(grid.getTAxis(), true);
				if ( tlo != null ) {
					timeConstraint.setLo(tlo);
				}
				if ( thi != null ) {
					timeConstraint.setHi(thi);
				}
				timeConstraint.setVisible(true);
			} else {
				timeConstraint.setVisible(false);
			}
			
			if ( grid.hasZ() ) {
				zAxisWidget.init(grid.getZAxis());
				zAxisWidget.setVisible(true);
			} else {
				zAxisWidget.setVisible(false);
			}
			
			if ( zlo != null && zhi != null ) {
				zAxisWidget.setLo(zlo);
				zAxisWidget.setHi(zhi);
			}
			
			setVariables();
			List<Map<String, String>> vcs = lasRequest.getVariableConstraints();
			for (Iterator vcIt = vcs.iterator(); vcIt.hasNext();) {
				Map<String, String> con = (Map<String, String>) vcIt.next();
				String varid = con.get("varID");
				String op = con.get("op");
				String value = con.get("value");
				String id = con.get("id");
				String plotv = plotVariable(varid);
				VariableSerializable v = xDatasetVariables.get(varid);
				if ( v != null && v.getName().toLowerCase().equals("latitude") ) {
					if ( op.equals("gt") || op.equals("ge") ) {
						mapConstraint.setCurrentSelection(Double.valueOf(value), Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
					} else if ( op.equals("eq") ) {
						mapConstraint.setCurrentSelection(Double.valueOf(value), Double.valueOf(value), Double.valueOf(xlo), Double.valueOf(xhi));
					} else {
						mapConstraint.setCurrentSelection(Double.valueOf(ylo), Double.valueOf(value), Double.valueOf(xlo), Double.valueOf(xhi));
					}
				} else if ( v != null &&  v.getName().toLowerCase().equals("longitude") ) {	
					if ( op.equals("gt") || op.equals("ge") ) {
						mapConstraint.setCurrentSelection(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(value), Double.valueOf(xhi));
					} else if ( op.equals("eq") ) {
						mapConstraint.setCurrentSelection(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(value), Double.valueOf(value));
					} else {
						mapConstraint.setCurrentSelection(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(value));
					}
				} else if ( v != null && v.getName().toLowerCase().equals("time") ) {
					if ( op.equals("gt") || op.equals("ge") ) {
						timeConstraint.setLo(value);
					} else if ( op.equals("eq") ) {
						timeConstraint.setLo(value);
						timeConstraint.setHi(value);
					} else {
						timeConstraint.setHi(value);
					}
				} else if ( v != null && plotv.equals("x") ) {
					xVariableConstraint.setApply(true);
					if ( op.equals("gt") || op.equals("ge") ) {
						xVariableConstraint.setMin(value);
					} else if ( op.equals("eq") ) {
						xVariableConstraint.setMin(value);
						xVariableConstraint.setMax(value);
					} else {
						xVariableConstraint.setMax(value);
					}
					constraintsLayout.setVisible(true);
				
				} else if ( plotv.equals("y") ){
					yVariableConstraint.setApply(true);
					if ( op.equals("gt") || op.equals("ge") ) {
						yVariableConstraint.setMin(value);
					} else if ( op.equals("eq") ) {
						yVariableConstraint.setMin(value);
						yVariableConstraint.setMax(value);
					} else {
						yVariableConstraint.setMax(value);
					}
					constraintsLayout.setVisible(true);
					
				} else {
					VariableConstraintWidget c = new VariableConstraintWidget(true);
					c.addRemoveHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							removeHandlerHelper(event);
						}
						
					});
					c.addApplyHandler(applyHandler);
					c.addChangeHandler(constraintChange);
					
					if ( v != null ) {
						c.setVariable(v);
						c.setApply(true);
						if ( op.equals("gt") || op.equals("ge") ) {
							c.setMin(value);
						} else if ( op.equals("eq") ) {
							c.setMin(value);
							c.setMax(value);
						} else {
							c.setMax(value);
						}
					}
					constraintsLayout.addWidget(c);
				}
				setConstraints();
			}
			updatePlot(true);
		}
    };
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
	ValueChangeHandler<String> historyHandler = new ValueChangeHandler<String>() {

		@Override
		public void onValueChange(ValueChangeEvent<String> event) {
			
			String xml = event.getValue();
			if ( !xml.equals("") ) {
				popHistory(xml);
			} else {
				print.setEnabled(false);
				outputPanel.removeCell(1, 0);
				xVariables.setSelectedIndex(0);
				yVariables.setSelectedIndex(0);
				resetConstraints("xy");
				constraintsLayout.setVisible(false);
			}
			
		}
		
	};
	private void setVariables() {			
		update.addStyleDependentName("APPLY-NEEDED");
		String vix = xVariables.getVariable(xVariables.getSelectedIndex()).getID();
		String viy = yVariables.getVariable(yVariables.getSelectedIndex()).getID();
		lasRequest.removeVariables();
		lasRequest.addVariable(dsid, vix, 0);
		lasRequest.addVariable(dsid, viy, 0);
		if ( colorCheckBox.getValue() ) {
			String varColor = colorVariables.getVariable(colorVariables.getSelectedIndex()).getID();
			lasRequest.addVariable(dsid, varColor, 0);
		}
	}
	private void resetConstraints(String vars) {
		xVariableConstraint.setApply(false);
		yVariableConstraint.setApply(false);
		if ( vars.contains("x") ) {
			xVariableConstraint.setConstraint("", "");
		}
		if ( vars.contains("y") ) {
			yVariableConstraint.setConstraint("", "");
		}
	}
	private void setConstraints() {
		update.addStyleDependentName("APPLY-NEEDED");
		lasRequest.removeConstraints();
		String varY = yVariables.getVariable(yVariables.getSelectedIndex()).getID();
		String varX = xVariables.getVariable(xVariables.getSelectedIndex()).getID();
		if ( xVariableConstraint.getApply().getValue() && xVariableConstraint.isActive() ) {
			String min = xVariableConstraint.getMin();
			String max = xVariableConstraint.getMax();
			if ( min != null && !min.equals("") ) {
				lasRequest.addVariableConstraint(dsid, varX, "gt", min, "minx");
			}
			if ( max != null && !max.equals("") ) {
				lasRequest.addVariableConstraint(dsid, varX, "le", max, "maxx");
			}
		}
		
		if ( yVariableConstraint.getApply().getValue() && yVariableConstraint.isActive() ) {
			String min = yVariableConstraint.getMin();
			String max = yVariableConstraint.getMax();
			if ( min != null && !min.equals("") ) {
				lasRequest.addVariableConstraint(dsid, varY, "gt", min, "miny");
			}
			if ( max != null && !max.equals("") ) {
				lasRequest.addVariableConstraint(dsid, varY, "le", max, "maxy");
			}
		}
		List<VariableConstraintWidget> oc = constraintsLayout.getWidgets();
		for (Iterator cwIt = oc.iterator(); cwIt.hasNext();) {
			VariableConstraintWidget cw = (VariableConstraintWidget) cwIt.next();
			if ( cw.getApply().getValue() ) {
				String min = cw.getMin();
				String max = cw.getMax();
				String id = cw.getVariable().getID();
				if ( min != null && !min.equals("") ) {
					lasRequest.addVariableConstraint(dsid, id, "gt", min, "min_"+id);
				}
				if ( max != null && !max.equals("") ) {
					lasRequest.addVariableConstraint(dsid, id, "le", max, "max_"+id);
				}
			}
		}
		// Remove any variables already showing as constraints from the list.
		constraintsLayout.restore();
		constraintsLayout.removeItem(xVariableConstraint.getVariable());
		constraintsLayout.removeItem(yVariableConstraint.getVariable());
		for (Iterator cwIt = oc.iterator(); cwIt.hasNext();) {
			VariableConstraintWidget cw = (VariableConstraintWidget) cwIt.next();
			constraintsLayout.removeItem(cw.getVariable());
		}
	}
	private void clearConstraint(String axis) {
		if ( axis.equals("y") ) {
			yVariableConstraint.setConstraint("", "");
			yVariableConstraint.setApply(false);
		} else if ( axis.equals("x") ) {
			xVariableConstraint.setConstraint("", "");
			xVariableConstraint.setApply(false);
		}
	}
	private void popHistory(String xml) {
		lasRequest = new LASRequest(xml);
		
		String vx = lasRequest.getVariable(0);
		String vy = lasRequest.getVariable(1);
		String vc = lasRequest.getVariable(2);
		if ( vx != null && !vx.equals("") ) {
			xVariables.setSelectedVariable(vx);
			VariableSerializable varX = xVariables.getVariable(xVariables.getSelectedIndex());
			xVariableConstraint.setVariable(varX);
			if ( varX.getName().toLowerCase().equals("latitude") || varX.getName().toLowerCase().equals("longitude") || varX.getName().toLowerCase().equals("time") ) {
				xVariableConstraint.setVisible(false);
				xVariableConstraint.setApply(false);
				xVariableConstraint.setActive(false);
			} else {
				xVariableConstraint.setVisible(true);
				xVariableConstraint.setActive(true);
			}
		}
		if ( vy != null && !vy.equals("") ) {
			yVariables.setSelectedVariable(vy);
			VariableSerializable varY = yVariables.getVariable(yVariables.getSelectedIndex());
			yVariableConstraint.setVariable(varY);
			if ( varY.getName().toLowerCase().equals("latitude") || varY.getName().toLowerCase().equals("longitude") || varY.getName().toLowerCase().equals("time") ) {
				yVariableConstraint.setVisible(false);
				yVariableConstraint.setApply(false);
				yVariableConstraint.setActive(false);
			} else {
				yVariableConstraint.setVisible(true);
				yVariableConstraint.setActive(true);
			}
		}
		if ( vc != null && !vc.equals("") ) {
			colorVariables.setSelectedVariable(vc);
			colorCheckBox.setValue(true);
			colorVariables.setEnabled(true);
		} else {
			colorCheckBox.setValue(false);
			colorVariables.setEnabled(false);
		}
		
		String xlo = lasRequest.getRangeLo("x", 0);
		String xhi = lasRequest.getRangeHi("x", 0);
		String ylo = lasRequest.getRangeLo("y", 0);
		String yhi = lasRequest.getRangeHi("y", 0);
		if ( xlo != null && !xlo.equals("") && xhi != null && !xhi.equals("") && ylo != null && !ylo.equals("") && yhi != null && !yhi.equals("") ) {
			mapConstraint.setCurrentSelection(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
		}
		
		String tlo = lasRequest.getRangeLo("t", 0);
		String thi = lasRequest.getRangeHi("t", 0);
		if (  tlo != null && !tlo.equals("") && thi != null && !thi.equals("") ) {
			timeConstraint.setLo(tlo);
			timeConstraint.setHi(thi);
		}
		
		String zlo = lasRequest.getRangeLo("t", 0);
		String zhi = lasRequest.getRangeHi("t", 0);
		if (  zlo != null && !zlo.equals("") && zhi != null && !zhi.equals("") ) {
			zAxisWidget.setLo(zlo);
			zAxisWidget.setHi(zhi);
		}

		List<Map<String, String>> vcons= lasRequest.getVariableConstraints();
		constraintsLayout.reset();
		
		if ( vcons.size() > 0 ) {
			for (Iterator vconsIt = vcons.iterator(); vconsIt.hasNext();) {
				Map<String, String> con = (Map<String, String>) vconsIt.next();
				String varid = con.get("varID");
				String op = con.get("op");
				String value = con.get("value");
				String id = con.get("id");
				if ( id.equals("minx") ) {
					if ( varid.equals(vx) ) {
						xVariableConstraint.setMin(value);
						xVariableConstraint.setApply(true);
					} else {
						clearConstraint("x");
					}
				} else if ( id.equals("maxx") ) {
					if ( varid.equals(vx) ) {
						xVariableConstraint.setMax(value);
						xVariableConstraint.setApply(true);
					} else {
						clearConstraint("x");
					}
				} else if ( id.equals("miny") ) {
					if ( varid.equals(vy) ) {
						yVariableConstraint.setMin(value);
						yVariableConstraint.setApply(true);
					} else {
						clearConstraint("y");
					}
				} else if ( id.equals("maxy") ) {
					if ( varid.equals(vy) ) {
						yVariableConstraint.setMax(value);
						yVariableConstraint.setApply(true);
					} else {
						clearConstraint("y");
					}
				} else {
					// This is an additional constraint.  
					// First see if it's there already.
					VariableConstraintWidget vcw = null;
					for (Iterator varsIt = constraintsLayout.getWidgets().iterator(); varsIt.hasNext();) {
						VariableConstraintWidget v = (VariableConstraintWidget) varsIt.next();
						if ( v.getVariable().getID().equals(varid) ) {
							vcw = v;
						}
					}
					// If not, build it.
					if ( vcw == null ) {
						vcw = new VariableConstraintWidget(true);
						vcw.addRemoveHandler(new ClickHandler() {

							@Override
							public void onClick(ClickEvent event) {
								removeHandlerHelper(event);
							}

						});

						vcw.setVariable(xDatasetVariables.get(varid));
						constraintsLayout.addWidget(vcw);
					}
					if ( id.contains("max") ) {
						vcw.setMax(value);
						vcw.setApply(true);
					} else {
						vcw.setMin(value);
						vcw.setApply(true);
					}
				}
			} 
		} else {
			xVariableConstraint.setApply(false);
			yVariableConstraint.setApply(false);
		}
		
		setVariables();
		
		updatePlot(false);

	}
	private void removeHandlerHelper(ClickEvent event) {
		PushButton source = (PushButton) event.getSource();
		String id = source.getElement().getId();
		List<VariableConstraintWidget> remove = new ArrayList<VariableConstraintWidget>();
		for (Iterator vcwIt = constraintsLayout.getWidgets().iterator(); vcwIt.hasNext();) {
			VariableConstraintWidget vcw = (VariableConstraintWidget) vcwIt.next();
			String vid = "other-"+ vcw.getVariable().getID();
			if (vid.equals(id) ) {
				remove.add(vcw);
			}
		}
		
		for (Iterator vcwIt = remove.iterator(); vcwIt.hasNext();) {
			VariableConstraintWidget vcw = (VariableConstraintWidget) vcwIt.next();
			constraintsLayout.removeWidget(vcw);
		}
       
        // Now the the constraint widgets are set, set the constraints in the request object.
        setConstraints();
	}
	private void pushHistory(String xml) {
		History.newItem(xml, false);
	}
	private String plotVariable(String id) {
		VariableSerializable varX = xVariables.getVariable(xVariables.getSelectedIndex());
		VariableSerializable varY = yVariables.getVariable(yVariables.getSelectedIndex());
		if ( varX.getID().equals(id) ) return "x";
		if ( varY.getID().equals(id) ) return "y";
		return "";
	}
	ChangeHandler constraintChange = new ChangeHandler() {

		@Override
		public void onChange(ChangeEvent event) {
			TextBox w = (TextBox) event.getSource();
			String id = w.getElement().getId();
			int index = Integer.valueOf(id.substring(id.indexOf("-")+1, id.length()));
			VariableConstraintLayout vl = (VariableConstraintLayout) ((FlexTable) w.getParent()).getParent();
			vl.setApply(index, true);
			setConstraints();				
		}
		
	};
	
	ClickHandler applyHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent arg0) {
			update.addStyleDependentName("APPLY-NEEDED");
			setConstraints();
		}
		
	};
}
