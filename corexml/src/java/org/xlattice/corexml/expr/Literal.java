/* Literal.java */
package org.xlattice.corexml.expr;

/**
 * A literal, a token representing a singly or doubly quoted string.
 *
 * @author Jim Dixon
 */
public class Literal extends Expr implements Token {

    private String value;

    public final static Literal TRUE  = new Literal("true");
    public final static Literal FALSE = new Literal("false");

    /**
     * Accept a double or single quoted literal.  The quotation
     * marks are not passed to the constructor.  In this implementation,
     * characters are not escaped here; the text passed must be well-formed.
     * No attempt is made to deal with ${} constructs.
     * 
     * @throws NullPointerException if text is null
     */
    public Literal (String text) {
        super("string");
        if (text == null)
            throw new NullPointerException("null literal");
        // empty strings are acceptable
        value = text;
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return a reference to the unquoted String value of the literal */
    public String getValue() {
        return value;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /** Debugging toString for Expr inteface. */
    public String toString() {
        return new StringBuffer("[literal:'")
            .append(value).append("']").toString();
    }
    /** @return the literal in a form suitable for XML output */
    protected String xmlBody(int indent) {
        return new StringBuffer()
            .append(SpaceFactory.makeSpaces(indent))
            .append(value).toString();
    }
}
