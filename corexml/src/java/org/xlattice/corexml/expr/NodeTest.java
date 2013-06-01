/* NodeTest.java */
package org.xlattice.corexml.expr;

/**
 * @author Jim Dixon
 **/

public class NodeTest extends Expr {

    private final Token type;       // a KLUDGE
    
    private Literal piName;
    
    public NodeTest (Token t) {
        super("nodetest");
        if (    !( (t instanceof NodeType)    || (t instanceof QName)
                || (t == ExprParser.SYM_STAR) || (t instanceof NCName) ) )
            throw new IllegalArgumentException("not a valid test: " 
                + t);
        type = t;
    }
    
    // PROPERTIES ///////////////////////////////////////////////////
    public Literal getPIName() {
        return piName;
    }
    public void setPIName(Literal lit) {
        if (type != ExprParser.NTYPE_PI)
            throw new IllegalArgumentException(
                "cannot add literal to node test of type " + type);
        piName = lit;
    }
    public Token getType () {
        return type;
    }
    // SERIALIZATION ////////////////////////////////////////////////
    // XXX THIS IS CONFUSED -- SHOULD NOT USE toString()
    public String xmlBody(int indent) {
        StringBuffer sb = new StringBuffer();
        String spaces = SpaceFactory.getInstance().makeSpaces(indent);
        if ( type instanceof NodeType ) {
            sb.append(spaces).append("<nodetype>")
              .append(((NodeType)type).toString()).append("</nodetype>\n");
        } else if ( type instanceof NCName ) {
            sb.append(spaces).append("<ncname>")
              .append(((NCName)type).toString()).append("</ncname>\n");
        } else if (type instanceof QName ) {
            sb.append(spaces).append("<qname>")
              .append(((QName)type).getName()).append("</qname>\n");
        } else if ((type instanceof Symbol) && (((Symbol)type).getIndex() == Symbol.STAR)) {
            sb.append(spaces).append("star\n");
        } else { 
            sb.append("<???>").append(type.toString()).append("</???>");
        }
        return sb.toString();
    }
}
