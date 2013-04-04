package gov.noaa.pmel.tmap.las.client.serializable;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ConstraintSerializable implements IsSerializable {
    String lhs;
    String op;
    String rhs;
    String id;
    public ConstraintSerializable() {
        super();
    }
    public ConstraintSerializable(String lhs, String op, String rhs, String id) {
        super();
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getLhs() {
        return lhs;
    }
    public void setLhs(String lhs) {
        this.lhs = lhs;
    }
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
    public String getRhs() {
        return rhs;
    }
    public void setRhs(String rhs) {
        this.rhs = rhs;
    }
    
}
