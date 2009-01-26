package gov.noaa.pmel.tmap.las.client;

import java.util.Iterator;
import java.util.Map;

import gov.noaa.pmel.tmap.las.client.map.ReferenceMap;
import gov.noaa.pmel.tmap.las.client.map.RegionWidget;
import gov.noaa.pmel.tmap.las.client.map.SelectControl;

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
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;


public class TestUI extends LASEntryPoint {
	HTML output;
	PopupPanel mDatasetPanel;
	DatasetWidget dsWidget;
	ReferenceMap refMap;
	RegionWidget regions;
	OperationsWidget operationsWidget;
	OperationsMenu operationsMenu;
	LASRequestWrapper lasRequest = new LASRequestWrapper();
	VariableSerializable selectedVariable;
	LASDateWidget dates;
	ListBox z;
	Label z_label;
	Grid datasetAndPlotButtons;
	Grid popupGrid;
	Button chooseDataset;
	Button plot;
	Grid zAndZlabel;
	Button close;
	public void onModuleLoad() {
		super.onModuleLoad();
		lasRequest.removeVariables();
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
					regions.setSelectedIndex(0);
					mDatasetPanel.hide();
					operationsWidget.setOperations(rpcService, null, selectedVariable.getDSID(), selectedVariable.getID(), operationsMenu);
					showDateWidgets();
				}		
			}
			public void onTreeItemStateChanged(TreeItem item) {
				
				
			}
			
		});
		
		operationsWidget = new OperationsWidget();
		operationsMenu = new OperationsMenu();
		mDatasetPanel = new PopupPanel(false);
		
		popupGrid = new Grid(2, 1);		
		chooseDataset = new Button("Choose a Dataset");
		plot = new Button("Update plot");
		plot.addClickListener(plotListener);
		chooseDataset.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				mDatasetPanel.setPopupPosition(sender.getAbsoluteLeft(), sender.getAbsoluteTop());
				mDatasetPanel.show();			
			}
			
		});
		close = new Button("close");
		close.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				mDatasetPanel.hide();			
			}
		});
		refMap = new ReferenceMap(LatLng.newInstance(0.0, 0.0), 1, 720, 360);
		regions = new RegionWidget(refMap);
        dsWidget.init(rpcService);
        RootPanel.get("refmap").add(refMap);
        popupGrid.setWidget(0, 0, close);
        popupGrid.setWidget(1, 0, dsWidget);
        mDatasetPanel.add(popupGrid);
        datasetAndPlotButtons = new Grid(1,2);
        datasetAndPlotButtons.setWidget(0, 0, chooseDataset);
        datasetAndPlotButtons.setWidget(0, 1, plot);
        RootPanel.get("datasets").add(datasetAndPlotButtons);
        RootPanel.get("regions").add(regions);
        RootPanel.get("operations").add(operationsWidget);
        RootPanel.get("operations_menu").add(operationsMenu);
        RootPanel.get("plot").add(output);
        RootPanel.get("z").add(zAndZlabel);
	}
	ClickListener plotListener = new ClickListener() {

		public void onClick(Widget sender) {
			output.setHTML("<img src=\"../JavaScript/components/mozilla_blu.gif\" alt=\"Spinner\"/>");
			lasRequest.removeRegion(0);
			lasRequest.removeVariables();
			lasRequest.removePropertyGroup("ferret");
			lasRequest.addVariable(selectedVariable.getDSID(), selectedVariable.getID());
			OperationSerializable op = operationsWidget.getCurrentOp();
			lasRequest.setOperation(op.getID(), "v7");
			//TODO How do you get the view from the state?
			lasRequest.setProperty("ferret", "view", "xy");
			if ( selectedVariable.getGrid().getTAxis() != null ) {
			    lasRequest.setRange("t", dates.getDate1_Ferret(), dates.getDate1_Ferret(), 0);
			}
			
			lasRequest.setRange("x", refMap.getXlo(), refMap.getXhi(), 0);
			lasRequest.setRange("y", refMap.getYlo(), refMap.getYhi(), 0);
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

		AxisSerializable tAxis = selectedVariable.getGrid().getTAxis();
		if ( tAxis != null ) {
			String hi = tAxis.getHi();
			String lo = tAxis.getLo();
			dates = dates.init(lo, hi, 0, 0, 0, 0);
			dates.render("dates", "YMDT", "YMDT");
			dates.show();
		} else {
			dates.hide();
		}

		AxisSerializable zAxis = selectedVariable.getGrid().getZAxis();
		if ( zAxis != null ) {
			z.clear();
			if (zAxis.getV() != null) {
				Map<String, String> v = zAxis.getV();
				for (Iterator vIt = v.keySet().iterator(); vIt.hasNext();) {
					String key = (String) vIt.next();
					String value = v.get(key);
					z.addItem(key, value);
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
