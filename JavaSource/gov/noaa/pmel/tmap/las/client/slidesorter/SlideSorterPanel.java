package gov.noaa.pmel.tmap.las.client.slidesorter;
//TODO handle error and batch responses...
import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DatasetButton;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequestWrapper;
import gov.noaa.pmel.tmap.las.client.map.SettingsButton;
import gov.noaa.pmel.tmap.las.client.serializable.AxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import java.util.List;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
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
public class SlideSorterPanel extends Composite {
	
	/* The base widget used to layout the other widgets.  A single column of three rows. */
	Grid grid;
	
	/* The top bar of widgets... */
	HorizontalPanel top;
	
	/* The current data set and variable.  */
	Label datasetLabel;
	
	/* This is the request.  It is either built from scratch or passed in via the constructor.  */
	LASRequestWrapper lasRequest = new LASRequestWrapper();
	
	// Keep track of the values that are currently being used as the fixed axis and compare axis
	String compareAxis;
	String fixedAxis;
	String fixedAxisValue;
	// List of all axes orthogonal to the view.  Should be a max of 2.
	List<String> ortho;
	
	/*
	 * A a button that pops up a panel for region selection, orthogonal axis selection and maybe plot options.
	 */
	SettingsButton settingsButton;
	
	/* The initialization parameters from the URL that kicked off this slide sorter.  NO NEEDED since we're keeping the variable around.*/

	String view;
	String op;
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
	
	/* A label for when the panel is using "Panel Settings" */
	 
	Label panelSettings = new Label("(Using panel settings.)");
	
	// The view will have at most two axes,  these hold the ranges for those axes.
	String xlo;
	String xhi;
	
	String ylo;
	String yhi;
	
	String ID;
	
	RPCServiceAsync rpcService;
	int pwidth;
	
	/**
	 * Builds a SlideSorter panel with a default plot for the variable.  See {@code}SlideSorter(LASRequest) if you want more options on the initial plot.
	 */
	public SlideSorterPanel(String id, VariableSerializable var, List<String>ortho, String op, String compareAxis, String fixedAxis, String view, String productServer, RPCServiceAsync rpcService) {
		this.ID = id;
		this.op = op;
		this.productServer = productServer;
		this.view = view;
		this.compareAxis = compareAxis;
		this.fixedAxis = fixedAxis;
		this.var = var;
		this.rpcService = rpcService;
		dateTimeWidget.addChangeListener(comparisonAxisChangeListener);
		zAxisWidget.addChangeListener(comparisonAxisChangeListener);
		spin = new PopupPanel();
		spinImage = new HTML("<img src=\"../JavaScript/components/mozilla_blu.gif\" alt=\"Spinner\"/>");
		spin.add(spinImage);
		
		grid = new Grid(3, 1);
		
		grid.setStyleName("regularBackground");
		datasetLabel = new Label();
		
		top = new HorizontalPanel();
		settingsButton = new SettingsButton(LatLng.newInstance(0.0, 0.0), 1, 256, 360, ID, rpcService);
		settingsButton.addApplyClickListener(updateOnClick);
		settingsButton.addCloseClickListener(closeClick);
		settingsButton.addDatasetTreeListener(datasetTreeListener);
		top.add(datasetLabel);
		top.add(settingsButton);
		
		grid.setWidget(0, 0, top);
		HTML plot = new HTML();
		plot.setHTML("<img src=\"../JavaScript/components/mozilla_blu.gif\" alt=\"Spinner\"/>");
		grid.setWidget(1, 0, plot);
		initWidget(grid);
		init();	
	}
	public void init() {
		datasetLabel.setText(var.getDSName()+": "+var.getName());
		GridSerializable ds_grid = var.getGrid();
		double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
		double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());
		
		double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
		double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());
		
		double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));
		
		LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(grid_south, grid_west), LatLng.newInstance(grid_north, grid_east));
		settingsButton.getRefMap().initDataBounds(bounds, delta, true);
		settingsButton.setOperations(rpcService, null, var.getDSID(), var.getID(), null);
		settingsButton.removeAxes();
		if ( ds_grid.getTAxis() != null ) {
			 dateTimeWidget.init(ds_grid.getTAxis(), false);
			 dateTimeWidget.setVisible(true);
			 settingsButton.getDateWidget().init(ds_grid.getTAxis(), false);
			 if ( compareAxis.equals("t") ) {
				 grid.setWidget(2, 0, dateTimeWidget);
			 }
		}
		if ( ds_grid.getZAxis() != null ) {
			zAxisWidget.init(ds_grid.getZAxis());
			zAxisWidget.setVisible(true);
			settingsButton.getZWidget().init(ds_grid.getZAxis());
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
			String tlo = settingsButton.getDateWidget().getFerretDateLo();
			dateTimeWidget.setLo(tlo);
			grid.setWidget(2, 0, dateTimeWidget);
		} else {
			String zlo = settingsButton.getZWidget().getSelectedValue();
			zAxisWidget.setLo(zlo);
			grid.setWidget(2, 0, zAxisWidget);
		}
		if ( isUsePanelSettings() ) {
			grid.setWidget(2, 0, panelSettings);
		}
	}
	// The SliderSorter needs to pass in which axis it is controlling and the value it should have...
	public void refreshPlot(boolean switchAxis, boolean popup) {
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
            lasRequest.setRange("t", settingsButton.getDateWidget().getFerretDateLo(), settingsButton.getDateWidget().getFerretDateHi(), 0);
		}
		


		// TODO -- All of these should first check to see if the axis is in the view.  If it is, get the value from the axis on the grid,
		// or get it from the request that was passed in.
		xlo = settingsButton.getRefMap().getXlo();
    	xhi = settingsButton.getRefMap().getXhi();
    	ylo = settingsButton.getRefMap().getYlo();
    	yhi = settingsButton.getRefMap().getYhi();
    	
		//TODO Then if it's not in the view, get the value from the menu or from the fixed value.
		if ( view.contains("x") ) {
			lasRequest.setRange("x", xlo, xhi, 0);
		}
		if ( view.contains("y") ) {
			lasRequest.setRange("y", ylo, yhi, 0);
		}
		
		if ( var.getGrid().getZAxis() != null ) {
			lasRequest.setRange("z", settingsButton.getZWidget().getSelectedValue(), settingsButton.getZWidget().getSelectedValue(), 0);
		}
		lasRequest.addProperty("ferret", "image_format", "gif");
		lasRequest.addProperty("las", "output_type", "xml");
		String url = productServer+"?xml="+URL.encode(lasRequest.getXMLText());
		RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
		try {
			sendRequest.sendRequest(null, lasRequestCallback);
		} catch (RequestException e) {
			HTML error = new HTML(e.toString());
			grid.setWidget(1, 0, error);
		}
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
				doc = doc.replaceAll("\n", "");
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
							image.setWidth(pwidth+"px");
							int h = (int) ((631./998.)*Double.valueOf(pwidth));
							image.setHeight(h+"px");
							grid.setWidget(1, 0 , image);
						} else if ( result.getAttribute("type").equals("error") ) {
							if ( result.getAttribute("ID").equals("las_message") ) {
								Node text = result.getFirstChild();
								if ( text instanceof Text ) {
									Text t = (Text) text;
									HTML error = new HTML(t.getData());
									grid.setWidget(1, 0, error);
								}
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
				settingsButton.getDateWidget().setLo(dateTimeWidget.getFerretDateLo());
			} else {
				settingsButton.getZWidget().setLo(zAxisWidget.getSelectedValue());
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
				grid.setStyleName("panelSettingsColor");
				var = (VariableSerializable) v;
				datasetLabel.setText(var.getDSName()+": "+var.getName());
				init();
			}
		}

		public void onTreeItemStateChanged(TreeItem item) {
			// TODO Auto-generated method stub
			
		}
		
	};
	public void setVariable(VariableSerializable v) {
		var = v;
		datasetLabel.setText(var.getDSName()+": "+var.getName());
	}
	public void setCompareAxis(String axis) {
		compareAxis = axis;
	}
	ClickListener updateOnClick = new ClickListener() {
		public void onClick(Widget sender) {
	    	if (settingsButton.getApply().isChecked() ) {
	    	    setCompareAxisVisible(false);
	    	    grid.addStyleName("panelSettingsColor");
	    	    grid.setWidget(2, 0, panelSettings);
	    	} else {
	    		setCompareAxisVisible(true);
	    		grid.setStyleName("regularBackground");
	    		grid.setWidget(2, 0, getCompareWidget());
	    	}	
		}
	};
	ClickListener closeClick = new ClickListener() {
		public void onClick(Widget sender) {
			
	    	if (settingsButton.getApply().isChecked() ) {
	    		setCompareAxisVisible(false);
	    		grid.addStyleName("panelSettingsColor");
	    		grid.setWidget(2, 0, panelSettings);
	    	} else {
	    		setCompareAxisVisible(true);
	    		grid.setWidget(2, 0, getCompareWidget());
	    		if ( compareAxis.equals("t") ) {
	    			setAxisValue("t", dateTimeWidget.getFerretDateLo());
	    		} else if ( compareAxis.equals("z") ) {
	    			setAxisValue("z", zAxisWidget.getSelectedValue());
	    		}
	    		grid.setStyleName("regularBackground");
	    		refreshPlot(false, true);
	    	}
	    		
		}
	};
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
		return settingsButton.getDateWidget().getFerretDateHi();
	}
	public String getTlo() {
		return settingsButton.getDateWidget().getFerretDateLo();
	}

	public String getZhi() {
		//TODO Z CAN BE A RANGE!!
		return settingsButton.getZWidget().getSelectedValue();
	}
	public String getZlo() {
		return settingsButton.getZWidget().getSelectedValue();
	}

	public String getYhi() {
		return settingsButton.getRefMap().getYhi();
	}
	public String getYlo() {
		return settingsButton.getRefMap().getYlo();
	}
	public String getXhi() {
		return settingsButton.getRefMap().getXhi();
	}
	public String getXlo() {
		return settingsButton.getRefMap().getXlo();
	}

	public void setT(String tlo, String thi) {
		settingsButton.getDateWidget().setLo(tlo);
		// TODO also set the hi value
		//mapButton.getDateWidget().setHi(thi);		
	}

	public void setZ(String zlo, String zhi) {
		settingsButton.getZWidget().setLo(zlo);
		// TODO also set hi if it's a range
		// mapButton.getZWidget().setHi(zhi);
	}

	public void setLatLon(String xlo, String xhi, String ylo, String yhi) {
		settingsButton.setLatLon(xlo, xhi, ylo, yhi);
		
	}
	
	public void setPanelWidth(int width) {
		pwidth = width;
		Widget w = grid.getWidget(1, 0);
		int h = (int) ((631./998.)*Double.valueOf(pwidth));
		w.setWidth(width+"px");
		w.setHeight(h+"px");
	}
	public void addRegionChangeListener(ChangeListener listener) {
		settingsButton.getRefMap().addRegionChangeListener(listener);
	}

	public void setAxisValue(String axis, String value) {
		//TODO what if it's a range?
		if ( axis.equals("z") ) {
		    settingsButton.getZWidget().setLo(value);
		} else {
			settingsButton.getDateWidget().setLo(value);
		}
		
	}

	public boolean isUsePanelSettings() {
		return settingsButton.getApply().isChecked();
	}
	public VariableSerializable getVariable() {
		return var;
	}
	public void computeDifference(boolean switchAxis, VariableSerializable variable, String view,
			String xlo_in, String xhi_in, String ylo_in, String yhi_in, String zlo_in,
			String zhi_in, String tlo_in, String thi_in) {
		if (switchAxis) {
			switchAxis();
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
		
		// From the current slidesoter is this comment.
		// Need to add a dummy variable here because the addRegion/AddVariable
		// methods of LASRequest.js will not always add at the end of the <args>
		// node list when there are duplicate <link ...> or <region ...> nodes.
		// Using 'DUMMY' guarantees that addRegion will place the new region at
		// the end.  Then we just replace 'DUMMY'.
		// All this is necessary because order is important in comparison 
		// requests.
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
		if ( isUsePanelSettings() ) {
			if ( !view.contains("x") ) {
				if ( var.getGrid().getXAxis() != null ) {
					lasRequest.setRange("x", settingsButton.getRefMap().getXlo(), settingsButton.getRefMap().getXhi(), 1);
				}
			}
			if ( !view.contains("y") ) {
				if ( var.getGrid().getYAxis() != null ) {
					lasRequest.setRange("y", settingsButton.getRefMap().getYlo(), settingsButton.getRefMap().getYhi(), 1);
				}
			}
			if ( !view.contains("z") ) {
				if ( var.getGrid().getZAxis() != null ) {
					lasRequest.setRange("z", settingsButton.getZWidget().getSelectedValue(), settingsButton.getZWidget().getSelectedValue(), 1);
				}
			}
			if ( !view.contains("t") ) {
				if ( var.getGrid().getTAxis() != null ) {
					lasRequest.setRange("t", settingsButton.getDateWidget().getFerretDateLo(), settingsButton.getDateWidget().getFerretDateHi(), 1);
				}
			}
		} else {
			if ( !view.contains("x") ) {
				if ( var.getGrid().getXAxis() != null ) {
					if ( compareAxis.equals("x") ) {
						//TODO get it from the local compare axis widget.
					} else {
						//TODO get it from the fixed axis in the slide sorter (the passed in value).
					}
				}
			}
			if ( !view.contains("y") ) {
				if ( var.getGrid().getYAxis() != null ) {
					if ( compareAxis.equals("y") ) {
						//TODO get it from the local compare axis widget.
					} else {
						//TODO get it from the fixed axis in the slide sorter (the passed in value).
					}
				}
			}
			if ( !view.contains("z") ) {
				if ( var.getGrid().getZAxis() != null ) {
					if ( compareAxis.equals("z") ) {
						// Use the panel's compare axis widget.
						lasRequest.setRange("z", zAxisWidget.getSelectedValue(), zAxisWidget.getSelectedValue(), 1);
					} else {
						//Use the fixed axis in the slide sorter (the passed in value).
						lasRequest.setRange("z", zlo_in, zhi_in, 1);
					}				
				}
				if ( !view.contains("t") ) {
					if ( var.getGrid().getTAxis() != null ) {
						if ( compareAxis.equals("t") ) {
							// Use the panel's compare axis widget.
							lasRequest.setRange("t", dateTimeWidget.getFerretDateLo(), dateTimeWidget.getFerretDateHi(), 1);
						} else {
							//Use the fixed axis in the slide sorter (the passed in value).
							lasRequest.setRange("t", tlo_in, thi_in, 1);
						}	
					}
				}
			}		
		}
		lasRequest.addProperty("ferret", "image_format", "gif");
		lasRequest.addProperty("las", "output_type", "xml");
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
}
