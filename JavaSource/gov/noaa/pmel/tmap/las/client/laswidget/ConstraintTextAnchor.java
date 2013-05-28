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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;

public class ConstraintTextAnchor extends Composite {
    String variable;
    String value;
    String key;
    String keyValue;
    
    String type;
    String dsid;
    String varid;
    
    String op;
    
    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();
    
    Anchor a = new Anchor();
    FlowPanel p = new FlowPanel();
    
    public ConstraintTextAnchor(String type, String dsid, String varid, String variable, String value, String key, String keyValue, String op) {
        this.type = type;
        this.dsid = dsid;
        this.varid = varid;
        this.variable = variable;
        this.value = value;
        this.key = key;
        this.keyValue = keyValue;
        this.op = op;
        init();
    }

    private void init() {
        final String anchor_text;
        String opv = "";
        if ( op.equals("eq") ) {
            opv = "=";
        } else if ( op.equals("lt") ) {
            opv = "<";
        } else if ( op.equals("le") ) {
            opv = "<=";
        } else if ( op.equals("gt") ) {
            opv = ">";
        } else if ( op.equals("ge") ) {
            opv = ">=";
        } else if ( op.equals("ne") ) {
            opv = "!=";
        }
        if ( variable.equals(key) ) {
            anchor_text = "(x) "+variable+" "+opv+" "+value;
        } else {
            anchor_text = "(x) "+variable+" "+opv+" "+value + " with " + key + " "+opv+" " + keyValue;
        }
       
        a.setText(anchor_text);
        a.addStyleDependentName("CONSTRAINT");
        
        a.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEventFromSource(new RemoveSelectionConstraintEvent(ConstraintTextAnchor.this.variable, ConstraintTextAnchor.this.value, ConstraintTextAnchor.this.key, ConstraintTextAnchor.this.keyValue), ConstraintTextAnchor.this);              
            }
            
        });
        p.add(a);
        initWidget(p);
    }
    public ConstraintTextAnchor(String con) {
        /* 
         * This is a string that looks like variable_cr_op_value_cr_key_cr_keyValue
         * These are stuffed into this form so they can go on the token map for the history.
         * This code unpacks the string and fixed up the thing.
         */
        String[] parts = con.split("_cr_");
        this.variable = parts[0];
        this.value = parts[1];
        this.op = parts[2];
        this.key = parts[3];
        this.keyValue = parts[4];
        init();
    }


    public String getVariable() {
        return variable;
    }


    public void setVariable(String variable) {
        this.variable = variable;
    }


    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
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
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDsid() {
        return dsid;
    }

    public void setDsid(String dsid) {
        this.dsid = dsid;
    }

    public String getVarid() {
        return varid;
    }

    public void setVarid(String varid) {
        this.varid = varid;
    }

    @Override
    public boolean equals(Object object) {
        if ( object instanceof ConstraintTextAnchor ) {
            ConstraintTextAnchor a = (ConstraintTextAnchor) object;
            return a.getKey().equals(key) && a.getKeyValue().equals(keyValue) && a.getOp().equals(op);
        } else {
            return false;
        }
    }
}
