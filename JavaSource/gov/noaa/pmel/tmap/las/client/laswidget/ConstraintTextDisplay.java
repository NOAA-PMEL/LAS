package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.ConstraintSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.FlowPanel;

public class ConstraintTextDisplay extends FlowPanel {
    public List<ConstraintSerializable> getConstraints() {
        List<ConstraintSerializable> constraints = new ArrayList<ConstraintSerializable>();
        Map<String, ConstraintSerializable> cons = new HashMap<String, ConstraintSerializable>();
        for (int i = 0; i < this.getWidgetCount(); i++) {
            ConstraintTextAnchor anchor = (ConstraintTextAnchor) this.getWidget(i);
            if ( anchor.getType().equals("text") ) {
                String key = anchor.getKey();
                String op = anchor.getOp();
                String value = anchor.getKeyValue();
                ConstraintSerializable keyConstraint = cons.get(key);
                if ( keyConstraint == null ) {
                    keyConstraint = new ConstraintSerializable("text", null, null, key, op, value, key+"_"+value);
                    cons.put(key, keyConstraint);
                } else {
                    String v = keyConstraint.getRhs();
                    
                    v = v + "," + value;
                    keyConstraint.setRhs(v);
                    keyConstraint.setOp("is");
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

    public ConstraintTextAnchor findMatchingAnchor(ConstraintTextAnchor anchor) {
        for (int i = 0; i < this.getWidgetCount(); i++) {
            ConstraintTextAnchor a = (ConstraintTextAnchor) this.getWidget(i);
            if ( a.getKey().equals(anchor.getKey()) && a.getOp().equals(anchor.getOp()) ) {
                return a;
            }
        }
        return null;
    }

    public boolean contains(ConstraintTextAnchor anchor) {
        for (int i = 0; i < this.getWidgetCount(); i++) {
            ConstraintTextAnchor a = (ConstraintTextAnchor) this.getWidget(i);
            if ( anchor.equals(a) ) {
                return true;
            }
        }
        return false;
    }
}
