package gov.noaa.pmel.tmap.las.client;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Window;
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
		add(sectionPlots, "Section Plots");
		
		hofmullerPlots.add(new Label("Select a variable..."));
		add(hofmullerPlots, "Hofmuller Plots");
		
		setWidth("240px");
	}
	public void setOperations(RPCServiceAsync rpcService, String view, String dsID, String varID, OperationsMenu menu) {
		this.opService = rpcService;
		this.menu = menu;
		opService.getOperations(view, dsID, varID, operationsCallback);	
	}
	AsyncCallback operationsCallback = new AsyncCallback() {
		public void onSuccess(Object result) {
			hasHofmullerPlots = false;
			hasLinePlots = false;
			hasSectionPlots = false;
			hasXYMap = false;
			ops = (OperationSerializable[]) result;
			for (int i = 0; i < ops.length; i++) {
				OperationSerializable op = ops[i];
				String category = op.getAttributes().get("category");
				if ( category.equals("visualization")) {
					List<String> views = op.getViews();
					for (Iterator viewIt = views.iterator(); viewIt.hasNext();) {
						String view = (String) viewIt.next();
						if ( !op.getName().contains("omparison") ) {
							Map<String, String> attrs = op.getAttributes();
							if ( attrs != null && attrs.containsKey("default")) {
								if ( view.equals("xy") ) {	
									if (!hasXYMap) {
										xyMap.clear();
										hasXYMap = true;
									}
									OperationButton button = new OperationButton("op", op.getName()+" in "+view);
									button.setOperation(op);
									button.addClickListener(buttonListener);
									button.setChecked(true);
									currentOp = button.getOperation();
									xyMap.add(button);
								} else if ( view.equals("x") || view.equals("y") || view.equals("z") || view.equals("t") ) {
									if ( !hasLinePlots ) {
										linePlots.clear();
										hasLinePlots = true;
									}
									OperationButton button = new OperationButton("op", op.getName()+" in "+view);
									button.setOperation(op);
									button.addClickListener(buttonListener);
									linePlots.add(button);
								} else if ( view.equals("xz") || view.equals("yz") ) {
									if ( !hasSectionPlots ) {
										sectionPlots.clear();
										hasSectionPlots = true;
									}
									OperationButton button = new OperationButton("op", op.getName()+" in "+view);
									button.setOperation(op);
									button.addClickListener(buttonListener);
									sectionPlots.add(button);
								} else if ( view.equals("xt") || view.equals("yt") || view.equals("zt") ) {
									if ( !hasHofmullerPlots ) {
										hofmullerPlots.clear();
										hasSectionPlots = true;
									}
									OperationButton button = new OperationButton("op", op.getName()+" in "+view);
									button.setOperation(op);
									button.addClickListener(buttonListener);
									hofmullerPlots.add(button);
								}
							}
						}
					}
				}
			}
			menu.setMenus(ops);
		}
        ClickListener buttonListener = new ClickListener() {

			public void onClick(Widget sender) {
				OperationButton button = (OperationButton) sender;
				currentOp = button.getOperation();
			}
        	
        };
		public void onFailure(Throwable caught) {
			Window.alert("Messed up with "+caught.getMessage());
		}
	};
	public OperationSerializable[] getOperationsSerializable() {
		return ops;
	}
	public OperationSerializable getCurrentOp() {
		return currentOp;
	}
}
