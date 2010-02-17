package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;

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
	protected OLMapWidget refMap;
	protected DatasetButton datasetButton;
	protected OptionsButton optionsButton;
	protected String operationID;
	protected String optionID;
	boolean usePanel = false;

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

	public SettingsWidget(String title, String panelID, String operationID, String optionID, String layout) {
		this.operationID = operationID;
		this.optionID = optionID;
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
		datasetButton = new DatasetButton();
		if ( layout.equals("button") ) {
		    optionsButton = new OptionsButton(optionID, 300);
		    datasetButton.setOffset(0);
		} else {
			optionsButton = new OptionsButton(optionID, 0);
			datasetButton.setOffset(260);
		}
		datasetAndOptions = new HorizontalPanel();
		datasetAndOptions.add(datasetButton);
		datasetAndOptions.add(optionsButton);
		refMap = new OLMapWidget();
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
			leftInteriorPanel.add(refMap);
			interiorPanel.add(leftInteriorPanel);
			rightInteriorPanel.add(datasetAndOptions);
			rightInteriorPanel.add(operations);
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
			if ( layout.equals("button") ) {
				settingsDialog.hide();
			} else {
				// Do something else with the panel layout
			}
		}	
	};
	public ClickListener operationsClickListener = new ClickListener() {
		public void onClick(Widget sender) {
			refMap.setTool(getCurrentOperationView());		
			optionsButton.setOptions(getCurrentOp().getOptionsID());
		}
	};

	public void addCloseClickListener(ClickListener close) {
		closeButton.addClickListener(close);
	}

	public void addApplyClickListener(ClickListener apply) {
		applyButton.addClickListener(apply);
	}

	public void setOperations(String intervals, String dsID, String varID, String opID, String view, OperationsMenu menu) {
		operations.setOperations(intervals, dsID, varID, opID, view, menu);
	}

	public void setLatLon(String xlo, String xhi, String ylo, String yhi) {
		refMap.setCurrentSelection(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
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
		refMap.setTool(view);
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
			
			/*
			 * If you don't remove the map, all the features that have
			 * been rendered to it while it was hidden will appear on
			 * the map and will be zombies (you can't clear them
			 * and you can't select them).  Remove the map and reinitializing
			 * it works around this problem.
			 */
			leftInteriorPanel.remove(refMap);
			
			// n, s, e, w...
			double[] data = refMap.getDataExtent();
			
			double xlo = refMap.getXlo();
			double xhi = refMap.getXhi();
			double ylo = refMap.getYlo();
			double yhi = refMap.getYhi();
			
			double delta = refMap.getDelta();
			int zoom = refMap.getZoom();
			double[] center = refMap.getCenterLatLon();
			String tool = refMap.getTool();
			
			settingsDialog.setPopupPosition(settingsButton.getAbsoluteLeft()-350, settingsButton.getAbsoluteTop());
			settingsDialog.show();		
			
			refMap = new OLMapWidget();
			
			refMap.setDataExtentOnly(data[1], data[0], data[3], data[2], delta);
			
			refMap.setToolOnly(tool);
			
			refMap.setCenter(center[0], center[1], zoom);
			
			refMap.setCurrentSelection(ylo, yhi, xlo, xhi);
			
			leftInteriorPanel.add(refMap);
			
		}		
	};
    public OLMapWidget getRefMap() {
    	return refMap;
    }
	public String getHistoryToken() {
		StringBuilder token = new StringBuilder();
		token.append(";xlo="+getRefMap().getXlo());
		token.append(";xhi="+getRefMap().getXhi());
		token.append(";ylo="+getRefMap().getYlo());
		token.append(";yhi="+getRefMap().getYhi());
		if ( getCurrentOp() != null ) {
			token.append(";operation_id="+getCurrentOp().getID());
			token.append(";view="+getCurrentOperationView());
		}
		Map<String, String> options = getOptions();
		for (Iterator opIt = options.keySet().iterator(); opIt.hasNext();) {
			String name = (String) opIt.next();
			String value = options.get(name);
			if ( !value.equalsIgnoreCase("default") ) {
				token.append(";ferret_"+name+"="+value);
			}
		}		
		return token.toString();
	}
	public void setFromHistoryToken(Map<String, String> tokenMap, Map<String, String> optionsMap) {
		setOperation(tokenMap.get("operation_id"), tokenMap.get("view"));
		setLatLon(tokenMap.get("xlo"), tokenMap.get("xhi"), tokenMap.get("ylo"), tokenMap.get("yhi"));
		if ( optionsMap.size() >= 1 ) {
			optionsButton.setState(optionsMap);
		}
	}
}