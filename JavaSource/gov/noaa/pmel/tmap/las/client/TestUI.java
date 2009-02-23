package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.DatasetWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASDateWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequestWrapper;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsWidget;
import gov.noaa.pmel.tmap.las.client.map.ReferenceMap;
import gov.noaa.pmel.tmap.las.client.serializable.ArangeSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.AxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.TimeAxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

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
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;


public class TestUI extends LASEntryPoint {
	HTML output;
	PopupPanel mDatasetPanel;
	DatasetWidget dsWidget;
	ReferenceMap refMap;
	Label datasetLabel;
	OperationsWidget operationsWidget;
	OperationsMenu operationsMenu;
	LASRequestWrapper lasRequest = new LASRequestWrapper();
	VariableSerializable selectedVariable;
	LASDateWidget dates;
	ListBox z;
	Label z_label;
	Grid datasetAndPlotButtons;
	Grid popupGrid;
	Button plot;
	Grid zAndZlabel;
	Button close;
	public void onModuleLoad() {
		super.onModuleLoad();
		lasRequest.removeVariables();
		datasetLabel = new Label("Select a data set...");
		output = new HTML("<h3>Select a variable and push the \"Update plot\" button.  :-)</h3>");
		dates = dates.init("1990-01-01", "2000-01-01", 0, 0, 0, 0);
		dates.render("dates", "YMDT", "YMDT");
		dates.hide();
		z_label = new Label("Z");
		z_label.setVisible(false);
		z = new ListBox();
		z.setVisible(false);
		zAndZlabel = new Grid(2, 1);
		zAndZlabel.setWidget(0, 0, z_label);
		zAndZlabel.setWidget(1, 0, z);
		dsWidget = new DatasetWidget();
		dsWidget.addTreeListener(new TreeListener() {
			public void onTreeItemSelected(TreeItem item) {
				Object u = item.getUserObject();
				if ( u instanceof VariableSerializable ) {
					selectedVariable = (VariableSerializable) u;
					GridSerializable grid = selectedVariable.getGrid();
					double grid_west = Double.valueOf(grid.getXAxis().getLo());
					double grid_east = Double.valueOf(grid.getXAxis().getHi());
					
					double grid_south = Double.valueOf(grid.getYAxis().getLo());
					double grid_north = Double.valueOf(grid.getYAxis().getHi());
					
					double delta = Math.abs(Double.valueOf(grid.getXAxis().getArangeSerializable().getStep()));
					
					LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(grid_south, grid_west), LatLng.newInstance(grid_north, grid_east));
					
					refMap.setDataBounds(bounds, delta, true);
					refMap.getResetWidget().setDataBounds(bounds);
					refMap.getResetWidget().setSelectionBounds(bounds);
					refMap.getRegionWidget().setSelectedIndex(0);
					mDatasetPanel.hide();
					operationsWidget.setOperations(rpcService, null, selectedVariable.getDSID(), selectedVariable.getID(), operationsMenu);
					showDateWidgets();
					datasetLabel.setText(selectedVariable.getDSName()+": "+selectedVariable.getName());
				}		
			}
			public void onTreeItemStateChanged(TreeItem item) {
				
				
			}
			
		});
		
		operationsWidget = new OperationsWidget();
		operationsMenu = new OperationsMenu(datasetCommand, plotCommand);
		mDatasetPanel = new PopupPanel(false);
		
		popupGrid = new Grid(2, 1);		
		
		close = new Button("close");
		close.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				mDatasetPanel.hide();			
			}
		});
		refMap = new ReferenceMap(LatLng.newInstance(0.0, 0.0), 1, 256, 360);
		
        dsWidget.init(rpcService);
        RootPanel.get("refmap").add(refMap);
        popupGrid.setWidget(0, 0, close);
        popupGrid.setWidget(1, 0, dsWidget);
        mDatasetPanel.add(popupGrid);
        RootPanel.get("operations").add(operationsWidget);
        RootPanel.get("operations_menu").add(operationsMenu);
        RootPanel.get("dataset_info").add(datasetLabel);
        RootPanel.get("plot").add(output);
        RootPanel.get("z").add(zAndZlabel);
	}
	Command datasetCommand = new Command() {

		public void execute() {
			
			mDatasetPanel.setPopupPosition(operationsMenu.getAbsoluteLeft(), operationsMenu.getAbsoluteTop());
			mDatasetPanel.show();	
		}
		
	};
	Command plotCommand = new Command() {

		public void execute() {
			output.setHTML("<img src=\"../JavaScript/components/mozilla_blu.gif\" alt=\"Spinner\"/>");
			lasRequest.removeRegion(0);
			lasRequest.removeVariables();
			lasRequest.removePropertyGroup("ferret");
			lasRequest.addVariable(selectedVariable.getDSID(), selectedVariable.getID());
			OperationSerializable op = operationsWidget.getCurrentOp();
			String view = operationsWidget.getCurrentView();
			lasRequest.setOperation(op.getID(), "v7");
			lasRequest.setProperty("ferret", "view", view);
			
			if ( selectedVariable.getGrid().getTAxis() != null ) {
				if ( view.contains("t") ) {
					lasRequest.setRange("t", dates.getDate1_Ferret(), dates.getDate2_Ferret(), 0);
				} else {
					lasRequest.setRange("t", dates.getDate1_Ferret(), dates.getDate1_Ferret(), 0);
				}			    
			}
			
			if ( view.contains("x") ) {
			   lasRequest.setRange("x", refMap.getXlo(), refMap.getXhi(), 0);
			}
			if ( view.contains("y") ) {
			    lasRequest.setRange("y", refMap.getYlo(), refMap.getYhi(), 0);
			}
			if ( z.isVisible() ) {
				lasRequest.setRange("z", z.getValue(z.getSelectedIndex()), z.getValue(z.getSelectedIndex()), 0);
			}
			lasRequest.addProperty("ferret", "image_format", "gif");
			String url = productServer+"?xml="+URL.encode(lasRequest.getXMLText());
			RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
			try {
				sendRequest.sendRequest(null, lasRequestCallback);
			} catch (RequestException e) {
				output.setHTML(e.toString());
			}
		}
		
	};
	private RequestCallback lasRequestCallback = new RequestCallback() {
		public void onError(Request request, Throwable exception) {
			output.setHTML(exception.toString());
		}

		public void onResponseReceived(Request request, Response response) {
			String doc = response.getText();
			output.setHTML(doc);
		}
	};
	public void showDateWidgets() {

		TimeAxisSerializable tAxis = selectedVariable.getGrid().getTAxis();
		if ( tAxis != null ) {
			String hi = tAxis.getHi();
			String lo = tAxis.getLo();
			int interval = (int) tAxis.getMinuteInterval();
			dates = dates.init(lo, hi, interval, 0, 0, 0);
			dates.render("dates", "YMDT", "YMDT");
			dates.show();
		} else {
			dates.hide();
		}

		AxisSerializable zAxis = selectedVariable.getGrid().getZAxis();
		if ( zAxis != null ) {
			z.clear();
			if (zAxis.getNames() != null) {
				String names[] = zAxis.getNames();
				String values[] = zAxis.getValues();
				for (int i=0; i<names.length;i++) {
					z.addItem(names[i], values[i]);
				}
			} else {
				ArangeSerializable a = zAxis.getArangeSerializable();
				double start = Double.valueOf(a.getStart()).doubleValue();
				int size = Integer.valueOf(a.getSize()).intValue();
				double step = Double.valueOf(a.getStep()).doubleValue();
				for ( int i = 0; i < size; i++ ) {
					double v = start + i*step;
					z.addItem(String.valueOf(i), String.valueOf(v));
				}
			}
			z.setVisible(true);
			if ( zAxis.getAttributes().get("label") != null && !zAxis.getAttributes().get("label").equals("")) {
				z_label.setText(zAxis.getAttributes().get("label")+" ("+zAxis.getAttributes().get("units")+"):");
			} else {
				z_label.setText("Depth or height ("+zAxis.getAttributes().get("units")+"):");
			}
			z_label.setVisible(true);
		}
	}
}
