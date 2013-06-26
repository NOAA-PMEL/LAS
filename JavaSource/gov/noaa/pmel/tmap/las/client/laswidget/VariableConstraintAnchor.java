package gov.noaa.pmel.tmap.las.client.laswidget;


public class VariableConstraintAnchor extends ConstraintAnchor {
   
    public VariableConstraintAnchor(String con) {
        super(con);
    }


    public VariableConstraintAnchor(String type, String dsid, String varid, String variable, String value, String key, String keyValue, String op) {
        super(type, dsid, varid, variable, value, key, keyValue, op);
    }
    

    @Override
    public boolean equals(Object object) {
        if ( object instanceof VariableConstraintAnchor ) {
            VariableConstraintAnchor a = (VariableConstraintAnchor) object;
            return a.getKey().equals(key) && a.getOp().equals(op);
        } else {
            return false;
        }
    }
}
