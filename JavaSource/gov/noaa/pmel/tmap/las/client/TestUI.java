package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;


public class TestUI extends LASEntryPoint {
	PopupPanel mDatasetPanel;
	ScrollPanel scrollPanel;
	DatasetWidget dsWidget;
	LASReferenceMap mMap;
	public void onModuleLoad() {
		super.onModuleLoad();
		scrollPanel = new ScrollPanel();
		dsWidget = new DatasetWidget();
		dsWidget.addTreeListener(new TreeListener() {

			public void onTreeItemSelected(TreeItem item) {
				Object u = item.getUserObject();
				if ( u instanceof VariableSerializable ) {
					VariableSerializable v = (VariableSerializable) u;
					mMap.zoomToGrid(v.getGrid());
					mDatasetPanel.hide();
				}
				
			}
			public void onTreeItemStateChanged(TreeItem item) {
				
				
			}
			
		});
		scrollPanel.add(dsWidget);
		mDatasetPanel = new PopupPanel(true);
		
		Grid popupGrid = new Grid(2, 1);		
		Button chooseDataset = new Button("Choose a Dataset");
		chooseDataset.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				mDatasetPanel.setPopupPosition(sender.getAbsoluteLeft(), sender.getAbsoluteTop());
				mDatasetPanel.show();			
			}
			
		});
		Button close = new Button("close");
		close.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				mDatasetPanel.hide();			
			}
		});
		mMap = new LASReferenceMap(LatLng.newInstance(0.0, 0.0), 1, 720, 360);
		
        dsWidget.init(rpcService);
        RootPanel.get("refmap").add(mMap);
        popupGrid.setWidget(0, 0, close);
        popupGrid.setWidget(1, 0, scrollPanel);
        mDatasetPanel.add(popupGrid);
        RootPanel.get("datasets").add(chooseDataset);
	}
	
	

}
