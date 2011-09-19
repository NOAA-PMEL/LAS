package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.HelpPanel;
import gov.noaa.pmel.tmap.las.client.laswidget.LASAnnotationsPanel;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequest;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequestWrapper;
import gov.noaa.pmel.tmap.las.client.laswidget.VariableListBox;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.URLUtil;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.EntryPoint;
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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

public class Correlation implements EntryPoint {
	LASAnnotationsPanel lasAnnotationsPanel = new LASAnnotationsPanel();
	HorizontalPanel topRow = new HorizontalPanel();
	HelpPanel help = new HelpPanel();
	PopupPanel spin;
	HTML spinImage;
	HorizontalPanel coloredBy = new HorizontalPanel();
	HorizontalPanel corTopRow = new HorizontalPanel();
	FlexTable constraintsLayout = new FlexTable();
	VerticalPanel constraintsPanel = new VerticalPanel();
	HelpPanel corHelp = new HelpPanel();
	TextBox xminBox = new TextBox();
	TextBox xmaxBox = new TextBox();
	TextBox yminBox = new TextBox();
	TextBox ymaxBox = new TextBox();
	Label xVariableLabel = new Label("x");
	Label yVariableLabel = new Label("y");
	CheckBox useXConstraint = new CheckBox();
	CheckBox useYConstraint = new CheckBox();
	NumberFormat dFormat = NumberFormat.getFormat("########.##");
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
    @Override
	public void onModuleLoad() {
    	String spinImageURL = URLUtil.getImageURL()+"/mozilla_blu.gif";
		spinImage = new HTML("<img src=\""+spinImageURL+"\" alt=\"Spinner\"/>");
		spin = new PopupPanel();
		spin.add(spinImage);
    	update.addStyleDependentName("SMALLER");
    	update.setWidth("80px");
    	
    	useXConstraint.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				setConstraints();
				
			}
    		
    	});
    	useYConstraint.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				setConstraints();
				
			}
		});
    	
    	xminBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				useXConstraint.setValue(true);
				setConstraints();
			}
		});
    	xmaxBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				useXConstraint.setValue(true);
				setConstraints();
			}
		});
    	yminBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				useYConstraint.setValue(true);
				setConstraints();
			}
		});
    	ymaxBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {	
				useYConstraint.setValue(true);
				setConstraints();
			}
		});
    	corTopRow.add(corHelp);
    	corTopRow.add(new HTML("<b>&nbsp;&nbsp;Data Constraints:</b>"));
    	constraintsLayout.setWidget(0, 0, corTopRow);
    	constraintsLayout.getFlexCellFormatter().setColSpan(0, 0, 4);
    	constraintsLayout.setWidget(1, 0, yminBox);
    	constraintsLayout.setWidget(1, 1, yVariableLabel);
    	constraintsLayout.setWidget(1, 2, ymaxBox);
    	constraintsLayout.setWidget(1, 3, useYConstraint);

    	constraintsLayout.setWidget(2, 0, xminBox);
    	constraintsLayout.setWidget(2, 1, xVariableLabel);
    	constraintsLayout.setWidget(2, 2, xmaxBox);
    	constraintsLayout.setWidget(2, 3, useXConstraint);
    	    	
    	constraintsPanel.add(constraintsLayout);
    	corHelp.setPopupWidth("550px");
    	corHelp.setPopupHeight("550px");
    	corHelp.setHelpURL("../css/constraint_help.html");
    	constraintsPanel.setVisible(false);
    	print.addStyleDependentName("SMALLER");
    	print.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				printerFriendly();
			}
    		
    	});
    	print.setEnabled(false);
    	topRow.add(help);
    	topRow.add(new HTML("<b>&nbsp;&nbsp;Data Selection: </b>"));
    	controlPanel.setWidget(0, 0, topRow);
		controlPanel.getFlexCellFormatter().setColSpan(0, 0, 6);
    	controlPanel.setWidget(1, 0, yVariables);
		controlPanel.setWidget(1, 1, new Label(" as a function of "));
		controlPanel.setWidget(1, 2, xVariables);
		controlPanel.setWidget(1, 3, update);	
		controlPanel.setWidget(1, 4, print);
		controlPanel.setWidget(1, 5, lasAnnotationsPanel);
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
			lasRequest = new LASRequest(xml);
			dsid = lasRequest.getDataset(0);		
			varid = lasRequest.getVariable(0);
			Util.getRPCService().getFullDataset(dsid, datasetCallback);
		} else {
			
		}
		yVariables.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				
				String varY = yVariables.getVariable(yVariables.getSelectedIndex()).getName();
				yVariableLabel.setText("<= "+varY+" <=");
			
				setVariables();
				resetConstraints("y");

			}
		});
		xVariables.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				String varX = xVariables.getVariable(xVariables.getSelectedIndex()).getName();
				xVariableLabel.setText("<= "+varX+" <=");

				setVariables();
				resetConstraints("x");
			
			}
		});
		RootPanel.get("data_selection").add(topPanel);
		RootPanel.get("data_constraints").add(constraintsPanel);
		RootPanel.get("correlation").add(outputPanel);
		lasAnnotationsPanel.setPopupLeft(outputPanel.getAbsoluteLeft());
		lasAnnotationsPanel.setPopupTop(outputPanel.getAbsoluteTop());
		lasAnnotationsPanel.setTitle("Plot Annotations");
		lasAnnotationsPanel.setError("Click \"Update plot\" to refresh the plot.&nbsp;");
		History.addValueChangeHandler(historyHandler);
	}
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
    	update.removeStyleDependentName("APPLY-NEEDED");
    	lasAnnotationsPanel.setTitle("Plot Annotations");
		lasAnnotationsPanel.setError("Fetching plot annotations...");
    	if ( xVariables.getSelectedIndex() == yVariables.getSelectedIndex() ) {
    		Window.alert("The same variable on both axes is going to be a straight line.  I just can't bring myself to plot that.");
    	} else {
    		spin.setPopupPosition(outputPanel.getAbsoluteLeft(), outputPanel.getAbsoluteTop());
    		spin.show();
    		String tlo = lasRequest.getRangeLo("t", 0);
    		String thi = lasRequest.getRangeHi("t", 0);
    		String xlo = lasRequest.getRangeLo("x", 0);
    		String xhi = lasRequest.getRangeHi("x", 0);
    		String ylo = lasRequest.getRangeLo("y", 0);
    		String yhi = lasRequest.getRangeHi("y", 0);
    		String zlo = lasRequest.getRangeLo("z", 0);
    		String zhi = lasRequest.getRangeHi("z", 0);

    		// If it's defined it will be in the hi or both.
    		if ( tlo != null && thi != null ) {
    			lasRequest.setRange("t", tlo, thi, 0);
    		} else if ( thi != null ) {
    			lasRequest.setRange("t", thi, thi, 0);
    		}

    		if ( xlo != null && xhi != null ) {
    			lasRequest.setRange("x", xlo, xhi, 0);
    		} else if ( thi != null ) {
    			lasRequest.setRange("x", xhi, xhi, 0);
    		}

    		if ( ylo != null && yhi != null ) {
    			lasRequest.setRange("y", ylo, yhi, 0);
    		} else if ( yhi != null ) {
    			lasRequest.setRange("y", yhi, yhi, 0);
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
    			outputPanel.setWidget(0, 0, error);
    		}
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
				outputPanel.setWidget(0, 0, result);
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
									outputPanel.setWidget(0, 0, error);
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
						outputPanel.setWidget(1, 0, image);
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
											useXConstraint.setValue(true);
											useYConstraint.setValue(true);
											
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
								outputPanel.setWidget(0, 0, frontCanvas);
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
						outputPanel.setWidget(0, 0, image);
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

		constraintsPanel.setVisible(true);
		
		if ( world_startx <= world_endx ) {
		
			xminBox.setText(dFormat.format(world_startx));
			xmaxBox.setText(dFormat.format(world_endx));
				
		} else {
						
			xminBox.setText(dFormat.format(world_endx));
			xmaxBox.setText(dFormat.format(world_startx));	
			
		}
		
		if ( world_starty <= world_endy ) {
			
			yminBox.setText(dFormat.format(world_starty));
			ymaxBox.setText(dFormat.format(world_endy));
			
		} else {
						
			yminBox.setText(dFormat.format(world_endy));
			ymaxBox.setText(dFormat.format(world_starty));
			
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
			for (int i = 0; i < variables.length; i++) {
				if ( !variables[i].getAttributes().get("grid_type").equals("vector") ) {
					xVariables.addItem(variables[i]);
					yVariables.addItem(variables[i]);
					colorVariables.addItem(variables[i]);
					if ( variables[i].getID().equals(varid) ) {
						index = i;
					}
				}
			}
			if ( index > 0 ) {
				yVariables.setSelectedIndex(index);
			}
			String grid_type = xVariables.getVariable(0).getAttributes().get("grid_type");
			if ( grid_type.equals("regular") ) {
				operationID = "prop_prop_plot";
			} else if ( grid_type.equals("trajectory") ) {
				operationID = "Trajectory_correlation_plot";
			}
			String varY = yVariables.getVariable(yVariables.getSelectedIndex()).getName();
			yVariableLabel.setText("<= "+varY+" <=");
			String varX = xVariables.getVariable(xVariables.getSelectedIndex()).getName();
			xVariableLabel.setText("<= "+varX+" <=");
			setVariables();
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
				outputPanel.removeCell(0, 0);
				xVariables.setSelectedIndex(0);
				yVariables.setSelectedIndex(0);
				resetConstraints("xy");
				constraintsPanel.setVisible(false);
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
		useXConstraint.setValue(false);
		useYConstraint.setValue(false);
		if ( vars.contains("x") ) {
			xminBox.setText("");
			xmaxBox.setText("");
		}
		if ( vars.contains("y") ) {
			yminBox.setText("");
			ymaxBox.setText("");
		}
	}
	private void setConstraints() {
		update.addStyleDependentName("APPLY-NEEDED");
		lasRequest.removeConstraints();
		String varY = yVariables.getVariable(yVariables.getSelectedIndex()).getID();
		String varX = xVariables.getVariable(xVariables.getSelectedIndex()).getID();
		if ( useXConstraint.getValue() ) {
			String min = xminBox.getText();
			String max = xmaxBox.getText();
		    lasRequest.addVariableConstraint(dsid, varX, "gt", min, "minx");
		    lasRequest.addVariableConstraint(dsid, varX, "le", max, "maxx");
		}
		
		if ( useYConstraint.getValue() ) {
			String min = yminBox.getText();
			String max = ymaxBox.getText();
			lasRequest.addVariableConstraint(dsid, varY, "gt", min, "miny");
			lasRequest.addVariableConstraint(dsid, varY, "le", max, "maxy");
		}
	}
	private void clearConstraint(String axis) {
		if ( axis.equals("y") ) {
			yminBox.setText("");
			ymaxBox.setText("");
			useYConstraint.setValue(false);
		} else if ( axis.equals("x") ) {
			xminBox.setText("");
			xmaxBox.setText("");
			useXConstraint.setValue(false);
		}
	}
	private void popHistory(String xml) {
		lasRequest = new LASRequest(xml);
		
		String vx = lasRequest.getVariable(0);
		String vy = lasRequest.getVariable(1);
		String vc = lasRequest.getVariable(2);
		if ( vx != null && !vx.equals("") ) {
			xVariables.setSelectedVariable(vx);
			String varX = xVariables.getVariable(xVariables.getSelectedIndex()).getName();
			xVariableLabel.setText("<= "+varX+" <=");
		}
		if ( vy != null && !vy.equals("") ) {
			yVariables.setSelectedVariable(vy);
			String varY = yVariables.getVariable(yVariables.getSelectedIndex()).getName();
			yVariableLabel.setText("<= "+varY+" <=");
			
		}
		if ( vc != null && !vc.equals("") ) {
			colorVariables.setSelectedVariable(vc);
			colorCheckBox.setValue(true);
		} else {
			colorCheckBox.setValue(false);
		}

		List<Map<String, String>> vcons= lasRequest.getVariableConstraints();
		if ( vcons.size() > 0 ) {
			for (Iterator vconsIt = vcons.iterator(); vconsIt.hasNext();) {
				Map<String, String> con = (Map<String, String>) vconsIt.next();
				String varid = con.get("varID");
				String op = con.get("op");
				String value = con.get("value");
				String id = con.get("id");
				if ( id.equals("minx") ) {
					if ( varid.equals(vx) ) {
						xminBox.setText(value);
						useXConstraint.setValue(true);
					} else {
						clearConstraint("x");
					}
				}
				if ( id.equals("maxx") ) {
					if ( varid.equals(vx) ) {
						xmaxBox.setText(value);
						useXConstraint.setValue(true);
					} else {
						clearConstraint("x");
					}
				}
				if ( id.equals("miny") ) {
					if ( varid.equals(vy) ) {
						yminBox.equals(vy);
						useYConstraint.setValue(true);
					} else {
						clearConstraint("y");
					}
				}
				if ( id.equals("maxy") ) {
					if ( varid.equals(vy) ) {
						ymaxBox.equals(vy);
						useYConstraint.setValue(true);
					} else {
						clearConstraint("y");
					}
				}
			} 
		} else {
			useXConstraint.setValue(false);
			useYConstraint.setValue(false);
		}
		
		setVariables();
		
		updatePlot(false);

	}
	private void pushHistory(String xml) {
		History.newItem(xml, false);
	}
}
