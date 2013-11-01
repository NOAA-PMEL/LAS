package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.AddSelectionConstraintEvent;
import gov.noaa.pmel.tmap.las.client.serializable.ERDDAPConstraint;
import gov.noaa.pmel.tmap.las.client.serializable.ERDDAPConstraintGroup;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SetSelectionModel;

public class SelectionConstraintPanel extends Composite {
    
    
    VerticalPanel mainPanel  = new VerticalPanel();

    // Selection variables...
    VerticalPanel variablesRadioGroup = new VerticalPanel();
    // Current selection values
    ListBox valuesList = new ListBox();
    
    // The data set for this group.
    String dsid;
    
    // The Key that drives all the selections for this group.
    String key;
    
    // The constraint and the variables it needs
    ERDDAPConstraint constraint;
    
    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();
    
    // The name of the currently selected variable from the radio buttons group.
    String currentVariable;
    
    Map<String, String> currentValues;
    
    TextBox filter = new TextBox();
    Label filterLabel = new Label("Filter: ");
    
    HorizontalPanel filterPanel = new HorizontalPanel();


    protected AsyncCallback<Map<String, String>> outerSequenceValuesCallback = new AsyncCallback<Map<String,String>>() {

        @Override
        public void onFailure(Throwable caught) {
            Window.alert("Unable to get variable values from the server.");
        }

        @Override
        public void onSuccess(Map<String, String> result) {
            currentValues = result;
            valuesList.clear();
            valuesList.setVisibleItemCount(Math.min(result.keySet().size(), 10));
            for (Iterator rIt = result.keySet().iterator(); rIt.hasNext();) {
                String key_value = (String) rIt.next();
                String value = (String) result.get(key_value);
                valuesList.addItem(value, key_value);
            }
            
        }
    };
    
    
    public SelectionConstraintPanel() {
        valuesList.setVisibleItemCount(10);
        valuesList.addChangeHandler(new ChangeHandler(){

            @Override
            public void onChange(ChangeEvent event) {
                int index = valuesList.getSelectedIndex();
                String key_value = valuesList.getValue(index);
                String value = valuesList.getItemText(index);
                String variable = getShortName(currentVariable);
                eventBus.fireEventFromSource(new AddSelectionConstraintEvent(variable, value, key, key_value), SelectionConstraintPanel.this);
            }
        });
        
        filter.addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                String f = filter.getText();
                valuesList.clear();
                if ( !f.equals("") ) {
                    for (Iterator rIt = currentValues.keySet().iterator(); rIt.hasNext();) {
                        String key_value = (String) rIt.next();
                        String value = (String) currentValues.get(key_value);
                        if ( value.toLowerCase().startsWith(f.toLowerCase()) ) {
                            valuesList.addItem(value, key_value);
                        }
                    }
                } else {
                    for (Iterator rIt = currentValues.keySet().iterator(); rIt.hasNext();) {
                        String key_value = (String) rIt.next();
                        String value = (String) currentValues.get(key_value);
                        valuesList.addItem(value, key_value);
                    }
                }
                
            }
            
        });
        
        valuesList.addItem(Constants.PICK);
        valuesList.addItem(Constants.APPEAR);
        filterPanel.add(filterLabel);
        filterPanel.add(filter);
        mainPanel.add(filterPanel);
        mainPanel.add(variablesRadioGroup);
        valuesList.setWidth(Constants.CONTROLS_WIDTH-6+"px");
        valuesList.addStyleDependentName("PADDING");
        mainPanel.add(valuesList);
        initWidget(mainPanel);
    }
    public void init(ERDDAPConstraintGroup constraintGroup) {
        variablesRadioGroup.clear();
        constraint = constraintGroup.getConstraints().get(0);
        dsid = constraintGroup.getDsid();
        key = constraint.getKey();
        List<VariableSerializable> variables = constraint.getVariables();
        for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
            VariableSerializable variable = (VariableSerializable) varIt.next();
            final String id = variable.getID();
            String name = variable.getName();
            RadioButton button = new RadioButton("selection_variable", name);
            button.addStyleDependentName("PADDING");
            button.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    RadioButton button = (RadioButton) event.getSource();
                    if ( button.getValue() ) {
                        filter.setText("");
                        valuesList.clear();
                        valuesList.addItem(Constants.LOADING);
                        currentVariable = button.getText();
                        Util.getRPCService().getERDDAPOuterSequenceValues(dsid, id, key, new HashMap<String, String>(), outerSequenceValuesCallback);
                    }
                }});
            variablesRadioGroup.add(button);
        }
    }
    private String getShortName(String name) {
        List<VariableSerializable> variables = constraint.getVariables();
        for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
            VariableSerializable variableSerializable = (VariableSerializable) varIt.next();
            if ( variableSerializable.getName().equals(name) ) {
                return variableSerializable.getShortname();
            }
        }
        return null;
    }
    public void clearSelection() {
        int index = valuesList.getSelectedIndex();
        if ( index >= 0 ) {
            valuesList.setItemSelected(index, false);
        }
    }
}
