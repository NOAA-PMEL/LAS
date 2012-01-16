package gov.noaa.pmel.tmap.las.client;

import java.util.HashSet;
import java.util.Set;

import gov.noaa.pmel.tmap.las.client.laswidget.DatasetFilter;
import gov.noaa.pmel.tmap.las.client.laswidget.DatasetWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequest;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TreeItem;

public class ClimateAnalysis implements EntryPoint {
    DatasetWidget xDatasetWidget = new DatasetWidget();
    Label xCurrentDataset = new Label("none");
    Label xSecondDataset = new Label("...");
    ListBox xAnalysisType = new ListBox();
    PushButton xSubmit = new PushButton("Submit");
    DateTimeWidget xDateWidget = new DateTimeWidget();
    LASRequest lasRequest = new LASRequest();
    DatasetFilter cimp5filter = new DatasetFilter(true, "name", "cmip5");
	String xFirstName = "none";
	String xFirstID = "id";
	String xFirstVarID = "id";
	String xSecondName = "none";
	String xInitialId = "x-Unintialized-id";
	String xSecondID = xInitialId;
	String xSecondVarID = xInitialId;
	String xTlo;
	String xThi;
	String[] panelIDs = {"DatasetSelection", "AnalysisSelection", "DateSelection", "SubmitSelection"};
	String[] selectionIDs = {"DatasetWidget", "AnalysisType", "DateRange", "Submit"};
	String highLightStyle = "highlightSelection";
	String selectionStyle = "selection";
	Set<Integer> set = new HashSet<Integer>();
	@Override
	public void onModuleLoad() {	
		set.clear();
		xSubmit.addClickHandler(xSubmitClick);
		xSubmit.setWidth("80px");
		RootPanel.get("Submit").add(xSubmit);
		xAnalysisType.setVisibleItemCount(1);
		xAnalysisType.addItem("Temperature Average Spectrum", "tave_spectrum");
		xAnalysisType.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent arg0) {
				set.add(1);
				setBackground(2);
			}
			
		});
		RootPanel.get("AnalysisType").add(xAnalysisType);
		xDatasetWidget.addFilter(cimp5filter);
		String xml = Util.getParameterString("xml");
	    xFirstName = Util.getParameterString("dsname");
		if ( xFirstName != null && !xFirstName.equals("") ) {
			xCurrentDataset.setText(xFirstName);
		}
		xTlo = Util.getParameterString("tlo");
		xThi = Util.getParameterString("thi");
		xDateWidget.init(xTlo, xThi, "y", false);
		xDateWidget.setRange(true);
		xDateWidget.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent arg0) {
				set.add(2);
				setBackground(3);
			}
			
		});
		RootPanel.get("DateRange").add(xDateWidget);
		RootPanel.get("SecondDataset").add(xSecondDataset);
		RootPanel.get("CurrentDataset").add(xCurrentDataset);
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
			xFirstID = lasRequest.getDataset(0);
			xFirstVarID = lasRequest.getVariable(0);
			DatasetFilter filter = new DatasetFilter(false, "ID", xFirstID);
			xDatasetWidget.addFilter(filter);
		}
		xDatasetWidget.init();
		xDatasetWidget.addSelectionHandler(xSelectionHandler);
		RootPanel.get("DatasetWidget").add(xDatasetWidget);
	}
	private ClickHandler xSubmitClick = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			if ( xSecondID.equals(xInitialId) ) {
				Window.alert("Please select a second data set.");
			} else if ( xSecondVarID.equals(xInitialId) ) {
				Window.alert("Second variable not set, try again in a moment.");
			} else {
				set.clear();
				lasRequest = new LASRequest();
				lasRequest.setOperation("Climate_Analysis_Plot", "V7");
				lasRequest.addVariable(xFirstID, xFirstVarID, 0);
				lasRequest.addVariable(xSecondID, xSecondVarID, 0);
				lasRequest.setRange("t", xDateWidget.getFerretDateLo(), xDateWidget.getFerretDateHi(), 0);
				String type = xAnalysisType.getValue(xAnalysisType.getSelectedIndex());
				lasRequest.setProperty("climate_analysis", "type", type);
				Window.open(Util.getProductServer()+"?xml="+URL.encode(lasRequest.toString()), "_blank", "");
			}
		}
		
	};
	private SelectionHandler<TreeItem> xSelectionHandler = new SelectionHandler<TreeItem>() {

		@Override
		public void onSelection(SelectionEvent event) {
			DatasetWidget source = (DatasetWidget) event.getSource();
			Object uo = source.getCurrentlySelected();
			if ( uo instanceof VariableSerializable ) {
				VariableSerializable v = (VariableSerializable) uo;
				xSecondName = v.getDSName();
				xSecondID = v.getDSID();
				xSecondVarID = v.getID();
				xSecondDataset.setText(xSecondName);
				set.add(0);
				setBackground(1);

			} else if ( uo instanceof CategorySerializable) {
				CategorySerializable c = (CategorySerializable) uo;
				// Category children, open it up.
				
				// Variable children, dagnabit.  We need the variable, so let's go get it.
				Util.getRPCService().getCategories(c.getID(), categoryCallback);
			}
		}
		
	};
	AsyncCallback categoryCallback = new AsyncCallback() {

		@Override
		public void onFailure(Throwable e) {
			Window.alert("Could not get a variable for the second data set. "+e.getLocalizedMessage());
		}

		@Override
		public void onSuccess(Object result) {
			CategorySerializable[] cats = (CategorySerializable[]) result;
			CategorySerializable cat = cats[0];
			if ( cat.isCategoryChildren() ) {
				Window.alert("Could not get a variable for the second data set.");
			} else {
				DatasetSerializable ds = cat.getDatasetSerializable();
				VariableSerializable[] vars = ds.getVariablesSerializable();	
				xSecondName = vars[0].getDSName();
				xSecondID = vars[0].getDSID();
				xSecondVarID = vars[0].getID();
				xSecondDataset.setText(xSecondName);
				set.add(0);
				setBackground(1);
			}
		}
		
	};
	private void setBackground(int id) {
		for (int i = 0; i < panelIDs.length; i++) {
			RootPanel.get(panelIDs[i]).removeStyleName(highLightStyle);
			RootPanel.get(selectionIDs[i]).removeStyleName(selectionStyle);
		}
		if ( set.contains(0) && set.contains(1) && set.contains(2) ) {
			RootPanel.get(panelIDs[3]).addStyleName(highLightStyle);
			RootPanel.get(selectionIDs[3]).addStyleName(selectionStyle);
		} else {
			if ( xSecondID.equals(xInitialId) ) {
				RootPanel.get("DatasetSelection").addStyleName(highLightStyle);
				RootPanel.get("DatasetWidget").addStyleName(selectionStyle);
			} else{ 
				if ( !set.contains(id) ) {
					RootPanel.get(panelIDs[id]).addStyleName(highLightStyle);
					RootPanel.get(selectionIDs[id]).addStyleName(selectionStyle);
				}
			}
		}
	}
}
