package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.RemoveSelectionConstraintEvent;
import gov.noaa.pmel.tmap.las.client.serializable.ConstraintSerializable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.shared.DirectionEstimator;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Anchor;

public class ConstraintTextAnchor extends Anchor {
    String variable;
    String value;
    String key;
    String keyValue;
    
    String op;
    
    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();
    
    
    public ConstraintTextAnchor(String variable, String value, String key, String keyValue, String op) {
        super();
        this.variable = variable;
        this.value = value;
        this.key = key;
        this.keyValue = keyValue;
        this.op = op;
        final String anchor_text;
        if ( variable.equals(key) ) {
            anchor_text = "(x) "+variable+" = "+value;
        } else {
            anchor_text = "(x) "+variable+" = "+value + " with " + key + " = " + keyValue;
        }
        setText(anchor_text);
        addStyleDependentName("PADDING");
        addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEventFromSource(new RemoveSelectionConstraintEvent(ConstraintTextAnchor.this.variable, ConstraintTextAnchor.this.value, ConstraintTextAnchor.this.key, ConstraintTextAnchor.this.keyValue), ConstraintTextAnchor.this);              
            }
            
        });
    }


    public String getKey() {
        return key;
    }


    public void setKey(String key) {
        this.key = key;
    }


    public String getKeyValue() {
        return keyValue;
    }


    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }


    public String getOp() {
        return op;
    }


    public void setOp(String op) {
        this.op = op;
    }
    @Override
    public boolean equals(Object object) {
        if ( object instanceof ConstraintTextAnchor ) {
            ConstraintTextAnchor a = (ConstraintTextAnchor) object;
            return a.getKey().equals(key) && a.getKeyValue().equals(keyValue);
        } else {
            return false;
        }
    }
}
