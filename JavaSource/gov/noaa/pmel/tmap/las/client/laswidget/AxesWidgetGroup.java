package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

public class AxesWidgetGroup extends Composite {
	// Apply button only used in the horizontal layout.
	PushButton plotApplyButton;
	PushButton orthoApplyButton;
	OLMapWidget refMap;
	DateTimeWidget dateTimeWidget;
	AxisWidget zWidget;
	FlexTable plotAxesLayout;
	FlexTable orthoAxesLayout;
	DisclosurePanel plotPanel;
	DisclosurePanel orthoPanel;
	FlexTable panelLayout;
	HTML plotAxisMessage;
	String plotTitle;
	String orthoTitle;
	boolean hasZ;
	boolean hasT;
	List<String> viewAxes = new ArrayList<String>();   // This is just the view, but individual axes
	/**
	 * A widget to hold a set of x, y, z and t axis controls and to display them in groups according to the view.  Initially the map
	 * is at the top and z and t are below, but this can be switched.
	 * @param plot_title
	 * @param ortho_title
	 * @param layout
	 */
	public AxesWidgetGroup(String plot_title, String ortho_title, String layout, String width, String panel_title) {
		plotTitle = plot_title;
		orthoTitle = ortho_title;
		plotApplyButton = new PushButton("Apply");
		plotApplyButton.setTitle(panel_title);
		plotApplyButton.setWidth("35px");
		orthoApplyButton = new PushButton("Apply");
		orthoApplyButton.setTitle(ortho_title);
		orthoApplyButton.setWidth("35px");
		plotAxesLayout = new FlexTable();
		orthoAxesLayout = new FlexTable();
		refMap = new OLMapWidget();
		zWidget = new AxisWidget();
		zWidget.setVisible(false);
		dateTimeWidget = new DateTimeWidget();
		dateTimeWidget.setVisible(false);
		plotAxesLayout.setWidget(0, 0, plotApplyButton);
		plotAxesLayout.setWidget(1, 0, refMap);
		plotPanel = new DisclosurePanel(plot_title);
		plotPanel.add(plotAxesLayout);
		plotPanel.setOpen(true);
		orthoAxesLayout.setWidget(0, 0, orthoApplyButton);
		orthoAxesLayout.setWidget(1, 0, zWidget);
		orthoAxesLayout.setWidget(2, 0, dateTimeWidget);
		orthoPanel = new DisclosurePanel(ortho_title);
		orthoPanel.add(orthoAxesLayout);
		orthoPanel.setOpen(true);
		panelLayout = new FlexTable();
		panelLayout.setWidget(0, 0, plotPanel);
		if ( layout != null && layout.equals("horizontal") ) {
			panelLayout.setWidget(0, 1, orthoPanel);
		} else {
			panelLayout.setWidget(1, 0, orthoPanel);
		}
		if ( width != null && !width.equals("") ) {
		    plotPanel.setWidth(width);
		    orthoPanel.setWidth(width);
		}	
		initWidget(panelLayout);
	}

	public void init(GridSerializable grid) {
		hasZ = grid.hasZ();
		hasT = grid.hasT();
		if ( grid.hasZ() ) {
			zWidget = new AxisWidget(grid.getZAxis());
		} else {
			zWidget = new AxisWidget();
			zWidget.setVisible(false);
		}
		if ( grid.hasT() ) {
			dateTimeWidget = new DateTimeWidget(grid.getTAxis(), false);
		} else {
			dateTimeWidget = new DateTimeWidget();
			dateTimeWidget.setVisible(false);
		}
		if ( grid.hasX() && grid.hasY() ) {
			refMap.setDataExtent(Double.valueOf(grid.getYAxis().getLo()), 
					             Double.valueOf(grid.getYAxis().getHi()), 
					             Double.valueOf(grid.getXAxis().getLo()), 
					             Double.valueOf(grid.getXAxis().getHi()));
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
	public void setRange(String type, boolean range) {
		// Does not apply to x and y
		if ( type.equals("z") ) {
			zWidget.setRange(range);
		}
		if ( type.equals("t") ) {
			dateTimeWidget.setRange(range);
		}
	}
    private void arrangeAxes(String view, List<String> ortho, String compareAxis) {

    	// First put all of the axes into the correct panels.
    	plotAxesLayout.clear();
    	orthoAxesLayout.clear();
    	viewAxes.clear();
    	plotAxesLayout.setWidget(0, 0, plotApplyButton);
    	orthoAxesLayout.setWidget(0, 0, orthoApplyButton);
    	int plotAxesRow = 1;
    	if ( view.contains("x") || view.contains("y") ) {
    		plotAxesLayout.setWidget(plotAxesRow, 0, refMap);
    		refMap.setVisible(true);
    		plotAxesRow++;
    		if ( view.contains("x") ) {
    			viewAxes.add("x");
    		}
    		if ( view.contains("y") ) {
    			viewAxes.add("y");
    		}
    	}
    	
    	if ( view.contains("z") ) {
    		plotAxesLayout.setWidget(plotAxesRow, 0, zWidget);
    		zWidget.setVisible(true);
    		zWidget.setRange(true);
    		plotAxesRow++;
    		viewAxes.add("z");
    	}
    	
    	if ( view.contains("t") ) {
    		plotAxesLayout.setWidget(plotAxesRow, 0, dateTimeWidget);
    		dateTimeWidget.setVisible(true);
    		dateTimeWidget.setRange(true);
    		plotAxesRow++;
    		viewAxes.add("t");
    	}
    	/*
    	 If x a plot axis the plot and y is not then really the value of x should vary as a range and the range should be
    	 set in the controls for all plots and the value of Y should be allowed to vary in the plots if that is the selected
         "compare" axis which makes the interface pretty wild.
         
         Maybe the range value should be set with a slider with two handles, but for a simple interface it could be a map
         that only allows x to vary or a couple of text boxes.
        */
    	
    	int orthoAxesRow = 1;
    	if ( ortho.contains("x") || ortho.contains("y") || ortho.contains("xy") ) {
    		orthoAxesLayout.setWidget(orthoAxesRow, 0, refMap);
    		if ( view.contains("x") && !view.contains("y") ) {
    			if ( compareAxis.equals("y") ) {
    				plotAxisMessage = new HTML("(The x axis values are set in the map in the upper left panel.)");
    			} else {
    			    plotAxisMessage = new HTML("(The x axis values are set in the map below.)");
    			}
    			
    			plotAxesLayout.setWidget(orthoAxesRow, 0, plotAxisMessage);
    		}
    		if ( view.contains("y") && !view.contains("x") ) {
    			if ( compareAxis.equals("x") ) {
    				plotAxisMessage = new HTML("(The y axis values are set in the map in the upper left panel.)");
    			} else {
    			    plotAxisMessage = new HTML("(The y axis values are set in the map below.)");
    			}
    			plotAxesLayout.setWidget(orthoAxesRow, 0, plotAxisMessage);
    		}
    		orthoAxesRow++;
    	}
    	
    	if ( ortho.contains("z") ) {
    		orthoAxesLayout.setWidget(orthoAxesRow++, 0, zWidget);
    		zWidget.setVisible(true);
    		zWidget.setRange(false);
    	}
    	if ( ortho.contains("t") ) {
    		orthoAxesLayout.setWidget(orthoAxesRow++, 0, dateTimeWidget);
    		dateTimeWidget.setVisible(true);
    		dateTimeWidget.setRange(false);
    	}
    }
	public OLMapWidget getRefMap() {
		return refMap;
	}
    public void setZChangeHandler(ChangeHandler zchange) {
    	zWidget.addChangeHandler(zchange);
    }
    public void setTChangeHandler(ChangeHandler tchange) {
    	dateTimeWidget.addChangeHandler(tchange);
    }
    public DateTimeWidget getTAxis() {
    	return dateTimeWidget;
    }
    public AxisWidget getZAxis() {
    	return zWidget;
    }
    public void setCompareAxis(String view, List<String> ortho, String compareAxis) {
    	arrangeAxes(view, ortho, compareAxis);
    	plotPanel.setVisible(false);
    	if ( compareAxis.equals("xy") || compareAxis.equals("y") || compareAxis.equals("x") ) {
    		zWidget.setVisible(false);
    		dateTimeWidget.setVisible(false);
    		refMap.setVisible(true);
    	}
    	if ( compareAxis.equals("t") ) {
    		zWidget.setVisible(false);
    		dateTimeWidget.setVisible(true);
    		refMap.setVisible(false);
    	}
    	if ( compareAxis.equals("z") ) {
    		zWidget.setVisible(true);
    		dateTimeWidget.setVisible(false);
    		refMap.setVisible(false);
    	}
    }
    public void setFixedAxis(String view, List<String> ortho, String fixedAxis, String compareAxis) {
        arrangeAxes(view, ortho, compareAxis);
        
        for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
			String ax = (String) orthoIt.next();
			if ( !compareAxis.contains(ax) ) {
		        setAxisVisible(ax, true);
			} else {
			    setAxisVisible(ax, false);
			}
		}
    }
    public void showAll(String view, List<String> ortho) {
    	arrangeAxes(view, ortho, "");
    	plotPanel.setVisible(true);
    	orthoPanel.setVisible(true);
    	refMap.setVisible(true);
    	refMap.setTool(view);
    	if ( hasZ ) {
    		zWidget.setVisible(true);
    		if ( view.contains("z") ) {
    			zWidget.setRange(true);
    		} else {
    			zWidget.setRange(false);
    		}
    	}
    	if ( hasT ) {
    		dateTimeWidget.setVisible(true);
    		if ( view.contains("t") ) {
    			dateTimeWidget.setRange(true);
    		} else {
    			dateTimeWidget.setRange(false);
    		}
    	}
    }
    public void addApplyHandler(ClickHandler handler) {
    	plotApplyButton.addClickHandler(handler);
    	orthoApplyButton.addClickHandler(handler);
    }
    public List<String> getViewAxes() {
    	return viewAxes;
    }
    public void setWidth(String width) {
    	plotPanel.setWidth(width);
    	orthoPanel.setWidth(width);
    }

	public void setOpen(boolean b) {
		plotPanel.setOpen(b);
		orthoPanel.setOpen(b);
	}
}
