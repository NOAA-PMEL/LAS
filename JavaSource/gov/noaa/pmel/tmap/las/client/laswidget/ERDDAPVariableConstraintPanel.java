package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;

public class ERDDAPVariableConstraintPanel extends Composite {

    ScrollPanel mainPanel = new ScrollPanel();
    FlowPanel menuPanel = new FlowPanel();
    public ERDDAPVariableConstraintPanel() {

        mainPanel.add(menuPanel);
        initWidget(mainPanel);
    }
    public void setVariables(List<VariableSerializable> variables) {
        for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
            VariableSerializable variableSerializable = (VariableSerializable) varIt.next();
            if ( variableSerializable.getAttributes().get("units") == null || !variableSerializable.getAttributes().get("units").equals("text") ) {
                ERDDAPVariableConstraint vc = new ERDDAPVariableConstraint(variableSerializable);   
                menuPanel.add(vc);
            }
        }
    }
    public void setVariables(VariableSerializable[] variables) {
        for (int i = 0; i < variables.length; i++) {
            if ( variables[i].getAttributes().get("units") == null || !variables[i].getAttributes().get("units").equals("text") ) {
                ERDDAPVariableConstraint vc = new ERDDAPVariableConstraint(variables[i]);
                menuPanel.add(vc);
            }
        }
    }
    public void clearTextField(ConstraintTextAnchor anchor) {
        String variable = anchor.getVariable();
        for(int i = 0; i < menuPanel.getWidgetCount(); i++) {
            ERDDAPVariableConstraint vc = (ERDDAPVariableConstraint) menuPanel.getWidget(i);
            if ( vc.getVariable().getID().equals(anchor.getVarid()) && vc.getVariable().getDSID().equals(anchor.getDsid())) {
                if ( anchor.getOp().equals("gt") ) {
                    vc.clearLhs();
                } else if ( anchor.getOp().equals("le")) {
                    vc.clearRhs();
                }
                // if the rhs and lhs are empty turn off the check.
                if ( vc.getRhs().equals("") && vc.getLhs().equals("") ) {
                    vc.setApply(false);
                }
            }
        }
        
    }
}
