package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.AddSelectionConstraintEvent;
import gov.noaa.pmel.tmap.las.client.serializable.ERDDAPConstraint;
import gov.noaa.pmel.tmap.las.client.serializable.ERDDAPConstraintGroup;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SubsetConstraintPanel extends Composite {
    
    // Current selection values
    ListBox valuesList = new ListBox();

    VerticalPanel mainPanel  = new VerticalPanel();
    HorizontalPanel rightPanel = new HorizontalPanel();
    
    VerticalPanel radioButtonGroup = new VerticalPanel();  

    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();
    
    String currentVariable;
    ERDDAPConstraintGroup constraintGroup;

    String dsid;
    public SubsetConstraintPanel(ERDDAPConstraintGroup constraintGroup) {
        
        this.constraintGroup = constraintGroup;
        valuesList.setVisibleItemCount(10);
        valuesList.setWidth(Constants.CONTROLS_WIDTH-6+"px");
        valuesList.addChangeHandler(new ChangeHandler(){

            @Override
            public void onChange(ChangeEvent event) {
                int index = valuesList.getSelectedIndex();
                String key_value = valuesList.getValue(index);
                String value = valuesList.getItemText(index);
                String shortName = getShortName(currentVariable);
                //  the key and the variable are always the same in this case.
                eventBus.fireEventFromSource(new AddSelectionConstraintEvent(shortName, value, shortName, key_value), SubsetConstraintPanel.this);
            }
        });
        rightPanel.add(valuesList);
        valuesList.addItem(Constants.PICK);
        valuesList.addItem(Constants.APPEAR);
        mainPanel.add(radioButtonGroup);
        mainPanel.add(rightPanel);
        mainPanel.setHeight("100px");
        initWidget(mainPanel);
        for (Iterator conIt = constraintGroup.getConstraints().iterator(); conIt.hasNext();) {
            ERDDAPConstraint constraint = (ERDDAPConstraint) conIt.next();
            dsid = constraintGroup.getDsid();
            final String key = constraint.getKey();
            // There should be only 1.
            VariableSerializable variable = constraint.getVariables().get(0);
           
            final String id = variable.getID();
            
            String type = constraint.getWidget();
            if (type.equals("list")) {

                RadioButton radio = new RadioButton("constraint_type", variable.getName());
                radioButtonGroup.add(radio);
                radio.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        RadioButton button = (RadioButton) event.getSource();
                        valuesList.clear();
                        valuesList.addItem(Constants.LOADING);
                        currentVariable = button.getText();
                        Util.getRPCService().getERDDAPOuterSequenceValues(dsid, id, key, new HashMap<String, String>(), outerSequenceValuesCallback);
                    }

                });
            }
        }
    }
    protected AsyncCallback<Map<String, String>> outerSequenceValuesCallback = new AsyncCallback<Map<String,String>>() {

        @Override
        public void onFailure(Throwable caught) {
            Window.alert("Unable to get variable values from the server.");
        }

        @Override
        public void onSuccess(Map<String, String> result) {
            valuesList.clear();
            valuesList.setVisibleItemCount(Math.min(result.keySet().size(), 10));
            for (Iterator rIt = result.keySet().iterator(); rIt.hasNext();) {
                String key_value = (String) rIt.next();
                String value = (String) result.get(key_value);
                valuesList.addItem(value, key_value);
            }
            
        }
    };
    private String getShortName(String variable) {
        for (Iterator conIt = constraintGroup.getConstraints().iterator(); conIt.hasNext();) {
            ERDDAPConstraint constraint = (ERDDAPConstraint) conIt.next();
            VariableSerializable vs = constraint.getVariables().get(0);
            if (vs.getName().equals(variable)) {
                return vs.getShortname();
            }
        }
        return null;
    }
}
