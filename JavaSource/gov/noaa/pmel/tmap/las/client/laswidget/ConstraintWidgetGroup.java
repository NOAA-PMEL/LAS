package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.AddSelectionConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.AddVariableConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.GridChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.MapChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.RemoveSelectionConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.WidgetSelectionChangeEvent;
import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConstraintSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ERDDAPConstraintGroup;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
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
    Label topLabel = new Label("Constrain the data displayed on the plot.");
    Label constraintLabel = new Label("My selections:");
    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();
    
    ERDDAPVariableConstraintPanel variableConstraints;
    SelectionConstraintPanel selectionConstraintPanel;
    SubsetConstraintPanel subsetConstraintPanel;
    
    // Keep track of the dsid for this panel.
    String dsid;

    public ConstraintWidgetGroup() {
        constraintPanel.setSize(Constants.CONTROLS_WIDTH+"px", Constants.CONTROLS_WIDTH+"px");
        mainPanel.add(topLabel);
        interiorPanel.add(constraintPanel);
        mainPanel.add(interiorPanel);
        scrollPanel.addStyleName("allBorder");
        scrollPanel.setSize(Constants.CONTROLS_WIDTH+"px", "152px");
        scrollPanel.add(displayPanel);
        displayPanel.setSize(Constants.CONTROLS_WIDTH-20+"px", "150px");
        mainPanel.add(constraintLabel);
        mainPanel.add(scrollPanel);
        initWidget(mainPanel);
    }

    public void init(String dsid) {
        this.dsid = dsid;
        Util.getRPCService().getERDDAPConstraintGroups(dsid, initConstraintsCallback);
    }
    protected AsyncCallback<List<ERDDAPConstraintGroup>> initConstraintsCallback = new AsyncCallback<List<ERDDAPConstraintGroup>>() {

        @Override
        public void onFailure(Throwable caught) {
            Window.alert("Unable to initialize the constraints panel.");
        }

        @Override
        public void onSuccess(List<ERDDAPConstraintGroup> constraintGroups) {
            init(constraintGroups);
            Util.getRPCService().getCategories(dsid, dsid, categoryCallback);
        }
    };
    protected AsyncCallback<CategorySerializable[]> categoryCallback = new AsyncCallback<CategorySerializable[]>() {

        @Override
        public void onFailure(Throwable caught) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onSuccess(CategorySerializable[] cats) {
            variableConstraints = new ERDDAPVariableConstraintPanel();
            constraintPanel.add(variableConstraints, "Select a Sub-set by Variable Value", 30);
            for (int i = 0; i < cats.length; i++) {
                if ( cats[i].isVariableChildren() ) {
                    VariableSerializable[] variables = cats[i].getDatasetSerializable().getVariablesSerializable();
                    variableConstraints.setVariables(variables);
                }
            }
        }
        
    };
    public void init(List<ERDDAPConstraintGroup> constraintGroups) {

        constraintPanel.clear();

        for (Iterator iterator = constraintGroups.iterator(); iterator.hasNext();) {
            ERDDAPConstraintGroup erddapConstraintGroup = (ERDDAPConstraintGroup) iterator.next();
            if ( erddapConstraintGroup.getType().equals("selection") ) {
                selectionConstraintPanel = new SelectionConstraintPanel(erddapConstraintGroup);
                constraintPanel.add(selectionConstraintPanel, erddapConstraintGroup.getName(), 30);
            } else {
                subsetConstraintPanel = new SubsetConstraintPanel(erddapConstraintGroup);
                constraintPanel.add(subsetConstraintPanel, erddapConstraintGroup.getName(), 30);
            }
        }
 
        constraintPanel.showWidget(0);
        eventBus.addHandler(AddVariableConstraintEvent.TYPE, new AddVariableConstraintEvent.Handler() {
            
            @Override
            public void onAdd(AddVariableConstraintEvent event) {
                String variable = event.getVariable();
                String op1 = event.getOp1();
                String op2 = event.getOp2();
                String lhs = event.getLhs();
                String rhs = event.getRhs();
                String varid = event.getVarid();
                String dsid = event.getDsid();
                boolean apply = event.isApply();
                ConstraintTextAnchor anchor1 = new ConstraintTextAnchor("variable", dsid, varid, variable, lhs, variable, lhs, op1);
                ConstraintTextAnchor anchor2 = new ConstraintTextAnchor("variable", dsid, varid, variable, rhs, variable, rhs, op2);
                if ( apply ) {
                    if ( lhs != null && !lhs.equals("") ) {

                        if ( !contains(anchor1) ) {
                            displayPanel.add(anchor1);
                        }

                    }
                    if ( rhs != null && !rhs.equals("") ) {

                        if ( !contains(anchor2) ) {
                            displayPanel.add(anchor2);
                        }
                    }
                } else {
                    // remove them regardless of whether or not they are defined.
                    remove(anchor1);
                    remove(anchor2);
                }
            }
        });
        eventBus.addHandler(AddSelectionConstraintEvent.TYPE, new AddSelectionConstraintEvent.Handler() {

            @Override
            public void onAdd(AddSelectionConstraintEvent event) {

                String variable = event.getVariable();
                String value = event.getValue();
                String key = event.getKey();
                String keyValue = event.getKeyValue();

                ConstraintTextAnchor anchor = new ConstraintTextAnchor("text", null, null, variable, value, key, keyValue, "eq");
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
                if ( anchor.getType().equals("variable") ) {
                    variableConstraints.clearTextField(anchor);
                }
                eventBus.fireEvent(new WidgetSelectionChangeEvent(false, true, true));
            }

        });
    }
//    public void init(List<ERDDAPConstraintGroup> constraintGroups, List<VariableSerializable> variables) {
//        VariableConstraintLayout variableConstraints = new VariableConstraintLayout("", true);
//        init(constraintGroups);
//        constraintPanel.add(variableConstraints, "Select Data by Variable Value", 30);
//        for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
//            VariableSerializable variableSerializable = (VariableSerializable) varIt.next();
//            variableConstraints.addItem(variableSerializable);
//        }
//    }
    public List<ConstraintSerializable> getConstraints() {
        List<ConstraintSerializable> constraints = new ArrayList<ConstraintSerializable>();
        Map<String, ConstraintSerializable> cons = new HashMap<String, ConstraintSerializable>();
        for (int i = 0; i < displayPanel.getWidgetCount(); i++) {
            ConstraintTextAnchor anchor = (ConstraintTextAnchor) displayPanel.getWidget(i);
            if ( anchor.getType().equals("text") ) {
                String key = anchor.getKey();
                String op = anchor.getOp();
                String value = anchor.getKeyValue();
                ConstraintSerializable keyConstraint = cons.get(key);
                if ( keyConstraint == null ) {
                    keyConstraint = new ConstraintSerializable("text", null, null, key, op, "\""+value+"\"", key+"_"+value);
                    cons.put(key, keyConstraint);
                } else {
                    String v = keyConstraint.getRhs();
                    v = v.substring(0, v.length()-1);
                    v = v + "|" + value+"\"";
                    keyConstraint.setRhs(v);
                    keyConstraint.setOp("like");
                }
            } else {
                String dsid = anchor.getDsid();
                String varid = anchor.getVarid();
                String op = anchor.getOp();
                String lhs = anchor.getValue();
                ConstraintSerializable con = new ConstraintSerializable("variable", dsid, varid, varid, op, lhs, dsid+"_"+varid);
                constraints.add(con);
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
    private void remove(ConstraintTextAnchor anchor) {
        ConstraintTextAnchor remove = null;
        for (int i = 0; i < displayPanel.getWidgetCount(); i++) {
            ConstraintTextAnchor a = (ConstraintTextAnchor) displayPanel.getWidget(i);
            if ( anchor.equals(a) ) {
                remove = a;
            }
        }
        if ( remove != null ) {
            displayPanel.remove(remove);
        }
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public boolean isActive() {
        return active;
    }

    public void setSelectedPanelIndex(int panelIndex) {
        constraintPanel.showWidget(panelIndex);
    }
    
    public List<ConstraintTextAnchor> getAnchors() {
        List<ConstraintTextAnchor> anchors = new ArrayList<ConstraintTextAnchor>();
        for (int i=0; i < displayPanel.getWidgetCount(); i++ ) {
            ConstraintTextAnchor cta = (ConstraintTextAnchor) displayPanel.getWidget(i);
            anchors.add(cta);
        }
        return anchors;
    }

    public void setConstraints(List<ConstraintTextAnchor> cons) {
        for (Iterator conIt = cons.iterator(); conIt.hasNext();) {
            ConstraintTextAnchor cta = (ConstraintTextAnchor) conIt.next();
            displayPanel.add(cta);
        }
    }

    public int getConstraintPanelIndex() {
       return constraintPanel.getVisibleIndex();
    }

    public void clearConstraints() {
       displayPanel.clear();
        
    }
}
