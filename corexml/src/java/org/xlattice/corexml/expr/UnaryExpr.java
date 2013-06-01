/* UnaryExpr.java */
package org.xlattice.corexml.expr;

/**
 * An XPath 1.0 unary expression.  This always represents an 
 * expression preceded by a minus sign.
 *
 * @author Jim Dixon
 */
public class UnaryExpr extends Expr {

    private final Operator op;          // always minus
    private Expr           right;

    /**
     * Create a unary expression.
     * @param o        the operator (always '-'), may not be null
     * @param rightSub the expression being negated, may not be null
     */
    public UnaryExpr (Operator o, Expr rightSub) {
        super(Operator.TAGS[o.getIndex()] );
        if (o == null || rightSub == null)
            throw new NullPointerException(
                            "null operator or right subexpression");
        op   = o;
        right = rightSub;

    }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return a reference to the operator */
    public Operator getOperator () {
        return op;
    }
    /** @return the index of the operator involved */
    public int getOperatorType() {
        return op.getIndex();
    }
    /** @return a reference to the XPath sub-expression */
    public Expr getSubexpr () {
        return right;
    }
    /** 
     * Assign a new sub-expression.
     * @param expr a reference to the new sub-expression (may not be null)
     */
    public void setSubexpr (Expr expr) {
        if (expr == null)
            throw new NullPointerException();
        right = expr;
    }
    
    // SERIALIZATION ////////////////////////////////////////////////
    protected String xmlBody(int indent) {
        StringBuffer sb = new StringBuffer();
        sb.append(right.toXml(indent + 2));
        return sb.toString();
    }
}
