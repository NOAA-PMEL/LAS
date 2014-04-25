package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.CheckBoxPanel;
import gov.noaa.pmel.tmap.las.client.laswidget.Constants;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASDateWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequestWrapper;
import gov.noaa.pmel.tmap.las.client.laswidget.OptionsWidget;
import gov.noaa.pmel.tmap.las.client.serializable.ArangeSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.AxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.TimeAxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The OpenLayers based Time Series UI.
 */
public class TimeSeries implements EntryPoint {

	ListBox timeSeriesList = new ListBox();
	OLTimeSeriesMap timeSeriesMap = new OLTimeSeriesMap();
	CheckBoxPanel variables;
	DateTimeWidget dates = new DateTimeWidget();
	HTML output = new HTML();
	ListBox z = new ListBox();
	PopupPanel options_panel = new PopupPanel(true);
	OptionsWidget options_widget;
	HashMap<String, CategorySerializable> categories = new HashMap<String, CategorySerializable>();
	CategorySerializable cat;
	LASRequestWrapper lasRequest =  new LASRequestWrapper();
	Map<String, String> options_state;
	Label z_label = new Label("Select a z-value:");
	Label dates_label = new Label("Select a date range:");
	public static final String PLOT_BUTTON_NAME = "Plot";
	public static final String PLOT_OPTIONS_BUTTON_NAME = "Plot Options";

	public void onModuleLoad() {
		// Set up the RPC service for getting LAS metadata
		timeSeriesList.addChangeListener( new ChangeListener() {
			public void onChange(Widget sender) {
				int index = timeSeriesList.getSelectedIndex();
				String id = timeSeriesList.getValue(index);
				cat = categories.get(id);
				timeSeriesMap.update(categories.get(id));
				variables.setVisible(false);
				dates.setVisible(false);
				dates_label.setVisible(false);
				output.setVisible(false);
				z_label.setVisible(false);
				z.setVisible(false);
			}
		});
		variables = new CheckBoxPanel(clickListener);
		Util.getRPCService().getTimeSeries(timeSeriesCallback);
		lasRequest.removeVariables();
		//timeSeriesMap.addMapClickHandler(mapClick);
		RootPanel.get("timeseries_collection_listbox").add(timeSeriesList);
		variables.setVisible(false);
		RootPanel.get("map").add(timeSeriesMap);
		RootPanel.get("variables").add(variables);
		z.setVisible(false);
		RootPanel.get("z").add(z);
		RootPanel.get("z_label").add(z_label);
		z_label.setVisible(false);
		z_label.setStyleName("small-banner");
		RootPanel.get("dates").add(dates);
		RootPanel.get("dates_label").add(dates_label);
		dates.setVisible(false);
		dates_label.setVisible(false);
		dates_label.setStyleName("small-banner");
		RootPanel.get("output").add(output);
		options_widget = new OptionsWidget("Options_1D_7", clickListener, clickListener);
		options_panel.add(options_widget);
		//this.setElement(RootPanel.get().getElement());
	}
	
	AsyncCallback timeSeriesCallback = new AsyncCallback() {

		public void onFailure(Throwable caught) {
			HTML out = new HTML();
			PopupPanel p = new PopupPanel();
			p.add(out);
			out.setHTML("<H1>Error getting time series data sets.  Message:"+caught.getMessage());
			p.show();
		}

		public void onSuccess(Object result) {
			if ( result == null ) {
				Window.alert("No time series data sets found.");
				return;
			}
			CategorySerializable[] cats = (CategorySerializable[]) result;
			if ( cats.length == 0 ) {
				Window.alert("No time series data sets found.");
				return;
			}
			Arrays.sort(cats);
			for (int i = 0; i < cats.length; i++) {
				CategorySerializable cat = cats[i];
				timeSeriesList.addItem(cat.getName(), cat.getID());
				categories.put(cat.getID(), cat);
			}
			cat = cats[0];
			timeSeriesMap.update(cat);
		}

	};

	public MapClickHandler mapClick = new MapClickHandler() {

		public void onClick(MapClickEvent event) {
			Overlay sender = event.getOverlay();
			if ( sender != null && sender instanceof Marker ) {
				Marker marker = (Marker) sender;
				String gridID = timeSeriesMap.getCurrentGridID();
				showVariables(gridID, marker);
				dates.setVisible(false);
				dates_label.setVisible(false);
				variables.hideButtons();
				z.setVisible(false);
				z_label.setVisible(false);
				variables.setFirst(true);
				output.setVisible(false);
			}
		}

	};
	public void showDateWidgets(String varID) {
		if (variables.isFirst() ) {
			TimeAxisSerializable tAxis = cat.getVariable(varID).getGrid().getTAxis();
			dates.init(tAxis, false);
			dates.setVisible(true);
			AxisSerializable zAxis = cat.getVariable(varID).getGrid().getZAxis();
			if ( zAxis != null ) {
				z.clear();
				if (zAxis.getNames() != null) {
					String[] names = zAxis.getNames();
					String[] values = zAxis.getValues();
					for (int i=0; i<names.length; i++) {
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
	public void showVariables(String gridID, Marker sender) {
		HashMap<String, ArrayList<VariableSerializable>> allVars = new HashMap<String, ArrayList<VariableSerializable>>();
		if ( cat.hasMultipleDatasets() ) {
			DatasetSerializable[] ds = cat.getDatasetSerializableArray();
			ArrayList<VariableSerializable> varsWithGrid = new ArrayList<VariableSerializable>();
			for (int i = 0; i < ds.length; i++) {
				boolean match = false;
				DatasetSerializable d = ds[i];
				VariableSerializable[] vars = d.getVariablesSerializable();
				for (int j = 0; j < vars.length; j++) {
					VariableSerializable var = vars[j];
					GridSerializable grid = var.getGrid();
					if ( var.getGrid().getID().equals(gridID)) {
						varsWithGrid.add(var);
						match = true;
					}
				}
				if (match) {
					allVars.put(d.getName(), varsWithGrid);
					match = false;
				}
			}
		} else {
			DatasetSerializable ds = cat.getDatasetSerializable();
			VariableSerializable[] vars = ds.getVariablesSerializable();
			ArrayList<VariableSerializable> varsWithGrid = new ArrayList<VariableSerializable>();
			for (int j = 0; j < vars.length; j++) {
				VariableSerializable var = vars[j];
				GridSerializable grid = var.getGrid();
				if ( var.getGrid().getID().equals(gridID)) {
					varsWithGrid.add(var);
				}
			}
			allVars.put(ds.getName(), varsWithGrid);
		}
		variables.update(sender.getPoint(), allVars);
		variables.setVisible(true);
	}
	public ClickListener clickListener = new ClickListener() {
		public void onClick(Widget widget) {
			if ( widget instanceof CheckBox ) {
				showDateWidgets(widget.getTitle());
			} else if ( widget instanceof Button ) {
				Button button = (Button) widget;
				String button_name = button.getText();
				if ( button_name.equals(PLOT_BUTTON_NAME)) {
					//					output.setHTML("<img src=\"../JavaScript/components/mozilla_blu.gif\" alt=\"Spinner\"/>");
					//					output.setVisible(true);
					lasRequest.removeRegion(0);
					lasRequest.removeVariables();
					lasRequest.removePropertyGroup("ferret");
					ArrayList<String> selectedVariables = variables.getSelected();

					if ( selectedVariables.size() == 0 ) {
						Window.alert("Please select a variable.");
						return;
					}
					for (Iterator varIt = selectedVariables.iterator(); varIt.hasNext();) {
						String varID = (String) varIt.next();
						VariableSerializable variable = cat.getVariable(varID);
						lasRequest.addVariable(variable.getDSID(), variable.getID());
					}

					lasRequest.setOperation("Plot_1D", "v7");
					lasRequest.setProperty("ferret", "view", "t");

					if ( z.isVisible() ) {
						String zval = z.getItemText(z.getSelectedIndex());
						lasRequest.setRange("z", zval, zval, 0);
					}

					lasRequest.setRange("t", dates.getFerretDateLo(), dates.getFerretDateHi(), 0);
					GridSerializable grid = cat.getVariable(selectedVariables.get(0)).getGrid();
					AxisSerializable xAxis = grid.getXAxis();
					AxisSerializable yAxis = grid.getYAxis();
					lasRequest.setRange("x", xAxis.getLo(), xAxis.getHi(), 0);
					lasRequest.setRange("y", yAxis.getLo(), yAxis.getHi(), 0);

					if ( options_state != null && options_state.size() > 0 ) {
						lasRequest.removePropertyGroup("ferret");

						for (Iterator opIt = options_state.keySet().iterator(); opIt.hasNext();) {
							String name = (String) opIt.next();
							String value = options_state.get(name);
							if ( !value.equalsIgnoreCase("default") ) {
								lasRequest.addProperty("ferret", name, value);
							} else {
								lasRequest.removeProperty("ferret", name);
							}
						}					
					}
					lasRequest.addProperty("ferret", "view", "t");
					lasRequest.addProperty("ferret", "image_format", "gif");

					String url = Util.getProductServer()+"?xml="+URL.encode(lasRequest.getXMLText());
					/*
					RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
					try {
						sendRequest.sendRequest(null, lasRequestCallback);
					} catch (RequestException e) {
						output.setHTML(e.toString());
					}
					 */
					Window.open(url,"_blank",Constants.WINDOW_FEATURES); 
				} else if ( button_name.equals(PLOT_OPTIONS_BUTTON_NAME) ) {
					options_state = options_widget.getState();
					options_panel.center();
				} else if ( button_name.equals("OK") ) {
					options_state = options_widget.getState();
					options_panel.hide();
				} else if ( button_name.equals("Cancel") ) {
					options_widget.restore(options_state);
					options_panel.hide();
				}
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

}
