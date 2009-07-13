package gov.noaa.pmel.tmap.las.client.map;

import gov.noaa.pmel.tmap.las.client.DateWidgetTest;
import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.laswidget.DatasetButton;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsMenu;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.OptionsButton;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SettingsWidget extends Composite {
	
	String layout;
	/*
	 * Objects common to any layout.
	 */
	protected OperationsWidget operations;
	protected Button closeButton;
	protected Button applyButton;
	protected HorizontalPanel closeAndApply;
	protected HorizontalPanel datasetAndOptions;
	protected ReferenceMap refMap;
	protected DatasetButton datasetButton;
	protected OptionsButton optionsButton;
	protected String operationID;
	boolean usePanel = false;
	protected RPCServiceAsync rpcService;
	
	/*
	 * Objects specific to the button layout
	 */
	
	protected Button settingsButton;
	protected DialogBox settingsDialog;
	protected VerticalPanel mainPanel;
	protected HorizontalPanel interiorPanel;
	protected VerticalPanel leftInteriorPanel;
	protected VerticalPanel rightInteriorPanel;
	
	/*
	 * Objects specific to the vertical panel layout
	 */
	Grid vertical = new Grid(5,1);
	
	public SettingsWidget(String title, LatLng center, int zoom, int xpix, int ypix, String panelID, String operationID, RPCServiceAsync rpcService, String layout) {
		this.operationID = operationID;
		this.rpcService = rpcService;
		this.layout = layout;
		applyButton = new Button("Apply");
		applyButton.setTitle("Apply changes to "+panelID);
		applyButton.addStyleName("blackBorder");
		applyButton.addClickListener(applyClick);
		closeButton = new Button("Close");
		closeButton.setTitle("Close settings panel for "+panelID);
		closeButton.addClickListener(closeClick);
		closeAndApply = new HorizontalPanel();
		closeAndApply.add(closeButton);
		closeAndApply.add(applyButton);
        datasetButton = new DatasetButton(rpcService);
		optionsButton = new OptionsButton(rpcService, operationID);
		datasetAndOptions = new HorizontalPanel();
		datasetAndOptions.add(datasetButton);
		datasetAndOptions.add(optionsButton);
		refMap = new ReferenceMap(center, zoom, xpix, ypix);
		operations = new OperationsWidget();
		operations.addClickListener(operationsClickListener);
		if ( layout.equals("button") ) {
			settingsButton = new Button (title);
			settingsButton.addClickListener(settingsButtonClick);
			interiorPanel = new HorizontalPanel();
	        mainPanel = new VerticalPanel();
	       
			settingsDialog = new DialogBox(false, false);
			settingsDialog.setText("Set Region and Plot Options for "+panelID+" ... [Drag Me]");
			leftInteriorPanel = new VerticalPanel();
	        rightInteriorPanel = new VerticalPanel();
		    leftInteriorPanel.add(datasetAndOptions);
			leftInteriorPanel.add(operations);
			interiorPanel.add(leftInteriorPanel);
			rightInteriorPanel.add(refMap);
			interiorPanel.add(rightInteriorPanel);
			mainPanel.add(closeAndApply);
			mainPanel.add(interiorPanel);
			settingsDialog.add(mainPanel);
			initWidget(settingsButton);	
		} else {
			vertical.setWidget(0, 0, new Label(title));
			vertical.setWidget(1, 0, applyButton);
		    vertical.setWidget(2, 0, datasetAndOptions);
		    vertical.setWidget(3, 0, refMap);
		    vertical.setWidget(4, 0, operations);
		    vertical.setWidth("256px");
		    initWidget(vertical);
		}

	}

	public void addClickListener(ClickListener listener) {
		closeButton.addClickListener(listener);
	}

	
	protected ClickListener applyClick = new ClickListener() {
			public void onClick(Widget sender) {
				usePanel = true;
			}	
		};
	public ClickListener operationsClickListener = new ClickListener() {
			public void onClick(Widget sender) {
				refMap.setToolType(getCurrentOperationView());		
				optionsButton.setOptions(getCurrentOp().getOptionsID());
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

	public void setOperations(RPCServiceAsync rpcService, String intervals, String dsID,
			String varID, String opID, String view, OperationsMenu menu) {
				operations.setOperations(rpcService, intervals, dsID, varID, opID, view, menu);
			}

	public void setLatLon(String xlo, String xhi, String ylo, String yhi) {
		refMap.setLatLon(xlo, xhi, ylo, yhi);
	}

	public void addDatasetTreeListener(TreeListener datasetTreeListener) {
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

	public OperationsWidget getOperationsWidget() {
		return operations;
	}

	public void addOperationClickListener(ClickListener operationsClickListener) {
		operations.addClickListener(operationsClickListener);
	}

	public void setOperation(String id, String view) {
		operations.setOperation(id, view);		
	}
	protected ClickListener closeClick = new ClickListener() {
		public void onClick(Widget sender) {
			if ( layout.equals("button") ) {
			   settingsDialog.hide();
			} else {
				// Do something else with the panel layout
			}
		}
	};
	protected ClickListener settingsButtonClick = new ClickListener() {
		public void onClick(Widget sender) {
			settingsDialog.setPopupPosition(settingsButton.getAbsoluteLeft()-350, settingsButton.getAbsoluteTop());
			settingsDialog.show();			
		}		
	};

	public String getHistoryToken() {
		StringBuilder token = new StringBuilder();
		token.append(";xlo="+getRefMap().getXlo());
		token.append(";xhi="+getRefMap().getXhi());
		token.append(";ylo="+getRefMap().getYlo());
		token.append(";yhi="+getRefMap().getYhi());
		token.append(";operation_id="+getCurrentOp().getID());
		token.append(";view="+getCurrentOperationView());
		Map<String, String> options = getOptions();
		for (Iterator opIt = options.keySet().iterator(); opIt.hasNext();) {
			String name = (String) opIt.next();
			String value = options.get(name);
			token.append(";ferret_"+name+"="+value);
		}		
		return token.toString();
	}
	public void setFromHistoryToken(Map<String, String> tokenMap, Map<String, String> optionsMap) {
		setOperation(tokenMap.get("operation_id"), tokenMap.get("view"));
		setLatLon(tokenMap.get("xlo"), tokenMap.get("xhi"), tokenMap.get("ylo"), tokenMap.get("yhi"));
		if ( optionsMap.size() > 1 ) {
			optionsButton.setOptions(tokenMap.get("operations_id"), optionsMap);
		}
	}
}