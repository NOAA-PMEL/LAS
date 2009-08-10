package gov.noaa.pmel.tmap.las.client.laswidget;


import gov.noaa.pmel.tmap.las.client.OperationButton;
import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class OperationsWidget extends StackPanel {
    RPCServiceAsync opService;
	VerticalPanel xyMap = new VerticalPanel();
	VerticalPanel linePlots = new VerticalPanel();
	VerticalPanel sectionPlots = new VerticalPanel();
	VerticalPanel hofmullerPlots = new VerticalPanel();
	OperationsMenu menu;
	boolean hasXYMap = false;
	boolean hasLinePlots = false;
	boolean hasSectionPlots = false;
	boolean hasHofmullerPlots = false;
	OperationSerializable[] ops;
	OperationSerializable currentOp;
	String currentView;
    ArrayList<OperationButton> buttons = new ArrayList<OperationButton>();
    ArrayList<ClickListener> clicks = new ArrayList<ClickListener>();
    String intervals;
    String initialOp;
    String initialView;
	/**
	 * Set up the StackPanel and the associated RPC.
	 */
	public OperationsWidget() {
		super();
		xyMap.add(new Label("Select a variable..."));
		add(xyMap, "Maps");
		
		linePlots.add(new Label("Select a variable..."));
		add(linePlots, "Line Plots");
		
		sectionPlots.add(new Label("Select a variable..."));
		add(sectionPlots, "Vertical Section Plots");
		
		hofmullerPlots.add(new Label("Select a variable..."));
		add(hofmullerPlots, "Hofmuller Plots");
		
		setSize("256px", "200px");
		
	}
	public void setOperations(RPCServiceAsync rpcService, String intervals, String dsID, String varID, String opID, String view, OperationsMenu menu) {
		this.opService = rpcService;
		this.menu = menu;
		this.intervals = intervals;
		this.initialOp = opID;
		this.initialView = view;
		this.currentView = initialView;
		if ( ops != null ) {
			setOps();
		}  else {
			opService.getOperations(null, dsID, varID, operationsCallback);	
		}
	}
	AsyncCallback operationsCallback = new AsyncCallback() {
		public void onSuccess(Object result) {
			ops = (OperationSerializable[]) result;
			setOps();
			if ( menu != null ) {
			    menu.setMenus(ops);
			}
		}
		public void onFailure(Throwable caught) {
			// TODO Alert users...
		}
	};
	ClickListener buttonListener = new ClickListener() {

		public void onClick(Widget sender) {
			OperationButton button = (OperationButton) sender;
			currentOp = button.getOperation();
			currentView = button.getView();
		}
    	
    };
	
	public void setOps() {
		hasHofmullerPlots = false;
		hasLinePlots = false;
		hasSectionPlots = false;
		hasXYMap = false;
		
		buttons.clear();
		xyMap.clear();
		linePlots.clear();
		sectionPlots.clear();
		hofmullerPlots.clear();
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
									xyMap.clear();
									hasXYMap = true;
								}
								OperationButton button = new OperationButton("op", "Latitude-Longitude");
								button.setView(view);
								button.setOperation(op);
								button.addClickListener(buttonListener);
								button.setChecked(true);
								buttons.add(button);
								currentOp = button.getOperation();
								currentView = "xy";
								xyMap.add(button);
							} else if ( (view.equals("x") && intervals.contains("x")) || (view.equals("y") && intervals.contains("y")) || (view.equals("z") && intervals.contains("z")) || (view.equals("t") && intervals.contains("t")) ) {
								if ( !hasLinePlots ) {
									linePlots.clear();
									hasLinePlots = true;
								}
								OperationButton button;
								if ( view.equals("x") ) {
									button = new OperationButton("op", "Longitude");
								} else if ( view.equals("y") ) {
									button = new OperationButton("op", "Latitude");
								} else if ( view.equals("z") ) {
									// TODO, get the grid and initialize from the grid so you have the z-axis label.
									button = new OperationButton("op", "Z");
								} else {
									button = new OperationButton("op", "Time");
								}
								
								button.setView(view);
								button.setOperation(op);
								button.addClickListener(buttonListener);
								buttons.add(button);
								linePlots.add(button);
							} else if ( (view.equals("xz") && intervals.contains("x") && intervals.contains("z") )|| 
									    (view.equals("yz") && intervals.contains("y") && intervals.contains("z") )) {
								if ( !hasSectionPlots ) {
									sectionPlots.clear();
									hasSectionPlots = true;
								}
								OperationButton button;
								if ( view.equals("xz") ) {
									button = new OperationButton("op", "Longitude-z");
								} else {
									button = new OperationButton("op", "Latitude-z");
								}
								
								button.setOperation(op);
								button.setView(view);
								button.addClickListener(buttonListener);
								buttons.add(button);
								sectionPlots.add(button);
							} else if ( (view.equals("xt") && intervals.contains("x") && intervals.contains("t") ) || 
									    (view.equals("yt") && intervals.contains("y") && intervals.contains("t") ) || 
									    (view.equals("zt") && intervals.contains("z") && intervals.contains("t") ) ) {
								if ( !hasHofmullerPlots ) {
									hofmullerPlots.clear();
									hasHofmullerPlots = true;
								}
								OperationButton button;
								if ( view.equals("xt") ) {
								    button = new OperationButton("op", "Longitude-time");
								} else if (view.equals("yt") ) {
									button = new OperationButton("op", "Latitude-time");
								} else {
									button = new OperationButton("op", "Z-time");
								}
								button.setView(view);
								button.setOperation(op);
								button.addClickListener(buttonListener);
								buttons.add(button);
								hofmullerPlots.add(button);
							}
						}
					}
				}
			}
		}
		for (Iterator clickIt = clicks.iterator(); clickIt.hasNext();) {
			ClickListener click = (ClickListener) clickIt.next();
			addClickListener(click);
		}
		if ( initialOp != null && initialView != null ) {
			setOperation(initialOp, initialView);
		}
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
	public void addClickListener(ClickListener operationsClickListener) {
		if ( !clicks.contains(operationsClickListener) ) {
			clicks.add(operationsClickListener);
		}
		for (Iterator buttonIt = buttons.iterator(); buttonIt.hasNext();) {
			OperationButton button = (OperationButton) buttonIt.next();
			button.addClickListener(operationsClickListener);
		}
	}
	public void setOperation(String id, String view) {
		for (Iterator buttonId = buttons.iterator(); buttonId.hasNext();) {
			OperationButton button = (OperationButton) buttonId.next();
			if ( button.getOperation().getID().equals(id) && button.getView().equals(view) ) {
				button.setChecked(true);
				currentOp = button.getOperation();
				currentView = button.getView();
			} else {
				button.setChecked(false);
			}
		}
		
	}
}
