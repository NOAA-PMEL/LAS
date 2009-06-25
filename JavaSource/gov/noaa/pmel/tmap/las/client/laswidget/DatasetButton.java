package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DatasetButton extends Composite {
	Button choose;
	PopupPanel datasetPanel;
	Label dataset_name = new Label("");
	Label variable_name = new Label("");
	DecoratorPanel selection = new DecoratorPanel();
	VerticalPanel verticalPanel = new VerticalPanel();
	DatasetWidget datasetWidget;
	VariableSerializable selectedVariable;
	Grid popupGrid;
	Button close;
	public DatasetButton (RPCServiceAsync rpcService) {
		choose = new Button("Data Set");
		choose.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				datasetPanel.setPopupPosition(choose.getAbsoluteLeft(), choose.getAbsoluteTop()-60);
				datasetPanel.show();
			}
		});
		datasetPanel = new PopupPanel(false);
		datasetWidget = new DatasetWidget();
		datasetWidget.addTreeListener(new TreeListener() {
			public void onTreeItemSelected(TreeItem item) {
				Object u = item.getUserObject();
				if ( u instanceof VariableSerializable ) {
					selectedVariable = (VariableSerializable) u;
					dataset_name.setText(selectedVariable.getDSName());
					variable_name.setText(selectedVariable.getName());
					// Don't hide on select.  Wait for close.
					// datasetPanel.hide();
				}		
			}
			public void onTreeItemStateChanged(TreeItem item) {
			}
			
		});
		datasetWidget.init(rpcService);
		popupGrid = new Grid(3, 1);		
		close = new Button("close");
		close.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				datasetPanel.hide();			
			}
		});
		verticalPanel.add(dataset_name);
		verticalPanel.add(variable_name);
		selection.add(verticalPanel);
		popupGrid.setWidget(0, 0, close);
		popupGrid.setWidget(1, 0, selection);
		popupGrid.setWidget(2, 0, datasetWidget);
		datasetPanel.add(popupGrid);
		datasetPanel.hide();
		initWidget(choose);
	}
	public VariableSerializable getSelectedVariable() {
		return selectedVariable;
	}
	public void addTreeListener(TreeListener treeListener) {
		datasetWidget.addTreeListener(treeListener);
	}
}
