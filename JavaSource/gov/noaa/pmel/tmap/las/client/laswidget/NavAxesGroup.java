package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class NavAxesGroup extends Composite {
	OLMapWidget refMap;
	DateTimeWidget dateTimeWidget;
	AxisWidget zWidget;
	FlexTable axesLayout = new FlexTable();
	FlowPanel axesPanel;//DisclosurePanel axesPanel;
	String axesTitle;
	HTML message = new HTML("message");
	boolean hasZ;
	boolean hasT;
	boolean hasE;
	boolean axesPanelIsOpen = true;
	public NavAxesGroup(String title, String width, String tile_server) {
		refMap = new OLMapWidget("128px", "256px", tile_server);
		refMap.activateNativeHooks();
		zWidget = new AxisWidget();
		zWidget.setVisible(false);
		dateTimeWidget = new DateTimeWidget();
		dateTimeWidget.setVisible(false);
		message.setVisible(false);
		axesLayout.setWidget(0, 0, message);
		axesLayout.setWidget(1, 0, refMap);
		axesPanel = new FlowPanel();//new DisclosurePanel(title);
		axesPanel.add(axesLayout);
		axesPanel.setVisible(true);//.setOpen(true);
		message.addStyleName("TinyBlue");
		initWidget(axesPanel);
	}
	public void setMessage(String mess) {
		message.setVisible(true);
		message.setHTML(mess);
	}
	public void init(GridSerializable grid) {
		hasZ = grid.hasZ();
		hasT = grid.hasT();
		if ( grid.hasZ() ) {
			zWidget = new AxisWidget(grid.getZAxis());
			zWidget.setVisible(true);
		} else {
			zWidget = new AxisWidget();
			zWidget.setVisible(false);
		}
		if ( grid.hasT() ) {
			dateTimeWidget = new DateTimeWidget(grid.getTAxis(), false);
			dateTimeWidget.setVisible(true);
		} else {
			dateTimeWidget = new DateTimeWidget();
			dateTimeWidget.setVisible(false);
		}
		axesLayout.setWidget(2, 0, zWidget);
		axesLayout.setWidget(3, 0, dateTimeWidget);
		if ( grid.hasX() && grid.hasY() ) {
			refMap.setDataExtent(Double.valueOf(grid.getYAxis().getLo()), 
					             Double.valueOf(grid.getYAxis().getHi()), 
					             Double.valueOf(grid.getXAxis().getLo()), 
					             Double.valueOf(grid.getXAxis().getHi()));
		}
	}
	public DateTimeWidget getTAxis() {
    	return dateTimeWidget;
    }
    public AxisWidget getZAxis() {
    	return zWidget;
    }
    public OLMapWidget getRefMap() {
    	return refMap;
    }
    public void setOpen(boolean value) {
    	axesPanel.setVisible(value);//.setOpen(value);
    }
    public void closePanels() {
    	axesPanelIsOpen = axesPanel.isVisible();//.isOpen();
    	axesPanel.setVisible(false);//.setOpen(false);
    }
    public void restorePanels() {
    	axesPanel.setVisible(axesPanelIsOpen);//.setOpen(axesPanelIsOpen);
    }
    public void setRange(String type, boolean range) {
		// Does not apply to x and y
		if ( type.equals("z") ) {
			zWidget.setRange(range);
		}
		if ( type.equals("t") ) {
			dateTimeWidget.setRange(range);
		}
	}
	public void showMessage(boolean b) {
		message.setVisible(b);
	}
	public void showViewAxes(String xView, List<String> ortho, String analysis) {
		for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
    		String ax = (String) orthoIt.next();
    		setAxisVisible(ax, false);
			setRange(ax, true);
		}
		for (int i = 0; i < xView.length(); i++) {
			String type = xView.substring(i, i+1);
			setAxisVisible(type, true);
			setRange(type, true);
		}
		if ( analysis != null ) {
		    for ( int i = 0; i < analysis.length(); i++ ) {
		        String type = analysis.substring(i, i+1);
		        setAxisVisible(type, true);
		        setRange(type, true);
		    }
		}
		// Always show the map in the nav window since it will always control the compare panel selection.
		setAxisVisible("x", true);
		setAxisVisible("y", true);
	}
	public void setFixedAxis(String xView, List<String> ortho, String compareAxis) {
		if ( compareAxis != null ) {
        	for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
        		String ax = (String) orthoIt.next();
        		if ( !compareAxis.contains(ax) ) {
        			setAxisVisible(ax, true);
        		} else {
        			setAxisVisible(ax, false);
        		}
        	}
        } else {
        	// Otherwise just show all the orthogonal axes
        	for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
        		String ax = (String) orthoIt.next();
        		setAxisVisible(ax, true);
        	}
        }
	}
	private void setAxisVisible(String type, boolean visible) {
		if ( type.contains("x") ) {
			refMap.setVisible(visible);
		}
		if ( type.contains("y") ) {
			refMap.setVisible(visible);
		}
		if ( type.equals("z") ) {
			zWidget.setVisible(visible);
		}
		if ( type.equals("t") ) {
			dateTimeWidget.setVisible(visible);
		}
	}
}
