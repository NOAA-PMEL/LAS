package gov.noaa.pmel.tmap.las.client.laswidget;


public class TextConstraintAnchor extends ConstraintAnchor {
   
    public TextConstraintAnchor(String con) {
        super(con);
    }

    public TextConstraintAnchor(String type, String dsid, String varid, String variable, String value, String key, String keyValue, String op) {
        super(type, dsid, varid, variable, value, key, keyValue, op);
    }

    @Override
    public boolean equals(Object object) {
        if ( object instanceof TextConstraintAnchor ) {
            TextConstraintAnchor a = (TextConstraintAnchor) object;
            return a.getKey().equals(key) && a.getKeyValue().equals(keyValue) && a.getOp().equals(op);
        } else {
            return false;
        }
    }
}
