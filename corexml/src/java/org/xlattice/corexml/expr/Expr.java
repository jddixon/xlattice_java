/* Expr.java */
package org.xlattice.corexml.expr;

/**
 * Superclass for XPath 1.5 expressions.
 *
 * @author Jim Dixon
 */
public abstract class Expr {
    /** tag distinguishing the type of expression */
    protected final String tag;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /** 
     * Constructor.
     * @param t the tag distinguishing the expression type
     */
    public Expr (String t) {
        tag = t;
    }
    /** unreachable private constructor */
    private Expr () {
        tag = null;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    // BY CONVENTION toString() is for debugging, returns something 
    // like [class:info], eg [qname:fred.smith]
    
    /** @return the opening part of the XML serialization */
    protected String startElement () {
        return new StringBuffer("<").append(tag).append(">\n").toString();
    }
    /** @return the closing part of the XML serialization */
    protected String endElement () {
        return new StringBuffer("</").append(tag).append(">\n").toString();
    }
    /** @return the body of a subclass's XML serialization */
    protected abstract String xmlBody(int indent);
    /** 
     * XML serialization for an XPath 1.0 expression, recursive, 
     * with the body indented 2 spaces at each level
     *
     * @param indent number of spaces indented so far
     */
    public String toXml(int indent) {
        StringBuffer sb = new StringBuffer();
        if (indent <= 0) {
            sb.append(startElement())
              .append(xmlBody(indent + 2))
              .append(endElement());
        } else {
            StringBuffer spaces = new StringBuffer();
            for (int i = 0; i < indent; i++)
                spaces.append(' ');
            sb.append(spaces).append(startElement())
              .append(xmlBody(indent + 2))
              .append(spaces).append(endElement());
        }
        return sb.toString();
    }
    public String toXml() {
        return toXml (0);
    }
}
