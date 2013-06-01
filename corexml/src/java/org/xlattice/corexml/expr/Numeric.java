/* Numeric.java */
package org.xlattice.corexml.expr;

/**
 * A numeric token, a Double, one of XPath's fundamental data types.
 * In evaluating XPath expressions, all numbers are converted to this
 * type.  Specifically, there is no XPath integer data type;  all
 * XPath numbers are objects in Java terms.
 *
 * @author Jim Dixon
 */
public class Numeric extends Expr implements Token {

    private double value;

    public static final Numeric NaN = new Numeric (Double.NaN);
    public static final Numeric NEGATIVE_INFINITY 
                            = new Numeric (Double.NEGATIVE_INFINITY);
    public static final Numeric POSITIVE_INFINITY 
                            = new Numeric (Double.POSITIVE_INFINITY);
    public static final Numeric ZERO = new Numeric (0.0);
    public static final Numeric ONE  = new Numeric (1.0);

    /** 
     * Create a numeric token, given its value in String form.
     */
    public Numeric (String text) {
        this (Double.parseDouble(text));
    }
    /**
     * Create a numeric token, given its value as a Java double.
     * 
     * XXX possibly only used in debugging
     */
    public Numeric (double d) {
        super("number");
        value = d;
    }
    /**
     * Create a numeric token, given its value as a Java Double.
     */
    public Numeric (Double d) {
        this (d.doubleValue());
    }
    // PROPERTIES ///////////////////////////////////////////////////
    /** @return the double corresponding to the value of this token */
    public double getValue() {
        return value;
    }
    /**
     * No warning if strange results from truncation.
     *
     * @return a signed 32-bit integer, the truncated value of this token
     */
    public int intValue() {
        return (int)value;
    }
        
    // SERIALIZATION ////////////////////////////////////////////////
    public String toString() {
        return ExprParser.string( new Double(value) );
    }
    protected String xmlBody(int indent) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indent; i++)
            sb.append(' ');
        sb.append(toString());
        return sb.toString();
    }
}
