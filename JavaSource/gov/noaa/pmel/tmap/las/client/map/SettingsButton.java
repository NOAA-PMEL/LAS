package gov.noaa.pmel.tmap.las.client.map;

import java.util.ArrayList;
import java.util.List;

import gov.noaa.pmel.tmap.las.client.OperationsMenu;
import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DatasetButton;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.OperationsWidget;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
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
	OperationsWidget operations;
	Button closeButton;
	Button applyButton;
	Button cancelButton;
	ReferenceMap refMap;
	Label dateTimeLabel = new Label("Select a date/time:");
	DateTimeWidget dateTimeWidget = new DateTimeWidget();
	Label zLabel = new Label("z");
	AxisWidget zAxisWidget = new AxisWidget();
	CheckBox apply = new CheckBox("Use setting for this panel.");
	DatasetButton datasetButton;
	int popupTop;
	int popupLeft;
	
	public SettingsButton (LatLng center, int zoom, int xpix, int ypix, String panelID, RPCServiceAsync rpcService) {
		settingsButton = new Button ("Panel Settings");
		settingsButton.addClickListener(settingsButtonClick);
		
		datasetButton = new DatasetButton(rpcService);
		
		
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
		/*
		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(closeClick);
		*/
		
		buttonFlow = new HorizontalPanel();
		buttonFlow.add(closeButton);
		buttonFlow.add(applyButton);
		//buttonFlow.add(cancelButton);

		leftInteriorPanel.add(buttonFlow);
	    leftInteriorPanel.add(datasetButton);
		operations = new OperationsWidget();
		leftInteriorPanel.add(operations);
		
		interiorPanel.add(leftInteriorPanel);
		
		refMap = new ReferenceMap(center, zoom, xpix, ypix);
		rightInteriorPanel.add(refMap);
		
		interiorPanel.add(rightInteriorPanel);

		mainPanel.add(apply);
		mainPanel.add(interiorPanel);
		settingsDialog.add(mainPanel);
		popupLeft = settingsButton.getAbsoluteLeft();
		popupTop = settingsButton.getAbsoluteTop();
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
	public void setDateTimeWidget(DateTimeWidget t) {
		this.dateTimeWidget = t;
		leftInteriorPanel.add(dateTimeLabel);
		leftInteriorPanel.add(dateTimeWidget);
	}
	public void setZAxis(AxisWidget z) {
		this.zAxisWidget = z;
		zLabel.setText("Select "+z.getLabel()+" value: ");
		leftInteriorPanel.add(zLabel);
		leftInteriorPanel.add(zAxisWidget);
	}
	public DateTimeWidget getDateWidget() {
		return dateTimeWidget;
	}
	public AxisWidget getZWidget() {
		return zAxisWidget;
	}
	
	public void setLatLon(String xlo, String xhi, String ylo, String yhi) {
		refMap.setLatLon(xlo, xhi, ylo, yhi);
	}
	public CheckBox getApply() {
		return apply;
	}
	public void addDatasetTreeListener (TreeListener datasetTreeListener) {
	    datasetButton.addTreeListener(datasetTreeListener);
	}
	public void removeAxes() {
		if ( dateTimeLabel != null ) {
			leftInteriorPanel.remove(dateTimeLabel);
		}
		if ( dateTimeWidget != null ) {
			leftInteriorPanel.remove(dateTimeWidget);
		}
		if ( zLabel != null ) {
			leftInteriorPanel.remove(zLabel);
		}
		if ( zAxisWidget != null ) {
			leftInteriorPanel.remove(zAxisWidget);
		}		
	}
}
