/**
 * 
 */
package gov.noaa.pmel.tmap.las.util;

/**
 * A simple utility class that holds the three parts of a constraint, the left
 * hand side, the operation and the right hand side.
 * @author Roland Schweitzer
 *
 */
public class Constraint {
    String lhs;
    String rhs;
    String op;
    public Constraint() {
        this.lhs = "";
        this.rhs = "";
        this.op = "";
    }
    public Constraint(String lhs, String op, String rhs) {
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
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
    public String getOpAsSymbol() {
        String opString = "";
        if ( op.equals("lt")) {
            opString = "<";
        } else if ( op.equals("le")) {
            opString = "<=";
        } else if (op.equals("eq")) {
            opString = "=";
        } else if (op.equals("ne") ) {
            opString = "!=";
        } else if (op.equals("gt")) {
            opString = ">";
        } else if (op.equals("ge")) {
            opString = ">=";
        }
        return opString;
    }
    /**
     * 
     * @return constraint -- the SQL ready string to go in the WHERE clause
     */
    public String getAsString() {
        String constraintString = "";

        try {
            Float.valueOf(rhs).floatValue();
            constraintString = constraintString + lhs+getOpAsSymbol()+rhs;
        } catch (NumberFormatException e) {
            constraintString = constraintString + lhs+getOpAsSymbol()+"\""+rhs+"\"";
        }
        return constraintString;
    }
}
