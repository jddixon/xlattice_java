/* BinaryExpr.java */
package org.xlattice.corexml.expr;

/**
 * Class repesenting an XPath 1.0 binary expression.
 *
 * @author Jim Dixon
 */
public class BinaryExpr extends Expr {

    /** binary operator */
    private final Operator op;
    /** left sub-expression */
    private Expr  left;     // XXX can we make this final??
    /** right sub-expression */
    private Expr  right;

    /** 
     * Create a binary (two-operand) expression.
     * @param o        operator
     * @param leftSub  left sub-expression, an XPath expression 
     * @param rightSub right sub-expression, an XPath expression 
     */
    public BinaryExpr (Operator o, Expr leftSub, Expr rightSub) {
        super(Operator.TAGS[o.getIndex()]);
        if (o == null || leftSub == null)
            throw new NullPointerException(
                            "null operator or left subexpression");
        op    = o;
        left  = leftSub;
        right = rightSub;
    }

    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the expression's binary operator */
    public Operator getOperator () {
        return op;
    }
    /** @return its type, an index into a table */
    public int getOperatorType() {
        return op.getIndex();
    }
    /** @return the left sub-expression */
    public Expr getLeftSubexpr () {
        return left;
    }
    /**
     * Set the left sub-expression.  The new value may not be null.
     * 
     * @param expr new left sub-expression, an XPath 1.0 expression
     */
    public void setLeftSubexpr (Expr expr) {
        if (expr == null)
            throw new NullPointerException();
        left = expr;
    }
    /** @return the right sub-expression */
    public Expr getRightSubexpr () {
        return right;
    }
    /**
     * Set the rightt sub-expression.  The new value may not be null.
     * 
     * @param expr new right sub-expression, an XPath 1.0 expression
     */
    public void setRightSubexpr (Expr expr) {
        if (expr == null)
            throw new NullPointerException();
        right = expr;
    }
    
    // SERIALIZATION ////////////////////////////////////////////////
    /** @return XML expression in String form, recursing as necessary */
    protected String xmlBody(int indent) {
        StringBuffer spaces = new StringBuffer();
        for (int i = 0; i < indent; i++)
            spaces.append(' ');
         
        StringBuffer sb = new StringBuffer();
        sb.append(spaces.toString())
            .append("<left>\n").append(left.toXml(indent + 2))
            .append("</left>\n")
            .append("<right>\n").append(right.toXml(indent + 2))
            .append("</right>\n");
        return sb.toString();
    }

}
