package gov.noaa.pmel.tmap.las.client.laswidget;


import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class OperationsWidget extends Composite {
    boolean isOpen;
	FlexTable layout = new FlexTable();
	DisclosurePanel xyMap = new DisclosurePanel("Maps");
	FlexTable xyMapTable = new FlexTable();
	DisclosurePanel linePlots = new DisclosurePanel("Line Plots");
	FlexTable linePlotsTable = new FlexTable();
	DisclosurePanel sectionPlots = new DisclosurePanel("Vertical Section Plots");
	FlexTable sectionPlotsTable = new FlexTable();
	DisclosurePanel hofmullerPlots = new DisclosurePanel("Hofmuller Plots");
	FlexTable hofmullerPlotsTable = new FlexTable();
	
	int xyMapRow = 0;
	int linePlotsRow = 0;
	int sectionPlotsRow = 0;
	int hofmullerPlotsRow = 0;
	
	boolean hasXYMap = false;
	boolean hasLinePlots = false;
	boolean hasSectionPlots = false;
	boolean hasHofmullerPlots = false;
	OperationSerializable[] ops;
	OperationSerializable currentOp;
	String currentView;
    ArrayList<OperationRadioButton> buttons = new ArrayList<OperationRadioButton>();
    ArrayList<ClickHandler> clicks = new ArrayList<ClickHandler>();
    String intervals;
    String initialOp;
    String initialView;
    String groupName;
    
    // Optional OperationsMenu.  If set, then it is kept in sync with this widget.
    OperationsMenu operationsMenu = null;
    
	public OperationsMenu getOperationsMenu() {
		return operationsMenu;
	}
	public void setOperationsMenu(OperationsMenu operationsMenu) {
		this.operationsMenu = operationsMenu;
	}
	/**
	 * Set up the StackPanel and the associated RPC.
	 */
	public OperationsWidget(String groupName) {
		this.groupName = groupName;
		layout.setWidth("256px");
	    initWidget(layout);
	}
	public void setOperations(String intervals, String dsID, String varID, String opID, String view) {
		
		this.intervals = intervals;
		this.initialOp = opID;
		this.initialView = view;
		this.currentView = initialView;
		if ( ops != null ) {
			setOps();
		}  else {
			Util.getRPCService().getOperations(null, dsID, varID, operationsCallback);	
		}
	}
	AsyncCallback operationsCallback = new AsyncCallback() {
		public void onSuccess(Object result) {
			ops = (OperationSerializable[]) result;
			setOps();		
		}
		public void onFailure(Throwable caught) {
			// TODO Alert users...
		}
	};
	ClickListener buttonListener = new ClickListener() {

		public void onClick(Widget sender) {
			OperationRadioButton button = (OperationRadioButton) sender;
			currentOp = button.getOperation();
			currentView = button.getView();
		}
    	
    };
	private void setMenu() {
		if ( operationsMenu != null ) {
			operationsMenu.setMenus(ops, currentView);
		}
	}
	private void setOps() {
		isOpen = true;
		hasHofmullerPlots = false;
		hasLinePlots = false;
		hasSectionPlots = false;
		hasXYMap = false;
		layout.clear();
		buttons.clear();
		xyMap.clear();
		linePlots.clear();
		sectionPlots.clear();
		hofmullerPlots.clear();
		xyMapRow = 0;
		linePlotsRow = 0;
		sectionPlotsRow = 0;
		hofmullerPlotsRow = 0;
		for (int i = 0; i < ops.length; i++) {
			OperationSerializable op = ops[i];
			String category = op.getAttributes().get("category");
			if ( category.equals("visualization")) {
				List<String> views = op.getViews();
				for (Iterator viewIt = views.iterator(); viewIt.hasNext();) {
					String view = (String) viewIt.next();
					if ( !op.getName().contains("omparison") ) {
						Map<String, String> attrs = op.getAttributes();
						
						// A hack to allow zoomable plots in the old interface and use Plot_2D_XY here.
						if ( (attrs != null && attrs.containsKey("default") && !op.getID().equals("XY_zoomable_image"))|| op.getID().equals("Plot_2D_XY")) {
							if ( view.equals("xy") && (intervals.contains("x") && intervals.contains("y"))) {	
								if (!hasXYMap) {
									xyMapTable.clear();
									hasXYMap = true;
								}
								OperationRadioButton button = new OperationRadioButton(groupName, "Latitude-Longitude");
								button.setView(view);
								button.setOperation(op);
								button.addClickListener(buttonListener);
								button.setChecked(true);
								buttons.add(button);
								currentOp = button.getOperation();
								currentView = "xy";
								xyMapTable.setWidget(xyMapRow, 0, button);
								xyMapRow++;
							} else if ( (view.equals("x") && intervals.contains("x")) || (view.equals("y") && intervals.contains("y")) || (view.equals("z") && intervals.contains("z")) || (view.equals("t") && intervals.contains("t")) ) {
								if ( !hasLinePlots ) {
									linePlotsTable.clear();
									hasLinePlots = true;
								}
								OperationRadioButton button;
								if ( view.equals("x") ) {
									button = new OperationRadioButton(groupName, "Longitude");
								} else if ( view.equals("y") ) {
									button = new OperationRadioButton(groupName, "Latitude");
								} else if ( view.equals("z") ) {
									// TODO, get the grid and initialize from the grid so you have the z-axis label.
									button = new OperationRadioButton(groupName, "Z");
								} else {
									button = new OperationRadioButton(groupName, "Time");
								}
								
								button.setView(view);
								button.setOperation(op);
								button.addClickListener(buttonListener);
								buttons.add(button);
								linePlotsTable.setWidget(linePlotsRow, 0, button);
								linePlotsRow++;
							} else if ( (view.equals("xz") && intervals.contains("x") && intervals.contains("z") )|| 
									    (view.equals("yz") && intervals.contains("y") && intervals.contains("z") )) {
								if ( !hasSectionPlots ) {
									sectionPlotsTable.clear();
									hasSectionPlots = true;
								}
								OperationRadioButton button;
								if ( view.equals("xz") ) {
									button = new OperationRadioButton(groupName, "Longitude-z");
								} else {
									button = new OperationRadioButton(groupName, "Latitude-z");
								}
								
								button.setOperation(op);
								button.setView(view);
								button.addClickListener(buttonListener);
								buttons.add(button);
								sectionPlotsTable.setWidget(sectionPlotsRow, 0, button);
								sectionPlotsRow++;
							} else if ( (view.equals("xt") && intervals.contains("x") && intervals.contains("t") ) || 
									    (view.equals("yt") && intervals.contains("y") && intervals.contains("t") ) || 
									    (view.equals("zt") && intervals.contains("z") && intervals.contains("t") ) ) {
								if ( !hasHofmullerPlots ) {
									hofmullerPlotsTable.clear();
									hasHofmullerPlots = true;
								}
								OperationRadioButton button;
								if ( view.equals("xt") ) {
								    button = new OperationRadioButton(groupName, "Longitude-time");
								} else if (view.equals("yt") ) {
									button = new OperationRadioButton(groupName, "Latitude-time");
								} else {
									button = new OperationRadioButton(groupName, "Z-time");
								}
								button.setView(view);
								button.setOperation(op);
								button.addClickListener(buttonListener);
								buttons.add(button);
								hofmullerPlotsTable.setWidget(hofmullerPlotsRow, 0, button);
								hofmullerPlotsRow++;
							}
						}
					}
				}
			}
		}
		int row = 0;
		if ( hasXYMap ) {
			xyMap.add(xyMapTable);
			layout.setWidget(row, 0, xyMap);
			xyMap.setOpen(true);
			row++;
		}
		if ( hasLinePlots ) {
			linePlots.add(linePlotsTable);
			layout.setWidget(row, 0, linePlots);
			linePlots.setOpen(true);
			row++;
		}
		if ( hasSectionPlots ) {
			sectionPlots.add(sectionPlotsTable);
			layout.setWidget(row, 0, sectionPlots);
			sectionPlots.setOpen(true);
			row++;
		}
		if ( hasHofmullerPlots ) {
			hofmullerPlots.add(hofmullerPlotsTable);
			layout.setWidget(row, 0, hofmullerPlots);
			hofmullerPlots.setOpen(true);
		}
		for (Iterator clickIt = clicks.iterator(); clickIt.hasNext();) {
			ClickHandler click = (ClickHandler) clickIt.next();
			addClickHandler(click);
		}
		if ( initialOp != null && initialView != null ) {
			setOperation(initialOp, initialView);
		}
		setMenu();
	}
	public OperationSerializable[] getOperationsSerializable() {
		return ops;
	}
	public OperationSerializable getCurrentOp() {
		return currentOp;
	}
	public String getCurrentView() {
		return currentView;
	}
	public void addClickHandler(ClickHandler operationsClickHandler) {
		if ( !clicks.contains(operationsClickHandler) ) {
			clicks.add(operationsClickHandler);
		}
		for (Iterator buttonIt = buttons.iterator(); buttonIt.hasNext();) {
			OperationRadioButton button = (OperationRadioButton) buttonIt.next();
			button.addClickHandler(operationsClickHandler);
		}
	}
	public void setOperation(String id, String view) {
		for (Iterator buttonId = buttons.iterator(); buttonId.hasNext();) {
			OperationRadioButton button = (OperationRadioButton) buttonId.next();
			if ( button.getOperation().getID().equals(id) && button.getView().equals(view) ) {
				button.setChecked(true);
				currentOp = button.getOperation();
				currentView = button.getView();
			} else {
				button.setChecked(false);
			}
		}
	}
	public void setOpen(boolean open) {
		isOpen = open;
		xyMap.setOpen(open);
		linePlots.setOpen(open);
		sectionPlots.setOpen(open);
		hofmullerPlots.setOpen(open);
	}
	public boolean isOpen() {
		return isOpen;
	}
}
