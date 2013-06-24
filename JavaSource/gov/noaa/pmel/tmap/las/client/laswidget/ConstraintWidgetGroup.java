package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.AddSelectionConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.AddVariableConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.CategoriesReturnedEvent;
import gov.noaa.pmel.tmap.las.client.event.ConfigReturnedEvent;
import gov.noaa.pmel.tmap.las.client.event.GetCategoriesEvent;
import gov.noaa.pmel.tmap.las.client.event.GetConfigEvent;
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
import com.google.gwt.user.client.ui.Widget;

public class ConstraintWidgetGroup extends Composite {

    boolean active = false;
    VerticalPanel mainPanel = new VerticalPanel();
    HorizontalPanel interiorPanel = new HorizontalPanel();
    ScrollPanel scrollPanel = new ScrollPanel();
    ConstraintTextDisplay displayPanel = new ConstraintTextDisplay();
    StackLayoutPanel constraintPanel = new StackLayoutPanel(Style.Unit.PX);
    HTML topLabel = new HTML("<strong>Select:</strong>");
    HTML constraintLabel = new HTML("<strong>My selections:</strong>");
    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();
    private static double STACK_HEIGHT = Constants.CONTROLS_WIDTH + 60;
    ERDDAPVariableConstraintPanel variableConstraints;
    ERDDAPValidDataConstraintPanel validConstraints;
    
    // Keep track of the dsid for this panel.
    String dsid;
    
    // Keep track of the stack panel index.
    int panel = 0;

    public ConstraintWidgetGroup() {
        constraintPanel.setSize(Constants.CONTROLS_WIDTH+"px", STACK_HEIGHT+"px");
        mainPanel.add(topLabel);
        interiorPanel.add(constraintPanel);
        mainPanel.add(interiorPanel);
        scrollPanel.setSize(Constants.CONTROLS_WIDTH-10+"px", "100px");
        scrollPanel.add(displayPanel);
        scrollPanel.addStyleName(Constants.ALLBORDER);
        displayPanel.setSize(Constants.CONTROLS_WIDTH-25+"px", "125px");
        mainPanel.add(constraintLabel);
        mainPanel.add(scrollPanel);

        variableConstraints = new ERDDAPVariableConstraintPanel();
        validConstraints = new ERDDAPValidDataConstraintPanel();
        
        constraintPanel.addSelectionHandler(new SelectionHandler<Integer>() {

            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                int panel = event.getSelectedItem();
                Widget opened = constraintPanel.getWidget(panel);
                if ( opened instanceof ERDDAPVariableConstraintPanel ) {
                    ERDDAPVariableConstraintPanel vcw = (ERDDAPVariableConstraintPanel) opened;
                    VariableSerializable variable = vcw.getVariable();
                    // Set the current values for the variable if it's already constrainted.
                    eventBus.fireEventFromSource(new AddVariableConstraintEvent(variable.getDSID(), variable.getID(), "", "gt", variable.getName(), "", "le", false), ConstraintWidgetGroup.this);
                }
                
            }
        });
        
        eventBus.addHandler(ConfigReturnedEvent.TYPE, new ConfigReturnedEvent.Handler() {
            
            @Override
            public void onConfigReturned(ConfigReturnedEvent event) {
                List<ERDDAPConstraintGroup> gs = event.getConfigSerializable().getConstraintGroups();
                CategorySerializable cat = event.getConfigSerializable().getCategorySerializable();
                VariableSerializable[] variables = cat.getDatasetSerializable().getVariablesSerializable();
                variableConstraints.setVariables(variables);
                validConstraints.setVariables(variables);               
                init(gs);
            }
        });
       
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
                ConstraintTextAnchor anchor1 = new ConstraintTextAnchor(Constants.VARIABLE_CONSTRAINT, dsid, varid, variable, lhs, variable, lhs, op1);
                ConstraintTextAnchor anchor2 = new ConstraintTextAnchor(Constants.VARIABLE_CONSTRAINT, dsid, varid, variable, rhs, variable, rhs, op2);
                ConstraintTextAnchor a = findMatchingAnchor(anchor1);
                ConstraintTextAnchor b = findMatchingAnchor(anchor2);
                if ( apply ) {
                    if ( op1.equals("ne") && op2.equals("ne") ) {
                        // Both are the same, only use the second here:
                        if ( rhs != null && !rhs.equals("") ) {
                            if ( b != null ) {
                                displayPanel.remove(b);
                            }
                            displayPanel.add(anchor2);
                        } else if ( rhs != null ) {
                            // It's the same as above...
                            if ( b != null ) {
                                displayPanel.remove(b);
                            }
                        }
                    } else {
                        if ( lhs != null && !lhs.equals("") ) {
                            if ( a != null ) {
                                displayPanel.remove(a);
                            }
                            displayPanel.add(anchor1);
                        } else if ( lhs != null ) {
                            // it's blank, applies been pressed, remove the anchor if it exists
                            if ( a != null ) {
                                displayPanel.remove(a);
                            }
                        }
                        if ( rhs != null && !rhs.equals("") ) {
                            if ( b != null ) {
                                displayPanel.remove(b);
                            }
                            displayPanel.add(anchor2);
                        } else if ( rhs != null ) {
                            // It's the same as above...
                            if ( b != null ) {
                                displayPanel.remove(b);
                            }
                        }
                    }
                } else {
                    // Apply button not pressed, newly created variable constraint, if there are active constraints, fill them in...
                    variableConstraints.clearTextFields();
                    if ( lhs != null && rhs != null && lhs.equals("") && rhs.equals("") ) {                        
                        if ( a != null ) {
                            // There is a matching active constraint for the lhs.
                            if (variable.equals(variableConstraints.getVariable().getName()) ) {
                                variableConstraints.setLhs(a.getKeyValue());
                            }
                        }
                        if ( b != null ) {
                            if (variable.equals(variableConstraints.getVariable().getName()) ) {
                                variableConstraints.setRhs(b.getKeyValue());
                            }
                        }
                    }
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
                Object source = event.getSource();
                if ( source instanceof ConstraintTextAnchor ) {
                    ConstraintTextAnchor anchor = (ConstraintTextAnchor) source;
                    displayPanel.remove(anchor);
                    if ( anchor.getType().equals(Constants.VARIABLE_CONSTRAINT) ) {
                        variableConstraints.clearTextField(anchor);
                    }
                } else if ( source instanceof SeasonConstraintPanel ) {
                    String variable = event.getVariable();
                    String value = event.getValue();
                    String key = event.getKey();
                    String keyValue = event.getKeyValue();

                    ConstraintTextAnchor anchor = new ConstraintTextAnchor("text", null, null, variable, value, key, keyValue, "eq");
                    for (int i = 0; i < displayPanel.getWidgetCount(); i++ ) {
                        ConstraintTextAnchor a = (ConstraintTextAnchor) displayPanel.getWidget(i);
                        if ( a.equals(anchor) ) {
                            displayPanel.remove(a);
                        }
                    }
                }
                eventBus.fireEvent(new WidgetSelectionChangeEvent(false, true, true));
            }

        });
        
        initWidget(mainPanel);
    }

    public void init(String catid, String dsid, String varid) {
        this.dsid = dsid;
        eventBus.fireEventFromSource(new GetConfigEvent(catid, dsid, varid), this); 
    }
    // Init after a call from somewhere's else.  Event?
    public void init(List<ERDDAPConstraintGroup> constraintGroups, VariableSerializable[] variables) {       
        variableConstraints.setVariables(variables);
        validConstraints.setVariables(variables);  
        init(constraintGroups);
    }
    public void init(List<ERDDAPConstraintGroup> constraintGroups) {
        panel = 0;
        if ( constraintPanel.getWidgetCount() > 0 ) constraintPanel.clear();
        for (Iterator iterator = constraintGroups.iterator(); iterator.hasNext();) {
            ERDDAPConstraintGroup erddapConstraintGroup = (ERDDAPConstraintGroup) iterator.next();
            if ( erddapConstraintGroup.getType().equals("selection") ) {
                SelectionConstraintPanel selectionConstraintPanel = new SelectionConstraintPanel();
                selectionConstraintPanel.init(erddapConstraintGroup);
                constraintPanel.add(selectionConstraintPanel, erddapConstraintGroup.getName(), 22);       
                constraintPanel.setHeaderHTML(panel, "<div style='font-size:.7em'>"+erddapConstraintGroup.getName()+"</div>");
                panel++;
            } else if ( erddapConstraintGroup.getType().equals("subset")) {
                SubsetConstraintPanel subsetConstraintPanel = new SubsetConstraintPanel();
                subsetConstraintPanel.init(erddapConstraintGroup);
                constraintPanel.add(subsetConstraintPanel, erddapConstraintGroup.getName(), 22);
                constraintPanel.setHeaderHTML(panel, "<div style='font-size:.7em'>"+erddapConstraintGroup.getName()+"</div>");
                panel++;
            } else if ( erddapConstraintGroup.getType().equals("season") ) {
                SeasonConstraintPanel seasonConstraintPanel = new SeasonConstraintPanel();
                seasonConstraintPanel.init(erddapConstraintGroup);
                constraintPanel.add(seasonConstraintPanel, erddapConstraintGroup.getName(), 22);
                constraintPanel.setHeaderHTML(panel, "<div style='font-size:.7em'>"+erddapConstraintGroup.getName()+"</div>");
                panel++;
            } else if ( erddapConstraintGroup.getType().equals(Constants.VARIABLE_CONSTRAINT) ) {
                constraintPanel.add(variableConstraints, erddapConstraintGroup.getName(), 22);
                constraintPanel.setHeaderHTML(panel, "<div style='font-size:.7em'>"+erddapConstraintGroup.getName()+"</div>");
                panel++;
            } else if ( erddapConstraintGroup.getType().equals("valid") ) {
                constraintPanel.add(validConstraints, erddapConstraintGroup.getName(), 22);
                constraintPanel.setHeaderHTML(panel, "<div style='font-size:.7em'>"+erddapConstraintGroup.getName()+"</div>");
                panel++;
            }
        }
 
        if ( constraintPanel.getWidgetCount() > 0 ) {
            constraintPanel.showWidget(0);
        }
        
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
        return displayPanel.getConstraints();
    }
    public ConstraintTextAnchor findMatchingAnchor(ConstraintTextAnchor anchor) {
        return displayPanel.findMatchingAnchor(anchor);
        
    }
    public boolean contains(ConstraintTextAnchor anchor) {
        return displayPanel.contains(anchor);
        
    }
    public void remove(ConstraintTextAnchor anchor) {
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
    public void addConstraint(ConstraintTextAnchor con) {
        displayPanel.add(con);
    }
    public int getConstraintPanelIndex() {
       return constraintPanel.getVisibleIndex();
    }

    public void clearConstraints() {
       displayPanel.clear();
        
    }

    public void setLhs(String a1) {
        variableConstraints.setLhs(a1);
    }

    public void setRhs(String value) {
       variableConstraints.setRhs(value);        
    }

    public void clearTextFields() {
        variableConstraints.clearTextFields();
        
    }

    public VariableSerializable getVariable() {
        return variableConstraints.getVariable();
    }
}
