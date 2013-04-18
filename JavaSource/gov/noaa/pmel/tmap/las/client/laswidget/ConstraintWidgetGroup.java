package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.AddSelectionConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.GridChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.MapChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.RemoveSelectionConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.WidgetSelectionChangeEvent;
import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.ConstraintSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ERDDAPConstraintGroup;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ConstraintWidgetGroup extends Composite {

    boolean active = false;
    VerticalPanel mainPanel = new VerticalPanel();
    HorizontalPanel interiorPanel = new HorizontalPanel();
    ScrollPanel scrollPanel = new ScrollPanel();
    FlowPanel displayPanel = new FlowPanel();
    StackLayoutPanel constraintPanel = new StackLayoutPanel(Style.Unit.PX);
    

    OLMapWidget refMap = new OLMapWidget();
    DateTimeWidget dateTimeWidget = new DateTimeWidget();


    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();

    // The selection state
    List<ConstraintTextAnchor> selectionState = new ArrayList<ConstraintTextAnchor>();
    
    // The subset state
    List<ConstraintTextAnchor> subsetState = new ArrayList<ConstraintTextAnchor>();

    public ConstraintWidgetGroup() {
        mainPanel.add(new Label("Constrain the data displayed on the plot.  Only the constraints from the active tab will be applied."));
        constraintPanel.addSelectionHandler(new SelectionHandler<Integer>() {

            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                int tab = event.getSelectedItem();
                if ( tab == 0 ) {
                    // Save the selection state...
                    selectionState.clear();
                    for (int i = 0; i < displayPanel.getWidgetCount(); i++) {
                        selectionState.add((ConstraintTextAnchor) displayPanel.getWidget(i));
                    }
                    displayPanel.clear();
                    // Restore the subset state
                    for (Iterator subsetIt = subsetState.iterator(); subsetIt.hasNext();) {
                        ConstraintTextAnchor anchor = (ConstraintTextAnchor) subsetIt.next();
                        displayPanel.add(anchor);
                    }
                } else {
                    // Do the opposite
                    subsetState.clear();
                    for (int i = 0; i < displayPanel.getWidgetCount(); i++) {
                        subsetState.add((ConstraintTextAnchor) displayPanel.getWidget(i));
                    }
                    displayPanel.clear();
                    // Restore the Selection state
                    for (Iterator subsetIt = selectionState.iterator(); subsetIt.hasNext();) {
                        ConstraintTextAnchor anchor = (ConstraintTextAnchor) subsetIt.next();
                        displayPanel.add(anchor);
                    }
                }
                
            }
            
        });
        scrollPanel.setSize(Constants.CONTROLS_WIDTH+"px", "152px");
        scrollPanel.add(displayPanel);
        displayPanel.addStyleDependentName("BORDER-TOP");
        displayPanel.setSize(Constants.CONTROLS_WIDTH+"px", "150px");
        mainPanel.add(scrollPanel);
        constraintPanel.setSize(Constants.CONTROLS_WIDTH+"px", Constants.CONTROLS_WIDTH+"px");
        interiorPanel.add(constraintPanel);
        interiorPanel.add(refMap);
        interiorPanel.add(dateTimeWidget);
        mainPanel.add(interiorPanel);
        eventBus.addHandler(MapChangeEvent.TYPE, new MapChangeEvent.Handler() {

            @Override
            public void onMapSelectionChange(MapChangeEvent event) {

                refMap.setCurrentSelection(event.getYlo(), event.getYhi(), event.getXlo(), event.getXhi());

            }
        });
        eventBus.addHandler(GridChangeEvent.TYPE, new GridChangeEvent.Handler() {

            @Override
            public void onGridChange(GridChangeEvent event) {
                GridSerializable grid = event.getGrid();
                if ( grid.hasT() ) {
                    dateTimeWidget.init(grid.getTAxis(), true);
                }
                if ( grid.hasX() && grid.hasY() ) {
                    String xlo = grid.getXAxis().getLo();
                    String xhi = grid.getXAxis().getHi();
                    String ylo = grid.getYAxis().getLo();
                    String yhi = grid.getYAxis().getHi();
                    refMap.setDataExtent(Double.valueOf(ylo), Double.valueOf(yhi), Double.valueOf(xlo), Double.valueOf(xhi));
                    refMap.setTool("xy");
                }
            }
        });
        initWidget(mainPanel);
    }

    public void init(String dsid) {
        Util.getRPCService().getERDDAPConstraintGroups(dsid, initConstraintsCallback);
    }
    protected AsyncCallback<List<ERDDAPConstraintGroup>> initConstraintsCallback = new AsyncCallback<List<ERDDAPConstraintGroup>>() {

        @Override
        public void onFailure(Throwable caught) {
           Window.alert("Unable to initialize the constraints panel.");
        }

        @Override
        public void onSuccess(List<ERDDAPConstraintGroup> constraintGroups) {
            init(constraintGroups, false, false);
        }
    };
    public void init(List<ERDDAPConstraintGroup> constraintGroups, boolean map, boolean dateTime) {
        
        if ( !map ) {
            interiorPanel.remove(refMap);
        }
        if ( !dateTime ) {
            interiorPanel.remove(dateTimeWidget);
        }
        
        constraintPanel.clear();

        for (Iterator iterator = constraintGroups.iterator(); iterator.hasNext();) {
            ERDDAPConstraintGroup erddapConstraintGroup = (ERDDAPConstraintGroup) iterator.next();
            if ( erddapConstraintGroup.getType().equals("selection") ) {
                constraintPanel.add(new SelectionConstraintPanel(erddapConstraintGroup), erddapConstraintGroup.getName(), 30);
            } else {
                constraintPanel.add(new SubsetConstraintPanel(erddapConstraintGroup), erddapConstraintGroup.getName(), 30);
            }
        }
        constraintPanel.showWidget(0);
        eventBus.addHandler(AddSelectionConstraintEvent.TYPE, new AddSelectionConstraintEvent.Handler() {

            @Override
            public void onAdd(AddSelectionConstraintEvent event) {

                String variable = event.getVariable();
                String value = event.getValue();
                String key = event.getKey();
                String keyValue = event.getKeyValue();

                ConstraintTextAnchor anchor = new ConstraintTextAnchor(variable, value, key, keyValue, "eq");
                if ( !contains(anchor) ) {
                    displayPanel.add(anchor);
                }
                eventBus.fireEvent(new WidgetSelectionChangeEvent(false, true, true));

            }


        });
        // Event handlers for date time and lat/lon (map) constraint events.
        eventBus.addHandler(RemoveSelectionConstraintEvent.TYPE, new RemoveSelectionConstraintEvent.Handler() {

            @Override
            public void onRemove(RemoveSelectionConstraintEvent event) {
                ConstraintTextAnchor anchor = (ConstraintTextAnchor) event.getSource();
                displayPanel.remove(anchor);
                eventBus.fireEvent(new WidgetSelectionChangeEvent(false, true, true));
            }

        });
    }
    public List<ConstraintSerializable> getConstraints() {
        List<ConstraintSerializable> constraints = new ArrayList<ConstraintSerializable>();
        Map<String, ConstraintSerializable> cons = new HashMap<String, ConstraintSerializable>();
        for (int i = 0; i < displayPanel.getWidgetCount(); i++) {
            ConstraintTextAnchor anchor = (ConstraintTextAnchor) displayPanel.getWidget(i);
            String key = anchor.getKey();
            String op = anchor.getOp();
            String value = anchor.getKeyValue();
            ConstraintSerializable keyConstraint = cons.get(key);
            if ( keyConstraint == null ) {
                keyConstraint = new ConstraintSerializable(key, op, "\""+value+"\"", key+"_"+value);
                cons.put(key, keyConstraint);
            } else {
                String v = keyConstraint.getRhs();
                v = v.substring(0, v.length()-1);
                v = v + "|" + value+"\"";
                keyConstraint.setRhs(v);
                keyConstraint.setOp("like");
            }
        }
        for (Iterator keysIt = cons.keySet().iterator(); keysIt.hasNext();) {
            String key = (String) keysIt.next();
            constraints.add(cons.get(key));
        }
        return constraints;
    }
    private boolean contains(ConstraintTextAnchor anchor) {
        for (int i = 0; i < displayPanel.getWidgetCount(); i++) {
            ConstraintTextAnchor a = (ConstraintTextAnchor) displayPanel.getWidget(i);
            if ( anchor.equals(a) ) {
                return true;
            }
        }
        return false;
    }

    public String getXlo() {
        return String.valueOf(refMap.getXlo());
    }
    public String getXhi() {
        return String.valueOf(refMap.getXhi());
    }
    public String getYlo() {
        return String.valueOf(refMap.getYlo());
    }
    public String getYhi() {
        return String.valueOf(refMap.getYhi());
    }
    public String getTlo() {
        return dateTimeWidget.getFerretDateLo();
    }
    public String getThi() {
        return dateTimeWidget.getFerretDateHi();
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public boolean isActive() {
        return active;
    }
}
