package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.AddVariableConstraintEvent;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ERDDAPVariableConstraintPanel extends Composite {

    ScrollPanel scrollPanel = new ScrollPanel();
    FlowPanel mainPanel = new FlowPanel();
    FlowPanel menuPanel = new FlowPanel();
    FlowPanel constraintPanel = new FlowPanel();
    VariableListBox variablesListBox = new VariableListBox(false);
    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();
    ERDDAPVariableConstraint vc;
    public ERDDAPVariableConstraintPanel() {
        variablesListBox.setVisibleItemCount(3);
        variablesListBox.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                int index = variablesListBox.getSelectedIndex();
                VariableSerializable v = variablesListBox.getVariable(index);
                vc = new ERDDAPVariableConstraint(v);
                constraintPanel.clear();
                constraintPanel.add(vc);
                eventBus.fireEventFromSource(new AddVariableConstraintEvent(v.getDSID(), v.getID(), "", "gt", v.getName(), "", "le", false), ERDDAPVariableConstraintPanel.this);
            }
            
        });
        menuPanel.add(variablesListBox);
        mainPanel.add(menuPanel);
        mainPanel.add(constraintPanel);
        scrollPanel.add(mainPanel);
        initWidget(scrollPanel);
    }
    public void setVariables(List<VariableSerializable> variables) {
        Vector vs = new Vector();
        for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
            VariableSerializable variableSerializable = (VariableSerializable) varIt.next();
            if ( variableSerializable.getAttributes().get("units") == null || !variableSerializable.getAttributes().get("units").equals("text") ) {
                variablesListBox.addItem(variableSerializable);          
            }
        }
    }
    public void setVariables(VariableSerializable[] variables) {
        variablesListBox.clear();
        Vector vs = new Vector();
        for (int i = 0; i < variables.length; i++) {
            if ( variables[i].getAttributes().get("units") == null || !variables[i].getAttributes().get("units").equals("text") ) {
                variablesListBox.addItem(variables[i]);
            }
        }
    }
    public void clearTextField(ConstraintTextAnchor anchor) {
        for(int i = 0; i < constraintPanel.getWidgetCount(); i++) {
            Widget object = constraintPanel.getWidget(i);
            if ( object instanceof ERDDAPVariableConstraint ) {
                ERDDAPVariableConstraint vc = (ERDDAPVariableConstraint) object;
                if ( vc.getVariable().getID().equals(anchor.getVarid()) && vc.getVariable().getDSID().equals(anchor.getDsid())) {
                    if ( anchor.getOp().equals("gt") ) {
                        vc.clearLhs();
                    } else if ( anchor.getOp().equals("le")) {
                        vc.clearRhs();
                    }
                }
            }
        }
    }
    public void setLhs(String text) {
        vc.setLhs(text);
    }
    public void setRhs(String text) {
        vc.setRhs(text);
    }
}
