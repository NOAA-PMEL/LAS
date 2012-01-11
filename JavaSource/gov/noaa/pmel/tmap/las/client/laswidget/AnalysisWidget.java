package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.AnalysisAxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.AnalysisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.AxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
/**
 * A widget for setting up an analysis Widget in LAS --- NOT YET WORKING.
 * @author rhs
 *
 */
public class AnalysisWidget extends Composite {
	ListBox analysisType = new ListBox();
	DisclosurePanel disclosurePanel = new DisclosurePanel("Optional Calculations");
	FlexTable layoutPanel = new FlexTable();
	ListBox analysisAxis = new ListBox();
	CheckBox apply = new CheckBox("Apply Analysis");
	
	// Axis state information.
	AnalysisAxisSerializable xAxis = new AnalysisAxisSerializable();
	AnalysisAxisSerializable yAxis = new AnalysisAxisSerializable();
	AnalysisAxisSerializable zAxis = new AnalysisAxisSerializable();
	AnalysisAxisSerializable tAxis = new AnalysisAxisSerializable();
	
	// The container
	AnalysisSerializable analysis = new AnalysisSerializable();

	
	public AnalysisWidget(String width, String tile_server) {
		
		disclosurePanel.setTitle("Select a statistic to compute and the space and time dimensions over which to compute it");
		
		analysisType.addItem("Average");
		analysisType.addItem("Minimum");
		analysisType.addItem("Maximum");
		analysisType.addItem("Sum");
		analysisType.addItem("Variance");
		analysisType.addChangeHandler(opChangeHandler);
		
		layoutPanel.getFlexCellFormatter().setColSpan(0, 0, 2);
		layoutPanel.setWidget(0, 0, apply);
		layoutPanel.setWidget(1, 0, new Label("Compute:"));
        layoutPanel.setWidget(1, 1, analysisType);
        layoutPanel.setWidget(2, 0, new Label("over:"));
        layoutPanel.setWidget(2, 1, analysisAxis);
        layoutPanel.getFlexCellFormatter().setColSpan(3, 0, 2);
        
        disclosurePanel.add(layoutPanel);		
        
        analysis.getAxes().put("x", xAxis);
        analysis.getAxes().put("y", yAxis);
        analysis.getAxes().put("z", zAxis);
        analysis.getAxes().put("t", tAxis);
        
        analysisAxis.addChangeHandler(axisChangeHandler);
        
        apply.addClickHandler(applyClickHandler);
		initWidget(disclosurePanel);
		
		
	}
	public void setAnalysisAxes(GridSerializable grid) {
		analysisAxis.clear();
		analysisAxis.addItem("Area", "xy");
		analysisAxis.addItem("Longitude", "x");
		analysisAxis.addItem("Latitude", "y");
		if ( grid.hasZ() ) {
			analysisAxis.addItem("Height/Depth", "z");
		}
		if ( grid.hasT() ) {
			analysisAxis.addItem("Time", "t");
		}
		
	}
	public AnalysisSerializable getAnalysisSerializable() {		
		return analysis;
	}
	public void setLabel(String label) {
		analysis.setLabel(label);
	}
	public void addAnalysisAxesChangeHandler(ChangeHandler analysisAxesChange) {
		analysisAxis.addChangeHandler(analysisAxesChange);
		
	}
	public void addAnalysisOpChangeHandler(ChangeHandler analysisOpChange) {
		analysisType.addChangeHandler(analysisOpChange);
	}
	public boolean isActive() {
		return apply.getValue();
	}
	public String getAnalysisAxis() {
		return analysisAxis.getValue(analysisAxis.getSelectedIndex());
	}
	public void addAnalysisCheckHandler(ClickHandler analysisActiveChange) {
		apply.addClickHandler(analysisActiveChange);
	}
	public ChangeHandler axisChangeHandler = new ChangeHandler() {

		@Override
		public void onChange(ChangeEvent event) {
			set();
		}
		
	};
	public ChangeHandler opChangeHandler = new ChangeHandler() {

		@Override
		public void onChange(ChangeEvent event) {
			set();	
		}
	};
	public ClickHandler applyClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			if (apply.getValue()) {
				set();
			}
		}		
	};
	private void set() {
	    String axis = analysisAxis.getValue(analysisAxis.getSelectedIndex());
		String op = analysisType.getValue(analysisType.getSelectedIndex());
		xAxis.setOp(null);
		yAxis.setOp(null);
		zAxis.setOp(null);
		tAxis.setOp(null);
		if ( axis.equals("xy") ) {
			xAxis.setType("x");
			xAxis.setOp(op);
			yAxis.setType("y");
			yAxis.setOp(op);
		} else if ( axis.equals("x") ) {
			xAxis.setType("x");
			xAxis.setOp(op);
		} else if ( axis.equals("y") ) {
			yAxis.setType("y");
			yAxis.setOp(op);
		}  else if ( axis.equals("z") ) {
			zAxis.setType("z");
			zAxis.setOp(op);
		} else if ( axis.equals("t") ) {
			tAxis.setType("t");
			tAxis.setOp(op);
		}
	}
	public void setActive(boolean b) {
		apply.setValue(b);
	}
}
