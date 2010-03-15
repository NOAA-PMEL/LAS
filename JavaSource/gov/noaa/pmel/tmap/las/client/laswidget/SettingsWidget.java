package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

public class SettingsWidget extends Composite {

	String layout;
	/*
	 * Objects common to any layout.
	 */
	protected OperationsWidget operations;
	protected PushButton closeButton;
	protected PushButton applyButton;
	protected HorizontalPanel closeAndApply;
	protected HorizontalPanel buttonBar;
	protected OLMapWidget refMap;
	protected DatasetButton datasetButton;
	protected OptionsButton optionsButton;
	protected String operationID;
	protected String optionID;
	boolean usePanel = false;
	protected ComparisonAxisSelector comparisonAxisSelector = new ComparisonAxisSelector(new ArrayList<String>());

	/*
	 * Objects specific to the button layout
	 */

	protected Button settingsButton;
	protected DialogBox settingsDialog;
	protected VerticalPanel mainPanel;
	protected HorizontalPanel interiorPanel;
	protected FlexTable leftInteriorPanel;
	protected VerticalPanel rightInteriorPanel;

	/*
	 * Objects specific to the vertical panel layout
	 */
	Grid vertical = new Grid(5,1);

	public SettingsWidget(String title, String panelID, String operationID, String optionID, String layout) {
		this.operationID = operationID;
		this.optionID = optionID;
		this.layout = layout;
		applyButton = new PushButton("Apply");
		applyButton.setTitle("Apply changes to "+panelID);
		applyButton.addClickListener(applyClick);
		closeButton = new PushButton("Close");
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
		buttonBar = new HorizontalPanel();
		
		refMap = new OLMapWidget();
		operations = new OperationsWidget(title);
		operations.addClickHandler(operationsClickHandler);
		leftInteriorPanel = new FlexTable();
		if ( layout.equals("button") ) {
			settingsButton = new Button (title);
			settingsButton.addClickListener(settingsButtonClick);
			interiorPanel = new HorizontalPanel();
			mainPanel = new VerticalPanel();
			settingsDialog = new DialogBox(false, false);
			settingsDialog.setText("Set Region and Plot Options for "+panelID+" ... [Drag Me]");		
			rightInteriorPanel = new VerticalPanel();
			interiorPanel.add(leftInteriorPanel);
			buttonBar.add(datasetButton);
			buttonBar.add(optionsButton);
			rightInteriorPanel.add(buttonBar);
			rightInteriorPanel.add(operations);
			interiorPanel.add(rightInteriorPanel);
			mainPanel.add(closeAndApply);
			mainPanel.add(interiorPanel);
			settingsDialog.add(mainPanel);
			initWidget(settingsButton);	
		} else {
			FlexCellFormatter cellFormatter = leftInteriorPanel.getFlexCellFormatter();
		    cellFormatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
			leftInteriorPanel.setHTML(0, 0, "Plot Axes");
			leftInteriorPanel.setWidget(1, 0, refMap);
			DecoratorPanel decPanel = new DecoratorPanel();
			decPanel.add(leftInteriorPanel);
			decPanel.setWidth("260px");
			if ( title.equals("") ) {
				
				vertical.setWidget(0, 0, buttonBar);
				vertical.setWidget(1, 0, decPanel);
				vertical.setWidget(2, 0, comparisonAxisSelector);
				vertical.setWidget(3, 0, operations);
			} else {
				vertical.setWidget(0, 0, new Label(title));
				vertical.setWidget(1, 0, buttonBar);
				vertical.setWidget(2, 0, decPanel);
				vertical.setWidget(3, 0, comparisonAxisSelector);
				vertical.setWidget(4, 0, operations);
			}
			buttonBar.add(applyButton);
			buttonBar.add(datasetButton);
			buttonBar.add(optionsButton);
			
			vertical.setWidth("256px");
			initWidget(vertical);
		}

	}

	public void showMap() {
		refMap.setVisible(true);
	}
	public void hideMap() {
		refMap.setVisible(false);
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
	public ClickHandler operationsClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
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

	public void setOperations(String intervals, String dsID, String varID, String opID, String view) {
		operations.setOperations(intervals, dsID, varID, opID, view);
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

	public void addOperationClickHandler(ClickHandler operationsClickHandler) {
		operations.addClickHandler(operationsClickHandler);
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
			resetRefMap();
			
		}		
	};
    public OLMapWidget getRefMap() {
    	return refMap;
    }
    public OLMapWidget getDisplayRefMap() {
    	resetRefMap();
    	return refMap;
    }
    private void resetRefMap() {
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
				
		refMap = new OLMapWidget();
		refMap.setDataExtentOnly(data[1], data[0], data[3], data[2], delta);
		refMap.setToolOnly(tool);
		refMap.setCenter(center[0], center[1], zoom);
		refMap.setCurrentSelection(ylo, yhi, xlo, xhi);
		
		leftInteriorPanel.add(refMap);
		
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

	public void setOperationsMenu(OperationsMenu operationsMenu) {
		operations.setOperationsMenu(operationsMenu);
	}

	public void addComparisonAxisSelector(List<String> ortho) {
		// TODO Auto-generated method stub
		
	}

	public ComparisonAxisSelector getComparisonAxisSelector() {
		return comparisonAxisSelector;
	}

	public void setComparisonAxisSelector(ComparisonAxisSelector comparisonAxisSelector) {
		this.comparisonAxisSelector = comparisonAxisSelector;
	}

	public void addComparisonAxisSelectorChangeHandler(ChangeHandler compareAxisChangeHandler) {
		this.comparisonAxisSelector.addAxesChangeHandler(compareAxisChangeHandler);		
	}
}