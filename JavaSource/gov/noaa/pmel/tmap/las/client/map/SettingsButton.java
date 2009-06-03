package gov.noaa.pmel.tmap.las.client.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DatasetButton;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsMenu;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.OptionsButton;
import gov.noaa.pmel.tmap.las.client.serializable.AxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SettingsButton extends Composite {
	Button settingsButton;
	DialogBox settingsDialog;
	VerticalPanel mainPanel;
	HorizontalPanel interiorPanel;
	VerticalPanel leftInteriorPanel;
	VerticalPanel rightInteriorPanel;
	HorizontalPanel buttonFlow;
	HorizontalPanel datasetAndOptions;
	OperationsWidget operations;
	Button closeButton;
	Button applyButton;
	ReferenceMap refMap;
	DatasetButton datasetButton;
	OptionsButton optionsButton;
	String operationID;
	boolean usePanel = false;
	RPCServiceAsync rpcService;
	public SettingsButton (String title, LatLng center, int zoom, int xpix, int ypix, String panelID, String operationID, RPCServiceAsync rpcService) {
		this.operationID = operationID;
		this.rpcService = rpcService;
		settingsButton = new Button (title);
		settingsButton.addClickListener(settingsButtonClick);

		datasetButton = new DatasetButton(rpcService);
		
		optionsButton = new OptionsButton(rpcService, operationID);
		
		interiorPanel = new HorizontalPanel();
        mainPanel = new VerticalPanel();
       
		settingsDialog = new DialogBox(false, false);
		settingsDialog.setText("Set Region and Plot Options for "+panelID+" ... [Drag Me]");
		leftInteriorPanel = new VerticalPanel();
        rightInteriorPanel = new VerticalPanel();
		closeButton = new Button("Close");
		closeButton.setTitle("Close settings panel for "+panelID);
		closeButton.addClickListener(closeClick);
		
		applyButton = new Button("Apply");
		applyButton.setTitle("Apply changes to "+panelID);
		applyButton.addStyleName("blackBorder");
		applyButton.addClickListener(applyClick);

		buttonFlow = new HorizontalPanel();
		buttonFlow.add(closeButton);
		buttonFlow.add(applyButton);
		
		datasetAndOptions = new HorizontalPanel();
		datasetAndOptions.add(datasetButton);
		datasetAndOptions.add(optionsButton);
		
	    leftInteriorPanel.add(datasetAndOptions);
		operations = new OperationsWidget();
		operations.addClickListener(operationsClickListener);
		leftInteriorPanel.add(operations);
		
		interiorPanel.add(leftInteriorPanel);
		
		refMap = new ReferenceMap(center, zoom, xpix, ypix);
		rightInteriorPanel.add(refMap);
		
		interiorPanel.add(rightInteriorPanel);
		mainPanel.add(buttonFlow);
		mainPanel.add(interiorPanel);
		settingsDialog.add(mainPanel);
		initWidget(settingsButton);	
	}
	public void addClickListener(ClickListener listener) {
		closeButton.addClickListener(listener);
	}
	ClickListener settingsButtonClick = new ClickListener() {
		public void onClick(Widget sender) {
			settingsDialog.setPopupPosition(settingsButton.getAbsoluteLeft()-350, settingsButton.getAbsoluteTop());
			settingsDialog.show();			
		}		
	};
	ClickListener closeClick = new ClickListener() {
		public void onClick(Widget sender) {
			settingsDialog.hide();
		}
	};
	ClickListener applyClick  = new ClickListener() {
		public void onClick(Widget sender) {
			usePanel = true;
		}	
	};
	public void addCloseClickListener(ClickListener close) {
		closeButton.addClickListener(close);
	}
	public void addApplyClickListener(ClickListener apply) {
		applyButton.addClickListener(apply);
	}
	public ReferenceMap getRefMap() {
		return refMap;
	}
	public void setOperations(RPCServiceAsync rpcService, String view, String dsID, String varID, OperationsMenu menu) {
		operations.setOperations(rpcService, view, dsID, varID, menu);
	}
	public void setLatLon(String xlo, String xhi, String ylo, String yhi) {
		refMap.setLatLon(xlo, xhi, ylo, yhi);
	}
	public void addDatasetTreeListener (TreeListener datasetTreeListener) {
	    datasetButton.addTreeListener(datasetTreeListener);
	}
	public boolean isUsePanelSettings() {
		return usePanel;
	}
	public void setUsePanel(boolean b) {
		usePanel = b;
	}
	public void addOptionsOkClickListener(ClickListener listener) {
		optionsButton.addOkClickListener(listener);
	}
	public Map<String, String> getOptions() {
		return optionsButton.getState();
	}
	public OperationSerializable getCurrentOp() {
		return operations.getCurrentOp();
	}
	public String getCurrentOperationView() {
		return operations.getCurrentView();
	}
	public void setToolType(String view) {
		refMap.setToolType(view);
	}
	public ClickListener operationsClickListener = new ClickListener() {
		public void onClick(Widget sender) {
			refMap.setToolType(getCurrentOperationView());		
			optionsButton.setOptions(getCurrentOp().getOptionsID());
		}
	};
	public OperationsWidget getOperationsWidget() {
		return operations;
	}
	public void addOperationClickListener(ClickListener operationsClickListener) {
		operations.addClickListener(operationsClickListener);
	}
	public void setOperation(String id, String view) {
		operations.setOperation(id, view);		
	}
}
