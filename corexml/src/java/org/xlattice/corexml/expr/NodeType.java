/* NodeType.java */
package org.xlattice.corexml.expr;

/**
 * Token representing an XML node type.
 * 
 * @author Jim Dixon
 */
public class NodeType implements FixedNameToken {
    
    private final int index;

    public static final int TEXT                    = 0;
    public static final int NODE                    = 1;
    public static final int COMMENT                 = 2;
    public static final int PROCESSING_INSTRUCTION  = 3;

    // XXX not in spec ?
    public static final int ELEMENT                 = 4;
    // conflicts with AxisName
    //public static final int ATTRIBUTE             = 5;
   
    /** string representation, useful in debugging */
    public static final String[] NAMES = {
        "text", "node", "comment", "processing-instruction"

        // XXX not in spec ?
        , "element" /* , "attribute" */
    };
    // CONSTRUCTORS /////////////////////////////////////////////////
    /** Create the Nth node type token. */
    public NodeType (int i) {
        if ( i < TEXT || i > PROCESSING_INSTRUCTION)
            throw new IllegalStateException("invalid node index");
        index = i;
    }
    /** Create a node type token given its string representation. */
    public NodeType (String s) {
        for (int i = 0; i < NAMES.length; i++) {
            if (s.equals(NAMES[i])) {
                index = i;
                return;
            }
        } 
        throw new IllegalStateException("invalid node type");
    }
    // FIXED NAME TOKEN /////////////////////////////////////////////
    /** @return a reference to the list of node type names */
    public static String [] getNames() {
        return NAMES;
    }
    /** @return the index of this node type */
    public int getIndex() {
        return index;
    }
    /** @return the string representation of this node type */
    public String getName() {
        return NAMES[index];
    }
    // SERIALIZATION ////////////////////////////////////////////////
    /** @return token in a form useful for debugging */
    public String toString() {
        return new StringBuffer("[nodetype:")
            .append(NAMES[index])
            .append("]").toString();
    }
}
