package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.LASRequestWrapper;
import gov.noaa.pmel.tmap.las.client.laswidget.VariableListBox;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.CanvasElement;
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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
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
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

public class Correlation implements EntryPoint {
	FlexTable constraintsLayout = new FlexTable();
	TextBox xminBox = new TextBox();
	TextBox xmaxBox = new TextBox();
	TextBox yminBox = new TextBox();
	TextBox ymaxBox = new TextBox();
	Label xVariableLabel = new Label("x");
	Label yVariableLabel = new Label("y");
	CheckBox useXConstraint = new CheckBox();
	CheckBox useYConstraint = new CheckBox();
	NumberFormat dFormat = NumberFormat.getFormat("########.##");
	PopupPanel box = new PopupPanel();
	FlexTable boxLayout = new FlexTable();
	Label selection = new Label("Current selection:");
	Label horizontalLabel = new Label("Horizontal: ");
	Label verticalLabel = new Label("Vertical: ");
	Label chorizontalLabel = new Label("Horizontal: ");
	Label cverticalLabel = new Label("Vertical: ");
	Label horizontalValue = new Label("nada");
	Label verticalValue = new Label("nada");
	FlexTable topPanel = new FlexTable();
	FlexTable uiPanel = new FlexTable();
    VariableListBox xVariables = new VariableListBox();
    VariableListBox yVariables = new VariableListBox();
    VariableListBox colorVariables = new VariableListBox();
    PushButton update = new PushButton("Update Plot");
    CheckBox colorCheckBox = new CheckBox("Use color variable");
    LASRequestWrapper lasRequest;
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
    @Override
	public void onModuleLoad() {
    	
    	constraintsLayout.setWidget(0, 0, cverticalLabel);
    	constraintsLayout.setWidget(0, 1, yminBox);
    	constraintsLayout.setWidget(0, 2, yVariableLabel);
    	constraintsLayout.setWidget(0, 3, ymaxBox);
    	constraintsLayout.setWidget(0, 4, useYConstraint);
    	
    	constraintsLayout.setWidget(1, 0, chorizontalLabel);
    	constraintsLayout.setWidget(1, 1, xminBox);
    	constraintsLayout.setWidget(1, 2, xVariableLabel);
    	constraintsLayout.setWidget(1, 3, xmaxBox);
    	constraintsLayout.setWidget(1, 4, useXConstraint);
    	
    	boxLayout.setWidget(0, 0, selection);
    	boxLayout.setWidget(1, 0, horizontalLabel);
    	boxLayout.setWidget(1, 1, horizontalValue);
    	boxLayout.setWidget(2, 0, verticalLabel);
    	boxLayout.setWidget(2, 1, verticalValue);
    	box.add(boxLayout);
		topPanel.setWidget(0, 0, yVariables);
		topPanel.setWidget(0, 1, new Label("Y as a function of"));
		topPanel.setWidget(0, 2, xVariables);
		topPanel.setWidget(0, 3, update);
		topPanel.setWidget(1, 0, new Label("Colored By"));
		topPanel.setWidget(1, 1, colorVariables);
		topPanel.setWidget(1, 2, colorCheckBox);
		uiPanel.setWidget(0, 0, topPanel);
		uiPanel.setWidget(1, 0, constraintsLayout);
		update.addClickHandler(updateClick);
		String xml = Util.getParameterString("xml");
		if ( xml != null && !xml.equals("") ) {
			lasRequest = new LASRequestWrapper(xml);
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
				
				
			}
		});
		xVariables.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				String varX = xVariables.getVariable(xVariables.getSelectedIndex()).getName();
				xVariableLabel.setText("<= "+varX+" <=");
			}
		});
		RootPanel.get("correlation").add(uiPanel);
	}
	ClickHandler updateClick = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			lasRequest.removeVariables();
			String varY = yVariables.getVariable(yVariables.getSelectedIndex()).getID();
			lasRequest.addVariable(dsid, varY);
			String varX = xVariables.getVariable(xVariables.getSelectedIndex()).getID();
			lasRequest.addVariable(dsid, varX);
			if ( colorCheckBox.getValue() ) {
				String varColor = colorVariables.getVariable(colorVariables.getSelectedIndex()).getID();
				lasRequest.addVariable(dsid, varColor);
			}
			String tlo = lasRequest.getRangeHi("t", 0);
			String thi = lasRequest.getRangeLo("t", 0);
			String xlo = lasRequest.getRangeHi("x", 0);
			String xhi = lasRequest.getRangeLo("x", 0);
			String yhi = lasRequest.getRangeHi("y", 0);
			String ylo = lasRequest.getRangeLo("y", 0);
			String zhi = lasRequest.getRangeHi("z", 0);
			String zlo = lasRequest.getRangeLo("z", 0);
			lasRequest.removeRegion(0);
			
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
			lasRequest.addProperty("las", "output_type", "xml");
			lasRequest.setOperation(operationID, operationType);
			lasRequest.removeConstraints();
			if ( useXConstraint.getValue() ) {
				String min = xminBox.getText();
				String max = xmaxBox.getText();
			    lasRequest.addVariableConstraint(dsid, varX, "gt", min);
			    lasRequest.addVariableConstraint(dsid, varX, "le", max);
			}
			
			if ( useYConstraint.getValue() ) {
				String min = yminBox.getText();
				String max = ymaxBox.getText();
				lasRequest.addVariableConstraint(dsid, varY, "gt", min);
				lasRequest.addVariableConstraint(dsid, varX, "le", max);
			}
			
			frontCanvas = Canvas.createIfSupported();
			frontCanvasContext = frontCanvas.getContext2d();
			
			int rndRedColor = 190;
			int rndGreenColor = 40;
			int rndBlueColor = 40;
			double rndAlpha = .25;

			randomColor = CssColor.make("rgba(" + rndRedColor + ", " + rndGreenColor + "," + rndBlueColor + ", " + rndAlpha + ")");
			
			String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.getXMLText());
			
				currentURL = url;
				RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
				try {
					sendRequest.sendRequest(null, lasRequestCallback);
				} catch (RequestException e) {
					HTML error = new HTML(e.toString());
					uiPanel.setWidget(2, 0, error);
				}
			
		}
		
	};
	
	RequestCallback lasRequestCallback = new RequestCallback() {

		@Override
		public void onError(Request request, Throwable exception) {

			Window.alert("Product request failed.");

		}

		@Override
		public void onResponseReceived(Request request, Response response) {
			String doc = response.getText();
			String imageurl = "";
			// Look at the doc.  If it's not obviously XML, treat it as HTML.
			if ( !doc.substring(0, 100).contains("<?xml") ) {
				HTML result = new HTML(doc, true);
				uiPanel.setWidget(2, 0, result);
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
									uiPanel.setWidget(2, 0, error);
								}
							}
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

					uiPanel.setWidget(2, 0, image);
					if ( frontCanvas != null ) {
						image.setVisible(false);
						image.addLoadHandler(new LoadHandler() {

							@Override
							public void onLoad(LoadEvent event) {
								
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
											
											box.setPopupPosition(frontCanvas.getAbsoluteLeft(), frontCanvas.getAbsoluteTop());
											box.show();
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
											horizontalValue.setText(dFormat.format(world_startx)+" to "+dFormat.format(world_endx));
											verticalValue.setText(dFormat.format(world_starty)+" to "+ dFormat.format(world_endy));
											setTextValues();
											frontCanvasContext.setFillStyle(randomColor);
											frontCanvasContext.drawImage(ImageElement.as(image.getElement()), 0, 0);
											frontCanvasContext.fillRect(startx, starty, currentx - startx, currenty-starty);
										}
									}
								});
								uiPanel.setWidget(2, 0, frontCanvas);

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
								box.hide();
								draw = false;
							}
						});
					}
				}
				world_startx = x_axis_lower_left;
                world_endx = x_axis_upper_right;
                world_starty = y_axis_lower_left;
                world_endy = y_axis_upper_right;
                setTextValues();
			}
		}
	};
	private void setTextValues() {

		if ( world_startx <= world_endx ) {
			
			horizontalValue.setText(dFormat.format(world_startx)+" to "+dFormat.format(world_endx));
		
			xminBox.setText(dFormat.format(world_startx));
			xmaxBox.setText(dFormat.format(world_endx));
				
		} else {
			
			horizontalValue.setText(dFormat.format(world_endx)+" to "+dFormat.format(world_startx));
			
			xminBox.setText(dFormat.format(world_endx));
			xmaxBox.setText(dFormat.format(world_startx));	
			
		}
		
		if ( world_starty <= world_endy ) {
			
			verticalValue.setText(dFormat.format(world_starty)+" to "+ dFormat.format(world_endy));
			
			yminBox.setText(dFormat.format(world_starty));
			ymaxBox.setText(dFormat.format(world_endy));
			
		} else {
			
			verticalValue.setText(dFormat.format(world_endy)+" to "+ dFormat.format(world_starty));
			
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
				xVariables.addItem(variables[i]);
				yVariables.addItem(variables[i]);
				colorVariables.addItem(variables[i]);
				if ( variables[i].getID().equals(varid) ) {
					index = i;
				}
			}
			if ( index > 0 ) {
				yVariables.setSelectedIndex(index);
			}
			String grid_type = xVariables.getVariable(0).getAttributes().get("grid_type");
			if ( grid_type.equals("regular") ) {
				operationID = "prop_prop_plot";
			} else if ( grid_type.equals("trajectory") ) {
				operationID = "Trajectory_correlation";
			}
			String varY = yVariables.getVariable(yVariables.getSelectedIndex()).getName();
			yVariableLabel.setText("<= "+varY+" <=");
			String varX = xVariables.getVariable(xVariables.getSelectedIndex()).getName();
			xVariableLabel.setText("<= "+varX+" <=");
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
}
