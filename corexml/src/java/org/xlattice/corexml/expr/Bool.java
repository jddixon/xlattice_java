/* Bool.java */
package org.xlattice.corexml.expr;

/**
 * Token representing an XPath boolean expression.
 * 
 * @author Jim Dixon
 */
public class Bool extends Expr implements Token {

    /** its (immutable) value */
    private final Boolean value;
    
    public final static Bool TRUE  = new Bool(Boolean.TRUE);
    public final static Bool FALSE = new Bool(Boolean.FALSE);

    public Bool (Boolean b) {
        super("bool");
        value = b;
    }
    /**
     * Constructor with NodeSet argument.
     * <BR/> XXX STUBBED!
     */
    public Bool (NodeSet nodes) {
        this(Boolean.FALSE);
    }
    /** 
     * Constructor with Numeric argument, evaluating to false only if
     * 0.0.
     */
    public Bool(Numeric d) {
        this ( d.getValue() == 0.0 ? Boolean.FALSE : Boolean.TRUE);
    }
    /**
     * Constructor with String argument, evalutating to false only if
     * the String is null or equal to "".
     */
    public Bool(String s) {
        this ( ( s == null || s.length() == 0) ? 
                                        Boolean.FALSE : Boolean.TRUE);
    }

    // EXPR /////////////////////////////////////////////////////////
    /** @return the value of the expression */
    public Boolean getValue() {
        return value;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    public String toString() {
        return value.toString();
    }
    protected String xmlBody(int indent) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indent; i++)
            sb.append(' ');
        sb.append(toString());
        return sb.toString();
    }
}
