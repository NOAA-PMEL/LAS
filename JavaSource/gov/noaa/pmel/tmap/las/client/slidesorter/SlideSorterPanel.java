package gov.noaa.pmel.tmap.las.client.slidesorter;
//TODO handle error and batch responses...
import java.util.Iterator;
import java.util.List;

import gov.noaa.pmel.tmap.las.client.ArangeSerializable;
import gov.noaa.pmel.tmap.las.client.AxisSerializable;
import gov.noaa.pmel.tmap.las.client.AxisWidget;
import gov.noaa.pmel.tmap.las.client.DatasetButton;
import gov.noaa.pmel.tmap.las.client.LASDateWidget;
import gov.noaa.pmel.tmap.las.client.LASRequestWrapper;
import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.TimeAxisSerializable;
import gov.noaa.pmel.tmap.las.client.VariableSerializable;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
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
public class SlideSorterPanel extends SlideSorterComposite {
	
	/* The base widget used to layout the other widgets.  A single column of three rows. */
	Grid grid;
	/* The top widget.  A data set/variable selector button hooked to a data set widget tree.  */
	DatasetButton datasetButton;
		
	/* Since I don't know how to register for events on the LASDateWidget, I'll do it myself, by listening on the datePanel. */
	boolean firstClick = true;
	String firstValue;
	
	/* The container for the output. This panel parses the response XML and builds the HTML for this panel.*/
	HTML plot;
	
	/* This is the request.  It is either built from scratch or passed in via the constructor.  */
	LASRequestWrapper lasRequest = new LASRequestWrapper();
	
	// Keep track of the values that are currently being used as the fixed axis and compare axis
	String compareAxis;
	String fixedAxis;
	String fixedAxisValue;
	// List of all axes orthogonal to the view.  Should be a max of 2.
	List<String> ortho;
	
	
	/**/
	String dsid;
	String varid;
	String view;
	String op;
	String productServer;
	
	// Some widgets to show when a panel is being refreshed.
	PopupPanel spin;
	HTML spinImage;
	
	// The view will have at most two axes,  these hold the ranges for those axes.
	String view1lo;
	String view1hi;
	
	String view2lo;
	String view2hi;
	
	/**
	 * Builds a SlideSorter panel with a default plot for the variable.  See {@code}SlideSorter(LASRequest) if you want more options on the initial plot.
	 
	 */
	public SlideSorterPanel(List<AxisSerializable> axes, String dsid, String varid, String op, String compareAxis, String view, String productServer, RPCServiceAsync rpcService) {
		super(axes);
		this.dsid = dsid;
		this.varid = varid;
		this.op = op;
		this.productServer = productServer;
		this.view = view;
		this.compareAxis = compareAxis;
		
		spin = new PopupPanel();
		spinImage = new HTML("<img src=\"../JavaScript/components/mozilla_blu.gif\" alt=\"Spinner\"/>");
		spin.add(spinImage);
		
		grid = new Grid(3, 1);
		
		grid.addStyleName("regularBackground");
		datasetButton = new DatasetButton(rpcService);
		datasetButton.addTreeListener(datasetTreeListener);
		grid.setWidget(0, 0, datasetButton);
		plot = new HTML();
		plot.setHTML("<img src=\"../JavaScript/components/mozilla_blu.gif\" alt=\"Spinner\"/>");
		grid.setWidget(1, 0, plot);
		if ( hasDateWidget() || hasDateMenu() ) {
			if ( hasDateMenu() ) {
				dateMenu.addChangeListener(comparisonAxisChangeListener);
				if ( compareAxis.equals("t") ) grid.setWidget(2,0,dateMenu);
				grid.setWidget(2, 0, dateMenu);
			} else {
				dateWidget.addChangeListener(comparisonAxisChangeListener);
				if ( compareAxis.equals("t") ) grid.setWidget(2,0,dateWidget);
				grid.setWidget(2, 0, dateWidget);
			}			
		} 
		if ( hasXYZMenu() ) {
			xyzMenu.addChangeListener(comparisonAxisChangeListener);
			if ( !compareAxis.equals("t") ) {
			    grid.setWidget(2, 0, xyzMenu);
			}
		}
		
		grid.setWidget(0, 0, datasetButton);
		grid.setWidget(1, 0, plot);
		initWidget(grid);
	}
	public void switchCompareAxis(String compareAxis) {
		this.compareAxis = compareAxis;
		if ( compareAxis.equals("t") ) {
			if ( hasDateMenu() ) {
				grid.setWidget(2, 0, dateMenu);
			} else {
				grid.setWidget(2, 0, dateWidget);
			}
		} else {
			grid.setWidget(2, 0, xyzMenu);
		}	
	}
	// The SliderSorter needs to pass in which axis it is controlling and the value it should have...
	public void refreshPlot(String fixedAxis, String fixedAxisValue, String view1lo, String view1hi, String view2lo, String view2hi, boolean popup) {
		this.fixedAxis = fixedAxis;
		this.fixedAxisValue = fixedAxisValue;
		this.view1lo = view1lo;
		this.view1hi = view1hi;
		this.view2lo = view2lo;
		this.view2hi = view2hi;
		if (popup) {
			spin.setPopupPosition(plot.getAbsoluteLeft(), plot.getAbsoluteTop());
			spin.show();
		}
		
		lasRequest.removeRegion(0);
		lasRequest.removeVariables();
		lasRequest.removePropertyGroup("ferret");
		lasRequest.addVariable(dsid, varid);


		lasRequest.setOperation(op, "v7");
		lasRequest.setProperty("ferret", "view", view);
        
		if ( fixedAxis.equals("t") ) {
			// use the supplied value
			lasRequest.setRange("t", fixedAxisValue, fixedAxisValue, 0);
		} else {
			// get it from the widget
			if ( hasDateMenu() ) {
				lasRequest.setRange("t", dateMenu.getSelectedValue(), dateMenu.getSelectedValue(), 0);
			} else if ( hasDateWidget() ) {
				lasRequest.setRange("t", dateWidget.getFerretDateLo(), dateWidget.getFerretDateLo(), 0);
			}
		}

		// TODO -- All of these should first check to see if the axis is in the view.  If it is, get the value from the axis on the grid,
		// or get it from the request that was passed in.
		
		// Then if it's not in the view, get the value from the menu or from the fixed value.
		if ( view.contains("x") ) {
			lasRequest.setRange("x", view1lo, view1hi, 0);
		}
		if ( view.contains("y") ) {
			lasRequest.setRange("y", view2lo, view2hi, 0);
		}
		
		if ( fixedAxis.equals("z") ) {
		    // Use the supplied value
			lasRequest.setRange("z", fixedAxisValue, fixedAxisValue, 0);
		} else if ( !fixedAxis.equals("none") ) {
			// Get if from the menu
			lasRequest.setRange("z", xyzMenu.getSelectedValue(), xyzMenu.getSelectedValue(), 0);
		}
		lasRequest.addProperty("ferret", "image_format", "gif");
		lasRequest.addProperty("las", "output_type", "xml");
		String url = productServer+"?xml="+URL.encode(lasRequest.getXMLText());
		RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
		try {
			sendRequest.sendRequest(null, lasRequestCallback);
		} catch (RequestException e) {
			plot.setHTML(e.toString());
		}
	}
	private RequestCallback lasRequestCallback = new RequestCallback() {
		public void onError(Request request, Throwable exception) {
			spin.hide();
			plot.setHTML(exception.toString());
		}

		public void onResponseReceived(Request request, Response response) {
			spin.hide();
			String doc = response.getText();
			doc = doc.replaceAll("\n", "");
			StringBuilder html = new StringBuilder();
			Document responseXML = XMLParser.parse(doc);
			NodeList results = responseXML.getElementsByTagName("result");
			for(int n=0; n<results.getLength();n++) {
				if ( results.item(n) instanceof Element ) {
					Element result = (Element) results.item(n);
					if ( result.getAttribute("type").equals("image") ) {
					    html.append("<a target=\"_blank\" href=\""+result.getAttribute("url")+"\"><img height=\"100%\" width=\"100%\" src=\""+result.getAttribute("url")+"\" alt=\"Plot Image\"/></a>");
					} else if ( result.getAttribute("type").equals("error") ) {
						if ( result.getAttribute("ID").equals("las_message") ) {
							Node text = result.getFirstChild();
							if ( text instanceof Text ) {
								Text t = (Text) text;
								html.append(t.getData());
							}
						}
					}
				}
			}
			plot.setHTML(html.toString());
		}
	};
	ChangeListener comparisonAxisChangeListener = new ChangeListener() {
		public void onChange(Widget sender) {
			refreshPlot(fixedAxis, fixedAxisValue, view1lo, view1hi, view2lo, view2hi, true);	
		}
		
	};
	
	TreeListener datasetTreeListener = new TreeListener() {

		public void onTreeItemSelected(TreeItem item) {
			Object v = item.getUserObject();
			if ( v instanceof VariableSerializable ) {
				VariableSerializable var = (VariableSerializable) v;
				dsid = var.getDSID();
				varid = var.getID();
				//refreshPlot();
			}
		}

		public void onTreeItemStateChanged(TreeItem item) {
			// TODO Auto-generated method stub
			
		}
		
	};
	public void setCompareAxis(String axis) {
		compareAxis = fixedAxis;
	}
}
