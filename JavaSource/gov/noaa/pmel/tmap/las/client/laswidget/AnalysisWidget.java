package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.AnalysisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class AnalysisWidget extends Composite {
	ListBox analysisType = new ListBox();
	DisclosurePanel disclosurePanel = new DisclosurePanel("Define Analysis");
	FlexTable layoutPanel = new FlexTable();
	
	ListBox analysisAxis = new ListBox();
	
	public AnalysisWidget() {
		
		analysisType.addItem("Average");
		analysisType.addItem("Minimum");
		analysisType.addItem("Maximum");
		analysisType.addItem("Sum");
		analysisType.addItem("Variance");
		
		
		layoutPanel.setWidget(0, 0, new Label("Analysis Type"));
        layoutPanel.setWidget(1, 0, analysisType);
        layoutPanel.setWidget(2, 0, new Label("Analysis Axis"));
        layoutPanel.setWidget(3, 0, analysisAxis);
        disclosurePanel.add(layoutPanel);		
        
		initWidget(disclosurePanel);
		
		
	}
	public void setAnalysisAxes(GridSerializable grid) {
		analysisAxis.clear();
		analysisAxis.addItem("Area");
		analysisAxis.addItem("Longitude");
		analysisAxis.addItem("Latitude");
		if ( grid.hasZ() ) {
			analysisAxis.addItem("Height/Depth");
		}
		if ( grid.hasT() ) {
			analysisAxis.addItem("Time");
		}
		
	}
	public AnalysisSerializable getAnalysisSerializable() {
		AnalysisSerializable analysis = new AnalysisSerializable();
		
		return analysis;
	}
}
